/**
 * 
 */
package com.identity4j.connector.flatfile;

import java.util.List;
import java.util.NoSuchElementException;

import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.principal.Identity;

public class FlatFileConnectorIdentityIterator implements ResultIterator<Identity> {

    private DelimitedFlatFile flatFile;
    private int keyFieldIndex;
    private AbstractFlatFileConnector<?> connector;
	private String tag;

    FlatFileConnectorIdentityIterator(DelimitedFlatFile flatFile, int keyFieldIndex, AbstractFlatFileConnector<?> connector, String tag) {
        this.flatFile = flatFile;
        this.keyFieldIndex = keyFieldIndex;
        this.connector = connector;
        this.tag = tag;
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

	@Override
	public String tag() {
		return tag;
	}
}