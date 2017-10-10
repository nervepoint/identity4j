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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.MultiMapException;

public class MultiMapTest {
    private static final String MAP_KEY = "keyName";

    @Test
    public void getStringNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals("", multiMap.getString(MAP_KEY));
    }

    @Test
    public void getStringNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals("", multiMap.getString(MAP_KEY));
    }

    @Test
    public void getStringEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getString(MAP_KEY));
    }

    @Test
    public void getStringWithValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getString(MAP_KEY));
    }

    @Test
    public void getStringOrDefaultNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals("", multiMap.getStringOrDefault(MAP_KEY, ""));
    }

    @Test
    public void getStringOrDefaultNullValue() {
        final String defaultValue = "default";
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(defaultValue, multiMap.getStringOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getStringOrDefaultEmptyValue() {
        final String value = "";
        final String defaultValue = "default";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getStringOrDefaultWithValue() {
        final String value = "value";
        final String defaultValue = "default";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getStringOrNullNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(null, multiMap.getStringOrNull(MAP_KEY));
    }

    @Test
    public void getStringOrNullNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(null, multiMap.getStringOrNull(MAP_KEY));
    }

    @Test
    public void getStringOrNullEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrNull(MAP_KEY));
    }

    @Test
    public void getStringOrNullWithValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrNull(MAP_KEY));
    }

    @Test(expected = MultiMapException.class)
    public void getStringOrFailNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        multiMap.getStringOrFail(MAP_KEY);
    }

    @Test(expected = MultiMapException.class)
    public void getStringOrFailNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        multiMap.getStringOrFail(MAP_KEY);
    }

    @Test
    public void getStringOrFailEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrFail(MAP_KEY));
    }

    @Test
    public void getStringOrFailWithValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(value, multiMap.getStringOrFail(MAP_KEY));
    }

    @Test
    public void getStringArrayNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertArrayEquals(new String[] {}, multiMap.getStringArray(MAP_KEY));
    }

    @Test
    public void getStringArrayNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertArrayEquals(new String[] {}, multiMap.getStringArray(MAP_KEY));
    }

    @Test
    public void getStringArrayEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { "" }, multiMap.getStringArray(MAP_KEY));
    }

    @Test
    public void getStringArrayWithValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { value }, multiMap.getStringArray(MAP_KEY));
    }

    @Test
    public void getStringArrayWithValues() {
        final String[] values = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap(values);
        assertArrayEquals(values, multiMap.getStringArray(MAP_KEY));
    }

    @Test
    public void getStringArrayOrDefaultNoValue() {
        final String[] defaultValues = { "valueOne", "valueTwo" };
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertArrayEquals(defaultValues, multiMap.getStringArrayOrDefault(MAP_KEY, defaultValues));
    }

    @Test
    public void getStringArrayOrDefaultNullValue() {
        final String[] defaultValues = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertArrayEquals(defaultValues, multiMap.getStringArrayOrDefault(MAP_KEY, defaultValues));
    }

    @Test
    public void getStringArrayOrDefaultEmptyValue() {
        final String value = "";
        final String[] defaultValues = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { "" }, multiMap.getStringArrayOrDefault(MAP_KEY, defaultValues));
    }

    @Test
    public void getStringArrayOrDefaultWithValue() {
        final String value = "value";
        final String[] defaultValues = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { value }, multiMap.getStringArrayOrDefault(MAP_KEY, defaultValues));
    }

    @Test
    public void getStringArrayOrDefaultWithValues() {
        final String[] values = { "valueOne", "valueTwo" };
        final String[] defaultValues = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap(values);
        assertArrayEquals(values, multiMap.getStringArrayOrDefault(MAP_KEY, defaultValues));
    }

    @Test
    public void getStringArrayOrNullNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertArrayEquals(null, multiMap.getStringArrayOrNull(MAP_KEY));
    }

    @Test
    public void getStringArrayOrNullNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertArrayEquals(null, multiMap.getStringArrayOrNull(MAP_KEY));
    }

    @Test
    public void getStringArrayOrNullEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { "" }, multiMap.getStringArrayOrNull(MAP_KEY));
    }

    @Test
    public void getStringArrayOrNullWithValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertArrayEquals(new String[] { value }, multiMap.getStringArrayOrNull(MAP_KEY));
    }

    @Test
    public void getStringArrayOrNullWithValues() {
        final String[] values = { "valueOne", "valueTwo" };
        MultiMap multiMap = buildMultiMap(values);
        assertArrayEquals(values, multiMap.getStringArrayOrNull(MAP_KEY));
    }

    @Test
    public void getBooleanNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(Boolean.FALSE, multiMap.getBoolean(MAP_KEY));
    }

    @Test
    public void getBooleanNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(Boolean.FALSE, multiMap.getBoolean(MAP_KEY));
    }

    @Test
    public void getBooleanEmptyValue() {
        MultiMap multiMap = buildMultiMap("");
        assertEquals(Boolean.FALSE, multiMap.getBoolean(MAP_KEY));
    }

    @Test
    public void getBooleanWithValue() {
        MultiMap multiMap = buildMultiMap("true");
        assertEquals(Boolean.TRUE, multiMap.getBoolean(MAP_KEY));
    }

    @Test
    public void getBooleanWithInvalidValue() {
        MultiMap multiMap = buildMultiMap("value");
        assertEquals(Boolean.FALSE, multiMap.getBoolean(MAP_KEY));
    }

    @Test
    public void getBooleanOrDefaultNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(true, multiMap.getBooleanOrDefault(MAP_KEY, true));
    }

    @Test
    public void getBooleanOrDefaultNullValue() {
        final boolean defaultValue = true;
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(defaultValue, multiMap.getBooleanOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getBooleanOrDefaultEmptyValue() {
        final String value = "";
        final boolean defaultValue = true;
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getBooleanOrDefaultWithValue() {
        final String value = "false";
        final boolean defaultValue = true;
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getBooleanOrDefaultWithInvalidValue() {
        final String value = "value";
        final boolean defaultValue = true;
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getBooleanOrNullNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(null, multiMap.getBooleanOrNull(MAP_KEY));
    }

    @Test
    public void getBooleanOrNullNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(null, multiMap.getBooleanOrNull(MAP_KEY));
    }

    @Test
    public void getBooleanOrNullEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrNull(MAP_KEY));
    }

    @Test
    public void getBooleanOrNullWithValue() {
        final String value = "true";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.TRUE, multiMap.getBooleanOrNull(MAP_KEY));
    }

    @Test
    public void getBooleanOrNullWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrNull(MAP_KEY));
    }

    @Test(expected = MultiMapException.class)
    public void getBooleanOrFailNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        multiMap.getBooleanOrFail(MAP_KEY);
    }

    @Test(expected = MultiMapException.class)
    public void getBooleanOrFailNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        multiMap.getBooleanOrFail(MAP_KEY);
    }

    @Test
    public void getBooleanOrFailEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrFail(MAP_KEY));
    }

    @Test
    public void getBooleanOrFailWithValue() {
        final String value = "true";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.TRUE, multiMap.getBooleanOrFail(MAP_KEY));
    }

    @Test
    public void getBooleanOrFailWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Boolean.FALSE, multiMap.getBooleanOrFail(MAP_KEY));
    }

    @Test
    public void getIntegerNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(Integer.valueOf(-1), multiMap.getInteger(MAP_KEY));
    }

    @Test
    public void getIntegerNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(Integer.valueOf(-1), multiMap.getInteger(MAP_KEY));
    }

    @Test
    public void getIntegerEmptyValue() {
        MultiMap multiMap = buildMultiMap("");
        assertEquals(Integer.valueOf(-1), multiMap.getInteger(MAP_KEY));
    }

    @Test
    public void getIntegerWithValue() {
        final String value = "2";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Integer.valueOf(value), multiMap.getInteger(MAP_KEY));
    }

    @Test
    public void getIntegerWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Integer.valueOf(-1), multiMap.getInteger(MAP_KEY));
    }

    @Test
    public void getIntegerOrDefaultNoValue() {
        final Integer defaultValue = Integer.valueOf(3);
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(defaultValue, multiMap.getIntegerOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getIntegerOrDefaultNullValue() {
        Integer defaultValue = Integer.valueOf(3);
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(defaultValue, multiMap.getIntegerOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getIntegerOrDefaultEmptyValue() {
        final String value = "";
        Integer defaultValue = Integer.valueOf(3);
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(defaultValue, multiMap.getIntegerOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getIntegerOrDefaultWithValue() {
        final String value = "2";
        Integer defaultValue = Integer.valueOf(3);
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Integer.valueOf(value), multiMap.getIntegerOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getIntegerOrDefaultWithInvalidValue() {
        final String value = "value";
        Integer defaultValue = Integer.valueOf(3);
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(defaultValue, multiMap.getIntegerOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getIntegerOrNullNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(null, multiMap.getIntegerOrNull(MAP_KEY));
    }

    @Test
    public void getIntegerOrNullNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(null, multiMap.getIntegerOrNull(MAP_KEY));
    }

    @Test
    public void getIntegerOrNullEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(null, multiMap.getIntegerOrNull(MAP_KEY));
    }

    @Test
    public void getIntegerOrNullWithValue() {
        final String value = "2";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Integer.valueOf(value), multiMap.getIntegerOrNull(MAP_KEY));
    }

    @Test
    public void getIntegerOrNullWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(null, multiMap.getIntegerOrNull(MAP_KEY));
    }

    @Test(expected = MultiMapException.class)
    public void getIntegerOrFailNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        multiMap.getIntegerOrFail(MAP_KEY);
    }

    @Test(expected = MultiMapException.class)
    public void getIntegerOrFailNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        multiMap.getIntegerOrFail(MAP_KEY);
    }

    @Test(expected = MultiMapException.class)
    public void getIntegerOrFailEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        multiMap.getIntegerOrFail(MAP_KEY);
    }

    @Test
    public void getIntegerOrFailWithValue() {
        final String value = "2";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Integer.valueOf(value), multiMap.getIntegerOrFail(MAP_KEY));
    }

    @Test(expected = MultiMapException.class)
    public void getIntegerOrFailWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        multiMap.getIntegerOrFail(MAP_KEY);
    }

    @Test
    public void getMapNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapEmptyValue() {
        MultiMap multiMap = buildMultiMap("");
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapWithValue() {
        MultiMap multiMap = buildMultiMap("key=value");
        assertEquals(Collections.singletonMap("key", "value"), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapWithInvalidValue() {
        MultiMap multiMap = buildMultiMap("value");
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapWithNoKey() {
        MultiMap multiMap = buildMultiMap("=value");
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapWithNoValue() {
        MultiMap multiMap = buildMultiMap("key=");
        assertEquals(Collections.emptyMap(), multiMap.getMap(MAP_KEY));
    }

    @Test
    public void getMapOrDefaultNoValue() {
        final Map<String, String> defaultValue = Collections.singletonMap("key", "value");
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(defaultValue, multiMap.getMapOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getMapOrDefaultNullValue() {
        final Map<String, String> defaultValue = Collections.singletonMap("key", "value");
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(defaultValue, multiMap.getMapOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getMapOrDefaultEmptyValue() {
        final String value = "";
        final Map<String, String> defaultValue = Collections.singletonMap("key", "value");
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getMapOrDefaultWithValue() {
        final String value = "key=value";
        final Map<String, String> defaultValue = Collections.singletonMap("key", "value");
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(defaultValue, multiMap.getMapOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getMapOrDefaultWithInvalidValue() {
        final String value = "invalid";
        final Map<String, String> defaultValue = Collections.singletonMap("key", "value");
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrDefault(MAP_KEY, defaultValue));
    }

    @Test
    public void getMapOrNullNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        assertEquals(null, multiMap.getMapOrNull(MAP_KEY));
    }

    @Test
    public void getMapOrNullNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        assertEquals(null, multiMap.getMapOrNull(MAP_KEY));
    }

    @Test
    public void getMapOrNullEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrNull(MAP_KEY));
    }

    @Test
    public void getMapOrNullWithValue() {
        final String value = "key=value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.singletonMap("key", "value"), multiMap.getMapOrNull(MAP_KEY));
    }

    @Test
    public void getMapOrNullWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrNull(MAP_KEY));
    }

    @Test(expected = MultiMapException.class)
    public void getMapOrFailNoValue() {
        MultiMap multiMap = new MultiMap(Collections.<String, String[]> emptyMap());
        multiMap.getMapOrFail(MAP_KEY);
    }

    @Test(expected = MultiMapException.class)
    public void getMapOrFailNullValue() {
        MultiMap multiMap = buildMultiMap((String[]) null);
        multiMap.getMapOrFail(MAP_KEY);
    }

    @Test
    public void getMapOrFailEmptyValue() {
        final String value = "";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrFail(MAP_KEY));
    }

    @Test
    public void getMapOrFailWithValue() {
        final String value = "key=value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.singletonMap("key", "value"), multiMap.getMapOrFail(MAP_KEY));
    }

    @Test
    public void getMapOrFailWithInvalidValue() {
        final String value = "value";
        MultiMap multiMap = buildMultiMap(value);
        assertEquals(Collections.emptyMap(), multiMap.getMapOrFail(MAP_KEY));
    }

    private MultiMap buildMultiMap(String... values) {
        return new MultiMap(Collections.singletonMap(MAP_KEY, values));
    }
}