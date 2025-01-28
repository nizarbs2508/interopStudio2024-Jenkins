package com.ans.cda.service.parametrage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Class Handler
 * 
 * @author bensa Nizar
 */
public final class Handler extends DefaultHandler {
	/**
	 * item
	 */
	private TreeItem<String> item = new TreeItem<>();
	/**
	 * elementStack
	 */
	private final Deque<Element> elementStack = new ArrayDeque<>();
	/**
	 * textBuffer
	 */
	private final StringBuilder textBuffer = new StringBuilder();
	/**
	 * locator
	 */
	private Locator locator;
	/**
	 * doc
	 */
	private static Document doc;
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(Handler.class);

	/**
	 * Handler constructor
	 */
	private Handler() {
		super();
	}

	/**
	 * children
	 * 
	 * @return
	 */
	private ObservableList<TreeItem<String>> children() {
		return item.getChildren();
	}

	/**
	 * firstChild
	 * 
	 * @return
	 */
	private TreeItem<String> firstChild() {
		return children().get(0);
	}

	/**
	 * readXML
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static TreeItem<String> readXML(final File file) throws IOException, SAXException {
		SAXParser parser;
		TreeItem<String> itemm = null;
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			final Handler handler = new Handler();
			final XMLReader reader = getXMLReader(parser);
			reader.setContentHandler(handler);
			parser.parse(file.toURI().toString(), handler);
			itemm = handler.firstChild();
			handler.children().clear();
		} catch (final ParserConfigurationException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return itemm;

	}

	/**
	 * getXMLReader
	 * 
	 * @param parser
	 * @return
	 * @throws SAXException
	 */
	public static XMLReader getXMLReader(final SAXParser parser) throws SAXException {
		return parser.getXMLReader();
	}

	/**
	 * setDocumentLocator
	 */
	@Override
	public void setDocumentLocator(final Locator locator) {
		this.locator = locator;
	}

	/**
	 * startElement
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		// call addTextIfNeeded function
		addTextIfNeeded();
		final Element element = doc.createElement(qName);
		final String node = doc.getNodeValue();
		for (int i = 0; i < attributes.getLength(); i++) {
			element.setAttribute(attributes.getQName(i), attributes.getValue(i));
		}
		element.setAttribute("lineNumAttribName", String.valueOf(locator.getLineNumber()));
		element.setTextContent(node);
		elementStack.push(element);
		// start a new node and use it as the current
		final TreeItem<String> treeItem = new TreeItem<>(
				qName + " [" + element.getAttribute("lineNumAttribName") + "]");
		getChildren(this.item).add(treeItem);
		this.item = treeItem;
		element.getAttributes().getLength();
		if (element.hasAttributes()) {
			for (int i = 0; i < element.getAttributes().getLength(); i++) {
				final Node attr = element.getAttributes().item(i);
				if (!"lineNumAttribName".equals(attr.getNodeName())) {
					treeItem.getChildren().add(new TreeItem<>(attr.getNodeName() + " = " + attr.getNodeValue()));
				}
			}
		}
		final NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node nodeItem = nodeList.item(i);
			if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
				final TreeItem<String> childItem = XmlToGraph.createTreeItemFromElement((Element) nodeItem);
				treeItem.getChildren().add(childItem);
			} else if (nodeItem.getNodeType() == Node.TEXT_NODE) {
				final String textContent = nodeItem.getTextContent().trim();
				if (!textContent.isEmpty()) {
					treeItem.getChildren().add(new TreeItem<>(textContent));
				}
			}
		}
	}

	/**
	 * getChildren
	 * 
	 * @param item
	 * @return
	 */
	public static ObservableList<TreeItem<String>> getChildren(final TreeItem<String> item) {
		return item.getChildren();
	}

	/**
	 * endElement
	 */
	@Override
	public void endElement(final String uri, final String localName, final String qName) {
		addTextIfNeeded();
		final Element closedEl = elementStack.pop();
		if (elementStack.isEmpty()) {
			doc.appendChild(closedEl);
		} else {
			final Element parentEl = elementStack.peek();
			parentEl.appendChild(closedEl);
		}
		this.item = this.item.getParent();
	}

	/**
	 * characters
	 */
	@Override
	public void characters(final char character[], final int start, final int length) throws SAXException {
		textBuffer.append(character, start, length);
		final String string = String.valueOf(character, start, length).trim();
		if (!string.isEmpty()) {
			getChildren(this.item).add(new TreeItem<>(string));
		}
	}

	/**
	 * addTextIfNeeded Outputs text accumulated under the current node
	 */
	private void addTextIfNeeded() {
		if (textBuffer.length() > 0) {
			final Element element = elementStack.peek();
			final Node textNode = doc.createTextNode(textBuffer.toString());
			element.appendChild(textNode);
			textBuffer.delete(0, textBuffer.length());
		}
	}
}