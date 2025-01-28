package com.ans.cda.fhir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.ValueSet;
import org.json.JSONObject;

import com.ans.cda.service.parametrage.IniFile;
import com.ans.cda.utilities.general.Constant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * FhirUtilities
 * 
 * @author nizarben salem
 */
public class FhirUtilities {
	/**
	 * ACTIVE_STATUS
	 */
	private static final int ACTIVE_STATUS = 200;
	/**
	 * codeSystemsOntoserver
	 */
	public static final List<String[]> CODESYSTEMSONTOSERVER = new ArrayList<>();
	/**
	 * codeSystemsOntoserverNoContent
	 */
	public static final List<String[]> CODESYSTEMSONTOSERVERNOCONTENT = new ArrayList<>();
	/**
	 * distinctCodeSystemUsed
	 */
	public static List<String> distinctCodeSystemUsed = new ArrayList<>();
	/**
	 * distinctCodeUsed
	 */
	public static List<String> distinctCodeUsed = new ArrayList<>();
	/**
	 * nbMappingDone
	 */
	public static int nbMappingDone = 0;
	/**
	 * nbMappingFailed
	 */
	public static int nbMappingFailed = 0;
	/**
	 * nbValueSets
	 */
	public static int nbValueSets = 0;
	/**
	 * nbCodeSystemsUsed
	 */
	public static int nbCodeSystemsUsed = 0;
	/**
	 * cache
	 */
	private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();
	/**
	 * map
	 */
	public static Map<String, Long> map = new ConcurrentHashMap<>();
	/**
	 * content
	 */
	public static StringBuilder content = new StringBuilder();
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(FhirUtilities.class);

