package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateLoginProfileRequest;
import software.amazon.awssdk.utils.StringUtils;

public class CreateUserPasswordRequestCommand extends RequestCommand<Void> {

	private CreateLoginProfileRequest loginProfileRequest;
	
	public CreateUserPasswordRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String password = options.get("password");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
			throw new IllegalStateException("Username or Password is missing.");
		}
		
		Boolean passwordResetRequired = Boolean.parseBoolean(options.get("passwordResetRequired"));
		
		loginProfileRequest = CreateLoginProfileRequest.builder()
				.password(new String(password)).passwordResetRequired(passwordResetRequired).userName(userName)
				.build();
		
	}

	
	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.createLoginProfile(loginProfileRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
