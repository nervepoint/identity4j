package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DetachUserPolicyRequest;
import software.amazon.awssdk.utils.StringUtils;

public class DetachPolicyFromUserRequestCommand extends RequestCommand<Void> {
	
	private DetachUserPolicyRequest detachUserPolicyRequest;
	
	public DetachPolicyFromUserRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String policyArn = options.get("policyArn");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(policyArn)) {
			throw new IllegalStateException("User or Policy is missing.");
		}
		
		detachUserPolicyRequest = DetachUserPolicyRequest.builder().userName(userName).policyArn(policyArn).build();
	}

	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.detachUserPolicy(detachUserPolicyRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}
	
}