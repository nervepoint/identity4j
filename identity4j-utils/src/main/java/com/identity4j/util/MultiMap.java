/* HEADER */
package com.identity4j.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Object that acts like a Map that can have multiple string values.
 */
public class MultiMap implements Serializable, Map<String, String[]> {
	
	private static final long serialVersionUID = 2939486586092126095L;
	
	private static final String[] EMPTY_ARRAY = new String[0];
	private Map<String, String[]> values = new HashMap<String, String[]>();


	public static MultiMap fromMapSingle(Map<String, String> values) {
		MultiMap m = new MultiMap();
		for(Map.Entry<String, String> s : values.entrySet()) {
			m.put(s.getKey(), new String[] { s.getValue() });
		}
		return m;
	}
	
	public static MultiMap fromMap(Map<String, String[]> map) {
		MultiMap mm = new MultiMap();
		mm.values = map;
		return mm;
	}

	/**
	 * Construct using a empty map.
	 */
	public MultiMap() {
	}

	/**
	 * Construct using a string array map.
	 * 
	 * @param values
	 */
	public MultiMap(Map<String, String[]> values) {
		this.values.putAll(values);
	}

	/**
	 * Sets supplied values.
	 * 
	 * @param values
	 */
	public void setAll(Map<String, String[]> values) {
		this.values.putAll(values);
	}

	/**
	 * Adds the supplied values to any values currently set under this key. A
	 * <code>null</code> value will be returned if no such value exists.
	 * 
	 * @param key key
	 * @param values values
	 * @return old value
	 */
	public String[] setMore(String key, String... values) {
		String[] oldValue = getStringArray(key);
		Collection<String> more = new ArrayList<String>(Arrays.asList(oldValue));
		more.addAll(Arrays.asList(values));
		return set(key, more);
	}

	/**
	 * Set a value. A <code>null</code> value will be returned if no such value
	 * exists.
	 * 
	 * @param key key
	 * @param values values
	 * @return old value
	 */
	public String[] set(String key, String... values) {
		return this.values.put(key, values);
	}

	/**
	 * Set a value. A <code>null</code> value will be returned if no such value
	 * exists.
	 * 
	 * @param key key
	 * @param values values
	 * @return old value
	 */
	public String[] set(String key, Collection<String> values) {
		if (values == null) {
			return set(key, (String[]) null);
		}
		return set(key, values.toArray(new String[values.size()]));
	}

	/**
	 * Get the string value of an entry given its key. An empty string will be
	 * returned if no such entry exists.
	 * 
	 * @param key entry key
	 * @return string entry value
	 */
	public final String getString(String key) {
		return StringUtil.nonNull(getStringOrNull(key));
	}

