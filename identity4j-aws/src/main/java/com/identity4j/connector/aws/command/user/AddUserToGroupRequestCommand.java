package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AddUserToGroupRequest;
import software.amazon.awssdk.utils.StringUtils;

public class AddUserToGroupRequestCommand extends RequestCommand<Void> {

	private AddUserToGroupRequest addUserToGroupRequest;
	
	public AddUserToGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(groupName)) {
			throw new IllegalStateException("Username or Group name is missing.");
		}
		
		addUserToGroupRequest = AddUserToGroupRequest.builder().userName(userName).groupName(groupName).build();
		
	}

	
	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.addUserToGroup(addUserToGroupRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
