package com.identity4j.util.xml;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-611 – XMLDataExtractor must have external entity
 * resolution disabled to prevent XXE injection.
 * #L%
 */

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.HashSet;

import javax.xml.stream.XMLInputFactory;

import org.junit.Test;

public class XMLDataExtractorCwe611Test {

    @Test
    public void externalEntitiesAreDisabled() throws Exception {
        XMLDataExtractor extractor = XMLDataExtractor.getInstance();
        Field f = XMLDataExtractor.class.getDeclaredField("inputFactory");
        f.setAccessible(true);
        XMLInputFactory factory = (XMLInputFactory) f.get(extractor);

        Object supportsEntities = factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
        assertNotNull("IS_SUPPORTING_EXTERNAL_ENTITIES property must be set", supportsEntities);
        assertFalse("IS_SUPPORTING_EXTERNAL_ENTITIES must be false (CWE-611)",
                Boolean.TRUE.equals(supportsEntities));
    }

    @Test
    public void dtdSupportIsDisabled() throws Exception {
        XMLDataExtractor extractor = XMLDataExtractor.getInstance();
        Field f = XMLDataExtractor.class.getDeclaredField("inputFactory");
        f.setAccessible(true);
        XMLInputFactory factory = (XMLInputFactory) f.get(extractor);

        Object supportsDtd = factory.getProperty(XMLInputFactory.SUPPORT_DTD);
        assertNotNull("SUPPORT_DTD property must be set", supportsDtd);
        assertFalse("SUPPORT_DTD must be false (CWE-611)", Boolean.TRUE.equals(supportsDtd));
    }

    @Test
    public void wellFormedXmlParsesNormally() throws Exception {
        XMLDataExtractor extractor = XMLDataExtractor.getInstance();
        Set<String> nodes = new HashSet<>();
        nodes.add("value");
        // Simple well-formed XML with no external entities
        java.util.Map<String, XMLDataExtractor.Node> result =
                extractor.extract("<root><value>42</value></root>", nodes);
        assertNotNull("result must not be null for valid XML", result);
    }
}
