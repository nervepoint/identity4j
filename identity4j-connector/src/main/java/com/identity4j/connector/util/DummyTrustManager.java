package com.identity4j.connector.util;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Provides an implementation of the <code>X509TrustManager</code> which does
 * not validate the supplied certificate.
 */
public class DummyTrustManager implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] cert, String authType) {
        // nothing to do
    }

    public void checkServerTrusted(X509Certificate[] cert, String authType) {
        // nothing to do
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}