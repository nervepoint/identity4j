package com.identity4j.connector.google;

import static com.identity4j.util.StringUtil.isNullOrEmpty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.PemReader.Section;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.WebAuthenticationAPI;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.StringUtil;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Google Apps connector makes use of admin SDK to perform admin operations.
 * Connector enables CRUD operations on Users and can map them to Groups. Users
 * and Groups are referred as GoogleIdentity and Role respectively in identity4j
 * domain.
 * 
 * <p>
 * To map properties of User not supported by GoogleIdentity we can make use
 * attributes map.
 * 
 * <pre>
 * E.g.role.setAttribute("email", group.getEmail());
 * </pre>
 * 
 * Here we are using attribute map with key "email" to store emai id which is
 * not a property in role.
 * </p>
 * 
 * <p>
 * The API can be referred from
 * <a href="https://developers.google.com/admin-sdk/directory/">Directory
 * API</a>
 * </p>
 * 
 * @author gaurav
 *
 */
public class GoogleConnector extends AbstractConnector {

	private static final int RESOURCE_CONFLICT = 409;
	private static final int RESOURCE_NOT_FOUND = 404;

	private Directory directory = null;
	private GoogleConfiguration configuration = null;

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static final Log log = LogFactory.getLog(GoogleConnector.class);

	private long lastRequestTime = 0L;

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(
			Arrays.asList(new ConnectorCapability[] { ConnectorCapability.passwordChange,
					ConnectorCapability.passwordSet, ConnectorCapability.createUser, ConnectorCapability.deleteUser,
					ConnectorCapability.updateUser, ConnectorCapability.hasFullName, ConnectorCapability.hasEmail,
					ConnectorCapability.roles, ConnectorCapability.createRole, ConnectorCapability.deleteRole,
					ConnectorCapability.updateRole, ConnectorCapability.webAuthentication,
					ConnectorCapability.identities, ConnectorCapability.accountDisable }));

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		return null;
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	protected void checkRequestInterval() {
		if (lastRequestTime > 0) {
			long sinceLastRequest = System.currentTimeMillis() - lastRequestTime;
			if (sinceLastRequest > 0 && sinceLastRequest < configuration.getRequestInterval()) {
				try {
					Thread.sleep(configuration.getRequestInterval() - sinceLastRequest);
				} catch (InterruptedException e) {
				}
			}
		}
		lastRequestTime = System.currentTimeMillis();
	}

