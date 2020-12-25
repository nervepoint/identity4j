package com.identity4j.connector.aws;

import java.io.Serializable;
import java.util.List;

import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.User;

public class AwsGroup implements Serializable {

	private static final long serialVersionUID = 7956938776764193910L;

	private Group group;
	private List<User> users;
	
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	
}
