package com.identity4j.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.DefaultConnectorConfiguration;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.WebAuthenticationAPI;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpData;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

import jakarta.servlet.DispatcherType;

public class Identity4JRemoteFilterTest {

    private Server server;
    private int port;
    private File secretFile;

    @Before
    public void startServer() throws Exception {
        secretFile = File.createTempFile("identity4j-remote", ".properties");
        secretFile.deleteOnExit();

        server = new Server(port = findPort());
        ServletContextHandler webAppContext = new ServletContextHandler();
        webAppContext.setContextPath("/test");
        webAppContext.setClassLoader(getClass().getClassLoader());

        var holder = webAppContext.addFilter(Identity4JRemoteFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        holder.setInitParameter(Identity4JRemoteFilter.CONFIG_SECRET_PROPERTIES_FILE, secretFile.getAbsolutePath());
        holder.setInitParameter(Identity4JRemoteFilter.CONFIG_HANDLE_TIMEOUT_SECONDS, "3600");

        server.setHandler(webAppContext);
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testSecretConnectorAndDeleteLifecycle() throws IOException {
        HttpProviderClient secretClient = Http
                .getProvider().getClient("http://localhost:" + port + "/test/secrets/ad-service-account");
        HttpResponse secretResp = secretClient.post(null,
                List.of(new HttpPair("username", "Administrator"), new HttpPair("password", "s3cret")),
                new HttpPair("Accept", "application/json"),
                new HttpPair("Content-Type", "application/x-www-form-urlencoded"));
        assertEquals(200, secretResp.status().getCode());
        assertTrue(secretResp.contentString().contains("\"code\":201"));

        HttpProviderClient connectorClient = Http.getProvider().getClient("http://localhost:" + port
                + "/test/connectors/" + MockConnector.class.getName());
        HttpResponse connectorResp = connectorClient.post(null,
                List.of(new HttpPair("bind.user", "%credential.ad-service-account.username%")),
                new HttpPair("Accept", "application/json"),
                new HttpPair("Content-Type", "application/x-www-form-urlencoded"));
        assertEquals(200, connectorResp.status().getCode());
        String handle = jsonField(connectorResp.contentString(), "handle");

        HttpProviderClient identityClient = Http
                .getProvider().getClient("http://localhost:" + port + "/test/identities/" + handle + "/alice");
        HttpResponse getIdentity = identityClient.get(null, new HttpPair("Accept", "application/json"));
        assertEquals(200, getIdentity.status().getCode());
        assertTrue(getIdentity.contentString().contains("\"success\":true"));

        HttpProviderClient deleteConnector = Http
                .getProvider().getClient("http://localhost:" + port + "/test/connectors/" + handle);
        HttpResponse deleted = deleteConnector.delete(null, new HttpPair("Accept", "application/json"));
        assertEquals(200, deleted.status().getCode());
        assertTrue(deleted.contentString().contains("\"code\":200"));
    }

    @Test
    public void testStreamEndpointAndEof() throws IOException {
        HttpProviderClient connectorClient = Http.getProvider()
                .getClient("http://localhost:" + port + "/test/connectors/" + MockConnector.class.getName());
        HttpResponse connectorResp = connectorClient.post(null, List.of(), new HttpPair("Accept", "application/json"),
                new HttpPair("Content-Type", "application/x-www-form-urlencoded"));
        String handle = jsonField(connectorResp.contentString(), "handle");

        HttpProviderClient listClient = Http
                .getProvider().getClient("http://localhost:" + port + "/test/identities/" + handle);
        HttpResponse listResp = listClient.get(null, new HttpPair("Accept", "application/json"));
        String streamHandle = jsonField(listResp.contentString(), "handle");

        HttpProviderClient streamClient = Http
                .getProvider().getClient("http://localhost:" + port + "/test/streams/" + streamHandle + "?items=10");
        HttpResponse streamResp = streamClient.get(null, new HttpPair("Accept", "application/json"));
        assertEquals(200, streamResp.status().getCode());
        assertTrue(streamResp.contentString().contains("\"eof\":true"));
    }

    private String jsonField(String json, String key) {
        String needle = "\"" + key + "\":\"";
        int idx = json.indexOf(needle);
        int start = idx + needle.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    int findPort() {
        int start = (int) (Math.random() * 40000) + 20000;
        for (int i = start; i < start + 1000; i++) {
            try {
                ServerSocket ss = new ServerSocket(i);
                try {
                    ss.setReuseAddress(true);
                    return i;
                } finally {
                    ss.close();
                }
            } catch (IOException ioe) {
                // Continue
            }
        }
        throw new IllegalStateException("Could not find a port to listen on for HTTP.");
    }

    public static class MockConnector extends AbstractConnector<DefaultConnectorConfiguration> {

        private final Map<String, IdentityImpl> identities = new LinkedHashMap<>();
        private final Map<String, RoleImpl> roles = new LinkedHashMap<>();
        private boolean open;

        @Override
        protected void onOpen(DefaultConnectorConfiguration parameters) {
            open = true;
            IdentityImpl alice = new IdentityImpl("guid-alice", "alice");
            alice.setFullName("Alice Example");
            identities.put(alice.getPrincipalName(), alice);

            RoleImpl staff = new RoleImpl("role-staff", "staff");
            roles.put(staff.getPrincipalName(), staff);
            alice.setRoles(new Role[] { staff });
        }

        @Override
        public Set<ConnectorCapability> getCapabilities() {
            Set<ConnectorCapability> caps = new HashSet<>();
            caps.add(ConnectorCapability.identities);
            caps.add(ConnectorCapability.roles);
            caps.add(ConnectorCapability.authentication);
            caps.add(ConnectorCapability.passwordChange);
            caps.add(ConnectorCapability.passwordSet);
            caps.add(ConnectorCapability.createUser);
            caps.add(ConnectorCapability.updateUser);
            caps.add(ConnectorCapability.deleteUser);
            caps.add(ConnectorCapability.createRole);
            caps.add(ConnectorCapability.updateRole);
            caps.add(ConnectorCapability.deleteRole);
            caps.add(ConnectorCapability.accountLocking);
            caps.add(ConnectorCapability.accountDisable);
            caps.add(ConnectorCapability.webAuthentication);
            return caps;
        }

        @Override
        public PasswordCharacteristics getPasswordCharacteristics() {
            return new DefaultPasswordCharacteristics();
        }

        @Override
        public Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
            List<PasswordCharacteristics> list = new ArrayList<>();
            list.add(new DefaultPasswordCharacteristics());
            return list.iterator();
        }

        @Override
        public ResultIterator<Identity> allIdentities(OperationContext opContext) {
            List<Identity> list = new ArrayList<>(identities.values());
            return ResultIterator.createDefault(list.iterator(), null);
        }

        @Override
        public ResultIterator<Role> allRoles(OperationContext opContext) {
            List<Role> list = new ArrayList<>(roles.values());
            return ResultIterator.createDefault(list.iterator(), null);
        }

        @Override
        public Identity getIdentityByName(String identityName, boolean withGroups) {
            Identity identity = identities.get(identityName);
            if (identity == null) {
                throw new com.identity4j.connector.exception.PrincipalNotFoundException(identityName + " not found");
            }
            return identity;
        }

        @Override
        public Identity getIdentityByGuid(String identityGuid) {
            for (IdentityImpl identity : identities.values()) {
                if (identityGuid.equals(identity.getGuid())) {
                    return identity;
                }
            }
            throw new com.identity4j.connector.exception.PrincipalNotFoundException(identityGuid + " not found");
        }

        @Override
        public Role getRoleByName(String roleName) {
            Role role = roles.get(roleName);
            if (role == null) {
                throw new com.identity4j.connector.exception.PrincipalNotFoundException(roleName + " not found");
            }
            return role;
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
        protected boolean areCredentialsValid(Identity identity, char[] password) {
            return "password".equals(new String(password));
        }

        @Override
        public Identity createIdentity(Identity identity, char[] password) {
            if (identities.containsKey(identity.getPrincipalName())) {
                throw new com.identity4j.connector.exception.PrincipalAlreadyExistsException("already exists");
            }
            IdentityImpl created = new IdentityImpl(identity.getGuid(), identity.getPrincipalName());
            created.setFullName(identity.getFullName());
            created.setAttributes(identity.getAttributes());
            identities.put(created.getPrincipalName(), created);
            return created;
        }

        @Override
        public void updateIdentity(Identity identity) {
            identities.put(identity.getPrincipalName(), (IdentityImpl) identity);
        }

        @Override
        public void deleteIdentity(String principalName) {
            if (identities.remove(principalName) == null) {
                throw new com.identity4j.connector.exception.PrincipalNotFoundException(principalName + " not found");
            }
        }

        @Override
        public Role createRole(Role role) {
            RoleImpl created = new RoleImpl(role.getGuid(), role.getPrincipalName());
            created.setAttributes(role.getAttributes());
            roles.put(created.getPrincipalName(), created);
            return created;
        }

        @Override
        public void updateRole(Role role) {
            roles.put(role.getPrincipalName(), (RoleImpl) role);
        }

        @Override
        public void deleteRole(String principalName) {
            if (roles.remove(principalName) == null) {
                throw new com.identity4j.connector.exception.PrincipalNotFoundException(principalName + " not found");
            }
        }

        @Override
        protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
                com.identity4j.connector.Connector.PasswordResetType type) {
            if (password == null || password.length == 0) {
                throw new com.identity4j.connector.exception.InvalidLoginCredentialsException("Password required");
            }
        }

        @Override
        public void lockIdentity(Identity identity) {
            identity.getAccountStatus().lock();
        }

        @Override
        public void unlockIdentity(Identity identity) {
            identity.getAccountStatus().unlock();
        }

        @Override
        public void disableIdentity(Identity identity) {
            identity.getAccountStatus().setDisabled(true);
        }

        @Override
        public void enableIdentity(Identity identity) {
            identity.getAccountStatus().setDisabled(false);
        }

        @Override
        public WebAuthenticationAPI startAuthentication() {
            return new WebAuthenticationAPI() {
                @Override
                public String getUsername() {
                    return "alice";
                }

                @Override
                public String getId() {
                    return "weblogon-1";
                }

                @Override
                public String open(String returnTo) {
                    return "https://example.test/login?returnTo=" + returnTo;
                }

                @Override
                public String getState() {
                    return "state-1";
                }

                @Override
                public long getCreated() {
                    return System.currentTimeMillis();
                }

                @Override
                public Status getStatus() {
                    return Status.COMPLETE;
                }

                @Override
                public ReturnStatus validate(Map<String, String[]> returnParameters) {
                    return ReturnStatus.AUTHENTICATED;
                }
            };
        }

        @Override
        protected void onClose() {
            open = false;
        }
    }
}
