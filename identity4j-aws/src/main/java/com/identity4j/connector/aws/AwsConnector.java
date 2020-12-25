package com.identity4j.connector.aws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.aws.command.CommandFactory;
import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestResultIterator;
import com.identity4j.connector.aws.command.group.AttachPolicyToGroupRequestCommand;
import com.identity4j.connector.aws.command.group.CreateGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DeleteGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DetachPolicyFromGroupRequestCommand;
import com.identity4j.connector.aws.command.group.GetGroupRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupsRequestCommand;
import com.identity4j.connector.aws.command.policy.GetPolicyRequestCommand;
import com.identity4j.connector.aws.command.policy.ListPoliciesRequestCommand;
import com.identity4j.connector.aws.command.user.AddUserToGroupRequestCommand;
import com.identity4j.connector.aws.command.user.AttachPolicyToUserRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserRequestCommand;
import com.identity4j.connector.aws.command.user.DeleteUserRequestCommand;
import com.identity4j.connector.aws.command.user.DetachPolicyFromUserRequestCommand;
import com.identity4j.connector.aws.command.user.GetUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListGroupsForUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListUserAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.user.ListUsersRequestCommand;
import com.identity4j.connector.aws.command.user.RemoveUserFromGroupRequestCommand;
import com.identity4j.connector.aws.command.user.RemoveUserPasswordRequestCommand;
import com.identity4j.connector.aws.command.user.UpdateUserPasswordRequestCommand;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.EntityAlreadyExistsException;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.utils.StringUtils;

public class AwsConnector extends AbstractConnector<AwsConfiguration> {
	
	private static final Log log = LogFactory.getLog(AwsConnector.class);

	public static final String AWS_ATTRIBUTE_POLICY_ARNS = "policyArns";

