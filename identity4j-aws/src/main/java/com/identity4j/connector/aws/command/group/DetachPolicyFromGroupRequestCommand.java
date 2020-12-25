package com.identity4j.connector.aws.command.group;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DetachGroupPolicyRequest;
import software.amazon.awssdk.utils.StringUtils;

public class DetachPolicyFromGroupRequestCommand extends RequestCommand<Void> {
	
	private DetachGroupPolicyRequest detachGroupPolicyRequest;
	
	public DetachPolicyFromGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String groupName = options.get("groupName");
		String policyArn = options.get("policyArn");
		
		if (StringUtils.isBlank(groupName) || StringUtils.isBlank(policyArn)) {
			throw new IllegalStateException("Group or Policy is missing.");
		}
		
		detachGroupPolicyRequest = DetachGroupPolicyRequest.builder().groupName(groupName).policyArn(policyArn).build();
	}

	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.detachGroupPolicy(detachGroupPolicyRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}
	
}