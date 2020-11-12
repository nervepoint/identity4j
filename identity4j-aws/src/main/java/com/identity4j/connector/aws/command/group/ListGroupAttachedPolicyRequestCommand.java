package com.identity4j.connector.aws.command.group;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.ListAttachedGroupPoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListAttachedGroupPoliciesResponse;
import software.amazon.awssdk.utils.StringUtils;

public class ListGroupAttachedPolicyRequestCommand extends RequestCommand<AttachedPolicy>{
	
	private ListAttachedGroupPoliciesRequest listAttachedGroupPoliciesRequest;
	
	public ListGroupAttachedPolicyRequestCommand(Map<String, String> options, String marker) {
		super(marker);
		
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(groupName)) {
			throw new IllegalArgumentException("Group name is missing.");
		}
		
		if (marker == null) {
			listAttachedGroupPoliciesRequest = ListAttachedGroupPoliciesRequest.builder().groupName(groupName).build();
		} else {
			listAttachedGroupPoliciesRequest = ListAttachedGroupPoliciesRequest.builder().groupName(groupName).marker(marker).build();
        }
	}

	@Override
	public CommandResult<AttachedPolicy> execute(IamClient client) {
		ListAttachedGroupPoliciesResponse response = client.listAttachedGroupPolicies(listAttachedGroupPoliciesRequest);
		return new CommandResult<AttachedPolicy>(!response.isTruncated(), response.marker(), response.attachedPolicies().iterator());
	}

}
