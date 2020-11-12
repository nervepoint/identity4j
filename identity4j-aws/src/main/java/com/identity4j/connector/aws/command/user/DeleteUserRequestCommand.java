package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeleteUserRequest;
import software.amazon.awssdk.utils.StringUtils;

public class DeleteUserRequestCommand extends RequestCommand<Void> {

	private DeleteUserRequest deleteUserRequest;
	
	public DeleteUserRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		
		if (StringUtils.isBlank(userName)) {
			throw new IllegalStateException("Username is missing.");
		}
		
		deleteUserRequest = DeleteUserRequest.builder().userName(userName).build();
		
	}
	
	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.deleteUser(deleteUserRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
