package com.identity4j.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.identity4j.connector.Connector;
import com.identity4j.connector.Connector.PasswordResetType;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.DefaultConnectorConfiguration;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.WebAuthenticationAPI;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.remote.secrets.PropertiesSecretService;
import com.identity4j.remote.secrets.SecretService;
import com.identity4j.util.MultiMap;
import com.identity4j.util.passwords.PasswordCharacteristics;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Identity4J HTTP bridge filter implementation.
 */
public class Identity4JRemoteFilter implements Filter {

    public static final String CONFIG_SECRET_SERVICE_CLASS = "secretServiceClass";
    public static final String CONFIG_SECRET_PROPERTIES_FILE = "secretPropertiesFile";
    public static final String CONFIG_HANDLE_TIMEOUT_SECONDS = "handleTimeoutSeconds";

    private static final Pattern SECRET_PATTERN = Pattern.compile("%(credential|secret)\\.([^.]+)\\.([^%]+)%");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ConnectorHolder> connectors = new ConcurrentHashMap<>();
    private final Map<String, StreamCursor> streams = new ConcurrentHashMap<>();
    private final Map<String, SessionHolder> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebLogonHolder> webLogons = new ConcurrentHashMap<>();

    private SecretService secretService;
    private long timeoutMillis = 30L * 60L * 1000L;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        timeoutMillis = parseLong(filterConfig.getInitParameter(CONFIG_HANDLE_TIMEOUT_SECONDS), 1800L) * 1000L;
        secretService = createSecretService(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        cleanupIdle();

        String path = requestPath(req);
        if (path.isEmpty() || "/".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        boolean json = acceptsJson(req);
        try {
            RouteResult result = dispatch(req, path);
            writeResult(resp, json, result);
        } catch (PrincipalAlreadyExistsException pae) {
            writeError(resp, json, 409, pae.getMessage());
        } catch (PrincipalNotFoundException pnfe) {
            writeError(resp, json, 404, pnfe.getMessage());
        } catch (InvalidLoginCredentialsException ilce) {
            writeError(resp, json, 404, ilce.getMessage());
        } catch (ApiException ae) {
            writeError(resp, json, ae.getStatus(), ae.getMessage());
        } catch (UnsupportedOperationException uoe) {
            writeError(resp, json, 405, uoe.getMessage() == null ? "Operation not supported" : uoe.getMessage());
        } catch (ConnectorException ce) {
            writeError(resp, json, 500, ce.getMessage() == null ? "Connector error" : ce.getMessage());
        } catch (Exception e) {
            writeError(resp, json, 500, e.getMessage() == null ? "Unexpected server error" : e.getMessage());
        }
    }

    @Override
    public void destroy() {
        for (ConnectorHolder holder : connectors.values()) {
            safeClose(holder.connector());
        }
        connectors.clear();
        streams.clear();
        sessions.clear();
        webLogons.clear();
        if (secretService != null) {
            safeClose(secretService);
        }
    }

    private RouteResult dispatch(HttpServletRequest req, String path) throws IOException {
        String method = req.getMethod().toUpperCase();
        String[] tokens = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (tokens.length == 0) {
            throw new ApiException(404, "No such endpoint");
        }

        return switch (tokens[0]) {
        case "streams" -> handleStreams(req, method, tokens);
        case "secrets" -> handleSecrets(req, method, tokens);
        case "connectors" -> handleConnectors(req, method, tokens);
        case "capabilities" -> handleCapabilities(req, method, tokens);
        case "password-characteristics" -> handlePasswordCharacteristics(req, method, tokens);
        case "logon" -> handleLogon(req, method, tokens);
        case "web-logon" -> handleWebLogon(req, method, tokens);
        case "complete-web-logon" -> handleCompleteWebLogon(req, method, tokens);
        case "check-credentials" -> handleCheckCredentials(req, method, tokens);
        case "change-password" -> handleChangePassword(req, method, tokens);
        case "set-password" -> handleSetPassword(req, method, tokens);
        case "identities" -> handleIdentities(req, method, tokens);
        case "identity-count" -> handleIdentityCount(req, method, tokens);
        case "identities-by-guid" -> handleIdentitiesByGuid(req, method, tokens);
        case "roles" -> handleRoles(req, method, tokens);
        case "role-count" -> handleRoleCount(req, method, tokens);
        case "roles-by-guid" -> handleRolesByGuid(req, method, tokens);
        case "locks" -> handleLocks(req, method, tokens);
        case "enablement" -> handleEnablement(req, method, tokens);
        default -> throw new ApiException(404, "No such endpoint");
        };
    }

    private RouteResult handleStreams(HttpServletRequest req, String method, String[] t) {
        if (t.length != 2) {
            throw new ApiException(404, "No such stream endpoint");
        }
        String streamHandle = t[1];
        StreamCursor stream = streams.get(streamHandle);
        if (stream == null) {
            throw new ApiException(404, "No such stream exists");
        }
        stream.touch();

        if ("DELETE".equals(method)) {
            streams.remove(streamHandle);
            return RouteResult.ok("Stream " + streamHandle + " closed");
        }

        if (!"GET".equals(method)) {
            throw new ApiException(405, "Method not allowed");
        }

        int items = parseInt(req.getParameter("items"), 1);
        int skip = parseInt(req.getParameter("skip"), 0);
        if (items > 0 && skip > 0) {
            throw new ApiException(400, "Use either items or skip in a single request");
        }

        if (skip > 0) {
            stream.skip(skip);
        }

        List<Map<String, Object>> page = stream.next(items);
        boolean eof = stream.eof();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("results", page);
        payload.put("eof", eof);
        return new RouteResult(200, true, "Page of " + page.size() + " items", payload, RouteKind.STREAM_PAGE);
    }

    private RouteResult handleSecrets(HttpServletRequest req, String method, String[] t) throws IOException {
        if ("GET".equals(method) && t.length == 1) {
            Set<String> aliases = secretService.aliases();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("secrets", new ArrayList<>(aliases));
            return new RouteResult(200, true, "There are " + aliases.size() + " secrets.", payload, RouteKind.NORMAL);
        }

        if (t.length != 2) {
            throw new ApiException(404, "No such secrets endpoint");
        }

        String alias = t[1];
        Map<String, String> body = requestParams(req);

        if ("POST".equals(method)) {
            secretService.create(alias, body);
            return new RouteResult(201, true, "Secret stored using properties file.", Collections.emptyMap(), RouteKind.NORMAL);
        }
        if ("PATCH".equals(method)) {
            secretService.patch(alias, body);
            return RouteResult.ok("Secret updated using properties file.");
        }
        if ("HEAD".equals(method)) {
            if (!secretService.exists(alias)) {
                throw new ApiException(404, "The secret " + alias + " does not exists");
            }
            return RouteResult.ok("Secret " + alias + " exists.");
        }
        if ("DELETE".equals(method)) {
            secretService.delete(alias);
            return RouteResult.ok("Secret " + alias + " deleted.");
        }
        throw new ApiException(405, "Method not allowed");
    }

    private RouteResult handleConnectors(HttpServletRequest req, String method, String[] t) throws IOException {
        if ("POST".equals(method) && t.length == 2) {
            String connectorClass = t[1];
            Map<String, String> values = requestParams(req);
            values.put(ConnectorBuilder.CONNECTOR_CLASS, connectorClass);
            resolveSecrets(values);

            MultiMap params = MultiMap.fromMapSingle(values);
            ConnectorBuilder builder = new ConnectorBuilder();
            Connector<?> connector = builder.buildConnector(new DefaultConnectorConfiguration(params));
            String handle = newHandle();
            connectors.put(handle, new ConnectorHolder(connector));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("handle", handle);
            return new RouteResult(201, true, "The connector was opened.", payload, RouteKind.NORMAL);
        }

        if ("DELETE".equals(method) && t.length == 2) {
            String handle = t[1];
            ConnectorHolder holder = connectors.remove(handle);
            if (holder == null) {
                throw new ApiException(404, "No such connector instance with handle " + handle);
            }
            safeClose(holder.connector());
            streams.entrySet().removeIf(e -> handle.equals(e.getValue().connectorHandle()));
            sessions.entrySet().removeIf(e -> handle.equals(e.getValue().connectorHandle()));
            webLogons.entrySet().removeIf(e -> handle.equals(e.getValue().connectorHandle()));
            return RouteResult.ok("Connector " + handle + " closed");
        }

        throw new ApiException(404, "No such connectors endpoint");
    }

    private RouteResult handleCapabilities(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such capabilities endpoint");
        }
        Connector<?> connector = connector(t[1]);
        Set<ConnectorCapability> capabilities = connector.getCapabilities();
        List<String> names = new ArrayList<>();
        if (capabilities != null) {
            for (ConnectorCapability capability : capabilities) {
                names.add(capability.name());
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("capabilities", names);
        return new RouteResult(200, true, "There are " + names.size() + " capabilities.", payload, RouteKind.NORMAL);
    }

    private RouteResult handlePasswordCharacteristics(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length < 2 || t.length > 3) {
            throw new ApiException(404, "No such password characteristics endpoint");
        }

        Connector<?> connector = connector(t[1]);
        if (t.length == 2) {
            List<Map<String, Object>> rows = new ArrayList<>();
            Iterator<? extends PasswordCharacteristics> it = connector.getPasswordPolicies();
            if (it != null) {
                while (it.hasNext()) {
                    rows.add(passwordCharacteristicsToMap(it.next()));
                }
            } else if (connector.getPasswordCharacteristics() != null) {
                rows.add(passwordCharacteristicsToMap(connector.getPasswordCharacteristics()));
            }

            String streamHandle = newHandle();
            streams.put(streamHandle, new StreamCursor(t[1], rows));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("handle", streamHandle);
            return new RouteResult(200, true, "The password characteristics stream was opened.", payload, RouteKind.NORMAL);
        }

        String id = t[2];
        PasswordCharacteristics pc;
        if ("default".equals(id)) {
            pc = connector.getPasswordCharacteristics();
        } else {
            pc = findPasswordCharacteristicsById(connector, id);
        }
        if (pc == null) {
            throw new ApiException(404, "No such password characteristics " + id);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("characteristics", passwordCharacteristicsToMap(pc));
        return new RouteResult(200, true, "Password characteristics " + id + ".", payload, RouteKind.NORMAL);
    }

    private RouteResult handleLogon(HttpServletRequest req, String method, String[] t) throws IOException {
        if ("POST".equals(method) && t.length == 2) {
            Map<String, String> params = requestParams(req);
            String username = require(params, "username");
            String password = require(params, "password");
            Connector<?> connector = connector(t[1]);

            Identity identity = connector.logon(username, password.toCharArray());
            String sessionHandle = newHandle();
            sessions.put(sessionHandle, new SessionHolder(t[1], identity));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessionHandle", sessionHandle);
            return new RouteResult(201, true,
                    "The credentials were validated as OK and a session was created.", payload, RouteKind.NORMAL);
        }

        if ("DELETE".equals(method) && t.length == 2) {
            String sessionHandle = t[1];
            SessionHolder session = sessions.remove(sessionHandle);
            if (session == null) {
                throw new ApiException(404, "No such session");
            }
            Connector<?> connector = connector(session.connectorHandle());
            connector.logoff(session.identity());
            return RouteResult.ok("The session was logged off.");
        }

        throw new ApiException(404, "No such logon endpoint");
    }

    private RouteResult handleWebLogon(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such web-logon endpoint");
        }
        Connector<?> connector = connector(t[1]);
        String returnTo = req.getParameter("redirectURI");
        if (returnTo == null || returnTo.isEmpty()) {
            returnTo = req.getParameter("returnTo");
        }
        if (returnTo == null || returnTo.isEmpty()) {
            throw new ApiException(400, "Missing redirectURI parameter");
        }

        WebAuthenticationAPI web = connector.startAuthentication();
        String webHandle = web.getId();
        if (webHandle == null || webHandle.isEmpty()) {
            webHandle = newHandle();
        }
        String amendedReturn = addWebLogonHandle(returnTo, webHandle);
        String location = web.open(amendedReturn);
        webLogons.put(webHandle, new WebLogonHolder(t[1], web));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("location", location);
        return new RouteResult(302, true, "The web logon session was started.", payload, RouteKind.REDIRECT);
    }

    private RouteResult handleCompleteWebLogon(HttpServletRequest req, String method, String[] t) throws IOException {
        if (!"GET".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such complete-web-logon endpoint");
        }

        String webHandle = t[1];
        WebLogonHolder holder = webLogons.remove(webHandle);
        if (holder == null) {
            throw new ApiException(404, "No such web logon session");
        }

        WebAuthenticationAPI web = holder.webAuthenticationAPI();
        WebAuthenticationAPI.ReturnStatus returnStatus = web.validate(req.getParameterMap());

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("id", web.getId());
        details.put("username", web.getUsername());
        details.put("state", web.getState());
        details.put("created", web.getCreated());
        details.put("status", web.getStatus() == null ? null : web.getStatus().name());
        details.put("returnStatus", returnStatus == null ? null : returnStatus.name());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("webLogon", details);
        return new RouteResult(200, true, "The web logon session is complete.", payload, RouteKind.NORMAL);
    }

    private RouteResult handleCheckCredentials(HttpServletRequest req, String method, String[] t) throws IOException {
        if (!"POST".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such check-credentials endpoint");
        }
        Map<String, String> params = requestParams(req);
        String username = require(params, "username");
        String password = require(params, "password");
        boolean ok = connector(t[1]).checkCredentials(username, password.toCharArray());
        if (!ok) {
            throw new ApiException(404, "Incorrect credentials");
        }
        return RouteResult.ok("The credentials were validated as OK.");
    }

    private RouteResult handleChangePassword(HttpServletRequest req, String method, String[] t) throws IOException {
        if (!"POST".equals(method) || t.length != 3) {
            throw new ApiException(404, "No such change-password endpoint");
        }
        Map<String, String> params = requestParams(req);
        String currentPassword = require(params, "currentPassword");
        String password = require(params, "password");

        Connector<?> connector = connector(t[1]);
        Identity identity = connector.getIdentityByName(t[2], false);
        connector.changePassword(t[2], identity.getGuid(), currentPassword.toCharArray(), password.toCharArray());
        return RouteResult.ok("The credentials were updated.");
    }

    private RouteResult handleSetPassword(HttpServletRequest req, String method, String[] t) throws IOException {
        if (!"POST".equals(method) || t.length != 3) {
            throw new ApiException(404, "No such set-password endpoint");
        }

        Map<String, String> params = requestParams(req);
        String password = require(params, "password");
        boolean forceChange = Boolean.parseBoolean(params.getOrDefault("forceChangeAtNextLogon", "true"));
        PasswordResetType resetType = parseResetType(params.getOrDefault("passwordResetType", "ADMINISTRATIVE"));

        Connector<?> connector = connector(t[1]);
        Identity identity = connector.getIdentityByName(t[2], false);
        connector.setPassword(t[2], identity.getGuid(), password.toCharArray(), forceChange, resetType);
        return RouteResult.ok("The credentials were reset.");
    }

    private RouteResult handleIdentities(HttpServletRequest req, String method, String[] t) throws IOException {
        if ("GET".equals(method) && t.length == 2) {
            Connector<?> connector = connector(t[1]);
            List<Map<String, Object>> rows = new ArrayList<>();
            Iterator<Identity> it = connector.allIdentities(OperationContext.createDefault());
            while (it.hasNext()) {
                rows.add(identityToMap(it.next()));
            }
            String streamHandle = newHandle();
            streams.put(streamHandle, new StreamCursor(t[1], rows));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("handle", streamHandle);
            return new RouteResult(200, true, "Listing identities.", payload, RouteKind.NORMAL);
        }

        if ("HEAD".equals(method) && t.length == 3) {
            boolean exists = connector(t[1]).isIdentityNameInUse(t[2]);
            if (!exists) {
                throw new ApiException(404, "The identity " + t[2] + " does not exists");
            }
            return RouteResult.ok("Identity " + t[2] + " exists.");
        }

        if ("GET".equals(method) && t.length == 3) {
            boolean withRoles = boolParameter(req, "withRoles", false);
            Identity identity = connector(t[1]).getIdentityByName(t[2], withRoles);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("identity", identityToMap(identity));
            return new RouteResult(200, true, "Identity " + t[2] + ".", payload, RouteKind.NORMAL);
        }

        if ("POST".equals(method) && t.length == 3) {
            Map<String, String> params = requestParams(req);
            Connector<?> connector = connector(t[1]);
            Identity identity = new IdentityImpl(params.get("principal.guid"), t[2]);
            applyIdentityFields((IdentityImpl) identity, params, false);
            String password = params.get("password");
            Identity created = connector.createIdentity(identity, password == null ? null : password.toCharArray());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("identity", identityToMap(created));
            return new RouteResult(200, true, "Identity " + t[2] + " created.", payload, RouteKind.NORMAL);
        }

        if ("PATCH".equals(method) && t.length == 3) {
            Map<String, String> params = requestParams(req);
            Connector<?> connector = connector(t[1]);
            Identity existing = connector.getIdentityByName(t[2], true);
            if (existing instanceof IdentityImpl impl) {
                applyIdentityFields(impl, params, true);
                connector.updateIdentity(impl);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("identity", identityToMap(impl));
                return new RouteResult(200, true, "Identity " + t[2] + " updated.", payload, RouteKind.NORMAL);
            }
            throw new ApiException(500, "Identity implementation is not mutable");
        }

        if ("DELETE".equals(method) && t.length == 3) {
            connector(t[1]).deleteIdentity(t[2]);
            return RouteResult.ok("Identity " + t[2] + " deleted.");
        }

        throw new ApiException(404, "No such identities endpoint");
    }

    private RouteResult handleIdentityCount(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such identity-count endpoint");
        }
        long users = connector(t[1]).countIdentities();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("users", users);
        return new RouteResult(200, true, "There are " + users + " identities.", payload, RouteKind.NORMAL);
    }

