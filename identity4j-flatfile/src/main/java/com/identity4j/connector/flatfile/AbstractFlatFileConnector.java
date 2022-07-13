/* HEADER */
package com.identity4j.connector.flatfile;

/*
 * #%L
 * Identity4J Flat File
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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;

import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.vfs.AbstractVFSConnector;
import com.identity4j.util.StringUtil;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.EncoderManager;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public class AbstractFlatFileConnector<P extends AbstractFlatFileConfiguration> extends AbstractVFSConnector<P> {
	private final static EncoderManager encoderManager = DefaultEncoderManager.getInstance();

	private LocalDelimitedFlatFile flatFile;
	private final Map<String, Identity> identityMap = new HashMap<String, Identity>();
	private boolean open;
	private final Collection<String> supportedEncoderTypes;

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays.asList(new ConnectorCapability[] { 
			ConnectorCapability.passwordChange,
			ConnectorCapability.passwordSet,
			ConnectorCapability.createUser,
			ConnectorCapability.deleteUser,
			ConnectorCapability.updateUser,
			ConnectorCapability.hasFullName,
			ConnectorCapability.roles,
			ConnectorCapability.authentication,
			ConnectorCapability.requireGUID,
			ConnectorCapability.createIdentityGUID,
			ConnectorCapability.identities
	}));
	
	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}
	
	public AbstractFlatFileConnector(String... supportedEncoderTypes) {
		this.supportedEncoderTypes = Arrays.asList(supportedEncoderTypes);
	}

	public AbstractFlatFileConnector() {
		this(encoderManager.getEncoderIds());
	}

	public Collection<String> getSupportedEncoderTypes() {
		return supportedEncoderTypes;
	}

	public AbstractFlatFile getFlatFile() {
		return flatFile;
	}

	@Override
	public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {
		checkLoaded();
		return new FlatFileConnectorIdentityIterator(flatFile, getConfiguration().getKeyFieldIndex(), this, opContext.getTag());
	}

	public EncoderManager getEncoderManager() {
		return encoderManager;
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		if (getConfiguration().getPasswordFieldIndex() > -1) {
			if (forcePasswordChangeAtLogon) {
				throw new UnsupportedOperationException("Flatfile connectors do not support force password change at logon");
			}
			setPassword(flatFile, getConfiguration().getPasswordFieldIndex(), getConfiguration().getKeyFieldIndex(), identity, password, type);
		} else {
			// Will throw an exception
			super.setPassword(identity, password, forcePasswordChangeAtLogon, type);
		}
	}

	protected final void setPassword(AbstractFlatFile passwordFile, int passwordFieldIndex, int keyFieldIndex, Identity identity,
			char[] password, PasswordResetType type) throws ConnectorException {
		List<String> row = passwordFile.getRowByKeyField(keyFieldIndex, identity.getPrincipalName());
		try {
			row.set(
				passwordFieldIndex,
				new String(encoderManager.encode(password, getConfiguration().getIdentityPasswordEncoding(), getConfiguration().getCharset(),
					null, null), getConfiguration().getCharset()));
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		onSetPassword(passwordFile, passwordFieldIndex, keyFieldIndex, identity, password, type);

		// Store
		try {
			passwordFile.writeRows();
		} catch (IOException e) {
			throw new ConnectorException("Write failure", e);
		}
	}

	protected void onSetPassword(AbstractFlatFile passwordFile, int passwordFieldIndex, int keyFieldIndex, Identity identity,
			char[] password, PasswordResetType type) {
	}

	@Override
	protected final boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		checkLoaded();
		if (getConfiguration().getPasswordFieldIndex() > -1) {
			char[] storedPassword = getPasswordForIdentity(identity);
			try {
				Encoder encoderForStoredPassword = getEncoderForStoredPassword(storedPassword);
				if (encoderForStoredPassword == null) {
					encoderForStoredPassword = encoderManager.getEncoderById(getConfiguration().getIdentityPasswordEncoding());
				}
				final String charset = getConfiguration().getCharset();
				return encoderForStoredPassword.match(new String(storedPassword).getBytes(charset),
					new String(password).getBytes(charset), null, charset);
			} catch (UnsupportedEncodingException e) {
				throw new ConnectorException("Failed to check credentials.", e);
			}
		} else {
			// Will throw an exception
			return super.areCredentialsValid(identity, password);
		}
	}

	protected char[] getPasswordForIdentity(Identity identity) {
		return flatFile.getRowByKeyField(getConfiguration().getKeyFieldIndex(), identity.getPrincipalName())
			.get(getConfiguration().getPasswordFieldIndex()).toCharArray();
	}

	protected Encoder getEncoderForStoredPassword(char[] storedPassword) throws UnsupportedEncodingException {
		byte[] storedPasswordBytes = new String(storedPassword).getBytes(getConfiguration().getCharset());

		// Look for encoder based on current stored password
		for (String encoderId : supportedEncoderTypes) {
			Encoder encoder = encoderManager.getEncoderById(encoderId);
			if (encoder.isOfType(storedPasswordBytes, getConfiguration().getCharset())) {
				return encoder;
			}
		}

		return null;
	}

	@Override
	public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
		return ResultIterator.createDefault(opContext.getTag());
	}

	@Override
	public Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
		return null;
	}

	@Override
	public Identity getIdentityByName(String keyFieldValue) {
		checkLoaded();
		Identity identity = identityMap.get(keyFieldValue);
		if (identity == null) {
			final List<String> row = flatFile.getRowByKeyField(getConfiguration().getKeyFieldIndex(), keyFieldValue);
			if (row == null) {
				throw new PrincipalNotFoundException("Principal " + keyFieldValue + " could not be found");
			}
			identity = createIdentity(row);
			identityMap.put(keyFieldValue, identity);
		}
		return identity;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	protected Identity createIdentity(List<String> row) {
		int guidFieldIndex = getConfiguration().getGuidFieldIndex();
		String principalName = row.get(getConfiguration().getKeyFieldIndex());
		if (guidFieldIndex != -1 && guidFieldIndex >= row.size()) {
			throw new IllegalArgumentException(
				"GUID field index is greater than than the number of columns in the row. This may suggest either an incorrect field separator, or an incorrect column number for GUID");
		}
		String guid = guidFieldIndex == -1 ? principalName : row.get(guidFieldIndex);
		int fullNameFieldIndex = getConfiguration().getFullNameFieldIndex();
		String fullName = fullNameFieldIndex == -1 ? null : row.get(fullNameFieldIndex);
		IdentityImpl identity = new IdentityImpl(guid, principalName);
		identity.setFullName(fullName);
		return identity;
	}

	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		checkLoaded();

		// must have a principal name
		if (StringUtil.isNullOrEmpty(identity.getPrincipalName())) {
			throw new ConnectorException("No principal found");
		}

		int columnCount = getColumnCount();
		if (flatFile.getColumnCount() > 0) {
			columnCount = flatFile.getColumnCount();
		}

		// Build up the row
		List<String> row = new ArrayList<String>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			row.add("");
		}
		row.set(getConfiguration().getPasswordFieldIndex(), "");
		row.set(getConfiguration().getKeyFieldIndex(), identity.getPrincipalName());
		if (getConfiguration().getGuidFieldIndex() > -1) {
			row.set(getConfiguration().getGuidFieldIndex(), getInitialGUID(identity));
		}
		if (getConfiguration().getFullNameFieldIndex() > -1) {
			row.set(getConfiguration().getFullNameFieldIndex(), identity.getFullName());
		}

		// Create password if supplied
		if (password != null) {
			createPassword(password, row);
		}

		// write new row to remote file
		try {
			onCreateUser(identity, row, password);
			flatFile.add(row);
			if (flatFile.getFile().getFileSystem().hasCapability(Capability.APPEND_CONTENT)) {
				flatFile.appendRow(row);
			} else {
				flatFile.writeRows();
			}
			onCreatedUser(identity, password);
		} catch (ConnectorException ce) {
			flatFile.remove(row);
			throw ce;
		} catch (IOException e) {
			flatFile.remove(row);
			throw new ConnectorException("Write failure", e);
		}

		// return the newly created identity
		return getIdentityByName(identity.getPrincipalName());

	}

	protected String getInitialGUID(Identity identity) {
		// Subclasses may override to process the GUID used on creation of an
		// identity
		if (StringUtil.isNullOrEmpty(identity.getGuid())) {
			throw new ConnectorException("GUID is required");
		} else {
			return identity.getGuid();
		}
	}

	protected void onCreateUser(Identity identity, List<String> row, char[] password) throws ConnectorException {
		// Invoked before file has been written and identity added to memory

	}

	protected void onCreatedUser(Identity identity, char[] password) throws ConnectorException {
		// Invoked when file has been written. The new identity will be in
		// memory
	}

	protected void createPassword(char[] password, List<String> row) throws EncoderException, Error {
		if (getConfiguration().getPasswordFieldIndex() > -1) {
			try {
				final String encodedPassword = new String(encoderManager.encode(password,
						getConfiguration().getIdentityPasswordEncoding(), getConfiguration().getCharset(), null, null), getConfiguration().getCharset());
				row.set(getConfiguration().getPasswordFieldIndex(), encodedPassword);
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}
		}
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		checkLoaded();

		// must have a principal name
		if (StringUtil.isNullOrEmpty(identity.getPrincipalName())) {
			throw new ConnectorException("No principal found");
		}

		List<String> row = getFlatFile().getRowByKeyField(getConfiguration().getKeyFieldIndex(), identity.getPrincipalName());

		// if no guid is used by remote system then ignore
		if (getConfiguration().getGuidFieldIndex() > -1) {
			row.set(getConfiguration().getGuidFieldIndex(),
				(StringUtil.isNullOrEmpty(identity.getGuid()) ? String.valueOf(flatFile.size()) : identity.getGuid()));
		}

		// if fullname is not used by remote system then ignore
		if (getConfiguration().getFullNameFieldIndex() > -1) {
			row.set(getConfiguration().getFullNameFieldIndex(), identity.getFullName());
		} else {
			// write an empty string, write method should replace blank space
			// with separator
			row.set(getConfiguration().getFullNameFieldIndex(), "");
		}

		// Give subclasses an oppurtunity to write their own row date
		updateUserRow(row, identity);

		// write the entire file
		try {
			flatFile.writeRows();
		} catch (IOException e) {
			throw new ConnectorException("Write failure", e);
		}
	}

	protected int getColumnCount() {
		return 4;
	}

	protected void updateUserRow(List<String> row, Identity identity) {
		// Override to add custom row data on update before write
	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		checkLoaded();
		flatFile.remove(principalName);
		try {
			flatFile.writeRows();
		} catch (IOException e) {
			throw new ConnectorException("delete user failure during write", e);
		}
	}

	@Override
	public Role createRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	protected void onOpen(P config) throws ConnectorException {
		super.onOpen(config);
		checkLoaded();
		open = true;
	}

	@Override
	protected void onClose() {
		open = false;
	}

	protected void checkLoaded() throws ConnectorException {
		final FileObject file = getFile();
		try {
			if (!file.exists()) {
				throw new FileNotFoundException(file + " does not exist.");
			}
		} catch (Exception fse) {
			throw new ConnectorException("Could not find flat file.", fse);
		}
		if (flatFile == null) {
			flatFile = new LocalDelimitedFlatFile(file, getConfiguration().getCharset());
			flatFile.addIndex(getConfiguration().getKeyFieldIndex());
			flatFile.setFieldSeparator(getConfiguration().getFieldSeparator());
			flatFile.setEscapeCharacter(getConfiguration().getEscapeCharacter());
			configureFlatFile(flatFile);
		}
		synchronized (identityMap) {
			if (flatFile.isStale()) {
				identityMap.clear();
				try {
					flatFile.load();
				} catch (IOException e) {
					throw new ConnectorException("Failed to load " + flatFile.getFile());
				}
			}
		}
	}

	protected void configureFlatFile(AbstractFlatFile flatFile) {
	}

	protected String getFromRowOrDefault(List<String> row, int idx, String defaultValue) {
		return idx < row.size() ? row.get(idx) : defaultValue;
	}

	protected void setOnRowOrAdd(List<String> row, int idx, String value) {
		while (row.size() < idx) {
			row.add("");
		}
		row.set(idx, value);
	}

}