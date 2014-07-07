package com.identity4j.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractFilteredIterator<E> implements Iterator<E> {

    private Iterator<E> sourceIterator;
    private E next;

    public AbstractFilteredIterator(Iterator<E> sourceIterator) {
        this.sourceIterator = sourceIterator;
    }

    @Override
    public boolean hasNext() {
        checkNext();
        return next != null;
    }

    @Override
    public E next() {
        checkNext();
        if (next == null) {
            throw new NoSuchElementException();
        }
        try {
            return next;
        } finally {
            next = null;
        }
    }

    @Override
    public void remove() {
    }

    private void checkNext() {
        if (next == null) {
            while (sourceIterator.hasNext()) {
                E n = sourceIterator.next();
                if (n == null) {
                    break;
                } else if (n != null && include(n)) {
                    next = n;
                    break;
                }
            }
        }
    }

    protected abstract boolean include(E object);

}
