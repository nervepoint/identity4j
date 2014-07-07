package com.identity4j.util.expect;

public class DefaultExpectMatcher implements ExpectMatcher {

	public boolean matches(String line, String pattern) {
		return line.indexOf(pattern) > -1;
	}

}
