/* HEADER */
package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    private final static Version[] versions = new Version[] {
                    new Version("0.0.0"),
                    new Version("0.0.1"),
                    new Version("0.2.0"),
                    new Version("0.2.3-alpha"),
                    new Version("0.2.3-beta"),
                    new Version("0.2.3-rc1"),
                    new Version("0.2.3-rc2"),
                    new Version("0.2.3-ga1"),
                    new Version("0.2.3-ga2"),
                    new Version("0.2.3"),
                    new Version("3.2.1"),
                    new Version("6.1.4"),
                    new Version("9.8.7-beta1"),
                    new Version("9.8.7-beta2"),
                    new Version("9.8.7-beta5"),
                    new Version("9.8.7")
    };

    @Test
    public void testVersionComparison() {
        Version[] mixedVersions = new Version[versions.length];
        System.arraycopy(versions, 0, mixedVersions, 0, versions.length);
        for(int i = 0 ; i < 500 ; i++) {
            int a1 = (int)(Math.random() * mixedVersions.length);
            int a2 = (int)(Math.random() * mixedVersions.length);
            Version swap = mixedVersions[a1]; 
            mixedVersions[a1] = mixedVersions[a2];
            mixedVersions[a2] = swap;
        }        
        List<Version> listMixedVersions = Arrays.asList(mixedVersions);
        Collections.sort(listMixedVersions);
        listMixedVersions.toArray(mixedVersions);
        Assert.assertArrayEquals(versions, mixedVersions);
    }
}