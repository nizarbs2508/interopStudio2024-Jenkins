package com.ans.cda.utilities.general;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Utility
 * 
 * @author bensalem Nizar
 */
public final class Utility {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(Utility.class);

	/**
	 * Utilities constructor
	 */
	private Utility() {
		// empty constructor
	}

	/**
	 * getFile
	 * 
	 * @param string
	 * @return
	 */
	public static File getFile(final String string) {
		return new File(string);
	}

	/**
	 * open html file in browser
	 * 
	 * @param file
	 */
	public static void openFile(final File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
	}

	/**
	 * remove extension from file name
	 * 
	 * @param s
	 * @return string
	 */
	public static String removeExtension(final String string) {
		return string != null && string.lastIndexOf('.') > 0 ? string.substring(0, string.lastIndexOf('.')) : string;
	}

	/**
	 * getStyleScene
	 * 
	 * @param scene
	 */
	public static ObservableList<String> getStyleScene(final Scene scene) {
		return scene.getStylesheets();
	}

	/**
	 * getWebEngine
	 * 
	 * @param browserEngine
	 */
	public static WebEngine getWebEngine(final WebView browserEngine) {
		return browserEngine.getEngine();
	}

	/**
	 * getButtonTypes
	 * 
	 * @param alert
	 * @return
	 */
	public static ObservableList<ButtonType> getButtonTypes(final Alert alert) {
		return alert.getButtonTypes();
	}

	/**
	 * getChildrenNode
	 * 
	 * @param vBox
	 * @return
	 */
	public static ObservableList<Node> getChildrenNode(final VBox vBox) {
		return vBox.getChildren();
	}

	/**
	 * newObsMenu
	 * 
	 * @param fileChooser
	 */
	public static ObservableList<ExtensionFilter> newExtFilter(final FileChooser fileChooser) {
		return fileChooser.getExtensionFilters();
	}

	/**
	 * getItemsSplit
	 * 
	 * @param splitPane
	 * @return
	 */
	public static ObservableList<Node> getItemsSplit(final SplitPane splitPane) {
		return splitPane.getItems();
	}

	/**
	 * getChildrenHNode
	 * 
	 * @param hBox
	 * @return
	 */
	public static ObservableList<Node> getChildrenHNode(final HBox hBox) {
		return hBox.getChildren();
	}

