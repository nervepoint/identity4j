/* HEADER */
package com.identity4j.connector.vfs;

/*
 * #%L
 * Identity4J VFS
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


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.exception.ConnectorException;

public abstract class AbstractVFSConnector<P extends AbstractVFSConfiguration> extends AbstractConnector<P> {
    private FileSystemManager fsManager;
    private FileObject file;

    @Override
    public boolean isOpen() {
        return file != null;
    }

    @Override
    public boolean isReadOnly() {
        try {
			return file.isWriteable();
		} catch (FileSystemException e) {
			return true;
		}
    }
    
    @Override
    protected void onOpen(P config) throws ConnectorException {
        try {
            fsManager = VFS.getManager();
            file = fsManager.resolveFile(config.getUri());
        } catch (FileSystemException e) {
            throw new ConnectorException("Failed to get VFS manager.", e);
        }
    }
    
    public FileSystemManager getFileSystemManager() {
        return fsManager;
    }
    
    public FileObject getFile() {
        return file;
    }

}