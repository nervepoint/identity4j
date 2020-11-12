package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeleteLoginProfileRequest;
import software.amazon.awssdk.utils.StringUtils;

public class RemoveUserPasswordRequestCommand extends RequestCommand<Void> {
	
private DeleteLoginProfileRequest deleteLoginProfileRequest;
	
	public RemoveUserPasswordRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		
		if (StringUtils.isBlank(userName)) {
			throw new IllegalStateException("Username is missing.");
		}
		
		
		deleteLoginProfileRequest = DeleteLoginProfileRequest.builder()
				.userName(userName)
				.build();
		
	}

	
	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.deleteLoginProfile(deleteLoginProfileRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