	/**
	 * encodeBase64
	 * 
	 * @param input
	 * @return
	 */
	public static String encodeBase64(final String input) {
		return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * readFile
	 * 
	 * @param filePath
	 * @return
	 */
	public static String readFile(final String filePath) {
		final Path path = Path.of(filePath);
		String content = null;
		try {
			content = Files.readString(path);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return content;
	}

	/**
	 * writeFile
	 * 
	 * @param filePath
	 * @param content
	 */
	public static void writeFile(final String filePath, final String content) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
			writer.write(content);
			writer.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * getXpathSingleValue
	 * 
	 * @param file
	 * @return
	 */
	public static String getXpathSingleValue(final File file, final boolean done) {
		String content = null;
		if (done) {
			try {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document = builder.parse(file);
				document.getDocumentElement().normalize();
				final NodeList nList = document.getElementsByTagName("evs:status");
				if (nList != null && nList.getLength() > 0) {
					for (int temp = 0; temp < nList.getLength(); temp++) {
						final org.w3c.dom.Node node = nList.item(temp);
						if (org.w3c.dom.Node.ELEMENT_NODE == node.getNodeType()) {
							final Element eElement = (Element) node;
							content = eElement.getTextContent();
						}
					}
				}
			} catch (final ParserConfigurationException | SAXException | IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return content;
	}

	/**
	 * isXMLFile
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isXMLFile(final File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.parse(file);
			return true; // Parsing succeeded, the file is valid XML
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return false; // Parsing failed, the file is not valid XML
		}
	}

	/**
	 * getXpathSingleValue
	 * 
	 * @param file
	 * @return
	 */
	public static String getXpathSingleValue(final File file) {
		String content = null;
		final boolean isXml = isXMLFile(file);
		if (isXml) {
			try {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document = builder.parse(file);
				document.getDocumentElement().normalize();
				final NodeList nList = document.getElementsByTagName("evs:status");
				if (nList != null && nList.getLength() > 0) {
					for (int temp = 0; temp < nList.getLength(); temp++) {
						final org.w3c.dom.Node node = nList.item(temp);
						if (org.w3c.dom.Node.ELEMENT_NODE == node.getNodeType()) {
							final Element eElement = (Element) node;
							content = eElement.getTextContent();
						}
					}
				}
			} catch (final ParserConfigurationException | SAXException | IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return content;
	}

	/**
	 * getOutputStream
	 * 
	 * @param httpCon
	 * @return
	 * @throws IOException
	 */
	public static OutputStream getOutputStream(final HttpURLConnection httpCon) throws IOException {
		return httpCon.getOutputStream();
	}

	/**
	 * getEngine
	 * 
	 * @param browserEngine
	 * @return
	 */
	public static WebEngine getEngine(final WebView browserEngine) {
		return browserEngine.getEngine();
	}

	/**
	 * getHistory
	 * 
	 * @param browserEngine
	 * @return
	 */
	public static WebHistory getHistory(final WebView browserEngine) {
		return getEngine(browserEngine).getHistory();
	}

	/**
	 * getLoadWorker
	 * 
	 * @param browserEngine
	 * @return
	 */
	public static Worker<Void> getLoadWorker(final WebView browserEngine) {
		return getEngine(browserEngine).getLoadWorker();
	}

	/**
	 * getPopupContent
	 * 
	 * @param popup
	 * @return
	 */
	public static ObservableList<Node> getPopupContent(final Popup popup) {
		return popup.getContent();
	}

	/**
	 * getIcons
	 * 
	 * @param stage
	 * @return
	 */
	public static ObservableList<Image> getIcons(final Stage stage) {
		return stage.getIcons();
	}

	/**
	 * getStylesheets
	 * 
	 * @param menuBar
	 * @return
	 */
	public static ObservableList<String> getStylesheets(final MenuBar menuBar) {
		return menuBar.getStylesheets();
	}

	/**
	 * getStylesheets
	 * 
	 * @param menuBar
	 * @return
	 */
	public static ObservableList<String> getStylesheets(final Button menuBar) {
		return menuBar.getStylesheets();
	}

	/**
	 * getStylesheets
	 * 
	 * @param pane
	 * @return
	 */
	public static ObservableList<String> getStylesheets(final DialogPane pane) {
		return pane.getStylesheets();
	}

	/**
	 * getStyleClass
	 * 
	 * @param pane
	 * @return
	 */
	public static ObservableList<String> getStyleClass(final DialogPane pane) {
		return pane.getStylesheets();
	}

	/**
	 * getTreeItem
	 * 
	 * @param pane
	 * @return
	 */
	public static ObservableList<TreeItem<String>> getTreeItem(final TreeItem<String> pane) {
		return pane.getChildren();
	}

	/**
	 * getChild
	 * 
	 * @param child
	 * @return
	 */
	public static String getChild(final TreeItem<String> child) {
		return child.getValue();
	}

	/**
	 * getChildren
	 * 
	 * @param hBox
	 * @return
	 */
	public static ObservableList<Node> getChildren(final HBox hBox) {
		return hBox.getChildren();
	}

	/**
	 * getChildren
	 * 
	 * @param vBox
	 * @return
	 */
	public static ObservableList<Node> getChildren(final VBox vBox) {
		return vBox.getChildren();
	}

	/**
	 * getChildrenUnmodifiable
	 * 
	 * @param browser
	 * @return
	 */
	public static ObservableList<Node> getChildrenUnmodifiable(final WebView browser) {
		return browser.getChildrenUnmodifiable();
	}

	/**
	 * getChildrenUnmodifiable
	 * 
	 * @param browser
	 * @return
	 */
	public static ObservableList<Node> getChildrenUnmodifiable(final TextArea browser) {
		return browser.getChildrenUnmodifiable();
	}

	/**
	 * getItems
	 * 
	 * @param menu
	 * @return
	 */
	public static ObservableList<MenuItem> getItems(final Menu menu) {
		return menu.getItems();
	}

	/**
	 * newClassLoader
	 * 
	 */
	public static ClassLoader newClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * getOrientation
	 * 
	 * @param sbuilder
	 * @return
	 */
	public static Orientation getOrientation(final ScrollBar sbuilder) {
		return sbuilder.getOrientation();
	}

	/**
	 * getContextClassLoader
	 * 
	 * @return
	 */
	public static ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * getXMLReader
	 * 
	 * @return
	 * @throws SAXException
	 */
	public static XMLReader getXMLReader(final SAXParser parser) throws SAXException {
		return parser.getXMLReader();
	}

	/**
	 * transform
	 * 
	 * @param xmlFile
	 */
	public static void transform(final String xmlFile) {
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Transformer transformer = null;
		TransformerFactory transformerFac = null;
		Document doc = null;
		StreamResult result;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new File(xmlFile));
			transformerFac = TransformerFactory.newInstance();
			transformer = transformerFac.newTransformer();
		} catch (final ParserConfigurationException | TransformerConfigurationException | SAXException
				| IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		transformer.setOutputProperty(OutputKeys.INDENT, Constant.YES);
		transformer.setOutputProperty(OutputKeys.ENCODING, Constant.UTF8);
		transformer.setOutputProperty(OutputKeys.METHOD, Constant.EXTXML1);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, Constant.YES);
		try (InputStream iStream = newClassLoader().getResourceAsStream(Constant.PRETTY)) {
			transformer = transformerFac.newTransformer(new StreamSource(iStream));
			final DOMSource source = new DOMSource(doc);
			result = new StreamResult(new File(xmlFile));
			transformer.transform(source, result);
		} catch (final IOException | TransformerException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * readFileContents
	 * 
	 * @param selectedFile
	 * @throws IOException
	 */
	public static String readFileContents(final InputStream file) throws IOException {
		String singleString;
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(file))) {
			final StringBuilder sbuilder = new StringBuilder();
			String line = bReader.readLine();
			while (line != null) {
				sbuilder.append(line).append(Constant.RETOURCHARIOT);
				line = bReader.readLine();
			}
			singleString = sbuilder.toString();
		}
		return singleString;
	}
}