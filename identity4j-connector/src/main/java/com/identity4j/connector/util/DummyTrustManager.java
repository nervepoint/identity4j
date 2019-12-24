package com.identity4j.connector.util;

/*
 * #%L
 * Identity4J Connector
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