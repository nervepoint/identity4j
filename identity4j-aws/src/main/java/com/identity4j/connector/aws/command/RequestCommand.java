package com.identity4j.connector.aws.command;

import software.amazon.awssdk.services.iam.IamClient;

public abstract class RequestCommand<T> {
	
	protected String marker;
	
	public RequestCommand(String marker) {
		this.marker = marker;
	}
	
	public abstract CommandResult<T> execute(IamClient client);
}
