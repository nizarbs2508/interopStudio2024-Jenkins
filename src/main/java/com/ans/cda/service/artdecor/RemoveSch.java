package com.ans.cda.service.artdecor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * RemoveSch
 * 
 * @author Nizar Ben Salem
 */
public class RemoveSch {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(RemoveSch.class);

	/**
	 * RemoveSch constructor
	 */
	private RemoveSch() {
		// empty constructor
	}

	/**
	 * extract
	 * 
	 * @param filePath
	 * @return
	 */
	public static List<String> extract(final String filePath) {
		final List<String> list = new ArrayList<>();
		try {
			final File file = new File(filePath);
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(file);
			final XPathFactory xPathFactory = XPathFactory.newInstance();
			final XPath xPath = xPathFactory.newXPath();
			final String expression = "/schema/include";
			final XPathExpression xPathExpression = xPath.compile(expression);
			final NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
			final String parent = file.getParent();
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					final String href = node.getAttributes().getNamedItem("href").getNodeValue();
					list.add(parent + "\\" + href);
				}
			}

		} catch (final Exception e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return list;
	}

	/**
	 * validate
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	public static boolean validate(final String filePath) {
		Writer out = null;
		boolean bool = false;
		try {
			final File file = new File(filePath);
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(file);
			final XPath xPath = XPathFactory.newInstance().newXPath();
			final String[] listXPath = { "//rule[contains(@context,'structuredBody')]/extends[@rule='AD']/..",
					"//rule[contains(@context,'structuredBody')]/assert[starts-with(text() ,'(CI-SISAddr)')]/..",
					"//rule[contains(@context,'structuredBody')]/assert[starts-with(text() ,'(CI-SISTelecom)')]/.." };
			for (final String str : listXPath) {
				final NodeList nList = (NodeList) xPath.compile(str).evaluate(doc, XPathConstants.NODESET);
				for (int cpt = 0; cpt < nList.getLength(); cpt++) {
					final Node nNode = nList.item(cpt);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						nNode.getParentNode().removeChild(nNode);
					}
				}
			}
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			final DOMSource source = new DOMSource(doc);
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			final String xmlString = writer.getBuffer().toString();
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			out.write(xmlString.replace("xmlns=\"\"", ""));
			bool = true;
		} catch (final ParserConfigurationException | SAXException | XPathExpressionException | TransformerException
				| IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();

				LOG.error(error);
				bool = false;
			}
		} finally {
			try {
				out.close();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
					bool = false;
				}
			}
		}
		return bool;
	}
}