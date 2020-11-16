package com.identity4j.connector.aws;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;

import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.Tag;
import software.amazon.awssdk.services.iam.model.User;

public class AwsModelConverter {

	public static AwsIdentity userToAwsIdentity(User user){
		
		AwsIdentity awsIdentity = new AwsIdentity(user.userId(), user.userName());
		
		return userToAwsIdentity(user, awsIdentity);
	}
	
	public static AwsIdentity userToAwsIdentity(User user, Identity identity){
		
		AwsIdentity awsIdentity = new AwsIdentity(user.userId(), user.userName());
		
		String arn = user.arn();
		if (arn != null) {
			awsIdentity.setAttribute("arn", arn);
		}
		
		String userPath = user.path();
		if (userPath != null) {
			awsIdentity.setAttribute("path", userPath);
		}
		
		if (user.passwordLastUsed() != null) {
			awsIdentity.setLastSignOnDate(Date.from(user.passwordLastUsed()));
		}
		
		Instant createDate = user.createDate();
		if (createDate != null) {
			awsIdentity.setAttribute("createDate", createDate.toString());
		}
		
		List<Tag> tags = user.tags();
		if (tags != null) {
			for (Tag tag : tags) {
				awsIdentity.setAttribute(tag.key(), tag.value());
			}
		}
		
		return awsIdentity;
	}

	public static RoleImpl groupToRole(Group group) {
		RoleImpl awsGroup = new RoleImpl(group.groupId(), group.groupName());
		
		return groupToRole(group, awsGroup);
	}
	
	public static RoleImpl groupToRole(Group group, Role role) {
		RoleImpl awsGroup = new RoleImpl(group.groupId(), group.groupName());
		
		String arn = group.arn();
		if (arn != null) {
			awsGroup.setAttribute("arn", arn);
		}
		
		String groupPath = group.path();
		if (groupPath != null) {
			awsGroup.setAttribute("path", groupPath);
		}
		
		Instant createDate = group.createDate();
		if (createDate != null) {
			awsGroup.setAttribute("createDate", createDate.toString());
		}
		
		return awsGroup;
	}

	public static AwsPolicy policyToAwsPolicy(Policy policy) {
		
		AwsPolicy awsPolicy = new AwsPolicy();
		
		awsPolicy.setPolicyId(policy.policyId());
		awsPolicy.setPolicyName(policy.policyName());
		awsPolicy.setArn(policy.arn());
		awsPolicy.setAttachmentCount(policy.attachmentCount());
		awsPolicy.setCreateDate(policy.createDate());
		awsPolicy.setUpdateDate(policy.updateDate());
		awsPolicy.setDefaultVersionId(policy.defaultVersionId());
		awsPolicy.setDescription(policy.description());
		awsPolicy.setIsAttachable(policy.isAttachable());
		awsPolicy.setPath(policy.path());
		awsPolicy.setPermissionsBoundaryUsageCount(policy.permissionsBoundaryUsageCount());
		
		
		return awsPolicy;
	}
}
