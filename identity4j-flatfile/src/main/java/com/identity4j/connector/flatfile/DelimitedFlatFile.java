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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.util.StringUtil;

/**
 * File deliminated by a comma
 */
public abstract class DelimitedFlatFile extends AbstractFlatFile {
    final static Log LOG = LogFactory.getLog(DelimitedFlatFile.class);
    private char fieldSeparator;

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * Load up a flatfile that is deliminated by field seperator
     */
    @Override
    public void onLoad(InputStream inputStream, String charsetName) throws IOException {
        LineIterator it = IOUtils.lineIterator(inputStream, charsetName);

        // take line at a time and parse
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                LOG.debug("Parsing " + line);
                List<String> row = new ArrayList<String>();
                char fs= getFieldSeparator();
                char esc = getEscapeCharacter();
                int len = line.length();
                char c;
                boolean escaped = false;
                StringBuilder col = new StringBuilder(line.length());
                for(int i = 0 ; i < len ; i++) {
                    c = line.charAt(i);
                    if(c == fs && !escaped) {
                        row.add(col.toString());
                        col.setLength(0);
                    }
                    else {
                        if(esc != -1 && c == esc) {
                            escaped = true;
                        }
                        else {
                            col.append(c);
                            escaped = false;
                        }
                    }
                }
                row.add(col.toString());

                if (isFirstRowIsHeading() && size() == 0) {

                    // add line to internal object called index
                } else {
                    add(row);
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    /**
     * Format row by adding seperator to the end of each column (i.e col1, col2,
     * col3)
     * 
     */
    @Override
    protected String rowToString(List<String> row) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> itr = row.iterator(); itr.hasNext();) {
            builder.append(escape(StringUtil.nonNull(itr.next())));
            // add separator except to the last item
            if (itr.hasNext()) {
                builder.append(getFieldSeparator());
            }
        }
        return builder.toString();
    }
    
    public String escape(String text) {
        char esc = getEscapeCharacter();
        if(esc == -1) {
            return text;
        }
        int len = text.length();
        StringBuilder builder = new StringBuilder();
        char c;
        for(int i = 0 ; i < len ; i++) {
            c = text.charAt(i);
            if(c == getFieldSeparator()) {
                builder.append(getEscapeCharacter());
            }
            builder.append(c);
        }
        return builder.toString();
    }
}