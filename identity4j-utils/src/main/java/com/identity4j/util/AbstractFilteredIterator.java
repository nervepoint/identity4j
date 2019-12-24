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
