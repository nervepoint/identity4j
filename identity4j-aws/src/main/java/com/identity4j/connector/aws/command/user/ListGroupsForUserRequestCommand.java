package com.identity4j.connector.aws.command.user;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.ListGroupsForUserRequest;
import software.amazon.awssdk.services.iam.model.ListGroupsForUserResponse;
import software.amazon.awssdk.utils.StringUtils;

public class ListGroupsForUserRequestCommand extends RequestCommand<Group>{
	
	private ListGroupsForUserRequest listGroupsForUserRequest;
	
	public ListGroupsForUserRequestCommand(Map<String, String> options, String marker) {
		super(marker);
		
		String userName = options.get("userName");
		
		if (StringUtils.isBlank(userName)) {
			throw new IllegalArgumentException("User name is missing.");
		}
		
		if (marker == null) {
			listGroupsForUserRequest = ListGroupsForUserRequest.builder().userName(userName).build();
		} else {
			listGroupsForUserRequest = ListGroupsForUserRequest.builder().userName(userName).marker(marker).build();
        }
	}

	@Override
	public CommandResult<Group> execute(IamClient client) {
		ListGroupsForUserResponse response = client.listGroupsForUser(listGroupsForUserRequest);
		return new CommandResult<Group>(!response.isTruncated(), response.marker(), response.groups().iterator());
	}

}
