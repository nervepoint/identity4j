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