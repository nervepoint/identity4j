package com.identity4j.connector.aws.command.group;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeleteGroupRequest;
import software.amazon.awssdk.utils.StringUtils;

public class DeleteGroupRequestCommand extends RequestCommand<Void> {

	private DeleteGroupRequest deleteGroupRequest;
	
	public DeleteGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(groupName)) {
			throw new IllegalStateException("Groupname is missing.");
		}
		
		deleteGroupRequest = DeleteGroupRequest.builder().groupName(groupName).build();
		
	}

	
	@Override
	public CommandResult<Void> execute(IamClient client) {
		client.deleteGroup(deleteGroupRequest);
		return new CommandResult<Void>(true, null, Collections.emptyIterator());
	}

}
