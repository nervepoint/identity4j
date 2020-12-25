package com.identity4j.connector.aws.command;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.group.AttachPolicyToGroupRequestCommand;
import com.identity4j.connector.aws.command.group.CreateGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DeleteGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DetachPolicyFromGroupRequestCommand;
import com.identity4j.connector.aws.command.group.GetGroupRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupsRequestCommand;
import com.identity4j.connector.aws.command.policy.GetPolicyRequestCommand;
import com.identity4j.connector.aws.command.policy.ListPoliciesRequestCommand;
import com.identity4j.connector.aws.command.user.AddUserToGroupRequestCommand;
import com.identity4j.connector.aws.command.user.AttachPolicyToUserRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserPasswordRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserRequestCommand;
import com.identity4j.connector.aws.command.user.DeleteUserRequestCommand;
import com.identity4j.connector.aws.command.user.DetachPolicyFromUserRequestCommand;
import com.identity4j.connector.aws.command.user.GetUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListGroupsForUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListUserAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.user.ListUsersRequestCommand;
import com.identity4j.connector.aws.command.user.RemoveUserFromGroupRequestCommand;
import com.identity4j.connector.aws.command.user.RemoveUserPasswordRequestCommand;
import com.identity4j.connector.aws.command.user.UpdateUserPasswordRequestCommand;

public class CommandFactory {

	public static <T extends RequestCommand<?>> T get(Class<T> type, Map<String, String> options) {
		return get(type, null, options);
	}
	
	public static <T extends RequestCommand<?>> T get(Class<T> type, String marker) {
		return get(type, marker, Collections.emptyMap());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends RequestCommand<?>> T get(Class<T> type, String marker, Map<String, ?> options) {
		
		String classSimpleName = type.getSimpleName();
		
		switch (classSimpleName) {
		
			case "GetUserRequestCommand":
				return (T) new GetUserRequestCommand((Map<String, String>) options);
			
			case "ListUsersRequestCommand":
				return (T) new ListUsersRequestCommand(marker);
				
			case "CreateUserRequestCommand":
				return (T) new CreateUserRequestCommand((Map<String, String>) options);
				
			case "CreateUserPasswordRequestCommand":
				return (T) new CreateUserPasswordRequestCommand((Map<String, String>) options);
				
			case "UpdateUserPasswordRequestCommand":
				return (T) new UpdateUserPasswordRequestCommand((Map<String, String>) options);
				
			case "RemoveUserPasswordRequestCommand":
				return (T) new RemoveUserPasswordRequestCommand((Map<String, String>) options);
				
			case "DeleteUserRequestCommand":
				return (T) new DeleteUserRequestCommand((Map<String, String>) options);
				
			case "AddUserToGroupRequestCommand":
				return (T) new AddUserToGroupRequestCommand((Map<String, String>) options);
				
			case "DetachPolicyFromUserRequestCommand":
				return	(T) new DetachPolicyFromUserRequestCommand((Map<String, String>) options);
				
			case "RemoveUserFromGroupRequestCommand":
				return (T) new RemoveUserFromGroupRequestCommand((Map<String, String>) options);
				
			case "ListGroupsForUserRequestCommand":
				return	(T) new ListGroupsForUserRequestCommand((Map<String, String>) options, marker);
				
			case "ListGroupsRequestCommand":
				return (T) new ListGroupsRequestCommand(marker);
				
			case "GetGroupRequestCommand":
				return (T) new GetGroupRequestCommand((Map<String, String>) options);
				
			case "CreateGroupRequestCommand":
				return (T) new CreateGroupRequestCommand((Map<String, String>) options);
				
			case "DeleteGroupRequestCommand":
				return (T) new DeleteGroupRequestCommand((Map<String, String>) options);
				
			case "ListPoliciesRequestCommand":
				return (T) new ListPoliciesRequestCommand(marker);
				
			case "AttachPolicyToUserRequestCommand":
				return	(T) new AttachPolicyToUserRequestCommand((Map<String, String>) options);
				
			case "ListUserAttachedPolicyRequestCommand":
				return	(T) new ListUserAttachedPolicyRequestCommand((Map<String, String>) options, marker);
				
			case "AttachPolicyToGroupRequestCommand":
				return	(T) new AttachPolicyToGroupRequestCommand((Map<String, String>) options);
				
			case "DetachPolicyFromGroupRequestCommand":	
				return	(T) new DetachPolicyFromGroupRequestCommand((Map<String, String>) options);
				
			case "ListGroupAttachedPolicyRequestCommand":
				return (T) new ListGroupAttachedPolicyRequestCommand((Map<String, String>) options, marker);
				
			case "GetPolicyRequestCommand":
				return (T) new GetPolicyRequestCommand((Map<String, String>) options);
			
			default:
				break;
		}
		return null;
		
	}
}
