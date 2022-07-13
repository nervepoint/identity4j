package com.identity4j.connector.unix;

/*
 * #%L
 * Identity4J Unix
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;

import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.flatfile.AbstractFlatFile;
import com.identity4j.connector.flatfile.AbstractFlatFileConnector;
import com.identity4j.connector.flatfile.LocalDelimitedFlatFile;
import com.identity4j.connector.flatfile.LocalFixedWidthFlatFile;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.StringUtil;
import com.identity4j.util.Util;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.impl.UnixBlowfishEncoder;
import com.identity4j.util.crypt.impl.UnixDESEncoder;
import com.identity4j.util.crypt.impl.UnixMD5Encoder;
import com.identity4j.util.crypt.impl.UnixSHA256Encoder;
import com.identity4j.util.crypt.impl.UnixSHA512Encoder;
import com.identity4j.util.passwords.PasswordCharacteristics;
import com.identity4j.util.passwords.UNIXPasswordCharacteristics;
import com.identity4j.util.validator.ValidationException;

public class UnixConnector extends AbstractFlatFileConnector<UnixConfiguration> {

	// /etc/shadow
	private static final int DAYS_SINCE_LAST_PASSWORD_CHANGE_INDEX = 2;
	private static final int DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED_INDEX = 3;
	private static final int DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED_INDEX_INDEX = 4;
	private static final int DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_USER_IS_WARNED_INDEX = 5;
	private static final int DAYS_AFTER_PASSWORD_EXPIRES_ACCOUNT_IS_DISABLED_INDEX = 6;
	private static final int DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX = 7;

	// /etc/passwd
	private static final int GID_INDEX = 2;
	private static final int SHELL_FIELD_INDEX = 6;
	private static final int HOME_FIELD_INDEX = 5;
	private static final int GID_FIELD_INDEX = 3;

	// Attributes
	static final String ATTR_HOME = "home";
	static final String ATTR_SHELL = "shell";
	static final String ATTR_DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED = "daysBeforePasswordMayBeChanged";
	static final String ATTR_DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED = "daysAfterWhichPasswordMustBeChanged";
	static final String ATTR_DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_THAT_USER_IS_WARNED = "daysBeforePasswordIsToExpireThatUserIsWarned";
	static final String ATTR_DAYS_AFTER_PASSWORD_EXPIRES_THAT_ACCOUNT_IS_DISABLED = "daysAfterPasswordExpiresThatAccountIsDisabled";
	static final String ATTR_DAYS_SINCE_ACCOUNT_WAS_DISABLED = "daysSinceAccountWasDisabled";

	private final static Log LOG = LogFactory.getLog(UnixConnector.class);

	private LocalDelimitedFlatFile groupFlatFile;
	private LocalDelimitedFlatFile shadowFlatFile;
	private boolean passwordsInShadow = true;
	private final Map<String, Role> roleMap = new HashMap<String, Role>();
	private final Map<String, List<String>> additionalGroups = new HashMap<String, List<String>>();
	private long lastLogLastLoaded = -1;

	private LocalFixedWidthFlatFile lastLogFlatFile;

	public UnixConnector() {
		super(UnixDESEncoder.ID, UnixMD5Encoder.ID, UnixBlowfishEncoder.ID, UnixSHA256Encoder.ID, UnixSHA512Encoder.ID);
	}

	@Override
	public void checkLoaded() throws ConnectorException {
		try {
			checkGroupLoaded();
		} catch (IOException ioe) {
			throw new ConnectorException("Failed to load group file.", ioe);
		}
		try {
			checkLastLogLoaded();
		} catch (IOException ioe) {
			if (LOG.isDebugEnabled()) {
				LOG.warn("Failed to process lastlog. Last login times will not be collected.", ioe);
			} else {
				LOG.warn("Failed to process lastlog. Last login times will not be collected. " + ioe.getMessage());
			}
		}
		super.checkLoaded();
		try {
			checkShadowLoaded();
		} catch (IOException ioe) {
			throw new ConnectorException("Failed to load shadow file.", ioe);
		}
	}

	@Override
	public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
		checkLoaded();
		return ResultIterator.createDefault(new RoleIterator(), opContext.getTag());
	}

	@Override
	public Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
		checkLoaded();
		Role role = roleMap.get(roleName);
		if (role == null) {
			final List<String> row = groupFlatFile.getRowByKeyField(0, roleName);
			if (row == null)
				throw new PrincipalNotFoundException(String.format("No role named '%s'", roleName));
			role = new RoleImpl(row.get(GID_INDEX), roleName);
			roleMap.put(roleName, role);
		}
		return role;
	}

	protected boolean isStoredPasswordValid(char[] password, char[] storedPassword, Encoder encoderForStoredPassword,
			final String charset) throws UnsupportedEncodingException {
		if (storedPassword.length > 0 && storedPassword[0] == '!' && (password == null || password.length == 0)) {
			// Disabled
			return false;
		}
		return encoderForStoredPassword.match(new String(storedPassword).getBytes(charset),
				new String(password).getBytes(charset), null, charset);
	}

	@Override
	public void lockIdentity(Identity identity) throws ConnectorException {
		List<String> row = getPasswordFile().getRowByKeyField(getConfiguration().getKeyFieldIndex(),
				identity.getPrincipalName());
		String password = row.get(getConfiguration().getPasswordFieldIndex());
		List<String> shadowRow = passwordsInShadow
				? shadowFlatFile.getRowByKeyField(getConfiguration().getKeyFieldIndex(), identity.getPrincipalName())
				: null;
		if (!passwordsInShadow && password.startsWith("!") || passwordsInShadow && password.startsWith("!")
				&& !getFromRowOrDefault(shadowRow, DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX, "").trim().equals("")) {
			throw new IllegalStateException("Account already locked");
		}
		try {
			if (!password.startsWith("!")) {
				password = "!" + password;
				row.set(getConfiguration().getPasswordFieldIndex(), password);
				getPasswordFile().writeRows();
			}
			if (passwordsInShadow) {
				final long now = System.currentTimeMillis();
				setOnRowOrAdd(shadowRow, DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX,
						String.valueOf(now / 1000 / 60 / 60 / 24));
				shadowFlatFile.writeRows();
			}
			identity.getAccountStatus().lock();
		} catch (IOException e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
	    // Some explanation - http://www.itworld.com/endpoint-security/275056/how-enforce-password-complexity-linux
		try {
	    
		UNIXPasswordCharacteristics c = new UNIXPasswordCharacteristics();
	    c.setMinimumSize(6);
	    c.setDictionaryWordsAllowed(true);
	    c.setContainUsername(true);
	    c.setUseCracklib(false);
	    
	    
	    // Look for PAM configuration
	    File file = new File("/etc/pam.d/common-password");
	    if(file.exists()) {
	        LOG.debug("Use (Debian style) PAM from " + file);
	    }
	    else  {
	    	file = new File("/etc/pam.d/system-auth");
	    	if(file.exists()) {
	    		LOG.debug("Use (Redhat style) PAM from " + file);
	    	}
	    	else {
	    		file = null;
	    	}
	    }
	    
	    if(file != null) {

	        int lcredit = 1;
	        int ucredit = 1;
	        int dcredit = 1;
	        int ocredit = 1;
	        int minlength = -1;
	        int maxlength = -1;
	        int entries = 0;
	        String authToken = "UNIX";
	        int historySize = 0;
	        
	        // Unix
	        List<String> lines = FileUtils.readLines(file);
//	        var process = sshClient.executeCommand("grep \"password.*pam_unix.so\" " + file);
	        String scheme = "des";
	        for(String line : lines) {
	            line = line.trim();
	            if(!line.startsWith("#") && line.matches(".*password.*pam_unix.so")) {
	                entries++;
	                String[] elements = line.split(" ");
	                for(int i = 0; i < elements.length; i++) {
	                    if(elements[i].startsWith("remember")) {
	                        historySize = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("min") || elements[i].startsWith("minlen")) {
	                        minlength = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("max") || elements[i].startsWith("maxlen")) {
	                        maxlength = parsePamVal(elements[i]);
	                    }
	                    if(elements[i] == "md5" || elements[i] == "sha256" || elements[i] == "sha512" || elements[i] == "bigcrypt" || elements[i] == "blowfish") {
	                        scheme = elements[i];
	                    }
	                }
	            }
	        }
	        if(entries > 0) {
	            LOG.debug("Found pam_unix minlength = " + minlength + " maxlenght = " + maxlength + " scheme = " + scheme);
	            c.setHistorySize(historySize);
	            if(minlength > -1) {
	                c.setMinimumSize(minlength);
	            }
	            if(maxlength > -1) {
	                c.setMaximumSize(maxlength);
	            }
	            else if(scheme == "des") {
	                c.setMaximumSize(8);
	            }
	        }
	        
	        // Cracklib
	        entries = 0;
	        for(String line : lines) {
	            line = line.trim();
	            if(!line.startsWith("#") && line.matches(".*password.*pam_cracklib.so.*")) {
	                entries++;
	                String[] elements = line.split(" ");
	                for(int i = 0; i < elements.length; i++) {
	                    if(elements[i].startsWith("lcredit")) {
	                        lcredit = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("ucredit")) {
	                        ucredit = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("dcredit")) {
	                        dcredit = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("ocredit")) {
	                        ocredit = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("minlen")) {
	                        minlength = parsePamVal(elements[i]);
	                    }
	                    if(elements[i].startsWith("authtok_type")) {
	                        authToken = parsePamValStr(elements[i], authToken);
	                    }               
	                    
	                    if(elements[i].startsWith("reject_username")) {
	                        // TODO this would actually prevent username in reverse as well but
	                        // NAM doesn"t yet support this check
	                        c.setContainUsername(false);
	                    }
	                }
	            }
	        }
	        
	        if(entries > 0) {
	            // The very existence of cracklib signals dictionary words are not allowed
	            c.setDictionaryWordsAllowed(false);
	            c.setAuthToken(authToken);
	            c.setUseCracklib(true);
	            
	            c.setMinimumSize(6); // Minimum required by cracklib
	            int requiredMatches = 4;
	            LOG.debug("Found pam_cracklib minlength = " + minlength + " ocredit = " + ocredit + " lcredit = " + lcredit + " ucredit = " + ucredit  + " dcredit = " +dcredit);
	            
	            /* When the credit numbers are negative, these are fixed minimum character counts
	             * that must match. When negative, they are suppose to be optional but credit towards
	             * the minimum length. We can"t really do that, so they are not optional.
	             */
	            if(lcredit < 0) {
	                c.setMinimumLowerCase(Math.abs(lcredit));
	            }
	            else if(lcredit > 0) {
	                c.setMinimumLowerCase(lcredit);
	                minlength = minlength - lcredit;
	            } else {
	                requiredMatches--;
	            }
	            
	            if(ucredit < 0) {
	                c.setMinimumUpperCase(Math.abs(ucredit));
	            }
	            else if(ucredit > 0) {
	                c.setMinimumUpperCase(ucredit);
	                minlength = minlength - ucredit;
	            } else {
	                requiredMatches--;
	            }
	            
	            if(dcredit < 0) {
	                c.setMinimumDigits(Math.abs(dcredit));
	            }
	            else if(dcredit > 0) {
	                c.setMinimumDigits(dcredit);
	                minlength = minlength - dcredit;
	            } else {
	                requiredMatches--;
	            }
	            
	            if(ocredit < 0) {
	                c.setMinimumSymbols(Math.abs(ocredit));
	            }
	            else if(ocredit > 0) {
	                c.setMinimumSymbols(ocredit);
	                minlength = minlength - ocredit;
	            } else {
	                requiredMatches--;
	            }

	            c.setRequiresMatches(requiredMatches);
	            if(minlength > c.getMinimumSize()) {
	                c.setMinimumSize(minlength);
	            }
	            LOG.debug("Final pam_cracklib minlength = " + minlength);
	        }
	    }
	    else {
	        LOG.debug("No password rules were retreived from PAM");
	    }
	    return c;
		}
		catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	    
		
	}

	private int parsePamVal(String str) {
	    int i = str.indexOf('=');
	    if(i != -1) {
	        return Integer.parseInt(str.substring(i + 1).trim());
	    }
	    return -1;
	}

	private String parsePamValStr(String str, String defaultVal) {
	    int i = str.indexOf('=');
	    if(i != -1) {
	        return str.substring(i + 1).trim();
	    }
	    return defaultVal;
	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		List<String> row = getPasswordFile().getRowByKeyField(getConfiguration().getKeyFieldIndex(),
				identity.getPrincipalName());
		List<String> shadowRow = passwordsInShadow
				? shadowFlatFile.getRowByKeyField(getConfiguration().getKeyFieldIndex(), identity.getPrincipalName())
				: null;
		String password = row.get(1);
		if (!passwordsInShadow && !password.startsWith("!") || passwordsInShadow && !password.startsWith("!")
				&& getFromRowOrDefault(shadowRow, DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX, "").trim().equals("")) {
			throw new IllegalStateException("Account not locked");
		}
		try {
			if (password.startsWith("!")) {
				password = password.substring(1);
				row.set(getConfiguration().getPasswordFieldIndex(), password);
				getPasswordFile().writeRows();
			}
			if (passwordsInShadow) {
				shadowRow.set(DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX, "");
				shadowFlatFile.writeRows();
			}
			identity.getAccountStatus().unlock();
		} catch (IOException e) {
			throw new ConnectorException("Unlock account failure during write", e);
		}
	}

	@Override
	protected void onSetPassword(AbstractFlatFile passwordFile, int passwordFieldIndex, int keyFieldIndex,
			Identity identity, char[] password, PasswordResetType type) {
		
		List<String> row = passwordFile.getRowByKeyField(keyFieldIndex, identity.getPrincipalName());
		if (passwordsInShadow) {
			// Move the encoded password from passwd to shadow
			String encpw = row.set(getConfiguration().getPasswordFieldIndex(), "x");
			row = shadowFlatFile.getRowByKeyField(keyFieldIndex, identity.getPrincipalName());
			row.set(getConfiguration().getPasswordFieldIndex(), encpw);
			final long now = System.currentTimeMillis();
			row.set(DAYS_SINCE_LAST_PASSWORD_CHANGE_INDEX, String.valueOf(now / 1000 / 60 / 60 / 24));
			identity.setPasswordStatus(createPasswordStatusFromShadowRow(row));
			try {
				shadowFlatFile.writeRows();
			} catch (IOException e) {
				throw new ConnectorException("Write failure", e);
			}
		}
	}

	private PasswordStatus createPasswordStatusFromShadowRow(List<String> row) {
		PasswordStatus status = new PasswordStatus();

		// Last password change
		String daysSinceLastPasswordChange = row.get(DAYS_SINCE_LAST_PASSWORD_CHANGE_INDEX);
		if (!StringUtil.isNullOrEmpty(daysSinceLastPasswordChange)) {
			Date lastPasswordChange = new Date();
			lastPasswordChange.setTime(Util.daysToMillis(Long.parseLong(daysSinceLastPasswordChange)));
			status.setLastChange(lastPasswordChange);

			// How long must the user wait before they can change their password
			String daysBeforePasswordMayBeChanged = row.get(DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED_INDEX).trim();
			if (!StringUtil.isNullOrEmpty(daysBeforePasswordMayBeChanged)) {
				status.setUnlocked(new Date(lastPasswordChange.getTime()
						+ Util.daysToMillis(Long.parseLong(daysBeforePasswordMayBeChanged))));
			}

			// When is the password supposed to expire?
			String daysAfterWhichPasswordMustBeChanged = row.get(DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED_INDEX_INDEX)
					.trim();
			if (!StringUtil.isNullOrEmpty(daysAfterWhichPasswordMustBeChanged)) {
				status.setExpire(new Date(lastPasswordChange.getTime()
						+ Util.daysToMillis(Long.parseLong(daysAfterWhichPasswordMustBeChanged))));
			}

			// When is the user supposed to be warned?
			String daysBeforePasswordIsToExpireUserIsWarned = row
					.get(DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_USER_IS_WARNED_INDEX).trim();
			if (!StringUtil.isNullOrEmpty(daysBeforePasswordIsToExpireUserIsWarned)) {
				status.setWarn(new Date(lastPasswordChange.getTime()
						- Util.daysToMillis(Long.parseLong(daysBeforePasswordIsToExpireUserIsWarned))));
			}

			// When is the account supposed to be disabled?
			String daysAfterPasswordExpiresAccountIsDisabled = row
					.get(DAYS_AFTER_PASSWORD_EXPIRES_ACCOUNT_IS_DISABLED_INDEX).trim();
			if (!StringUtil.isNullOrEmpty(daysAfterPasswordExpiresAccountIsDisabled)) {
				status.setDisable(new Date(lastPasswordChange.getTime()
						- Util.daysToMillis(Long.parseLong(daysAfterPasswordExpiresAccountIsDisabled))));
			}

			// Determine in the status
			status.calculateType();

		}

		return status;
	}

	@Override
	protected void updateUserRow(List<String> row, Identity identity) {
		final Role[] groups = identity.getRoles();
		if (groups.length < 1) {
			throw new ValidationException(this, "unix", "Connector.UpdateUser.ErrorHasNoGroups",
					identity.getPrincipalName());
		}
		Role primaryGroup = groups[0];
		row.set(GID_FIELD_INDEX, primaryGroup.getGuid());
		row.set(HOME_FIELD_INDEX, identity.getAttributeOrDefault(ATTR_HOME, ""));
		row.set(SHELL_FIELD_INDEX, identity.getAttributeOrDefault(ATTR_SHELL, ""));
		if (passwordsInShadow) {
			List<String> shadowRow = shadowFlatFile.getRowByKeyField(0, identity.getPrincipalName());
			maybeSet(DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED_INDEX, ATTR_DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED, identity,
					shadowRow);
			maybeSet(DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED_INDEX_INDEX,
					ATTR_DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED, identity, shadowRow);
			maybeSet(DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_USER_IS_WARNED_INDEX,
					ATTR_DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_THAT_USER_IS_WARNED, identity, shadowRow);
			maybeSet(DAYS_AFTER_PASSWORD_EXPIRES_ACCOUNT_IS_DISABLED_INDEX,
					ATTR_DAYS_AFTER_PASSWORD_EXPIRES_THAT_ACCOUNT_IS_DISABLED, identity, shadowRow);
			maybeSet(DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX, ATTR_DAYS_SINCE_ACCOUNT_WAS_DISABLED, identity, shadowRow);
			try {
				shadowFlatFile.writeRows();
			} catch (IOException e) {
				throw new ConnectorException("Write failure", e);
			}
		}
	}

	private void maybeSet(int idx, String name, Identity identity, List<String> shadowRow) {
		String value = identity.getAttributeOrDefault(name, "");
		shadowRow.set(idx, value);
	}

	@Override
	protected int getColumnCount() {
		return 7;
	}

	@Override
	protected String getInitialGUID(Identity identity) {
		String guid = identity.getGuid();
		if (StringUtil.isNullOrEmpty(guid)) {
			// Find the next highest UID

			// TODO starting at 1000 is a bit arbitrary, make configurable?
			int uid = 1000;
			for (Iterator<Identity> identityIterator = allIdentities(OperationContext.createDefault()); identityIterator.hasNext();) {
				Identity exitingIdentity = identityIterator.next();
				uid = Math.max(uid, Integer.parseInt(exitingIdentity.getGuid()));
			}
			return String.valueOf(++uid);
		}
		return super.getInitialGUID(identity);
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		checkLoaded();

		// must have a principal name
		if (StringUtil.isNullOrEmpty(role.getPrincipalName())) {
			throw new ConnectorException("No principal found");
		}

		List<String> row = groupFlatFile.getRowByKeyField(GID_INDEX, role.getGuid());
		row.set(0, role.getPrincipalName());

		// write the entire file
		try {
			groupFlatFile.writeRows();
		} catch (IOException e) {
			throw new ConnectorException("Write failure", e);
		}
	}

	@Override
	public Role createRole(Role role) throws ConnectorException {
		checkLoaded();

		// must have a principal name
		if (StringUtil.isNullOrEmpty(role.getPrincipalName())) {
			throw new ConnectorException("Role name may not be empty");
		}

		// Build up the row
		List<String> row = new ArrayList<String>();
		row.add(role.getPrincipalName());
		row.add("x");
		row.add(getInitialGGUID(role));
		row.add("");

		// write new row to remote file
		try {
			groupFlatFile.add(row);
			if (groupFlatFile.getFile().getFileSystem().hasCapability(Capability.APPEND_CONTENT)) {
				groupFlatFile.appendRow(row);
			} else {
				groupFlatFile.writeRows();
			}
		} catch (ConnectorException ce) {
			groupFlatFile.remove(row);
			throw ce;
		} catch (IOException e) {
			groupFlatFile.remove(row);
			throw new ConnectorException("Write failure", e);
		}

		// return the newly created role
		return getRoleByName(role.getPrincipalName());

	}

	@Override
	public void deleteRole(String principalName) throws ConnectorException {
		checkLoaded();
		groupFlatFile.remove(principalName);
		try {
			groupFlatFile.writeRows();
		} catch (IOException e) {
			throw new ConnectorException("delete role failure during write", e);
		}
	}

	protected String getInitialGGUID(Role role) {
		String guid = role.getGuid();
		if (StringUtil.isNullOrEmpty(guid)) {
			// Find the next highest GID
			// TODO starting at 100 is a bit arbitrary, make configurable?
			int gid = 100;
			for (Iterator<Role> roleIterator = allRoles(OperationContext.createDefault()); roleIterator.hasNext();) {
				Role existingrole = roleIterator.next();
				gid = Math.max(gid, Integer.parseInt(existingrole.getGuid()));
			}
			guid = String.valueOf(++gid);
		}
		return guid;
	}

	@Override
	protected void onCreateUser(Identity identity, List<String> row, char[] password) throws ConnectorException {
		row.set(HOME_FIELD_INDEX, identity.getAttributeOrDefault(ATTR_HOME, ""));
		row.set(SHELL_FIELD_INDEX, identity.getAttributeOrDefault(ATTR_SHELL, ""));

		if (passwordsInShadow) {
			final LocalDelimitedFlatFile passwordFile = getPasswordFile();
			List<String> passwordRow = passwordFile.getRowByKeyField(0, identity.getPrincipalName());
			if (passwordRow == null) {
				passwordRow = new ArrayList<String>();
				passwordRow.add(identity.getPrincipalName()); // login name
				passwordRow.add("*"); // encrypted password
				passwordRow.add(String.valueOf(System.currentTimeMillis() / 1000 / 60 / 60 / 24));
				passwordRow.add(identity.getAttributeOrDefault(ATTR_DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED, ""));
				passwordRow.add(identity.getAttributeOrDefault(ATTR_DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED, ""));
				passwordRow.add(
						identity.getAttributeOrDefault(ATTR_DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_THAT_USER_IS_WARNED, ""));
				passwordRow.add(
						identity.getAttributeOrDefault(ATTR_DAYS_AFTER_PASSWORD_EXPIRES_THAT_ACCOUNT_IS_DISABLED, ""));
				String daysSinceAccountDisabled = identity.getAttributeOrDefault(ATTR_DAYS_SINCE_ACCOUNT_WAS_DISABLED,
						"");
				passwordRow.add(daysSinceAccountDisabled.equals("0") ? "" : daysSinceAccountDisabled);
				// reserved
				passwordRow.add("");
				passwordFile.add(passwordRow);

				try {
					if (passwordFile.getFile().getFileSystem().hasCapability(Capability.APPEND_CONTENT)) {
						passwordFile.appendRow(passwordRow);
					} else {
						passwordFile.writeRows();
					}
				} catch (IOException e) {
					throw new ConnectorException("Failed to append row.", e);
				}

				try {
					passwordRow.set(getConfiguration().getPasswordFieldIndex(),
							new String(
									getEncoderManager().encode(password,
											getConfiguration().getIdentityPasswordEncoding(),
											getConfiguration().getCharset(), null, null),
									getConfiguration().getCharset()));
					passwordFile.writeRows();
				} catch (UnsupportedEncodingException e) {
					throw new ConnectorException(e);
				} catch (EncoderException e) {
					throw new ConnectorException(e);
				} catch (IOException e) {
					throw new ConnectorException(e);
				}

			} else {
				setPassword(passwordFile, 1, 0, identity, password, PasswordResetType.USER);
			}
		} else {
		}
	}

	@Override
	protected void createPassword(char[] password, List<String> row) throws EncoderException, Error {
		if (passwordsInShadow) {
			/* Do nothing, we handle in onCreateUser */
		} else {
			super.createPassword(password, row);
		}
	}

	@Override
	protected char[] getPasswordForIdentity(Identity identity) {
		return getPasswordFile().getRowByKeyField(0, identity.getPrincipalName()).get(1).toCharArray();
	}

	@Override
	protected void onOpen(UnixConfiguration config) {
		super.onOpen(config);
		reset();
	}

	protected void reset() {
		lastLogFlatFile = null;
		lastLogLastLoaded = -1;
		shadowFlatFile = null;
		groupFlatFile = null;
	}

	@Override
	protected Encoder getEncoderForStoredPassword(char[] storedPassword) throws UnsupportedEncodingException {
		Encoder encoder = super.getEncoderForStoredPassword(storedPassword);
		if (encoder == null) {
			return getEncoderManager().getEncoderById(UnixDESEncoder.ID);
		}
		return encoder;
	}

	@Override
	protected Identity createIdentity(List<String> row) {
		IdentityImpl identity = (IdentityImpl) super.createIdentity(row);

		// If there is a shadow file we have access to last password change
		// info
		if (shadowFlatFile != null) {
			List<String> shadowRow = shadowFlatFile.getRowByKeyField(0, identity.getPrincipalName());
			if (shadowRow == null) {
				LOG.warn("No entry in " + shadowFlatFile.getFile() + " for " + identity.getPrincipalName());
			} else {
				identity.setPasswordStatus(createPasswordStatusFromShadowRow(shadowRow));
				Date now = new Date();
				String daysSinceAccountWasDisabled = getFromRowOrDefault(shadowRow,
						DAYS_SINCE_ACCOUNT_WAS_DISABLED_INDEX, "");
				if (!StringUtil.isNullOrEmpty(daysSinceAccountWasDisabled)) {
					// Explicitly disabled
					identity.getAccountStatus().lock();
					identity.setAttribute(ATTR_DAYS_SINCE_ACCOUNT_WAS_DISABLED, daysSinceAccountWasDisabled);
				} else {
					identity.setAttribute(ATTR_DAYS_SINCE_ACCOUNT_WAS_DISABLED, "0");
					String daysAfterPasswordExpiresAccountIsDisabled = shadowRow
							.get(DAYS_AFTER_PASSWORD_EXPIRES_ACCOUNT_IS_DISABLED_INDEX).trim();
					if (!StringUtil.isNullOrEmpty(daysAfterPasswordExpiresAccountIsDisabled)) {
						Date expire = new Date(identity.getPasswordStatus().getLastChange().getTime()
								- Util.daysToMillis(Long.parseLong(daysAfterPasswordExpiresAccountIsDisabled)));
						if (now.after(expire)) {
							// Disabled because past maximum expire days
							identity.getAccountStatus().lock();
						}
					}
				}

				identity.setAttribute(ATTR_DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED,
						shadowRow.get(DAYS_BEFORE_PASSWORD_MAY_BE_CHANGED_INDEX));
				identity.setAttribute(ATTR_DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED,
						shadowRow.get(DAYS_AFTER_WHICH_PASSWORD_MUST_BE_CHANGED_INDEX_INDEX));
				identity.setAttribute(ATTR_DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_THAT_USER_IS_WARNED,
						shadowRow.get(DAYS_BEFORE_PASSWORD_IS_TO_EXPIRE_USER_IS_WARNED_INDEX));
				identity.setAttribute(ATTR_DAYS_AFTER_PASSWORD_EXPIRES_THAT_ACCOUNT_IS_DISABLED,
						shadowRow.get(DAYS_AFTER_PASSWORD_EXPIRES_ACCOUNT_IS_DISABLED_INDEX));

				// Fall back to determining password disable
				// TODO this may cause incomplete account lockouts if
				// details
				// are edited outside of nervepoint
				if (!identity.getAccountStatus().equals(AccountStatusType.locked) && shadowRow.get(1).startsWith("!")) {
					identity.getAccountStatus().lock();

				}
			}
		} else {
			identity.getAccountStatus()
					.setType(row.get(1).startsWith("!") ? AccountStatusType.locked : AccountStatusType.unlocked);
		}

		// We might have the last login time
		if (lastLogFlatFile != null) {
			List<String> lastLogRow = lastLogFlatFile.getRowByKeyField(0, identity.getPrincipalName());
			if (lastLogRow != null) {
				String date = lastLogRow.get(3).trim();
				if (!date.equals("**Never logged in**")) {
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
					try {
						identity.setLastSignOnDate(sdf.parse(date));
					} catch (ParseException e) {
						LOG.warn("Invalid date '" + date + "' parsing lastlog");
					}
				}
			}
		}

		// Other attributes
		identity.setAttribute(ATTR_HOME, row.get(HOME_FIELD_INDEX));
		identity.setAttribute(ATTR_SHELL, row.get(SHELL_FIELD_INDEX));

		doIdentityRoles(row, identity);

		return identity;
	}

	private void doIdentityRoles(List<String> row, IdentityImpl identity) {
		// Add the users primary group
		String gid = row.get(GID_FIELD_INDEX);
		List<String> groupRow = groupFlatFile.getRowByKeyField(GID_INDEX, gid);
		Role role = null;
		if (groupRow != null) {
			role = getRoleByName(groupRow.get(0));
			identity.addRole(role);
		}

		// Add the additional groups
		List<String> groups = additionalGroups.get(identity.getPrincipalName());
		if (groups != null) {
			for (String groupName : groups) {
				if (role == null || !groupName.equals(role.getPrincipalName())) {
					Role additionalGroup = getRoleByName(groupName);
					if (additionalGroup != null) {
						identity.addRole(additionalGroup);
					}
				}
			}
		}
	}

	private void checkLastLogLoaded() throws IOException {
		if (lastLogFlatFile == null) {
			lastLogFlatFile = new LocalFixedWidthFlatFile(getConfiguration().getCharset());
			lastLogFlatFile.setFirstRowIsHeading(true);
			lastLogFlatFile.setAutoDetermineWidths(true);
			lastLogFlatFile.addIndex(0); // Name
		}
		if (lastLogLastLoaded == -1 || System.currentTimeMillis() > lastLogLastLoaded + 60000) {
			try {
				loadLastLog();
			} finally {
				lastLogLastLoaded = System.currentTimeMillis();
			}
		}
	}

	private void checkGroupLoaded() throws IOException {
		if (groupFlatFile == null) {
			groupFlatFile = new LocalDelimitedFlatFile(
					getFileSystemManager().resolveFile(((UnixConfiguration) getConfiguration()).getGroupFileUri()),
					getConfiguration().getCharset());
			groupFlatFile.addIndex(0); // Name
			groupFlatFile.addIndex(GID_INDEX); // GID
			groupFlatFile.setFieldSeparator(':');
		}
		if (groupFlatFile.isStale()) {
			roleMap.clear();
			additionalGroups.clear();
			groupFlatFile.load();

			// Index the groups users
			loadAdditionalGroupUsers();
		}
	}

	private LocalDelimitedFlatFile getPasswordFile() {
		if (passwordsInShadow) {
			return shadowFlatFile;
		}
		return (LocalDelimitedFlatFile) super.getFlatFile();
	}

	private void loadLastLog() throws IOException {
		Process process = new ProcessBuilder("lastlog").redirectErrorStream(true).start();

		// TODO handle error stream
		try {
			InputStream inputStream = process.getInputStream();
			try {
				lastLogFlatFile.load(inputStream, getConfiguration().getCharset());
			} finally {
				inputStream.close();
			}
		} finally {
			try {
				int returnCode = process.waitFor();
				if (returnCode != 0) {
					throw new IOException("Last command failed with status " + returnCode);
				}
			} catch (InterruptedException e) {
				IOException ioe = new IOException("Process interrupted.");
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	private void loadAdditionalGroupUsers() {
		for (List<String> row : groupFlatFile.getContents()) {
			if (row.size() > 3) {
				String users = row.get(3);
				StringTokenizer t = new StringTokenizer(users, ",");
				while (t.hasMoreTokens()) {
					String user = t.nextToken();
					List<String> groups = additionalGroups.get(user);
					if (groups == null) {
						groups = new ArrayList<String>();
						additionalGroups.put(user, groups);
					}
					groups.add(row.get(0));
				}
			}
		}
	}

	private void checkShadowLoaded() throws IOException {
		// If we have already decided passwords are not in the shadow file just
		// exit
		if (!passwordsInShadow) {
			return;
		}

		if (shadowFlatFile == null) {
			FileObject shadowFile = getFileSystemManager()
					.resolveFile(((UnixConfiguration) getConfiguration()).getShadowFileUri());
			if (shadowFile.exists()) {
				/*
				 * If there is a shadow file, then the passwords are here. It
				 * must be readable by the Java process owner
				 */
				shadowFlatFile = new LocalDelimitedFlatFile(shadowFile, getConfiguration().getCharset());
				shadowFlatFile.addIndex(0);
				shadowFlatFile.setFieldSeparator(':');
			} else {
				// Passwords are in /etc/passwd file
				passwordsInShadow = false;
			}
		}
		if (passwordsInShadow) {
			shadowFlatFile.reloadIfStale();
		}
	}

	class RoleIterator implements Iterator<Role> {
		private int row = 0;

		public boolean hasNext() {
			return row < groupFlatFile.size();
		}

		public Role next() {
			List<String> list = groupFlatFile.getContents().get(row++);
			String keyFieldValue = list.get(0);
			return getRoleByName(keyFieldValue);
		}

		public void remove() {
			groupFlatFile.getContents().remove(row);
		}
	}
}
