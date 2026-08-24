/* HEADER */
package com.identity4j.connector;

/*
 * #%L
 * Identity4J Connector
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-477 – deprecated setPassword(Identity,char[],boolean)
 * overload removed; default 4-arg method must throw UnsupportedOperationException.
 * #L%
 */

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.identity4j.connector.Connector.PasswordResetType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.Role;

/**
 * Verifies that the default AbstractConnector.setPassword implementation throws
 * UnsupportedOperationException after the deprecated 3-arg overload was removed
 * (CWE-477 remediation).
 */
public class Cwe477SetPasswordDefaultTest {

    /** Minimal connector that does not override setPassword. */
    private static class NoopConnector extends AbstractConnector<ConnectorConfigurationParameters> {

        private boolean open;

        @Override
        protected void onOpen(ConnectorConfigurationParameters parameters) throws ConnectorException {
            open = true;
        }

        @Override
        public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void reopen() throws ConnectorException {
            // noop
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Set<ConnectorCapability> getCapabilities() {
            return Collections.emptySet();
        }
    }

    @Test
    public void defaultSetPasswordThrowsUnsupportedOperation() throws ConnectorException {
        NoopConnector connector = new NoopConnector();
        Identity identity = new IdentityImpl("test-guid", "testuser");

        try {
            connector.setPassword(identity, "newpass".toCharArray(), false, PasswordResetType.ADMINISTRATIVE);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected: default implementation signals no set-password support
        }
    }

    @Test
    public void defaultSetPasswordWithUserResetTypeThrowsUnsupportedOperation() throws ConnectorException {
        NoopConnector connector = new NoopConnector();
        Identity identity = new IdentityImpl("test-guid", "testuser");

        try {
            connector.setPassword(identity, "newpass".toCharArray(), true, PasswordResetType.USER);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