	private IamClient client;

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays
			.asList(new ConnectorCapability[] { ConnectorCapability.passwordSet,
					ConnectorCapability.createUser, ConnectorCapability.updateUser, 
					ConnectorCapability.deleteUser, 
					ConnectorCapability.roles,
					ConnectorCapability.createRole, ConnectorCapability.deleteRole, ConnectorCapability.updateRole,
					ConnectorCapability.identities, ConnectorCapability.identityAttributes,
					ConnectorCapability.roleAttributes }));

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {
		
		log.info("Listing all identities.");

		return new Iterator<Identity>() {

			Iterator<User> iterator = new RequestResultIterator<>(ListUsersRequestCommand.class, client);

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Identity next() {
				
				User user = iterator.next();
				AwsIdentity identity = AwsModelConverter.userToAwsIdentity(user);
				
				Map<String, String> optionsForUserGroups = new HashMap<>();
				optionsForUserGroups.put("userName", identity.getPrincipalName());
				
				Iterator<Group> iteratorGroup = new RequestResultIterator<>(ListGroupsForUserRequestCommand.class, client, optionsForUserGroups);
				while(iteratorGroup.hasNext()) {
					Group group = iteratorGroup.next();
					Role role = AwsModelConverter.groupToRole(group);
					identity.addRole(role);
				}
				
				Map<String, String> options = new HashMap<>();
				options.put("userName", identity.getPrincipalName());
				
				Iterator<AttachedPolicy> policyIterator = new RequestResultIterator<>(ListUserAttachedPolicyRequestCommand.class, client, options);
				
				setPolicyInfo(identity, policyIterator);
				
				return identity;
			}
		};
	}

	@Override
	public Iterator<Role> allRoles() throws ConnectorException {
		
		log.info("Listing all roles.");
		
		return new Iterator<Role>() {

			Iterator<Group> iterator = new RequestResultIterator<>(ListGroupsRequestCommand.class, client);

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Role next() {
				Group group = iterator.next();
				RoleImpl awsGroup = AwsModelConverter.groupToRole(group);
				
				Map<String, String> options = new HashMap<>();
				options.put("groupName", group.groupName());
				
				Iterator<AttachedPolicy> policyIterator = new RequestResultIterator<>(ListGroupAttachedPolicyRequestCommand.class, client, options);
				
				setPolicyInfo(awsGroup, policyIterator);
				
				return awsGroup;
			}
		};
	}
	
	public Iterator<AwsPolicy> allPolicies() throws ConnectorException {
			
		log.info("Listing all policies.");
		
		return new Iterator<AwsPolicy>() {

			Iterator<Policy> iterator = new RequestResultIterator<>(ListPoliciesRequestCommand.class, client);

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public AwsPolicy next() {
				Policy policy = iterator.next();
				AwsPolicy awsPolicy = AwsModelConverter.policyToAwsPolicy(policy);
				
				return awsPolicy;
			}
		};
	}

	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		
		log.info(String.format("Creating identity by name %s", identity.getPrincipalName()));
		
		try {
			Map<String, String> options = new HashMap<>();
			options.put("userName", identity.getPrincipalName());
			options.put("password", new String(password));
			options.put("passwordResetRequired", "false");
			
			CreateUserRequestCommand createUserRequestCommand = CommandFactory.get(CreateUserRequestCommand.class, options);
			CommandResult<User> result = createUserRequestCommand.execute(client);
			
			String[] policyArns = getPolicyArnsFromNameAndValue(identity);
			attachPolicyToUser(identity, policyArns);
			
			Role[] roles = identity.getRoles();
			
			addRolesToUser(identity, roles);
	
			Identity identityRespnse = AwsModelConverter.userToAwsIdentity(result.getResult(), identity);
			return identityRespnse;
		} catch (EntityAlreadyExistsException e) {
			throw new PrincipalAlreadyExistsException(identity.getPrincipalName() + " already exists.", e,
					PrincipalType.user);
		}
	}
	
	
	@Override
	public Role createRole(Role role) throws ConnectorException {
		
		log.info(String.format("Creating role by name %s", role.getPrincipalName()));
		
		try {
			Map<String, String> options = new HashMap<>();
			options.put("groupName", role.getPrincipalName());
			
			CreateGroupRequestCommand createGroupRequestCommand = CommandFactory.get(CreateGroupRequestCommand.class, options);
			CommandResult<Group> result = createGroupRequestCommand.execute(client);
			
			String[] policyArns = getPolicyArnsFromNameAndValue(role);
			attachPolicyToRole(role, policyArns);
			
			Role roleResponse = AwsModelConverter.groupToRole(result.getResult(), role);
			return roleResponse;
		} catch (EntityAlreadyExistsException e) {
			throw new PrincipalAlreadyExistsException(role.getPrincipalName() + " already exists.", e,
					PrincipalType.role);
		}
	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		
		log.info(String.format("Deleting identity by name %s", principalName));
		
		try {
			Map<String, String> options = new HashMap<>();
			options.put("userName", principalName);
			
			RemoveUserPasswordRequestCommand removeUserPasswordRequestCommand = CommandFactory.get(RemoveUserPasswordRequestCommand.class, options);
			removeUserPasswordRequestCommand.execute(client);
			
			DeleteUserRequestCommand delteUserRequestCommand = CommandFactory.get(DeleteUserRequestCommand.class, options);
			delteUserRequestCommand.execute(client);
		} catch(NoSuchEntityException e) {
			throw new PrincipalNotFoundException(principalName + " not found.", e, PrincipalType.user);
		} catch(Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	@Override
	public void deleteRole(String principalName) throws ConnectorException {
		
		log.info(String.format("Deleting role by name %s", principalName));
		
		try {
			Map<String, String> options = new HashMap<>();
			options.put("groupName", principalName);
			
			GetGroupRequestCommand getGroupRequestCommand = CommandFactory.get(GetGroupRequestCommand.class, options);
			CommandResult<AwsGroup> commandResult = getGroupRequestCommand.execute(client);
			
			AwsGroup awsGroup = commandResult.getResult();
			
			List<User> attachedUsers = awsGroup.getUsers();
			
			if (attachedUsers != null) {
				for (User user : attachedUsers) {
					Map<String, String> optionsRemove = new HashMap<>();
					optionsRemove.put("userName", user.userName());
					optionsRemove.put("groupName", principalName);
					
					RemoveUserFromGroupRequestCommand removeUserFromGroupRequestCommand = CommandFactory.get(RemoveUserFromGroupRequestCommand.class, 
							optionsRemove);
					
					removeUserFromGroupRequestCommand.execute(client);
					
				}
			}
			
			DeleteGroupRequestCommand delteGroupRequestCommand = CommandFactory.get(DeleteGroupRequestCommand.class, options);
			delteGroupRequestCommand.execute(client);
		} catch(NoSuchEntityException e) {
			throw new PrincipalNotFoundException(principalName + " not found.", e, PrincipalType.role);
		} catch(Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		
		log.info(String.format("Setting password for principal name %s", identity.getPrincipalName()));
		
		Map<String, String> options = new HashMap<>();
		options.put("userName", identity.getPrincipalName());
		options.put("password", new String(password));
		options.put("passwordResetRequired", Boolean.toString(forcePasswordChangeAtLogon));
		
		UpdateUserPasswordRequestCommand updateUserPasswordRequestCommand = CommandFactory.get(UpdateUserPasswordRequestCommand.class, options);
		updateUserPasswordRequestCommand.execute(client);
		
	}
	
	@Override
	public Identity getIdentityByName(String name) throws PrincipalNotFoundException, ConnectorException {
		
		log.info(String.format("Fetch identity for principal name %s", name));
		
		try {
			Map<String, String> options = new HashMap<>();
			options.put("userName", name);
			
			GetUserRequestCommand getUserRequestCommand = CommandFactory.get(GetUserRequestCommand.class, options);
			CommandResult<User> result = getUserRequestCommand.execute(client);
			
			User user = result.getResult();
			
			if (user == null) {
				throw new PrincipalNotFoundException(name + " not found.", PrincipalType.user);
			}
			
			Identity identity = AwsModelConverter.userToAwsIdentity(user);
			
			Map<String, String> optionsForUserGroups = new HashMap<>();
			optionsForUserGroups.put("userName", name);
			
			Iterator<Group> iteratorGroup = new RequestResultIterator<>(ListGroupsForUserRequestCommand.class, client, optionsForUserGroups);
			while(iteratorGroup.hasNext()) {
				Group group = iteratorGroup.next();
				Role role = AwsModelConverter.groupToRole(group);
				identity.addRole(role);
			}

			Map<String, String> optionsForUserPolicies = new HashMap<>();
			optionsForUserPolicies.put("userName", name);
			
			Iterator<AttachedPolicy> iteratorPolicy = new RequestResultIterator<>(ListUserAttachedPolicyRequestCommand.class, client, optionsForUserPolicies);
			setPolicyInfo(identity, iteratorPolicy);
			
			return identity;
		} catch(NoSuchEntityException e) {
			throw new PrincipalNotFoundException(name + " not found.", e, PrincipalType.user);
		} catch(Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	@Override
	public Role getRoleByName(String name) throws PrincipalNotFoundException, ConnectorException {
		
		log.info(String.format("Fetch identity for role name %s", name));
		
		try {
		
			Map<String, String> options = new HashMap<>();
			options.put("groupName", name);
			
			GetGroupRequestCommand getGroupRequestCommand = CommandFactory.get(GetGroupRequestCommand.class, options);
			CommandResult<AwsGroup> result = getGroupRequestCommand.execute(client);
			
			Group group = result.getResult().getGroup();
			
			if (group == null) {
				throw new PrincipalNotFoundException(name + " not found.", PrincipalType.role);
			}
			
			Role role = AwsModelConverter.groupToRole(group);
			
			Map<String, String> optionsForGroupPolicies = new HashMap<>();
			optionsForGroupPolicies.put("groupName", name);
			
			Iterator<AttachedPolicy> iteratorPolicy = new RequestResultIterator<>(ListGroupAttachedPolicyRequestCommand.class, client, optionsForGroupPolicies);
			setPolicyInfo(role, iteratorPolicy);
			
			
			return role;
		} catch(NoSuchEntityException e) {
			throw new PrincipalNotFoundException(name + " not found.", e, PrincipalType.role);
		} catch(Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		
		log.info(String.format("Updating identity for identity %s", identity.getPrincipalName()));
		
		checkIdentityExist(identity);
		
		// Adjust roles
		// get iterator of current groups.
		Map<String, String> optionsForUserGroups = new HashMap<>();
		optionsForUserGroups.put("userName", identity.getPrincipalName());
		
		Iterator<Group> iteratorGroup = new RequestResultIterator<>(ListGroupsForUserRequestCommand.class, client, optionsForUserGroups);
		while(iteratorGroup.hasNext()) {
			Group group = iteratorGroup.next();
			log.info(String.format("Removing role %s during update operation.", group.groupName()));
			
			Map<String, String> optionsRemove = new HashMap<>();
			optionsRemove.put("userName", identity.getPrincipalName());
			optionsRemove.put("groupName", group.groupName());
			
			RemoveUserFromGroupRequestCommand removeUserFromGroupRequestCommand = CommandFactory.get(RemoveUserFromGroupRequestCommand.class, 
					optionsRemove);
			
			removeUserFromGroupRequestCommand.execute(client);
		}
		
		Role[] roles = identity.getRoles();
		addRolesToUser(identity, roles);
		
		
		// Adjust policies
		Map<String, String> optionsForUserPolicies = new HashMap<>();
		optionsForUserPolicies.put("userName", identity.getPrincipalName());
		
		Iterator<AttachedPolicy> iteratorPolicy = new RequestResultIterator<>(ListUserAttachedPolicyRequestCommand.class, client, optionsForUserPolicies);
		while(iteratorPolicy.hasNext()) {
			AttachedPolicy policy = iteratorPolicy.next();
			
			log.info(String.format("Removing policy %s", policy.policyArn()));
			
			Map<String, String> optionsRemove = new HashMap<>();
			optionsRemove.put("userName", identity.getPrincipalName());
			optionsRemove.put("policyArn", policy.policyArn());
			
			DetachPolicyFromUserRequestCommand detachPolicyFromUserRequestCommand = CommandFactory.get(DetachPolicyFromUserRequestCommand.class, optionsRemove);
			detachPolicyFromUserRequestCommand.execute(client);
		}
		
		String[] policyArns = getPolicyArnsFromNameAndValue(identity);
		attachPolicyToUser(identity, policyArns);
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		log.info(String.format("Updating role for principal %s", role.getPrincipalName()));
		
		checkRoleExists(role);
		
		// Adjust policies
		Map<String, String> optionsForGroupPolicies = new HashMap<>();
		optionsForGroupPolicies.put("groupName", role.getPrincipalName());
		
		Iterator<AttachedPolicy> iteratorPolicy = new RequestResultIterator<>(ListGroupAttachedPolicyRequestCommand.class, client, optionsForGroupPolicies);
		while(iteratorPolicy.hasNext()) {
			AttachedPolicy policy = iteratorPolicy.next();
			
			log.info(String.format("Removing policy %s", policy.policyArn()));
			
			Map<String, String> optionsRemove = new HashMap<>();
			optionsRemove.put("groupName", role.getPrincipalName());
			optionsRemove.put("policyArn", policy.policyArn());
			
			DetachPolicyFromGroupRequestCommand detachPolicyFromGroupRequestCommand = CommandFactory.get(DetachPolicyFromGroupRequestCommand.class, optionsRemove);
			detachPolicyFromGroupRequestCommand.execute(client);
		}
		
		String[] policyArns = getPolicyArnsFromNameAndValue(role);
		attachPolicyToRole(role, policyArns);
	}
	
	@Override
	public boolean isOpen() {
		return client != null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	protected void onOpen(AwsConfiguration parameters) throws ConnectorException {

		String regionConf = parameters.getAwsRegion();
		String accessKeyId = parameters.getAwsAccessKeyId();
		String secretAccessKey = parameters.getAwsSecretAccessKey();

		Region region = null;
		List<Region> regions = Region.regions();
		for (Region r : regions) {
			if (r.id().equals(regionConf)) {
				region = r;
				break;
			}
		}

		if (region == null) {
			region = Region.of(regionConf);
		}

		AwsCredentials awsCredentials = null;
		if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
			awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
		}

		IamClientBuilder clientBuilder = IamClient.builder().region(region);

		if (awsCredentials != null) {
			clientBuilder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
		}

		client = clientBuilder.build();
	}

	@Override
	public void close() {
		if (client != null) {
			client.close();
		}
	}
	
	private void attachPolicyToRole(Role role, String[] policyArns) {
		checkPoliciesExists(policyArns);
		
		if (policyArns != null) {
			for (String policyArn : policyArns) {
				log.info(String.format("Adding policy arn %s", policyArn));
				Map<String, String> op = new HashMap<>();
				op.put("groupName", role.getPrincipalName());
				op.put("policyArn", policyArn);
				AttachPolicyToGroupRequestCommand attachPolicyToGroupRequestCommand = CommandFactory.get(AttachPolicyToGroupRequestCommand.class, op);
				attachPolicyToGroupRequestCommand.execute(client);
			}
		}
	}
	
	private void attachPolicyToUser(Identity identity, String[] policyArns) {
		checkPoliciesExists(policyArns);
		
		if (policyArns != null) {
			for (String policyArn : policyArns) {
				log.info(String.format("Adding policy arn %s", policyArn));
				Map<String, String> op = new HashMap<>();
				op.put("userName", identity.getPrincipalName());
				op.put("policyArn", policyArn);
				AttachPolicyToUserRequestCommand attachPolicyToUserRequestCommand = CommandFactory.get(AttachPolicyToUserRequestCommand.class, op);
				attachPolicyToUserRequestCommand.execute(client);
			}
		}
	}
	
	private void addRolesToUser(Identity identity, Role[] roles) {
		checkRolesExists(roles);
		
		for (Role role : roles) {
			log.info(String.format("Adding role %s", role.getPrincipalName()));
			Map<String, String> op = new HashMap<>();
			op.put("userName", identity.getPrincipalName());
			op.put("groupName", role.getPrincipalName());
			AddUserToGroupRequestCommand addUserToGroupRequestCommand = CommandFactory.get(AddUserToGroupRequestCommand.class, op);
			addUserToGroupRequestCommand.execute(client);
		}
	}
	
	private String[] getPolicyArnsFromNameAndValue(Principal principal) {
		
		String[] policyArnsNameValuePairs = principal.getAttributes(AWS_ATTRIBUTE_POLICY_ARNS);
		
		if (policyArnsNameValuePairs == null || policyArnsNameValuePairs.length == 0) {
			return new String[0];
		}
		
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		
		for (String pair : policyArnsNameValuePairs) {
			list.add(new NameValuePair(pair)); 
		}
		
		List<String> policyArns = new ArrayList<String>();
		for (NameValuePair string : list) {
			policyArns.add(string.getName());
		}
		
		return policyArns.toArray(new String[0]);
	}

	private void setPolicyInfo(Principal principal, Iterator<AttachedPolicy> policyIterator) {
		
		List<NameValuePair> policyArns = new ArrayList<>();
		
		while (policyIterator.hasNext()) {
			
			AttachedPolicy attachedPolicy = policyIterator.next();
			
			policyArns.add(new NameValuePair(attachedPolicy.policyArn(), attachedPolicy.policyName()));
			
		}
		
		principal.setAttribute(AWS_ATTRIBUTE_POLICY_ARNS, NameValuePair.implodeNamePairs(policyArns));
		
	}
	
	private void checkIdentityExist(Identity identity) {
		Map<String, String> options = new HashMap<>();
		options.put("userName", identity.getPrincipalName());
		
		GetUserRequestCommand getUserRequestCommand = CommandFactory.get(GetUserRequestCommand.class, options);
		try {
			getUserRequestCommand.execute(client);
		} catch (NoSuchEntityException e) {
			throw new PrincipalNotFoundException(identity.getPrincipalName() + " not found.", PrincipalType.user);
		}
	}
	
	private void checkRolesExists(Role[] roles) {
		if (roles != null) {
			for (Role role: roles) {
				checkRoleExists(role);
			}
		}
		
	}

	private void checkRoleExists(Role role) {
		Map<String, String> op = new HashMap<>();
		op.put("groupName", role.getPrincipalName());
		GetGroupRequestCommand getGroupRequestCommand = CommandFactory.get(GetGroupRequestCommand.class, op);
		try {
			getGroupRequestCommand.execute(client);
		} catch (NoSuchEntityException e) {
			throw new PrincipalNotFoundException(role.getPrincipalName() + " not found.", PrincipalType.role);
		}
	}

	private void checkPoliciesExists(String[] policyArns) {
		if (policyArns != null) {
			for (String policyArn : policyArns) {
				checkPolicyExists(policyArn);
			}
		}
	}

	private void checkPolicyExists(String policyArn) {
		Map<String, String> op = new HashMap<>();
		op.put("policyArn", policyArn);
		GetPolicyRequestCommand getPolicyRequestCommand = CommandFactory.get(GetPolicyRequestCommand.class, op);
		try {
			getPolicyRequestCommand.execute(client);
		} catch (NoSuchEntityException e) {
			throw new ConnectorException(String.format("Policy %s not found.", policyArn));
		}
	}
	
}
