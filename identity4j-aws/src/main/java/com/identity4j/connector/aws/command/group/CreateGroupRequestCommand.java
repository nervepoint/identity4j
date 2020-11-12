package com.identity4j.connector.aws.command.group;

import java.util.Map;

import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateGroupRequest;
import software.amazon.awssdk.services.iam.model.CreateGroupResponse;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.utils.StringUtils;

public class CreateGroupRequestCommand extends RequestCommand<Group> {

	private CreateGroupRequest createGroupRequest;
	
	public CreateGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(groupName)) {
			throw new IllegalStateException("Groupname or Password is missing.");
		}
		
		createGroupRequest = CreateGroupRequest.builder().groupName(groupName).build();
		
	}

	@Override
	public CommandResult<Group> execute(IamClient client) {
		CreateGroupResponse response = client.createGroup(createGroupRequest);
		return new CommandResult<Group>(true, null, response.group());
	}

}
