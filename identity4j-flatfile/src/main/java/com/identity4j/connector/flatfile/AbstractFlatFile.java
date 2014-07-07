package com.identity4j.connector.flatfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractFlatFile {
    static final Log LOG = LogFactory.getLog(AbstractFlatFile.class);

    // Internal representation of file content
    private final List<List<String>> contents = new ArrayList<List<String>>();
    private final Map<Integer, Map<String, List<String>>> index = new HashMap<Integer, Map<String, List<String>>>();
    private boolean firstRowIsHeading;
    private final List<Column> columns = new ArrayList<Column>();
    private char escapeCharacter;
    private Filter filter;

    /**
     * Get the entire file content model
     * 
     * @return all rows
     */
    public List<List<String>> getContents() {
        return contents;
    }

    /**
     * Get the filter
     * 
     * @return filter
     */
    public final Filter getFilter() {
        return filter;
    }

    /**
     * Set the filter
     * 
     * @param filter filter
     */
    public final void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Set the escape character to use when encoding values. Use a value of
     * <code>(char)-1</code> to disable escaping
     * 
     * @param escapeCharacter escape character
     */
    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    /**
     * Get the escape character to use when encoding values. A value of
     * <code>(char)-1</code> indicates escaping is disabled
     * 
     * @return escape character
     */
    public char getEscapeCharacter() {
        return escapeCharacter;
    }

    public void removeAllColumns() {
        columns.clear();
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public Collection<Column> getColumns() {
        return columns;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Column getColumn(int column) {
        return columns.get(column);
    }

    public boolean isFirstRowIsHeading() {
        return firstRowIsHeading;
    }

    public void setFirstRowIsHeading(boolean firstRowIsHeading) {
        this.firstRowIsHeading = firstRowIsHeading;
    }

    public void addIndex(int column) {
        if (index.containsKey(column)) {
            throw new IllegalArgumentException("Index already exists");
        }
        index.put(column, new HashMap<String, List<String>>());
    }

    public Map<Integer, Map<String, List<String>>> getIndex() {
        return index;
    }

    /**
     * Given a key find the associated row
     * 
     * @param indexColumn
     * @param keyFieldValue
     * @return
     */
    public List<String> getRowByKeyField(int indexColumn, String keyFieldValue) {
        final Map<String, List<String>> map = index.get(indexColumn);
        return map.get(keyFieldValue);
    }

    public void clear() {
        contents.clear();
        for (Map<String, List<String>> rowIndex : index.values()) {
            rowIndex.clear();
        }
    }

    /**
     * Load the file and the using character encoding
     * 
     * @param inputStream
     * @param charsetName
     * @throws IOException
     */
    public final void load(InputStream inputStream, String charsetName) throws IOException {
        clear();
        onLoad(inputStream, charsetName);
    }

    /**
     * Wrapper around writeRow to write a single row into the file
     * 
     * @param row to write
     * @throws IOException
     */
    public void appendRow(List<String> row) throws IOException {
        writeRows(Collections.singletonList(row), true);
    }

    /**
     * Wrapper around writeRows to write many rows to the file
     * 
     * @throws IOException
     */
    public void writeRows() throws IOException {
        writeRows(contents, false);
    }

    /**
     * write lots of rows to a given file
     * 
     * @param rows
     * @param append to file or overwrite
     * @throws IOException
     */
    protected abstract void writeRows(List<List<String>> rows, boolean append) throws IOException;

    /**
     * Low level write method which simply writes the given content to file
     * 
     * @param out
     * @param row
     * @param encoding
     * @throws IOException
     */
    protected void write(OutputStream out, List<String> row, String encoding) throws IOException {
        synchronized (out) {
            IOUtils.writeLines(Collections.singletonList(rowToString(row)), null, out);
        }
    }

    /**
     * Convert the row to the desired string format
     * 
     * @param row to convert
     * @return
     */
    protected abstract String rowToString(List<String> row);

    protected abstract void onLoad(InputStream inputStream, String charsetName) throws IOException;

    /**
     * Local class that represents a column. A file can be seen as rows
     * (horizontal lines of text) and columns of text. A column is separated by
     * spaces
     * 
     * 
     */
    public class Column {
        private int width;
        private String heading;

        public Column(String heading, int width) {
            this.width = width;
            this.heading = heading;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            this.heading = heading;
        }
    }

    public boolean add(List<String> row) {
        if (filter == null || filter.include(row)) {
            if (!contents.add(row)) {
                return false;
            } else {
                for (Integer indexColumn : index.keySet()) {
                    if (row.size() > indexColumn) {
                        Map<String, List<String>> rowMap = index.get(indexColumn);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding index " + indexColumn + "/" + row.get(indexColumn));
                        }
                        rowMap.put(row.get(indexColumn), row);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean remove(List<String> row) {
        if (!contents.remove(row)) {
            return false;
        } else {
            for (Integer indexColumn : index.keySet()) {
                if (row.size() > indexColumn) {
                    Map<String, List<String>> rowMap = index.get(indexColumn);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removing index " + indexColumn + "/" + row.get(indexColumn));
                    }
                    rowMap.remove(row.get(indexColumn));
                }
            }
        }
        return true;
    }

    /**
     * Get stored rows. A row represents a line in a file which is now
     * internally captured within a map
     * 
     * @return row identifier, row
     */
    private Map<String, List<String>> getAllRows() {

        // determine which is the row index and use it to get the rows out
        Integer rowIndex = getRowIndex();
        return index.get(rowIndex);
    }

    /**
     * Get the index number which represents the row data in the map
     * 
     * @return
     */
    private Integer getRowIndex() {
        return index.keySet().iterator().next();
    }

    /**
     * Remove a row item
     * 
     * @param o
     * @return
     */
    public boolean remove(String principalName) {
        final List<String> row = getRowByKeyField(getRowIndex(), principalName);
        if (!contents.remove(row)) {
            return false;
        }
        getAllRows().remove(principalName);
        return true;
    }

    /**
     * Get size of internal array
     * 
     * @return
     */
    public int size() {
        return contents.size();
    }

    /**
     * Load the content of a file
     * 
     * @param file
     * @param charset
     * @throws IOException
     */
    public void load(File file, String charsetName) throws IOException {
        LOG.info("Loading flat file " + file);

        InputStream in = new FileInputStream(file);
        try {
            load(in, charsetName);

        } finally {
            in.close();
        }
    }
}