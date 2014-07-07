package com.identity4j.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractTransformingIterator<E, O> implements Iterator<O> {

    private Iterator<E> sourceIterator;
    private O next;

    public AbstractTransformingIterator(Iterator<E> sourceIterator) {
        this.sourceIterator = sourceIterator;
    }

    @Override
    public boolean hasNext() {
        checkNext();
        return next != null;
    }

    @Override
    public O next() {
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
                } else if (n != null) {
                    O enext = transform(n);
                    if(enext != null) {
                        next = enext;
                        break;
                    }
                }
            }
        }
    }

    protected abstract O transform(E object);

}
