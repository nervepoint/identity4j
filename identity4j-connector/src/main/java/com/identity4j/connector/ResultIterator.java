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
	static <E> ResultIterator<E> createDefault(Iterator<E> it, String tag) {
		return new ResultIterator<E>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public E next() {
				return it.next();
			}

			@Override
			public String tag() {
				return tag;
			}
		};
	}
	
	
	static <E> ResultIterator<E> createDefault(String tag) {
		return new ResultIterator<E>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public E next() {
				throw new IllegalStateException();
			}

			@Override
			public String tag() {
				return tag;
			}
		};
	}
}
