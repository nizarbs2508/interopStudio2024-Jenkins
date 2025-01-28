package com.ans.cda.service.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XPathLineEvaluator
 */
public class XPathLineEvaluator {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XPathLineEvaluator.class);
	/**
	 * XPathLineEvaluator
	 */
	private XPathLineEvaluator() {
		// empty constructor
	}

	/**
	 * Function to get the XPath of a given node
	 */
	public static String getXPath(final Node node) {
		String retour;
		final Node parent = node.getParentNode();
		if (parent == null) {
			retour = "/" + node.getNodeName();
		} else {
			retour = getXPath(parent) + "/" + node.getNodeName() + "[" + getNodeIndex(node) + "]";
		}
		return retour;
	}

	/**
	 * Function to get the index of the node among its siblings
	 * 
	 * @param node
	 * @return
	 */
	public static int getNodeIndex(final Node node) {
		int index = 1; // XPath indices are 1-based
		Node sibling = node.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(node.getNodeName())) {
				index++;
			}
			sibling = sibling.getPreviousSibling();
		}
		return index;
	}

	/**
	 * evaluate
	 * 
	 * @param xmlString
	 * @param xpathExpression
	 * @return
	 */
	public static List<String> evaluate(final String xmlString, final String xpathExpression) {
		final List<String> result = new ArrayList<>();
		try (InputStream istream = new ByteArrayInputStream(xmlString.getBytes())) {
			final Document doc = PositionalXMLReader.readXML(istream);
			doc.getDocumentElement().normalize();
			final XPathFactory xPathFactory = XPathFactory.newInstance();
			final XPath xPath = xPathFactory.newXPath();
			final SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
			nsContext.addNamespace("", "urn:hl7-org:v3"); // Default namespace
			xPath.setNamespaceContext(nsContext);
			final String[] parts = xpathExpression.split("/");
			String xpathStr = "/";
			for (final String part : parts) {
				if (!part.isEmpty()) {
					if (!part.startsWith("@")) {
						if (part.contains("[")) {
							final String[] partss = part.split("\\[");
							final String partt1 = partss[0];
							String partt2 = partss[1];
							partt2 = partt2.replace("]", "");
							xpathStr = xpathStr.concat("/*[local-name()= '" + partt1 + "'][" + partt2 + "]");
						} else {
							xpathStr = xpathStr.concat("/*[local-name()= '" + part + "']");
						}
					} else {
						xpathStr = xpathStr.concat("/" + part);
					}
				}
			}
			final XPathExpression expr = xPath.compile(xpathStr);
			final NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() != 0) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == 2) {
						final Element elem = ((Attr) node).getOwnerElement();
						node = elem;
					}
					final String xpath = getXPath(node);
					result.add("\n" + xpath + " : Line " + node.getUserData("lineNumber"));
				}
			}
		} catch (final XPathExpressionException | IOException | SAXException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return result;
	}
}
