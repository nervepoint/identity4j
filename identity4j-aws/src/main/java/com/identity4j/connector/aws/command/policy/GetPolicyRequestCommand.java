package com.identity4j.connector.aws.command.policy;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetPolicyRequest;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.utils.StringUtils;

public class GetPolicyRequestCommand extends RequestCommand<Policy> {

	private GetPolicyRequest getPolicyRequest;

	public GetPolicyRequestCommand(Map<String, String> options) {
		super(null);
		
		String policyArn = options.get("policyArn");
		
		if (StringUtils.isBlank(policyArn)) {
			throw new IllegalStateException("Policy ARN is missing.");
		}
		
		getPolicyRequest = GetPolicyRequest.builder().policyArn(policyArn).build();
	}

	@Override
	public CommandResult<Policy> execute(IamClient client) {
		GetPolicyResponse response = client.getPolicy(getPolicyRequest);
		return new CommandResult<Policy>(true, null, response.policy());
	}
}