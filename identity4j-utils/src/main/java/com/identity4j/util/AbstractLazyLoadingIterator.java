/* HEADER */
package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


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