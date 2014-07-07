package com.identity4j.connector.flatfile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

public class LocalDelimitedFlatFile extends DelimitedFlatFile {

    private FileObject file;
    private String charsetName;
    private long lastModified;

    public LocalDelimitedFlatFile(FileObject file, String charsetName) {
        super();
        this.file = file;
        this.charsetName = charsetName;
    }

    @Override
    public void clear() {
        lastModified = -1;
        super.clear();
    }

    public boolean isStale() {
        try {
            return file.exists() && file.getContent().getLastModifiedTime() != lastModified;
        } catch (FileSystemException e) {
            return false;
        }
    }

    public void load() throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getName().getURI());
        }
        load(file.getContent().getInputStream(), charsetName);
        lastModified = file.getContent().getLastModifiedTime();
    }

    public FileObject getFile() {
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
            throw new FileNotFoundException(file.getName().getURI());
        }
        OutputStream output = getFile().getContent().getOutputStream(append);
        try {

            for (List<String> row : rows) {
                write(output, row, charsetName);
            }
            lastModified = file.getContent().getLastModifiedTime();
        } finally {
            output.close();
        }
    }
}