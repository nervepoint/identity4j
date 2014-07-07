/* HEADER */
package com.identity4j.connector.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;

public abstract class AbstractVFSConnector extends AbstractConnector {
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
    protected void onOpen(ConnectorConfigurationParameters config) throws ConnectorException {
        try {
            fsManager = VFS.getManager();
            file = fsManager.resolveFile(((AbstractVFSConfiguration)config).getUri());
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