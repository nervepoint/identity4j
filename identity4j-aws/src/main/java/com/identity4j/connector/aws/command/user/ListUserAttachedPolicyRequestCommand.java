package com.identity4j.connector.aws.command.user;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.ListAttachedUserPoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListAttachedUserPoliciesResponse;
import software.amazon.awssdk.utils.StringUtils;

public class ListUserAttachedPolicyRequestCommand extends RequestCommand<AttachedPolicy>{
	
	private ListAttachedUserPoliciesRequest listAttachedUserPoliciesRequest;
	
	public ListUserAttachedPolicyRequestCommand(Map<String, String> options, String marker) {
		super(marker);
		
		String userName = options.get("userName");
		
		if (StringUtils.isBlank(userName)) {
			throw new IllegalArgumentException("User name is missing.");
		}
		
		if (marker == null) {
			listAttachedUserPoliciesRequest = ListAttachedUserPoliciesRequest.builder().userName(userName).build();
		} else {
			listAttachedUserPoliciesRequest = ListAttachedUserPoliciesRequest.builder().userName(userName).marker(marker).build();
        }
	}

	@Override
	public CommandResult<AttachedPolicy> execute(IamClient client) {
		ListAttachedUserPoliciesResponse response = client.listAttachedUserPolicies(listAttachedUserPoliciesRequest);
		return new CommandResult<AttachedPolicy>(!response.isTruncated(), response.marker(), response.attachedPolicies().iterator());
	}

}
