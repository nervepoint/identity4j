package com.identity4j.remote.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Count;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.WebAuthenticationAPI;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Connector that forwards all operations to the identity4j-remote servlet API.
 */
public class RemoteConnector extends AbstractConnector<RemoteConnectorConfiguration> {

    private static final Set<String> RESERVED_CONFIG_KEYS = Set.of(
            RemoteConnectorConfiguration.KEY_URI,
            RemoteConnectorConfiguration.REMOTE_CONNECTOR_CLASS,
            "i4jConnectorClass",
            "i4jConfigurationClass");

    private RemoteApiClient api;
    private String connectorHandle;
    private boolean open;
    private Set<ConnectorCapability> capabilities = new LinkedHashSet<>();

    private SecretServiceClient secretServiceClient;

    public SecretServiceClient secretServiceClient() {
        return secretServiceClient;
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        if (!open) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(capabilities);
    }

    @Override
    public PasswordCharacteristics getPasswordCharacteristics() {
        Map<String, Object> result = requestGet(path("password-characteristics", connectorHandle, "default"), null);
        Object characteristics = result.get("characteristics");
        if (characteristics instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) map;
            return toPasswordCharacteristics(data);
        }
        return null;
    }

    @Override
    public Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
        String streamHandle = streamHandle(path("password-characteristics", connectorHandle));
        List<Map<String, Object>> rows = streamAll(streamHandle);
        closeStream(streamHandle);
        List<PasswordCharacteristics> converted = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            converted.add(toPasswordCharacteristics(row));
        }
        return converted.iterator();
    }

    @Override
    public void logoff(Identity identity) throws PrincipalNotFoundException, ConnectorException {
        // No remote session handle is available through AbstractConnector final logon flow.
    }

    @Override
    public WebAuthenticationAPI startAuthentication() throws ConnectorException {
        return new RemoteWebAuthenticationAPI();
    }

    @Override
    protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("username", identity.getPrincipalName());
        payload.put("password", password == null ? "" : new String(password));
        try {
            requestPost(path("check-credentials", connectorHandle), payload);
            return true;
        } catch (InvalidLoginCredentialsException | PrincipalNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void changePassword(Identity identity, char[] oldPassword, char[] password) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("currentPassword", oldPassword == null ? "" : new String(oldPassword));
        payload.put("password", password == null ? "" : new String(password));
        requestPost(path("change-password", connectorHandle, identity.getPrincipalName()), payload);
    }

    @Override
    protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
            PasswordResetType type) throws ConnectorException {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("password", password == null ? "" : new String(password));
        payload.put("forceChangeAtNextLogon", String.valueOf(forcePasswordChangeAtLogon));
        payload.put("passwordResetType", type == null ? PasswordResetType.ADMINISTRATIVE.name() : type.name());
        requestPost(path("set-password", connectorHandle, identity.getPrincipalName()), payload);
    }

    @Override
    public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {
        String stream = streamHandle(path("identities", connectorHandle));
        List<Map<String, Object>> rows = streamAll(stream);
        closeStream(stream);

        List<Identity> identities = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            identities.add(toIdentity(row));
        }
        return ResultIterator.createDefault(identities.iterator(), opContext.getTag());
    }

    @Override
    public Count<Long> countIdentities(OperationContext opContext) throws ConnectorException {
        Map<String, Object> result = requestGet(path("identity-count", connectorHandle), null);
        long users = toLong(result.get("users"), 0L);
        return new Count<>(users, opContext.getTag());
    }

    @Override
    public Identity getIdentityByName(String identityName, boolean withGroups)
            throws PrincipalNotFoundException, ConnectorException {
        Map<String, String> query = withGroups ? Collections.singletonMap("withRoles", "true") : null;
        Map<String, Object> result = requestGet(path("identities", connectorHandle, identityName), query);
        Object identity = result.get("identity");
        if (identity instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) map;
            return toIdentity(payload);
        }
        throw new PrincipalNotFoundException(identityName + " not found");
    }

    @Override
    public Identity getIdentityByGuid(String identityGuid) throws PrincipalNotFoundException, ConnectorException {
        Map<String, Object> result = requestGet(path("identities-by-guid", connectorHandle, identityGuid), null);
        Object identity = result.get("identity");
        if (identity instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) map;
            return toIdentity(payload);
        }
        throw new PrincipalNotFoundException(identityGuid + " not found");
    }

    @Override
    public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
        String stream = streamHandle(path("roles", connectorHandle));
        List<Map<String, Object>> rows = streamAll(stream);
        closeStream(stream);

        List<Role> roles = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            roles.add(toRole(row));
        }
        return ResultIterator.createDefault(roles.iterator(), opContext.getTag());
    }

    @Override
    public Count<Long> countRoles(OperationContext opContext) throws ConnectorException {
        Map<String, Object> result = requestGet(path("role-count", connectorHandle), null);
        long users = toLong(result.get("users"), 0L);
        return new Count<>(users, opContext.getTag());
    }

    @Override
    public Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
        Map<String, Object> result = requestGet(path("roles", connectorHandle, roleName), null);
        Object role = result.get("role");
        if (role instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) map;
            return toRole(payload);
        }
        throw new PrincipalNotFoundException(roleName + " not found");
    }

    @Override
    protected void onOpen(RemoteConnectorConfiguration parameters) throws ConnectorException {
        this.api = new RemoteApiClient(parameters.getBaseUri());
        this.secretServiceClient = new RemoteSecretServiceClient(parameters.getBaseUri());

        String targetConnectorClass = parameters.getTargetConnectorClass();
        if (targetConnectorClass == null || targetConnectorClass.isBlank()) {
            throw new ConnectorException("Missing remote target connector class (remote.connectorClass)");
        }

        Map<String, String> payload = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : parameters.getConfigurationParameters().entrySet()) {
            if (RESERVED_CONFIG_KEYS.contains(entry.getKey())) {
                continue;
            }
            String[] values = entry.getValue();
            if (values != null && values.length > 0 && values[0] != null) {
                payload.put(entry.getKey(), values[0]);
            }
        }

        Map<String, Object> opened = requestPost(path("connectors", targetConnectorClass), payload);
        this.connectorHandle = asString(opened.get("handle"));
        if (connectorHandle == null || connectorHandle.isBlank()) {
            throw new ConnectorException("Remote API did not return a connector handle");
        }
        refreshCapabilities();
        open = true;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.putAll(principalAttributes(identity));
        if (identity.getFullName() != null) {
            payload.put("fullName", identity.getFullName());
        }
        if (identity.getOtherName() != null) {
            payload.put("otherName", identity.getOtherName());
        }
        if (password != null) {
            payload.put("password", new String(password));
        }

        Map<String, Object> result = requestPost(path("identities", connectorHandle, identity.getPrincipalName()), payload);
        Object created = result.get("identity");
        if (created instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) map;
            return toIdentity(response);
        }
        return getIdentityByName(identity.getPrincipalName(), false);
    }

    @Override
    public void updateIdentity(Identity identity) throws ConnectorException {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.putAll(principalAttributes(identity));
        if (identity.getFullName() != null) {
            payload.put("fullName", identity.getFullName());
        }
        if (identity.getOtherName() != null) {
            payload.put("otherName", identity.getOtherName());
        }
        requestPatch(path("identities", connectorHandle, identity.getPrincipalName()), payload);
    }

    @Override
    public void deleteIdentity(String principalName) throws ConnectorException {
        requestDelete(path("identities", connectorHandle, principalName));
    }

    @Override
    public Role createRole(Role role) throws ConnectorException {
        Map<String, String> payload = principalAttributes(role);
        Map<String, Object> result = requestPost(path("roles", connectorHandle, role.getPrincipalName()), payload);
        Object created = result.get("role");
        if (created instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) map;
            return toRole(response);
        }
        return getRoleByName(role.getPrincipalName());
    }

    @Override
    public void updateRole(Role role) throws ConnectorException {
        requestPatch(path("roles", connectorHandle, role.getPrincipalName()), principalAttributes(role));
    }

    @Override
    public void deleteRole(String principalName) throws ConnectorException {
        requestDelete(path("roles", connectorHandle, principalName));
    }

    @Override
    public void lockIdentity(Identity identity) {
        requestPost(path("locks", connectorHandle, identity.getPrincipalName()), Collections.emptyMap());
    }

    @Override
    public void unlockIdentity(Identity identity) {
        requestDelete(path("locks", connectorHandle, identity.getPrincipalName()));
    }

    @Override
    public void disableIdentity(Identity identity) {
        requestDelete(path("enablement", connectorHandle, identity.getPrincipalName()));
    }

    @Override
    public void enableIdentity(Identity identity) {
        requestPost(path("enablement", connectorHandle, identity.getPrincipalName()), Collections.emptyMap());
    }

    @Override
    protected void onClose() {
        if (connectorHandle != null) {
            try {
                requestDelete(path("connectors", connectorHandle));
            } catch (RuntimeException ignored) {
                // Connector may already be closed server-side.
            }
        }
        connectorHandle = null;
        open = false;
        capabilities = Collections.emptySet();
    }

    private String path(String... segments) {
        return api.path(segments);
    }

    private void refreshCapabilities() {
        Map<String, Object> response = requestGet(path("capabilities", connectorHandle), null);
        Object values = response.get("capabilities");
        Set<ConnectorCapability> loaded = new LinkedHashSet<>();
        if (values instanceof List<?> list) {
            for (Object value : list) {
                if (value == null) {
                    continue;
                }
                try {
                    loaded.add(ConnectorCapability.valueOf(String.valueOf(value)));
                } catch (IllegalArgumentException ignored) {
                    // Ignore unknown server capability names.
                }
            }
        }
        capabilities = loaded;
    }

    private String streamHandle(String path) {
        Map<String, Object> response = requestGet(path, null);
        String handle = asString(response.get("handle"));
        if (handle == null || handle.isBlank()) {
            throw new ConnectorException("Remote endpoint did not return a stream handle");
        }
        return handle;
    }

    private List<Map<String, Object>> streamAll(String streamHandle) {
        List<Map<String, Object>> all = new ArrayList<>();
        boolean eof = false;
        while (!eof) {
            Map<String, Object> page = requestGet(path("streams", streamHandle), Collections.singletonMap("items", "500"));
            Object values = page.get("results");
            if (values instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> row = (Map<String, Object>) map;
                        all.add(row);
                    }
                }
            }
            eof = Boolean.TRUE.equals(page.get("eof"));
        }
        return all;
    }

    private void closeStream(String streamHandle) {
        try {
            requestDelete(path("streams", streamHandle));
        } catch (RuntimeException ignored) {
            // Best effort cleanup.
        }
    }

    private Map<String, Object> requestGet(String path, Map<String, String> query) {
        return withConnectorMapping(() -> api.get(path, query));
    }

    private Map<String, Object> requestPost(String path, Map<String, String> payload) {
        return withConnectorMapping(() -> api.post(path, payload));
    }

    private Map<String, Object> requestPatch(String path, Map<String, String> payload) {
        return withConnectorMapping(() -> api.patch(path, payload));
    }

    private Map<String, Object> requestDelete(String path) {
        return withConnectorMapping(() -> api.delete(path));
    }

    private Map<String, Object> withConnectorMapping(ThrowingSupplier<Map<String, Object>> supplier) {
        try {
            return supplier.get();
        } catch (RemoteApiException rae) {
            throw mapRemoteException(rae);
        } catch (IOException ioe) {
            throw new ConnectorException(ioe.getMessage(), ioe);
        }
    }

    private ConnectorException mapRemoteException(RemoteApiException rae) {
        if (rae.getCode() == 404) {
            String msg = rae.getMessage() == null ? "Not found" : rae.getMessage();
            if (msg.toLowerCase().contains("credentials")) {
                return new InvalidLoginCredentialsException(msg);
            }
            return new PrincipalNotFoundException(msg);
        }
        if (rae.getCode() == 409) {
            return new PrincipalAlreadyExistsException(rae.getMessage());
        }
        return new ConnectorException(rae.getMessage(), rae);
    }

    private Identity toIdentity(Map<String, Object> map) {
        Map<String, Object> principal = mapObject(map.get("principal"));
        String name = asString(principal.get("name"));
        String guid = asString(principal.get("guid"));
        IdentityImpl identity = new IdentityImpl(guid, name);
        identity.setSystem(toBoolean(principal.get("system")));
        identity.setFullName(asString(map.get("fullName")));
        identity.setOtherName(asString(map.get("otherName")));

        Object lastLogon = map.get("lastLogon");
        long lastLogonEpoch = toLong(lastLogon, -1L);
        if (lastLogonEpoch >= 0) {
            identity.setLastSignOnDate(new java.util.Date(lastLogonEpoch));
        }

        applyPrincipalAttributes(identity, principal);
        applyAccountStatus(identity, mapObject(map.get("accountStatus")));
        applyPasswordStatus(identity, mapObject(map.get("passwordStatus")));

        Object roles = map.get("roles");
        if (roles instanceof List<?> roleList) {
            List<Role> converted = new ArrayList<>();
            for (Object roleObj : roleList) {
                Map<String, Object> roleMap = mapObject(roleObj);
                if (roleMap.containsKey("principal")) {
                    converted.add(toRole(roleMap));
                } else {
                    converted.add(toRole(Collections.singletonMap("principal", roleMap)));
                }
            }
            identity.setRoles(converted.toArray(new Role[0]));
        }

        return identity;
    }

    private Role toRole(Map<String, Object> map) {
        Map<String, Object> principal = mapObject(map.get("principal"));
        RoleImpl role = new RoleImpl(asString(principal.get("guid")), asString(principal.get("name")));
        role.setSystem(toBoolean(principal.get("system")));
        applyPrincipalAttributes(role, principal);

        Object roles = map.get("roles");
        if (roles instanceof List<?> roleList) {
            List<Role> children = new ArrayList<>();
            for (Object roleObj : roleList) {
                Map<String, Object> roleMap = mapObject(roleObj);
                if (roleMap.containsKey("principal")) {
                    children.add(toRole(roleMap));
                } else {
                    children.add(toRole(Collections.singletonMap("principal", roleMap)));
                }
            }
            role.setRoles(children.toArray(new Role[0]));
        }
        return role;
    }

    private void applyPrincipalAttributes(Principal principal, Map<String, Object> principalMap) {
        Map<String, Object> attrs = mapObject(principalMap.get("attributes"));
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            if (e.getValue() instanceof List<?> values) {
                List<String> converted = new ArrayList<>();
                for (Object value : values) {
                    converted.add(String.valueOf(value));
                }
                principal.setAttribute(e.getKey(), converted.toArray(new String[0]));
            } else if (e.getValue() != null && e.getValue().getClass().isArray()) {
                principal.setAttribute(e.getKey(), Arrays.stream((Object[]) e.getValue()).map(String::valueOf).toArray(String[]::new));
            } else if (e.getValue() != null) {
                principal.setAttribute(e.getKey(), String.valueOf(e.getValue()));
            }
        }
    }

    private void applyPasswordStatus(IdentityImpl identity, Map<String, Object> values) {
        PasswordStatus status = new PasswordStatus();
        status.setNeedChange(toBoolean(values.get("needChange")));
        status.setExpire(date(values.get("expire")));
        status.setLastChange(date(values.get("lastChange")));
        status.setUnlocked(date(values.get("unlocked")));
        status.setWarn(date(values.get("warn")));
        status.setDisable(date(values.get("disable")));
        String type = asString(values.get("type"));
        if (type != null) {
            try {
                status.setType(PasswordStatusType.valueOf(type));
            } catch (IllegalArgumentException ignored) {
                // Keep default
            }
        }
        identity.setPasswordStatus(status);
    }

    private void applyAccountStatus(IdentityImpl identity, Map<String, Object> values) {
        AccountStatus status = new AccountStatus();
        status.setDisabled(toBoolean(values.get("disabled")));
        status.setExpire(date(values.get("expire")));
        status.setLocked(date(values.get("locked")));
        status.setUnlocked(date(values.get("unlocked")));
        String type = asString(values.get("type"));
        if (type != null) {
            try {
                status.setType(AccountStatusType.valueOf(type));
            } catch (IllegalArgumentException ignored) {
                // Keep default
            }
        }
        identity.setAccountStatus(status);
    }

    private PasswordCharacteristics toPasswordCharacteristics(Map<String, Object> data) {
        DefaultPasswordCharacteristics pc = new DefaultPasswordCharacteristics();
        pc.setVeryStrongFactor((float) toDouble(data.get("veryStrongFactor"), pc.getVeryStrongFactor()));
        pc.setMinimumSize(toInt(data.get("minimumSize"), pc.getMinimumSize()));
        pc.setMaximumSize(toInt(data.get("maximumSize"), pc.getMaximumSize()));
        pc.setRequiresMatches(toInt(data.get("requiredMatches"), pc.getRequiredMatches()));
        pc.setMinimumLowerCase(toInt(data.get("minimumLowerCase"), pc.getMinimumLowerCase()));
        pc.setMinimumUpperCase(toInt(data.get("minimumUpperCase"), pc.getMinimumUpperCase()));
        pc.setMinimumDigits(toInt(data.get("minimumDigits"), pc.getMinimumDigits()));
        pc.setMinimumSymbols(toInt(data.get("minimumSymbols"), pc.getMinimumSymbols()));
        pc.setHistorySize(toInt(data.get("historySize"), pc.getHistorySize()));
        pc.setDictionaryWordsAllowed(toBoolean(data.get("dictionaryWordsAllowed")));
        pc.setAdditionalAnalysis(toBoolean(data.get("additionalAnalysis")));
        pc.setMinStrength((float) toDouble(data.get("minStrength"), pc.getMinStrength()));
        pc.setContainUsername(toBoolean(data.get("containUsername")));

        String symbols = asString(data.get("symbols"));
        if (symbols != null) {
            pc.setSymbols(symbols.toCharArray());
        }

        Map<String, Object> attributes = mapObject(data.get("attributes"));
        Map<String, String> casted = new HashMap<>();
        for (Map.Entry<String, Object> e : attributes.entrySet()) {
            casted.put(e.getKey(), String.valueOf(e.getValue()));
        }
        pc.getAttributes().putAll(casted);
        return pc;
    }

    private Map<String, String> principalAttributes(Principal principal) {
        Map<String, String> values = new LinkedHashMap<>();
        if (principal.getGuid() != null) {
            values.put("principal.guid", principal.getGuid());
        }
        values.put("principal.system", String.valueOf(principal.isSystem()));
        for (Map.Entry<String, String[]> e : principal.getAttributes().entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            if (e.getValue().length == 1) {
                values.put("principal.attributes." + e.getKey(), e.getValue()[0]);
            } else {
                for (int i = 0; i < e.getValue().length; i++) {
                    values.put("principal.attributes." + e.getKey() + "[" + i + "]", e.getValue()[i]);
                }
            }
        }
        return values;
    }

    private static Map<String, Object> mapObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> converted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                converted.put(String.valueOf(e.getKey()), e.getValue());
            }
            return converted;
        }
        return Collections.emptyMap();
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int toInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // default
            }
        }
        return defaultValue;
    }

    private static long toLong(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // default
            }
        }
        return defaultValue;
    }

    private static double toDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // default
            }
        }
        return defaultValue;
    }

    private static boolean toBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b.booleanValue();
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return false;
    }

    private static java.util.Date date(Object value) {
        long epoch = toLong(value, -1L);
        return epoch < 0 ? null : new java.util.Date(epoch);
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws IOException;
    }

    private final class RemoteWebAuthenticationAPI implements WebAuthenticationAPI {

        private String id;
        private String username;
        private String state;
        private long created;
        private Status status = Status.STARTED;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String open(String returnTo) {
            Map<String, String> query = new LinkedHashMap<>();
            query.put("redirectURI", returnTo);
            Map<String, Object> response = requestGet(path("web-logon", connectorHandle), query);

            String location = asString(response.get("location"));
            String discovered = extractWebLogonHandle(location);
            if (discovered != null) {
                this.id = discovered;
            }
            this.status = Status.OPENED;
            return location;
        }

        @Override
        public String getState() {
            return state;
        }

        @Override
        public long getCreated() {
            return created;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException {
            String resolvedId = webLogonHandle(returnParameters);
            if (resolvedId == null) {
                throw new IOException("No webLogonHandle was supplied.");
            }
            this.id = resolvedId;

            Map<String, String> query = new LinkedHashMap<>();
            if (returnParameters != null) {
                for (Map.Entry<String, String[]> e : returnParameters.entrySet()) {
                    if (e.getValue() == null || e.getValue().length == 0) {
                        continue;
                    }
                    query.put(e.getKey(), e.getValue()[0]);
                }
            }

            Map<String, Object> response = requestGet(path("complete-web-logon", resolvedId), query);
            Map<String, Object> webLogon = mapObject(response.get("webLogon"));
            username = asString(webLogon.get("username"));
            state = asString(webLogon.get("state"));
            created = toLong(webLogon.get("created"), 0L);

            String statusValue = asString(webLogon.get("status"));
            if (statusValue != null) {
                try {
                    status = Status.valueOf(statusValue);
                } catch (IllegalArgumentException ignored) {
                    status = Status.COMPLETE;
                }
            }

            String returnStatus = asString(webLogon.get("returnStatus"));
            if (returnStatus != null) {
                try {
                    return ReturnStatus.valueOf(returnStatus);
                } catch (IllegalArgumentException ignored) {
                    return ReturnStatus.UNKNOWN;
                }
            }
            return ReturnStatus.UNKNOWN;
        }

        private String extractWebLogonHandle(String location) {
            if (location == null) {
                return null;
            }
            String marker = "webLogonHandle=";
            int idx = location.indexOf(marker);
            if (idx < 0) {
                return null;
            }
            String value = location.substring(idx + marker.length());
            int amp = value.indexOf('&');
            if (amp > -1) {
                value = value.substring(0, amp);
            }
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        }

        private String webLogonHandle(Map<String, String[]> returnParameters) {
            if (returnParameters != null && returnParameters.containsKey("webLogonHandle")) {
                String[] values = returnParameters.get("webLogonHandle");
                if (values != null && values.length > 0) {
                    return values[0];
                }
            }
            return id;
        }
    }
}
