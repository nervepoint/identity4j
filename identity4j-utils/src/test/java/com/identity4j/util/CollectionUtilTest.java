package com.identity4j.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.util.CollectionUtil;

public class CollectionUtilTest {

	@Test
	public void itShouldFindElementsNotPresentInProbeCollection(){
		List<Integer> providedCollection = Arrays.asList(1,2,9,5);
		List<Integer> probeCollection = Arrays.asList(3,1,5,8);

		Collection<Integer> collection = CollectionUtil.objectsNotPresentInProbeCollection(providedCollection, probeCollection);
		
		Assert.assertTrue("Size of collection is 2", collection.size() == 2);
		Assert.assertTrue("Collection contains 2", collection.contains(2));
		Assert.assertTrue("Collection contains 9", collection.contains(9));
		
	}
	
	@Test
	public void itShouldFindZeroElementsIfBothProvidedAndProbeCollectionAreSame(){
		List<Integer> providedCollection = Arrays.asList(1,2,9,5);
		List<Integer> probeCollection = Arrays.asList(1,2,9,5);

		Collection<Integer> collection = CollectionUtil.objectsNotPresentInProbeCollection(providedCollection, probeCollection);
		
		Assert.assertTrue("Collection is empty", collection.isEmpty());
		
	}
	
	@Test
	public void itShouldReturnSameNumberOfElementsAsProvidedCollectionIfBothProvidedAndProbeCollectionAreNotSame(){
		List<Integer> providedCollection = Arrays.asList(1,2);
		List<Integer> probeCollection = Arrays.asList(11,12);

		Collection<Integer> collection = CollectionUtil.objectsNotPresentInProbeCollection(providedCollection, probeCollection);
		
		Assert.assertTrue("Size of collection is 2", collection.size() == 2);
		Assert.assertTrue("Collection contains 1", collection.contains(1));
		Assert.assertTrue("Collection contains 2", collection.contains(2));
		
	}
	
	@Test
	public void itShouldReturnEmptyIteratorWithZeroElements(){
		Iterator<String> iterator = CollectionUtil.emptyIterator(String.class);
		Assert.assertFalse("Iterator has no elements", iterator.hasNext());
	}
	
	
}
