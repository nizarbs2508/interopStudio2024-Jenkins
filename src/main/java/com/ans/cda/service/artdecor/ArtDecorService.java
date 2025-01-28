package com.ans.cda.service.artdecor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ans.cda.service.parametrage.IniFile;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.LocalUtility;
import com.ans.cda.utilities.general.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ArtDecorService
 * 
 * @author bensalem Nizar
 */
public class ArtDecorService {

	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ArtDecorService.class);
	/**
	 * statusCode
	 */
	private static final String STATUS = "statusCode";

	/**
	 * ArtDecorService
	 */
	private ArtDecorService() {
		// empty constructor
	}

	/**
	 * transform
	 * 
	 * @param xmlFile
	 * @return
	 */
	public static File transform(final String xmlFile) {
		final File selectedFile = new File(xmlFile);
		final String sGeneratedFN = selectedFile.getParent() + "/"
				+ selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.')) + "_Nettoyé.xml";
		final TransformerFactory transformer = TransformerFactory.newInstance();
		// attribut properties to make javafx working in heigher jre than 1.8
		System.setProperty(Constant.GRP, "-1");
		System.setProperty(Constant.EXPR_OP_LIMIT, "-1");
		System.setProperty(Constant.TOT_OP_LIMIT, "-1");
		Transformer xform;
		final ClassLoader classloader = Utility.newClassLoader();
		try (InputStream iStream = classloader.getResourceAsStream(Constant.CLEANER)) {
			xform = transformer.newTransformer(new StreamSource(iStream));
			xform.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xform.setOutputProperty(OutputKeys.INDENT, "yes");
			xform.setOutputProperty(OutputKeys.METHOD, "xml");
			xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			xform.transform(new StreamSource(new File(xmlFile)), new StreamResult(new File(sGeneratedFN)));
		} catch (final TransformerException | IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
		return new File(sGeneratedFN);
	}

	/**
	 * convertXmlToJson
	 * 
	 * @param xmlFile
	 */
	public static String convertXmlToJson(final String xmlFile) {
		String sGeneratedFN;
		String str = "";
		final File file = new File(new File(xmlFile).getAbsolutePath());
		final File selectedFile = new File(xmlFile);
		sGeneratedFN = selectedFile.getParent() + "/"
				+ selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.')) + ".json";
		try (InputStream targetStream = Files.newInputStream(Paths.get(file.toURI()));
				BufferedWriter fileW = Files.newBufferedWriter(Paths.get(sGeneratedFN))) {
			final String fileContent = IniFile.readFileContents(targetStream);
			final JSONObject json = XML.toJSONObject(fileContent);
			final String jsonString = json.toString(4);
			fileW.write(jsonString);
			fileW.close();
			str = readJsonFile(new File(sGeneratedFN));
		} catch (final JSONException | IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		} finally {
			new File(sGeneratedFN).delete();
		}
		return str;
	}

	/**
	 * readJsonFile
	 * 
	 * @param jsonFile
	 */
	public static String readJsonFile(final File jsonFile) {
		final List<String> map = new ArrayList<>();
		final List<String> mapRules = new ArrayList<>();
		final List<String> mapRulesSec = new ArrayList<>();
		final List<String> mapRulesOther = new ArrayList<>();
		final StringBuffer strFinal = new StringBuffer();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode root = mapper.readTree(new File(jsonFile.getAbsolutePath()));
			final JsonNode project = root.get("decor");
			if (project != null) {
				for (final JsonNode objNode : project) {
					final JsonNode terminology = objNode.get("valueSet");
					if (terminology != null) {
						for (final JsonNode objNode3 : terminology) {
							final JsonNode statusCode = objNode3.get(STATUS);
							if (statusCode != null) {
								map.add(statusCode.toString());

							}
						}
					}
					final JsonNode rules = objNode.get("template");
					if (rules != null) {
						for (final JsonNode objNode3 : rules) {
							final JsonNode classification = objNode3.get("classification");
							if (classification != null) {
								final JsonNode type = classification.get("type");
								if (type != null) {
									final String result = type.toString().substring(1, type.toString().length() - 1);
									if ("cdaentrylevel".equals(result)) {
										final JsonNode statusCode = objNode3.get(STATUS);
										if (statusCode != null) {
											mapRules.add(statusCode.toString());

										}
									} else if ("cdasectionlevel".equals(result)) {
										final JsonNode statusCode = objNode3.get(STATUS);
										if (statusCode != null) {
											mapRulesSec.add(statusCode.toString());

										}
									} else {
										final JsonNode statusCode = objNode3.get(STATUS);
										if (statusCode != null) {
											mapRulesOther.add(statusCode.toString());

										}
									}
								} else {
									final JsonNode statusCode = objNode3.get(STATUS);
									if (statusCode != null) {
										mapRulesOther.add(statusCode.toString());
									}
								}
							} else {
								final JsonNode statusCode = objNode3.get(STATUS);
								if (statusCode != null) {
									mapRulesOther.add(statusCode.toString());
								}
							}
						}
					}
				}
			}
			strFinal.append(countFrequencies(map)).append('\n').append(countFrequenciesRules(mapRules)).append('\n')
					.append(countFrequenciesRulesSec(mapRulesSec)).append('\n')
					.append(countFrequenciesRulesOther(mapRulesOther));
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
		return strFinal.toString();
	}

	/**
	 * countFrequencies
	 * 
	 * @param list
	 */
	private static String countFrequencies(final List<String> list) {
		final StringBuilder str = new StringBuilder();
		final Map<String, Integer> hmap = new ConcurrentHashMap<>();
		for (final String iCount : list) {
			final Integer jCount = hmap.get(iCount);
			hmap.put(iCount, (jCount == null) ? 1 : jCount + 1);
		}
		for (final Map.Entry<String, Integer> val : hmap.entrySet()) {
			str.append(LocalUtility.getString("message.status.jdv")).append(val.getKey()).append(val.getValue())
					.append('\n');
		}
		return str.toString();
	}

	/**
	 * countFrequencies
	 * 
	 * @param list
	 */
	private static String countFrequenciesRules(final List<String> list) {
		final StringBuilder str = new StringBuilder();
		final Map<String, Integer> hmap = new ConcurrentHashMap<>();
		for (final String iCount : list) {
			final Integer jCount = hmap.get(iCount);
			hmap.put(iCount, (jCount == null) ? 1 : jCount + 1);
		}
		for (final Map.Entry<String, Integer> val : hmap.entrySet()) {
			str.append(LocalUtility.getString("message.status.entree")).append(val.getKey()).append(val.getValue())
					.append('\n');
		}
		return str.toString();
	}

	/**
	 * countFrequencies
	 * 
	 * @param list
	 */
	private static String countFrequenciesRulesSec(final List<String> list) {
		final StringBuilder str = new StringBuilder();
		final Map<String, Integer> hmap = new ConcurrentHashMap<>();
		for (final String iCount : list) {
			final Integer jCount = hmap.get(iCount);
			hmap.put(iCount, (jCount == null) ? 1 : jCount + 1);
		}
		for (final Map.Entry<String, Integer> val : hmap.entrySet()) {
			str.append(LocalUtility.getString("message.status.section")).append(val.getKey()).append(val.getValue())
					.append('\n');
		}
		return str.toString();
	}

	/**
	 * countFrequencies
	 * 
	 * @param list
	 */
	private static String countFrequenciesRulesOther(final List<String> list) {
		final StringBuilder str = new StringBuilder();
		final Map<String, Integer> hmap = new ConcurrentHashMap<>();
		for (final String iCount : list) {
			final Integer jCount = hmap.get(iCount);
			hmap.put(iCount, (jCount == null) ? 1 : jCount + 1);
		}
		for (final Map.Entry<String, Integer> val : hmap.entrySet()) {
			str.append(LocalUtility.getString("message.status.other")).append(val.getKey()).append(val.getValue())
					.append('\n');
		}
		return str.toString();
	}
}