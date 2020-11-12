package com.identity4j.connector.aws.command.user;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandFactory;
import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.utils.StringUtils;

public class CreateUserRequestCommand extends RequestCommand<User> {

	private CreateUserRequest createUserRequest;
	private CreateUserPasswordRequestCommand createUserPasswordRequestCommand;
	
	public CreateUserRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String password = options.get("password");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
			throw new IllegalStateException("Username or Password is missing.");
		}
		
		createUserRequest = CreateUserRequest.builder().userName(userName).build();
		
		createUserPasswordRequestCommand = CommandFactory.get(CreateUserPasswordRequestCommand.class, options);
		
	}

	
	
	@Override
	public CommandResult<User> execute(IamClient client) {
		CreateUserResponse response = client.createUser(createUserRequest);
		createUserPasswordRequestCommand.execute(client);
		return new CommandResult<User>(true, null, response.user());
	}

}
