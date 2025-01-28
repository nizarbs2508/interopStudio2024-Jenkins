package com.ans.cda.service.parametrage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.ans.cda.fhir.FhirUtilities;
import com.ans.cda.utilities.general.Constant;

/**
 * IniFile
 * 
 * @author bensalem Nizar
 */
public final class IniFile {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(IniFile.class);
	/**
	 * file
	 */
	private static final File FILE = new File(Constant.INTEROPFOLDER + "\\InteropStudio2024.ini");

	/**
	 * API
	 */
	private static final String API = "API-MAPPING";
	/**
	 * NOS
	 */
	private static final String NOS = "NOS";

	/**
	 * IniFile
	 */
	private IniFile() {
		// empty constructor
	}

	/**
	 * init
	 */
	public static void init() {
		try {
			final Ini ini = new Ini();
			String dir = System.getProperty("user.dir");
			dir = dir.replace("\\", "/");
			ini.put("MEMORY", "LAST-CDA-FILE", "");
			ini.put("MEMORY", "LAST-PATH-USED", dir);
			ini.put("MEMORY", "LAST-META-FILE", "");
			ini.put("LOINC", "FILE-NAME", "Loinc.csv");
			ini.put("DIAGNOSTIC", "LAST-REQUEST",
					"string-length(/ClinicalDocument/effectiveTime[1]/@value/string()) < 19");
			ini.put("API", "API-URL-PROD", "https://interop.esante.gouv.fr/evs/rest/validations");
			ini.put("API", "CDA-VALIDATION-SERVICE-NAME", "Schematron Based CDA Validator");
			ini.put("API", "METADATA-VALIDATION-SERVICE-NAME", "Model-based XDS Validator");

			ini.put("SCH-CLEANER", "LAST_DIRECTORY_SELECTED", dir);

			ini.put(API, "1.2.250.1.213.1.1.1.40", "ANEST-CR-ANEST_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.41", "ANEST-CR-CPA_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.15", "AVC-AUNV_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.16", "AVC-EUNV_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.25", "AVC-PAVC_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.17", "AVC-SUNV_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.32", "CANCER-CR-GM_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.28", "CANCER-D2LM-FIDD_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.27", "CANCER-D2LM-FIN_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.8", "CANCER-FRCP-2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.26", "CANCER-PPS_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.5.3", "CSE-CS24_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.5.1", "CSE-CS8_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.5.2", "CSE-CS9_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.5.4", "CSE-MDE_2023.01");
			ini.put(API, "1.2.250.1.213.1.1.1.22", "DLU-EHPAD-DLU_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.24", "DLU-EHPAD-FLUDR_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.23", "DLU-EHPAD-FLUDT_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.21", "LDL-EES_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.29", "LDL-SES_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.12.1", "OBP-SAP_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.12.5", "OBP-SCE_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.12.4", "OBP-SCM_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.12.3", "OBP-SNE-2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.12.2", "OBP-SNM-2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.42", "OPH-BRE_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.18", "OPH-CR-RTN_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.30", "SDMMR_2022.01");
			ini.put(API, "1.3.6.1.4.1.19376.1.2.20", "TLM_CR_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.38", "TLM_DA_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.37", "VAC-2023.01");
			ini.put(API, "1.2.250.1.213.1.1.1.46", "VAC-NOTE_2023.01");
			ini.put(API, "1.2.250.1.213.1.1.1.13", "VSM_2013 (v1.4)");
			ini.put(API, "1.2.250.1.213.1.1.1.13", "VSM_2022.01 (v1.4 de 2013)");
			ini.put(API, "1.2.250.1.213.1.1.1.20", "ANS-PPS-PAERPA_2022.01");
			ini.put(API, "1.2.250.1.213.1.1.1.59", "ANS-PPS-BIO-ATTEST-DEPIST_COVID-19_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.59", "BIO-ATTEST-DEPIST_COVID-19_Grippe-A_Grippe-B_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.55", "BIO-CR-BIO-2021.01");
			ini.put(API, "1.2.250.1.213.1.1.1.55", "BIO-CR-BIO-2023.01");
			ini.put(API, "1.2.250.1.213.1.1.1.2.1.1", "CARD-F-PRC-AVK_2022");
			ini.put(API, "1.2.250.1.213.1.1.1.2.1.3", "CARD-F-PRC-DCI_2022");
			ini.put(API, "1.2.250.1.213.1.1.1.2.1.5", "CARD-F-PRC-PPV_2022");
			ini.put(API, "1.2.250.1.213.1.1.1.2.1.4", "CARD-F-PRC-PSC_2022");
			ini.put(API, "1.2.250.1.213.1.1.1.2.1.2", "CARD-F-PRC-TAP_2022");
			ini.put(API, "1.2.250.1.213.1.1.1.39", "EP-MED-DM_2023.01");
			ini.put(API, "1.2.250.1.213.1.1.1.45", "IMG-CR-IMG_2024.01");
			ini.put(API, "1.2.250.1.213.1.1.1.47", "IMG-DA-IMG_2024.01");
			ini.put(API, "METADATA", "ASIP XDM ITI-32 FR Distribute Document Set on Media");

			ini.put(NOS, "ASS_A11",
					"https://mos.esante.gouv.fr/NOS/ASS_A11-CorresModeleCDA-XdsFormatCode-CISIS/ASS_A11-CorresModeleCDA-XdsFormatCode-CISIS.xml");
			ini.put(NOS, "ASS_X04",
					"https://mos.esante.gouv.fr/NOS/ASS_X04-CorrespondanceType-Classe-CISIS/ASS_X04-CorrespondanceType-Classe-CISIS.xml");
			ini.put(NOS, "JDV_J01",
					"https://mos.esante.gouv.fr/NOS/JDV_J01-XdsAuthorSpecialty-CISIS/JDV_J01-XdsAuthorSpecialty-CISIS.xml");
			ini.put(NOS, "JDV_J02",
					"https://mos.esante.gouv.fr/NOS/JDV_J02-XdsHealthcareFacilityTypeCode-CISIS/JDV_J02-XdsHealthcareFacilityTypeCode-CISIS.xml");
			ini.put(NOS, "JDV_J04",
					"https://mos.esante.gouv.fr/NOS/JDV_J04-XdsPracticeSettingCode-CISIS/JDV_J04-XdsPracticeSettingCode-CISIS.xml");
			ini.put(NOS, "JDV_J06",
					"https://mos.esante.gouv.fr/NOS/JDV_J06-XdsClassCode-CISIS/JDV_J06-XdsClassCode-CISIS.xml");
			ini.put(NOS, "JDV_J10",
					"https://mos.esante.gouv.fr/NOS/JDV_J10-XdsFormatCode-CISIS/JDV_J10-XdsFormatCode-CISIS.xml");
			ini.put(NOS, "TRE_A04", "https://mos.esante.gouv.fr/NOS/TRE_A04-Loinc/TRE_A04-Loinc.xml");
			ini.put(NOS, "TRE_A05",
					"https://mos.esante.gouv.fr/NOS/TRE_A05-TypeDocComplementaire/TRE_A05-TypeDocComplementaire.xml");
			ini.put(NOS, "TRE_A06",
					"https://mos.esante.gouv.fr/NOS/TRE_A06-FormatCodeComplementaire/TRE_A06-FormatCodeComplementaire.xml");

			ini.put("JOSHUA5", "TYPEWRITER-ACTIVATED", Constant.FALSEVAL);
			ini.put("JOSHUA5", "SILENCE", Constant.FALSEVAL);

			ini.put("MAINFORM", "URL-ANS-LOGO", "https://esante.gouv.fr/offres-services/ci-sis/espace-publication");

			ini.put("IHE_XDM", "DEFAULT_CDA_NAME", "DOC0001.XML");
			ini.put("IHE_XDM", "DEFAULT_METADATA_NAME", "METADATA.XML");

			ini.put(Constant.FHIRMOD, "URL-ONTOSERVER", "https://smt.esante.gouv.fr/fhir/");
			ini.put(Constant.FHIRMOD, "FHIR2SVS-CONVERTER", "Fhir2ArtDecor-SVS.xslt");
			ini.put(Constant.FHIRMOD, "SERVER-TYPE", "PROD");
			ini.put(Constant.FHIRMOD, "LOINC-USER", "");
			ini.put(Constant.FHIRMOD, "LOINC-PASSWORD", "");
			ini.put(Constant.FHIRMOD, "LOGIN", "");
			ini.put(Constant.FHIRMOD, "PASSWORD", "");
			ini.put(Constant.FHIRMOD, "RACINE-CANONIQUE", "https://smt.esante.gouv.fr/fhir/");
			ini.put(Constant.FHIRMOD, "USE-TOKEN", Constant.FALSEVAL);
			ini.put(Constant.FHIRMOD, "USE-CREDENTIALS", Constant.FALSEVAL);
			ini.put(Constant.FHIRMOD, "URL-TOKEN-ONTOSERVER",
					"https://smt.esante.gouv.fr/ans/sso/auth/realms/ANS/protocol/openid-connect/token");

			ini.put("PH-SCHEMATRON", "ROOT-DIRECTORY", "TOOLS\\ph-schematron");

			ini.put(Constant.SCHEMATRONS, "ENTRY-FILE-NAME", "schematron.sch");
			ini.put(Constant.SCHEMATRONS, "COMMON", "COMMON");
			ini.put(Constant.SCHEMATRONS, "VALUESETS", "VALUESETS");
			ini.put(Constant.SCHEMATRONS, "RULES", "RULES");
			ini.put(Constant.SCHEMATRONS, "PATH", "/opt/SchematronValidator_prod/bin/schematron/cda_asip/COURANT");

			final File iniFile = new File(FILE.getAbsolutePath());
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8)) {
				ini.store(writer);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * read
	 * 
	 * @param key
	 * @param section
	 * @return
	 */
	public static String read(final String key, final String section) {
		String value = null;
		try {
			if (FILE.exists()) {
				final Ini ini = new Ini(FILE);
				value = ini.get(section, key, String.class);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return value;
	}


	/**
	 * readTermino
	 * 
	 * @param key
	 * @param section
	 * @return
	 */
	public static void readTermino(String sSection, final File file, final Integer lineNumber) {
	    try {
	        if (file.exists()) {
	            final List<String> sMapping = new ArrayList<>();
	            final Ini ini = new Ini(file);
	            sMapping.add(ini.get(sSection, "NAME", String.class));
	            sMapping.add(ini.get(sSection, "OID", String.class));
	            sMapping.add(ini.get(sSection, "URI", String.class));
	            sMapping.add(ini.get(sSection, "CONTENT", String.class));
	            sMapping.add(sSection);
	            sMapping.add(lineNumber.toString());

	            String modifiedSection = sSection;
	            if (!sSection.startsWith(Constant.URNOID) && FhirUtilities.isNumericOid(sSection)) {
	                modifiedSection = Constant.URNOID + sSection;
	            }
	            
	            if (!ini.get(modifiedSection, "OID", String.class).startsWith(Constant.URNOID)
	                    && FhirUtilities.isNumericOid(sMapping.get(1))) {
	                sMapping.set(1, Constant.URNOID + sMapping.get(1));
	            }

	            if ("complete".equals(ini.get(modifiedSection, "CONTENT", String.class))) {
	                final String[] stringArray = sMapping.toArray(new String[5]);
	                FhirUtilities.CODESYSTEMSONTOSERVER.add(stringArray);
	            } else {
	                final String[] stringArray = sMapping.toArray(new String[5]);
	                FhirUtilities.CODESYSTEMSONTOSERVER.add(stringArray);
	                FhirUtilities.CODESYSTEMSONTOSERVERNOCONTENT.add(stringArray);
	            }
	        }
	    } catch (final IOException e) {
	        if (LOG.isInfoEnabled()) {
	            final String error = e.getMessage();
	            LOG.error(error);
	        }
	    }
	}


	/**
	 * read
	 * 
	 * @param section
	 * @return
	 */
	public static Section read(final String section) {
		Section value = null;
		try {
			if (FILE.exists()) {
				final Ini ini = new Ini(FILE);
				value = ini.get(section);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return value;
	}

	/**
	 * getOidForUri
	 * 
	 * @param iniFilePath
	 * @param targetURI
	 * @return
	 * @throws IOException
	 */
	public static String getOidForUri(final String iniFilePath, final String targetURI) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(iniFilePath));
		String line;
		final Map<String, String> currentSection = new HashMap<>();
		String oid = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("[") && line.endsWith("]")) {
				currentSection.clear(); // Reset for the new section
			} else if (line.contains("=")) {
				final String[] keyValue = line.split("=", 2);
				final String key = keyValue[0].trim();
				final String value = keyValue[1].trim();
				currentSection.put(key, value);
				if ("URI".equalsIgnoreCase(key) && targetURI.equals(value)) {
					oid = currentSection.get("OID");
					break;
				}
			}
		}

		reader.close();
		return oid;
	}

	/**
	 * write
	 * 
	 * @param key
	 * @param value
	 * @param section
	 */
	public static boolean write(final String key, final String value, final String section) {
		boolean bool = true;
		if (FILE.exists()) {
			try {
				final Ini ini = new Ini(FILE);
				final Ini.Section sec = ini.get(section);
				ini.put(section, key, value);
				ini.store();
				if (sec == null) {
					bool = true;
				} else {
					bool = false;
				}
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return bool;
	}

	/**
	 * writeTermino
	 * 
	 * @param key
	 * @param value
	 * @param section
	 * @return
	 */
	public static boolean writeTermino(final String key, final String value, final String section, final File file) {
		boolean bool = true;
		if (file.exists()) {
			try {
				final Ini ini = new Ini(file);
				final Ini.Section sec = ini.get(section);
				ini.put(section, key, value);
				ini.store();
				if (sec == null) {
					bool = true;
				} else {
					bool = false;
				}
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return bool;
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

	/**
	 * readFileContents
	 * 
	 * @param selectedFile
	 * @throws IOException
	 */
	public static int readSectionFile(final String file) throws IOException {
		int count = 0;
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (line != null && line.startsWith("[")) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * removeSectionFromIniFile
	 * 
	 * @param iniFilePath
	 * @param section
	 * @throws IOException
	 */
	public static void removeSectionFromIniFile(final String iniFilePath, final String section) throws IOException {
		final File iniFile = new File(iniFilePath);
		final List<String> lines = Files.readAllLines(iniFile.toPath());
		final List<String> updatedLines = new ArrayList<>();
		boolean inSectionToRemove = false;
		for (final String line : lines) {
			if (line.matches("\\[.*\\]")) {
				inSectionToRemove = line.equals("[" + section + "]");
			}
			if (!inSectionToRemove) {
				updatedLines.add(line);
			}
		}
		Files.write(iniFile.toPath(), updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * updateIniFile
	 * 
	 * @param file @param sectionName
	 * @param name @param url
	 * @param oid  @param content
	 */
	public static void updateIniFile(final String file, final String sectionName, final String name, final String url,
			final String oid, final String content) {
		try {
			final Ini iniFile = new Ini(new File(file));
			iniFile.put(sectionName, "NAME", name);
			iniFile.put(sectionName, "URI", url);
			iniFile.put(sectionName, "OID", oid);
			iniFile.put(sectionName, "CONTENT", content);
			iniFile.store();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
	}
}