	/**
	 * initializeLogFiles
	 */
	public static void initializeLogFiles() {
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.FAILED).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.FAILED).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DONE).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DONE).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.INCORRECTDISP).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.INCORRECTDISP).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNCODE).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNCODE).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNSYSTEM).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNSYSTEM).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.VALIDATEFAIL).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.VALIDATEFAIL).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGSYSTEMNOCONTENT).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGSYSTEMNOCONTENT).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.RAPPORTGLOBAL).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.RAPPORTGLOBAL).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNCODE).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNCODE).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNSYSTEM).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNSYSTEM).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISPALY).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISPALY).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISP).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISP).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODE).exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODE).delete();
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + "distinctCodeUsed").exists()) {
			new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODEUSED).delete();
		}
	}

	/**
	 * getLogFiles
	 * 
	 * @return
	 */
	public static List<File> getLogFiles() {
		final List<File> list = new ArrayList<>();
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.FAILED).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.FAILED));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DONE).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DONE));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.INCORRECTDISP).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.INCORRECTDISP));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNCODE).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNCODE));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNSYSTEM).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.UNKNOWNSYSTEM));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.VALIDATEFAIL).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.VALIDATEFAIL));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGSYSTEMNOCONTENT).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGSYSTEMNOCONTENT));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.RAPPORTGLOBAL).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.RAPPORTGLOBAL));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNCODE).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNCODE));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNSYSTEM).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGUNKNOWNSYSTEM));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISPALY).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISPALY));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISP).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.LOGINCORRECTDISP));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODE).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODE));
		}
		if (new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODEUSED).exists()) {
			list.add(new File(Constant.LOGFHIRFOLDER + "\\" + Constant.DISTINCTCODEUSED));
		}
		return list;
	}

	/**
	 * loadTerminology
	 */
	public static void loadTerminology(final File file) {
		CODESYSTEMSONTOSERVER.clear();
		CODESYSTEMSONTOSERVERNOCONTENT.clear();
		final String sNomFichier = file.getAbsolutePath();
		String line;
		Integer lineNumber = 0;
		try (BufferedReader breader = new BufferedReader(Files.newBufferedReader(Paths.get(sNomFichier)))) {
			line = breader.readLine();
			while (line != null) {
				lineNumber++;
				if (line.startsWith("[")) {
					IniFile.readTermino(line.substring(1, line.length() - 1), file, lineNumber);
				}
				line = breader.readLine();
			}
		} catch (final IOException ex) {
			if (LOGGER.isInfoEnabled()) {
				final String error = ex.getMessage();
				LOGGER.info(error);
			}
		}
	}

	/**
	 * deleteContentsRecursively
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void deleteContentsRecursively(final Path directory) throws IOException {
		if (!Files.exists(directory) || !Files.isDirectory(directory)) {
			return;
		}
		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, IOException exc) throws IOException {
				if (!dir.equals(directory)) {
					Files.delete(dir);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * isNumericOid
	 * 
	 * @param pNumericOid
	 * @return
	 */
	public static boolean isNumericOid(final String pNumericOid) {
		final char[] caracteres = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.' };
		for (final char c : pNumericOid.toCharArray()) {
			boolean found = false;
			for (final char validChar : caracteres) {
				if (c == validChar) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * convertSvsJdv2Fhir
	 * 
	 * @param pNomRepertoireSortie
	 */
	public static List<String> convertSvsJdv2Fhir(final String pNomRepertoireSortie, final File xlmFile,
			final CheckBox checkboxLimite, final TextField textField) {
		distinctCodeSystemUsed = new ArrayList<>();
		distinctCodeUsed = new ArrayList<>();
		nbMappingDone = 0;
		nbMappingFailed = 0;
		final String sRacineCanonique = IniFile.read("RACINE-CANONIQUE", Constant.FHIRMOD);
		final boolean pReplaceUnderS = true;
		final boolean pReductJDVName64 = false;

		final Processor saxonProcessor = new Processor(false);
		final net.sf.saxon.s9api.DocumentBuilder builde = saxonProcessor.newDocumentBuilder();
		XPathSelector selector;
		final XPathCompiler compiler;
		final List<String> list = new ArrayList<>();
		final List<File> files = new ArrayList<>();
		try {
			final XdmNode doc = builde.build(xlmFile);
			compiler = saxonProcessor.newXPathCompiler();
			selector = compiler.compile("//*:terminology/valueSet").load();
			selector.setContextItem(doc);
			XdmValue xdmValues;
			xdmValues = selector.evaluate();
			int max;
			if (checkboxLimite.isSelected() && !textField.getText().isEmpty()) {
				max = Integer.parseInt(textField.getText());
			} else {
				max = xdmValues.size();
			}
			int count = 0;
			for (final XdmItem item : xdmValues) {
				if (item instanceof XdmNode && count < max) {
					final XdmNode myNode = (XdmNode) item;
					String sName = myNode.getAttributeValue(new QName("name"));
					if (pReplaceUnderS) {
						sName = sName.replace('-', '_');
					}
					if (pReductJDVName64) {
						if (sName.length() > 64)
							sName = sName.substring(0, 63);
					}
					String sId = myNode.getAttributeValue(new QName("id"));
					if (sId.startsWith("urn:oid:")) {
						sId = sId.substring(8);
					}
					String sDisplayName = myNode.getAttributeValue(new QName("displayName"));
					sDisplayName = sDisplayName.replace("\"", "&quot;");
					sDisplayName = ansCase(sDisplayName);
					final String sUrl = sRacineCanonique + "ValueSet/" + sId;
					final String sStatusAD = myNode.getAttributeValue(new QName("statusCode"));
					final String sRevisionDate = myNode.getAttributeValue(new QName("revisionDate"));
					final String sVersionDate = sRevisionDate.replace("-", "").replace(":", "").replace("T", "");
					final String sDate = sRevisionDate + "+01:00";
					final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
					final ZonedDateTime zonedDateTime = ZonedDateTime.parse(sDate, formatter);
					LocalDateTime dateTime = null;
					try {
						dateTime = zonedDateTime.toLocalDateTime();
					} catch (final DateTimeParseException e) {
						if (LOGGER.isInfoEnabled()) {
							final String error = e.getMessage();
							LOGGER.error(error);
						}
					}
					final Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
					PublicationStatus sStatusFhir;

					switch (sStatusAD) {
					case "draft":
						sStatusFhir = PublicationStatus.DRAFT;
						break;
					case "active":
						sStatusFhir = PublicationStatus.ACTIVE;
						break;
					case "retired":
						sStatusFhir = PublicationStatus.RETIRED;
						break;
					case "final":
						sStatusFhir = PublicationStatus.ACTIVE;
						break;
					default:
						sStatusFhir = PublicationStatus.UNKNOWN;
						break;
					}

					final ValueSet monJdv = new ValueSet();

					monJdv.setName(sName);
					monJdv.setId(sId);
					monJdv.setUrl(sUrl);
					monJdv.setTitle(sDisplayName);
					monJdv.setStatus(sStatusFhir);
					monJdv.setDate(date);
					monJdv.setExperimental(false);
					monJdv.setPublisher(
							"Agence du Numérique en Santé(ANS) -2 - 10 Rue d'Oradour-sur-Glane, 75015 Paris");
					monJdv.setDescription(sDisplayName);

					monJdv.setVersion(sVersionDate);

					String sIdentifierValue = sId;
					if (!sIdentifierValue.startsWith("urn:oid:")) {
						sIdentifierValue = "urn:oid:" + sId;
					}
					final Identifier identifier = new Identifier();
					identifier.setValue(sIdentifierValue);
					identifier.setSystem("urn:ietf:rfc:3986");
					monJdv.getIdentifier().add(identifier);

					final Meta md = new Meta();
					final List<CanonicalType> profiles = new ArrayList<>();
					profiles.add(new CanonicalType("http://hl7.org/fhir/StructureDefinition/shareablevalueset"));
					md.setProfile(profiles);
					monJdv.setMeta(md);

					final Period valuePeriod = new Period();
					final ZonedDateTime zonedDateTim = ZonedDateTime.parse("2021-03-15T00:00:00+01:00", formatter);
					LocalDateTime dateTim = null;
					try {
						dateTim = zonedDateTim.toLocalDateTime();
					} catch (final DateTimeParseException e) {
						if (LOGGER.isInfoEnabled()) {
							final String error = e.getMessage();
							LOGGER.error(error);
						}
					}
					final Date dateT = Date.from(dateTim.atZone(ZoneId.systemDefault()).toInstant());
					valuePeriod.setStart(dateT);
					final Extension effectivePeriod = new Extension(
							"http://hl7.org/fhir/StructureDefinition/resource-effectivePeriod", valuePeriod);
					monJdv.getExtension().add(effectivePeriod);

					final ValueSet.ValueSetComposeComponent monCompose = new ValueSet.ValueSetComposeComponent();
					final ValueSet.ConceptSetComponent monComposantConcept = new ValueSet.ConceptSetComponent();

					final XPathSelector selectorConcepts = compiler.compile("conceptList/concept").load();
					XdmValue xdmConcepts;
					selectorConcepts.setContextItem(myNode);
					xdmConcepts = selectorConcepts.evaluate();

					for (final XdmItem concept : xdmConcepts) {
						if (concept instanceof XdmNode) {
							final XdmNode myConcept = (XdmNode) concept;
							String sConceptCodeSystem = myConcept.getAttributeValue(new QName("codeSystem"));
							String sConceptCode = myConcept.getAttributeValue(new QName("code")).trim();
							sConceptCode = sConceptCode.replaceAll("\\s", "");
							String sConceptDisplayName = myConcept.getAttributeValue(new QName("displayName"));
							sConceptDisplayName = sConceptDisplayName.replace("\"", "&quot;");

							if ((!distinctCodeSystemUsed.contains(sConceptCodeSystem))
									&& (!sConceptCodeSystem.equals(""))) {
								distinctCodeSystemUsed.add(sConceptCodeSystem);
							}

							final String sCodeInfoLine = sConceptCode + " [" + sConceptCodeSystem + "]-{"
									+ sConceptDisplayName + "}";
							if (!distinctCodeUsed.contains(sCodeInfoLine)) {
								distinctCodeUsed.add(sCodeInfoLine);
							}
							String sUriCodeSystem = "";
							if (!sUriCodeSystem.startsWith("http")) {
								sUriCodeSystem = getUriFromOId(sConceptCodeSystem);
								sConceptCodeSystem = sUriCodeSystem;
							}

							final ValueSet.ConceptReferenceComponent monConcept = new ValueSet.ConceptReferenceComponent();
							monComposantConcept.setSystem(sConceptCodeSystem);
							monConcept.setCode(sConceptCode.trim());
							monConcept.setDisplay(sConceptDisplayName);

							boolean bCodeSystemExist = false;
							for (final ValueSet.ConceptSetComponent unComposantConcept : monCompose.getInclude()) {
								if (unComposantConcept.getSystem().equals(sConceptCodeSystem)) {
									unComposantConcept.getConcept().add(monConcept);
									bCodeSystemExist = true;
								}
							}
							if (!bCodeSystemExist) {
								final ValueSet.ConceptSetComponent monNouveauComposantConcept = new ValueSet.ConceptSetComponent();
								monNouveauComposantConcept.setSystem(sConceptCodeSystem);
								monNouveauComposantConcept.getConcept().add(monConcept);
								monCompose.getInclude().add(monNouveauComposantConcept);
							}
						}
					}

					monJdv.setCompose(monCompose);

					final String name = monJdv.getName() + "_" + monJdv.getStatus() + "_" + monJdv.getId();
					list.add(name.trim().replace(" ", ""));

					final FhirContext fhirContext = FhirContext.forR4();
					final IParser jsonParser = fhirContext.newJsonParser();
					jsonParser.setPrettyPrint(true);
					final String jdvContent = jsonParser.setPrettyPrint(true).encodeResourceToString(monJdv);
					try {
						final String fileName = (pNomRepertoireSortie + "\\" + monJdv.getName().trim() + "_"
								+ monJdv.getStatus().toString().trim() + "_" + monJdv.getId().trim() + ".json")
								.replace(" ", "");
						Files.write(Paths.get(fileName), jdvContent.getBytes(StandardCharsets.UTF_8));
						files.add(new File(fileName));
					} catch (final IOException e) {
						if (LOGGER.isInfoEnabled()) {
							final String error = e.getMessage();
							LOGGER.error(error);
						}
					}

					count++;
				}
			}
		} catch (final SaxonApiException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
		if (files != null) {
			content = new StringBuilder();
			for (final File file : files) {
				if (file.isFile()) {
					content.append(readFileContent(file));
					content.append("\n\n");
				}
			}
		}
		return list;

	}

	/**
	 * readFileContent
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileContent(final File file) {
		final StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Paths.get(file.toURI())))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
		return content.toString();
	}

	/**
	 * remplacerCaracteresAccentues
	 * 
	 * @param pChaineEntree
	 * @return
	 */
	public static String remplacerCaracteresAccentues(final String pChaineEntree) {
		byte[] tempBytes;
		tempBytes = new String(pChaineEntree.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
				.getBytes(StandardCharsets.UTF_8);
		return new String(tempBytes, StandardCharsets.UTF_8);
	}

	/**
	 * ansCase
	 * 
	 * @param inputString
	 * @return
	 */
	public static String ansCase(final String inputString) {
		return inputString.replace("-", "_");
	}

	/**
	 * getUriFromOId
	 * 
	 * @param pOid
	 * @return
	 */
	public static String getUriFromOId(final String pOid) {
		String sOid = pOid;
		if (!sOid.startsWith("urn:oid:")) {
			sOid = "urn:oid:" + sOid;
		}
		final File folder = new File(Constant.LOGFHIRFOLDER);
		final Path mappingDonePath = Paths.get(folder + "\\MappingDone.log");
		final Path mappingFailedPath = Paths.get(folder + "\\MappingFailed.log");
		final int nbNamingSystems = CODESYSTEMSONTOSERVER.size();
		for (int i = 0; i < nbNamingSystems; i++) {
			final String[] array = CODESYSTEMSONTOSERVER.get(i);
			if (array != null && array.length > 2 && array[1].equals(sOid)) {
				try {
					if (!mappingDonePath.toFile().exists()) {
						Files.createFile(mappingDonePath);
					}
					Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\MappingDone.log"),
							("Mapping found from " + sOid + " to " + array[2] + "\n").getBytes(),
							StandardOpenOption.APPEND);
				} catch (final IOException e) {
					if (LOGGER.isInfoEnabled()) {
						final String error = e.getMessage();
						LOGGER.error(error);
					}
				}
				nbMappingDone++;
				return array[2];
			}
		}
		try {
			if (!mappingFailedPath.toFile().exists()) {
				Files.createFile(mappingFailedPath);
			}
			Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\MappingFailed.log"),
					("Mapping failed for: " + sOid + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
		nbMappingFailed++;
		return pOid;
	}

	/**
	 * getValueSetFromJson
	 * 
	 * @param jsonContent
	 * @return
	 */
	public static ValueSet getValueSetFromJson(final String jsonContent) {
		ValueSet vs = null;
		try {
			final FhirContext ctx = FhirContext.forR4();
			final IParser parser = ctx.newJsonParser();
			vs = parser.parseResource(ValueSet.class, jsonContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vs;
	}

	/**
	 * validateCode
	 * 
	 * @param pCodeSystemUrl
	 * @param pCode
	 * @param pDisplay
	 * @return
	 */
	public static String validateCode(final String url, final String pCode, final String pDisplay) {
		String sValRet = "";
		try {
			final String uri = IniFile.read("URL-ONTOSERVER", Constant.FHIRMOD) + "CodeSystem/$validate-code";
			String response;
			response = postGlobalRequest(uri, url, pCode, pDisplay);
			if (response.startsWith("!!!{")) {
				sValRet += "!!!{ERROR}:" + "\n" + response;
			} else {
				try {
					final JSONObject object = new JSONObject(response);
					sValRet += object.toString();
				} catch (final Exception ex) {
					sValRet += "!!!{ERROR_EXCEPTION_" + ex.getMessage() + "}:\n" + response;
				}
			}
		} catch (final Exception e) {
			sValRet += "!!!{ERROR_EXCEPTION_" + e.getMessage() + "}";
		}
		return sValRet;
	}

	/**
	 * getToken
	 * 
	 * @param pTokenUrl
	 * @param pUsername
	 * @param pPassword
	 * @return
	 */
	public static String getToken(final String pTokenUrl, final String pUsername, final String pPassword) {
		String accessToken = "";
		try {
			final URL url = new URL(pTokenUrl);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			final Map<String, String> values = new ConcurrentHashMap<>();
			values.put("grant_type", "password");
			values.put("client_id", "ontoserver");
			values.put("username", pUsername);
			values.put("password", pPassword);
			final String postData = values.entrySet().stream()
					.map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
					.collect(Collectors.joining("&"));
			final byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			try (DataOutputStream stream = new DataOutputStream(connection.getOutputStream())) {
				stream.write(postDataBytes);
			}
			final StringBuilder response = new StringBuilder();
			try (BufferedReader breader = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
				String responseLine;
				while ((responseLine = breader.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				final JSONObject jsonResponse = new JSONObject(response.toString());
				accessToken = jsonResponse.getString("access_token");
			}
			connection.disconnect();
		} catch (final Exception ex) {
			return "!!!{ERROR}";
		}

		return accessToken;
	}

	/**
	 * postRequestWithToken
	 * 
	 * @param pUrl
	 * @param pToken
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postRequestWithToken(final String pUri, final String pUrl, final String pCode,
			final String pDisplay, final String token) {
		String responseContent = null;
		final String cacheKey = pCode;
		if (CACHE.containsKey(cacheKey)) {
			return CACHE.get(cacheKey); // Retourne directement le résultat en cache
		}
		org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig
				.custom().setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(30))) // Timeout for requesting
																							// connection from the pool
				.setResponseTimeout(Timeout.of(Duration.ofSeconds(60))) // Timeout for waiting for data
				.setConnectionKeepAlive(Timeout.of(Duration.ofSeconds(5))) // Keep-alive for idle connections
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(50); // Max total connections
		connManager.setDefaultMaxPerRoute(10);

		try (CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager)
				.setDefaultRequestConfig(requestConfig)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, Timeout.ofSeconds(1))).build()) {
			final URIBuilder uriBuilder = new URIBuilder(pUri);
			uriBuilder.addParameter("url", pUrl);
			uriBuilder.addParameter("code", pCode);
			uriBuilder.addParameter("display", pDisplay);
			final URI uri = uriBuilder.build();
			final HttpGet request = new HttpGet(
					uri);
			request.setHeader("Authorization", "Bearer " + token);
			try (@SuppressWarnings("deprecation")
			CloseableHttpResponse response = httpClient.execute(request)) {
				// Check if the response status is OK
				final int statusCode = response.getCode();
				if (statusCode == ACTIVE_STATUS) {
					try (InputStream inputStream = response.getEntity().getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

						StringBuilder responseContentBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							responseContentBuilder.append(line).append("\n");
						}
						responseContent = responseContentBuilder.toString();
						CACHE.put(cacheKey, responseContent);
					}
				}
			}
		} catch (final Exception e) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					((HttpURLConnection) new URL(pUrl).openConnection()).getErrorStream(), StandardCharsets.UTF_8))) {
				final StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				return response.toString();
			} catch (final Exception ex) {
				if (LOGGER.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOGGER.error(error);
				}
			}
		}
		return responseContent;
	}

	/**
	 * postSimpleRequest
	 * 
	 * @param pUrl
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postRequestWithCredentials(final String user, final String pwd, final String pUri,
			final String pUrl, final String pCode, final String pDisplay) {
		String responseContent = null;
		final String cacheKey = pCode;
		if (CACHE.containsKey(cacheKey)) {
			return CACHE.get(cacheKey); // Retourne directement le résultat en cache
		}
		final BasicCredentialsProvider credentialsPro = new BasicCredentialsProvider();
		credentialsPro.setCredentials(new AuthScope("smt.esante.gouv.fr", -1),
				new UsernamePasswordCredentials(user, pwd.toCharArray()));

		org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig
				.custom().setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(30))) // Timeout for requesting
																							// connection from the pool
				.setResponseTimeout(Timeout.of(Duration.ofSeconds(60))) // Timeout for waiting for data
				.setConnectionKeepAlive(Timeout.of(Duration.ofSeconds(5))) // Keep-alive for idle connections
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(50); // Max total connections
		connManager.setDefaultMaxPerRoute(10);

		try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsPro)
				.setConnectionManager(connManager).setDefaultRequestConfig(requestConfig)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, Timeout.ofSeconds(1))).build()) {
			final URIBuilder uriBuilder = new URIBuilder(pUri);
			uriBuilder.addParameter("url", pUrl);
			uriBuilder.addParameter("code", pCode);
			uriBuilder.addParameter("display", pDisplay);
			final URI uri = uriBuilder.build();
			final HttpGet request = new HttpGet(
					uri);
			try (@SuppressWarnings("deprecation")
			CloseableHttpResponse response = httpClient.execute(request)) {
				// Check if the response status is OK
				final int statusCode = response.getCode();
				if (statusCode == ACTIVE_STATUS) {
					try (InputStream inputStream = response.getEntity().getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

						StringBuilder responseContentBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							responseContentBuilder.append(line).append("\n");
						}
						responseContent = responseContentBuilder.toString();
						CACHE.put(cacheKey, responseContent);
					}
				}
			}
		} catch (final Exception e) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					((HttpURLConnection) new URL(pUrl).openConnection()).getErrorStream(), StandardCharsets.UTF_8))) {
				final StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				return response.toString();
			} catch (final Exception ex) {
				if (LOGGER.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOGGER.error(error);
				}
			}
		}
		return responseContent;
	}

	/**
	 * postSimpleRequest
	 * 
	 * @param pUrl
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postRequestWithCredentialsLoinc(final String user, final String pwd, final String pUri,
			final String pUrl, final String pCode) {
		final String cacheKey = pCode;
		if (CACHE.containsKey(cacheKey)) {
			return CACHE.get(cacheKey); // Retourne directement le résultat en cache
		}
		String responseContent = null;

		final BasicCredentialsProvider credentialsPro = new BasicCredentialsProvider();
		credentialsPro.setCredentials(new AuthScope("fhir.loinc.org", 443),
				new UsernamePasswordCredentials(user, pwd.toCharArray()));

		org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig
				.custom().setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(30))) // Timeout for requesting
																							// connection from the pool
				.setResponseTimeout(Timeout.of(Duration.ofSeconds(60))) // Timeout for waiting for data
				.setConnectionKeepAlive(Timeout.of(Duration.ofSeconds(5))) // Keep-alive for idle connections
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(50); // Max total connections
		connManager.setDefaultMaxPerRoute(10);

		try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsPro)
				.setConnectionManager(connManager).setDefaultRequestConfig(requestConfig)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, Timeout.ofSeconds(1))).build()) {
			final URIBuilder uriBuilder = new URIBuilder(pUri);
			uriBuilder.addParameter("system", pUrl);
			uriBuilder.addParameter("code", pCode);
			final URI uri = uriBuilder.build();
			final HttpGet request = new HttpGet(
					uri);
			try (@SuppressWarnings("deprecation")
			CloseableHttpResponse response = httpClient.execute(request)) {
				// Check if the response status is OK
				final int statusCode = response.getCode();
				if (statusCode == ACTIVE_STATUS) {
					try (InputStream inputStream = response.getEntity().getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

						StringBuilder responseContentBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							responseContentBuilder.append(line).append("\n");
						}
						responseContent = responseContentBuilder.toString();
						CACHE.put(cacheKey, responseContent);
					}
				}
			}
		} catch (final Exception e) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					((HttpURLConnection) new URL(pUrl).openConnection()).getErrorStream(), StandardCharsets.UTF_8))) {
				final StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				return response.toString();
			} catch (final Exception ex) {
				if (LOGGER.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOGGER.error(error);
				}
			}
		}
		return responseContent;
	}

	/**
	 * postSimpleRequest
	 * 
	 * @param pUri
	 * @param pUrl
	 * @param pCode
	 * @param pDisplay
	 * @return
	 */
	public static String postSimpleRequest(final String pUri, final String pUrl, final String pCode,
			final String pDisplay) {
		String responseContent = null;
		final String cacheKey = pCode;
		org.apache.hc.client5.http.config.RequestConfig requestConfig = org.apache.hc.client5.http.config.RequestConfig
				.custom().setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(30))) // Timeout for requesting
																							// connection from the pool
				.setResponseTimeout(Timeout.of(Duration.ofSeconds(60))) // Timeout for waiting for data
				.setConnectionKeepAlive(Timeout.of(Duration.ofSeconds(5))) // Keep-alive for idle connections
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(50); // Max total connections
		connManager.setDefaultMaxPerRoute(10);

		try (CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager)
				.setDefaultRequestConfig(requestConfig)
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, Timeout.ofSeconds(1))).build()) {
			final URIBuilder uriBuilder = new URIBuilder(pUri);
			uriBuilder.addParameter("url", pUrl);
			uriBuilder.addParameter("code", pCode);
			uriBuilder.addParameter("display", pDisplay);
			final URI uri = uriBuilder.build();
			final HttpGet request = new HttpGet(
					uri);
			try (@SuppressWarnings("deprecation")
			CloseableHttpResponse response = httpClient.execute(request)) {
				// Check if the response status is OK
				final int statusCode = response.getCode();
				if (statusCode == ACTIVE_STATUS) {
					try (InputStream inputStream = response.getEntity().getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

						StringBuilder responseContentBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							responseContentBuilder.append(line).append("\n");
						}
						responseContent = responseContentBuilder.toString();
						CACHE.put(cacheKey, responseContent);
					}
				}
			}
		} catch (final Exception e) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					((HttpURLConnection) new URL(pUrl).openConnection()).getErrorStream(), StandardCharsets.UTF_8))) {
				final StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				return response.toString();
			} catch (final Exception ex) {
				if (LOGGER.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOGGER.error(error);
				}
			}
		}

		return responseContent;
	}

	/**
	 * postGlobalRequest
	 * 
	 * @param pUrl
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postGlobalRequest(final String pUrl, final String pJsonContent, final String pRequestMethod) {
		final String requestMethod = (pRequestMethod == null) ? "POST" : pRequestMethod;
		if (IniFile.read("USE-TOKEN", "FHIR").equals("TRUE")) {
			String token = "";
			token = getToken(IniFile.read("URL-TOKEN-ONTOSERVER", "FHIR"), IniFile.read("LOGIN", "FHIR"),
					IniFile.read("PASSWORD", "FHIR"));
			if (token.equals("!!!{ERREUR}")) {
				return "!!!{Erreur dans la demande de Token}";
			}
			return postRequestWithToken(pUrl, token, pJsonContent, requestMethod);
		}
		if (IniFile.read("USE-TOKEN", "FHIR").equals("FALSE")
				&& IniFile.read("USE-CREDENTIALS", "FHIR").equals("TRUE")) {
			return postRequestWithCredentials(pUrl, IniFile.read("LOGIN", "FHIR"), IniFile.read("PASSWORD", "FHIR"),
					pJsonContent, requestMethod);

		}
		if (IniFile.read("USE-TOKEN", "FHIR").equals("FALSE")
				&& IniFile.read("USE-CREDENTIALS", "FHIR").equals("FALSE")) {
			return postSimpleRequest(pUrl, pJsonContent, requestMethod);
		}
		return "!!!{ERREUR}:No Operation";
	}

	/**
	 * postSimpleRequest
	 * 
	 * @param pUrl
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postSimpleRequest(final String pUrl, final String pJsonContent, final String pRequestMethod) {
		final String requestMethod = (pRequestMethod == null) ? "POST" : pRequestMethod;
		String responseContent = null;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpUriRequestBase request;
			switch (requestMethod.toUpperCase()) {
			case "PUT":
				request = new HttpPut(pUrl);
				break;
			case "GET":
				request = new HttpGet(pUrl);
				break;
			default:
				request = new HttpPost(pUrl);
				break;
			}
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod)) {
				StringEntity entity = new StringEntity(pJsonContent, ContentType.APPLICATION_JSON);
				if (request instanceof HttpPost) {
					((HttpPost) request).setEntity(entity);
				} else if (request instanceof HttpPut) {
					((HttpPut) request).setEntity(entity);
				}
			}
			try (@SuppressWarnings("deprecation")
			CloseableHttpResponse response = httpClient.execute(request)) {
				responseContent = EntityUtils.toString(response.getEntity());
				if (!responseContent.startsWith("{") && !responseContent.startsWith("[")) {
					return "Error: Server returned non-JSON content.";
				}
			}
		} catch (final Exception ex) {
			responseContent = "Error: " + ex.getMessage();
		}
		return responseContent;
	}

	/**
	 * postRequestWithCredentials
	 * 
	 * @param pUrl
	 * @param pUserName
	 * @param pPassword
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postRequestWithCredentials(final String pUrl, final String pUserName, final String pPassword,
			final String pJsonContent, final String pRequestMethod) {
		final String requestMethod = (pRequestMethod == null) ? "POST" : pRequestMethod;
		String responseContent = null;
		try {
			final URL url = new URL(pUrl);
			final HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod(requestMethod);
			request.setRequestProperty("Content-Type", "application/json");
			request.setRequestProperty("Accept", "application/json");

			final String svcCredentials = Base64.getEncoder()
					.encodeToString((pUserName + ":" + pPassword).getBytes("ASCII"));
			request.setRequestProperty("Authorization", "Basic " + svcCredentials);
			if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
				request.setDoOutput(true);
				try (OutputStream os = request.getOutputStream()) {
					final byte[] input = pJsonContent.getBytes("utf-8");
					os.write(input, 0, input.length);
				}
			}

			final int responseCode = request.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
					final StringBuilder response = new StringBuilder();
					String line;
					while ((line = in.readLine()) != null) {
						response.append(line.trim());
					}
					responseContent = response.toString();
				}
			}

		} catch (final Exception ex) {
			return "!!!{ERREUR}:" + ex.getMessage();
		}

		return responseContent;
	}

	/**
	 * postRequestWithToken
	 * 
	 * @param pUrl
	 * @param pToken
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postRequestWithToken(final String pUrl, final String pToken, final String pJsonContent,
			final String pRequestMethod) {
		final String requestMethod = (pRequestMethod == null) ? "POST" : pRequestMethod;
		String responseContent = null;
		try {
			final URL url = new URL(pUrl);
			final HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod(requestMethod);
			request.setRequestProperty("Content-Type", "application/json");
			request.setRequestProperty("Accept", "application/json");
			if (!pToken.equals("")) {
				request.setRequestProperty("Authorization", "Bearer " + pToken);
			}
			if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
				request.setDoOutput(true);
				try (OutputStream os = request.getOutputStream()) {
					final byte[] input = pJsonContent.getBytes("utf-8");
					os.write(input, 0, input.length);
				}
			}

			final int responseCode = request.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"))) {
					final StringBuilder response = new StringBuilder();
					String responseLine;
					while ((responseLine = in.readLine()) != null) {
						response.append(responseLine.trim());
					}
					responseContent = response.toString();
				}
			} else {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(request.getErrorStream(), "utf-8"))) {
					final StringBuilder errorResponse = new StringBuilder();
					String errorLine;
					while ((errorLine = in.readLine()) != null) {
						errorResponse.append(errorLine.trim());
					}
					responseContent = errorResponse.toString();
				}
			}
		} catch (final Exception e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
		return responseContent;
	}

	/**
	 * 
	 * @param pUrl
	 * @param pJsonContent
	 * @param pRequestMethod
	 * @return
	 */
	public static String postGlobalRequest(final String pUri, final String url, final String code,
			final String display) {
		if (IniFile.read("USE-TOKEN", Constant.FHIRMOD).equals(Constant.TRUEVAL)) {
			String token = "";
			token = getToken(IniFile.read("URL-TOKEN-ONTOSERVER", Constant.FHIRMOD),
					IniFile.read("LOGIN", Constant.FHIRMOD), IniFile.read("PASSWORD", Constant.FHIRMOD));
			if (token.equals("!!!{ERREUR}")) {
				return "!!!{Erreur dans la demande de Token}";
			}
			return postRequestWithToken(pUri, url, code, display, token);
		} else if (IniFile.read("USE-TOKEN", Constant.FHIRMOD).equals("FALSE")
				&& IniFile.read("USE-CREDENTIALS", Constant.FHIRMOD).equals(Constant.TRUEVAL)) {
			return postRequestWithCredentials(IniFile.read("LOGIN", Constant.FHIRMOD),
					IniFile.read("PASSWORD", Constant.FHIRMOD), pUri, url, code, display);
		} else if (IniFile.read("USE-TOKEN", Constant.FHIRMOD).equals("FALSE")
				&& IniFile.read("USE-CREDENTIALS", Constant.FHIRMOD).equals("FALSE")) {
			return postSimpleRequest(pUri, url, code, display);
		}
		return "!!!{ERREUR}:No Operation";
	}

	/**
	 * moveJdvFile
	 * 
	 * @param pJdvName
	 * @param pRepertoireSortieJson
	 * @param pDestinationDirectory
	 */
	public static void moveJdvFile(final String pJdvName, final String pRepertoireSortieJson,
			final String pDestinationDirectory) {
		final String sJdvFileName = new File(pJdvName).getName();
		final Path sourceFilePath = Paths.get(pRepertoireSortieJson, sJdvFileName);
		final Path destinationDirectory = Paths.get(pRepertoireSortieJson, pDestinationDirectory);
		final Path destinationFilePath = destinationDirectory.resolve(sJdvFileName);
		try {
			if (!Files.exists(destinationDirectory)) {
				Files.createDirectories(destinationDirectory);
			}
			if (sourceFilePath.toFile().exists()) {
				Files.move(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
			} else if (new File(sourceFilePath.toString() + ".json").exists()) {
				Files.move(new File(sourceFilePath.toString() + ".json").toPath(),
						new File(destinationFilePath.toString() + ".json").toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
	}

	/**
	 * 
	 * @param pCodeSystem
	 * @param pCode
	 * @param pDisplay
	 * @param pJDV
	 * @return
	 */
	public static String getOntoserverValidateCodeResult(final String pCodeSystem, final String pCode,
			final String pDisplay, final String pJDV) {
		final String response = validateCode(pCodeSystem, pCode, pDisplay);
		try {
			final String sLigneUrl = URLEncoder.encode(pCodeSystem, StandardCharsets.UTF_8.toString());
			final String decodedUrl = URLDecoder.decode(sLigneUrl, StandardCharsets.UTF_8.toString());
			final String results = getJsonPathValues(response, "$.parameter[?(@.name == 'result')].valueBoolean");
			if (results.toUpperCase(Locale.FRENCH).equals(Constant.TRUEVAL)) {
				return "(+)Le code " + pCode + " existe dans " + decodedUrl + " et le display est correct.";
			}
			final String details = getJsonPathValues(response,
					"$.parameter[?(@.name == 'issues')].resource.issue[?(@.severity == 'error')].details.coding[0].code");
			final String detailsTxt = getJsonPathValues(response,
					"$.parameter[?(@.name == 'issues')].resource.issue[?(@.severity == 'error')].details.text");

			final String displayCorrect = getJsonPathValues(response,
					"$.parameter[?(@.name == 'display')].valueString");
			final String unknownSystemDiagnostics = getJsonPathValue(response,
					"$.parameter[?(@.name == 'issues')].resource.issue[?(@.severity=='error' && @.code=='not-found')].details.text");
			final String errorCode = getJsonPathValue(response,
					"$.parameter[?(@.name == 'issues')].resource.issue[?(@.severity=='error')].code");
			if (details.contains("vs-invalid") || details.contains("not-found")
					|| unknownSystemDiagnostics.contains("A definition for CodeSystem")
							&& unknownSystemDiagnostics.contains("not be found")
					|| errorCode.contains("not-found")
							&& unknownSystemDiagnostics.contains("A definition for CodeSystem")) {
				final String valRet = "(-){UNKNOWN-SYSTEM}" + pCodeSystem +")";
				if (!new File(Constant.LOGFHIRFOLDER + "\\Log-unknown-system.log").exists()) {
					Files.createFile(new File(Constant.LOGFHIRFOLDER + "\\Log-unknown-system.log").toPath());
				}
				Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-unknown-system.log"), (valRet + "\n").getBytes(),
						StandardOpenOption.APPEND);
				return "(-){UNKNOWN-SYSTEM} Le system " + pCodeSystem + " est inconnu du serveur. (JDV: " + pJDV + ")";
			} else if (details.contains("invalid-display") || detailsTxt.contains("Wrong Display Name")) {
				final String valRet = "(-){INCORRECT-DISPLAY} Le code " + pCode + " existe dans " + decodedUrl
						+ " mais le display correct est: '" + displayCorrect + "' au lieu de '" + pDisplay + "' (JDV:"
						+ pJDV + ")";
				if (!new File(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.log").exists()) {
					Files.createFile(new File(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.log").toPath());
				}
				Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.log"),
						(valRet + "\n").getBytes(), StandardOpenOption.APPEND);
				final String sLine = decodedUrl + ";" + pCode + ";" + pDisplay + "; ==> ;" + displayCorrect + "\n";
				if (!new File(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.csv").exists()) {
					Files.createFile(new File(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.csv").toPath());
				}
				Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-Incorrect-Display.csv"),
						sLine.getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.APPEND);
				return valRet;
			} else if (details.contains("invalid-code")
					|| (detailsTxt.contains("Unknown code") && detailsTxt.contains("in the CodeSystem"))) {
				final String valRet = "(-){UNKNOWN-CODE} Le code " + pCode + " n'existe pas dans " + decodedUrl
						+ " ( JDV: " + pJDV + ")";
				if (!new File(Constant.LOGFHIRFOLDER + "\\Log-unknown-code.log").exists()) {
					Files.createFile(new File(Constant.LOGFHIRFOLDER + "\\Log-unknown-code.log").toPath());
				}
				Files.write(Paths.get(Constant.LOGFHIRFOLDER + "\\Log-unknown-code.log"), (valRet + "\n").getBytes(),
						StandardOpenOption.APPEND);
				return valRet;
			} else {
				return "(-){NO-INTERPRETATION} Aucune interprétation correcte du Json n'a été possible.\n\n" + response;
			}
		} catch (final Exception ex) {
			return "(-){UNKNOWN-ERROR}";
		}
	}

	/**
	 * 
	 * @param pCodeSystem
	 * @param pCode
	 * @param pDisplay
	 * @param pJDV
	 * @return
	 */
	public static String getOntoserverValidateCodeResults(final String pCodeSystem, final String pCode,
			final String pDisplay, final String pJDV) {
		final String response = validateCode(pCodeSystem, pCode, pDisplay);
		try {
			final String sLigneUrl = URLEncoder.encode(pCodeSystem, StandardCharsets.UTF_8.toString());
			final String decodedUrl = URLDecoder.decode(sLigneUrl, StandardCharsets.UTF_8.toString());
			final String results = getJsonPathValues(response, "$.parameter[?(@.name == 'result')].valueBoolean");
			if (results.toUpperCase(Locale.FRENCH).equals(Constant.TRUEVAL)) {
				return "(+)Le code " + pCode + " existe dans " + decodedUrl + " et le display est correct.";
			}

			final String details = getJsonPathValues(response,
					"$.parameter[?(@.name == 'issues')].resource.issue[0].details.coding[0].code");
			final String displayCorrect = getJsonPathValues(response,
					"$.parameter[?(@.name == 'display')].valueString");
			if (details.contains("invalid-display")) {
				final String valRet = "(-){INCORRECT-DISPLAY} Le code " + pCode + " existe dans " + decodedUrl
						+ " mais le display correct est: '" + displayCorrect + "' au lieu de '" + pDisplay + "' (JDV:"
						+ pJDV + ")";
				return valRet;
			}
			if (details.contains("invalid-code")) {
				final String valRet = "(-){UNKNOWN-CODE} Le code " + pCode + " n'existe pas dans " + decodedUrl
						+ " ( JDV: " + pJDV + ")";
				return valRet;
			}
			if (details.contains("vs-invalid")) {
				return "(-){UNKNOWN-SYSTEM} Le system " + pCodeSystem + " est inconnu du serveur. (JDV: " + pJDV + ")";
			}

			return "(-){NO-INTERPRETATION} Aucune interprétation correcte du Json n'a été possible.\n\n" + response;

		} catch (final Exception ex) {
			return "(-){UNKNOWN-ERROR}";
		}
	}

	/**
	 * log
	 * 
	 * @param pChapitre
	 * @param pLogContent
	 */
	public static void log(final String pChapitre, final String pLogContent) {
		final String sFileName = pChapitre + ".log";
		try (BufferedWriter fwriter = Files.newBufferedWriter(Paths.get(sFileName))) {
			fwriter.write("\n" + pLogContent);
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
	}

	/**
	 * loincLookUp
	 * 
	 * @param pCodeSystem
	 * @param pCode
	 * @param pUser
	 * @param pPassword
	 * @return
	 */
	public static String loincLookUp(final String pCodeSystem, final String pCode, final String pUser,
			final String pPassword) {
		String sValRet = "";
		final String loincBaseURL = "https://fhir.loinc.org/CodeSystem/$lookup";
		final String response = postRequestWithCredentialsLoinc(pUser, pPassword, loincBaseURL, pCodeSystem, pCode);
		if (response.contains("!!!{ERREUR}")) {
			sValRet += response;
		} else {
			try {
				final JSONObject o = new JSONObject(response);
				sValRet += o.toString();
			} catch (final Exception ex) {
				sValRet += "\n" + response;
			}
		}
		return sValRet;
	}

	/**
	 * getLoincLookUpResult
	 * 
	 * @param pCodeSystem
	 * @param pCode
	 * @param pUser
	 * @param pPassword
	 * @return
	 */
	public static boolean getLoincLookUpResult(final String pCodeSystem, final String pCode, final String pUser,
			final String pPassword) {
		boolean bool;
		final String response = loincLookUp(pCodeSystem, pCode, pUser, pPassword);
		if (response.contains("!!!{ERREUR}")) {
			bool = false;
		} else {
			bool = true;
		}
		return bool;

	}

	/**
	 * getJsonPathValue
	 * 
	 * @param jsonContent
	 * @param jsonPath
	 * @return
	 */
	public static String getJsonPathValue(final String jsonContent, final String jsonPath) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readTree(jsonContent);
			final Object result = JsonPath.read(rootNode.toString(), jsonPath);
			if (result instanceof String) {
				return (String) result;
			} else if (result instanceof List) {
				final List<?> listResult = (List<?>) result;
				if (!listResult.isEmpty()) {
					return listResult.get(0).toString();
				}
			}
			return "[NULL]";
		} catch (final Exception ex) {
			return "!!!{ERROR in getJsonPathValue()}:" + ex.getMessage();
		}
	}

	/**
	 * getJsonPathValues
	 * 
	 * @param pJsonContent
	 * @param pJsonPath
	 * @return
	 */
	public static String getJsonPathValues(final String pJsonContent, final String pJsonPath) {
		try {
			final ObjectMapper objectMapper = new ObjectMapper();
			final JsonNode jsonNode = objectMapper.readTree(pJsonContent);
			final List<Object> tokens = JsonPath.read(jsonNode.toString(), pJsonPath);
			if (tokens.isEmpty()) {
				return Constant.NULLVAL;
			} else {
				return tokens.get(0).toString();
			}
		} catch (final PathNotFoundException e) {
			return Constant.NULLVAL;
		} catch (final Exception e) {
			return "!!!{ERROR in Fhir.getJsonPathValue()}: " + e.getMessage();
		}
	}

	/**
	 * getValueSetFromJson
	 * 
	 * @param jsonContent
	 * @param pRapportErreurs
	 * @return
	 */
	public static String getValueSetFromJson(final String jsonContent, final StringBuilder pRapportErreurs) {
		final FhirContext ctx = FhirContext.forR4();
		final IParser parser = ctx.newJsonParser();
		try {
			final ValueSet parsedResource = parser.parseResource(ValueSet.class, jsonContent);
			pRapportErreurs.append("Le valueSet " + parsedResource.getName() + " est valide.");
		} catch (final Exception e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
		return pRapportErreurs.toString();
	}

	/**
	 * distinctCodeSys
	 */
	public static void distinctCodeSys(final List<String> jsonFiles) {
		distinctCodeSystemUsed = new ArrayList<>();
		final Set<String> distinctValues = new HashSet<>();
		final ObjectMapper mapper = new ObjectMapper();

		try {
			for (final String jsonFile : jsonFiles) {
				final JsonNode rootNode = mapper.readTree(new File(jsonFile));
				final JsonNode includeArray = rootNode.path("compose").path("include");
				if (includeArray.isArray()) {
					for (JsonNode includeNode : includeArray) {
						JsonNode systemNode = includeNode.path("system");
						if (!systemNode.isMissingNode()) {
							distinctValues.add(systemNode.asText());
						}
					}
				}
			}
			distinctCodeSystemUsed.addAll(distinctValues);
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
	}

	/**
	 * distinctCodeSys
	 */
	public static void distinctCode(final List<String> jsonFiles, final String path) {
		final Set<String> distinctValues = new HashSet<>();
		final ObjectMapper mapper = new ObjectMapper();
		try {
			for (final String jsonFile : jsonFiles) {
				final JsonNode rootNode = mapper.readTree(new File(jsonFile));
				final JsonNode includeArray = rootNode.path("compose").path("include");
				if (includeArray.isArray()) {
					for (JsonNode includeNode : includeArray) {
						JsonNode system = includeNode.path("system");
						String sConceptCodeSystem = null;
						if (!system.isMissingNode()) {
							String oid = IniFile.getOidForUri(path, system.asText());
							if (oid != null) {
								if (oid.contains("urn:oid:")) {
									oid = oid.replaceFirst("^urn:oid:", "");
								}
							}
							sConceptCodeSystem = oid;
						}
						final JsonNode conceptArray = includeNode.path("concept");

						// Check if "concept" is an array
						if (conceptArray.isArray()) {
							for (JsonNode conceptNode : conceptArray) {
								JsonNode codeNode = conceptNode.path("code");
								JsonNode sConceptDisplayName = conceptNode.path("display");
								if (!codeNode.isMissingNode()) {
									distinctValues.add(codeNode.asText() + " [" + sConceptCodeSystem + "]-{"
											+ sConceptDisplayName.asText() + "}");
								}
							}
						}
					}
				}
			}

			distinctCodeUsed.addAll(distinctValues);
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
	}

	/**
	 * getFhirValidationErrors
	 * 
	 * @param validationResponse
	 * @param detailed
	 * @return
	 */
	public static int getFhirValidationErrors(String pJsonReport, boolean pZeroTolerance) {
		int nbErrors = 0;
		if (pZeroTolerance) {
			List<String> diagnostics = getReportDiagnostics(pJsonReport);
			for (String diag : diagnostics) {
				String code = "";
				String codeSystem = "";
				if (getUnvalidCode(diag, code, codeSystem)) {
					if (!codeSystemExistsInMemory(codeSystem)) {
						nbErrors++;
					}
				}
			}
		}
		nbErrors += jsonCountElement(pJsonReport, "issue.[?(@.severity == 'error')].severity");
		return nbErrors;
	}

	/**
	 * jsonCountElement
	 * 
	 * @param pJsonContent
	 * @param pJsonPath
	 * @return
	 */
	public static int jsonCountElement(final String pJsonContent, final String pJsonPath) {
		try {
			int nbrErr = 0;
			final ObjectMapper objectMapper = new ObjectMapper();
			final JsonNode jsonNode = objectMapper.readTree(pJsonContent);
			final List<Object> tokens = JsonPath.read(jsonNode.toString(), pJsonPath);
			if (tokens.size() > 0) {
				net.minidev.json.JSONArray result = JsonPath.read(pJsonContent,
						"$.issue[?(@.severity=='error')].details.coding[?(@.code=='Validation_VAL_Profile_Unknown')].code");
				if (result == null || result.isEmpty()) {
					nbrErr++;
				}
			}
			return nbrErr;
		} catch (final Exception ex) {
			return -1;
		}
	}

	/**
	 * codeSystemExistsInMemory
	 * 
	 * @param pCodeSystem
	 * @param codeSystemsOntoserver
	 * @return
	 */
	public static boolean codeSystemExistsInMemory(final String pCodeSystem) {
		for (final String[] x : CODESYSTEMSONTOSERVER) {
			if (x[1].equals(pCodeSystem) || x[2].equals(pCodeSystem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * getUnvalidCode
	 * 
	 * @param pDiagnostic
	 * @param code
	 * @param codeSystem
	 * @return
	 */
	public static boolean getUnvalidCode(final String pDiagnostic, String code, String codeSystem) {
		if (pDiagnostic.contains("The code ") && pDiagnostic.contains(" is not valid in the system ")) {
			final int iDebutCode = pDiagnostic.indexOf("The code ") + 9;
			final int iFinCode = pDiagnostic.indexOf(" is not valid in the system ");
			final int iDebutCodeSystem = iFinCode + 28;
			code = pDiagnostic.substring(iDebutCode, iFinCode);
			codeSystem = pDiagnostic.substring(iDebutCodeSystem, pDiagnostic.length());
			return true;
		}
		return false;
	}

	/**
	 * getReportDiagnostics
	 * 
	 * @param pJsonReport
	 * @return
	 */
	public static List<String> getReportDiagnostics(final String pJsonReport) {
		List<String> retListe = new ArrayList<>();
		boolean bExit = false;
		int iRes = 0;
		try {
			final JSONObject o = new JSONObject(pJsonReport);
			do {
				try {
					final String diagnostic = o.optJSONArray("issue").optJSONObject(iRes).optString("diagnostics", "");

					if (diagnostic.equals("")) {
						bExit = true;
					} else {
						retListe.add(diagnostic);
					}

					iRes++;
				} catch (final Exception ex) {
					return retListe;
				}
			} while (!bExit);
		} catch (final Exception ex) {
			retListe = null;
			return retListe;
		}
		return retListe;
	}

	/**
	 * deleteFilesInDirectory
	 * 
	 * @param directory
	 */
	public static void deleteFilesInDirectory(final Path directory) {
		try {
			Files.list(directory).forEach(path -> {
				try {
					Files.delete(path);
				} catch (final IOException e) {
					if (LOGGER.isInfoEnabled()) {
						final String error = e.getMessage();
						LOGGER.error(error);
					}
				}
			});
		} catch (final IOException e) {
			if (LOGGER.isInfoEnabled()) {
				final String error = e.getMessage();
				LOGGER.error(error);
			}
		}
	}

	/**
	 * buildJdvFileName
	 * 
	 * @param pName
	 * @param pStatus
	 * @param pOid
	 * @return
	 */
	public static String buildJdvFileName(final String pName, final String pStatus, final String pOid) {
		return pName.trim() + "_" + pStatus + "_" + pOid;

	}
}