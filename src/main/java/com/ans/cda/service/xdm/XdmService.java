package com.ans.cda.service.xdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ans.cda.service.bom.BomService;
import com.ans.cda.service.parametrage.IniFile;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.Utility;

import javafx.stage.Stage;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * XdmService
 * 
 * @author bensalem Nizar
 */
public class XdmService {

	/**
	 * sCheminCourantExe
	 */
	public static final String SCHEMINCOURANTEXE = System.getProperty("user.dir");
	/**
	 * URN1
	 */
	private static final String URN1 = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
	/**
	 * AUTHOR
	 */
	private static final String AUTHOR = "//*:author[";
	/**
	 * CLINAUTHOR
	 */
	private static final String CLINAUTHOR = "//*:ClinicalDocument/*:author[";
	/**
	 * VALUE
	 */
	private static final String VALUE = "ns2:Value";
	/**
	 * LOCALE
	 */
	private static final String LOCALE = "ns2:LocalizedString";
	/**
	 * LANG
	 */
	private static final String LANG = "xml:lang";
	/**
	 * CHARSET
	 */
	private static final String CHARSET = "charset";
	/**
	 * name1
	 */
	private static final String NAME1 = "ns2:Name";
	/**
	 * ISO
	 */
	private static final String ISO = "&amp;ISO^";
	/**
	 * PID
	 */
	private static final String PID = "PID-5|";
	/**
	 * NAME
	 */
	private static final String NAME = "name";
	/**
	 * UTF8
	 */
	private static final String UTF8 = "UTF8";
	/**
	 * VAL
	 */
	private static final String VAL = "value";
	/**
	 * VLIST
	 */
	private static final String VLIST = "ns2:ValueList";
	/**
	 * EXCLAMATION
	 */
	private static final String EXCLAMATION = "!!!{";
	/**
	 * COMM1
	 */
	private static final String COMM1 = "##################################################################";
	/**
	 * COMM2
	 */
	private static final String COMM2 = "===================";
	/**
	 * CLASSIFICATION
	 */
	private static final String CLASSIFICATION = "ns2:Classification";
	/**
	 * REGISTRY
	 */
	private static final String REGISTRY = "registryObject";
	/**
	 * IDSCHEME
	 */
	private static final String IDSCHEME = "identificationScheme";
	/**
	 * EXIDENTIFIER
	 */
	private static final String EXIDENTIFIER = "ns2:ExternalIdentifier";
	/**
	 * SLOT
	 */
	private static final String SLOT = "ns2:Slot";
	/**
	 * SCHEME
	 */
	private static final String SCHEME = "classificationScheme";
	/**
	 * CODESCH
	 */
	private static final String CODESCH = "codingScheme";
	/**
	 * CLASS OBJ
	 */
	private static final String CLASSOBJ = "classifiedObject";
	/**
	 * NODE
	 */
	private static final String NODE = "nodeRepresentation";
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XdmService.class);
	/**
	 * sChemin
	 */
	private static Map<String, String> sChemin = new ConcurrentHashMap<>();
	/**
	 * documentEntryUUID
	 */
	private static String[] documentEntryUUID = new String[20];
	/**
	 * UUIDID
	 */
	private static final String UUIDID = random();

	/**
	 * openXDMFile
	 * 
	 * @param stage
	 */
	public static Map<String, String> openXDMFile(final Stage stage, final File file) {
		if (file != null) {
			final String filePath = file.getAbsolutePath();
			final String sCheminTempDir = Constant.INTEROPFOLDER + "\\ZIPTEMP";
			final String sCheminDocuments = sCheminTempDir + "\\IHE_XDM\\SUBSET01";
			final String sCheminCDA = sCheminDocuments + "\\" + IniFile.read("DEFAULT_CDA_NAME", "IHE_XDM");
			final String sCheminMETA = sCheminDocuments + "\\" + IniFile.read("DEFAULT_METADATA_NAME", "IHE_XDM");
			try {
				FileUtils.deleteDirectory(new File(sCheminTempDir));
				XdmUtilities.unzip(filePath, sCheminTempDir);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
			if (new File(sCheminCDA).exists()) {
				IniFile.write("LAST-CDA-FILE", sCheminCDA, "MEMORY");
				IniFile.write("LAST-PATH-USED", sCheminDocuments, "MEMORY");
			} else {
				if (LOG.isInfoEnabled()) {
					final String error = "Impossible de trouver le fichier CDA dans " + sCheminCDA;
					LOG.error(error);
				}
			}

			if (new File(sCheminMETA).exists()) {
				IniFile.write("LAST-META-FILE", sCheminMETA, "MEMORY");

			} else {
				if (LOG.isInfoEnabled()) {
					final String error = "Impossible de trouver le fichier metadata dans " + sCheminMETA;
					LOG.error(error);
				}
			}
			sChemin.put(sCheminCDA, sCheminMETA);
		}
		return sChemin;
	}

	/**
	 * writeXml
	 * 
	 * @param doc
	 * @param output
	 * @throws TransformerException
	 */
	private static File writeXml(final Document doc, final OutputStream output, final Path path)
			throws TransformerException {
		File file = null;
		final ClassLoader classloader = Utility.getContextClassLoader();
		try (InputStream iStream = classloader.getResourceAsStream(Constant.PRETTY)) {
			final TransformerFactory transformerF = TransformerFactory.newInstance();
			final Transformer transformer = transformerF.newTransformer(new StreamSource(iStream));
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("omit-xml-declaration", "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(output);
			transformer.transform(source, result);
			file = XdmUtilities.replaceInFile(path.toFile());
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return file;
	}

	/**
	 * random
	 * 
	 * @return
	 */
	private static String random() {
		final UUID uuid = UUID.randomUUID();
		return "urn:uuid:" + uuid.toString();
	}

	/**
	 * rootElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element rootElement(final Document doc) {
		final Element rootElement = doc.createElement("ns5:SubmitObjectsRequest");
		doc.appendChild(rootElement);
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		rootElement.setAttribute("xmlns:ns5", "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0");
		return rootElement;
	}

	/**
	 * setSecondElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element secondElement(final Document doc) {
		final Element secondElement = doc.createElement("ns2:RegistryPackage");
		secondElement.setAttribute("xmlns:ns2", URN1);
		secondElement.setAttribute("id", UUIDID);
		secondElement.setAttribute("status", "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved");
		return secondElement;
	}

	/**
	 * setFiveElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element fiveElement(final Document doc) {
		final Element fiveElement = doc.createElement(VALUE);
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		final LocalDateTime now = LocalDateTime.now();
		fiveElement.setTextContent(dtf.format(now));
		return fiveElement;
	}

	/**
	 * setSevenElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element sevenElement(final Document doc, final List<String> pListCda) {
		final Element sevenElement = doc.createElement(LOCALE);
		sevenElement.setAttribute(LANG, "FR");
		sevenElement.setAttribute(CHARSET, UTF8);
		final String title = getXpathSingleValue(new File(pListCda.get(0)), "//*:ClinicalDocument/*:title/string()");
		sevenElement.setAttribute(VAL, title);
		return sevenElement;
	}

	/**
	 * setNineElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element nineElement(final Document doc) {
		final Element nineElement = doc.createElement(LOCALE);
		nineElement.setAttribute(LANG, "FR");
		nineElement.setAttribute(CHARSET, UTF8);
		nineElement.setAttribute(VAL, "");
		return nineElement;
	}

	/**
	 * setTeenElement
	 * 
	 * @param doc
	 * @param secondElement
	 * @return
	 */
	public static Element teenElement(final Document doc, final Element secondElement) {
		final Element teenElement = doc.createElement(CLASSIFICATION);
		teenElement.setAttribute("id", "SubmissionSet01_c001");
		teenElement.setAttribute("classificationNode", "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd");
		teenElement.setAttribute(CLASSOBJ, UUIDID);
		teenElement.setAttribute(NODE, "");
		return teenElement;
	}

	/**
	 * setQuatreElement
	 * 
	 * @param doc
	 * @param sHealthcareFacilityCode
	 * @return
	 */
	public static Element quatreElement(final Document doc, final String sHealthcareFC) {
		final Element quatreElement = doc.createElement(CLASSIFICATION);
		quatreElement.setAttribute("id", "SubmissionSet01_c002");
		quatreElement.setAttribute(SCHEME, "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500");
		quatreElement.setAttribute(CLASSOBJ, UUIDID);
		quatreElement.setAttribute(NODE, sHealthcareFC);
		return quatreElement;
	}

	/**
	 * seteeElement
	 * 
	 * @param doc
	 * @param sHealthcareFacilityDN
	 * @return
	 */
	public static Element eeElement(final Document doc, final String sHealthcareFDN) {
		final Element eeElement = doc.createElement(LOCALE);
		eeElement.setAttribute(LANG, "FR");
		eeElement.setAttribute(CHARSET, UTF8);
		eeElement.setAttribute(VAL, sHealthcareFDN);
		return eeElement;
	}

	/**
	 * setElem
	 * 
	 * @param doc
	 * @param pListCda
	 * @return
	 */
	public static Element elem(final Document doc, final List<String> pListCda) {
		final Element eElem = doc.createElement(EXIDENTIFIER);
		eElem.setAttribute("id", random());
		eElem.setAttribute(IDSCHEME, "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446");
		eElem.setAttribute(REGISTRY, "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
		eElem.setAttribute(VAL, getPatientId(new File(pListCda.get(0))));
		return eElem;
	}

	/**
	 * setElem1
	 * 
	 * @param doc
	 * @param sSourceId
	 * @return
	 */
	public static Element elem1(final Document doc, final String sSourceId) {
		final Element eElem1 = doc.createElement(EXIDENTIFIER);
		eElem1.setAttribute("id", random());
		eElem1.setAttribute(IDSCHEME, "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832");
		eElem1.setAttribute(REGISTRY, "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
		eElem1.setAttribute(VAL, sSourceId);
		return eElem1;
	}

	/**
	 * boomFile
	 * 
	 * @param pListCda
	 */
	private static void boomFile(final List<String> pListCda) {
		for (final String cda : pListCda) {
			try {
				BomService.saveAsUTF8WithoutBOM(cda, StandardCharsets.UTF_8);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
	}

	/**
	 * generateMeta XDM
	 * 
	 * @param pListCda
	 */
	public static String generateMeta(final List<String> pListCda, final String urlNos, final String urlCode,
			final String urlCode1, final String urlA11, final String urlX04, final String urlA04) {
		boomFile(pListCda);
		String file = null;
		final Path path = Paths.get(Constant.INTEROPFOLDER + "\\nouveauDoc.xml");
		if (!path.toFile().exists()) {
			try {
				path.toFile().createNewFile();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		try (FileOutputStream output = new FileOutputStream(path.toFile())) {
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();
			// ns5:SubmitObjectsRequest
			final Element rootElement = rootElement(doc);
			// RegistryObjectList
			final Element firstElement = doc.createElement("RegistryObjectList");
			rootElement.appendChild(firstElement);
			firstElement.setAttribute("xmlns", URN1);
			// ns2:RegistryPackage
			final Element secondElement = secondElement(doc);
			firstElement.appendChild(secondElement);
			final Comment comment = doc.createComment(COMM1);
			final Comment comment1 = doc.createComment("RegistryPackage");
			final Comment comment2 = doc.createComment(COMM1);
			secondElement.getParentNode().insertBefore(comment, secondElement);
			secondElement.getParentNode().insertBefore(comment1, secondElement);
			secondElement.getParentNode().insertBefore(comment2, secondElement);
			// ns2:Slot
			final Element thirdElement = doc.createElement(SLOT);
			thirdElement.setAttribute(NAME, "submissionTime");
			secondElement.appendChild(thirdElement);
			final Comment commentSt = doc.createComment(COMM2);
			final Comment commentSt1 = doc.createComment("SubmissionTime");
			final Comment commentSt2 = doc.createComment(COMM2);
			thirdElement.getParentNode().insertBefore(commentSt, thirdElement);
			thirdElement.getParentNode().insertBefore(commentSt1, thirdElement);
			thirdElement.getParentNode().insertBefore(commentSt2, thirdElement);
			// ns2:ValueList
			final Element fourElement = doc.createElement(VLIST);
			thirdElement.appendChild(fourElement);
			// ns2:Value
			final Element fiveElement = fiveElement(doc);
			fourElement.appendChild(fiveElement);
			// ns2:Name
			final Element sixElement = doc.createElement(NAME1);
			secondElement.appendChild(sixElement);
			final Comment commentN = doc.createComment(COMM2);
			final Comment commentN1 = doc.createComment("Name");
			final Comment commentN2 = doc.createComment(COMM2);
			sixElement.getParentNode().insertBefore(commentN, sixElement);
			sixElement.getParentNode().insertBefore(commentN1, sixElement);
			sixElement.getParentNode().insertBefore(commentN2, sixElement);
			// ns2:LocalizedString
			final Element sevenElement = sevenElement(doc, pListCda);
			sixElement.appendChild(sevenElement);
			// ns2:Description
			final Element eightElement = doc.createElement("ns2:Description");
			secondElement.appendChild(eightElement);
			final Comment commentD = doc.createComment(COMM2);
			final Comment commentD1 = doc.createComment("Description (comments)");
			final Comment commentD2 = doc.createComment(COMM2);
			eightElement.getParentNode().insertBefore(commentD, eightElement);
			eightElement.getParentNode().insertBefore(commentD1, eightElement);
			eightElement.getParentNode().insertBefore(commentD2, eightElement);
			// ns2:LocalizedString
			final Element nineElement = nineElement(doc);
			eightElement.appendChild(nineElement);
			// ns2:Classification
			final Element teenElement = teenElement(doc, secondElement);
			secondElement.appendChild(teenElement);
			final Comment commentC = doc.createComment(COMM2);
			final Comment commentC1 = doc.createComment("Classification");
			final Comment commentC2 = doc.createComment(COMM2);
			teenElement.getParentNode().insertBefore(commentC, teenElement);
			teenElement.getParentNode().insertBefore(commentC1, teenElement);
			teenElement.getParentNode().insertBefore(commentC2, teenElement);
			// ns2:Classification --> Author attributes
			final String submissionSet = random();
			final long nbAuthors = Long.parseUnsignedLong(
					getXpathSingleValue(new File(pListCda.get(0)), "count(//*:ClinicalDocument/*:author)"), 16);
			for (int i = 0; i < (int) nbAuthors; i++) {
				final Element elevenElement = doc.createElement(CLASSIFICATION);
				secondElement.appendChild(elevenElement);
				elevenElement.setAttribute("id", submissionSet);
				elevenElement.setAttribute(SCHEME, "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d");
				elevenElement.setAttribute(CLASSOBJ, UUIDID);
				elevenElement.setAttribute(NODE, "");
				final Comment commentA = doc.createComment(COMM2);
				final Comment commentA1 = doc.createComment("Author attributes");
				final Comment commentA2 = doc.createComment(COMM2);
				elevenElement.getParentNode().insertBefore(commentA, elevenElement);
				elevenElement.getParentNode().insertBefore(commentA1, elevenElement);
				elevenElement.getParentNode().insertBefore(commentA2, elevenElement);

				final String sAuthorInst = getAuthorInstitution(i + 1, new File(pListCda.get(0)));
				if (!sAuthorInst.isEmpty()) {
					// AuthorInstitution
					final Element twelveElement = doc.createElement(SLOT);
					elevenElement.appendChild(twelveElement);
					twelveElement.setAttribute(NAME, "authorInstitution");
					final Element thirteenElement = doc.createElement(VLIST);
					twelveElement.appendChild(thirteenElement);

					final Comment commentI = doc.createComment(COMM2);
					final Comment commentI1 = doc.createComment("AuthorInstitution");
					final Comment commentI2 = doc.createComment(COMM2);
					twelveElement.getParentNode().insertBefore(commentI, twelveElement);
					twelveElement.getParentNode().insertBefore(commentI1, twelveElement);
					twelveElement.getParentNode().insertBefore(commentI2, twelveElement);

					final Element fourteenElement = doc.createElement(VALUE);
					thirteenElement.appendChild(fourteenElement);
					fourteenElement.setTextContent(sAuthorInst);
				}
				final String sAuthorPerson = getAuthorPerson(i + 1, new File(pListCda.get(0)));
				if (!sAuthorPerson.isEmpty()) {
					// sAuthorPerson
					final Element fifteenElement = doc.createElement(SLOT);
					elevenElement.appendChild(fifteenElement);
					fifteenElement.setAttribute(NAME, "authorPerson");
					final Element seventeenElement = doc.createElement(VLIST);
					fifteenElement.appendChild(seventeenElement);
					final Comment commentP = doc.createComment(COMM2);
					final Comment commentP1 = doc.createComment("AuthorPerson");
					final Comment commentP2 = doc.createComment(COMM2);
					fifteenElement.getParentNode().insertBefore(commentP, fifteenElement);
					fifteenElement.getParentNode().insertBefore(commentP1, fifteenElement);
					fifteenElement.getParentNode().insertBefore(commentP2, fifteenElement);
					final Element heigtteenElement = doc.createElement(VALUE);
					seventeenElement.appendChild(heigtteenElement);
					heigtteenElement.setTextContent(sAuthorPerson);
				}
				final String sAuthorRole = getXpathSingleValue(new File(pListCda.get(0)),
						AUTHOR + (i + 1) + "]/*:functionCode/@displayName/string()");
				if (!sAuthorRole.startsWith(EXCLAMATION) && !sAuthorRole.isEmpty()) {
					// sAuthorRole
					final Element unElement = doc.createElement(SLOT);
					elevenElement.appendChild(unElement);
					unElement.setAttribute(NAME, "authorRole");
					final Comment commentPer = doc.createComment(COMM2);
					final Comment commentPer1 = doc.createComment("authorRole");
					final Comment commentPer2 = doc.createComment(COMM2);
					unElement.getParentNode().insertBefore(commentPer, unElement);
					unElement.getParentNode().insertBefore(commentPer1, unElement);
					unElement.getParentNode().insertBefore(commentPer2, unElement);
					final Element deuxElement = doc.createElement(VLIST);
					unElement.appendChild(deuxElement);
					final Element troisElement = doc.createElement(VALUE);
					deuxElement.appendChild(troisElement);
					troisElement.setTextContent(sAuthorRole);
				}
				final String xPathString = CLINAUTHOR + (i + 1) + "]/*:assignedAuthor/*:code/@code";
				final String sAssignedAutEx = getXpathSingleValue(new File(pListCda.get(0)), xPathString);
				if (!sAssignedAutEx.startsWith(EXCLAMATION) && !sAssignedAutEx.isEmpty()) {
					// authorSpecialty
					final Element quatreElement = doc.createElement(SLOT);
					elevenElement.appendChild(quatreElement);
					quatreElement.setAttribute(NAME, "authorSpecialty");
					final Comment commentS = doc.createComment(COMM2);
					final Comment commentS1 = doc.createComment("authorSpecialty");
					final Comment commentS2 = doc.createComment(COMM2);
					quatreElement.getParentNode().insertBefore(commentS, quatreElement);
					quatreElement.getParentNode().insertBefore(commentS1, quatreElement);
					quatreElement.getParentNode().insertBefore(commentS2, quatreElement);
					final Element cinqElement = doc.createElement(VLIST);
					quatreElement.appendChild(cinqElement);
					final Element sixeElement = doc.createElement(VALUE);
					cinqElement.appendChild(sixeElement);
					final String sAuthorSpecialty = getAuthorSpecialty(i + 1, new File(pListCda.get(0)));
					sixeElement.setTextContent(sAuthorSpecialty);
				}
			}
			// HealthcareFacility
			final String sHealthcareFC = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@code/string()");
			final String sHealthcareFCS = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@codeSystem/string()");
			final String sHealthcareFDN = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@displayName/string()");
			// authorSpecialty
			final Element quatreElement = quatreElement(doc, sHealthcareFC);
			secondElement.appendChild(quatreElement);

			final Comment commentH = doc.createComment(COMM2);
			final Comment commentH1 = doc.createComment("HealthcareFacility");
			final Comment commentH2 = doc.createComment(COMM2);
			quatreElement.getParentNode().insertBefore(commentH, quatreElement);
			quatreElement.getParentNode().insertBefore(commentH1, quatreElement);
			quatreElement.getParentNode().insertBefore(commentH2, quatreElement);

			final Element septElement = doc.createElement(SLOT);
			quatreElement.appendChild(septElement);
			septElement.setAttribute(NAME, CODESCH);
			final Element cinqElement = doc.createElement(VLIST);
			septElement.appendChild(cinqElement);
			final Element sixeElement = doc.createElement(VALUE);
			cinqElement.appendChild(sixeElement);
			sixeElement.setTextContent(sHealthcareFCS);
			final Element eElement = doc.createElement(NAME1);
			quatreElement.appendChild(eElement);
			final Element eeElement = eeElement(doc, sHealthcareFDN);
			eElement.appendChild(eeElement);
			final Element eElem = elem(doc, pListCda);
			secondElement.appendChild(eElem);
			final Comment commentP = doc.createComment(COMM2);
			final Comment commentP1 = doc.createComment("PatientId");
			final Comment commentP2 = doc.createComment(COMM2);
			eElem.getParentNode().insertBefore(commentP, eElem);
			eElem.getParentNode().insertBefore(commentP1, eElem);
			eElem.getParentNode().insertBefore(commentP2, eElem);
			final Element eeElem = doc.createElement(NAME1);
			eElem.appendChild(eeElem);
			final Element eeeElem = doc.createElement(LOCALE);
			eeElem.appendChild(eeeElem);
			eeeElem.setAttribute(VAL, "XDSSubmissionSet.patientId");
			String sSourceId = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:custodian/*:assignedCustodian/*:representedCustodianOrganization/*:id/@root/string()");
			sSourceId += "." + getXpathSingleValue(new File(pListCda.get(0)),
					"//*:custodian/*:assignedCustodian/*:representedCustodianOrganization/*:id/@extension/string()");
			final Element eElem1 = elem1(doc, sSourceId);
			secondElement.appendChild(eElem1);

			final Comment commentS = doc.createComment(COMM2);
			final Comment commentS1 = doc.createComment("SourceId");
			final Comment commentS2 = doc.createComment(COMM2);
			eElem1.getParentNode().insertBefore(commentS, eElem1);
			eElem1.getParentNode().insertBefore(commentS1, eElem1);
			eElem1.getParentNode().insertBefore(commentS2, eElem1);

			final Element eeElem1 = doc.createElement(NAME1);
			eElem1.appendChild(eeElem1);
			final Element eeeElem1 = doc.createElement(LOCALE);
			eeElem1.appendChild(eeeElem1);
			eeeElem1.setAttribute(VAL, "XDSSubmissionSet.sourceId");
			// UniqueId
			final String sRacineOID = "1.2.250.1.213.1.1.1.1";
			final Date date = new Date();
			final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			final LocalDateTime localDateTime = LocalDateTime.now();
			final ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
			final int month = localDate.getMonthValue();
			final String sUniqueId = sRacineOID + "." + localDate.getYear() + "." + month + "." + month + "."
					+ zdt.toInstant().toEpochMilli();
			final Element eElem2 = doc.createElement(EXIDENTIFIER);
			secondElement.appendChild(eElem2);
			eElem2.setAttribute("id", random());
			eElem2.setAttribute(IDSCHEME, "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8");
			eElem2.setAttribute(REGISTRY, "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
			eElem2.setAttribute(VAL, sUniqueId);
			final Element eeElem2 = doc.createElement(NAME1);
			eElem2.appendChild(eeElem2);
			final Comment commentU = doc.createComment(COMM2);
			final Comment commentU1 = doc.createComment("UniqueId");
			final Comment commentU2 = doc.createComment(COMM2);
			eElem2.getParentNode().insertBefore(commentU, eElem2);
			eElem2.getParentNode().insertBefore(commentU1, eElem2);
			eElem2.getParentNode().insertBefore(commentU2, eElem2);
			final Element eeeElem2 = doc.createElement(LOCALE);
			eeElem2.appendChild(eeeElem2);
			eeeElem2.setAttribute(VAL, "XDSSubmissionSet.uniqueId");
			for (int k = 0; k < pListCda.size(); k++) {
				file = buildExtrinsicObject(k, firstElement, doc, new File(pListCda.get(k)), urlNos, urlCode, urlCode1,
						urlX04, urlA11, urlA04);
			}
			if (file.isEmpty()) {
				for (int k = 0; k < pListCda.size(); k++) {
					buildAssociations(firstElement, doc);
				}
				file = writeXml(doc, output, path).getAbsolutePath();
			}
		} catch (final IOException | TransformerException | ParserConfigurationException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return file;
	}

	/**
	 * buildAssociations
	 * 
	 * @param firstElement
	 * @param doc
	 */
	private static void buildAssociations(final Element firstElement, final Document doc) {
		final String associationId = random();
		final Element eeEle15 = doc.createElement("ns2:Association");
		firstElement.appendChild(eeEle15);
		eeEle15.setAttribute("id", associationId);
		eeEle15.setAttribute("objectType", "Original");
		eeEle15.setAttribute("associationType", "urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember");
		eeEle15.setAttribute("sourceObject", UUIDID);
		eeEle15.setAttribute("targetObject", random());
		eeEle15.setAttribute("xmlns:ns2", URN1);

		final Comment commentU = doc.createComment(COMM2);
		final Comment commentU1 = doc.createComment("ASSOCIATIONS");
		final Comment commentU2 = doc.createComment(COMM2);
		eeEle15.getParentNode().insertBefore(commentU, eeEle15);
		eeEle15.getParentNode().insertBefore(commentU1, eeEle15);
		eeEle15.getParentNode().insertBefore(commentU2, eeEle15);

		addExtrinsicObjectSlot("SubmissionSetStatus", "Original", doc, eeEle15);
	}

	/**
	 * buildExtrinsicObject
	 * 
	 * @param pDocumentNumber
	 * @param firstElement
	 * @param doc
	 * @param file
	 */
	private static String buildExtrinsicObject(final int pDocumentNumber, final Element firstElement,
			final Document doc, final File file, final String urlNos, final String urlCode, final String urlCode1,
			final String urlX04, final String urlA11, final String urlA04) {
		String errorV = "";
		documentEntryUUID[pDocumentNumber] = random();
		final Element eElem2 = doc.createElement("ns2:ExtrinsicObject");
		firstElement.appendChild(eElem2);
		eElem2.setAttribute("mimeType", "text/xml");
		eElem2.setAttribute("objectType", "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1");
		eElem2.setAttribute("id", documentEntryUUID[pDocumentNumber]);
		eElem2.setAttribute("xmlns:ns2", URN1);
		eElem2.setAttribute("status", "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved");
		final String sVersionNumber = getXpathSingleValue(file, "//*:ClinicalDocument/*:versionNumber/@value/string()");
		if (!sVersionNumber.isEmpty()) {
			final int iVersionNumber = (int) Long.parseUnsignedLong(sVersionNumber, 16);
			if (iVersionNumber > 1) {
				final Comment commentE = doc.createComment(COMM1);
				final Comment commentE1 = doc.createComment("ExtraMetadata remplacement de document");
				final Comment commentE2 = doc.createComment(COMM1);
				final Element eeElem2 = doc.createElement(SLOT);
				eeElem2.setAttribute(NAME, "urn:action:extraMetadataSlot");
				eElem2.appendChild(eeElem2);
				final Element eeeElem2 = doc.createElement(VLIST);
				eeElem2.appendChild(eeeElem2);
				final Element eeeElem3 = doc.createElement(VALUE);
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent("C");
				eeElem2.getParentNode().insertBefore(commentE, eeElem2);
				eeElem2.getParentNode().insertBefore(commentE1, eeElem2);
				eeElem2.getParentNode().insertBefore(commentE2, eeElem2);
			}
		}
		final Comment commentE = doc.createComment(COMM1);
		final Comment commentE1 = doc.createComment("Extrinsic object - CDA " + file.getName());
		final Comment commentE2 = doc.createComment(COMM1);
		eElem2.getParentNode().insertBefore(commentE, eElem2);
		eElem2.getParentNode().insertBefore(commentE1, eElem2);
		eElem2.getParentNode().insertBefore(commentE2, eElem2);

		final String sHash = XdmUtilities.getHash(file.getAbsolutePath());
		final Element elem = addExtrinsicObjectSlot("hash", sHash, doc, eElem2);
		final Comment commentU = doc.createComment(COMM2);
		final Comment commentU1 = doc.createComment("Hash");
		final Comment commentU2 = doc.createComment(COMM2);
		elem.getParentNode().insertBefore(commentU, elem);
		elem.getParentNode().insertBefore(commentU1, elem);
		elem.getParentNode().insertBefore(commentU2, elem);

		final long lSize = file.length();
		final String sSize = String.valueOf(lSize);
		final Element elemS = addExtrinsicObjectSlot("size", sSize, doc, eElem2);
		final Comment commentS = doc.createComment(COMM2);
		final Comment commentS1 = doc.createComment("Size");
		final Comment commentS2 = doc.createComment(COMM2);
		elemS.getParentNode().insertBefore(commentS, elemS);
		elemS.getParentNode().insertBefore(commentS1, elemS);
		elemS.getParentNode().insertBefore(commentS2, elemS);

		final String sDocName = "DOC" + "000" + (pDocumentNumber + 1) + ".XML";
		final Element elemU = addExtrinsicObjectSlot("URI", sDocName, doc, eElem2);
		final Comment commentUr = doc.createComment(COMM2);
		final Comment commentUr1 = doc.createComment("URI");
		final Comment commentUr2 = doc.createComment(COMM2);
		elemU.getParentNode().insertBefore(commentUr, elemU);
		elemU.getParentNode().insertBefore(commentUr1, elemU);
		elemU.getParentNode().insertBefore(commentUr2, elemU);

		final String sDHCreationLocale = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:effectiveTime/@value/string()");
		final String[] words = sDHCreationLocale.split("[+]");
		String year = "";
		String month = "";
		String day = "";
		String heure = "";
		String min = "";
		String sec = "";
		String heureUtc = "";
		String minUtc = "";
		if (words.length > 0 && words[0] != null && words[0].length() >= 4) {
			year = words[0].substring(0, 4);
		}
		if (words.length > 0 && words[0] != null && words[0].length() >= 6) {
			month = words[0].substring(4, 6);
		}
		if (words.length > 0 && words[0] != null && words[0].length() >= 8) {
			day = words[0].substring(6, 8);
		}
		if (words.length > 0 && words[0] != null && words[0].length() >= 10) {
			heure = words[0].substring(8, 10);
		}
		if (words.length > 0 && words[0] != null && words[0].length() >= 12) {
			min = words[0].substring(10, 12);
		}
		if (words.length > 0 && words[0] != null && words[0].length() >= 14) {
			sec = words[0].substring(12, 14);
		}
		if (words.length > 0 && words[1] != null && words[1].length() >= 2) {
			heureUtc = words[1].substring(0, 2);
		}
		if (words.length > 0 && words[1] != null && words[1].length() >= 4) {
			minUtc = words[1].substring(2, 4);
		}
		final Integer heureI = Integer.parseInt(heure) - Integer.parseInt(heureUtc);
		String heureIStr = heureI.toString();
		final int length = String.valueOf(heureI).length();
		if (length == 1) {
			heureIStr = "0" + heureI;
		}
		final Integer minI = Integer.parseInt(min) - Integer.parseInt(minUtc);
		String minIStr = minI.toString();
		final int lengthMin = String.valueOf(minI).length();
		if (lengthMin == 1) {
			minIStr = "0" + minI;
		}
		final String sDateHeureCUTC = year + month + day + heureIStr + minIStr + sec;
		final Element elemCt = addExtrinsicObjectSlot("creationTime", sDateHeureCUTC, doc, eElem2);
		final Comment commentC = doc.createComment(COMM2);
		final Comment commentC1 = doc.createComment("CreationTime");
		final Comment commentC2 = doc.createComment(COMM2);
		elemCt.getParentNode().insertBefore(commentC, elemCt);
		elemCt.getParentNode().insertBefore(commentC1, elemCt);
		elemCt.getParentNode().insertBefore(commentC2, elemCt);

		final Element language = addExtrinsicObjectSlot("languageCode",
				getXpathSingleValue(file, "//*:ClinicalDocument/*:languageCode/@code/string()"), doc, eElem2);
		final Comment commentL = doc.createComment(COMM2);
		final Comment commentL1 = doc.createComment("LangageCode");
		final Comment commentL2 = doc.createComment(COMM2);
		language.getParentNode().insertBefore(commentL, language);
		language.getParentNode().insertBefore(commentL1, language);
		language.getParentNode().insertBefore(commentL2, language);

		final Element legal = addExtrinsicObjectSlot("legalAuthenticator", getLegalAuthenticator(file), doc, eElem2);
		final Comment commentLe = doc.createComment(COMM2);
		final Comment commentLe1 = doc.createComment("LegalAuthenticator");
		final Comment commentLe2 = doc.createComment(COMM2);
		legal.getParentNode().insertBefore(commentLe, legal);
		legal.getParentNode().insertBefore(commentLe1, legal);
		legal.getParentNode().insertBefore(commentLe2, legal);

		final String sEffectiveTimeLow = getXpathSingleValue(file,
				"//*:documentationOf/*:serviceEvent/*:effectiveTime/*:low/@value/string()");
		if (sEffectiveTimeLow != null && !sEffectiveTimeLow.isEmpty()) {
			final String[] wordss = sEffectiveTimeLow.split("[+]");
			String years = "";
			String months = "";
			String days = "";
			String heures = "";
			String mins = "";
			String secs = "";
			String heureUtcs = "";
			String minUtcs = "";
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 4) {
				years = wordss[0].substring(0, 4);
			}
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 6) {
				months = wordss[0].substring(4, 6);
			}
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 8) {
				days = wordss[0].substring(6, 8);
			}
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 10) {
				heures = wordss[0].substring(8, 10);
			}
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 12) {
				mins = wordss[0].substring(10, 12);
			}
			if (wordss.length > 0 && wordss[0] != null && wordss[0].length() >= 14) {
				secs = wordss[0].substring(12, 14);
			}
			if (wordss.length > 0 && wordss[1] != null && wordss[1].length() >= 2) {
				heureUtcs = wordss[1].substring(0, 2);
			}
			if (wordss.length > 0 && wordss[1] != null && wordss[1].length() >= 4) {
				minUtcs = wordss[1].substring(2, 4);
			}
			final Integer hheure = Integer.parseInt(heures) - Integer.parseInt(heureUtcs);
			final Integer mmin = Integer.parseInt(mins) - Integer.parseInt(minUtcs);
			String hhStr = String.valueOf(hheure);
			String mmStr = String.valueOf(mmin);
			final int lengthH = String.valueOf(hheure).length();
			if (lengthH == 1) {
				hhStr = "0" + hhStr;
			}
			final int lengthM = String.valueOf(mmin).length();
			if (lengthM == 1) {
				mmStr = "0" + mmStr;
			}
			final String sHeureTransformee = years + months + days + hhStr + mmStr + secs;
			final Element serviceSt = addExtrinsicObjectSlot("serviceStartTime", sHeureTransformee, doc, eElem2);
			final Comment commentSt = doc.createComment(COMM2);
			final Comment commentSt1 = doc.createComment("ServiceStartTime");
			final Comment commentSt2 = doc.createComment(COMM2);
			serviceSt.getParentNode().insertBefore(commentSt, serviceSt);
			serviceSt.getParentNode().insertBefore(commentSt1, serviceSt);
			serviceSt.getParentNode().insertBefore(commentSt2, serviceSt);
		}

		final String sEffectiveTH = getXpathSingleValue(file,
				"//*:documentationOf/*:serviceEvent/*:effectiveTime/*:high/@value/string()");
		if (sEffectiveTH != null && !sEffectiveTH.isEmpty()) {
			final String[] wordsss = sEffectiveTH.split("[+]");
			String yearss = "";
			String monthss = "";
			String dayss = "";
			String heuress = "";
			String minss = "";
			String secss = "";
			String heureUtcss = "";
			String minUtcss = "";
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 4) {
				yearss = wordsss[0].substring(0, 4);
			}
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 6) {
				monthss = wordsss[0].substring(4, 6);
			}
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 8) {
				dayss = wordsss[0].substring(6, 8);
			}
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 10) {
				heuress = wordsss[0].substring(8, 10);
			}
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 12) {
				minss = wordsss[0].substring(10, 12);
			}
			if (wordsss.length > 0 && wordsss[0] != null && wordsss[0].length() >= 14) {
				secss = wordsss[0].substring(12, 14);
			}
			if (wordsss.length > 0 && wordsss[1] != null && wordsss[1].length() >= 2) {
				heureUtcss = wordsss[1].substring(0, 2);
			}
			if (wordsss.length > 0 && wordsss[1] != null && wordsss[1].length() >= 4) {
				minUtcss = wordsss[1].substring(2, 4);
			}
			final Integer hheure1 = Integer.parseInt(heuress) - Integer.parseInt(heureUtcss);
			final Integer mmin1 = Integer.parseInt(minss) - Integer.parseInt(minUtcss);
			String hhStr1 = String.valueOf(hheure1);
			String mmStr1 = String.valueOf(mmin1);
			final int lengthH1 = String.valueOf(hheure1).length();
			if (lengthH1 == 1) {
				hhStr1 = "0" + hhStr1;
			}
			final int lengthM1 = String.valueOf(mmin1).length();
			if (lengthM1 == 1) {
				mmStr1 = "0" + mmStr1;
			}
			final String sHeureTrans = yearss + monthss + dayss + hhStr1 + mmStr1 + secss;
			final Element serviceSt = addExtrinsicObjectSlot("serviceStopTime", sHeureTrans, doc, eElem2);
			final Comment commentSt = doc.createComment(COMM2);
			final Comment commentSt1 = doc.createComment("ServiceStopTime");
			final Comment commentSt2 = doc.createComment(COMM2);
			serviceSt.getParentNode().insertBefore(commentSt, serviceSt);
			serviceSt.getParentNode().insertBefore(commentSt1, serviceSt);
			serviceSt.getParentNode().insertBefore(commentSt2, serviceSt);
		}

		final String sSourcePIDXReq = "if ( count( //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')   and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')] ) > 0 ) then string-join(( //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9') and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')][1]/@extension /string() , '^^^&amp;' , //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9') and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')][1]/@root /string() , '&amp;ISO^PI'), '') else if (count( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.8']) >0 ) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.8']/@extension /string(), '^^^&amp;','1.2.250.1.213.1.4.8' , '&amp;ISO^NH'),'') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.9'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.9']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.9' , '&amp;ISO^NH'), '') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.10'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.10']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.10' , '&amp;ISO^NH'), '') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.11'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.11']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.11' , '&amp;ISO^NH'), '') else 'ERREUR'";
		final String sSourcePatientID = getXpathSingleValue(file, sSourcePIDXReq);
		final Element sourceP = addExtrinsicObjectSlot("sourcePatientId", sSourcePatientID, doc, eElem2);
		final Comment commentSt = doc.createComment(COMM2);
		final Comment commentSt1 = doc.createComment("SourcePatientId");
		final Comment commentSt2 = doc.createComment(COMM2);
		sourceP.getParentNode().insertBefore(commentSt, sourceP);
		sourceP.getParentNode().insertBefore(commentSt1, sourceP);
		sourceP.getParentNode().insertBefore(commentSt2, sourceP);
		final String sNomBR = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:family[@qualifier='BR']/string()");
		final String sNomCL = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:family[@qualifier='CL']/string()");
		String sPrenomCL = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[@qualifier='CL']/string()");
		String sPrenomBR = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[@qualifier='BR']/string()");
		final String sPrenom = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[1]/string()");
		final String sListePN = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[count(@*)=0]/string()");

		if (sPrenomBR.startsWith(EXCLAMATION) || sPrenomBR.isEmpty()) {
			sPrenomBR = sPrenom;
		}
		if (sPrenomCL.startsWith(EXCLAMATION) || sPrenomCL.isEmpty()) {
			sPrenomCL = sPrenom;
		}
		// SourcePatientInfo
		final Element eeElem2 = doc.createElement(SLOT);
		eeElem2.setAttribute(NAME, "sourcePatientInfo");
		eElem2.appendChild(eeElem2);
		final Element eeeElem2 = doc.createElement(VLIST);
		eeElem2.appendChild(eeeElem2);
		final Comment commentSp = doc.createComment(COMM2);
		final Comment commentSp1 = doc.createComment("SourcePatientInfo");
		final Comment commentSp2 = doc.createComment(COMM2);
		eeElem2.getParentNode().insertBefore(commentSp, eeElem2);
		eeElem2.getParentNode().insertBefore(commentSp1, eeElem2);
		eeElem2.getParentNode().insertBefore(commentSp2, eeElem2);

		if (!sNomBR.startsWith(EXCLAMATION) && !sNomBR.isEmpty()) {
			if (sListePN.isEmpty()) {
				final Element eeeElem3 = doc.createElement(VALUE);
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent(PID + sNomBR + "^" + sPrenomBR + "^^^^^" + "L");
			} else {
				final Element eeeElem3 = doc.createElement(VALUE);
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent(PID + sNomBR + "^" + sPrenomBR + "^" + sListePN + "^^^^" + "L");
			}
		}
		if (!sNomCL.startsWith(EXCLAMATION) && !sNomCL.isEmpty()) {
			if (sPrenomCL.startsWith(EXCLAMATION) && sPrenomCL.isEmpty()) {
				final Element eeeElem3 = doc.createElement(VALUE);
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent(PID + sNomCL + "^" + sPrenomBR + "^^^^^" + "D");
			} else {
				final Element eeeElem3 = doc.createElement(VALUE);
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent(PID + sNomCL + "^" + sPrenomCL + "^^^^^" + "D");
			}
		}
		final String sIPP2 = getXpathSingleValue(file,
				"//*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11')][2]/@extension/string()");
		final String sIPP2Root = getXpathSingleValue(file,
				"//*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11')][2]/@root/string()");
		if (!sIPP2.startsWith(EXCLAMATION) && !sIPP2.isEmpty()) {
			final Element eeeElem3 = doc.createElement(VALUE);
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-3|" + sIPP2 + "^^^&amp;" + sIPP2Root + "&amp;ISO^PI");
		}
		final String sDateNais = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:birthTime/@value/string()");
		if (!sDateNais.startsWith(EXCLAMATION) && !sDateNais.isEmpty()) {
			final Element eeeElem3 = doc.createElement(VALUE);
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-7|" + sDateNais);
		}
		final String sGenre = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:administrativeGenderCode/@code/string()");
		if (!sGenre.startsWith(EXCLAMATION) && !sGenre.isEmpty()) {
			final Element eeeElem3 = doc.createElement(VALUE);
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-8|" + sGenre);
		}
		final String pId = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:birthplace/*:place/*:addr/*:county/string()");
		if (!pId.startsWith(EXCLAMATION) && !pId.isEmpty()) {
			final Element eeeElem3 = doc.createElement(VALUE);
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-11|" + "^^^^^^BDL" + "^^" + pId);
		}
		// Name
		final String sName = getXpathSingleValue(file, "//*:ClinicalDocument/*:title/string()");
		final Element eeElem4 = doc.createElement(NAME1);
		eElem2.appendChild(eeElem4);
		final Element eeElem5 = doc.createElement(LOCALE);
		eeElem4.appendChild(eeElem5);

		final Comment commentN = doc.createComment(COMM2);
		final Comment commentN1 = doc.createComment("Name");
		final Comment commentN2 = doc.createComment(COMM2);
		eeElem4.getParentNode().insertBefore(commentN, eeElem4);
		eeElem4.getParentNode().insertBefore(commentN1, eeElem4);
		eeElem4.getParentNode().insertBefore(commentN2, eeElem4);

		eeElem5.setAttribute(LANG, "FR");
		eeElem5.setAttribute(CHARSET, UTF8);
		eeElem5.setAttribute(VAL, sName);
		// Description
		final String sDesc = "";
		final Element eeElem6 = doc.createElement("ns2:Description");
		eElem2.appendChild(eeElem6);
		final Element eeElem7 = doc.createElement(LOCALE);
		eeElem6.appendChild(eeElem7);

		final Comment commentD = doc.createComment(COMM2);
		final Comment commentD1 = doc.createComment("Description (Comments)");
		final Comment commentD2 = doc.createComment(COMM2);
		eeElem6.getParentNode().insertBefore(commentD, eeElem6);
		eeElem6.getParentNode().insertBefore(commentD1, eeElem6);
		eeElem6.getParentNode().insertBefore(commentD2, eeElem6);

		eeElem7.setAttribute(LANG, "FR");
		eeElem7.setAttribute(CHARSET, UTF8);
		eeElem7.setAttribute(VAL, sDesc);
		// EventCodeList
		final String sNbrDocOf = getXpathSingleValue(file, "count(//*:ClinicalDocument/*:documentationOf)");
		final long sNbrDocumentation = Long.parseUnsignedLong(sNbrDocOf, 16);
		for (int i = 1; i <= sNbrDocumentation; i++) {
			final String sEventCodeNRep = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@code/string()");
			final String sEventCodeDN = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@displayName/string()");
			final String sEventCodeCS = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@codeSystem/string()");
			if (!sEventCodeNRep.startsWith(EXCLAMATION) && !sEventCodeNRep.isEmpty()) {
				final Element eeElem8 = doc.createElement(CLASSIFICATION);
				eElem2.appendChild(eeElem8);
				eeElem8.setAttribute(SCHEME, "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4");
				eeElem8.setAttribute(CLASSOBJ, documentEntryUUID[pDocumentNumber]);
				eeElem8.setAttribute("id", random());
				eeElem8.setAttribute(NODE, sEventCodeNRep);
				eeElem8.setAttribute("objectType",
						"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
				addExtrinsicObjectSlot(CODESCH, sEventCodeCS, doc, eeElem8);
				final Element eeElem9 = doc.createElement(NAME1);
				eeElem8.appendChild(eeElem9);

				final Comment commentEv = doc.createComment(COMM2);
				final Comment commentEv1 = doc.createComment("EventCodeList");
				final Comment commentEv2 = doc.createComment(COMM2);
				eeElem8.getParentNode().insertBefore(commentEv, eeElem8);
				eeElem8.getParentNode().insertBefore(commentEv1, eeElem8);
				eeElem8.getParentNode().insertBefore(commentEv2, eeElem8);

				final Element eeElem10 = doc.createElement(LOCALE);
				eeElem9.appendChild(eeElem10);
				eeElem10.setAttribute(LANG, "FR");
				eeElem10.setAttribute(CHARSET, UTF8);
				eeElem10.setAttribute(VAL, sEventCodeDN);
			}
		}
		final long nbAuthors = Long
				.parseUnsignedLong(getXpathSingleValue(file, " count(//*:ClinicalDocument/*:author)"), 16);
		for (int iAuthor = 1; iAuthor <= nbAuthors; iAuthor++) {
			final Element eeElem11 = doc.createElement(CLASSIFICATION);
			eElem2.appendChild(eeElem11);
			eeElem11.setAttribute("id", random());
			eeElem11.setAttribute(SCHEME, "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d");
			eeElem11.setAttribute(CLASSOBJ, UUIDID);
			eeElem11.setAttribute(NODE, "");

			final Comment commentEv = doc.createComment(COMM2);
			final Comment commentEv1 = doc.createComment("Authors Attributes");
			final Comment commentEv2 = doc.createComment(COMM2);
			eeElem11.getParentNode().insertBefore(commentEv, eeElem11);
			eeElem11.getParentNode().insertBefore(commentEv1, eeElem11);
			eeElem11.getParentNode().insertBefore(commentEv2, eeElem11);

			final String sAuthorInst = getAuthorInstitution(iAuthor, file);
			if (!sAuthorInst.isEmpty()) {
				final Element eeElem12 = doc.createElement(SLOT);
				eeElem11.appendChild(eeElem12);
				eeElem12.setAttribute(NAME, "authorInstitution");
				final Element eeElem13 = doc.createElement(VLIST);
				eeElem12.appendChild(eeElem13);

				final Comment commentEi = doc.createComment(COMM2);
				final Comment commentEi1 = doc.createComment("AuthorInstitution");
				final Comment commentEi2 = doc.createComment(COMM2);
				eeElem12.getParentNode().insertBefore(commentEi, eeElem12);
				eeElem12.getParentNode().insertBefore(commentEi1, eeElem12);
				eeElem12.getParentNode().insertBefore(commentEi2, eeElem12);

				final Element eeElem14 = doc.createElement(VALUE);
				eeElem13.appendChild(eeElem14);
				eeElem14.setTextContent(getAuthorInstitution(iAuthor, file));
			}
			final Element eeElem12 = doc.createElement(SLOT);
			eeElem11.appendChild(eeElem12);
			eeElem12.setAttribute(NAME, "authorPerson");
			final Element eeElem13 = doc.createElement(VLIST);
			eeElem12.appendChild(eeElem13);

			final Comment commentEi = doc.createComment(COMM2);
			final Comment commentEi1 = doc.createComment("AuthorPerson");
			final Comment commentEi2 = doc.createComment(COMM2);
			eeElem12.getParentNode().insertBefore(commentEi, eeElem12);
			eeElem12.getParentNode().insertBefore(commentEi1, eeElem12);
			eeElem12.getParentNode().insertBefore(commentEi2, eeElem12);

			final Element eeElem14 = doc.createElement(VALUE);
			eeElem13.appendChild(eeElem14);
			eeElem14.setTextContent(getAuthorPerson(iAuthor, file));

			final String sAuthorRole = getXpathSingleValue(file,
					AUTHOR + iAuthor + "]/*:functionCode/@displayName/string()");
			if (!sAuthorRole.startsWith(EXCLAMATION) && !sAuthorRole.isEmpty()) {
				final Element eeElem15 = doc.createElement(SLOT);
				eeElem11.appendChild(eeElem15);
				eeElem15.setAttribute(NAME, "authorRole");
				final Element eeElem16 = doc.createElement(VLIST);
				eeElem15.appendChild(eeElem16);
				final Element eeElem17 = doc.createElement(VALUE);
				eeElem16.appendChild(eeElem17);
				eeElem17.setTextContent(sAuthorRole);

				final Comment commentEs = doc.createComment(COMM2);
				final Comment commentEs1 = doc.createComment("AuthorRole");
				final Comment commentEs2 = doc.createComment(COMM2);
				eeElem15.getParentNode().insertBefore(commentEs, eeElem15);
				eeElem15.getParentNode().insertBefore(commentEs1, eeElem15);
				eeElem15.getParentNode().insertBefore(commentEs2, eeElem15);
			}
			final String xPathString = CLINAUTHOR + iAuthor + "]/*:assignedAuthor/*:code/@code";
			final String sAssigAutEx = getXpathSingleValue(file, xPathString);
			if (!sAssigAutEx.startsWith(EXCLAMATION) && !sAssigAutEx.isEmpty()) {
				final Element eeElem15 = doc.createElement(SLOT);
				eeElem11.appendChild(eeElem15);
				eeElem15.setAttribute(NAME, "authorSpecialty");
				final Element eeElem16 = doc.createElement(VLIST);
				eeElem15.appendChild(eeElem16);

				final Comment commentEs = doc.createComment(COMM2);
				final Comment commentEs1 = doc.createComment("AuthorSpecialty");
				final Comment commentEs2 = doc.createComment(COMM2);
				eeElem15.getParentNode().insertBefore(commentEs, eeElem15);
				eeElem15.getParentNode().insertBefore(commentEs1, eeElem15);
				eeElem15.getParentNode().insertBefore(commentEs2, eeElem15);

				final Element eeElem17 = doc.createElement(VALUE);
				eeElem16.appendChild(eeElem17);
				eeElem17.setTextContent(getAuthorSpecialty(iAuthor, file));
			}
		}
		final String sLoincCode = getXpathSingleValue(file, "//*:ClinicalDocument/*:code/@code/string()");
		final File fileNos = new File(Constant.FILENAME + Constant.IMAGE9 + Constant.URLFILEJSON);
		final File fileNosTre = new File(Constant.FILENAME + Constant.IMAGE9 + Constant.URLFILEJSONTRE);
		List<File> xmlFile;
		File xFile;
		File aFile;
		File jFile = null;
		File jjFile = null;
		File tFile;
		File ttFile = null;
		File dest;
		File dest1;
		File dest2;
		String saveDir = Constant.INTEROPFOLDER;
		try {
			final String fileF = urlX04;
			dest = downloadFile(fileF, saveDir);

			final String fileF1 = urlA11;
			dest1 = downloadFile(fileF1, saveDir);

			final String fileF2 = urlA04;
			dest2 = downloadFile(fileF2, saveDir);

			final String absolutePath = dest.getAbsolutePath();
			xFile = new File(absolutePath);
			final String absolutePath1 = dest1.getAbsolutePath();
			aFile = new File(absolutePath1);
			final String absolutePath2 = dest2.getAbsolutePath();
			tFile = new File(absolutePath2);
			xmlFile = XdmUtilities.downloadUsingNIO(urlNos, fileNos.getAbsolutePath(), urlCode1);
			final List<File> xmlFileCs = XdmUtilities.downloadUsingNIO(urlCode, fileNosTre.getAbsolutePath(), urlCode1);
			for (final File fileX : xmlFile) {
				if (fileX.getName().startsWith("JDV_J06_")) {
					jFile = fileX;
				}
				if (fileX.getName().startsWith("JDV_J10_")) {
					jjFile = fileX;
				}
			}
			for (final File fileX : xmlFileCs) {
				if (fileX.getName().startsWith("TRE_A05_")) {
					ttFile = fileX;
				}
			}
			final String retour = XdmUtilities.marshelling(xFile, sLoincCode);
			final List<String> retourJdv = XdmUtilities.marshellingJdv(jFile, retour);
			if (retourJdv != null && !retourJdv.isEmpty()) {
				getXpathSingleValue(xFile,
						"//*:RetrieveValueSetResponse/*:ValueSet/*:MappedConceptList/*:MappedConcept/*:Concept[@code="
								+ "'" + sLoincCode + "'" + "]");
				final String sTemplateID = recupereTemplateId(file);
				final String gTemplateID = sTemplateID;
				final Element eeElem11 = doc.createElement(CLASSIFICATION);
				eElem2.appendChild(eeElem11);
				eeElem11.setAttribute("id", random());
				eeElem11.setAttribute(SCHEME, "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a");
				eeElem11.setAttribute(CLASSOBJ, UUIDID);
				eeElem11.setAttribute(NODE, retour);
				final Comment commentEs = doc.createComment(COMM2);
				final Comment commentEs1 = doc.createComment("Class");
				final Comment commentEs2 = doc.createComment(COMM2);
				eeElem11.getParentNode().insertBefore(commentEs, eeElem11);
				eeElem11.getParentNode().insertBefore(commentEs1, eeElem11);
				eeElem11.getParentNode().insertBefore(commentEs2, eeElem11);
				addExtrinsicObjectSlot(CODESCH, retourJdv.get(0), doc, eeElem11);
				// ns2:Name
				final Element sixElement = doc.createElement(NAME1);
				eeElem11.appendChild(sixElement);
				// ns2:LocalizedString
				final Element sevenElement = doc.createElement(LOCALE);
				sevenElement.setAttribute(VAL, retourJdv.get(1));
				sixElement.appendChild(sevenElement);
				final String sConfDN = getXpathSingleValue(file,
						"//*:ClinicalDocument/*:confidentialityCode/@displayName/string()");
				final String sConfCode = getXpathSingleValue(file,
						"//*:ClinicalDocument/*:confidentialityCode/@code/string()");
				final String sCodingScheme = getXpathSingleValue(file,
						"//*:ClinicalDocument/*:confidentialityCode/@codeSystem/string()");
				final Element eeEle11 = doc.createElement(CLASSIFICATION);
				eElem2.appendChild(eeEle11);
				eeEle11.setAttribute("id", random());
				eeEle11.setAttribute(SCHEME, "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f");
				eeEle11.setAttribute(CLASSOBJ, UUIDID);
				eeEle11.setAttribute(NODE, sConfCode);
				addExtrinsicObjectSlot(CODESCH, sCodingScheme, doc, eeEle11);

				final Comment commentCo = doc.createComment(COMM2);
				final Comment commentCo1 = doc.createComment("Confidentiality");
				final Comment commentCo2 = doc.createComment(COMM2);
				eeEle11.getParentNode().insertBefore(commentCo, eeEle11);
				eeEle11.getParentNode().insertBefore(commentCo1, eeEle11);
				eeEle11.getParentNode().insertBefore(commentCo2, eeEle11);

				// ns2:Name
				final Element sixxElement = doc.createElement(NAME1);
				eeEle11.appendChild(sixxElement);
				// ns2:LocalizedString
				final Element sevennElement = doc.createElement(LOCALE);
				sevennElement.setAttribute(VAL, sConfDN);
				sixxElement.appendChild(sevennElement);
				final String sNonXMLMediaType = getXpathSingleValue(file,
						"/*:ClinicalDocument/*:component/*:nonXMLBody/*:text/@mediaType/string()");
				String sFormatCode = XdmUtilities.getXmlns(aFile, gTemplateID);
				final List<String> listJdv = XdmUtilities.marshellingJdv(jjFile, sFormatCode);
				if (listJdv != null && !listJdv.isEmpty()) {
					String sFormatCDName = listJdv.get(1);
					String sFormatCodeSystem = listJdv.get(0);
					if (sNonXMLMediaType.equals("application/pdf")) {
						sFormatCode = "urn:ihe:iti:xds-sd:pdf:2008";
						sFormatCDName = "Document à corps non structuré en Pdf/A-1";
						sFormatCodeSystem = "1.2.250.1.213.1.1.4.12";
					}
					final Element eeEle12 = doc.createElement(CLASSIFICATION);
					eElem2.appendChild(eeEle12);
					eeEle12.setAttribute("id", random());
					eeEle12.setAttribute(SCHEME, "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d");
					eeEle12.setAttribute(CLASSOBJ, UUIDID);
					eeEle12.setAttribute(NODE, sFormatCode);

					final Comment commentF = doc.createComment(COMM2);
					final Comment commentF1 = doc.createComment("Format");
					final Comment commentF2 = doc.createComment(COMM2);
					eeEle12.getParentNode().insertBefore(commentF, eeEle12);
					eeEle12.getParentNode().insertBefore(commentF1, eeEle12);
					eeEle12.getParentNode().insertBefore(commentF2, eeEle12);
					addExtrinsicObjectSlot(CODESCH, sFormatCodeSystem, doc, eeEle12);
					// ns2:Name
					final Element sixxxElement = doc.createElement(NAME1);
					eeEle12.appendChild(sixxxElement);
					// ns2:LocalizedString
					final Element sevennnElement = doc.createElement(LOCALE);
					sevennnElement.setAttribute(VAL, sFormatCDName);
					sixxxElement.appendChild(sevennnElement);
				}
			}
			final String sHealthcareFC = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@code/string()");
			final String sHealthcareFCS = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@codeSystem/string()");
			final String sHealthcareFDN = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@displayName/string()");
			final Element eeEle13 = doc.createElement(CLASSIFICATION);
			eElem2.appendChild(eeEle13);
			eeEle13.setAttribute("id", random());
			eeEle13.setAttribute(SCHEME, "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1");
			eeEle13.setAttribute(CLASSOBJ, UUIDID);
			eeEle13.setAttribute(NODE, sHealthcareFC);

			final Comment commentH = doc.createComment(COMM2);
			final Comment commentH1 = doc.createComment("HealthcareFacility");
			final Comment commentH2 = doc.createComment(COMM2);
			eeEle13.getParentNode().insertBefore(commentH, eeEle13);
			eeEle13.getParentNode().insertBefore(commentH1, eeEle13);
			eeEle13.getParentNode().insertBefore(commentH2, eeEle13);

			addExtrinsicObjectSlot(CODESCH, sHealthcareFCS, doc, eeEle13);
			// ns2:Name
			final Element septElement = doc.createElement(NAME1);
			eeEle13.appendChild(septElement);
			// ns2:LocalizedString
			final Element septtElement = doc.createElement(LOCALE);
			septtElement.setAttribute(VAL, sHealthcareFDN);
			septElement.appendChild(septtElement);
			final String sPracticeSC = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@code/string()");
			final String sPracticeSDN = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@displayName/string()");
			final String sCodingSchem = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@codeSystem/string()");
			final Element eeEle14 = doc.createElement(CLASSIFICATION);
			eElem2.appendChild(eeEle14);
			eeEle14.setAttribute("id", random());
			eeEle14.setAttribute(SCHEME, "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead");
			eeEle14.setAttribute(CLASSOBJ, UUIDID);
			eeEle14.setAttribute(NODE, sPracticeSC);

			final Comment commentP = doc.createComment(COMM2);
			final Comment commentP1 = doc.createComment("Practice Settings");
			final Comment commentP2 = doc.createComment(COMM2);
			eeEle14.getParentNode().insertBefore(commentP, eeEle14);
			eeEle14.getParentNode().insertBefore(commentP1, eeEle14);
			eeEle14.getParentNode().insertBefore(commentP2, eeEle14);

			addExtrinsicObjectSlot(CODESCH, sCodingSchem, doc, eeEle14);
			// ns2:Name
			final Element sepElement = doc.createElement(NAME1);
			eeEle14.appendChild(sepElement);
			// ns2:LocalizedString
			final Element septttElement = doc.createElement(LOCALE);
			septttElement.setAttribute(LANG, "FR");
			septttElement.setAttribute(CHARSET, UTF8);
			septttElement.setAttribute(VAL, sPracticeSDN);
			sepElement.appendChild(septttElement);
			List<String> list = XdmUtilities.marshellingTre(tFile, sLoincCode);
			String sTypeDisplayName;
			String sTypeCodeSystem;
			if (!list.isEmpty() && !list.get(1).isEmpty()) {
				sTypeDisplayName = list.get(1);
				sTypeCodeSystem = list.get(0);
			} else {
				list = XdmUtilities.marshellingJdv(ttFile, sLoincCode);
				sTypeDisplayName = list.get(1);
				sTypeCodeSystem = list.get(0);
			}
			final Element eeEle15 = doc.createElement(CLASSIFICATION);
			eElem2.appendChild(eeEle15);
			eeEle15.setAttribute("id", random());
			eeEle15.setAttribute(SCHEME, "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983");
			eeEle15.setAttribute(CLASSOBJ, UUIDID);
			eeEle15.setAttribute(NODE, sLoincCode);

			final Comment commentT = doc.createComment(COMM2);
			final Comment commentT1 = doc.createComment("Type");
			final Comment commentT2 = doc.createComment(COMM2);
			eeEle15.getParentNode().insertBefore(commentT, eeEle15);
			eeEle15.getParentNode().insertBefore(commentT1, eeEle15);
			eeEle15.getParentNode().insertBefore(commentT2, eeEle15);

			addExtrinsicObjectSlot(CODESCH, sTypeCodeSystem, doc, eeEle15);
			// ns2:Name
			final Element sepElementt = doc.createElement(NAME1);
			eeEle15.appendChild(sepElementt);
			// ns2:LocalizedString
			final Element septtttElement = doc.createElement(LOCALE);
			septtttElement.setAttribute(LANG, "FR");
			septtttElement.setAttribute(CHARSET, UTF8);
			septtttElement.setAttribute(VAL, sTypeDisplayName);
			sepElementt.appendChild(septtttElement);
			// ExternalIdentifier
			final Element eeEle16 = doc.createElement(EXIDENTIFIER);
			eElem2.appendChild(eeEle16);
			eeEle16.setAttribute("id", random());
			eeEle16.setAttribute(IDSCHEME, "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427");
			eeEle16.setAttribute(REGISTRY, "urn:uuid:4a889fd4-012f-4000-e008-12c48372fe64");
			eeEle16.setAttribute(VAL, getPatientId(file));

			final Comment commentPid = doc.createComment(COMM2);
			final Comment commentPid1 = doc.createComment("PatientId");
			final Comment commentPid2 = doc.createComment(COMM2);
			eeEle16.getParentNode().insertBefore(commentPid, eeEle16);
			eeEle16.getParentNode().insertBefore(commentPid1, eeEle16);
			eeEle16.getParentNode().insertBefore(commentPid2, eeEle16);

			// ns2:Name
			final Element sepElementtt = doc.createElement(NAME1);
			eeEle16.appendChild(sepElementtt);
			// ns2:LocalizedString
			final Element septtttElementt = doc.createElement(LOCALE);
			septtttElementt.setAttribute(VAL, "XDSDocumentEntry.patientId");
			sepElementtt.appendChild(septtttElementt);
			// ExternalIdentifier
			final String sExtension = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@extension/string()");
			String sUniqueId;
			if (sExtension.isEmpty()) {
				sUniqueId = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@root/string()");
			} else {
				sUniqueId = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@root/string()") + "^" + sExtension;
			}
			final Element eeEle17 = doc.createElement(EXIDENTIFIER);
			eElem2.appendChild(eeEle17);
			eeEle17.setAttribute("id", random());
			eeEle17.setAttribute(IDSCHEME, "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab");
			eeEle17.setAttribute(REGISTRY, "b576aac4-33be-4875-b875-f8ee50bf66e0");
			eeEle17.setAttribute(VAL, sUniqueId);

			final Comment commentUid = doc.createComment(COMM2);
			final Comment commentUid1 = doc.createComment("UniqueId");
			final Comment commentUid2 = doc.createComment(COMM2);
			eeEle17.getParentNode().insertBefore(commentUid, eeEle17);
			eeEle17.getParentNode().insertBefore(commentUid1, eeEle17);
			eeEle17.getParentNode().insertBefore(commentUid2, eeEle17);

			// ns2:Name
			final Element sepElementttt = doc.createElement(NAME1);
			eeEle17.appendChild(sepElementttt);
			// ns2:LocalizedString
			final Element septtttElementtt = doc.createElement(LOCALE);
			septtttElementtt.setAttribute(LANG, "FR");
			septtttElementtt.setAttribute(CHARSET, UTF8);
			septtttElementtt.setAttribute(VAL, "XDSDocumentEntry.uniqueId");
			sepElementttt.appendChild(septtttElementtt);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
				errorV = error;
			}
		} finally {
			if (jFile != null) {
				jFile.delete();
			}
			if (jjFile != null) {
				jjFile.delete();
			}
			if (ttFile != null) {
				ttFile.delete();
			}
		}
		return errorV;
	}

	/**
	 * recupereTemplateId
	 * 
	 * @param file
	 * @return
	 */
	private static String recupereTemplateId(final File file) {
		return getXpathSingleValue(file, "//*:ClinicalDocument/*:templateId[last()]/@root/string()");
	}

	/**
	 * getLegalAuthenticator
	 * 
	 * @param SaxonWrapperDocument
	 * @return
	 */
	private static String getLegalAuthenticator(final File file) {
		final String sC1Id = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:id/@extension/string()");
		final String sC2Nom = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:assignedPerson/*:name/*:family/string()");
		final String sC3Prenom = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:assignedPerson/*:name/*:given/string()");
		final String sC9AssigningAut = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:id/@root//string()");
		final String sC10TypeNom = "D";
		final String sC13TypeId = "IDNPS";
		return sC1Id + "^" + sC2Nom + "^" + sC3Prenom + "^^^^^^&amp;" + sC9AssigningAut + ISO + sC10TypeNom + "^^^"
				+ sC13TypeId;
	}

	/**
	 * addExtrinsicObjectSlot
	 * 
	 * @param pNomMetadonnee
	 * @param pValeurMetadonnee
	 */
	private static Element addExtrinsicObjectSlot(final String pNomMetadonnee, final String pValeurMetadonnee,
			final Document doc, final Element elem) {
		final Element eeElem3 = doc.createElement(SLOT);
		eeElem3.setAttribute(NAME, pNomMetadonnee);
		elem.appendChild(eeElem3);
		final Element eeeElem2 = doc.createElement(VLIST);
		eeElem3.appendChild(eeeElem2);
		final Element eeeElem3 = doc.createElement(VALUE);
		eeeElem2.appendChild(eeeElem3);
		eeeElem3.setTextContent(String.valueOf(pValeurMetadonnee));
		return eeElem3;
	}

	/**
	 * getXpathSingleValue
	 * 
	 * @param file
	 * @return
	 */
	public static String getXpathSingleValue(final File file, final String expression) {
		String content = "";
		final Processor saxonProcessor = new Processor(false);
		final net.sf.saxon.s9api.DocumentBuilder builde = saxonProcessor.newDocumentBuilder();
		try {
			final XdmNode doc = builde.build(file);
			final XPathCompiler xpath = saxonProcessor.newXPathCompiler();
			final XPathSelector xdm = xpath.compile(expression).load();
			xdm.setContextItem(doc);
			if (xdm.evaluateSingle() != null) {
				content = xdm.evaluateSingle().toString();
			}
		} catch (final SaxonApiException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return content;
	}

	/**
	 * getAuthorInstitution
	 * 
	 * @param iAuthor
	 * @param cda
	 * @return
	 */
	private static String getAuthorInstitution(final Integer iAuthor, final File cda) {
		String retour;
		final String pointEx = "!!!";
		final String sRoot = getXpathSingleValue(cda,
				CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:representedOrganization/string()");
		if (sRoot.startsWith(pointEx) || sRoot.isEmpty()) {
			retour = "";
		} else {
			String sC1Nom = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:representedOrganization/*:name/string()");
			if (sC1Nom.startsWith(pointEx)) {
				sC1Nom = "";
			}
			String sC6AssigningAuth = getXpathSingleValue(cda, CLINAUTHOR + iAuthor.toString()
					+ "]/*:assignedAuthor/*:representedOrganization/*:id/@root/string()");
			if (sC6AssigningAuth.startsWith(pointEx)) {
				sC6AssigningAuth = "";
			}
			final String sC7TypeId = "IDNST";
			String sC10TypeId = getXpathSingleValue(cda, CLINAUTHOR + iAuthor.toString()
					+ "]/*:assignedAuthor/*:representedOrganization/*:id/@extension/string()");
			if (sC10TypeId.startsWith(pointEx)) {
				sC10TypeId = "";
			}
			retour = sC1Nom + "^^^^^&amp;" + sC6AssigningAuth + ISO + sC7TypeId + "^^^" + sC10TypeId;
		}
		return retour;
	}

	/**
	 * 
	 * @param SaxonWrapperDocument
	 * @return
	 */
	private static String getAuthorPerson(final Integer iAuthor, final File cda) {
		final String sC1Id = getXpathSingleValue(cda,
				CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:id/@extension/string()");
		final String sPersonneExiste = getXpathSingleValue(cda,
				CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:assignedPerson/string()");
		String sC2Nom;
		String sC3Prenom;
		String sC10TypeNom;
		String sC13TypeId;
		if (!sPersonneExiste.startsWith(EXCLAMATION) && !sPersonneExiste.isEmpty()) {
			sC2Nom = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:assignedPerson/*:name/*:family/string()");
			sC3Prenom = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:assignedPerson/*:name/*:given/string()");
			sC10TypeNom = "D";
			if (sC1Id.contains("/")) {
				sC13TypeId = "EI";
			} else {
				sC13TypeId = "IDNPS";
			}
		} else { // Pas de personne trouvée. Il s'agit d'un dispositif
			sC2Nom = getXpathSingleValue(cda, CLINAUTHOR + iAuthor.toString()
					+ "]/*:assignedAuthor/*:assignedAuthoringDevice/*:softwareName/string()");
			sC3Prenom = getXpathSingleValue(cda, CLINAUTHOR + iAuthor.toString()
					+ "]/*:assignedAuthor/*:assignedAuthoringDevice/*:manufacturerModelName/string()");
			sC10TypeNom = "U";
			sC13TypeId = "RI";
		}
		final String sC9assigningAut = getXpathSingleValue(cda,
				CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:id/@root/string()");
		return sC1Id + "^" + sC2Nom + "^" + sC3Prenom + "^^^^^^&amp;" + sC9assigningAut + ISO + sC10TypeNom + "^^^"
				+ sC13TypeId;
	}

	/**
	 * getAuthorSpecialty
	 * 
	 * @param iAuthor
	 * @param cda
	 * @return
	 */
	private static String getAuthorSpecialty(final Integer iAuthor, final File cda) {
		String retour;
		final String sAssignedAutEx = getXpathSingleValue(cda,
				AUTHOR + iAuthor.toString() + "]/*:assignedAuthor/string()");
		if (sAssignedAutEx.startsWith(EXCLAMATION) || sAssignedAutEx.isEmpty()) {
			retour = "";
		} else {
			final String sC1Id = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:code/@code/string()");
			final String sC2Intitule = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:code/@displayName/string()");
			final String sC3CodeSystem = getXpathSingleValue(cda,
					CLINAUTHOR + iAuthor.toString() + "]/*:assignedAuthor/*:code/@codeSystem/string()");
			retour = sC1Id + "^" + sC2Intitule + "^" + sC3CodeSystem;
		}
		return retour;
	}

	/**
	 * getPatientId
	 * 
	 * @return
	 */
	private static String getPatientId(final File cda) {
		final String[][] toId = { { "1.2.250.1.213.1.4.8", "NH" }, { "1.2.250.1.213.1.4.9", "NH" },
				{ "1.2.250.1.213.1.4.10", "NH" }, { "1.2.250.1.213.1.4.11", "NH" } };
		String sOIDReconnu;
		String sTypeINSReconnu;
		String sIdentifiant;
		for (int i = 0; i < toId.length; i++) {
			final String sQueryResult = getXpathSingleValue(cda,
					"//*:recordTarget/*:patientRole/*:id[@root='" + toId[i][0] + "']/@extension/string()");
			if (!sQueryResult.startsWith(EXCLAMATION) && !sQueryResult.isEmpty()) {
				sIdentifiant = sQueryResult;
				sOIDReconnu = toId[i][0];
				sTypeINSReconnu = toId[i][1];
				return sIdentifiant + "^^^&amp;" + sOIDReconnu + ISO + sTypeINSReconnu;
			}
		}
		final String sC1IdLocal = getXpathSingleValue(cda,
				"//*:recordTarget/*:patientRole/*:id[1]/@extension/string()");
		final String sC4AssigningAut = getXpathSingleValue(cda, "//*:recordTarget/*:patientRole/*:id/@root/string()");
		final String sC5TypeId = "PI";
		if (!sC1IdLocal.startsWith(EXCLAMATION) && !sC1IdLocal.isEmpty()) {
			return sC1IdLocal + "^^^&amp;" + sC4AssigningAut + ISO + sC5TypeId;
		} else {
			return "!!!{getPatientId():Pas d'INS-C ni de NIR trouvé}";
		}
	}

	/**
	 * downloadFile
	 * 
	 * @param fileURL
	 * @param saveDir
	 * @throws IOException
	 */
	public static File downloadFile(final String fileURL, final String saveDir) throws IOException {
		final URL url = new URL(fileURL);
		final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		final int responseCode = httpConnection.getResponseCode();
		String saveFilePath = "";
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			final String disposition = httpConnection.getHeaderField("Content-Disposition");

			if (disposition != null && disposition.contains("filename=")) {
				fileName = disposition.split("filename=")[1].replaceAll("\"", "");
			} else {
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
			}
			final InputStream inputStream = httpConnection.getInputStream();
			saveFilePath = saveDir + File.separator + fileName;
			final FileOutputStream outputStream = new FileOutputStream(saveFilePath);
			int bytesRead = -1;
			final byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();
		}
		httpConnection.disconnect();
		return new File(saveFilePath);
	}
}