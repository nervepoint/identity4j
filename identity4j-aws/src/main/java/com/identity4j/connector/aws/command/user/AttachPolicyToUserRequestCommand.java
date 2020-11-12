package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachUserPolicyRequest;
import software.amazon.awssdk.utils.StringUtils;

public class AttachPolicyToUserRequestCommand extends RequestCommand<Void> {
	
	private AttachUserPolicyRequest attachUserPolicyRequest;
	
	public AttachPolicyToUserRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String policyArn = options.get("policyArn");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(policyArn)) {
			throw new IllegalStateException("User or Policy is missing.");
		}
		
		attachUserPolicyRequest = AttachUserPolicyRequest.builder().userName(userName).policyArn(policyArn).build();
	}

	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.attachUserPolicy(attachUserPolicyRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}
	
}