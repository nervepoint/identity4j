package com.identity4j.connector.aws.command;

import java.util.Collections;
import java.util.Map;

import com.identity4j.connector.aws.command.group.AttachPolicyToGroupRequestCommand;
import com.identity4j.connector.aws.command.group.CreateGroupRequestCommand;
import com.identity4j.connector.aws.command.group.DeleteGroupRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.group.ListGroupsRequestCommand;
import com.identity4j.connector.aws.command.policy.ListPoliciesRequestCommand;
import com.identity4j.connector.aws.command.user.AttachPolicyToUserRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserPasswordRequestCommand;
import com.identity4j.connector.aws.command.user.CreateUserRequestCommand;
import com.identity4j.connector.aws.command.user.DeleteUserRequestCommand;
import com.identity4j.connector.aws.command.user.ListUserAttachedPolicyRequestCommand;
import com.identity4j.connector.aws.command.user.ListUsersRequestCommand;
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
	public static <T extends RequestCommand<?>> T get(Class<T> type, String marker, Map<String, String> options) {
		
		String classSimpleName = type.getSimpleName();
		
		switch (classSimpleName) {
			case "ListUsersRequestCommand":
				return (T) new ListUsersRequestCommand(marker);
				
			case "CreateUserRequestCommand":
				return (T) new CreateUserRequestCommand(options);
				
			case "CreateUserPasswordRequestCommand":
				return (T) new CreateUserPasswordRequestCommand(options);
				
			case "UpdateUserPasswordRequestCommand":
				return (T) new UpdateUserPasswordRequestCommand(options);
				
			case "RemoveUserPasswordRequestCommand":
				return (T) new RemoveUserPasswordRequestCommand(options);
				
			case "DeleteUserRequestCommand":
				return (T) new DeleteUserRequestCommand(options);
				
			case "ListGroupsRequestCommand":
				return (T) new ListGroupsRequestCommand(marker);
				
			case "CreateGroupRequestCommand":
				return (T) new CreateGroupRequestCommand(options);
				
			case "DeleteGroupRequestCommand":
				return (T) new DeleteGroupRequestCommand(options);
				
			case "ListPoliciesRequestCommand":
				return (T) new ListPoliciesRequestCommand(marker);
				
			case "AttachPolicyToUserRequestCommand":
				return	(T) new AttachPolicyToUserRequestCommand(options);
				
			case "ListUserAttachedPolicyRequestCommand":
				return	(T) new ListUserAttachedPolicyRequestCommand(options, marker);
				
			case "AttachPolicyToGroupRequestCommand":
				return	(T) new AttachPolicyToGroupRequestCommand(options);
				
			case "ListGroupAttachedPolicyRequestCommand":
				return (T) new ListGroupAttachedPolicyRequestCommand(options, marker);	
			
			default:
				break;
		}
		return null;
		
	}
}
