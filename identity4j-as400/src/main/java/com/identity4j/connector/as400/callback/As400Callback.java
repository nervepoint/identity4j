/* HEADER */
package com.identity4j.connector.as400.callback;

/*
 * #%L
 * Identity4J AS400
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


import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.RequestNotSupportedException;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalNotFoundException;

public abstract class As400Callback<T> {

    public final T execute() {
        try {
            return executeInCallback();
        } catch (AS400Exception ase) {
            throw new ConnectorException("Failed to execute():" + ase.getMessage(), ase);
        } catch (AS400SecurityException ase) {
            throw new ConnectorException("Failed to execute():" + ase.getMessage(), ase);
        } catch (ErrorCompletingRequestException ecre) {
            throw new ConnectorException("Failed to execute():" + ecre.getMessage(), ecre);
        } catch (InterruptedException ire) {
            throw new ConnectorException("Failed to execute():" + ire.getMessage(), ire);
        } catch (IOException ioe) {
            throw new ConnectorException("Failed to execute():" + ioe.getMessage(), ioe);
        } catch (PrincipalNotFoundException pnf) {
            throw new PrincipalNotFoundException("Failed to execute():" + pnf.getMessage(), pnf);
        } catch (ObjectDoesNotExistException odnee) {
            throw new PrincipalNotFoundException("Failed to execute():" + odnee.getMessage(), odnee);
        } catch (RequestNotSupportedException rnse) {
            throw new ConnectorException("Failed to execute():" + rnse.getMessage(), rnse);
        } catch (PropertyVetoException pve) {
            throw new ConnectorException("Failed to execute():" + pve.getMessage(), pve);
        } catch (Exception pve) {
            throw new ConnectorException("Failed to execute():" + pve.getMessage(), pve);
        }
    }

    protected abstract T executeInCallback() throws Exception;
}