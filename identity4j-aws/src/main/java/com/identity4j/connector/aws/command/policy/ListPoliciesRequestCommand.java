package com.identity4j.connector.aws.command.policy;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListPoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListPoliciesResponse;
import software.amazon.awssdk.services.iam.model.Policy;

public class ListPoliciesRequestCommand extends RequestCommand<Policy> {
	
	private ListPoliciesRequest listPoliciesRequest;
	
	public ListPoliciesRequestCommand(String marker) {
		super(marker);
		
		if (marker == null) {
			listPoliciesRequest = ListPoliciesRequest.builder().build();
		} else {
			listPoliciesRequest = ListPoliciesRequest.builder().marker(marker).build();
        }
	}

	@Override
	public CommandResult<Policy> execute(IamClient client) {
		ListPoliciesResponse response = client.listPolicies(listPoliciesRequest);
		return new CommandResult<Policy>(!response.isTruncated(), response.marker(), response.policies().iterator());
	}
	
}