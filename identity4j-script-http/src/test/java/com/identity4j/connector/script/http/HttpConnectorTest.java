/* HEADER */
package com.identity4j.connector.script.http;

/*
 * #%L
 * Identity4J Scripted HTTP Connector
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

import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;


@SuppressWarnings("rawtypes")
public class HttpConnectorTest extends AbstractConnectorTest {
    public HttpConnectorTest() {
        super("/script-http-connector.properties");
    }

    @Test
    public void test() {
        // Makes Eclipse think it can test this class
    }
}
