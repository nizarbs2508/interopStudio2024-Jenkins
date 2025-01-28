package com.ans.cda.service.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.LocalUtility;
import com.ans.cda.utilities.general.Utility;

/**
 * SaxonValidator
 * 
 * @author bensalem Nizar
 */
public final class SaxonValidator {
	/**
	 * CONFIG
	 */
	private static final File CONFIG = new File(Constant.INTEROPFOLDER + "\\config.properties");
	/**
	 * newFilePath
	 */
	private static Path apiFilePath = Paths.get(Constant.FILENAME + "//interopStudio//Validation//API");
	/**
	 * newFilePath
	 */
	private static Path newFilePath;
	/**
	 * newFilePath1
	 */
	private static Path newFilePath1;
	/**
	 * newFilePath2
	 */
	private static Path newFilePath2;
	/**
	 * newFilePath3
	 */
	private static Path newFilePath3;
	/**
	 * NEWFILEPATH4
	 */
	private static Path newFilePath4;
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(SaxonValidator.class);

	/**
	 * SaxonValidator
	 */
	private SaxonValidator() {
		// empty
	}

	/**
	 * isXMLFile
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isXMLFile(final File file) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.parse(file);
			return true; // Parsing succeeded, the file is valid XML
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return false; // Parsing failed, the file is not valid XML
		}
	}

	/**
	 * displayLastReport
	 * 
	 * @param webEngine
	 */
	public static String displayLastReport() {
		String ret = getNewFilePath4().toString();
		File dest = null;
		final String reportURL = getNewFilePath2().toString();
		try {
			final StreamSource myXPathDoc = new StreamSource(new File(reportURL));
			final String fileF = SaxonValidator.class.getResource("/API/svrl-to-html.xsl").toExternalForm();
			dest = new File(Constant.INTEROPFOLDER + "\\svrl-to-html.xsl");
			if (!dest.exists()) {
				try {
					FileUtils.copyURLToFile(new URL(fileF), dest);
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			}

			if (isXMLFile(new File(reportURL))) {
				final Transformer myXslTrans = TransformerFactory.newInstance().newTransformer(new StreamSource(dest));
				final File outputFile = new File(getNewFilePath4().toString());
				myXslTrans.transform(myXPathDoc, new StreamResult(outputFile));
			} else {
				ret = "error";
			}
		} catch (final TransformerException ex) {
			if (LOG.isInfoEnabled()) {
				final String error = ex.getMessage();
				LOG.error(error);
			}
			ret = "error";
		} finally {
			dest.delete();
		}
		return ret;
	}

	/**
	 * displayLastReport
	 * 
	 * @param webEngine
	 */
	public static String displayLastReport(final String svrl) {
		String ret = getNewFilePath4().toString();
		File dest = null;
		try {
			final StreamSource myXPathDoc = new StreamSource(new File(svrl));
			final String fileF = SaxonValidator.class.getResource("/API/svrl-to-html.xsl").toExternalForm();
			dest = new File(Constant.INTEROPFOLDER + "\\svrl-to-html.xsl");
			if (!dest.exists()) {
				try {
					FileUtils.copyURLToFile(new URL(fileF), dest);
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			}
			final Transformer myXslTrans = TransformerFactory.newInstance().newTransformer(new StreamSource(dest));
			final File outputFile = new File(getNewFilePath4().toString());
			myXslTrans.transform(myXPathDoc, new StreamResult(outputFile));
		} catch (final TransformerException ex) {
			if (LOG.isInfoEnabled()) {
				final String error = ex.getMessage();
				LOG.error(error);
			}
			ret = "error";
		} finally {
			dest.deleteOnExit();
		}
		return ret;
	}

	/**
	 * valideDocument
	 * 
	 * @param pDocumentContent
	 * @param pValidationServiceName
	 * @param pValidatorName
	 * @return
	 */
	public static String valideDocument(final String pDocumentContent, final String pValidationSName,
			final String pValidatorName, final String validationUrl) {
		getNewFilePath3().toFile().delete();
		getNewFilePath2().toFile().delete();
		getNewFilePath1().toFile().delete();
		getNewFilePath().toFile().delete();
		final String encodedDocument = Utility.encodeBase64(pDocumentContent);
		boolean done = false;
		final String fileF = Utility.getContextClassLoader().getResource("API/templateRequeteValidation.txt")
				.toExternalForm();
		final File dest = new File(Constant.INTEROPFOLDER + "\\templateRequeteValidation.txt");
		if (!dest.exists()) {
			try {
				FileUtils.copyURLToFile(new URL(fileF), dest);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}

			}
		}
		String sRequete = null;
		try {
			if (dest.exists()) {
				sRequete = readFile(dest.getAbsolutePath());
				sRequete = sRequete.replace("$CONTENT$", encodedDocument)
						.replace("$VALIDATION-SERVICE-NAME$", pValidationSName).replace("$VALIDATOR$", pValidatorName);
				prepareFilePaths();
				final List<String> lines = Collections.singletonList(sRequete);
				Files.write(getNewFilePath3().toFile().toPath(), lines, StandardCharsets.UTF_8);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

		try {
			done = sendRequest(validationUrl, sRequete);
		} catch (final IOException | InterruptedException e) {
			writeFile(getNewFilePath().toFile().getAbsolutePath(), LocalUtility.getString("message.error.server"));
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			dest.delete();
		}

		return Utility.getXpathSingleValue(getNewFilePath1().toFile(), done);
	}

	private static void prepareFilePaths() throws IOException {
		if (!apiFilePath.toFile().exists()) {
			apiFilePath.toFile().mkdirs();
		}
		createFileIfNotExists(getNewFilePath3());
		createFileIfNotExists(getNewFilePath());
		createFileIfNotExists(getNewFilePath1());
		createFileIfNotExists(getNewFilePath2());
		if (Files.size(getNewFilePath2()) == 0) {
			getNewFilePath2().toFile().delete();
			Files.createFile(getNewFilePath2());
		}
	}

	private static void createFileIfNotExists(final Path path) throws IOException {
		if (!path.toFile().exists()) {
			Files.createFile(path);
		}
	}

	/**
	 * writeFile
	 * 
	 * @param filePath
	 * @param content
	 */
	private static void writeFile(final String filePath, final String content) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
			writer.write(content);
			writer.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	private static boolean sendRequest(final String validationUrl, final String sRequete)
			throws IOException, InterruptedException {
		boolean done = false;
		final URL url = new URL(validationUrl);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-Type", "application/xml");
		httpCon.setConnectTimeout(1000000);
		httpCon.setReadTimeout(1000000);
		try (OutputStream ostream = Utility.getOutputStream(httpCon)) {
			final byte[] input = sRequete.getBytes("utf-8");
			ostream.write(input, 0, input.length);
		}
		final String message = httpCon.getResponseMessage();
		if (message.contains("Time-out")) {
			Files.write(getNewFilePath2().toFile().toPath(),
					Collections.singletonList(LocalUtility.getString("message.error.timeout")), StandardCharsets.UTF_8);
		} else {
			final int responseCode = httpCon.getResponseCode();
			if (responseCode == 429) {
				handleRateLimit(httpCon, url, sRequete);
			} else {
				done = handleResponse(httpCon, responseCode);
			}
		}
		return done;
	}

	private static void handleRateLimit(HttpURLConnection httpCon, URL url, String sRequete)
			throws IOException, InterruptedException {
		final String retryAfter = httpCon.getHeaderField("Retry-After");
		if (retryAfter != null) {
			final int waitTime = Integer.parseInt(retryAfter);
			Thread.sleep(waitTime * 1000);
			sendRequest(url.toString(), sRequete);
		}
	}

	private static boolean handleResponse(final HttpURLConnection httpCon, final int responseCode) throws IOException {
		boolean done = false;
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED
				|| responseCode == HttpURLConnection.HTTP_CREATED) {
			final String locationHeader = httpCon.getHeaderField("Location");
			writeFile(getNewFilePath().toFile().getAbsolutePath(), locationHeader);
			final URL url1 = new URL(locationHeader);
			final HttpURLConnection httpConnnection = (HttpURLConnection) url1.openConnection();
			httpConnnection.setRequestMethod("GET");
			httpConnnection.setRequestProperty("Accept", "application/xml");
			httpConnnection.setConnectTimeout(1000000);
			httpConnnection.setReadTimeout(1000000);
			if (handleGetResponse(httpConnnection, getNewFilePath1())) {
				done = true;
			}
			final URL url2 = new URL(locationHeader + "/report");
			final HttpURLConnection httpConnnection2 = (HttpURLConnection) url2.openConnection();
			httpConnnection2.setRequestMethod("GET");
			httpConnnection2.setRequestProperty("Accept", "application/xml");
			httpConnnection2.setConnectTimeout(1000000);
			httpConnnection2.setReadTimeout(1000000);
			handleGetResponse(httpConnnection2, getNewFilePath2());
		} else {
			writeFile(getNewFilePath().toFile().getAbsolutePath(), LocalUtility.getString("message.error.server"));
		}
		return done;
	}

	private static boolean handleGetResponse(HttpURLConnection httpConnnection, Path filePath) throws IOException {
		final int responseCode = httpConnnection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED
				|| responseCode == HttpURLConnection.HTTP_CREATED) {
			try (InputStream inputStream = httpConnnection.getInputStream();
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					BufferedReader reader = new BufferedReader(inputStreamReader)) {

				final StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				Files.write(filePath, Collections.singletonList(response.toString()), StandardCharsets.UTF_8);
			}
			return true;
		} else {
			Files.write(filePath, Collections.singletonList(LocalUtility.getString("message.error.timeout")),
					StandardCharsets.UTF_8);
			return false;
		}
	}

	/**
	 * readFile
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}

	/**
	 * getNewFilePath
	 * 
	 * @return
	 */
	public static Path getNewFilePath() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String lastUrl = prop.getProperty("url.last.result");
				if (lastUrl != null && !lastUrl.isEmpty()) {
					newFilePath = new File(lastUrl).toPath();
				} else {
					newFilePath = Paths.get(apiFilePath + "//document_validation_last_result_URL.txt");
				}
			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			newFilePath = Paths.get(apiFilePath + "//document_validation_last_result_URL.txt");
		}
		return newFilePath;
	}

	/**
	 * setNewFilePath
	 * 
	 * @param newFilePath
	 */
	public static void setNewFilePath(final Path newFilePath) {
		SaxonValidator.newFilePath = newFilePath;
	}

	/**
	 * getNewFilePath1
	 * 
	 * @return
	 */
	public static Path getNewFilePath1() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String lastResult = prop.getProperty("last.result");
				if (lastResult != null && !lastResult.isEmpty()) {
					newFilePath1 = new File(lastResult).toPath();
				} else {
					newFilePath1 = Paths.get(apiFilePath + "//document_validation_last_result.xml");
				}
			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			newFilePath1 = Paths.get(apiFilePath + "//document_validation_last_result.xml");
		}
		return newFilePath1;
	}

	/**
	 * setNewFilePath1
	 * 
	 * @param newFilePath1
	 */
	public static void setNewFilePath1(final Path newFilePath1) {
		SaxonValidator.newFilePath1 = newFilePath1;
	}

	/**
	 * getNewFilePath2
	 * 
	 * @return
	 */
	public static Path getNewFilePath2() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String lastReport = prop.getProperty("last.report");
				if (lastReport != null && !lastReport.isEmpty()) {
					newFilePath2 = new File(lastReport).toPath();
				} else {
					newFilePath2 = Paths.get(apiFilePath + "//document_validation_last_report.xml");
				}
			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			newFilePath2 = Paths.get(apiFilePath + "//document_validation_last_report.xml");
		}
		return newFilePath2;
	}

	/**
	 * setNewFilePath2
	 * 
	 * @param newFilePath2
	 */
	public static void setNewFilePath2(final Path newFilePath2) {
		SaxonValidator.newFilePath2 = newFilePath2;
	}

	/**
	 * getNewFilePath3
	 * 
	 * @return
	 */
	public static Path getNewFilePath3() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String lastRequest = prop.getProperty("last.request");
				if (lastRequest != null && !lastRequest.isEmpty()) {
					newFilePath3 = new File(lastRequest).toPath();
				} else {
					newFilePath3 = Paths.get(apiFilePath + "//document_validation_last_request.xml");
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			newFilePath3 = Paths.get(apiFilePath + "//document_validation_last_request.xml");
		}
		return newFilePath3;
	}

	/**
	 * setNewFilePath3
	 * 
	 * @param newFilePath3
	 */
	public static void setNewFilePath3(final Path newFilePath3) {
		SaxonValidator.newFilePath3 = newFilePath3;
	}

	/**
	 * getNewFilePath4
	 * 
	 * @return
	 */
	public static Path getNewFilePath4() {
		if (CONFIG.exists()) {
			try (InputStream input = new FileInputStream(CONFIG)) {
				final Properties prop = new Properties();
				prop.load(input);
				final String lastReportHtml = prop.getProperty("last.report.html");
				if (lastReportHtml != null && !lastReportHtml.isEmpty()) {
					newFilePath4 = new File(lastReportHtml).toPath();
				} else {
					newFilePath4 = Paths.get(apiFilePath + "//document_last_report_HTML.html");
				}

			} catch (final IOException ex) {
				if (LOG.isInfoEnabled()) {
					final String error = ex.getMessage();
					LOG.error(error);
				}
			}
		} else {
			newFilePath4 = Paths.get(apiFilePath + "//document_last_report_HTML.html");
		}
		return newFilePath4;
	}

	/**
	 * setNewFilePath4
	 * 
	 * @param newFilePath4
	 */
	public static void setNewFilePath4(final Path newFilePath4) {
		SaxonValidator.newFilePath4 = newFilePath4;
	}
}