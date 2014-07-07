package com.identity4j.connector.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.Media;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.StringUtil;
import com.identity4j.util.crypt.EncoderManager;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public abstract class JDBCConnector extends AbstractConnector {

	protected final static EncoderManager encoderManager = DefaultEncoderManager
			.getInstance();

	protected Connection connect = null;
	protected JDBCConfiguration configuration = null;

	static Log log = LogFactory.getLog(JDBCConnector.class);

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays.asList(new ConnectorCapability[] { 
			ConnectorCapability.passwordChange,
			ConnectorCapability.passwordSet,
			ConnectorCapability.createUser,
			ConnectorCapability.deleteUser,
			ConnectorCapability.updateUser,
			ConnectorCapability.hasFullName,
			ConnectorCapability.hasEmail,
			ConnectorCapability.roles,
			ConnectorCapability.createRole,
			ConnectorCapability.deleteRole,
			ConnectorCapability.updateRole,
			ConnectorCapability.authentication,
			ConnectorCapability.identities
	}));
	
	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	public Iterator<Identity> allIdentities() throws ConnectorException {

		List<Identity> identities = new ArrayList<Identity>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery(configuration
					.getSelectIdentitiesSQL());

			while (resultSet.next()) {
				identities.add(createIdentity(resultSet));
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return identities.iterator();
	}

	protected List<Role> selectIdentityRoles(Identity identity) {

		List<Role> roles = new ArrayList<Role>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery(configuration
					.getSelectIdentitiesRolesSQL(identity));

			while (resultSet.next()) {
				roles.add(createRole(resultSet));
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return roles;
	}

	protected Identity createIdentity(ResultSet resultSet) throws SQLException {

		JDBCIdentity i = new JDBCIdentity(resultSet.getString(configuration
				.getIdentityGuidColumn()), resultSet.getString(configuration
				.getIdentityPrincipalNameColumn()));

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityEmailColumn())) {
			i.setAddress(Media.email,
					resultSet.getString(configuration.getIdentityEmailColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration.getIdentityMobileColumn())) {
			i.setAddress(Media.mobile, resultSet.getString(configuration
					.getIdentityMobileColumn()));
		}

		if (!StringUtil
				.isNullOrEmpty(configuration.getIdentityFullnameColumn())) {
			i.setFullName(resultSet.getString(configuration
					.getIdentityFullnameColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration
				.getIdentityOtherNameColumn())) {
			i.setOtherName(resultSet.getString(configuration
					.getIdentityOtherNameColumn()));
		}

		if (!StringUtil.isNullOrEmpty(configuration
				.getIdentityLastSignOnColumn())) {
			i.setLastSignOnDate(resultSet.getDate(configuration
					.getIdentityLastSignOnColumn()));
		}

		i.setRoles(selectIdentityRoles(i));

		// Account status is currently unsupported
		AccountStatus status = new AccountStatus();
		i.setAccountStatus(status);

		// Password status is currently unsupported
		PasswordStatus pwdStatus = new PasswordStatus();
		i.setPasswordStatus(pwdStatus);

		// This should always be false
		i.setSystem(false);

		return i;
	}

	protected Role createRole(ResultSet resultSet) throws SQLException {

		JDBCRole r = new JDBCRole(resultSet.getString(configuration
				.getRoleGuidColumn()), resultSet.getString(configuration
				.getRolePrincipalNameColumn()));

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

	public Iterator<Role> allRoles() throws ConnectorException {
		List<Role> roles = new ArrayList<Role>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery(configuration
					.getSelectRolesSQL());

			while (resultSet.next()) {
				roles.add(createRole(resultSet));
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return roles.iterator();
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
		// TODO Auto-generated method stub
		return null;
	}

	public void updateRole(Role role) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	public void deleteRole(String principleName) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters)
			throws ConnectorException {

		configuration = (JDBCConfiguration) parameters;

		try {

			// This will load the MySQL driver, each DB has its own driver
			Class.forName(configuration.getDriverClassName());
			// Setup the connection with the DB
			connect = DriverManager.getConnection(configuration
					.generateJDBCUrl());

		} catch (Exception e) {
			log.error(
					"Failed to open JDBC connection "
							+ configuration.generateJDBCUrl(), e);
			close();
		}

	}

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password)
			throws ConnectorException {

		// Encode the password, if its 'plain' then the database will encode it
		// most likely using PASSWORD() function or similar.
		String encodedPassword = new String(encoderManager.encode(password,
				configuration.getIdentityPasswordEncoding(),
				configuration.getCharset(), null, null));

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery(configuration
					.getSelectPasswordSQL(identity, encodedPassword));

			return resultSet.next();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
	}

	@Override
	protected void setPassword(Identity identity, char[] password,
			boolean forcePasswordChangeAtLogon) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public Identity createIdentity(Identity identity, char[] password)
			throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockIdentity(Identity identity) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Checks for failed sql command in a batch transaction. 
	 * If any code matches {@link Statement.EXECUTE_FAILED} the transaction is declared as failed
	 * and ConnectorException is thrown.
	 * @param codes
	 */
	protected void checkBatchCommit(int[] codes){
		for (int i = 0; i < codes.length; i++) {
			if(codes[i] == Statement.EXECUTE_FAILED){
				throw new ConnectorException(String.format("Batch commit failed with code %d at index %d",Statement.EXECUTE_FAILED,i));
			}
		}
	}
	
	/**
	 * Sets the auto commit flag to true on a JDBC Connection.
	 * 
	 * @param connection
	 */
	protected void autoCommitTrue(Connection connection){
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
	protected void rollback(Connection connection){
		try {
			connect.rollback();
		} catch (SQLException e) {
			throw new ConnectorException("Problem in rollback.", e);
		}
	}
	
	/**
	 * Helper function which executes a sql query with arguments passed and process the result set as per the logic
	 * in JDBCResultsetBlock.
	 * 
	 * @param sql query to be processed
	 * @param params parameters if any to be passed on to sql query
	 * @param block custom logic to be executed on result set producing a result of type T
	 * @return object instance as per the logic in block
	 */
	protected <T> T jdbcAction(String sql,Object[] params,JDBCResultsetBlock<T> block){
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
	 * @param sql DML query to be executed
	 * @param params parameters any to be passed to DML query.
	 * 
	 */
	protected void updateHelper(String sql,Object...params){
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
	 * <br>
	 * The method handles both {@link PreparedStatement} and {@link Statement}.
	 * If a SQL query is passed PreparedStatement instance is created else Statement.
	 * The logic to act on Statement is passed in JDBCBlock
	 * 
	 * @param sql
	 * @param block
	 */
	protected void inTransaction(String sql,JDBCBlock block){
		Statement statement = null;
		try{
			connect.setAutoCommit(false);
			if(!StringUtil.isNullOrEmpty(sql)){
				statement = connect.prepareStatement(sql);
			}else{
				statement = connect.createStatement();
			}
			
			block.apply(statement);
			
			int[] codes = statement.executeBatch();
			
			checkBatchCommit(codes);
			
			connect.commit();
		}catch (SQLException e) {
			rollback(connect);
			throw new ConnectorException(e);
		} finally {
			autoCommitTrue(connect);
			closeStatement(statement);
		}
	}
	
	/**
	 * Helper method to perform JDBC transaction.
	 * The logic passed inside JDBCBlock is executed in a transaction.
	 * 
	 * <br />
	 * <b>Note : </b> Since no SQL query is passed, the logic will use {@link Statement}
	 * 
	 * @param block
	 */
	protected void inTransaction(JDBCBlock block) {
		inTransaction(null,block);
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
	 * Provides a hook method apply which will act on JDBC result set and produce an instance of type T.
	 * 
	 * @author gaurav
	 *
	 * @param <T>
	 */
	public interface JDBCResultsetBlock<T> {
		public T apply(ResultSet resultSet) throws SQLException;
	}
}
