package com.identity4j.connector.aws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.aws.command.CommandFactory;
import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestResultIterator;
import com.identity4j.connector.aws.command.group.AttachPolicyToGroupRequestCommand;
import com.identity4j.connector.aws.command.group.CreateGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DeleteGroupRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupsRequestCommand;
import com.identity4j.connector.aws.command.policy.ListPoliciesRequestCommand;
import com.identity4j.connector.aws.command.user.AttachPolicyToUserRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserRequestCommand;
import com.identity4j.connector.aws.command.user.DeleteUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListUserAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.user.ListUsersRequestCommand;
import com.identity4j.connector.aws.command.user.RemoveUserPasswordRequestCommand;
import com.identity4j.connector.aws.command.user.UpdateUserPasswordRequestCommand;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AbstractPrincipal;
import com.identity4j.connector.principal.Identity;
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

	public static final String AWS_ATTRIBUTE_POLICY_ARNS = "policyArns";

	private IamClient client;

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays
			.asList(new ConnectorCapability[] { ConnectorCapability.passwordSet,
					ConnectorCapability.createUser, ConnectorCapability.deleteUser, 
					ConnectorCapability.roles,
					ConnectorCapability.createRole, ConnectorCapability.deleteRole,
					ConnectorCapability.identities, ConnectorCapability.identityAttributes,
					ConnectorCapability.roleAttributes }));

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {

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
		try {
			Map<String, String> options = new HashMap<>();
			options.put("userName", identity.getPrincipalName());
			options.put("password", new String(password));
			options.put("passwordResetRequired", "false");
			
			CreateUserRequestCommand createUserRequestCommand = CommandFactory.get(CreateUserRequestCommand.class, options);
			CommandResult<User> result = createUserRequestCommand.execute(client);
			
			String[] policyArns = identity.getAttributes(AWS_ATTRIBUTE_POLICY_ARNS);
			if (policyArns != null) {
				for (String policyArn : policyArns) {
					Map<String, String> op = new HashMap<>();
					op.put("userName", identity.getPrincipalName());
					op.put("policyArn", policyArn);
					AttachPolicyToUserRequestCommand attachPolicyToUserRequestCommand = CommandFactory.get(AttachPolicyToUserRequestCommand.class, op);
					attachPolicyToUserRequestCommand.execute(client);
				}
			}
	
			Identity identityRespnse = AwsModelConverter.userToAwsIdentity(result.getResult());
			return identityRespnse;
		} catch (EntityAlreadyExistsException e) {
			throw new PrincipalAlreadyExistsException(identity.getPrincipalName() + " already exists.", e,
					PrincipalType.user);
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	@Override
	public Role createRole(Role role) throws ConnectorException {
		try {
			Map<String, String> options = new HashMap<>();
			options.put("groupName", role.getPrincipalName());
			
			CreateGroupRequestCommand createGroupRequestCommand = CommandFactory.get(CreateGroupRequestCommand.class, options);
			CommandResult<Group> result = createGroupRequestCommand.execute(client);
			
			String[] policyArns = role.getAttributes(AWS_ATTRIBUTE_POLICY_ARNS);
			if (policyArns != null) {
				for (String policyArn : policyArns) {
					Map<String, String> op = new HashMap<>();
					op.put("groupName", role.getPrincipalName());
					op.put("policyArn", policyArn);
					AttachPolicyToGroupRequestCommand attachPolicyToGroupRequestCommand = CommandFactory.get(AttachPolicyToGroupRequestCommand.class, op);
					attachPolicyToGroupRequestCommand.execute(client);
				}
			}
			
			Role roleResponse = AwsModelConverter.groupToRole(result.getResult());
			return roleResponse;
		} catch (EntityAlreadyExistsException e) {
			throw new PrincipalAlreadyExistsException(role.getPrincipalName() + " already exists.", e,
					PrincipalType.role);
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
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
		try {
			Map<String, String> options = new HashMap<>();
			options.put("groupName", principalName);
			
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
		
		Map<String, String> options = new HashMap<>();
		options.put("userName", identity.getPrincipalName());
		options.put("password", new String(password));
		options.put("passwordResetRequired", Boolean.toString(forcePasswordChangeAtLogon));
		
		UpdateUserPasswordRequestCommand updateUserPasswordRequestCommand = CommandFactory.get(UpdateUserPasswordRequestCommand.class, options);
		updateUserPasswordRequestCommand.execute(client);
		
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

	private void setPolicyInfo(AbstractPrincipal principal, Iterator<AttachedPolicy> policyIterator) {
		
		List<String> policyArns = new ArrayList<String>();
		
		while (policyIterator.hasNext()) {
			
			AttachedPolicy attachedPolicy = policyIterator.next();
			
			String arn = attachedPolicy.policyArn();
			
			policyArns.add(arn);
			
		}
		
		principal.setAttribute(AWS_ATTRIBUTE_POLICY_ARNS, policyArns.toArray(new String[0]));
		
	}

}
