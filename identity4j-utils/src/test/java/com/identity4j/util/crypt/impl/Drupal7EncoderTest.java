package com.identity4j.util.crypt.impl;

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

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Drupal7EncoderTest extends AbstractEncoderTest {

    public Drupal7EncoderTest() throws UnsupportedEncodingException {
        super(Drupal7Encoder.ID,
                        new byte[][] { "$S$DnO4ij9KOjnBioZhI6.t.JLitZVShF7bkN/fFbUaua8nf27yTsc2".getBytes("UTF-8"),
                                        "$S$D3M39kOc.7Z1EpCad8FZfeTBJqFWyDfuMdxZuZFptqDL8HZKuz7x".getBytes("UTF-8"),
                                        "$S$Dl7IOt27lwHIIEvpCFjJnnE2qkIKaiYx8MXJxH9NxH/kN.e1BAwC".getBytes("UTF-8") },
                        new byte[][] { "$S$DnO4ij9KO".getBytes("UTF-8"), "$S$D3M39kOc.".getBytes("UTF-8"),
                                        "$S$Dl7IOt27l".getBytes("UTF-8") },
                        null, false, true);

    }

}
