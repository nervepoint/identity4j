/* HEADER */
package com.identity4j.connector.script;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class ScriptConnector extends AbstractConnector {

	private ScriptConfiguration scriptConfiguration;
	private final ScriptEngineManager manager;

	private boolean open;
	private ScriptEngine engine;

	private Float floatVersion;

	private boolean scriptRoleSupport;
	private boolean scriptIdentitySupport;

	public ScriptConnector() {
		manager = new ScriptEngineManager();
		manager.put("connector", this);
		manager.put("log", LogFactory.getLog("Script"));

		// Get a version string. We only really care about one decimal place in
		// the version string
		String versionString = SystemUtils.JAVA_VERSION;
		int idx = versionString.indexOf('.');
		if (idx != -1) {
			idx = versionString.indexOf('.', idx + 1);
			if (idx != -1)
				versionString = versionString.substring(0, idx);
		}
		floatVersion = Float.valueOf(versionString);

		manager.put("JAVA_RUNTIME_VERSION", floatVersion);
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	public ScriptConfiguration getConfiguration() {
		return scriptConfiguration;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ConnectorCapability> getCapabilities() {
		try {
			return (Set<ConnectorCapability>) ((Invocable) engine).invokeFunction("getCapabilities");
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new RuntimeException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return new LinkedHashSet<ConnectorCapability>(Arrays.asList(ConnectorCapability.values()));
		}
	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		scriptRoleSupport = true;
		scriptConfiguration = (ScriptConfiguration) parameters;
		engine = manager.getEngineByMimeType(scriptConfiguration.getScriptMimeType());
		engine.put("config", scriptConfiguration);
		engine.put("JAVA_RUNTIME_VERSION", floatVersion);
		try {
			String scriptContent = getScriptContent();
			if (floatVersion >= 1.8f) {
				scriptContent = "load('nashorn:mozilla_compat.js');\n" + scriptContent;
			}
			engine.eval(scriptContent);
		} catch (Exception e) {
			if (e instanceof ScriptException) {
				throw new ConnectorException(e.getLocalizedMessage(), e);
			} else {
				throw new ConnectorException("Error executing script.", e);
			}
		}
		open = true;
		onOpened(parameters);
	}

	protected void onOpened(ConnectorConfigurationParameters parameters) {
		try {
			((Invocable) engine).invokeFunction("onOpen", parameters);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
		}
	}

	protected String getScriptContent() throws IOException {
		final String scriptContent = scriptConfiguration.getScriptContent();
		return scriptContent;
	}

	@Override
	public Identity getIdentityByName(String name) throws PrincipalNotFoundException, ConnectorException {
		try {
			Identity identity = (Identity) ((Invocable) engine).invokeFunction("getIdentityByName", name);
			if (identity == null) {
				throw new PrincipalNotFoundException("Could not find user " + name + ".");
			}
			return identity;
		} catch (ScriptException e) {
			try {
				processScriptExecption(e);
				throw new ConnectorException("Failed script execution.", e);
			} catch (UnsupportedOperationException ueo) {
				scriptIdentitySupport = false;
				return super.getIdentityByName(name);
			}
		} catch (NoSuchMethodException e) {
			scriptIdentitySupport = false;
			return super.getIdentityByName(name);
		}
	}

	@Override
	public Role getRoleByName(String name) throws PrincipalNotFoundException, ConnectorException {
		try {
			Role role = (Role) ((Invocable) engine).invokeFunction("getRoleByName", name);
			if (role == null) {
				throw new PrincipalNotFoundException("Could not find group " + name + ".");
			}
			return role;
		} catch (ScriptException e) {
			try {
				processScriptExecption(e);
			} catch (UnsupportedOperationException uoe) {
				scriptRoleSupport = false;
				return super.getRoleByName(name);
			}
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			scriptRoleSupport = false;
			return super.getRoleByName(name);
		}
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		try {
			final Boolean val = (Boolean) ((Invocable) engine).invokeFunction("setPassword", identity,
					new String(password), forcePasswordChangeAtLogon);
			if (val != null && !val.booleanValue()) {
				throw new UnsupportedOperationException("Set password is not supported");
			}
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.setPassword(identity, password, forcePasswordChangeAtLogon, type);
		}
	}

	/**
	 * Checks that the supplied credentials are valid for authentication
	 * 
	 * @param identity
	 * @param password
	 * @return <tt>true</tt> if the credentials are valid
	 * @throws ConnectorException
	 */
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		
		try {
			final Object obj = ((Invocable) engine).invokeFunction("areCredentialsValid", identity,
					new String(password));
			if (obj instanceof PasswordChangeRequiredException) {
				throw (PasswordChangeRequiredException) obj;
			}
			Boolean ok = (Boolean) obj;
			if (ok && identity.getPasswordStatus().getType().equals(PasswordStatusType.changeRequired)) {
				throw new PasswordChangeRequiredException();
			}
			return ok;
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			try {
				return defaultAreCredentialsValid(identity, password);
			}
			catch(UnsupportedOperationException uoe) {			
				return super.areCredentialsValid(identity, password);
			} catch (IOException e1) {
				throw new ConnectorException(e1);
			}
		}
	}

	protected boolean defaultAreCredentialsValid(Identity identity, char[] password) throws IOException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public Iterator<Identity> allIdentities() throws ConnectorException {
		try {
			return (Iterator<Identity>) ((Invocable) engine).invokeFunction("allIdentities");
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	@SuppressWarnings("unchecked")
	public Iterator<Role> allRoles() throws ConnectorException {
		try {
			return (Iterator<Role>) ((Invocable) engine).invokeFunction("allRoles");
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	public void disableIdentity(Identity identity) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("disableIdentity", identity);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	public void enableIdentity(Identity identity) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("enableIdentity", identity);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	public void lockIdentity(Identity identity) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("lockIdentity", identity);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	public void unlockIdentity(Identity identity) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("unlockIdentity", identity);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			throw new ConnectorException("Missing function in script " + getConfiguration().getScriptResource() + ".",
					e);
		}
	}

	@Override
	public long countIdentities() throws ConnectorException {
		try {
			return ((Number) ((Invocable) engine).invokeFunction("countIdentities")).longValue();
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return super.countIdentities();
		}
	}

	@Override
	public void deleteIdentity(String principleName) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("deleteIdentity", principleName);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.deleteIdentity(principleName);
		}
	}

	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		try {
			return ((Identity) ((Invocable) engine).invokeFunction("createIdentity", identity,
					password == null ? null : new String(password)));
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return super.createIdentity(identity, password);
		}
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("updateIdentity", identity);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.updateIdentity(identity);
		}
	}

	@Override
	public long countRoles() throws ConnectorException {
		try {
			return ((Number) ((Invocable) engine).invokeFunction("countRoles")).longValue();
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return super.countRoles();
		}
	}

	@Override
	protected void changePassword(Identity identity, char[] oldPassword, char[] password) {
		try {
			final Boolean val = (Boolean) ((Invocable) engine).invokeFunction("changePassword", identity,
					new String(oldPassword), new String(password));
			if (!val.booleanValue()) {
				throw new UnsupportedOperationException("Change password is not supported");
			}
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.changePassword(identity, oldPassword, password);
		}
	}

	@Override
	public Role createRole(Role role) throws ConnectorException {
		try {
			return ((Role) ((Invocable) engine).invokeFunction("createRole", role));
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return super.createRole(role);
		}
	}

	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("deleteRole", principleName);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.deleteRole(principleName);
		}
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		try {
			((Invocable) engine).invokeFunction("updateRole", role);
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			super.updateRole(role);
		}
	}

	protected ScriptEngine getEngine() {
		return engine;
	}

	@Override
	protected void onClose() {
		super.onClose();
		open = false;
	}

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		try {
			PasswordCharacteristics pc = (PasswordCharacteristics) ((Invocable) engine)
					.invokeFunction("getPasswordCharacteristics");
			return pc;
		} catch (ScriptException e) {
			processScriptExecption(e);
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			return super.getPasswordCharacteristics();
		}
	}

	void processScriptExecption(ScriptException se) {
		if (se.getMessage() != null && se.getMessage().startsWith("UnsupportedOperationException")) {
			throw new UnsupportedOperationException();
		}
		/*
		 * JDK6 its impossible to get the underyling exception which is
		 * absolutely terrible JDK7 the situation seems SLIGHTLY improved in
		 * that at least there is cause to get at.
		 * http://stackoverflow.com/questions
		 * /7889369/how-to-access-java-exception
		 * -that-causes-scriptexception-using-jsr-223
		 */
		if (se.getCause() != null
				&& se.getCause().getClass().getName().equals("sun.org.mozilla.javascript.JavaScriptException")) {
			try {
				Class<?> c = se.getCause().getClass();
				Object val = c.getMethod("getValue").invoke(se.getCause());
				if (val.getClass().getName().equals("sun.org.mozilla.javascript.NativeJavaObject")) {
					c = val.getClass();
					val = c.getMethod("unwrap").invoke(val);
				}
				if (val instanceof ConnectorException) {
					throw (ConnectorException) val;
				}
			} catch (ConnectorException ce) {
				throw ce;
			} catch (Exception e) {
			}
		}
	}
}