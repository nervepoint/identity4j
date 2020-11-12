package com.identity4j.connector.aws.command.group;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.ListGroupsRequest;
import software.amazon.awssdk.services.iam.model.ListGroupsResponse;

public class ListGroupsRequestCommand extends RequestCommand<Group> {
	
	private ListGroupsRequest listGroupsRequest; 
	
	public ListGroupsRequestCommand(String marker) {
		super(marker);
		
		if (marker == null) {
			listGroupsRequest = ListGroupsRequest.builder().build();
		} else {
			listGroupsRequest = ListGroupsRequest.builder().marker(marker).build();
        }
	}

	@Override
	public CommandResult<Group> execute(IamClient client) {
		ListGroupsResponse response = client.listGroups(listGroupsRequest);
		return new CommandResult<Group>(!response.isTruncated(), response.marker(), response.groups().iterator());
	}
	
}