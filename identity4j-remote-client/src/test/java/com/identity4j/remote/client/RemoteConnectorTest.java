package com.identity4j.remote.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
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
import com.identity4j.remote.Identity4JRemoteFilter;
import com.identity4j.util.MultiMap;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

import jakarta.servlet.DispatcherType;

public class RemoteConnectorTest {

    private Server server;
    private int port;
    private File secretFile;

    @Before
    public void startServer() throws Exception {
        secretFile = File.createTempFile("identity4j-remote-client", ".properties");
        secretFile.deleteOnExit();

        server = new Server(port = findPort());
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/i4j");
        context.setClassLoader(getClass().getClassLoader());
        var filter = context.addFilter(Identity4JRemoteFilter.class, "/*", java.util.EnumSet.of(DispatcherType.REQUEST));
        filter.setInitParameter(Identity4JRemoteFilter.CONFIG_SECRET_PROPERTIES_FILE, secretFile.getAbsolutePath());
        server.setHandler(context);
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testRemoteConnectorAndSecretsClient() {
        String baseUri = "http://localhost:" + port + "/i4j";

        SecretServiceClient secrets = new RemoteSecretServiceClient(baseUri);
        Map<String, String> secret = new LinkedHashMap<>();
        secret.put("username", "svc-user");
        secret.put("password", "svc-pass");
        secrets.create("svc", secret);
        assertTrue(secrets.aliases().contains("svc"));
        assertTrue(secrets.exists("svc"));

        RemoteConnector connector = new RemoteConnector();
        MultiMap conf = new MultiMap();
        conf.set(RemoteConnectorConfiguration.KEY_URI, baseUri);
        conf.set(RemoteConnectorConfiguration.REMOTE_CONNECTOR_CLASS, MockConnector.class.getName());
        conf.set("bind.user", "%credential.svc.username%");
        conf.set("bind.pass", "%credential.svc.password%");
        connector.open(new RemoteConnectorConfiguration(conf));

        assertTrue(connector.isOpen());
        assertTrue(connector.getCapabilities().contains(ConnectorCapability.identities));

        Identity identity = connector.getIdentityByName("alice", true);
        assertNotNull(identity);
        assertEquals("alice", identity.getPrincipalName());

        long count = connector.countIdentities(OperationContext.createDefault()).amount();
        assertEquals(1L, count);

        ResultIterator<Identity> all = connector.allIdentities(OperationContext.createDefault());
        assertTrue(all.hasNext());

        connector.deleteIdentity("alice");
        assertFalse(connector.isIdentityNameInUse("alice"));

        connector.close();
        assertFalse(connector.isOpen());

        secrets.delete("svc");
        assertFalse(secrets.aliases().contains("svc"));
    }

    private int findPort() {
        int start = (int) (Math.random() * 40000) + 20000;
        for (int i = start; i < start + 1000; i++) {
            try (ServerSocket ss = new ServerSocket(i)) {
                ss.setReuseAddress(true);
                return i;
            } catch (IOException ignored) {
                // continue
            }
        }
        throw new IllegalStateException("Could not find a free port.");
    }

    public static class MockConnector extends AbstractConnector<DefaultConnectorConfiguration> {

        private final Map<String, IdentityImpl> identities = new LinkedHashMap<>();
        private final Map<String, RoleImpl> roles = new LinkedHashMap<>();
        private boolean open;

        @Override
        public Set<ConnectorCapability> getCapabilities() {
            return Set.of(
                    ConnectorCapability.identities,
                    ConnectorCapability.roles,
                    ConnectorCapability.authentication,
                    ConnectorCapability.createUser,
                    ConnectorCapability.updateUser,
                    ConnectorCapability.deleteUser,
                    ConnectorCapability.createRole,
                    ConnectorCapability.updateRole,
                    ConnectorCapability.deleteRole,
                    ConnectorCapability.passwordChange,
                    ConnectorCapability.passwordSet,
                    ConnectorCapability.accountLocking,
                    ConnectorCapability.accountDisable,
                    ConnectorCapability.webAuthentication);
        }

        @Override
        public PasswordCharacteristics getPasswordCharacteristics() {
            return new DefaultPasswordCharacteristics();
        }

        @Override
        public java.util.Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
            List<PasswordCharacteristics> p = new ArrayList<>();
            p.add(new DefaultPasswordCharacteristics());
            return p.iterator();
        }

        @Override
        public ResultIterator<Identity> allIdentities(OperationContext opContext) {
            List<Identity> copy = new ArrayList<>(identities.values());
            return ResultIterator.createDefault(copy.iterator(), null);
        }

        @Override
        public ResultIterator<Role> allRoles(OperationContext opContext) {
            List<Role> copy = new ArrayList<>(roles.values());
            return ResultIterator.createDefault(copy.iterator(), null);
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
        public Identity createIdentity(Identity identity, char[] password) {
            if (identities.containsKey(identity.getPrincipalName())) {
                throw new com.identity4j.connector.exception.PrincipalAlreadyExistsException(identity.getPrincipalName() + " already exists");
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
            // no-op
        }

        @Override
        protected boolean areCredentialsValid(Identity identity, char[] password) {
            return true;
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
        protected void onOpen(DefaultConnectorConfiguration parameters) {
            open = true;
            IdentityImpl alice = new IdentityImpl("guid-alice", "alice");
            alice.setFullName("Alice Example");
            identities.put(alice.getPrincipalName(), alice);
            roles.put("staff", new RoleImpl("role-staff", "staff"));
        }

        @Override
        protected void onClose() {
            open = false;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }
    }
}
