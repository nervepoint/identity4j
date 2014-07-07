package com.identity4j.util.xml;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>
 * This is a simple utility class which tries to extract node values and attributes from an XML.
 * <br />
 * This will extract nodes with character value only, a node with children node will result in null.
 * <br/>
 * A node with children or character with attributes will always shows attributes in node instance.
 * <br /> 
 * <b>e.g.</b>
 * <pre>
 * {@code
 * <item date="February 2014">
 *      <mode>2</mode>
 *      <unit>400</unit>
 *      <current>2</current>
 *      <interactive>5</interactive>
 * </item>
 * }
 * </pre>
 * </p>
 * <p>
 * When we extract nodes.
 * <ul>
 * 	<li>For unit it would extract node value as 400.</li>
 *  <li>For item it would extract node value as null, as no character data is present, but it will show attributes</li>
 * </ul>
 * </p>
 * 
 * @author gaurav
 *
 */
public class XMLDataExtractor {

	 private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	 
	 private XMLDataExtractor(){}
	 
 	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final XMLDataExtractor INSTANCE = new XMLDataExtractor();
	}
 
	public static XMLDataExtractor getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	/**
	 * 
	 * @param xml
	 * @param nodes
	 * @return
	 * @throws XMLStreamException
	 */
	public Map<String, Node> extract(String xml,Set<String> nodes) throws XMLStreamException{
		XMLEventReader eventReader = null;
		StringReader xmlStringReader = null;
		Map<String, Node> nodeValues = new HashMap<String, XMLDataExtractor.Node>();
		try{
			xmlStringReader = new StringReader(xml);
			eventReader = inputFactory.createXMLEventReader(xmlStringReader);
			
			XMLEvent event = null;
			Node node = null;
			boolean isExpectingNodeValue = false;
			while (eventReader.hasNext()) {
		        event = eventReader.nextEvent();
		        /**
		         * We reset node to null as after start of node we expect character value to be 
		         * available, if its not and we reach again to the start of the element, it means 
		         * new child element has been reached from parent and hence we don't expect any value
		         * for this node and set it to null and event for characters in loop will not be filled by
		         * immediate node value.
		         * e.g.
		         * <a>
		         * 	<b>value</b>
		         * </a>
		         * 
		         * At node 'a' we will create a node object and very next character event we will fill it with value.
		         * If check below is not applied value from node 'b' will be picked as events would be start, start, char.
		         * Hence on new start of element we need to flush/clean previous start of element.
		         */
		        if (event.isStartElement() && isExpectingNodeValue){
		        	node = null;
		        }
		        //its start of element create Node instance
		        if (event.isStartElement() && nodes.contains(event.asStartElement().getName().getLocalPart())) {
		        	StartElement startElement = event.asStartElement();
		        	node = new Node();
		        	node.setNodeName(event.asStartElement().getName().getLocalPart());
		        	
		        	// We read the attributes from this tag
		            @SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
		            while (attributes.hasNext()) {
		              Attribute attribute = attributes.next();
		              node.getAttributes().put(attribute.getName().toString(), attribute.getValue());
		            }
		            
		        	nodeValues.put(event.asStartElement().getName().getLocalPart(), node);
		        	isExpectingNodeValue = true;
		        }
		        
		        //its characters event fill it into node value of current node instance
		        if(event.isCharacters() && node != null){
		        	node.setNodeValue(event.asCharacters().getData());
		        	node = null;
		        	isExpectingNodeValue = false;
		        }
			}
		}finally{
			if(xmlStringReader != null) xmlStringReader.close();
			if(eventReader != null) eventReader.close();
		}
		return nodeValues;
		
	}
	
	/**
	 * Node entity representing an XML node.
	 * 
	 * @author gaurav
	 *
	 */
	public static class Node{
		private Map<String, String> attributes = new HashMap<String, String>();
		private String nodeName;
		private String nodeValue;
		
		public Map<String, String> getAttributes() {
			return attributes;
		}
		public void setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
		}
		public String getNodeName() {
			return nodeName;
		}
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		public String getNodeValue() {
			return nodeValue;
		}
		public void setNodeValue(String nodeValue) {
			this.nodeValue = nodeValue;
		}
		@Override
		public String toString() {
			return "Node [attributes=" + attributes + ", nodeName=" + nodeName
					+ ", nodeValue=" + nodeValue + "]";
		}
	}
}
