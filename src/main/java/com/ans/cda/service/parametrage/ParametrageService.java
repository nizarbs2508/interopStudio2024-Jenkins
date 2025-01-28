package com.ans.cda.service.parametrage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ans.cda.service.validation.SaxonValidator;
import com.ans.cda.service.validation.ValidationService;
import com.ans.cda.utilities.general.Constant;

/**
 * ParametrageService
 * 
 * @author bensalem Nizar
 */
public final class ParametrageService {
	/**
	 * myObj
	 */
	private static final File MYOBJ = new File(Constant.INTEROPFOLDER + "\\config.properties");
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ParametrageService.class);

	/**
	 * ParametrageService
	 */
	private ParametrageService() {
		// empty
	}

	/**
	 * 
	 * @param fieldXsl
	 * @param fieldCross  fieldSch
	 * @param fieldXdm    apiCda
	 * @param fieldAllXdm apiMeta
	 */
	public static boolean writeInFile(final String field, final String field1, final String fieldSch,
			final String fieldXsl, final String fieldCross, final String fieldXdm, final String fieldAllXdm,
			final String fieldSourceSch, final String fieldDestSch, final String fieldDestXsl, final String fieldUrl1,
			final String fieldUrl2, final String fieldUrl3, final String fieldFop, final String fieldUrl4, final String fieldUrl5, final String fieldUrl6) {
		boolean isOk = false;
		final Properties prop = new Properties();
		try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
			prop.load(configStream);
			configStream.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		try (OutputStream output = Files.newOutputStream(Paths.get(MYOBJ.getAbsolutePath()))) {
			// set the properties value
			prop.setProperty("url.last.result", field);
			prop.setProperty("last.result", field1);
			prop.setProperty("last.report", fieldSch);
			prop.setProperty("last.request", fieldXsl);
			prop.setProperty("last.report.html", fieldCross);
			prop.setProperty("folder.tovalidate", fieldXdm);
			prop.setProperty("folder.schematron", fieldAllXdm);
			prop.setProperty("folder.schematron.source", fieldSourceSch);
			prop.setProperty("folder.schematron.destination", fieldDestSch);
			prop.setProperty("folder.xsl.destination", fieldDestXsl);
			prop.setProperty("url.valueset", fieldUrl1);
			prop.setProperty("url.codesystem1", fieldUrl2);
			prop.setProperty("url.codesystem2", fieldUrl3);
			prop.setProperty("url.a11", fieldUrl4);
			prop.setProperty("url.x04", fieldUrl5);
			prop.setProperty("url.a04", fieldUrl6);
			prop.setProperty("folder.fop", fieldFop);
			// save properties to project root folder
			prop.store(output, null);
			isOk = true;
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				isOk = false;
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return isOk;
	}

	/**
	 * readValueInPropFile
	 * 
	 * @return
	 */
	public static String readValueInPropFile(final String key) {
		String value = null;
		final Properties prop = new Properties();
		try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
			prop.load(configStream);
			for (final String name : prop.stringPropertyNames()) {
				if (name.equals(key)) {
					value = prop.getProperty(name);
				}
			}
			configStream.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return value;
	}

	/**
	 * readOidInPropFile
	 */
	public static List<ParamEntity> readOidInPropFile() {
		final List<ParamEntity> listOid = new ArrayList<>();
		final Properties prop = new Properties();
		try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
			prop.load(configStream);
			for (final String name : prop.stringPropertyNames()) {
				if (Character.isDigit(name.charAt(0))) {
					final ParamEntity entity = new ParamEntity();
					entity.setOid(name);
					entity.setValue(prop.getProperty(name));
					listOid.add(entity);
				}
			}
			configStream.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return listOid;
	}

	/**
	 * removePro
	 * 
	 * @param key
	 */
	public static void removePro(final String key) {
		final Properties properties = new Properties();
		try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
			properties.load(configStream);
			configStream.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		try (OutputStream output = Files.newOutputStream(Paths.get(MYOBJ.getAbsolutePath()))) {
			properties.remove(key);
			properties.store(output, null);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

	}

	/**
	 * writeInPropFile
	 * 
	 * @param field
	 * @param field1
	 * @return
	 */
	public static boolean writeInPropFile(final String field, final String field1) {
		boolean isOk = false;
		final Properties prop = new Properties();
		try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
			prop.load(configStream);
			configStream.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		try (OutputStream output = Files.newOutputStream(Paths.get(MYOBJ.getAbsolutePath()))) {
			prop.setProperty(field, field1);
			prop.store(output, null);
			isOk = true;
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				isOk = false;
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return isOk;
	}

	/**
	 * init
	 * 
	 */
	public static void init() {
		if (!MYOBJ.exists()) {
			final Properties prop = new Properties();
			try {
				MYOBJ.createNewFile();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
			try (FileInputStream configStream = new FileInputStream(MYOBJ)) {
				prop.load(configStream);
				configStream.close();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
			try (OutputStream output = Files.newOutputStream(Paths.get(MYOBJ.getAbsolutePath()))) {
				prop.setProperty("1.2.250.1.213.1.1.1.40", "ANEST-CR-ANEST_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.41", "ANEST-CR-CPA_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.15", "AVC-AUNV_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.16", "AVC-EUNV_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.25", "AVC-PAVC_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.17", "AVC-SUNV_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.32", "CANCER-CR-GM_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.28", "CANCER-D2LM-FIDD_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.27", "CANCER-D2LM-FIN_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.8", "CANCER-FRCP-2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.26", "CANCER-PPS_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.5.3", "CSE-CS24_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.5.1", "CSE-CS8_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.5.2", "CSE-CS9_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.5.4", "CSE-MDE_2023.01");
				prop.setProperty("1.2.250.1.213.1.1.1.22", "DLU-EHPAD-DLU_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.24", "DLU-EHPAD-FLUDR_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.23", "DLU-EHPAD-FLUDT_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.21", "LDL-EES_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.29", "LDL-SES_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.12.1", "OBP-SAP_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.12.5", "OBP-SCE_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.12.4", "OBP-SCM_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.12.3", "OBP-SNE-2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.12.2", "OBP-SNM-2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.42", "OPH-BRE_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.18", "OPH-CR-RTN_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.30", "SDMMR_2022.01");
				prop.setProperty("1.3.6.1.4.1.19376.1.2.20", "TLM_CR_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.38", "TLM_DA_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.37", "VAC-2023.01");
				prop.setProperty("1.2.250.1.213.1.1.1.46", "VAC-NOTE_2023.01");
				prop.setProperty("1.2.250.1.213.1.1.1.13", "VSM_2013 (v1.4)");
				prop.setProperty("1.2.250.1.213.1.1.1.13", "VSM_2022.01 (v1.4 de 2013)");
				prop.setProperty("1.2.250.1.213.1.1.1.20", "ANS-PPS-PAERPA_2022.01");
				prop.setProperty("1.2.250.1.213.1.1.1.59", "ANS-PPS-BIO-ATTEST-DEPIST_COVID-19_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.59", "BIO-ATTEST-DEPIST_COVID-19_Grippe-A_Grippe-B_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.55", "BIO-CR-BIO-2021.01");
				prop.setProperty("1.2.250.1.213.1.1.1.55", "BIO-CR-BIO-2023.01");
				prop.setProperty("1.2.250.1.213.1.1.1.2.1.1", "CARD-F-PRC-AVK_2022");
				prop.setProperty("1.2.250.1.213.1.1.1.2.1.3", "CARD-F-PRC-DCI_2022");
				prop.setProperty("1.2.250.1.213.1.1.1.2.1.5", "CARD-F-PRC-PPV_2022");
				prop.setProperty("1.2.250.1.213.1.1.1.2.1.4", "CARD-F-PRC-PSC_2022");
				prop.setProperty("1.2.250.1.213.1.1.1.2.1.2", "CARD-F-PRC-TAP_2022");
				prop.setProperty("1.2.250.1.213.1.1.1.39", "EP-MED-DM_2023.01");
				prop.setProperty("1.2.250.1.213.1.1.1.45", "IMG-CR-IMG_2024.01");
				prop.setProperty("1.2.250.1.213.1.1.1.47", "IMG-DA-IMG_2024.01");
				prop.setProperty("url.last.result", SaxonValidator.getNewFilePath().toString());
				prop.setProperty("last.result", SaxonValidator.getNewFilePath1().toString());
				prop.setProperty("last.report", SaxonValidator.getNewFilePath2().toString());
				prop.setProperty("last.request", SaxonValidator.getNewFilePath3().toString());
				prop.setProperty("last.report.html", SaxonValidator.getNewFilePath4().toString());
				prop.setProperty("folder.tovalidate", ValidationService.getgPoolDirectory());
				prop.setProperty("folder.schematron", ValidationService.getgSchDirectory());
				prop.setProperty("folder.schematron.source", ValidationService.getgReportsDirectory());
				prop.setProperty("folder.schematron.destination", ValidationService.getgSvrlDirectory());
				prop.setProperty("folder.xsl.destination", ValidationService.getgSvrlDirectoryXsl());
				prop.setProperty("url.valueset", "https://smt.esante.gouv.fr/fhir/ValueSet?_summary=true");
				prop.setProperty("url.codesystem1", "https://smt.esante.gouv.fr/fhir/CodeSystem?_summary=true");
				prop.setProperty("url.codesystem2", "https://smt.esante.gouv.fr/fhir/CodeSystem/?url=");
				prop.setProperty("folder.fop", "");
				prop.setProperty("url.x04", "https://mos.esante.gouv.fr/NOS/ASS_X04-CorrespondanceType-Classe-CISIS/ASS_X04-CorrespondanceType-Classe-CISIS.xml");
				prop.setProperty("url.a04", "https://interop.esante.gouv.fr/ig/nos/CodeSystem-TRE-A04-Loinc.xml");
				prop.setProperty("url.a11", "https://mos.esante.gouv.fr/NOS/ASS_A11-CorresModeleCDA-XdsFormatCode-CISIS/ASS_A11-CorresModeleCDA-XdsFormatCode-CISIS.xml");
				
				// save properties to project root folder
				prop.store(output, null);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
	}
}