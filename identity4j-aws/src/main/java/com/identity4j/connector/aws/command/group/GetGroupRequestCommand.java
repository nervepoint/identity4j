package com.identity4j.connector.aws.command.group;

import java.util.Map;

import com.identity4j.connector.aws.AwsGroup;
import com.identity4j.connector.aws.command.CommandResult;
import com.identity4j.connector.aws.command.RequestCommand;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetGroupRequest;
import software.amazon.awssdk.services.iam.model.GetGroupResponse;
import software.amazon.awssdk.utils.StringUtils;

public class GetGroupRequestCommand extends RequestCommand<AwsGroup> {

	private GetGroupRequest getGroupRequest;

	public GetGroupRequestCommand(Map<String, String> options) {
		super(null);
		
		String groupName = options.get("groupName");
		
		if (StringUtils.isBlank(groupName)) {
			throw new IllegalStateException("Group name is missing.");
		}
		
		if (marker == null) {
			getGroupRequest = GetGroupRequest.builder().groupName(groupName).build();
		} else {
			getGroupRequest = GetGroupRequest.builder().groupName(groupName).marker(marker).build();
        }
		
	}

	@Override
	public CommandResult<AwsGroup> execute(IamClient client) {
		GetGroupResponse response = client.getGroup(getGroupRequest);
		AwsGroup awsGroup = new AwsGroup();
		awsGroup.setGroup(response.group());
		if (response.hasUsers()) {
			awsGroup.setUsers(response.users());
		}
		return new CommandResult<AwsGroup>(!response.isTruncated(), response.marker(), awsGroup);
	}
}