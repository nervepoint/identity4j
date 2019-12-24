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
