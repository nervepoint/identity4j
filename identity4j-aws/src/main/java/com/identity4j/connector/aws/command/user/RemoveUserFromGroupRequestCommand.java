package com.identity4j.connector.aws.command.user;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.RemoveUserFromGroupRequest;
import software.amazon.awssdk.utils.StringUtils;

public class RemoveUserFromGroupRequestCommand extends RequestCommand<Void> {

	private RemoveUserFromGroupRequest removeUserFromGroupRequest;
	
	public RemoveUserFromGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String userName = options.get("userName");
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(groupName)) {
			throw new IllegalStateException("Username or Group name is missing.");
		}
		
		removeUserFromGroupRequest = RemoveUserFromGroupRequest.builder().userName(userName).groupName(groupName).build();
		
	}

	
	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.removeUserFromGroup(removeUserFromGroupRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
