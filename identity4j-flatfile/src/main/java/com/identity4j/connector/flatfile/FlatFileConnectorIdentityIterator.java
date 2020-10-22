/**
 * 
 */
package com.identity4j.connector.flatfile;

/*
 * #%L
 * Identity4J Flat File
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
import java.util.List;
import java.util.NoSuchElementException;

import com.identity4j.connector.principal.Identity;

public class FlatFileConnectorIdentityIterator implements Iterator<Identity> {

    private DelimitedFlatFile flatFile;
    private int keyFieldIndex;
    private AbstractFlatFileConnector<?> connector;

    FlatFileConnectorIdentityIterator(DelimitedFlatFile flatFile, int keyFieldIndex, AbstractFlatFileConnector<?> connector) {
        this.flatFile = flatFile;
        this.keyFieldIndex = keyFieldIndex;
        this.connector = connector;
    }

    private int row = 0;

    public boolean hasNext() {
        return row < flatFile.size();
    }

    public Identity next() {
        List<List<String>> contents = flatFile.getContents();
        if(row >= contents.size())
        	throw new NoSuchElementException("No more identities.");
		List<String> list = contents.get(row++);
        String keyFieldValue = list.get(keyFieldIndex);
        return connector.getIdentityByName(keyFieldValue);
    }

    public void remove() {
        flatFile.getContents().remove(row);
    }
}