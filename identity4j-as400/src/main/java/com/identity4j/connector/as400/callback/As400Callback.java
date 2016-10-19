/* HEADER */
package com.identity4j.connector.as400.callback;

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