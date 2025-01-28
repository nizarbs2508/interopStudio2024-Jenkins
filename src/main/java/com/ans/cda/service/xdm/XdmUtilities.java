package com.ans.cda.service.xdm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ans.cda.utilities.general.Constant;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * XdmUtilities
 * 
 * @author bensalem Nizar
 */
public final class XdmUtilities {
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XdmUtilities.class);

	/**
	 * XdmUtilities
	 */
	private XdmUtilities() {
		// empty
	}

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(final String zipFilePath, final String destDirectory) throws IOException {
		final File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		final ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)));
		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			final String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				extractFile(zipIn, filePath);
			} else {
				final File dir = new File(filePath);
				dir.mkdirs();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * downloadFileUsingNIO
	 * 
	 * @param urlStr
	 * @param file
	 * @throws IOException
	 */
	public static void downloadFileUsingNIO(final String urlStr, final String file) throws IOException {
		final URL url = new URL(urlStr);
		FileUtils.copyURLToFile(url, new File(file));
	}

	/**
	 * readJsonFile
	 * 
	 * @param file
	 */
	public static List<File> readJsonFile(final String file, final String urlCode1) {
		final JSONParser jsonP = new JSONParser();
		final List<File> listFile = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(new File(file).toURI()))) {
			final Object obj = jsonP.parse(reader);
			final org.json.simple.JSONObject jObject = (org.json.simple.JSONObject) obj;
			final JSONArray jArray = (JSONArray) jObject.get("entry");
			final List<File> lfiles = new ArrayList<>();
			if (jArray != null) {
				for (final Object jObj : jArray) {
					final org.json.simple.JSONObject object = (org.json.simple.JSONObject) jObj;
					final String fullUrl = (String) object.get("fullUrl");
					if (fullUrl.contains("JDV-J06-") || fullUrl.contains("JDV-J10-") || fullUrl.contains("TRE-A05-")) {
						final org.json.simple.JSONObject resources = (org.json.simple.JSONObject) object
								.get("resource");
						final String name = (String) resources.get("name");
						final File file2 = new File(
								Constant.FILENAME + Constant.JSONFOLDER + name.concat(Constant.EXTENSIONJSON));
						downloadFileUsingNIO(fullUrl, file2.getAbsolutePath());
						if (file2.getName().startsWith(Constant.JDVFIRST)) {
							lfiles.add(file2);
						}
						if (file2.getName().startsWith(Constant.TREFIRST)) {
							lfiles.add(file2);
						}
					}
				}
			}
			if (!lfiles.isEmpty()) {
				for (final File file3 : lfiles) {
					final String str = ConvertJsonToXML.convert(file3.getAbsolutePath(), Constant.FILENAME, urlCode1);
					final String json = xmlToJson(str, file3.getParentFile());
					listFile.add(new File(json));
				}
			}
		} catch (final IOException | ParseException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return listFile;
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
	 * xmlToJson
	 * 
	 * @param xml
	 * @return
	 */
	public static String xmlToJson(final String xml, final File file) {
		String line;
		String str = "";
		BufferedReader breader;
		try {
			breader = new BufferedReader(Files.newBufferedReader(Paths.get(xml)));
			while ((line = breader.readLine()) != null) {
				str += line;
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		final JSONObject jsondata = XML.toJSONObject(str);
		final File fileJson = new File(file + "\\" + removeExtension(new File(xml).getName()) + ".json");
		try {
			Files.writeString(fileJson.toPath(), jsondata.toString(), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return fileJson.getAbsolutePath();

	}

	/**
	 * deleteDirectory
	 * 
	 * @param directoryToBeDeleted
	 * @return
	 */
	public static boolean deleteDirectory(final File directoryDel) {
		final File[] allContents = directoryDel.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryDel.delete();
	}

	/**
	 * décompresse le fichier zip dans le répertoire donné
	 * 
	 * @param folder  le répertoire où les fichiers seront extraits
	 * @param zipfile le fichier zip à décompresser
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<File> unzipFile(final String zipFilePath, final String destFilePath) throws IOException {
		final File destination = new File(destFilePath);
		final List<File> file = new ArrayList<>();
		if (!destination.exists()) {
			destination.mkdir();
		}
		if (new File(destFilePath).exists()) {
			new File(destFilePath).delete();
		}
		final Charset cp866 = Charset.forName(Constant.CHARSET);
		try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)), cp866)) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				final String filePath = destination + File.separator + zipEntry.getName();
				if (zipEntry.isDirectory()) {
					final File directory = new File(filePath);
					directory.mkdirs();
				} else {
					if (zipEntry.getName().startsWith("ASS_X04-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("ASS_A11-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("JDV_J06-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("JDV_J10-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("TRE_A04-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("TRE_A05-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
				}
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		return file;
	}

	/**
	 * extractFile
	 * 
	 * @param Zip_Input_Stream
	 * @param File_Path
	 * @throws IOException
	 */
	private static String extractZipFile(final ZipInputStream zipInputStream, final String filePath)
			throws IOException {
		try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(
				Files.newOutputStream(Paths.get(filePath)))) {
			final byte[] bytes = new byte[4096];
			int readByte = zipInputStream.read(bytes);
			while (readByte != -1) {
				bufferedOutput.write(bytes, 0, readByte);
				readByte = zipInputStream.read(bytes);
			}
		}
		final Path source = Paths.get(filePath);
		final String sourceReplace = source.toFile().getName().replace('-', '_');
		final Path sourceFile = source.resolveSibling(sourceReplace);
		if (sourceFile.toFile().exists()) {
			sourceFile.toFile().delete();
		}
		Files.move(source, sourceFile);
		String name = new File(filePath).getName();
		name = name.substring(0, name.indexOf('.')) + ".tabs";
		final String sourceName = sourceReplace.substring(0, sourceReplace.indexOf('.')) + ".tabs";
		new File(filePath).delete();
		final Path path = Paths.get(sourceFile.toUri());
		final Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll(name, sourceName);
		Files.write(path, content.getBytes(charset));
		return sourceFile.toString();
	}

	/**
	 * extractFile
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(final ZipInputStream zipIn, final String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
			final byte[] bytesIn = new byte[BUFFER_SIZE];
			int read;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static String marshelling(final File file, final String sLoincCode) {
		BufferedReader reader;
		String retour = "";
		String retours = "";
		try {
			reader = new BufferedReader(Files.newBufferedReader(Paths.get(file.getAbsolutePath())));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				retour = retour + line;
			}
			reader.close();
			final JSONObject json = XML.toJSONObject(retour);
			final String jsonString = json.toString(4);
			final ObjectMapper objectMapper = new ObjectMapper();
			final RootEntity root = objectMapper.readValue(jsonString, RootEntity.class);
			final List<MappedConcept> mappedConceptList = root.retrieveValueSetResponse.valueSet.mappedConceptList.mappedConcept;
			for (final MappedConcept mapped : mappedConceptList) {
				for (int i = 0; i < mapped.concept.size(); i++) {
					if (sLoincCode.equals(mapped.concept.get(i).code)) {
						retours = mapped.concept.get(i + 1).code;
					}
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static List<String> marshellingTre(final File file, final String sLoincCode) {
		final List<String> retours = new ArrayList<>();
		String codeSys = XdmService.getXpathSingleValue(file, "//*:CodeSystem/*:identifier/*:value/@value/string()");
		if (codeSys.contains("urn:oid:")) {
			codeSys = codeSys.replace("urn:oid:", "");
		}
		retours.add(codeSys);
		retours.add(XdmService.getXpathSingleValue(file,
				"//*:CodeSystem/*:concept[*:code/@value=\"" + sLoincCode + "\"]/*:display/@value/string()"));
		
		return retours;
	}

	/**
	 * marshellingJdv
	 * 
	 * @param file
	 */
	public static List<String> marshellingJdv(final File file, final String code) {
		List<String> retours = new ArrayList<>();
		try {
			final Object object = new JSONParser().parse(Files.newBufferedReader(Paths.get(file.getAbsolutePath())));
			final org.json.simple.JSONObject json = (org.json.simple.JSONObject) object;
			final String jsonString = json.toJSONString();
			final ObjectMapper objectMapper = new ObjectMapper();
			final RootJ root = objectMapper.readValue(jsonString, RootJ.class);
			objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			final List<ConceptJ> mappedConceptList = root.retrieveValueSetResponse.valueSet.conceptList.concept;
			for (final ConceptJ mapped : mappedConceptList) {
				if (code.equals(mapped.code)) {
					retours.add(mapped.codeSystem);
					retours.add(mapped.displayName);
				}
			}
		} catch (final IOException | ParseException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static String getXmlns(final File file, final String code) {
		BufferedReader reader;
		String retour = "";
		String retours = "";
		try {
			reader = new BufferedReader(Files.newBufferedReader(Paths.get(file.getAbsolutePath())));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				retour = retour + line;
			}
			reader.close();
			final JSONObject json = XML.toJSONObject(retour);
			final String jsonString = json.toString(4);
			final ObjectMapper objectMapper = new ObjectMapper();
			final RootEntity root = objectMapper.readValue(jsonString, RootEntity.class);
			final List<MappedConcept> mappedConceptList = root.retrieveValueSetResponse.valueSet.mappedConceptList.mappedConcept;
			for (final MappedConcept mapped : mappedConceptList) {
				for (int i = 0; i < mapped.concept.size(); i++) {
					if (code.equals(mapped.concept.get(i).code)) {
						retours = mapped.concept.get(i + 1).code;
					}
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * getHash
	 * 
	 * @param pCheminDocumentXML
	 * @return
	 */
	public static String getHash(final String pCheminDocumentXML) {
		String result = null;
		try {
			final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			try (final FileInputStream fis = new FileInputStream(pCheminDocumentXML);
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
			result = formatted.toString();
		} catch (final NoSuchAlgorithmException | IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return result;
	}

	/**
	 * downloadUsingNIO
	 * 
	 * @param urlStr
	 * @param file
	 * @throws IOException
	 */
	public static List<File> downloadUsingNIO(final String urlStr, final String file, final String urlCode1)
			throws IOException {
		final URL url = new URL(urlStr);
		List<File> xmlFile = null;
		FileUtils.copyURLToFile(url, new File(file));
		final String destFilePath = new File(file).getParent();
		try {
			if (file.endsWith(Constant.EXTENSIONJSON)) {
				xmlFile = readJsonFile(file, urlCode1);
			}
		} finally {
			new File(file).delete();
			new File(destFilePath).delete();
		}
		return xmlFile;
	}

	/**
	 * replaceInFile
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static File replaceInFile(final File file) throws IOException {
		final File fileToBeModified = file;
		String oldContent = "";
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(Files.newBufferedReader(Paths.get(fileToBeModified.getAbsolutePath())));
			String line = reader.readLine();
			while (line != null) {
				oldContent = oldContent + line + System.lineSeparator();
				line = reader.readLine();
			}
			final String newContent = oldContent.replaceAll("&amp;amp;", "&amp;");
			final String secondContent = newContent.replaceAll("xmlns=\"\"", "");
			writer = Files.newBufferedWriter(Paths.get(fileToBeModified.getAbsolutePath()));
			writer.write(secondContent);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		return fileToBeModified;
	}
}