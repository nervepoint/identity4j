/* HEADER */
package com.identity4j.connector.as400.callback;

public abstract class As400CallbackWithoutResult extends As400Callback<Object> {

    @Override
    protected final Object executeInCallback() throws Exception {
        executeInCallbackWithoutResult();
        return null;
    }

    protected abstract void executeInCallbackWithoutResult() throws Exception;
}