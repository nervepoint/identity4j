package com.identity4j.connector.aws.command.user;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;

public class ListUsersRequestCommand extends RequestCommand<User> {

	private ListUsersRequest listUsersRequest;

	public ListUsersRequestCommand(String marker) {
		super(marker);
		
		if (marker == null) {
			listUsersRequest = ListUsersRequest.builder().build();
		} else {
			listUsersRequest = ListUsersRequest.builder().marker(marker).build();
        }
	}

	@Override
	public CommandResult<User> execute(IamClient client) {
		ListUsersResponse response = client.listUsers(listUsersRequest);
		return new CommandResult<User>(!response.isTruncated(), response.marker(), response.users().iterator());
	}
}