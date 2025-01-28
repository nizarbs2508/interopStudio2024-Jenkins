package com.ans.cda.service.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ans.cda.service.bom.BomService;
import com.ans.cda.service.parametrage.IniFile;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.LocalUtility;

import javafx.stage.Stage;

/**
 * ValidationService
 * 
 * @author bensalem Nizar
 */
public class ValidationService {

	/**
	 * SREP
	 */
	public static final String SREP = "\\TOOLS\\ph-schematron";
	/**
	 * CONFIG
	 */
	private static final File CONFIG = new File(Constant.INTEROPFOLDER + "\\config.properties");
	/**
	 * GROOTDIRECTORY
	 */
	public static final String GROOTDIRECTORY = Constant.FILENAME + "\\interopStudio\\Validation" + SREP;
	/**
	 * API
	 */
	public static final String API = Constant.FILENAME + "\\interopStudio\\Validation\\API";
	/**
	 * GPOOLDIRECTORY
	 */
	private static String gPoolDirectory;
	/**
	 * gFOPDirectory
	 */
	private static String gFOPDirectory;
	/**
	 * GSCHDIRECTORY
	 */
	private static String gSchDirectory;
	/**
	 * GSVRLDIRECTORY
	 */
	private static String gSvrlDirectory;
	/**
	 * GSVRLDIRECTORY
	 */
	private static String gSvrlDirectoryXsl;
	/**
	 * GREPORTSDIRECTORY
	 */
	private static String gReportsDirectory;
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ValidationService.class);

	/**
	 * ValidationService
	 */
	private ValidationService() {
		// empty constructor
	}

	/**
	 * validateMetaCda
	 * 
	 * @param textfield
	 * @param listeTypeValidation
	 */
	public static String validateMeta(final File textfield, final String sValidationSName, final String sValidatorName,
			final String validationUrl) {
		final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("MyLog");
		String display = "";
		if (Files.exists(Paths.get(textfield.getAbsolutePath()))) {
			try {
				final Instant startTime = Instant.now();
				// Récupérer le nom du validateur META
				final String content = Files.readString(textfield.toPath(), StandardCharsets.UTF_8);
				// call valideDOCUMENT function
				int count = 0;
				String validationResult = SaxonValidator.valideDocument(content, sValidationSName, sValidatorName,
						validationUrl);
				while (validationResult == null && count <= 5) {
					validationResult = SaxonValidator.valideDocument(content, sValidationSName, sValidatorName,
							validationUrl);
					count++;
				}
				final Instant endTime = Instant.now();
				display = display + LocalUtility.getString("message.start.treatment") + Instant.now() + "\n"
						+ LocalUtility.getString("message.valid.treatment") + validationResult + "\n"
						+ LocalUtility.getString("message.duration.treatment")
						+ Duration.between(startTime, endTime).getSeconds() + " "
						+ LocalUtility.getString("message.second.treatment") + "\n"
						+ LocalUtility.getString("message.completed.treatment") + "\n\n"
						+ LocalUtility.getString("message.created.treatment") + "\n" + SaxonValidator.getNewFilePath()
						+ "\n" + SaxonValidator.getNewFilePath1() + "\n" + SaxonValidator.getNewFilePath2() + "\n"
						+ SaxonValidator.getNewFilePath3() + "\n";

			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} else {
			display = LocalUtility.getString("message.invalid.meta.file");
		}
		if (!Constant.LOGFILE.exists()) {
			try {
				Constant.LOGFILE.createNewFile();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		try (FileWriter myWriter = new FileWriter(Constant.LOGFILE)) {
			myWriter.write(display);
			myWriter.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
				logger.log(Level.SEVERE, error);
			}
		}
		return display;
	}

	/**
	 * displayLastReport
	 * 
	 * @param webEngine
	 */
	public static String displayLastReport() {
		return SaxonValidator.displayLastReport();
	}

	/**
	 * createValidFolder
	 */
	public static File createValidFolder(final File file) {
		final File theDir = new File(file + "\\VALID_CDA");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		return theDir;
	}

	/**
	 * createInValidFolder
	 */
	public static File createInValidFolder(final File file) {
		final File theDir = new File(file + "\\INVALID_CDA");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		return theDir;
	}

	/**
	 * createFolder
	 * 
	 * @param theDir
	 * @return
	 */
	public static File createFolder(final File theDir) {
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		return theDir;
	}

	/**
	 * displayLastReport
	 * 
	 * @param webEngine
	 */
	public static String displayLastReport(final String svrl) {
		return SaxonValidator.displayLastReport(svrl);
	}

	/**
	 * validateMetaCda
	 * 
	 * @param textfield
	 * @param listeTypeValidation
	 */
	public static String validateCda(final File textfield, final String sValidationSN, final String validationUrl,
			final String type, final Stage stage) {
		FileHandler fhandler = null;
		final SimpleFormatter formatter = new SimpleFormatter();
		String sValidatorName = "";
		String display = "";
		// API validation
		if (Files.exists(Paths.get(textfield.getAbsolutePath()))) {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("MyLog");
			try {
				fhandler = new FileHandler(Constant.LOGFOLDFER + "\\log.log", true);
				LOGGER.addHandler(fhandler);
				fhandler.setFormatter(formatter);
				BomService.saveAsUTF8WithoutBOM(textfield.getAbsolutePath(), StandardCharsets.UTF_8);
				dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				final DocumentBuilder dbuilder = dbf.newDocumentBuilder();
				final Document doc = dbuilder.parse(new File(textfield.getAbsolutePath()));
				doc.getDocumentElement().normalize();
				final NodeList list = doc.getElementsByTagNameNS("urn:hl7-org:v3", "templateId");
				for (int temp = 0; temp < list.getLength(); temp++) {
					final Node node = list.item(temp);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						if (node.getParentNode().getNodeName().contains("ClinicalDocument")) {
							final Element element = (Element) node;
							final String root = element.getAttribute("root");
							final String extension = element.getAttribute("extension");
							if (extension != null && !extension.isEmpty()) {
								final String sRep = IniFile.read(root, "API-MAPPING");
								sValidatorName = sRep;
								break;
							} else {
								final String sRep = IniFile.read(root, "API-MAPPING");
								sValidatorName = sRep;
							}
						}
					}
				}

				final Instant startTime = Instant.now();
				// Récupérer le nom du validateur META
				final String content = new String(Files.readAllBytes(Paths.get(textfield.getAbsolutePath())));
				// call valideDOCUMENT function
				int count = 0;
				String validationResult = SaxonValidator.valideDocument(content, sValidationSN, sValidatorName,
						validationUrl);
				while (validationResult == null && count <= 5) {
					validationResult = SaxonValidator.valideDocument(content, sValidationSN, sValidatorName,
							validationUrl);
					count++;
				}

				if (validationResult != null) {
					final Instant endTime = Instant.now();
					display = LocalUtility.getString("message.start.treatment") + Instant.now() + "\n"
							+ LocalUtility.getString("message.valid.treatment") + validationResult + "\n"
							+ LocalUtility.getString("message.duration.treatment")
							+ Duration.between(startTime, endTime).getSeconds() + " "
							+ LocalUtility.getString("message.second.treatment") + "\n"
							+ LocalUtility.getString("message.completed.treatment") + "\n\n"
							+ LocalUtility.getString("message.created.treatment") + "\n"
							+ SaxonValidator.getNewFilePath() + "\n" + SaxonValidator.getNewFilePath1() + "\n"
							+ SaxonValidator.getNewFilePath2() + "\n" + SaxonValidator.getNewFilePath3() + "\n";
				} else {
					final Instant endTime = Instant.now();
					display = LocalUtility.getString("message.start.treatment") + Instant.now() + "\n"
							+ LocalUtility.getString("message.valid.treatment") + validationResult + "\n"
							+ LocalUtility.getString("message.duration.treatment")
							+ Duration.between(startTime, endTime).getSeconds()
							+ LocalUtility.getString("message.second.treatment") + "\n"
							+ LocalUtility.getString("message.error.server") + "\n\n"
							+ LocalUtility.getString("message.created.treatment") + "\n"
							+ SaxonValidator.getNewFilePath() + "\n" + SaxonValidator.getNewFilePath1() + "\n"
							+ SaxonValidator.getNewFilePath2() + "\n" + SaxonValidator.getNewFilePath3() + "\n";
				}
			} catch (final IOException | ParserConfigurationException | SAXException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
					LOGGER.log(Level.SEVERE, error, fhandler);
				}
			} finally {
				LOGGER.log(Level.INFO, display, fhandler);
				fhandler.close();
			}
		} else {
			display = LocalUtility.getString("message.invalid.cda.file");
		}
		return display;
	}

	/**
	 * getgPoolDirectory
	 * 
	 * @return
	 */
	public static String getgPoolDirectory() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String toValidate = prop.getProperty("folder.tovalidate");
				if (toValidate != null && !toValidate.isEmpty()) {
					gPoolDirectory = toValidate;
				} else {
					gPoolDirectory = GROOTDIRECTORY + "\\to_validate";
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gPoolDirectory = GROOTDIRECTORY + "\\to_validate";
		}
		return gPoolDirectory;
	}

	/**
	 * getFOPDirectory
	 * 
	 * @return
	 */
	public static String getFOPDirectory() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String toValidate = prop.getProperty("folder.fop");
				if (toValidate != null && !toValidate.isEmpty()) {
					gFOPDirectory = toValidate;
				} else {
					gFOPDirectory = "";
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gFOPDirectory = "";
		}
		return gFOPDirectory;
	}

	/**
	 * setgPoolDirectory
	 * 
	 * @param gPoolDirectory
	 */
	public static void setgPoolDirectory(final String gPoolDirectory) {
		setgPoolDirectory(gPoolDirectory);
	}

	/**
	 * getgSchDirectory
	 * 
	 * @return
	 */
	public static String getgSchDirectory() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String schFolder = prop.getProperty("folder.schematron");
				if (schFolder != null && !schFolder.isEmpty()) {
					gSchDirectory = schFolder;
				} else {
					gSchDirectory = GROOTDIRECTORY + "\\schematron";
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gSchDirectory = GROOTDIRECTORY + "\\schematron";
		}
		return gSchDirectory;
	}

	/**
	 * setgSchDirectory
	 * 
	 * @param gSchDirectory
	 */
	public static void setgSchDirectory(final String gSchDirectory) {
		setgSchDirectory(gSchDirectory);
	}

	/**
	 * getgSvrlDirectory
	 * 
	 * @return
	 */
	public static String getgSvrlDirectory() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String destSchFolder = prop.getProperty("folder.schematron.destination");
				if (destSchFolder != null && !destSchFolder.isEmpty()) {
					gSvrlDirectory = destSchFolder;
				} else {
					gSvrlDirectory = new File(GROOTDIRECTORY).getParent() + Constant.GLOBALSVRL;
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gSvrlDirectory = new File(GROOTDIRECTORY).getParent() + Constant.GLOBALSVRL;
		}
		return gSvrlDirectory;
	}

	/**
	 * setgSvrlDirectory
	 * 
	 * @param gSvrlDirectory
	 */
	public static void setgSvrlDirectory(final String gSvrlDirectory) {
		setgSvrlDirectory(gSvrlDirectory);
	}

	/**
	 * getgSvrlDirectoryXsl
	 * 
	 * @return
	 */
	public static String getgSvrlDirectoryXsl() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String xslFolder = prop.getProperty("folder.xsl.destination");
				if (xslFolder != null && !xslFolder.isEmpty()) {
					gSvrlDirectoryXsl = xslFolder;
				} else {
					gSvrlDirectoryXsl = new File(GROOTDIRECTORY).getParent() + Constant.GLOBALSVRL;
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gSvrlDirectoryXsl = new File(GROOTDIRECTORY).getParent() + Constant.GLOBALSVRL;
		}
		return gSvrlDirectoryXsl;
	}

	/**
	 * getNameWithoutExtension
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getNameWithoutExtension(final String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
	}

	/**
	 * setgSvrlDirectoryXsl
	 * 
	 * @param gSvrlDirectoryXsl
	 */
	public static void setgSvrlDirectoryXsl(final String gSvrlDirectoryXsl) {
		setgSvrlDirectoryXsl(gSvrlDirectoryXsl);
	}

	/**
	 * getgReportsDirectory
	 * 
	 * @return
	 */
	public static String getgReportsDirectory() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String srcSchFolder = prop.getProperty("folder.schematron.source");
				if (srcSchFolder != null && !srcSchFolder.isEmpty()) {
					gReportsDirectory = srcSchFolder;
				} else {
					gReportsDirectory = GROOTDIRECTORY + "\\reports";
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			gReportsDirectory = GROOTDIRECTORY + "\\reports";
		}
		return gReportsDirectory;
	}

	/**
	 * setgReportsDirectory
	 * 
	 * @param gReportsDirectory
	 */
	public static void setgReportsDirectory(final String gReportsDirectory) {
		setgReportsDirectory(gReportsDirectory);
	}
}