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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Flatfile where the columns are seperated by space and each column has a fixed
 * width.
 * 
 * 
 */
public abstract class FixedWidthFlatFile extends AbstractFlatFile {

    final static Log LOG = LogFactory.getLog(FixedWidthFlatFile.class);

    private boolean autoDetermineWidths;

    public boolean isAutoDetermineWidths() {
        return autoDetermineWidths;
    }

    /**
     * Determine the size of each column
     * 
     * @param autoDetermineWidths
     */
    public void setAutoDetermineWidths(boolean autoDetermineWidths) {
        this.autoDetermineWidths = autoDetermineWidths;
    }

    @Override
    public void clear() {
        if (isAutoDetermineWidths()) {
            removeAllColumns();
        }
        super.clear();
    }

    /**
     * Load file using an equal column size to detemrine where the end of one
     * column is and the where the other starts
     * 
     */
    @Override
    public void onLoad(InputStream inputStream, String charsetName) throws IOException {
        try(LineIterator it = IOUtils.lineIterator(inputStream, charsetName)) {
            while (it.hasNext()) {
                String line = it.nextLine();
                LOG.debug("Parsing " + line);

                // Automatically determine column widths
                if (size() == 0 && isAutoDetermineWidths() && getColumnCount() == 0) {
                    autoDeterminColumns(line);
                    if (isFirstRowIsHeading()) {
                        continue;
                    }
                }

                if (getColumns().size() == 0) {
                    throw new IOException("No column widths set. Either use addColumn() or setAutoDetermineWidths()");
                }

                // work out the length of each column and split out each column
                int idx = 0;
                List<String> row = new ArrayList<String>();
                for (Column col : getColumns()) {
                    if (idx < line.length()) {
                        int eidx = idx + col.getWidth();
                        if (row.size() == getColumnCount() - 1 && eidx < line.length()) {
                            eidx = line.length();
                        }
                        String colVal = line.substring(idx, eidx).trim();
                        row.add(colVal);
                        idx += col.getWidth();
                    } else {
                        throw new IOException("Too few columns on line " + size());
                    }
                }

                // once the file has be parsed using fixed width columns save
                // them internally
                Map<Integer, Map<String, List<String>>> index = getIndex();
                for (Integer indexColumn : index.keySet()) {
                    if (row.size() > indexColumn) {
                        Map<String, List<String>> rowMap = index.get(indexColumn);
                        if (rowMap == null) {
                            rowMap = new HashMap<String, List<String>>();
                            index.put(indexColumn, rowMap);
                        }
                        LOG.debug("Adding index " + indexColumn + "/" + row.get(indexColumn));
                        rowMap.put(row.get(indexColumn), row);
                        add(row);
                    }

                }
            }
        } 
    }

    /**
     * Work out where the columns are in the file by deliminating by ' '
     * 
     * @param line
     * @throws IOException
     */
    private void autoDeterminColumns(String line) throws IOException {

        if (getColumns().size() != 0) {
            throw new IOException("Column already set. Either clear the columns or turn off autoDeterminWidths()");
        }

        int sp = -1;
        StringBuilder col = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == ' ') {
                if (sp == -1) {
                    col.append(ch);
                } else {
                    sp++;
                }
            } else {
                if (sp > 1) {
                    LOG.debug("Adding column " + col + " (width of " + (col.length() + sp));
                    addHeading(col, sp);
                    col.setLength(0);
                }
                sp = 0;
                col.append(ch);
            }
        }
        if (col.length() > 0) {
            addHeading(col, sp);
        }
    }

    /**
     * Store column
     * 
     * @param column name
     * @param width
     */
    private void addHeading(StringBuilder col, int sp) {
        String heading = null;
        if (isFirstRowIsHeading()) {
            heading = col.toString();
        }
        addColumn(new Column(heading, col.length() + sp));
        col.setLength(0);
    }

    /**
     * Format row into a string adding extra buffered ' ' to fill the width of
     * the column
     */
    @Override
    protected String rowToString(List<String> row) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> it = row.iterator();

        for (Column column : getColumns()) {
            int width = column.getWidth();
            String attribute = it.next();

            for (int i = width - attribute.length(); i > 0; i--) {
                attribute = attribute + ' ';
            }

            builder.append(attribute);
        }
        return builder.toString();
    }

}
