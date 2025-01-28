package com.ans.cda.service.xdm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ans.cda.service.crossvalidation.CrossValidationService;
import com.ans.cda.service.validation.ValidationService;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.ConvertPdfToPdfAB1;
import com.ans.cda.utilities.general.PdfUtility;
import com.ans.cda.utilities.general.Utility;

import javafx.scene.control.TextArea;

/**
 * IheXdmService
 * 
 * @author bensalem Nizar
 */
public final class IheXdmService {
	/**
	 * PASSED
	 */
	private static final String PASSED = "DONE_PASSED";
	/**
	 * Logger
	 */
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("MyLog");

	/**
	 * IheXdmService
	 */
	private IheXdmService() {
		// empty
	}

	/**
	 * generateIheXdmZip
	 * 
	 * @param listCda
	 * @param savePath
	 */
	public static void generateIheXdmZip(final List<String> listCda, final String savePath, final String value) {
		if (listCda != null && !listCda.isEmpty()) {
			final File file = new File(savePath + new File(listCda.get(0)).getName());
			try {
				if (file.exists()) {
					IheXdmUtilities.deleteDirectory(file);
				}
				final Path pathMeta = Paths.get(Constant.INTEROPFOLDER + "\\" + "nouveauDoc.xml");
				final Path path = IheXdmUtilities.cretaeFolder(
						savePath + "\\" + IheXdmUtilities.removeExtension(new File(listCda.get(0)).getName()));
				final File pdf = createPdf(new File(listCda.get(0)).getAbsolutePath(), value);
				Files.copy(
						pdf.toPath(), new File(path.toFile() + "\\"
								+ Utility.removeExtension(new File(listCda.get(0)).getName()) + ".pdf").toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				final Path contenuZip = IheXdmUtilities.cretaeFolder(path.toFile() + "\\Contenu du ZIP");
				final Path iheXdmFolder = IheXdmUtilities.cretaeFolder(contenuZip.toFile() + "\\IHE_XDM");
				final Path subset01Folder = IheXdmUtilities.cretaeFolder(iheXdmFolder.toFile() + "\\SUBSET01");
				IheXdmUtilities.copyFile(new File(listCda.get(0)), subset01Folder, "\\DOC0001.XML");
				IheXdmUtilities.copyFile(pathMeta.toFile(), subset01Folder, "\\METADATA.XML");
				final String sSuffix = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:suffix/string()");
				final String sNomAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:given/string()");
				final String sPrenomAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:family/string()");
				final String sOrganisme = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:name/string()");
				final String sOrganismeFiness = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:id/@extension/string()");
				final String sAdresseAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:addr/string-join((*:houseNumber,*:streetNameType,*:streetName,*:postalCode,*:city),' ')");
				final String sTelephoneAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
						"//*:ClinicalDocument/*:author/*:assignedAuthor/*:telecom/@value/string()");
				createReadmeFile(contenuZip.toFile().getAbsolutePath() + "\\README.TXT", sSuffix, sNomAuteur,
						sPrenomAuteur, sOrganisme, sOrganismeFiness, sAdresseAuteur, sTelephoneAuteur);
				createHtmlFile(contenuZip.toFile().getAbsolutePath() + "\\INDEX.HTM", sOrganisme, sOrganismeFiness);
				IheXdmUtilities.compressFolder(path, contenuZip);
			} catch (final IOException e) {
				final File file1 = new File(savePath);
				final File invalidCda = new File(file1.getParentFile() + "\\OTHER");
				final Path path = IheXdmUtilities.cretaeFolder(invalidCda.getAbsolutePath());
				final String pathFile = path + "\\" + new File(listCda.get(0)).getName();
				try {
					Files.copy(new File(listCda.get(0)).toPath(), new File(pathFile).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e1) {
					if (LOGGER.isLoggable(Level.SEVERE)) {
						LOGGER.log(Level.SEVERE, e1.getMessage());
					}
				}
			}
		}
	}

	/**
	 * createPdf
	 * 
	 * @param pathCda
	 */
	public static File createPdf(final String pathCda, final String path) {
		boolean isAutoPdf = false;
		final File file = new File(pathCda);
		final File fileXsl = new File(file.getAbsolutePath());
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder;
		try {
			dbuilder = dbf.newDocumentBuilder();
			final org.w3c.dom.Document doc = dbuilder.parse(fileXsl);
			doc.getDocumentElement().normalize();
			final NodeList nodeList = doc.getChildNodes();
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				final org.w3c.dom.Node node = nodeList.item(itr);
				if (Constant.STYLESHEET.equals(node.getNodeName())) {
					isAutoPdf = true;
				} else {
					isAutoPdf = false;
				}
			}
		} catch (final IOException | ParserConfigurationException | SAXException e) {
			final String error = e.getMessage();
			LOGGER.log(Level.SEVERE, error);
		}
		Path pdf = null;
		File pdfToOpen = null;
		try {
			pdf = Files.createTempFile(null, Constant.PDFEXT);
		} catch (final IOException e) {
			final String error = e.getMessage();
			LOGGER.log(Level.SEVERE, error);
		}
		final Path pdfFile = pdf;
		File fileCda = null;
		Path tempDirectory = null;
		try {
			final Path tempDir = Files.createTempDirectory("tempCDA");
			if (isAutoPdf) {
				fileCda = file;
			} else {
				fileCda = PdfUtility.copyFromJarFile(Constant.CDAFO, tempDir);
			}
			PdfUtility.copyFromJarFile(Constant.CDALN, tempDir);
			PdfUtility.copyFromJarFile(Constant.CDANARR, tempDir);
			tempDirectory = Paths.get(path);
		} catch (final IOException e) {
			final String error = e.getMessage();
			LOGGER.log(Level.SEVERE, error);
		}
		if (Files.exists(tempDirectory)) {
			String cmd = "cd " + '"' + tempDirectory.toString() + '"' + " && fop -xml " + '"' + pathCda + '"' + " -xsl "
					+ '"' + fileCda.getAbsolutePath() + '"' + " -pdf " + '"' + pdfFile.toString() + '"';
			cmd = cmd.replace("\\", "/");
			final String[] command = new String[3];
			command[0] = Constant.CMD;
			command[1] = Constant.C_SLASH;
			command[2] = cmd;
			final ProcessBuilder pbuilder = new ProcessBuilder();
			pbuilder.command(command);
			Process process;
			BufferedReader errStreamReader = null;
			try {
				process = pbuilder.start();
				errStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line = errStreamReader.readLine();
				while (line != null) {
					line = errStreamReader.readLine();
				}
				final String namePDF = new File(pathCda).getName();
				final File basedir = new File(System.getProperty("java.io.tmpdir"));
				if (new File(basedir, Utility.removeExtension(namePDF) + Constant.PDFEXT).exists()) {
					new File(basedir, Utility.removeExtension(namePDF) + Constant.PDFEXT).delete();
				}
				File pdfFinal = new File(basedir, Utility.removeExtension(namePDF) + Constant.PDFEXT);
				if (pdfFile != null) {
					int index = 1;
					while (pdfFinal.exists()) {
						final String path1 = pdfFinal.getAbsolutePath();
						final int idot = path1.lastIndexOf('.');
						final String path2 = path1.substring(0, idot) + "(" + ++index + ")" + path1.substring(idot);
						pdfFinal = Utility.getFile(path2);
					}
					ConvertPdfToPdfAB1.main(pdfFile.toString(), pdfFinal);
					pdfToOpen = new File(pdfFinal.toString());
				}
			} catch (final IOException e) {
				final String error = e.getMessage();
				LOGGER.log(Level.SEVERE, error);
			} finally {
				try {
					errStreamReader.close();
				} catch (IOException e) {
					final String error = e.getMessage();
					LOGGER.log(Level.SEVERE, error);
				}
			}
		}
		return pdfToOpen;
	}