	/**
	 * Get the string value of an entry given its key or a default value if no
	 * such entry exists..
	 * 
	 * @param key entry key
	 * @param defaultValue default value
	 * @return string entry value
	 */
	public final String getStringOrDefault(String key, String defaultValue) {
		String value = getStringOrNull(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Get the string value of an entry given its key or <code>null</code> if no
	 * such entry exists.
	 * 
	 * @param key entry key
	 * @return string entry value
	 */
	public final String getStringOrNull(String key) {
		String[] stringValues = values.get(key);
		if (stringValues != null && stringValues.length > 0) {
			return stringValues[0];
		}
		return null;
	}

	/**
	 * Get the string value of an entry given its key or fail with an exception
	 * if it does not.
	 * 
	 * @param key key
	 * @return string value
	 * @throws MultiMapException if no such entry exists
	 */
	public final String getStringOrFail(String key) {
		String value = getStringOrNull(key);
		if (value == null) {
			throw new MultiMapException("Missing value for attribute '" + key + "'.");
		}
		return value;
	}

	/**
	 * Get the string array value of an entry given its key. An empty array will
	 * be returned if no such entry exists.
	 * 
	 * @param key entry key
	 * @return string array entry values
	 */
	public final String[] getStringArray(String key) {
		String[] values = getStringArrayOrNull(key);
		return values == null ? EMPTY_ARRAY : values;
	}

	/**
	 * Get the string array value of an entry given its key or default values if
	 * no such entry exists.
	 * 
	 * @param key entry key
	 * @param defaultValues default values
	 * @return string array entry values
	 */
	public final String[] getStringArrayOrDefault(String key, String... defaultValues) {
		String[] values = getStringArrayOrNull(key);
		return values == null ? defaultValues : values;
	}

	/**
	 * Get the string array value of an entry given its key or <code>null</code>
	 * if no such entry exists.
	 * 
	 * @param key entry key
	 * @return string array entry value
	 */
	public final String[] getStringArrayOrNull(String key) {
		return values.get(key);
	}

	/**
	 * Get the string array value of an entry given its key or fail with an
	 * exception if it does not.
	 * 
	 * @param key entry key
	 * @return string array entry value
	 * @throws MultiMapException if no such entry exists
	 */
	public final String[] getStringArrayOrFail(String key) {
		String[] values = getStringArrayOrNull(key);
		if (values == null) {
			throw new MultiMapException("Missing value for attribute '" + key + "'.");
		}
		return values;
	}

	/**
	 * Get the boolean value of an entry given its key. {@link Boolean#FALSE}
	 * will be returned if no such entry exists.
	 * 
	 * @param key entry key
	 * @return boolean entry value
	 */
	public final Boolean getBoolean(String key) {
		Boolean value = getBooleanOrNull(key);
		return value == null ? Boolean.FALSE : value;
	}

	/**
	 * Get the boolean value of an entry given its key or a default value if no
	 * such entry exists.
	 * 
	 * @param key entry key
	 * @param defaultValue default value
	 * @return boolean entry value
	 */
	public final Boolean getBooleanOrDefault(String key, boolean defaultValue) {
		String value = getStringOrNull(key);
		return value == null ? defaultValue : Boolean.valueOf(value);
	}

	/**
	 * Get the boolean value of an entry given its key or <code>null</code> if
	 * no such entry exists.
	 * 
	 * @param key entry key
	 * @return boolean entry value
	 */
	public final Boolean getBooleanOrNull(String key) {
		String value = getStringOrNull(key);
		return value == null ? null : Boolean.valueOf(value);
	}

	/**
	 * Get the boolean value of an entry given its key or fail with an exception
	 * if it does not.
	 * 
	 * @param key key
	 * @return boolean entry value
	 * @throws MultiMapException if no such entry exists
	 */
	public final Boolean getBooleanOrFail(String key) {
		String value = getStringOrFail(key);
		return Boolean.valueOf(value);
	}

	/**
	 * Get the integer value of an entry given its key. <code>-1</code> will be
	 * returned if no such entry exists.
	 * 
	 * @param key entry key
	 * @return integer entry value
	 */
	public final Integer getInteger(String key) {
		Integer value = getIntegerOrNull(key);
		return value == null ? Integer.valueOf(-1) : value;
	}

	/**
	 * Get the integer value of an entry given its key or a default value if no
	 * such entry exists..
	 * 
	 * @param key entry key
	 * @param defaultValue default value
	 * @return integer entry value
	 */
	public final Integer getIntegerOrDefault(String key, Integer defaultValue) {
		Integer value = getIntegerOrNull(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Get the integer value of an entry given its key or <code>null</code> if
	 * no such entry exists.
	 * 
	 * @param key entry key
	 * @return integer entry value
	 */
	public final Integer getIntegerOrNull(String key) {
		String value = getStringOrNull(key);
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	/**
	 * Get the boolean value of an entry given its key or fail with an exception
	 * if it does not.
	 * 
	 * @param key key
	 * @return boolean entry value
	 * @throws MultiMapException if no such entry exists
	 */
	public final Integer getIntegerOrFail(String key) {
		try {
			String value = getStringOrFail(key);
			return Integer.valueOf(value);
		} catch (NumberFormatException nfe) {
			throw new MultiMapException("Unable to obtain Integer value for key '" + key + "'.", nfe);
		}
	}

	/**
	 * Get the <code>java.util.Map</code> value of an entry given its key.
	 * {@link Collections#emptyMap()} will be returned if no such entry exists.
	 * 
	 * @param key entry key
	 * @return Map entry value
	 */
	public final Map<String, String> getMap(String key) {
		String[] values = getStringArrayOrNull(key);
		return values == null ? Collections.<String, String> emptyMap() : toMap(values);
	}

	/**
	 * Get the <code>java.util.Map</code> value of an entry given its key or a
	 * default value if no such entry exists.
	 * 
	 * @param key entry key
	 * @param defaultValue default value
	 * @return Map entry value
	 */
	public final Map<String, String> getMapOrDefault(String key, Map<String, String> defaultValue) {
		String[] values = getStringArrayOrNull(key);
		return values == null ? defaultValue : toMap(values);
	}

	/**
	 * Get the <code>java.util.Map</code> value of an entry given its key or
	 * <code>null</code> if no such entry exists.
	 * 
	 * @param key entry key
	 * @return Map entry value
	 */
	public final Map<String, String> getMapOrNull(String key) {
		String[] values = getStringArrayOrNull(key);
		return values == null ? null : toMap(values);
	}

	/**
	 * Get the <code>java.util.Map</code> value of an entry given its key or
	 * fail with an exception if it does not.
	 * 
	 * @param key key
	 * @return Map entry value
	 * @throws MultiMapException if no such entry exists
	 */
	public final Map<String, String> getMapOrFail(String key) {
		String[] values = getStringArrayOrFail(key);
		return toMap(values);
	}

	private Map<String, String> toMap(String[] values) {
		Map<String, String> toMap = new HashMap<String, String>();
		for (String value : values) {
			String[] split = value.split("=");
			if (split.length == 2) {
				String mapKey = split[0];
				String mapValue = split[1];
				if (!StringUtil.isNullOrEmpty(mapKey) && !StringUtil.isNullOrEmpty(mapValue)) {
					toMap.put(mapKey, mapValue);
				}
			}
		}
		return toMap;
	}

	/**
	 * Get an iterator of all keys.
	 * 
	 * @return key iterator
	 */
	public final Iterable<String> getKeyIterator() {
		return new HashSet<String>(values.keySet());
	}

	public final Map<String, String[]> toMap() {
		return Collections.unmodifiableMap(values);
	}

	public final void clear() {
		values.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MultiMap)) {
			return false;
		}
		return values.equals(obj);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	@Override
	public String toString() {
		return values.toString();
	}

	/**
	 * Create a new {@link MultiMap} given properties.
	 * 
	 * @param originalMap string array map
	 * @return map
	 */
	public static MultiMap toMultiMap(Properties originalMap) {
		Map<String, String[]> values = new HashMap<String, String[]>();
		for (Entry<Object, Object> entry : originalMap.entrySet()) {
			values.put(entry.getKey().toString(), StringUtil.toDefaultArray(entry.getValue().toString()));
		}
		return new MultiMap(values);
	}

	/**
	 * Create a new {@link MultiMap} given a string array map.
	 * 
	 * @param originalMap string array map
	 * @return map
	 */
	public static MultiMap toMultiMap(Map<String, String> originalMap) {
		Map<String, String[]> values = new HashMap<String, String[]>();
		for (Entry<String, String> entry : originalMap.entrySet()) {
			values.put(entry.getKey(), StringUtil.toDefaultArray(entry.getValue()));
		}
		return new MultiMap(values);
	}

	public static Map<String, String> toMap(Map<String, String[]> map) {
		Map<String, String> m = new HashMap<String, String>();
		for (String key : map.keySet()) {
			m.put(key, StringUtil.toDefaultString(map.get(key)));
		}
		return m;
	}

	public void merge(MultiMap source) {
		values.putAll(source.values);
	}

	public Set<String> keySet() {
		return values.keySet();
	}

	@Override
	public boolean containsKey(Object key) {
		return values.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, String[]>> entrySet() {
		return values.entrySet();
	}

	@Override
	public String[] get(Object key) {
		return values.get(key);
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public String[] put(String key, String[] value) {
		return values.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String[]> map) {
		values.putAll(map);
	}

	@Override
	public String[] remove(Object key) {
		return values.remove(key);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public Collection<String[]> values() {
		return values.values();
	}
}