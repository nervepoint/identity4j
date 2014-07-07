/* HEADER */
package com.identity4j.connector.flatfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class LocalFixedWidthFlatFile extends FixedWidthFlatFile {

    private File file;
    private String charsetName;
    private long lastModified;

    /**
     * If the file is read/write this constructor should be used
     * 
     * @param file
     * @param charsetName
     */
    public LocalFixedWidthFlatFile(File file, String charsetName) {
        super();
        this.file = file;
        this.charsetName = charsetName;
    }


    /**
     * If the file is read/write this constructor should be used
     * 
     * @param file
     * @param charsetName
     */
    public LocalFixedWidthFlatFile(String charsetName) {
        this.charsetName = charsetName;
    }

    @Override
    public void clear() {
        lastModified = -1;
        super.clear();
    }

    public boolean isStale() {
        return lastModified == -1 || file.exists() && file.lastModified() != lastModified;
    }

    public void load() throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        load(file, charsetName);
        lastModified = file.lastModified();
    }

    public File getFile() {
        return file;
    }

    public void reloadIfStale() throws IOException {
        if (isStale()) {
            load();
        }
    }

    @Override
    protected void writeRows(List<List<String>> rows, boolean append) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        OutputStream output = new FileOutputStream(file, append);
        try {

            for (List<String> row : rows) {
                write(output, row, charsetName);
            }
            lastModified = file.lastModified();
        } finally {
            output.close();
        }
    }

}