	/**
	 * <p>
	 * Creates identity with specified roles and password provided. Role in
	 * google admin sdk is known as Group and is identified by email id,
	 * represented as role.getAttribute("email") for a Role instance. Role guid
	 * is auto generated number from Google.
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a> and <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">
	 * User</a>
	 * </p>
	 * 
	 * <p>
	 * The extra attributes supported by google e.g. etag, customerId are
	 * populated in identites's attributes map.
	 * 
	 * <pre>
	 * identity.getAttribute("customerId")
	 * </pre>
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException
	 *             if identity with same email id/principal already present in
	 *             google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 * 
	 * @return Identity instance with values specified for creation.
	 */
	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Creating google identity " + identity.getPrincipalName());
		}
		try {
			User user = GoogleModelConvertor.googleIdentityToUser(identity);
			user.setPassword(new String(password));
			checkRequestInterval();
			GoogleIdentity googleUserToGoogleIdentity = GoogleModelConvertor
					.googleUserToGoogleIdentity(directory.users().insert(user).execute());
			Role[] roles = identity.getRoles();
			for (Role role : roles) {
				addRoleToUser(role.getAttribute("email"), identity.getPrincipalName());
			}
			return googleUserToGoogleIdentity;
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in create identity " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_CONFLICT)
				throw new PrincipalAlreadyExistsException(identity.getPrincipalName() + " not found.", e,
						PrincipalType.user);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in create identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Deletes an identity in google data store. </>
	 * 
	 * <p>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a> and <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">
	 * User</a>
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Deleting google identity " + principalName);
		}
		try {
			checkRequestInterval();
			directory.users().delete(principalName).execute();
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in delete identity " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(principalName + " not found.", e, PrincipalType.user);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in delete identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Updates an identity in google data store. To update extra attributes
	 * supported by google, use attributes map.
	 * 
	 * <pre>
	 * e.g.identity.setAttribute("orgUnitPath", "/corp/engineering")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a> and <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">
	 * User</a>
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Updating google identity " + identity.getPrincipalName());
		}
		try {
			String userKey = identity.getPrincipalName();
			checkRequestInterval();
			directory.users().update(userKey, GoogleModelConvertor.googleIdentityToUser(identity)).execute();
			if (configuration.getFetchRoles()) {
				adjustAdditionRemovalOfRoleOnIdentityUpdate(identity);
			}
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in update identity " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(identity.getPrincipalName() + " not found.", e,
						PrincipalType.user);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in update identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Finds an identity by principal/email id supplied. <br />
	 * The extra attributes supported by google e.g. etag, customerId are
	 * populated in identites's attributes map.
	 * 
	 * <pre>
	 * identity.getAttribute("customerId")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a> and <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">
	 * User</a>
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 * 
	 * @return Identity instance found by the specified email id/principal name.
	 */
	@Override
	public Identity getIdentityByName(String name) throws PrincipalNotFoundException, ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Get google identity " + name);
		}
		try {
			checkRequestInterval();
			User user = directory.users().get(name).execute();
			GoogleIdentity identity = GoogleModelConvertor.googleUserToGoogleIdentity(user);
			if (configuration.getFetchRoles()) {
				List<Role> roles = findAllRolesForAUser(user.getPrimaryEmail());
				identity.setRoles(roles);
			}
			return identity;
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in get identity by name " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(name + " not found.", e, PrincipalType.user);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in get identity by name " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Finds all identities present in google data store. <br/>
	 * The extra attributes supported by google e.g. etag, customerId are
	 * populated in identites's attributes map.
	 * 
	 * <pre>
	 * identity.getAttribute("customerId")
	 * </pre>
	 * </p>
	 * <p>
	 * For api to work in configuration file we need to specify customer domain
	 * or customer id. If both are specified customer domain is considered.
	 * <p/>
	 * 
	 * <p>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a> and <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">
	 * User</a>
	 * </p>
	 * 
	 * 
	 * @throws IllegalStateException
	 *             if customer domain or customer id not specified
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 * 
	 * @return iterator with all identities.
	 */
	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Listing all google identities");
		}
		return new Iterator<Identity>() {

			Iterator<Identity> currentIterator;
			String pageToken = null;
			boolean expectMoreResults = true;

			private void getMoreResults() {
				try {
					if (isNullOrEmpty(configuration.getGoogleCustomerDomain())
							&& isNullOrEmpty(configuration.getGoogleCustomerId())) {
						throw new IllegalStateException("Customer Domain or Customer Id not set.");
					}

					Collection<String> includes = configuration.getIncludes();
					Collection<String> excludes = configuration.getExcludes();

					com.google.api.services.admin.directory.Directory.Users.List list = directory.users().list();

					list.setMaxResults(500);

					if (pageToken != null) {
						list.setPageToken(pageToken);
					}

					if (configuration.getGoogleCustomerDomain() != null) {
						list.setDomain(configuration.getGoogleCustomerDomain());
					} else {
						list.setCustomer(configuration.getGoogleCustomerId());
					}

					checkRequestInterval();
					Users users = list.execute();
					pageToken = users.getNextPageToken();

					if (pageToken == null) {
						expectMoreResults = false;
					}
					List<Identity> googleIdentities = new ArrayList<Identity>();

					for (User user : users.getUsers()) {
						String orgUnit = StringUtil.nonNull(user.getOrgUnitPath());
						if ((includes.isEmpty() || includes.contains(orgUnit))
								&& (excludes.isEmpty() || !excludes.contains(orgUnit))) {

							GoogleIdentity identity = GoogleModelConvertor.googleUserToGoogleIdentity(user);
							if (configuration.getFetchRoles()) {
								List<Role> roles = findAllRolesForAUser(user.getPrimaryEmail());
								identity.setRoles(roles);
							}
							googleIdentities.add(identity);
						}

					}

					currentIterator = googleIdentities.iterator();
				} catch (IOException e) {
					if (e instanceof TokenResponseException) {
						TokenResponseException tokenResponseException = (TokenResponseException) e;
						if (tokenResponseException.getDetails() != null
								&& "invalid_grant".equals(tokenResponseException.getDetails().getError())) {
							throw new ConnectorException(
									"Incorrect credentials. Check your configured service email addresses and the private key.");
						}
						;
					} else if (e instanceof GoogleJsonResponseException) {
						GoogleJsonResponseException responseExeption = (GoogleJsonResponseException) e;
						if (responseExeption.getDetails().getCode() == 404
								&& responseExeption.getDetails().getMessage().contains("Resource Not Found: domain")) {
							throw new ConnectorException("Incorrect customer domain.");
						}
					}
					log.error("Problem in all identities " + e.getMessage(), e);
					throw new ConnectorException(e.getMessage(), e);
				}
			}

			@Override
			public boolean hasNext() {
				if (currentIterator == null) {
					getMoreResults();
				}
				if (currentIterator.hasNext() || expectMoreResults) {
					return true;
				}
				return false;
			}

			@Override
			public Identity next() {
				if (currentIterator.hasNext()) {
					return currentIterator.next();
				} else if (expectMoreResults) {
					getMoreResults();
					return currentIterator.next();
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {

			}
		};

	}

	@Override
	public WebAuthenticationAPI startAuthentication() throws ConnectorException {
		return new GoogleOAuth(configuration);
	}

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		throw new UnsupportedOperationException(
				"Standard credential validation is not supported, web authentication must be used.");
	}

	/**
	 * Disables/Suspends an account in google data store.
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void disableIdentity(Identity identity) {
		if (log.isWarnEnabled()) {
			log.warn("Disabling google identity " + identity.getPrincipalName());
		}
		identitySuspensionHelper(identity, true);
	}

	/**
	 * Enables an account in google data store.
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void enableIdentity(Identity identity) {
		if (log.isWarnEnabled()) {
			log.warn("Enabling google identity " + identity.getPrincipalName());
		}
		identitySuspensionHelper(identity, false);
	}

	/**
	 * Changes the password of the identity specified by email id/principal.
	 * Also provides provision to force change password in next logon attempt.
	 * 
	 * 
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Setting password for google identity " + identity.getPrincipalName());
		}
		try {
			User user = new User();
			user.setPassword(new String(password));
			user.setPrimaryEmail(identity.getPrincipalName());
			user.setChangePasswordAtNextLogin(forcePasswordChangeAtLogon);
			checkRequestInterval();
			directory.users().update(identity.getPrincipalName(), user).execute();
		} catch (IOException e) {
			log.error("Problem in set password " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Retrieves all the roles (groups) present in the google data store. <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id. <br/>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a>.
	 * </p>
	 * <p>
	 * For api to work in configuration file we need to specify customer domain
	 * or customer id. If both are specified customer domain is considered.
	 * <p/>
	 * 
	 * @throws IllegalStateException
	 *             if customer domain or customer id not specified
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public Iterator<Role> allRoles() throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Listing all google groups");
		}
		return new Iterator<Role>() {

			Iterator<Role> currentIterator;
			String pageToken = null;
			boolean expectMoreResults = true;

			private void getMoreResults() {
				try {
					if (isNullOrEmpty(configuration.getGoogleCustomerDomain())
							&& isNullOrEmpty(configuration.getGoogleCustomerId())) {
						throw new IllegalStateException("Customer Domain or Customer Id not set.");
					}

					com.google.api.services.admin.directory.Directory.Groups.List list = directory.groups().list();

					list.setMaxResults(100);

					if (pageToken != null) {
						list.setPageToken(pageToken);
					}

					if (configuration.getGoogleCustomerDomain() != null) {
						list.setDomain(configuration.getGoogleCustomerDomain());
					} else {
						list.setCustomer(configuration.getGoogleCustomerId());
					}

					checkRequestInterval();
					Groups groups = list.execute();
					pageToken = groups.getNextPageToken();
					if (pageToken == null) {
						expectMoreResults = false;
					}

					List<Role> roles = new ArrayList<Role>();

					for (Group group : groups.getGroups()) {
						roles.add(GoogleModelConvertor.groupToRole(group));
					}

					currentIterator = roles.iterator();
				} catch (IOException e) {
					log.error("Problem in all roles " + e.getMessage(), e);
					throw new ConnectorException(e.getMessage(), e);
				}
			}

			@Override
			public boolean hasNext() {
				if (currentIterator == null) {
					getMoreResults();
				}
				if (currentIterator.hasNext() || expectMoreResults) {
					return true;
				}
				return false;
			}

			@Override
			public Role next() {
				if (currentIterator.hasNext()) {
					return currentIterator.next();
				} else if (expectMoreResults) {
					getMoreResults();
					return currentIterator.next();
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {

			}
		};

	}

	/**
	 * <p>
	 * Creates a role in the google data store. <br/>
	 * Role email is specified by role.setAttribute("email",.......) <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id and guid is auto generated. <br/>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a>.
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException
	 *             if role with same email id/principal already present in
	 *             google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public Role createRole(Role role) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Creating google group " + role.getPrincipalName());
		}
		try {
			Group group = GoogleModelConvertor.roleToGroup(role);
			checkRequestInterval();
			return GoogleModelConvertor.groupToRole(directory.groups().insert(group).execute());
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in create role " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_CONFLICT)
				throw new PrincipalAlreadyExistsException(role.getPrincipalName() + " already exists.", e,
						PrincipalType.role);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in create role " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Updates a role in the google data store with specified changes. <br/>
	 * 
	 * <pre>
	 * Role email is specified by role.setAttribute("email",.......)
	 * </pre>
	 * 
	 * <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id and guid is auto generated. <br/>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if role specified by email id/principal name not present in
	 *             google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void updateRole(Role role) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Updating google group " + role.getPrincipalName());
		}
		try {
			Group group = GoogleModelConvertor.roleToGroup(role);
			checkRequestInterval();
			directory.groups().update(role.getAttribute("email"), group);
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in update role " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(role.getAttribute("email") + " not found.", e, PrincipalType.role);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in update role " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Deletes a role in the google data store with specified principal name.
	 * <br/>
	 * 
	 * <pre>
	 * Role email is specified by role.setAttribute("email",.......)
	 * </pre>
	 * 
	 * <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id and guid is auto generated. <br/>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if role specified by email id/principal name not present in
	 *             google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Deleting google group " + principleName);
		}
		try {
			checkRequestInterval();
			directory.groups().delete(principleName).execute();
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in delete identity " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(principleName + " not found.", e, PrincipalType.role);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in delete identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	/**
	 * <p>
	 * Finds a role in the google data store with specified principal name.
	 * <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id and guid is auto generated. <br/>
	 * Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">
	 * Groups</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException
	 *             if role specified by email id/principal name not present in
	 *             google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	@Override
	public Role getRoleByName(String name) throws PrincipalNotFoundException, ConnectorException {
		if (log.isWarnEnabled()) {
			log.warn("Getting google group " + name);
		}
		try {
			return GoogleModelConvertor.groupToRole(directory.groups().get(name).execute());
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in get role by name " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(name + " not found.", e, PrincipalType.role);
			throw new ConnectorException(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Problem in get role by name " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Helper method to find all roles associated with a principal. <br/>
	 * <b>Note:</b> Role in google data store is referred as groups and
	 * identified by email id.
	 * 
	 * @param principal
	 * 
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 * 
	 * @return all roles associated with the identity principal
	 */
	private List<Role> findAllRolesForAUser(String principal) {
		if (log.isWarnEnabled()) {
			log.warn("Getting groups for google identity " + principal);
		}
		List<Role> roles = new ArrayList<Role>();
		try {
			checkRequestInterval();
			com.google.api.services.admin.directory.Directory.Groups.List list = directory.groups().list();
			Groups userGroups = list.setUserKey(principal).execute();
			List<Group> groups = userGroups.getGroups();

			// if user is associated with any groups.
			if (groups != null) {
				for (Group group : groups) {
					roles.add(GoogleModelConvertor.groupToRole(group));
				}
			}

		} catch (IOException e) {
			log.error("Problem in find all roles for an identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
		return roles;
	}

	/**
	 * Adds a Role(Group) to an identity. In google data store relation between
	 * User and Group is represented as Member. To add a member to a group, we
	 * create instance of member with principal name i.e. email id. Member role
	 * is MEMBER and type is USER. Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-group-members"/>
	 * Member</a> for more info.
	 * 
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	private void addRoleToUser(String roleName, String principal) {
		if (log.isWarnEnabled()) {
			log.warn("Adding google identity " + principal + " to group " + roleName);
		}
		try {
			Member member = new Member();
			member.setEmail(principal);
			member.setRole("MEMBER");
			member.setType("USER");
			checkRequestInterval();
			directory.members().insert(getRoleByName(roleName).getGuid(), member).execute();
		} catch (IOException e) {
			log.error("Problem in adding role " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Removes a Role(Group) from an identity. In google data store relation
	 * between User and Group is represented as Member. To add a member to a
	 * group, we create instance of member with principal name i.e. email id.
	 * Member role is MEMBER and type is USER. Please refer <a href=
	 * "https://developers.google.com/admin-sdk/directory/v1/guides/manage-group-members"/>
	 * Member</a> for more info.
	 * 
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	private void removeRoleFromUser(String roleName, String email) {
		if (log.isWarnEnabled()) {
			log.warn("Removing google identity " + email + " from group " + roleName);
		}
		try {
			Member member = new Member();
			member.setEmail(email);
			member.setRole("MEMBER");
			member.setType("USER");
			checkRequestInterval();
			directory.members().delete(getRoleByName(roleName).getGuid(), email).execute();
		} catch (IOException e) {
			log.error("Problem in removing role " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Helper utility method to adjust addition and removal of roles from an
	 * identity. It compares the roles currently assigned and new set of roles
	 * sent and finds which are to be added and which are to be removed and
	 * accordingly performs removal or addition action.
	 * 
	 * @param identity
	 * 
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	private void adjustAdditionRemovalOfRoleOnIdentityUpdate(Identity identity) {
		try {
			Set<Role> rolesCurrentlyAssigned = new HashSet<Role>(findAllRolesForAUser(identity.getPrincipalName()));
			Set<Role> rolesToBeAssigned = new HashSet<Role>(Arrays.asList(identity.getRoles()));

			Collection<Role> newRolesToAdd = CollectionUtil.objectsNotPresentInProbeCollection(rolesToBeAssigned,
					rolesCurrentlyAssigned);
			Collection<Role> rolesToRemove = CollectionUtil.objectsNotPresentInProbeCollection(rolesCurrentlyAssigned,
					rolesToBeAssigned);

			for (Role role : newRolesToAdd) {
				addRoleToUser(role.getGuid(), identity.getPrincipalName());
			}

			for (Role role : rolesToRemove) {
				removeRoleFromUser(role.getGuid(), identity.getPrincipalName());
			}
		} catch (Exception e) {
			log.error("Problem in adjusting roles " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Check to see if connector is open
	 */
	@Override
	public boolean isOpen() {
		return directory != null;
	}

	/**
	 * Check to see connector if in read only mode
	 */
	@Override
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * <p>
	 * Creates directory instance for remote method invocations. The directory
	 * instance is created by providing and enabling
	 * <ul>
	 * <li>Giving Consent i.e. permissions it can perform (corresponding
	 * consents should be given in admin console)</li>
	 * <li>Private key of the service id (It is expected in Base64 encoding)
	 * </li>
	 * <li>Enabling Admin SDK in admin console</li>
	 * <li>Enabling API Access</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * For performance reasons we have set all request to be gzipped by setting
	 * http header Accept-Encoding to gzip and user agent containing string
	 * "(gzip)"
	 * </p>
	 * 
	 */
	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) throws ConnectorException {

		if (log.isWarnEnabled()) {
			log.warn("Opening google directory");
		}
		configuration = (GoogleConfiguration) parameters;

		try {
			// consents given to service account id
			Set<String> scopes = new HashSet<String>();
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_ORGUNIT);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
			scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER_ALIAS);

			// credential credential store
			GoogleCredential credential = null;
			String json = configuration.getGoogleServiceAccountJson();
			if (json != null && json.length() > 0) {
				// http://stackoverflow.com/questions/32019322/howto-create-googlecredential-by-using-service-account-json
				// credential = GoogleCredential.fromStream(new
				// ByteArrayInputStream(json.getBytes("UTF-8")),
				// createTransport(), JSON_FACTORY);
				// credential = credential.createScoped(scopes);
 
				/*
				 * Grrr... CANT just use that, because there is no way to
				 * setServiceAccountUser()! Without this, will not get high
				 * enough privileges. All of this code is copied from
				 * GoogleCredential, with the setting of this attribute before
				 * the credential is created.
				 */

				JsonObjectParser parser = new JsonObjectParser(JSON_FACTORY);
				byte[] jsonStr = json.getBytes("UTF-8");
				System.out.println(">> " + new String(jsonStr));
				GenericJson fileContents = parser.parseAndClose(new ByteArrayInputStream(jsonStr),
						Charset.forName("UTF-8"), GenericJson.class);
				String fileType = (String) fileContents.get("type");
				if (fileType == null) {
					throw new IOException("Error reading credentials from stream, 'type' field not specified.");
				}
				if (!"service_account".equals(fileType)) {
					throw new ConnectorException("The JSON data provided is not for the service_account type.");
				}

				String clientId = (String) fileContents.get("client_id");
				String clientEmail = (String) fileContents.get("client_email");
				String privateKeyPem = (String) fileContents.get("private_key");
				String privateKeyId = (String) fileContents.get("private_key_id");
				if (clientId == null || clientEmail == null || privateKeyPem == null || privateKeyId == null) {
					throw new IOException("Error reading service account credential from stream, "
							+ "expecting  'client_id', 'client_email', 'private_key' and 'private_key_id'.");
				}

				PrivateKey privateKey = privateKeyFromPkcs8(privateKeyPem);

				Collection<String> emptyScopes = Collections.emptyList();

				Builder credentialBuilder = new GoogleCredential.Builder().setTransport(createTransport())
						.setJsonFactory(JSON_FACTORY).setServiceAccountId(clientEmail)
						.setServiceAccountScopes(emptyScopes).setServiceAccountPrivateKey(privateKey)
						.setServiceAccountPrivateKeyId(privateKeyId)
						.setServiceAccountUser(configuration.getGoogleUsername());
				String tokenUri = (String) fileContents.get("token_uri");
				if (tokenUri != null) {
					credentialBuilder.setTokenServerEncodedUrl(tokenUri);
				}
				credentialBuilder.setServiceAccountScopes(scopes);
				credential = credentialBuilder.build();

			} else {

				// loading the private key encoded as base 64
				PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(SecurityUtils.getPkcs12KeyStore(),
						new Base64InputStream(
								new ByteArrayInputStream(configuration.getGooglePrivateKeyEncoded().getBytes())),
						configuration.getGooglePrivatePassphrase(), "privatekey",
						configuration.getGooglePrivatePassphrase());
				credential = new GoogleCredential.Builder().setTransport(createTransport()).setJsonFactory(JSON_FACTORY)
						.setServiceAccountId(configuration.getGoogleServiceAccountId()).setServiceAccountScopes(scopes)
						.setServiceAccountPrivateKey(privateKey)
						.setServiceAccountUser(configuration.getGoogleUsername()).build();
			}

			// Adding gzip support for all requests
			HttpTransport transport = createTransport();
			transport.createRequestFactory(new HttpRequestInitializer() {

				@Override
				public void initialize(HttpRequest httpRequest) throws IOException {
					HttpHeaders httpHeaders = new HttpHeaders();
					httpHeaders.setAcceptEncoding("gzip");
					httpHeaders.setUserAgent(
							"Nervepoint Access Manager v1.2 (gzip) " + configuration.getGoogleCustomerDomain());
					httpRequest.setHeaders(httpHeaders);
				}
			});

			// directory instance provides API for remote methods
			directory = new Directory.Builder(createTransport(), JSON_FACTORY, credential)
					.setApplicationName("Identity4J").build();

			log.info("Directory instance created");
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("toDerInputStream rejects tag")) {
				throw new ConnectorException("Invalid private key.");
			}
			log.error("Problem in open connection " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	private static PrivateKey privateKeyFromPkcs8(String privateKeyPem) throws IOException {
		Reader reader = new StringReader(privateKeyPem);
		Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
		if (section == null) {
			throw new IOException("Invalid PKCS8 data.");
		}
		byte[] bytes = section.getBase64DecodedBytes();
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
		Exception unexpectedException = null;
		try {
			KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException exception) {
			unexpectedException = exception;
		} catch (InvalidKeySpecException exception) {
			unexpectedException = exception;
		}
		throw new IOException("Unexpected exception reading PKCS data", unexpectedException);
	}

	public static HttpTransport createTransport() throws GeneralSecurityException, IOException {
		return "true".equals(System.getProperty("identity4j.google.useIdentity4JTransport", "true"))
				? new Identity4JHTTPTransport()
				: ("true".equals(System.getProperty("identity4j.google.useApacheTransport"))
						? new ApacheHttpTransport(ApacheHttpTransport.newDefaultHttpClient())
						: GoogleNetHttpTransport.newTrustedTransport());
	}

	/**
	 * Helper utility method to enable/disable suspension for an identity
	 * 
	 * @param identity
	 * @param suspension
	 *            true to suspend an account, false to enable it
	 * 
	 * @throws PrincipalNotFoundException
	 *             if identity specified by email id/principal name not present
	 *             in google data store.
	 * @throws ConnectorException
	 *             for api, connection related errors.
	 */
	private void identitySuspensionHelper(Identity identity, boolean suspension) {
		try {
			User user = new User();
			user.setSuspended(suspension);
			user.setSuspensionReason("ADMIN");
			checkRequestInterval();
			directory.users().update(identity.getPrincipalName(), user).execute();
			// set the state in passed identity instance
			identity.getAccountStatus().setDisabled(suspension);
		} catch (GoogleJsonResponseException e) {
			log.error("Problem in suspending identity " + e.getMessage(), e);
			if (e.getStatusCode() == RESOURCE_NOT_FOUND)
				throw new PrincipalNotFoundException(identity.getPrincipalName() + " not found.", e);
			throw new ConnectorException(e.getMessage(), e);
		} catch (Exception e) {
			log.error("Problem in suspending identity " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}
}
