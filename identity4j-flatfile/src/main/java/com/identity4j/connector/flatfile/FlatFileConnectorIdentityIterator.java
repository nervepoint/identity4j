/**
 * 
 */
package com.identity4j.connector.flatfile;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.identity4j.connector.Connector;
import com.identity4j.connector.principal.Identity;

public class FlatFileConnectorIdentityIterator implements Iterator<Identity> {

    private DelimitedFlatFile flatFile;
    private int keyFieldIndex;
    private Connector connector;

    FlatFileConnectorIdentityIterator(DelimitedFlatFile flatFile, int keyFieldIndex, Connector connector) {
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