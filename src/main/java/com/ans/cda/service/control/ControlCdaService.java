package com.ans.cda.service.control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ans.cda.service.bom.BomService;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.LocalUtility;
import com.ans.cda.utilities.general.Utility;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * ControlCdaService
 * 
 * @author bensalem Nizar
 */
public final class ControlCdaService {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ControlCdaService.class);

	/**
	 * ControlCdaService
	 */
	private ControlCdaService() {
		// empty constructor
	}

	/**
	 * isUUID
	 * 
	 * @param testValue
	 * @return
	 */
	private static boolean isUUID(final String testValue) {
		boolean isUUID;
		final Pattern pattern = Pattern.compile("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$");
		final Matcher matcher = pattern.matcher(testValue);
		isUUID = matcher.matches();
		return isUUID;
	}

	/**
	 * checkUUID
	 * 
	 * @param xmlFile
	 */
	public static Map<List<String>, List<String>> checkUUID(final String xmlFile) {
		final Map<List<String>, List<String>> map = new ConcurrentHashMap<>();
		final List<String> validList = new ArrayList<>();
		final List<String> invalidList = new ArrayList<>();
		try {
			BomService.saveAsUTF8WithoutBOM(new File(xmlFile).getAbsolutePath(), StandardCharsets.UTF_8);
			// Open the XML.
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			// Find the id/@root elements
			final NodeList nodes = doc.getElementsByTagName("id");
			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);
				final NamedNodeMap attrs = node.getAttributes();
				final Node rootAttr = attrs.getNamedItem("root");
				final Node extAttr = attrs.getNamedItem("extension");
				if (rootAttr != null && extAttr == null) {
					final String myUUID = rootAttr.getNodeValue();
					final boolean isValid = isUUID(myUUID);
					if (isValid) {
						validList.add(myUUID);
					} else {
						invalidList.add(myUUID);
						// Création d'un UUID
						final UUID myuuid = UUID.randomUUID();
						final String myuuidAsString = myuuid.toString().toUpperCase(Locale.FRENCH);
						rootAttr.setNodeValue(myuuidAsString);
					}
				}
			}
			map.put(validList, invalidList);
			// Save the updated XML
			final TransformerFactory transformerFac = TransformerFactory.newInstance();
			Transformer transformer = transformerFac.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, Constant.YES);
			transformer.setOutputProperty(OutputKeys.ENCODING, Constant.UTF8);
			transformer.setOutputProperty(OutputKeys.METHOD, Constant.EXTXML1);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, Constant.YES);
			final ClassLoader classloader = Utility.newClassLoader();
			try (InputStream iStream = classloader.getResourceAsStream(Constant.PRETTY)) {
				transformer = transformerFac.newTransformer(new StreamSource(iStream));
				final DOMSource source = new DOMSource(doc);
				final StreamResult result = new StreamResult(new File(xmlFile));
				transformer.transform(source, result);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} catch (final TransformerException | SAXException | IOException | ParserConfigurationException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return map;
	}

	/**
	 * lookupxpath
	 * 
	 * @param xmlFile
	 * @param expression
	 * @return
	 */
	public static String lookupxpath(final String xmlFile, final String expression) {
		Boolean result1 = null;
		BomService.save(xmlFile);
		// Créer un processeur Saxon
		final Processor processor = new Processor(false); // false pour ne pas utiliser le mode XSLT
		// Créer un document source à partir de la chaîne XML
		final net.sf.saxon.s9api.DocumentBuilder docBuilder = processor.newDocumentBuilder();
		try {
			final String xmlContent = new String(Files.readAllBytes(new File(xmlFile).toPath()), StandardCharsets.UTF_8)
					.trim();

			final XdmNode document = docBuilder.build(new StreamSource(new StringReader(xmlContent)));
			// Créer un compilateur XPath
			final XPathCompiler xpathCompiler = processor.newXPathCompiler();
			xpathCompiler.declareNamespace("", "urn:hl7-org:v3");
			// Expression XPath pour vérifier la présence d'un templateId avec un attribut
			// root spécifique
			final String xpathExpression = "boolean(" + expression + ")";
			// Compiler l'expression XPath
			final XPathExecutable xpathExecutable = xpathCompiler.compile(xpathExpression);
			final XPathSelector xpathSelector = xpathExecutable.load();
			xpathSelector.setContextItem(document);
			final XdmItem resultItem = xpathSelector.evaluateSingle();
			result1 = (resultItem != null) && Boolean.parseBoolean(resultItem.getStringValue());
			// Afficher le résultat
		} catch (final IOException | SaxonApiException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

		return result1.toString();
	}

	/**
	 * controleLoincCodes
	 * 
	 * @param xmlFile
	 */
	public static Map<List<String>, List<String>> controleLoincCodes(final String xmlFile) {
		final List<String> listeCodes = new ArrayList<>();
		final List<String> listeLibelles = new ArrayList<>();
		final Map<List<String>, List<String>> map = new ConcurrentHashMap<>();
		final List<String> validList = new ArrayList<>();
		final List<String> invalidList = new ArrayList<>();
		final String url = Utility.getContextClassLoader().getResource(Constant.LOINCFILE).toExternalForm();
		File dest;
		dest = new File(Constant.FILENAME + "\\Loinc.csv");
		if (!dest.exists()) {
			try {
				FileUtils.copyURLToFile(new URL(url), dest);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Paths.get(dest.getAbsolutePath())))) {
			String line;
			while ((line = reader.readLine()) != null) {
				final String[] values = line.split(",");
				listeCodes.add(values[0].replace("\"", ""));
				listeLibelles.add(values[1].replace("\"", ""));
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		// Contrôle les codes LOINC dans les balises
		// //*:observation/*:code[@codeSystem='2.16.840.1.113883.6.1']/@code
		final Processor processor = new Processor(false);
		final XPathCompiler compiler = processor.newXPathCompiler();
		XPathSelector selector;
		try {
			selector = compiler.compile("//*:observation/*:code[@codeSystem='2.16.840.1.113883.6.1']/@code/string()")
					.load();
			selector.setContextItem(processor.newDocumentBuilder().build(new File(xmlFile)));
			final XdmValue xdmValues = selector.evaluate();
			for (final XdmItem myItem : xdmValues) {
				if (listeCodes.contains(myItem.getStringValue())) {
					final int index = listeCodes.indexOf(myItem.getStringValue());
					validList.add(myItem.getStringValue() + " " + listeLibelles.get(index));
				} else {
					invalidList.add(myItem.getStringValue());
				}
			}
			map.put(validList, invalidList);
		} catch (final SaxonApiException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			dest.delete();
		}
		return map;

	}

	/**
	 * printFileSizeNIO
	 * 
	 * @param fileName
	 */
	public static String printFileSizeNIO(final String fileName) {
		String strFinal = "";
		final Path path = Paths.get(fileName);
		try {
			// size of a file (in bytes)
			final long bytes = Files.size(path);
			strFinal = "\n" + LocalUtility.getString("message.size") + "(bytes) : "
					+ String.format("%,d bytes", bytes).concat("\n" + LocalUtility.getString("message.size")
							+ "(kilobytes) : " + String.format("%,d kilobytes", bytes / 1024));

		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return strFinal;

	}

	/**
	 * getHash
	 * 
	 * @param pCheminDocumentXML
	 * @return
	 */
	public static String getHash(final String pCheminXML) {
		String result = "Hash : ";
		try {
			final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			try (FileInputStream fis = new FileInputStream(pCheminXML);
					BufferedInputStream bis = new BufferedInputStream(fis)) {
				final byte[] buffer = new byte[8192];
				int read;
				while ((read = bis.read(buffer)) != -1) {
					sha1.update(buffer, 0, read);
				}
			}
			final byte[] hash = sha1.digest();
			final StringBuilder formatted = new StringBuilder(2 * hash.length);
			for (final byte bytes : hash) {
				formatted.append(String.format("%02X", bytes));
			}
			result = result + formatted.toString();
		} catch (final NoSuchAlgorithmException | IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return result;
	}

	/**
	 * downloadFileUsingNIO
	 * 
	 * @param urlStr
	 * @param file
	 * @throws IOException
	 */
	public static void downloadUsingNIO(final String urlStr, final String file) throws IOException {
		final URL url = new URL(urlStr);
		FileUtils.copyURLToFile(url, new File(file));
	}

	/**
	 * bomAllCda
	 * 
	 * @param files
	 */
	public static String bomAllCda(final File... files) {
		final StringBuilder result = new StringBuilder();
		for (final File file : files) {
			try {
				saveAsUTF8WithoutBOM(file.getAbsolutePath(), StandardCharsets.UTF_8);
				result.append(LocalUtility.get("message.bom.delete") + file.getName() + ". \n");
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return result.toString().trim();
	}

	/**
	 * saveAsUTF8WithoutBOM
	 * 
	 * @param fileName
	 * @param encoding
	 * @throws IOException
	 */
	public static void saveAsUTF8WithoutBOM(final String fileName, final Charset encoding) throws IOException {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName");
		}
		final String content = new String(Files.readAllBytes(Paths.get(fileName)), encoding);
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
			writer.write(content);
			writer.close();
		}
	}
}