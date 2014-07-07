package com.identity4j.util.expect;

public class RegularExpressionExpectMatcher implements ExpectMatcher {

	public RegularExpressionExpectMatcher() {
	}

	@Override
	public boolean matches(String line, String pattern) {
		return line.matches(pattern);
	}

}
