package com.ans.cda.service.parametrage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ans.cda.utilities.general.Utility;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * XmlToGraph
 * 
 * @author Nizar Ben Salem
 */
public class XmlToGraph {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XmlToGraph.class);
	/**
	 * NULL
	 */
	private static final Document NULL = null;
	/**
	 * lineContentMap
	 */
	public static final Map<Integer, String> lineContentMap = new ConcurrentHashMap<>();

	/**
	 * XmlToGraph
	 */
	private XmlToGraph() {
		// empty constructor
	}

	/**
	 * loadXmlDocument
	 * 
	 * @param filePath
	 * @return
	 */
	public static Document loadXmlDocument(final String filePath) {
		Document doc;
		try {
			final File file = new File(filePath);
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document document = dBuilder.parse(file);
			document.getDocumentElement().normalize();
			doc = document;
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			doc = NULL;
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return doc;
	}

	/**
	 * expandAll
	 * 
	 * @param item
	 */
	public static void expandAll(final TreeItem<String> item) {
		if (item == null) {
			return;
		}
		item.setExpanded(true);
		for (final TreeItem<String> child : Utility.getTreeItem(item)) {
			if (Utility.getChild(child).contains("ClinicalDocument")
					|| Utility.getChild(child).contains("ns5:SubmitObjectsRequest")
					|| Utility.getChild(child).contains("RegistryObjectList")) {
				expandAll(child);
			}
		}
	}

	/**
	 * parseXmlToTreeItem
	 * 
	 * @param filePath
	 * @return
	 */
	public static TreeItem<String> parseXmlToTreeItem(final String filePath, final TreeView<String> treeView) {
		TreeItem<String> item;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(filePath);
			document.getDocumentElement().normalize();
			final Element rootElement = document.getDocumentElement();
			item = createTreeItemFromElement(rootElement);
		} catch (final IOException | ParserConfigurationException | SAXException e) {
			item = new TreeItem<>("Error loading XML");
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return item;
	}

	/**
	 * selectedItemProperty
	 * 
	 * @param tvSelModel
	 * @return
	 */
	public static ReadOnlyObjectProperty<TreeItem<String>> selectedItemProperty(
			final MultipleSelectionModel<TreeItem<String>> tvSelModel) {
		return tvSelModel.selectedItemProperty();
	}

	/**
	 * getSelectionModelTV
	 * 
	 * @param treeView
	 * @return
	 */
	public static MultipleSelectionModel<TreeItem<String>> getSelectionModelTV(final TreeView<String> treeView) {
		return treeView.getSelectionModel();
	}

	/**
	 * createTreeItemFromElement
	 * 
	 * @param element
	 * @return
	 */
	public static TreeItem<String> createTreeItemFromElement(final Element element) {
		final String elementText = element.getTagName();
		final TreeItem<String> treeItem = new TreeItem<>(elementText);
		element.getAttributes().getLength();
		if (element.hasAttributes()) {
			for (int i = 0; i < element.getAttributes().getLength(); i++) {
				final Node attr = element.getAttributes().item(i);
				if (!"lineNumAttribName".equals(attr.getNodeName())) {
					Utility.getTreeItem(treeItem).add(new TreeItem<>(attr.getNodeName() + " = " + attr.getNodeValue()));
				}
			}
		}
		final NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				final TreeItem<String> childItem = createTreeItemFromElement((Element) node);
				Utility.getTreeItem(treeItem).add(childItem);
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				final String textContent = node.getTextContent().trim();
				if (!textContent.isEmpty()) {
					Utility.getTreeItem(treeItem).add(new TreeItem<>(textContent));
				}
			}
		}
		return treeItem;
	}

}