	/**
	 * generateIheXdmZip
	 * 
	 * @param listCda
	 * @param savePath
	 */
	public static boolean generateAllIheXdmZip(final String cda, final String savePath, final String invalidPathMeta,
			final String invalidPathCross, final boolean boolM, final boolean boolC, final String urlNos,
			final String urlCode, final String urlCode1, final String urlA11, final String urlX04, final String urlA04,
			final String value) {
		boolean isOk = false;
		try {
			final List<String> listCda = new ArrayList<>();
			listCda.add(cda);
			String result = "";
			String console = "";
			final File fileModif = new File(
					XdmService.generateMeta(listCda, urlNos, urlCode, urlCode1, urlA11, urlX04, urlA04));
			final String content = new String(Files.readAllBytes(Paths.get(fileModif.toURI())), StandardCharsets.UTF_8);
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileModif.toURI()),
					StandardCharsets.UTF_8)) {
				writer.write(content);
				writer.close();
			}
			if (boolM) {
				result = ValidationService.validateMeta(fileModif, Constant.MODEL, Constant.ASIPXDM,
						Constant.URLVALIDATION);
			}
			if (boolC && result.contains(PASSED)) {
				console = CrossValidationService.crossValidate(new File(cda), fileModif, Constant.URLVALIDATION);
			}
			if (!result.isEmpty() && !result.contains(PASSED)) {
				final String pathFile = invalidPathMeta + "\\" + new File(cda).getName();
				try {
					Files.copy(new File(cda).toPath(), new File(pathFile).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e1) {
					final String error = e1.getMessage();
					LOGGER.log(Level.SEVERE, error);
				}
			}
			if (!console.isEmpty() && !console.contains("DONE_PASSED")) {
				final String pathFile = invalidPathCross + "\\" + new File(cda).getName();
				try {
					Files.copy(new File(cda).toPath(), new File(pathFile).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e1) {
					final String error = e1.getMessage();
					LOGGER.log(Level.SEVERE, error);
				}
			}
			if (result.contains(PASSED) && console.contains(PASSED)) {
				isOk = constructXdm(savePath, cda, value);
			} else if (!boolM && console.contains(PASSED)) {
				isOk = constructXdm(savePath, cda, value);
			} else if (!boolC && result.contains(PASSED)) {
				isOk = constructXdm(savePath, cda, value);
			} else if (!boolM && !boolC) {
				isOk = constructXdm(savePath, cda, value);
			}
		} catch (final IOException e) {
			final File file = new File(savePath);
			final File invalidCda = new File(file.getParentFile() + "\\OTHER");
			final Path path = IheXdmUtilities.cretaeFolder(invalidCda.getAbsolutePath());
			final String pathFile = path + "\\" + new File(cda).getName();
			try {
				Files.copy(new File(cda).toPath(), new File(pathFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
				isOk = false;
			} catch (final IOException e1) {
				final String error = e1.getMessage();
				LOGGER.log(Level.SEVERE, error);
			}
		}
		return isOk;
	}

	/**
	 * constructXdm
	 * 
	 * @param savePath
	 * @param cda
	 * @param isOk
	 */
	private static boolean constructXdm(final String savePath, final String cda, final String pathTemp) {
		try {
			final Path path = IheXdmUtilities
					.cretaeFolder(savePath + "\\" + IheXdmUtilities.removeExtension(new File(cda).getName()));
			final File file = new File(savePath + new File(cda).getName());
			if (file.exists()) {
				IheXdmUtilities.deleteFolder(file);
			}
			final File pdf = createPdf(new File(cda).getAbsolutePath(), pathTemp);
			Files.copy(pdf.toPath(),
					new File(path.toFile() + "\\" + Utility.removeExtension(new File(cda).getName()) + ".pdf").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			final Path contenuZip = IheXdmUtilities.cretaeFolder(path.toFile() + "\\Contenu du ZIP");
			final Path iheXdmFolder = IheXdmUtilities.cretaeFolder(contenuZip.toFile() + "\\IHE_XDM");
			final Path subset01Folder = IheXdmUtilities.cretaeFolder(iheXdmFolder.toFile() + "\\SUBSET01");
			IheXdmUtilities.copyFile(new File(cda), subset01Folder, "\\DOC0001.XML");
			final Path pathMeta = Paths.get(Constant.INTEROPFOLDER + "\\" + "nouveauDoc.xml");
			IheXdmUtilities.copyFile(pathMeta.toFile(), subset01Folder, "\\METADATA.XML");
			final String sSuffix = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:suffix/string()");
			final String sNomAuteur = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:given/string()");
			final String sPrenomAuteur = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:family/string()");
			final String sOrganisme = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:name/string()");
			final String sOrganismeFiness = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:id/@extension/string()");
			final String sAdresseAuteur = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:addr/string-join((*:houseNumber,*:streetNameType,*:streetName,*:postalCode,*:city),' ')");
			final String sTelephoneAuteur = IheXdmUtilities.getXpathSingleValue(new File(cda),
					"//*:ClinicalDocument/*:author/*:assignedAuthor/*:telecom/@value/string()");
			createReadmeFile(contenuZip.toFile().getAbsolutePath() + "\\README.TXT", sSuffix, sNomAuteur, sPrenomAuteur,
					sOrganisme, sOrganismeFiness, sAdresseAuteur, sTelephoneAuteur);
			createHtmlFile(contenuZip.toFile().getAbsolutePath() + "\\INDEX.HTM", sOrganisme, sOrganismeFiness);
			IheXdmUtilities.compressFolder(path, contenuZip);
		} catch (final IOException e) {
			final String error = e.getMessage();
			LOGGER.log(Level.SEVERE, error);
		}
		return true;
	}

	/**
	 * createReadmeFile
	 * 
	 * @param sOrganisme       sPrenomAuteur
	 * @param sOrganismeFiness sNomAuteur
	 * @param sAdresseAuteur   sSuffix
	 * @param sTelephoneAuteur filePath
	 */
	public static void createReadmeFile(final String filePath, final String sSuffix, final String sNomAuteur,
			final String sPrenomAuteur, final String sOrganisme, final String sOrganismeFiness,
			final String sAdresseAuteur, final String sTelephoneAuteur) {
		IheXdmUtilities.createFile(filePath);
		IheXdmUtilities.writeToReadmeFile(filePath, sSuffix, sNomAuteur, sPrenomAuteur, sOrganisme, sOrganismeFiness,
				sAdresseAuteur, sTelephoneAuteur);
	}

	/**
	 * createHtmlFile
	 * 
	 * @param filePath
	 * @param sOrganisme
	 * @param sOrganismeFiness
	 */
	public static void createHtmlFile(final String filePath, final String sOrganisme, final String sOrganismeFiness) {
		IheXdmUtilities.createFile(filePath);
		IheXdmUtilities.writeToHtmFile(filePath, sOrganisme, sOrganismeFiness);
	}

	/**
	 * verifErrorMeta
	 * 
	 * @param textAreaConsole
	 * @param area
	 */
	public static String verifErrorMeta(final TextArea textArea, final String textAreaConsole) {
		String display;
		final int nbErreurs = countMatches(textAreaConsole, "!!!");
		if (nbErreurs > 0) {
			display = "Ce métadata contient " + nbErreurs + " erreur(s)! \n";
			final String[] lines = textArea.getText().split("\n");
			for (Integer i = 0; i < lines.length; i++) {
				if (lines[i].contains("!!!")) {
					display = display + "Erreur au niveau de la ligne " + i + 1 + "\n";
				}
			}

		} else {
			display = "Aucune erreur lors de la génération.";
		}
		return display;
	}

	/**
	 * countMatches
	 * 
	 * @param text
	 * @param pattern
	 * @return
	 */
	private static int countMatches(final String text, final String pattern) {
		final Pattern pat = Pattern.compile(pattern);
		final Matcher matcher = pat.matcher(text);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}
}