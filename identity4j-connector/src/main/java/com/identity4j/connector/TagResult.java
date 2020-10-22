/* HEADER */
package com.identity4j.connector;

/**
 * Some methods in {@link Connector} will return a <code>Tag</code> that may be used for
 * more efficient retrieval of remote user databases, only retrieving details that have
 * changed since the last known tag (e.g a timestamp string).
 * <p>
 * This interface is implement by {@link Count} and {@link ResultIterator}, as both
 * return these tags.
 */
public interface TagResult {
	/**
	 * Get the tag.
	 * 
	 * @return tag
	 */
	String tag();
}
