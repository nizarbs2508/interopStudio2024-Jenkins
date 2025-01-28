package com.ans.cda.service.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/***
 * PositionalXMLReader
 */
public class PositionalXMLReader {
	/**
	 * LINE_NUMBER_KEY_NAME
	 */
	private final static String LINENUMBERKEY = "lineNumber";
	/**
	 * PositionalXMLReader
	 */
	private PositionalXMLReader() {
		//empty constructor
	}

	/**
	 * readXML
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document readXML(final InputStream istream) throws IOException, SAXException, RuntimeException {
		final Document doc;
		SAXParser parser;
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setNamespaceAware(true);
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
		}

		final Deque<Element> elementStack = new ArrayDeque<>();
		final StringBuilder textBuffer = new StringBuilder();
		final DefaultHandler handler = new DefaultHandler() {
			/**
			 * locator
			 */
			private Locator locator;

			/**
			 * setDocumentLocator
			 */
			@Override
			public void setDocumentLocator(final Locator locator) {
				this.locator = locator; // Save the locator, so that it can be used later for line tracking when
										// traversing nodes.
			}

			/**
			 * startElement
			 */
			@Override
			public void startElement(final String uri, final String localName, final String qName,
					final Attributes attributes) throws SAXException {
				addTextIfNeeded();
				final Element element = doc.createElement(qName);
				for (int i = 0; i < attributes.getLength(); i++) {
					element.setAttribute(attributes.getQName(i), attributes.getValue(i));
				}
				element.setUserData(LINENUMBERKEY, String.valueOf(this.locator.getLineNumber()), null);
				elementStack.push(element);
			}

			/**
			 * endElement
			 */
			@Override
			public void endElement(final String uri, final String localName, final String qName) {
				addTextIfNeeded();
				final Element closedEl = elementStack.pop();
				if (elementStack.isEmpty()) { // Is this the root element?
					doc.appendChild(closedEl);
				} else {
					final Element parentEl = elementStack.peek();
					parentEl.appendChild(closedEl);
				}
			}

			/**
			 * characters
			 */
			@Override
			public void characters(final char charactar[], final int start, final int length) throws SAXException {
				textBuffer.append(charactar, start, length);
			}

			/**
			 * Outputs text accumulated under the current node
			 */
			private void addTextIfNeeded() {
				if (textBuffer.length() > 0) {
					final Element element = elementStack.peek();
					final Node textNode = doc.createTextNode(textBuffer.toString());
					element.appendChild(textNode);
					textBuffer.delete(0, textBuffer.length());
				}
			}
		};
		parser.parse(istream, handler);
		return doc;
	}
}