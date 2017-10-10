package com.identity4j.util.xml;

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
import java.util.HashSet;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.junit.Test;

import com.identity4j.util.xml.XMLDataExtractor.Node;

public class XMLDataExtractorTest {

	private static String XML = "<item date=\"February 2014\" color=\"Red\">"
								+ 	"<mode>2</mode>"
								+ 	"<unit>400</unit>"
								+ 	"<current>2</current>"
								+ 	"<interactive>5</interactive>"
								+ "</item>";
	
	@Test
	public void itShouldFindNodeValuesForNodesHavingCharacterValue() throws XMLStreamException{
		Map<String, Node> nodes = XMLDataExtractor.getInstance().extract(XML, new HashSet<String>(Arrays.asList("unit")));
		Assert.assertEquals("unit",nodes.get("unit").getNodeName());
		Assert.assertEquals("400",nodes.get("unit").getNodeValue());
	}

	@Test
	public void itShouldNotReturnAnyNodeValuesForNonExistentNodes() throws XMLStreamException{
		Map<String, Node> nodes = XMLDataExtractor.getInstance().extract(XML, new HashSet<String>(Arrays.asList("nop")));
		Assert.assertNull(nodes.get("nop"));
	}
	
	@Test
	public void itShouldFindAllAttributeValuesForNodesHavingAttributesValue() throws XMLStreamException{
		Map<String, Node> nodes = XMLDataExtractor.getInstance().extract(XML, new HashSet<String>(Arrays.asList("item")));
		Assert.assertEquals("item",nodes.get("item").getNodeName());
		Assert.assertEquals("Red",nodes.get("item").getAttributes().get("color"));
		Assert.assertEquals("February 2014",nodes.get("item").getAttributes().get("date"));
	}
	
	@Test
	public void itShouldNOtReturnAnyAttributeValuesForNodesHavingNoAttributes() throws XMLStreamException{
		Map<String, Node> nodes = XMLDataExtractor.getInstance().extract(XML, new HashSet<String>(Arrays.asList("current")));
		Assert.assertEquals("current",nodes.get("current").getNodeName());
		Assert.assertTrue(nodes.get("current").getAttributes().isEmpty());
	}
	
	@Test
	public void itShouldReturnNullForNodesHavingChildrenAsAnotherNode() throws XMLStreamException{
		Map<String, Node> nodes = XMLDataExtractor.getInstance().extract(XML, new HashSet<String>(Arrays.asList("item")));
		Assert.assertEquals("item",nodes.get("item").getNodeName());
		Assert.assertNull(nodes.get("item").getNodeValue());
	}
	
	
}
