/* HEADER */
package com.identity4j.connector;

import java.util.Iterator;

/**
 * Specialisation of {@link Iterator} that also returns a <strong>Tag</strong>.
 *
 * @param <E> type of result
 * @see {@link TagResult}
 */
public interface ResultIterator<E> extends Iterator<E>, TagResult {
}
