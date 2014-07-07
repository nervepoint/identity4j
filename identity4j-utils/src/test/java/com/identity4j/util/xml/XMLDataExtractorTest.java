package com.identity4j.util.xml;

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
