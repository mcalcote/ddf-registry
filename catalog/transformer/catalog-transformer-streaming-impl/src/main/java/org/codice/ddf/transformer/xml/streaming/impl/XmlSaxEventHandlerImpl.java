/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.transformer.xml.streaming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codice.ddf.transformer.xml.streaming.SaxEventHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * A sax event handler used to parse urn:catalog:metacard {@link Metacard}s. By default, handles all elements defined in the {@link XmlSaxEventHandlerImpl#xmlToMetacard}
 * These defaults can be overridden by passing a different {@link Map} in {@link XmlSaxEventHandlerImpl#setXmlToMetacard(Map)}
 * {@inheritDoc}
 */
public class XmlSaxEventHandlerImpl implements SaxEventHandler {

    /*
     * A list of Attributes that is populated during parsing and then returned by getAttributes
     */
    private List<Attribute> attributes;

    /*
     * The localName of the XML element that is currently being read
     */
    private String elementBeingRead;

    /*
     * A StringBuilder that holds the text value of an XML element, e.g. "foo" in <bar> foo </bar>
     */
    private StringBuilder textDataOfElement;

    /*
     * A mapping of XML Element names to Metacard Attribute names
     */
    private Map<String, String> xmlToMetacard;

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setDocumentLocator(Locator locator) {

    }

    protected XmlSaxEventHandlerImpl() {
        xmlToMetacard = new HashMap<>();
        /*
         * Map the XML element names to the metacard attributes
         */
        xmlToMetacard.put(Metacard.TITLE, Metacard.TITLE);
        xmlToMetacard.put(Metacard.POINT_OF_CONTACT, Metacard.POINT_OF_CONTACT);
        xmlToMetacard.put(Metacard.DESCRIPTION, Metacard.DESCRIPTION);
        xmlToMetacard.put("source", Metacard.RESOURCE_URI);
    }

    protected XmlSaxEventHandlerImpl(Map<String, String> xmlToMetacardMap) {
        this.xmlToMetacard = xmlToMetacardMap;
    }

    @Override
    public void startDocument() {
        textDataOfElement = new StringBuilder();
        attributes = new ArrayList<>();

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    /**
     * Takes in a sax event from {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}, and if it is
     * in the xmlToMetacardMapping, begin reading it.
     *
     * @param uri        the URI that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param localName  the localName that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param qName      the qName that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param attributes the attributes that are passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        /*
         * If the element name is in the xmlToMetacardMapping, begin elementBeingRead. The data will be in the characters event.
         */
        if (xmlToMetacard.get(localName.toLowerCase()) != null) {
            elementBeingRead = localName.toLowerCase();
            return;
        }

        /*
         * If the attribute "name" is in the xmlToMetacardMapping, begin elementBeingRead. The data will be in the characters event.
         */
        String attribute = attributes.getValue("name");
        if (attribute != null && xmlToMetacard.get(attribute) != null) {
            elementBeingRead = attribute;
        }
    }

    /**
     * Takes in a sax event from {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}. If the element is in the xmlToElementMapping,
     * add it to the attributes list
     *
     * @param namespaceURI the namespaceURI that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param localName    the localName that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param qName        the qName that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        /*
         * If the handler is currently "reading" an element this implies the element is in the xmlToElementMapping, add the value of the
         * textDataOfElement to the attributes list
         */
        if (elementBeingRead != null) {
            String result = textDataOfElement.toString()
                    .trim();
            textDataOfElement.setLength(0);

            attributes.add(new AttributeImpl(xmlToMetacard.get(elementBeingRead), result));

            elementBeingRead = null;

        }
    }

    /**
     * Takes in a sax event from {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate} and adds the characters
     * to textDataOfElement
     *
     * @param ch     the ch that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param start  the start that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     * @param length the length that is passed in by {@link org.codice.ddf.transformer.xml.streaming.lib.SaxEventHandlerDelegate}
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        /*
         * If the handler is currently "reading" an element, read the characters into the textDataOfElement Stringbuilder
         */
        if (elementBeingRead != null) {
            textDataOfElement.append(new String(ch, start, length));
        }

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }
}
