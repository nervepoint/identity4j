/* HEADER */
package com.identity4j.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation an {@link java.util.Iterator} which uses the <a
 * href="http://en.wikipedia.org/wiki/Lazy_loading">lazy loading</a> pattern to
 * retrieve one batch of results at a time.
 * 
 * @param <T>
 */
public abstract class AbstractLazyLoadingIterator<T extends Object> implements Iterator<T> {
    private Iterator<T> itr;
    private T nextItem;

    public final boolean hasNext() {
        lazyInitialise();
        if (itr != null && itr.hasNext()) {
            return true;
        }
        if (nextItem == null) {
            return false;
        }
        itr = fetchNext();
        return itr != null && itr.hasNext();
    }

    public final T next() {
        lazyInitialise();
        if (itr == null) {
            throw new NoSuchElementException("iterator has no more elements");
        }
        T next = itr.next();
        this.nextItem = next;
        onNext(next);
        return next;
    }

    private void lazyInitialise() {
        if (itr == null) {
            itr = fetchNext();
        }
    }

    protected void onNext(T next) {
        // template method, provided for sub-classes to override
    }

    public void remove() {
        throw new UnsupportedOperationException("remove is not supported on this Iterator");
    }

    protected abstract Iterator<T> fetchNext();
}