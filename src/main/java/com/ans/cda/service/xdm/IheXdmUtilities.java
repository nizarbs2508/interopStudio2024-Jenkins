package com.ans.cda.service.xdm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.Utility;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * IheXdmUtilities
 * 
 * @author bensalem Nizar
 */
public final class IheXdmUtilities {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(IheXdmUtilities.class);

	/**
	 * IheXdmUtilities
	 */
	private IheXdmUtilities() {
		// empty
	}

	/**
	 * writeToFile
	 * 
	 * @param file
	 */
	public static void writeToReadmeFile(final String file, final String sSuffix, final String sNomAuteur,
			final String sPrenomAuteur, final String sOrganisme, final String sOrganismeFiness,
			final String sAdresseAuteur, final String sTelephoneAuteur) {
		try (BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(file))) {
			myWriter.write(Constant.EMETTEUR);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.NAME + sSuffix + " " + sPrenomAuteur + " " + sNomAuteur + "\n");
			myWriter.write(Constant.ORGANISME + sOrganisme + " (" + sOrganismeFiness + ") " + "\n");
			myWriter.write(Constant.ADRESS + sAdresseAuteur + "\n");
			myWriter.write(Constant.TEL + sTelephoneAuteur + "\n\n");
			myWriter.write(Constant.APPEMETT);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.ADK);
			myWriter.write(Constant.VERSION);
			myWriter.write(Constant.EDITEUR);
			myWriter.write(Constant.INSTRUCTION);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.MESSAGERIE);
			myWriter.write(Constant.ARBO);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.README);
			myWriter.write(Constant.INDEX);
			myWriter.write(Constant.IHE);
			myWriter.write(Constant.SUBSET);
			myWriter.write(Constant.METADATA);
			myWriter.write(Constant.DOC);
			myWriter.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * writeToHtmFile
	 * 
	 * @param file
	 * @param sOrganisme
	 * @param sOrganismeFiness
	 */
	public static void writeToHtmFile(final String file, final String sOrganisme, final String sOrganismeFiness) {
		try (BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(file))) {
			myWriter.write(Constant.HEADER);
			myWriter.write(Constant.DOCTYPE);
			myWriter.write(Constant.HTML);
			myWriter.write(Constant.EMT + sOrganisme + " (" + sOrganismeFiness + ")" + "\n");
			myWriter.write(Constant.HREF);
			myWriter.write(Constant.HTM);
			myWriter.close();
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
	 * deleteDirectory
	 * 
	 * @param file
	 */
	public static void deleteDirectory(final File file) {
		for (final File subfile : file.listFiles()) {
			if (subfile.isDirectory()) {
				deleteDirectory(subfile);
			}
			subfile.delete();
		}
	}

	/**
	 * deleteFolder
	 * 
	 * @param file
	 */
	public static void deleteFolder(final File file) {
		file.delete();
	}

	/**
	 * copyFile
	 * 
	 * @param original
	 * @param copied
	 */
	public static void copyFile(final File original, final Path copied, final String suffix) {
		final String copiedFileStr = copied + suffix;
		final File copiedFile = new File(copiedFileStr);
		final Path originalPath = original.toPath();
		try {
			Files.copy(originalPath, copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * cretaeFolder
	 */
	public static Path cretaeFolder(final String dir) {
		Path path = null;
		try {
			path = Paths.get(dir);
			Files.createDirectories(path);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return path;
	}

	/**
	 * removeExtension
	 * 
	 * @param s
	 * @return
	 */
	public static String removeExtension(final String str) {
		return str != null && str.lastIndexOf(".") > 0 ? str.substring(0, str.lastIndexOf(".")) : str;
	}

	/**
	 * CreateFile
	 * 
	 * @param fileName
	 */
	public static void createFile(final String fileName) {
		try {
			final File myObj = new File(fileName);
			myObj.createNewFile();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * compressFolder
	 * 
	 * @param path
	 * @param contenuZip
	 */
	public static void compressFolder(final Path path, final Path contenuZip) {
		final String strPath = contenuZip.toString() + "\\*";
		try {
			final String res = Utility.getContextClassLoader().getResource(Constant.APIPATH).toExternalForm();
			File dest;
			dest = new File(Constant.INTEROPFOLDER + "\\7z.exe");
			if (!dest.exists()) {
				FileUtils.copyURLToFile(new URL(res), dest);
			}
			final String resdll = Utility.getContextClassLoader().getResource(Constant.ZIPPATH).toExternalForm();
			File destdll;
			destdll = new File(Constant.INTEROPFOLDER + "\\7z.dll");
			if (!destdll.exists()) {
				FileUtils.copyURLToFile(new URL(resdll), destdll);
			}
			final String finalPath = path.toString() + Constant.IHEXDM;
			final String[] params = { Constant.CMD, Constant.SLASHC,
					"7z a " + "\"" + finalPath + "\"" + " " + "\"" + strPath + "\"" };
			final ProcessBuilder builder = new ProcessBuilder();
			builder.directory(new File(Constant.INTEROPFOLDER));
			builder.command(params);
			builder.redirectErrorStream(true);
			final Process process = builder.start();
			final InputStream istream = process.getInputStream();
			new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream));) {
					@SuppressWarnings("unused")
					String line = null;
					while ((line = reader.readLine()) != null) {
						// TODO: handle line
					}
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			}).start();
			@SuppressWarnings("unused")
			Thread waitForThread = new Thread(() -> {
				try {
					process.waitFor();
				} catch (final InterruptedException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			});
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}
}
