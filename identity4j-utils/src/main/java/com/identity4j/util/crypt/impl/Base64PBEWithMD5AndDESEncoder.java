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


public class Base64PBEWithMD5AndDESEncoder extends CompoundEncoder {

    public final static String ID = PBEWithMD5AndDESEncoder.ID + "-base64";

    public Base64PBEWithMD5AndDESEncoder() {
        super(ID);
        addEncoder(new PBEWithMD5AndDESEncoder());
        addEncoder(new Base64Encoder());
    }
    
    public static void main(String[] args) {
        Base64PBEWithMD5AndDESEncoder d = new Base64PBEWithMD5AndDESEncoder();
        System.out.println(new String(d.decode(args[0].getBytes(), null, new StringBuilder(args[1]).reverse().toString().getBytes(), "UTF-8")));
    }
}
