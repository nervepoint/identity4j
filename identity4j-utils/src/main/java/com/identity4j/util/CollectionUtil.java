package com.identity4j.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides general purpose Collection utilities.
 */
public class CollectionUtil {

	/**
	 * Utility function which scans the providedCollection and returns elements not
	 * present in probeCollection
	 * 
	 * Collection 1 -- 1,2,9,5
	 * Collection 2 -- 3,1,5,8
	 * 
	 * Result -- 2,9
	 * 
	 * @param providedCollection
	 * @param probeCollection
	 * @return elements not found in probeCollection
	 */
	public static <T> Collection<T> objectsNotPresentInProbeCollection(Collection<T> providedCollection,Collection<T> probeCollection) {
		Set<T> objects = new HashSet<T>();
		for (T role : providedCollection) {
			if(!probeCollection.contains(role))
				objects.add(role);
		}
		return objects;
	}
	
	/**
	 * Provides empty iterator for the provided type of klass
	 * <br>
	 * <strong>Note:</strong> Java 7 provides emptyIterator implementation.
	 * 
	 * @param klass
	 * @return
	 */
	public static <T> Iterator<T> emptyIterator(Class<T> klass){
		return Collections.<T>emptyList().iterator();
	}
}
