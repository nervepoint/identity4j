package com.identity4j.connector.jdbc;

/*
 * #%L
 * Identity4J JDBC
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

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.Media;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.StringUtil;
import com.identity4j.util.crypt.EncoderManager;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public abstract class JDBCConnector<P extends ConnectorConfigurationParameters> extends AbstractConnector<P> {

	protected final static EncoderManager encoderManager = DefaultEncoderManager.getInstance();

	protected Connection connect = null;
	protected JDBCConfiguration configuration = null;

	static Log log = LogFactory.getLog(JDBCConnector.class);

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(
			Arrays.asList(new ConnectorCapability[] { ConnectorCapability.passwordChange,
					ConnectorCapability.passwordSet, ConnectorCapability.identities }));

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		Set<ConnectorCapability> caps = new LinkedHashSet<ConnectorCapability>();
		caps.addAll(capabilities);
		if (configuration != null) {
			if (!StringUtil.isNullOrEmpty(configuration.getIdentityFullnameColumn())) {
				caps.add(ConnectorCapability.hasFullName);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getRoleTable())) {
				caps.add(ConnectorCapability.roles);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getIdentityPasswordColumn())) {
				caps.add(ConnectorCapability.authentication);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getIdentityEmailColumn())) {
				caps.add(ConnectorCapability.hasEmail);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getIdentityEnabledColumn())) {
				caps.add(ConnectorCapability.accountDisable);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getIdentityLockedColumn())) {
				caps.add(ConnectorCapability.accountLocking);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getConfigurationParameters()
					.getString(JDBCConfiguration.SQL_IDENTITY_TABLE_UPDATE))) {
				caps.add(ConnectorCapability.updateUser);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getConfigurationParameters()
					.getString(JDBCConfiguration.SQL_IDENTITY_TABLE_CREATE))) {
				caps.add(ConnectorCapability.createUser);
			}
			if (!StringUtil.isNullOrEmpty(configuration.getConfigurationParameters()
					.getString(JDBCConfiguration.SQL_IDENTITY_TABLE_DELETE))) {
				caps.add(ConnectorCapability.deleteUser);
			}
		}

		return caps;
	}

	@Override
	public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {

		List<Identity> identities = new ArrayList<Identity>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getSelectIdentitiesSQL();
			resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				identities.add(createIdentity(resultSet));
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return ResultIterator.createDefault(identities.iterator(), opContext.getTag());
	}

	public Identity getIdentityByName(String name) throws PrincipalNotFoundException, ConnectorException {
		String sql = configuration.getSelectIdentitySQL(name);
		if (sql.equals("")) {
			return super.getIdentityByName(name);
		} else {
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				statement = connect.createStatement();
				resultSet = statement.executeQuery(sql);
				if (resultSet.next()) {
					return createIdentity(resultSet);
				}
			} catch (SQLException e) {
				throw new ConnectorException(e);
			} finally {
				closeStatement(statement);
				closeResultSet(resultSet);
			}
		}
		throw new PrincipalNotFoundException(name + " not found.");
	}

	protected List<Role> getGrantedRoles(Identity identity) {

		List<Role> roles = new ArrayList<Role>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getSelectIdentitiesRolesSQL(identity);
			if (sql.length() > 0) {
				resultSet = statement.executeQuery(sql);

				while (resultSet.next()) {
					roles.add(createRoleFromGrantResults(resultSet));
				}
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return roles;
	}
	
	protected Role createRoleFromGrantResults(ResultSet resultSet) throws SQLException {
		return createRole(resultSet);
	}

	protected Date getAsDate(ResultSet resultSet, int colidx) throws SQLException {
		// TODO This makes some big assumptions and will need tweaking
		// once we get some real use
		int columnType = resultSet.getMetaData().getColumnType(colidx);
		if (columnType == Types.DATE) {
			java.sql.Date date = resultSet.getDate(colidx);
			if (date != null)
				return date;
		} else if (columnType == Types.TIME || columnType == 2013) {
			// 2013 = Types.TIME_WITH_TIMEZONE (Java 8 only)
			Time time = resultSet.getTime(colidx);
			if (time != null)
				return new Date(time.getTime());
		} else if (columnType == Types.TIMESTAMP || columnType == 2014) {
			// 2014 = Types.TIMESTAMP_WITH_TIMEZONE (Java 8 only)
			Timestamp timestamp = resultSet.getTimestamp(colidx);
			if (timestamp != null)
				return new Date(timestamp.getTime());
		} else if (columnType == Types.BIGINT) {
			return new Date(resultSet.getLong(colidx));
		} else if (columnType == Types.INTEGER) {
			return new Date(resultSet.getLong(colidx) * 1000l);
		}
		return null;
	}

	protected Identity createIdentity(ResultSet resultSet) throws SQLException {

		JDBCIdentity i = new JDBCIdentity(resultSet.getString(configuration.getIdentityGuidColumn()),
				resultSet.getString(configuration.getIdentityPrincipalNameColumn()));

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityEmailColumn())) {
			i.setAddress(Media.email, resultSet.getString(configuration.getIdentityEmailColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityMobileColumn())) {
			i.setAddress(Media.mobile, resultSet.getString(configuration.getIdentityMobileColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityFullnameColumn())) {
			i.setFullName(resultSet.getString(configuration.getIdentityFullnameColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityOtherNameColumn())) {
			i.setOtherName(resultSet.getString(configuration.getIdentityOtherNameColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityLastSignOnColumn())) {
			int colidx = getColumnIndex(configuration.getIdentityLastSignOnColumn(), resultSet);
			if (colidx != -1) {
				Date d = getAsDate(resultSet, colidx);
				if (d != null)
					i.setLastSignOnDate(d);
			}
		}

		i.setRoles(getGrantedRoles(i));

		AccountStatus status = new AccountStatus();
		if (!StringUtil.isNullOrEmpty(configuration.getIdentityEnabledColumn())) {
			if (Objects.equals(String.valueOf(resultSet.getObject(configuration.getIdentityEnabledColumn())),
					configuration.getIdentityEnabledValue()))
				status.setDisabled(false);
			else if (Objects.equals(String.valueOf(resultSet.getObject(configuration.getIdentityEnabledColumn())),
					configuration.getIdentityDisabledValue()))
				status.setDisabled(true);
		}
		if (!StringUtil.isNullOrEmpty(configuration.getIdentityLockedColumn())) {
			if (Objects.equals(String.valueOf(resultSet.getObject(configuration.getIdentityLockedColumn())),
					configuration.getIdentityLockedValue()))
				// TODO get the lock date if possible?
				status.setLocked(new Date(0));
			else if (Objects.equals(String.valueOf(resultSet.getObject(configuration.getIdentityLockedColumn())),
					configuration.getIdentityUnlockedValue()))
				status.setLocked(null);
		}
		status.calculateType();
		i.setAccountStatus(status);

		// Password status is currently unsupported
		PasswordStatus pwdStatus = new PasswordStatus();
		if (!StringUtil.isNullOrEmpty(configuration.getIdentityForcePasswordChangeColumn())) {
			if (Objects.equals(
					String.valueOf(resultSet.getObject(configuration.getIdentityForcePasswordChangeColumn())),
					configuration.getIdentityForcePasswordChangeValue()))
				pwdStatus.setNeedChange(true);
			else if (Objects.equals(
					String.valueOf(resultSet.getObject(configuration.getIdentityForcePasswordChangeColumn())),
					configuration.getIdentityNoForcePasswordChangeValue()))
				pwdStatus.setNeedChange(false);
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityLastPasswordChangeColumn())) {
			int colidx = getColumnIndex(configuration.getIdentityLastPasswordChangeColumn(), resultSet);
			if (colidx != -1) {
				Date d = getAsDate(resultSet, colidx);
				if (d != null)
					pwdStatus.setLastChange(d);
			}
		}
		pwdStatus.calculateType();
		i.setPasswordStatus(pwdStatus);

		// This should always be false
		i.setSystem(false);

		return i;
	}

	private int getColumnIndex(String name, ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		for (int i = 0; i < metaData.getColumnCount(); i++) {
			if (metaData.getColumnName(i + 1).equals(name))
				return i;
		}
		return -1;
	}

	protected Role createRole(ResultSet resultSet) throws SQLException {

		JDBCRole r = new JDBCRole(resultSet.getString(configuration.getRoleGuidColumn()),
				resultSet.getString(configuration.getRolePrincipalNameColumn()));

		// This should always be false
		r.setSystem(false);
		return r;
	}

	protected void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	protected void closeResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
			}
		}
	}

	@Override
	public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
		List<Role> roles = new ArrayList<Role>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery(configuration.getSelectRolesSQL());

			while (resultSet.next()) {
				roles.add(createRole(resultSet));
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return ResultIterator.createDefault(roles.iterator(), opContext.getTag());
	}

	public boolean isOpen() {
		try {
			return connect != null && !connect.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	public void onClose() {
		if (isOpen()) {
			try {
				connect.close();
			} catch (SQLException e) {
			} finally {
				connect = null;
				configuration = null;
			}
		}
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public Role createRole(Role role) throws ConnectorException {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getCreateRoleSQL(role);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
		return getRoleByName(role.getPrincipalName());
	}

	public void updateRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException();
	}

	public void deleteRole(String principleName) throws ConnectorException {
		String sql = configuration.getDeleteRoleSQL(principleName);
		if (sql.equals("")) {
			super.deleteRole(principleName);
		} else {
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				statement = connect.createStatement();
				statement.executeUpdate(sql);
			} catch (SQLException e) {
				throw new ConnectorException(e);
			} finally {
				closeStatement(statement);
				closeResultSet(resultSet);
			}
		}
	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) throws ConnectorException {

		configuration = (JDBCConfiguration) parameters;

		try {

			// This will load the MySQL driver, each DB has its own driver
			Class.forName(configuration.getDriverClassName());
			// Setup the connection with the DB
			String username = configuration.getJDBCUsername();
			if (username != null) {
				char[] password = configuration.getJDBCPassword();
				connect = DriverManager.getConnection(configuration.generateJDBCUrl(), username,
						password == null ? null : new String(password));
			} else
				connect = DriverManager.getConnection(configuration.generateJDBCUrl());

		} catch (Exception e) {
			log.error("Failed to open JDBC connection " + configuration.generateJDBCUrl(), e);
			close();
		}

	}

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {

		// Encode the password, if its 'plain' then the database will encode it
		// most likely using PASSWORD() function or similar.
		String encodedPassword = new String(encoderManager.encode(password, configuration.getIdentityPasswordEncoding(),
				configuration.getCharset(), null, null));

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getSelectPasswordSQL(identity, encodedPassword, new String(password));
			if (sql.length() > 0) {
				resultSet = statement.executeQuery(sql);
				return resultSet.next();
			} else {
				sql = configuration.getSelectIdentitySQL(identity.getPrincipalName());
				resultSet = statement.executeQuery(sql);
				if (resultSet.next()) {
					String val = resultSet.getString(configuration.getIdentityPasswordColumn());
					boolean ok = encoderManager.getEncoderById(configuration.getIdentityPasswordEncoding())
							.match(val.getBytes("UTF-8"), new String(password).getBytes("UTF-8"), null, "UTF-8");
					if (ok) {
						Identity id = createIdentity(resultSet);
						if (id.getPasswordStatus().getType() == PasswordStatusType.changeRequired) {
							throw new PasswordChangeRequiredException();
						}
					}

					return ok;
				}
				return false;
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} catch (UnsupportedEncodingException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		String encodedPassword = new String(encoderManager.encode(password, configuration.getIdentityPasswordEncoding(),
				configuration.getCharset(), null, null));

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getUpdatePasswordSQL(identity, encodedPassword, new String(password),
					forcePasswordChangeAtLogon, type);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

	}

	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		String encodedPassword = new String(encoderManager.encode(password, configuration.getIdentityPasswordEncoding(),
				configuration.getCharset(), null, null));

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			String sql = configuration.getCreateSQL(identity, encodedPassword, new String(password));
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
		return getIdentityByName(identity.getPrincipalName());
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connect.setAutoCommit(false);
			Identity existingIdentity = getIdentityByName(identity.getPrincipalName());
			statement = connect.createStatement();
			String sql = configuration.getUpdateSQL(identity);
			statement.executeUpdate(sql);
			updateIdentityRoles(existingIdentity, identity);
			connect.commit();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e) {
			}
		}
	}

	protected void updateIdentityRoles(Identity oldIdentity, Identity newIdentity) throws SQLException {
		Map<String, Role> oldRoles = getRoleNames(oldIdentity);
		Map<String, Role> newRoles = getRoleNames(newIdentity);

		/* Revoke any roles that are no longer valid */
		for (Map.Entry<String, Role> n : oldRoles.entrySet()) {
			if (!newRoles.containsKey(n.getKey())) {
				Statement statement = connect.createStatement();
				try {
					String sql = configuration.getRevokeFromRoleSQL(oldIdentity, n.getValue());
					statement.executeUpdate(sql);
				} finally {
					statement.close();
				}
			}
		}
		
		/* Add any roles that are now granted */
		for (Map.Entry<String, Role> n : newRoles.entrySet()) {
			if (!oldRoles.containsKey(n.getKey())) {
				Statement statement = connect.createStatement();
				try {
					String sql = configuration.getGrantToRoleSQL(newIdentity, n.getValue());
					statement.executeUpdate(sql);
				} finally {
					statement.close();
				}
			}
		}
	}

	protected Map<String, Role> getRoleNames(Identity identity) {
		Role[] r = identity.getRoles();
		Map<String, Role> l = new HashMap<String, Role>();
		if (r != null)
			for (Role a : r)
				l.put(a.getPrincipalName(), a);
		return l;
	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		String sql = configuration.getDeleteSQL(principalName);
		if (sql.equals("")) {
			super.deleteIdentity(principalName);
		} else {
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				statement = connect.createStatement();
				statement.executeUpdate(sql);
			} catch (SQLException e) {
				throw new ConnectorException(e);
			} finally {
				closeStatement(statement);
				closeResultSet(resultSet);
			}
		}

	}

	@Override
	public void lockIdentity(Identity identity) throws ConnectorException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.prepareStatement(
					configuration.getSql(String.format("UPDATE ${identityTable} SET ${identityTableLocked} = ?")));
			statement.setObject(1, configuration.getIdentityLockedValue());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

	}

	@Override
	public void disableIdentity(Identity identity) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.prepareStatement(
					configuration.getSql(String.format("UPDATE ${identityTable} SET ${identityTableEnabled} = ?")));
			statement.setObject(1, configuration.getIdentityDisabledValue());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

	}

	@Override
	public void enableIdentity(Identity identity) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.prepareStatement(
					configuration.getSql(String.format("UPDATE ${identityTable} SET ${identityTableEnabled} = ?")));
			statement.setObject(1, configuration.getIdentityEnabledValue());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = configuration.getSql(String.format("UPDATE ${identityTable} SET ${identityTableLocked} = ?"));
			statement = connect.prepareStatement(sql);
			statement.setObject(1, configuration.getIdentityUnlockedValue());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

	}

	/**
	 * Checks for failed sql command in a batch transaction. If any code matches
	 * {@link Statement.EXECUTE_FAILED} the transaction is declared as failed
	 * and ConnectorException is thrown.
	 * 
	 * @param codes
	 */
	protected void checkBatchCommit(int[] codes) {
		for (int i = 0; i < codes.length; i++) {
			if (codes[i] == Statement.EXECUTE_FAILED) {
				throw new ConnectorException(
						String.format("Batch commit failed with code %d at index %d", Statement.EXECUTE_FAILED, i));
			}
		}
	}

	/**
	 * Sets the auto commit flag to true on a JDBC Connection.
	 * 
	 * @param connection
	 */
	protected void autoCommitTrue(Connection connection) {
		try {
			connect.setAutoCommit(true);
		} catch (SQLException e) {
			throw new ConnectorException("Problem in setting auto commit to true.", e);
		}
	}

	/**
	 * Marks a transaction currently held by connection for rollback.
	 * 
	 * @param connection
	 */
	protected void rollback(Connection connection) {
		try {
			connect.rollback();
		} catch (SQLException e) {
			throw new ConnectorException("Problem in rollback.", e);
		}
	}

	/**
	 * Helper function which executes a sql query with arguments passed and
	 * process the result set as per the logic in JDBCResultsetBlock.
	 * 
	 * @param sql
	 *            query to be processed
	 * @param params
	 *            parameters if any to be passed on to sql query
	 * @param block
	 *            custom logic to be executed on result set producing a result
	 *            of type T
	 * @return object instance as per the logic in block
	 */
	protected <T> T jdbcAction(String sql, Object[] params, JDBCResultsetBlock<T> block) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.prepareStatement(sql);

			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}

			resultSet = statement.executeQuery();

			return block.apply(resultSet);
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeResultSet(resultSet);
			closeStatement(statement);
		}
	}

	/**
	 * Update helper which executes a SQL DML query.
	 * 
	 * @param sql
	 *            DML query to be executed
	 * @param params
	 *            parameters any to be passed to DML query.
	 * 
	 */
	protected void updateHelper(String sql, Object... params) {
		PreparedStatement statement = null;
		try {
			statement = connect.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			statement.executeUpdate();

		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
		}
	}
	/**
	 * Helper method which performs the query in a transaction.  
	 * @param sql
	 * @param block
	 */
	protected void inTransaction(JDBCTransaction tx) {
		try {
			connect.setAutoCommit(false);
			tx.apply(connect);
			connect.commit();
		} catch (SQLException e) {
			rollback(connect);
			throw new ConnectorException(e);
		} finally {
			autoCommitTrue(connect);
		}
	}
	/**
	 * Helper method which performs the query in a transaction and return a result.  
	 * @param sql
	 * @param block
	 */
	protected <T> T jdbcAction(JDBCAction<T> tx) {
		try {
			return tx.apply(connect);
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
		}
	}

	/**
	 * Helper method which performs the query in a transaction. <br>
	 * The method handles both {@link PreparedStatement} and {@link Statement}.
	 * If a SQL query is passed PreparedStatement instance is created else
	 * Statement. The logic to act on Statement is passed in JDBCBlock
	 * 
	 * @param sql
	 * @param block
	 */
	protected void inTransaction(String sql, JDBCBlock block) {
		Statement statement = null;
		try {
			connect.setAutoCommit(false);
			if (!StringUtil.isNullOrEmpty(sql)) {
				statement = connect.prepareStatement(sql);
			} else {
				statement = connect.createStatement();
			}

			block.apply(statement);

			int[] codes = statement.executeBatch();

			checkBatchCommit(codes);

			connect.commit();
		} catch (SQLException e) {
			rollback(connect);
			throw new ConnectorException(e);
		} finally {
			autoCommitTrue(connect);
			closeStatement(statement);
		}
	}

	/**
	 * Helper method to perform JDBC transaction. The logic passed inside
	 * JDBCBlock is executed in a transaction.
	 * 
	 * <br />
	 * <b>Note : </b> Since no SQL query is passed, the logic will use
	 * {@link Statement}
	 * 
	 * @param block
	 */
	protected void inTransaction(JDBCBlock block) {
		inTransaction(null, block);
	}

	/**
	 * Provides a hook method apply which will act on JDBC statement.
	 * 
	 * @author gaurav
	 *
	 */
	public interface JDBCBlock {
		public void apply(Statement statement) throws SQLException;
	}

	/**
	 * Provides a hook method apply which will act on JDBC connection.
	 * 
	 * @author gaurav
	 *
	 */
	public interface JDBCTransaction {
		public void apply(Connection connection) throws SQLException;
	}

	/**
	 * Provides a hook method apply which will act on JDBC connection
	 * and provide a result
	 * 
	 * @author gaurav
	 *
	 */
	public interface JDBCAction<T> {
		public T apply(Connection connection) throws SQLException;
	}

	/**
	 * Provides a hook method apply which will act on JDBC result set and
	 * produce an instance of type T.
	 * 
	 * @author gaurav
	 *
	 * @param <T>
	 */
	public interface JDBCResultsetBlock<T> {
		public T apply(ResultSet resultSet) throws SQLException;
	}

	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equals(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}
}
