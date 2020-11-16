package com.identity4j.connector.aws.command.user;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetUserRequest;
import software.amazon.awssdk.services.iam.model.GetUserResponse;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.utils.StringUtils;

public class GetUserRequestCommand extends RequestCommand<User> {

	private GetUserRequest getUserRequest;

	public GetUserRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		
		if (StringUtils.isBlank(userName)) {
			throw new IllegalStateException("Username is missing.");
		}
		
		getUserRequest = GetUserRequest.builder().userName(userName).build();
	}

	@Override
	public CommandResult<User> execute(IamClient client) {
		GetUserResponse response = client.getUser(getUserRequest);
		return new CommandResult<User>(true, null, response.user());
	}
}