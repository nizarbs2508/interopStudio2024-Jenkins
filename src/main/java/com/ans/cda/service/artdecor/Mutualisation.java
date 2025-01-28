package com.ans.cda.service.artdecor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.ans.cda.service.parametrage.IniFile;

/**
 * Mutualisation
 * 
 * @author Nizar BS
 */
public class Mutualisation {
	/**
	 * sContenuFichier
	 */
	private static String sContenuFichier;
	/**
	 * bool
	 */
	private static boolean bool;
	/**
	 * SCHEMATRONS
	 */
	private static final String SCHEMATRONS = "SCHEMATRONS";
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(Mutualisation.class);
	/**
	 * Mutualisation
	 */
	private Mutualisation() {
		//empty constructor
	}

	/**
	 * mutualisation
	 * 
	 * @param pEntryFileName
	 * @param pNewFileName
	 * @throws IOException
	 */
	public static boolean mutualisation(final String pEntryFileName, final String pNewFileName) throws IOException {
		final String fileSelect = pEntryFileName;
		final String repSelect = Paths.get(fileSelect).getParent().toString();
		final String newNameFile = repSelect + "\\" + pNewFileName;
		final String common = IniFile.read("COMMON", SCHEMATRONS);
		final String valueSet = IniFile.read("VALUESETS", SCHEMATRONS);
		final String rules = IniFile.read("RULES", SCHEMATRONS);
		final String path = IniFile.read("PATH", SCHEMATRONS);
		final String repJdv = Paths.get(repSelect).getParent().toString() + "\\" + common + "\\" + valueSet + "\\";
		IniFile.write("LAST-PATH-USED", repSelect, "MEMORY");
		sContenuFichier = new String(Files.readAllBytes(Paths.get(fileSelect)), StandardCharsets.UTF_8);
		sContenuFichier = sContenuFichier.replace("href=\"include/DTr1",
				"href=\"" + path + "/" + common + "/" + rules + "/DTr1");
		Files.write(Paths.get(fileSelect), sContenuFichier.getBytes(StandardCharsets.UTF_8));
		if (!Files.exists(Paths.get(repJdv))) {
			Files.createDirectories(Paths.get(repJdv));
		}
		try (Stream<Path> paths = Files.walk(Paths.get(repSelect + "\\include"))) {
			paths.filter(Files::isRegularFile).filter(
					f -> f.getFileName().toString().startsWith("DTr1") && f.getFileName().toString().endsWith(".sch"))
					.forEach(f -> {
						try {
							Files.delete(f);
							bool = true;
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
								bool = false;
							}
						}
					});
		}
		try (Stream<Path> paths = Files.walk(Paths.get(repSelect + "\\include"))) {
			paths.filter(Files::isRegularFile).filter(
					f -> f.getFileName().toString().startsWith("voc-") && f.getFileName().toString().endsWith(".xml"))
					.forEach(f -> {
						try {
							final String sNomFichierSeul = f.getFileName().toString();
							final String sVoc = repJdv + sNomFichierSeul;
							if (Files.exists(Paths.get(sVoc))) {
								Files.delete(Paths.get(sVoc));
							}
							Files.move(f, Paths.get(sVoc), StandardCopyOption.REPLACE_EXISTING);
							bool = true;
						} catch (final IOException e) {
							if (LOG.isInfoEnabled()) {
								final String error = e.getMessage();
								LOG.error(error);
								bool = false;
							}
						}
					});
		}
		try (Stream<Path> paths = Files.walk(Paths.get(repSelect + "\\include"))) {
			paths.filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(".sch")).forEach(f -> {
				try {
					sContenuFichier = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
					final String sNouvelleChaine = "doc('" + path + "/" + common + "/" + valueSet + "/voc-";
					sContenuFichier = sContenuFichier.replace("doc('include/voc-", sNouvelleChaine);
					Files.write(f, sContenuFichier.getBytes(StandardCharsets.UTF_8));
					bool = true;
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
						bool = false;
					}
				}
			});
		}
		final String nomFichierSch = Paths.get(fileSelect).getFileName().toString();
		try (Stream<Path> paths = Files.walk(Paths.get(repSelect))) {
			paths.filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(".xml")).forEach(f -> {
				try {
					sContenuFichier = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
					sContenuFichier = sContenuFichier.replace(nomFichierSch, pNewFileName);
					Files.write(f, sContenuFichier.getBytes(StandardCharsets.UTF_8));
					bool = true;
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
						bool = false;
					}
				}
			});
		}
		if (!Files.exists(Paths.get(newNameFile))) {
			Files.move(Paths.get(fileSelect), Paths.get(newNameFile), StandardCopyOption.REPLACE_EXISTING);
		}
		return bool;
	}
}