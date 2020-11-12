package com.identity4j.connector.aws.command;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import software.amazon.awssdk.services.iam.IamClient;

public class RequestResultIterator<T, C extends RequestCommand<?>> implements Iterator<T> {
	
	private CommandResult<T> commandResult = null;
	private Class<C> type;
	private IamClient client;
	private Map<String, String> options = Collections.emptyMap();
	
	public RequestResultIterator(Class<C> type, IamClient client) {
		this.type = type;
		this.client = client;
	}
	
	public RequestResultIterator(Class<C> type, IamClient client, Map<String, String> options) {
		this.type = type;
		this.client = client;
		this.options = options;
	}

	@Override
	public boolean hasNext() {
		
		if (commandResult == null) {
			commandResult = fetch();
		}
		
		boolean hasNext = commandResult.getIterator().hasNext();
		
		if (!commandResult.isDone() && !hasNext) {
			hasNext = false;
			commandResult = fetch();
			if (commandResult != null) {
				hasNext = commandResult.getIterator().hasNext();
			}
		}
		
		return hasNext;
	}

	@Override
	public T next() {
		return commandResult.getIterator().next();
	}
	
	@SuppressWarnings("unchecked")
	private CommandResult<T> fetch() {
		RequestCommand<?> command = (RequestCommand<?>) CommandFactory.get(type, commandResult == null ? null : commandResult.getMarker(), options);
		return (CommandResult<T>) command.execute(client);
	}
}