    private RouteResult handleIdentitiesByGuid(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 3) {
            throw new ApiException(404, "No such identities-by-guid endpoint");
        }
        Identity identity = connector(t[1]).getIdentityByGuid(t[2]);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("identity", identityToMap(identity));
        return new RouteResult(200, true, "Identity " + t[2] + ".", payload, RouteKind.NORMAL);
    }

    private RouteResult handleRoles(HttpServletRequest req, String method, String[] t) throws IOException {
        if ("GET".equals(method) && t.length == 2) {
            Connector<?> connector = connector(t[1]);
            List<Map<String, Object>> rows = new ArrayList<>();
            Iterator<Role> it = connector.allRoles(OperationContext.createDefault());
            while (it.hasNext()) {
                rows.add(roleToMap(it.next()));
            }
            String streamHandle = newHandle();
            streams.put(streamHandle, new StreamCursor(t[1], rows));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("handle", streamHandle);
            return new RouteResult(200, true, "Listing roles.", payload, RouteKind.NORMAL);
        }

        if ("HEAD".equals(method) && t.length == 3) {
            boolean exists = connector(t[1]).isRoleNameInUse(t[2]);
            if (!exists) {
                throw new ApiException(404, "The role " + t[2] + " does not exists");
            }
            return RouteResult.ok("The role " + t[2] + " exists.");
        }

        if ("GET".equals(method) && t.length == 3) {
            Role role = connector(t[1]).getRoleByName(t[2]);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("role", roleToMap(role));
            return new RouteResult(200, true, "Role " + t[2] + ".", payload, RouteKind.NORMAL);
        }

        if ("POST".equals(method) && t.length == 3) {
            Map<String, String> params = requestParams(req);
            RoleImpl role = new RoleImpl(params.get("principal.guid"), t[2]);
            applyPrincipalAttributes(role, params, true);
            Role created = connector(t[1]).createRole(role);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("role", roleToMap(created));
            return new RouteResult(200, true, "Role " + t[2] + " created.", payload, RouteKind.NORMAL);
        }

        if ("PATCH".equals(method) && t.length == 3) {
            Map<String, String> params = requestParams(req);
            Connector<?> connector = connector(t[1]);
            Role role = connector.getRoleByName(t[2]);
            if (role instanceof RoleImpl impl) {
                applyPrincipalAttributes(impl, params, false);
                connector.updateRole(impl);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("role", roleToMap(impl));
                return new RouteResult(200, true, "Role " + t[2] + " updated.", payload, RouteKind.NORMAL);
            }
            throw new ApiException(500, "Role implementation is not mutable");
        }

        if ("DELETE".equals(method) && t.length == 3) {
            connector(t[1]).deleteRole(t[2]);
            return RouteResult.ok("Role " + t[2] + " deleted.");
        }

        throw new ApiException(404, "No such roles endpoint");
    }

    private RouteResult handleRoleCount(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 2) {
            throw new ApiException(404, "No such role-count endpoint");
        }
        long users = connector(t[1]).countRoles();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("users", users);
        return new RouteResult(200, true, "There are " + users + " roles.", payload, RouteKind.NORMAL);
    }

    private RouteResult handleRolesByGuid(HttpServletRequest req, String method, String[] t) {
        if (!"GET".equals(method) || t.length != 3) {
            throw new ApiException(404, "No such roles-by-guid endpoint");
        }
        Connector<?> connector = connector(t[1]);
        Iterator<Role> it = connector.allRoles(OperationContext.createDefault());
        Role found = null;
        while (it.hasNext()) {
            Role role = it.next();
            if (Objects.equals(role.getGuid(), t[2])) {
                found = role;
                break;
            }
        }
        if (found == null) {
            throw new ApiException(404, "No such role with guid " + t[2]);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("role", roleToMap(found));
        return new RouteResult(200, true, "Role " + t[2] + ".", payload, RouteKind.NORMAL);
    }

    private RouteResult handleLocks(HttpServletRequest req, String method, String[] t) {
        if (t.length != 3) {
            throw new ApiException(404, "No such locks endpoint");
        }
        Connector<?> connector = connector(t[1]);
        Identity identity = connector.getIdentityByName(t[2], false);
        if ("POST".equals(method)) {
            connector.lockIdentity(identity);
            return RouteResult.ok("Identity " + t[2] + " locked.");
        }
        if ("DELETE".equals(method)) {
            connector.unlockIdentity(identity);
            return RouteResult.ok("Identity " + t[2] + " unlocked.");
        }
        throw new ApiException(405, "Method not allowed");
    }

    private RouteResult handleEnablement(HttpServletRequest req, String method, String[] t) {
        if (t.length != 3) {
            throw new ApiException(404, "No such enablement endpoint");
        }
        Connector<?> connector = connector(t[1]);
        Identity identity = connector.getIdentityByName(t[2], false);
        if ("POST".equals(method)) {
            connector.enableIdentity(identity);
            return RouteResult.ok("Identity " + t[2] + " enabled.");
        }
        if ("DELETE".equals(method)) {
            connector.disableIdentity(identity);
            return RouteResult.ok("Identity " + t[2] + " disabled.");
        }
        throw new ApiException(405, "Method not allowed");
    }

    private Connector<?> connector(String handle) {
        ConnectorHolder holder = connectors.get(handle);
        if (holder == null) {
            throw new ApiException(404, "No such connector instance with handle " + handle);
        }
        holder.touch();
        return holder.connector();
    }

    private Map<String, String> requestParams(HttpServletRequest req) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            if (e.getValue() != null && e.getValue().length > 0) {
                values.put(e.getKey(), e.getValue()[0]);
            }
        }

        if (!hasBody(req)) {
            return values;
        }

        String body = readBody(req);
        if (body == null || body.isBlank()) {
            return values;
        }

        String contentType = req.getContentType() == null ? "" : req.getContentType().toLowerCase();

        if (contentType.startsWith("application/json")) {
            Map<String, Object> json = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            flattenJson("", json, values);
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            values.putAll(parseUrlEncoded(body));
        } else {
            parseDotted(body, values);
        }

        return values;
    }

    private void resolveSecrets(Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            Matcher m = SECRET_PATTERN.matcher(e.getValue());
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            while (m.find()) {
                sb.append(e.getValue(), idx, m.start());
                String alias = m.group(2);
                String prop = m.group(3);
                Map<String, String> secret = secretService.get(alias);
                if (!secret.containsKey(prop)) {
                    throw new ApiException(404, "The secret " + alias + " does not contain " + prop);
                }
                sb.append(secret.get(prop));
                idx = m.end();
            }
            if (idx > 0) {
                sb.append(e.getValue().substring(idx));
                e.setValue(sb.toString());
            }
        }
    }

    private void applyIdentityFields(IdentityImpl identity, Map<String, String> params, boolean patch) {
        if (!patch || params.containsKey("fullName")) {
            identity.setFullName(params.get("fullName"));
        }
        if (!patch || params.containsKey("otherName")) {
            identity.setOtherName(params.get("otherName"));
        }
        applyPrincipalAttributes(identity, params, patch);
    }

    private void applyPrincipalAttributes(Principal principal, Map<String, String> params, boolean includeSystemFields) {
        if (includeSystemFields && params.containsKey("principal.system")) {
            if (principal instanceof IdentityImpl identityImpl) {
                identityImpl.setSystem(Boolean.parseBoolean(params.get("principal.system")));
            } else if (principal instanceof RoleImpl roleImpl) {
                roleImpl.setSystem(Boolean.parseBoolean(params.get("principal.system")));
            }
        }

        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("principal.attributes.")) {
                continue;
            }
            String attr = key.substring("principal.attributes.".length());
            String normalized = normalizeIndexedKey(attr);
            grouped.computeIfAbsent(normalized, k -> new ArrayList<>()).add(e.getValue());
        }

        for (Map.Entry<String, List<String>> e : grouped.entrySet()) {
            principal.setAttribute(e.getKey(), e.getValue().toArray(new String[0]));
        }
    }

    private String normalizeIndexedKey(String key) {
        int idx = key.indexOf('[');
        if (idx > 0 && key.endsWith("]")) {
            return key.substring(0, idx);
        }
        return key;
    }

    private Map<String, Object> identityToMap(Identity identity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullName", identity.getFullName());
        map.put("lastLogon", identity.getLastSignOnDate() == null ? null : identity.getLastSignOnDate().getTime());
        map.put("passwordStatus", passwordStatusToMap(identity.getPasswordStatus()));
        map.put("accountStatus", accountStatusToMap(identity.getAccountStatus()));
        map.put("otherName", identity.getOtherName());

        Map<String, Object> addresses = new LinkedHashMap<>();
        for (com.identity4j.connector.Media m : com.identity4j.connector.Media.values()) {
            String v = identity.getAddress(m);
            if (v != null) {
                addresses.put(m.name().toLowerCase(), v);
            }
        }
        map.put("addresses", addresses);
        map.put("roles", rolesToList(identity.getRoles()));
        map.put("principal", principalToMap(identity));
        return map;
    }

    private Map<String, Object> roleToMap(Role role) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("roles", rolesToList(role.getRoles()));
        map.put("principal", principalToMap(role));
        return map;
    }

    private List<Map<String, Object>> rolesToList(Role[] roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Role role : roles) {
            if (role != null) {
                list.add(principalToMap(role));
            }
        }
        return list;
    }

    private Map<String, Object> principalToMap(Principal principal) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", principal.getPrincipalName());
        map.put("guid", principal.getGuid());
        map.put("system", principal.isSystem());

        Map<String, Object> attributes = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : principal.getAttributes().entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            if (e.getValue().length == 1) {
                attributes.put(e.getKey(), e.getValue()[0]);
            } else {
                attributes.put(e.getKey(), Arrays.asList(e.getValue()));
            }
        }
        map.put("attributes", attributes);
        return map;
    }

    private Map<String, Object> passwordStatusToMap(PasswordStatus status) {
        if (status == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("expire", status.getExpire() == null ? null : status.getExpire().getTime());
        map.put("lastChange", status.getLastChange() == null ? null : status.getLastChange().getTime());
        map.put("unlocked", status.getUnlocked() == null ? null : status.getUnlocked().getTime());
        map.put("type", status.getType() == null ? null : status.getType().name());
        map.put("warn", status.getWarn() == null ? null : status.getWarn().getTime());
        map.put("disable", status.getDisable() == null ? null : status.getDisable().getTime());
        map.put("needChange", status.isNeedChange());
        return map;
    }

    private Map<String, Object> accountStatusToMap(AccountStatus status) {
        if (status == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("expire", status.getExpire() == null ? null : status.getExpire().getTime());
        map.put("locked", status.getLocked() == null ? null : status.getLocked().getTime());
        map.put("unlocked", status.getUnlocked() == null ? null : status.getUnlocked().getTime());
        map.put("type", status.getType() == null ? null : status.getType().name());
        map.put("disabled", status.isDisabled());
        return map;
    }

    private Map<String, Object> passwordCharacteristicsToMap(PasswordCharacteristics pc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("veryStrongFactor", pc.getVeryStrongFactor());
        map.put("minimumSize", pc.getMinimumSize());
        map.put("maximumSize", pc.getMaximumSize());
        map.put("requiredMatches", pc.getRequiredMatches());
        map.put("minimumLowerCase", pc.getMinimumLowerCase());
        map.put("minimumUpperCase", pc.getMinimumUpperCase());
        map.put("minimumDigits", pc.getMinimumDigits());
        map.put("minimumSymbols", pc.getMinimumSymbols());
        map.put("historySize", pc.getHistorySize());
        map.put("symbols", pc.getSymbols() == null ? null : String.valueOf(pc.getSymbols()));
        map.put("dictionaryWordsAllowed", pc.isDictionaryWordsAllowed());
        map.put("additionalAnalysis", pc.isAdditionalAnalysis());
        map.put("minStrength", pc.getMinStrength());
        map.put("containUsername", pc.isContainUsername());
        map.put("attributes", pc.getAttributes() == null ? Collections.emptyMap() : pc.getAttributes());
        return map;
    }

    private PasswordCharacteristics findPasswordCharacteristicsById(Connector<?> connector, String id) {
        Iterator<? extends PasswordCharacteristics> it = connector.getPasswordPolicies();
        if (it == null) {
            return null;
        }
        while (it.hasNext()) {
            PasswordCharacteristics pc = it.next();
            Map<String, String> attributes = pc.getAttributes();
            if (attributes != null) {
                String name = attributes.get("name");
                if (id.equals(name)) {
                    return pc;
                }
                String pid = attributes.get("id");
                if (id.equals(pid)) {
                    return pc;
                }
            }
        }
        return null;
    }

    private void writeResult(HttpServletResponse resp, boolean json, RouteResult result) throws IOException {
        if (json) {
            resp.setStatus(200);
            resp.setContentType("application/json");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", result.success());
            body.put("message", result.message());
            body.put("code", result.status());
            body.putAll(result.payload());
            if (result.kind() == RouteKind.REDIRECT && result.payload().containsKey("location")) {
                resp.setHeader("Location", String.valueOf(result.payload().get("location")));
            }
            objectMapper.writeValue(resp.getOutputStream(), body);
            return;
        }

        resp.setStatus(result.status());
        resp.setContentType("text/plain");
        if (result.kind() == RouteKind.REDIRECT && result.payload().containsKey("location")) {
            resp.setHeader("Location", String.valueOf(result.payload().get("location")));
        }
        PrintWriter writer = resp.getWriter();
        writePlainBody(writer, result);
        writer.flush();
    }

    private void writeError(HttpServletResponse resp, boolean json, int code, String message) throws IOException {
        if (json) {
            resp.setStatus(200);
            resp.setContentType("application/json");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", message);
            body.put("code", code);
            objectMapper.writeValue(resp.getOutputStream(), body);
            return;
        }

        resp.setStatus(code);
        resp.setContentType("text/plain");
        resp.getWriter().println(message == null ? "Error" : message);
    }

    private void writePlainBody(PrintWriter writer, RouteResult result) {
        if (result.payload().isEmpty()) {
            return;
        }

        if (result.kind() == RouteKind.STREAM_PAGE) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) result.payload().getOrDefault("results", Collections.emptyList());
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> flat = flattenObject(rows.get(i));
                for (Map.Entry<String, String> e : flat.entrySet()) {
                    writer.println(e.getKey() + "=" + e.getValue());
                }
                writer.println();
            }
            boolean eof = Boolean.TRUE.equals(result.payload().get("eof"));
            if (eof) {
                writer.println();
            }
            return;
        }

        if (result.payload().containsKey("handle")) {
            writer.println(result.payload().get("handle"));
            return;
        }
        if (result.payload().containsKey("sessionHandle")) {
            writer.println(result.payload().get("sessionHandle"));
            return;
        }
        if (result.payload().containsKey("users")) {
            writer.println(result.payload().get("users"));
            return;
        }
        if (result.payload().containsKey("secrets")) {
            @SuppressWarnings("unchecked")
            List<String> aliases = (List<String>) result.payload().get("secrets");
            for (String alias : aliases) {
                writer.println(alias);
            }
            return;
        }

        Map<String, String> flat = flattenObject(result.payload());
        for (Map.Entry<String, String> e : flat.entrySet()) {
            writer.println(e.getKey() + "=" + e.getValue());
        }
    }

    private Map<String, String> flattenObject(Map<String, Object> object) {
        Map<String, String> out = new LinkedHashMap<>();
        flatten("", object, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Object value, Map<String, String> out) {
        if (value == null) {
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                flatten(prefix.isEmpty() ? key : prefix + "." + key, e.getValue(), out);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flatten(prefix + "[" + i + "]", list.get(i), out);
            }
            return;
        }
        if (value instanceof String[] arr) {
            for (int i = 0; i < arr.length; i++) {
                out.put(prefix + "[" + i + "]", arr[i]);
            }
            return;
        }
        out.put(prefix, String.valueOf(value));
    }

    private void flattenJson(String prefix, Map<String, Object> source, Map<String, String> out) {
        for (Map.Entry<String, Object> e : source.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            Object value = e.getValue();
            if (value instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) map;
                flattenJson(key, nested, out);
            } else if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    out.put(key + "[" + i + "]", String.valueOf(list.get(i)));
                }
            } else if (value != null) {
                out.put(key, String.valueOf(value));
            }
        }
    }

    private void parseDotted(String body, Map<String, String> out) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                if (idx < 1) {
                    continue;
                }
                out.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        }
    }

    private Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            if (idx < 0) {
                out.put(urlDecode(pair), "");
            } else {
                out.put(urlDecode(pair.substring(0, idx)), urlDecode(pair.substring(idx + 1)));
            }
        }
        return out;
    }

    private String urlDecode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = req.getInputStream();
                InputStreamReader ir = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(ir)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private boolean hasBody(HttpServletRequest req) {
        return req.getContentLengthLong() > 0 || req.getHeader("Transfer-Encoding") != null;
    }

    private boolean acceptsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.toLowerCase().contains("application/json");
    }

    private String requestPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    private String require(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isEmpty()) {
            throw new ApiException(400, "Missing required field " + key);
        }
        return value;
    }

    private boolean boolParameter(HttpServletRequest req, String key, boolean defaultValue) {
        String value = req.getParameter(key);
        if (value == null) {
            return req.getQueryString() != null && req.getQueryString().contains(key);
        }
        if (value.isEmpty()) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    private PasswordResetType parseResetType(String value) {
        try {
            return PasswordResetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new ApiException(400, "Invalid passwordResetType " + value);
        }
    }

    private String addWebLogonHandle(String returnTo, String webHandle) {
        if (returnTo.contains("?")) {
            return returnTo + "&webLogonHandle=" + webHandle;
        }
        return returnTo + "?webLogonHandle=" + webHandle;
    }

    private SecretService createSecretService(FilterConfig filterConfig) {
        String className = filterConfig.getInitParameter(CONFIG_SECRET_SERVICE_CLASS);
        if (className == null || className.isBlank()) {
            String file = filterConfig.getInitParameter(CONFIG_SECRET_PROPERTIES_FILE);
            return file == null || file.isBlank() ? new PropertiesSecretService() : new PropertiesSecretService(file);
        }
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof SecretService)) {
                throw new IllegalStateException(className + " does not implement SecretService");
            }
            return (SecretService) instance;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate SecretService " + className, e);
        }
    }

    private void cleanupIdle() {
        long cutoff = System.currentTimeMillis() - timeoutMillis;

        Set<String> expiredConnectors = new LinkedHashSet<>();
        for (Map.Entry<String, ConnectorHolder> e : connectors.entrySet()) {
            if (e.getValue().touched() < cutoff) {
                expiredConnectors.add(e.getKey());
            }
        }
        for (String handle : expiredConnectors) {
            ConnectorHolder holder = connectors.remove(handle);
            if (holder != null) {
                safeClose(holder.connector());
            }
        }

        streams.entrySet().removeIf(e -> e.getValue().touched() < cutoff || expiredConnectors.contains(e.getValue().connectorHandle()));
        sessions.entrySet().removeIf(e -> e.getValue().touched() < cutoff || expiredConnectors.contains(e.getValue().connectorHandle()));
        webLogons.entrySet().removeIf(e -> e.getValue().touched() < cutoff || expiredConnectors.contains(e.getValue().connectorHandle()));
    }

    private void safeClose(AutoCloseable c) {
        try {
            c.close();
        } catch (Exception ignored) {
            // Best-effort cleanup
        }
    }

    private String newHandle() {
        long n = Math.abs(ThreadLocalRandom.current().nextLong());
        return Long.toString(n, 36);
    }

    private int parseInt(String value, int def) {
        if (value == null || value.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ApiException(400, "Invalid numeric value " + value);
        }
    }

    private long parseLong(String value, long def) {
        if (value == null || value.isBlank()) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    private static final class ConnectorHolder {
        private final Connector<?> connector;
        private long touched;

        ConnectorHolder(Connector<?> connector) {
            this.connector = connector;
            this.touched = Instant.now().toEpochMilli();
        }

        Connector<?> connector() {
            return connector;
        }

        long touched() {
            return touched;
        }

        void touch() {
            this.touched = Instant.now().toEpochMilli();
        }
    }

    private static final class StreamCursor {
        private final String connectorHandle;
        private final List<Map<String, Object>> rows;
        private int index;
        private long touched;

        StreamCursor(String connectorHandle, List<Map<String, Object>> rows) {
            this.connectorHandle = connectorHandle;
            this.rows = new ArrayList<>(rows);
            this.touched = Instant.now().toEpochMilli();
        }

        String connectorHandle() {
            return connectorHandle;
        }

        long touched() {
            return touched;
        }

        void touch() {
            touched = Instant.now().toEpochMilli();
        }

        void skip(int amount) {
            index = Math.min(rows.size(), index + amount);
            touch();
        }

        List<Map<String, Object>> next(int amount) {
            touch();
            int limit = amount < 1 ? 1 : amount;
            int end = Math.min(rows.size(), index + limit);
            List<Map<String, Object>> page = new ArrayList<>();
            for (int i = index; i < end; i++) {
                page.add(rows.get(i));
            }
            index = end;
            return page;
        }

        boolean eof() {
            return index >= rows.size();
        }
    }

    private static final class SessionHolder {
        private final String connectorHandle;
        private final Identity identity;
        private long touched;

        SessionHolder(String connectorHandle, Identity identity) {
            this.connectorHandle = connectorHandle;
            this.identity = identity;
            this.touched = Instant.now().toEpochMilli();
        }

        String connectorHandle() {
            return connectorHandle;
        }

        Identity identity() {
            return identity;
        }

        long touched() {
            return touched;
        }

        void touch() {
            this.touched = Instant.now().toEpochMilli();
        }
    }

    private static final class WebLogonHolder {
        private final String connectorHandle;
        private final WebAuthenticationAPI webAuthenticationAPI;
        private long touched;

        WebLogonHolder(String connectorHandle, WebAuthenticationAPI webAuthenticationAPI) {
            this.connectorHandle = connectorHandle;
            this.webAuthenticationAPI = webAuthenticationAPI;
            this.touched = Instant.now().toEpochMilli();
        }

        String connectorHandle() {
            return connectorHandle;
        }

        WebAuthenticationAPI webAuthenticationAPI() {
            return webAuthenticationAPI;
        }

        long touched() {
            return touched;
        }

        void touch() {
            this.touched = Instant.now().toEpochMilli();
        }
    }

    private record RouteResult(int status, boolean success, String message, Map<String, Object> payload, RouteKind kind) {
        static RouteResult ok(String message) {
            return new RouteResult(200, true, message, Collections.emptyMap(), RouteKind.NORMAL);
        }
    }

    private enum RouteKind {
        NORMAL,
        STREAM_PAGE,
        REDIRECT
    }

    private static final class ApiException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final int status;

        ApiException(int status, String message) {
            super(message);
            this.status = status;
        }

        int getStatus() {
            return status;
        }
    }
}
