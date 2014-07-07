package com.identity4j.util.expect;

public interface ExpectMatcher {

    /**
     * Match a command output line against a defined pattern.
     * @param line the line of output to search
     * @param pattern the pattern required
     * @return boolean
     */
    public boolean matches(String line, String pattern);
}
