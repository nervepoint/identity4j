package com.identity4j.connector.aws.command;

import java.util.Iterator;

public class CommandResult<T> {
	boolean done;
	String marker;
	Iterator<T> iterator;
	T result;
	
	public CommandResult(boolean done, String marker, T result) {
		super();
		this.done = done;
		this.marker = marker;
		this.result = result;
	}
	
	public CommandResult(boolean done, String marker, Iterator<T> iterator) {
		super();
		this.done = done;
		this.marker = marker;
		this.iterator = iterator;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}
	
	public String getMarker() {
		return marker;
	}
	
	public void setMarker(String marker) {
		this.marker = marker;
	}
	
	public Iterator<T> getIterator() {
		return iterator;
	}
	
	public void setIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
	
	
}
