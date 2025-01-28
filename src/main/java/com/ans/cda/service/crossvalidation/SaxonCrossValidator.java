package com.ans.cda.service.crossvalidation;

import java.io.File;
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
 * SaxonCrossValidator
 * 
 * @author bensalem Nizar
 */
public final class SaxonCrossValidator {
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
	private static final Logger LOG = Logger.getLogger(SaxonCrossValidator.class);

	/**
	 * SaxonCrossValidator
	 */
	private SaxonCrossValidator() {
		// empty constructor
	}

	/**
	 * crossValidateDocument
	 * 
	 * @param pDocumentContent
	 * @param pValidationServiceName
	 * @param pValidatorName
	 * @return
	 */
	public static String crossValidateDocument(final String pCDAContent, final String pMetadataContent,
			final String validationUrl) {
		getNewFilePath3().toFile().delete();
		getNewFilePath2().toFile().delete();
		getNewFilePath1().toFile().delete();
		getNewFilePath().toFile().delete();
		final String encodedDocument = Utility.encodeBase64(pCDAContent);
		final String encodedMetadata = Utility.encodeBase64(pMetadataContent);
		String locationHeader;

		final String fileF = Utility.getContextClassLoader().getResource("API/templateRequeteCrossValidation.txt")
				.toExternalForm();
		File dest = new File(Constant.INTEROPFOLDER + "\\templateRequeteCrossValidation.txt");
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
				sRequete = Utility.readFile(dest.getAbsolutePath());
				sRequete = sRequete.replace("$ENCODED_CDA$", encodedDocument).replace("$ENCODED_METADATA$",
						encodedMetadata);

				if (!apiFilePath.toFile().exists()) {
					apiFilePath.toFile().mkdirs();
				}
				Files.createFile(getNewFilePath3());
				Files.createFile(getNewFilePath2());
				Files.createFile(getNewFilePath1());
				Files.createFile(getNewFilePath());

				Files.writeString(getNewFilePath3().toFile().toPath(), sRequete, StandardCharsets.UTF_8);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

		try {
			final URL url = new URL(validationUrl);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty("Content-Type", "application/xml");
			httpCon.setConnectTimeout(1000000);

			try (OutputStream os = httpCon.getOutputStream()) {
				os.write(sRequete.getBytes(StandardCharsets.UTF_8));
			}

			int responseCode = httpCon.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED
					|| responseCode == HttpURLConnection.HTTP_CREATED) {
				locationHeader = httpCon.getHeaderField("Location");
				Utility.writeFile(getNewFilePath().toFile().getAbsolutePath(), locationHeader);
				handleResponse(locationHeader);
			} else if (responseCode == 429) {
				handleRetry(httpCon, sRequete, validationUrl);
			} else {
				Utility.writeFile(getNewFilePath().toFile().getAbsolutePath(),
						LocalUtility.getString("message.error.server"));
			}

		} catch (final IOException e) {
			Utility.writeFile(getNewFilePath().toFile().getAbsolutePath(),
					LocalUtility.getString("message.error.server"));
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			dest.delete();
		}
		return Utility.getXpathSingleValue(getNewFilePath1().toFile());
	}

	/**
	 * handleResponse
	 * 
	 * @param locationHeader
	 * @throws IOException
	 */
	private static void handleResponse(String locationHeader) throws IOException {
		final URL url1 = new URL(locationHeader);
		HttpURLConnection httpConnnection = (HttpURLConnection) url1.openConnection();
		httpConnnection.setRequestMethod("GET");
		httpConnnection.setRequestProperty("Accept", "application/xml");
		httpConnnection.setConnectTimeout(1000000);

		int responseCode1 = httpConnnection.getResponseCode();
		if (responseCode1 == HttpURLConnection.HTTP_OK || responseCode1 == HttpURLConnection.HTTP_ACCEPTED
				|| responseCode1 == HttpURLConnection.HTTP_CREATED) {
			try (InputStream inputStream = httpConnnection.getInputStream();
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
				StringBuilder response = new StringBuilder();
				int read;
				char[] buffer = new char[200_000];
				while ((read = inputStreamReader.read(buffer)) != -1) {
					response.append(buffer, 0, read);
				}
				Files.writeString(getNewFilePath1().toFile().toPath(), response.toString(), StandardCharsets.UTF_8);
			}
		} else {
			Files.writeString(getNewFilePath1().toFile().toPath(), LocalUtility.getString("message.error.timeout"),
					StandardCharsets.UTF_8);
		}

		final URL url2 = new URL(locationHeader + "/report");
		HttpURLConnection httpConnnection2 = (HttpURLConnection) url2.openConnection();
		httpConnnection2.setRequestMethod("GET");
		httpConnnection2.setRequestProperty("Accept", "application/xml");
		httpConnnection2.setConnectTimeout(1000000);

		int responseCode2 = httpConnnection2.getResponseCode();
		if (responseCode2 == HttpURLConnection.HTTP_OK || responseCode2 == HttpURLConnection.HTTP_ACCEPTED
				|| responseCode2 == HttpURLConnection.HTTP_CREATED) {
			try (InputStream inputStream = httpConnnection2.getInputStream();
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
				StringBuilder response = new StringBuilder();
				int read;
				char[] buffer = new char[200_000];
				while ((read = inputStreamReader.read(buffer)) != -1) {
					response.append(buffer, 0, read);
				}
				Files.writeString(getNewFilePath2().toFile().toPath(), response.toString(), StandardCharsets.UTF_8);
			}
		} else {
			Files.writeString(getNewFilePath2().toFile().toPath(), LocalUtility.getString("message.error.timeout"),
					StandardCharsets.UTF_8);
		}
	}

	/**
	 * handleRetry
	 * 
	 * @param httpCon
	 * @param sRequete
	 * @param validationUrl
	 * @throws IOException
	 */
	private static void handleRetry(HttpURLConnection httpCon, String sRequete, String validationUrl)
			throws IOException {
		String retryAfter = httpCon.getHeaderField("Retry-After");
		if (retryAfter != null) {
			int waitTime = Integer.parseInt(retryAfter);
			try {
				Thread.sleep(waitTime * 1000);
				HttpURLConnection retryCon = (HttpURLConnection) new URL(validationUrl).openConnection();
				retryCon.setDoOutput(true);
				retryCon.setRequestMethod("POST");
				retryCon.setRequestProperty("Content-Type", "application/xml");
				retryCon.setConnectTimeout(1000000);

				try (OutputStream os = retryCon.getOutputStream()) {
					os.write(sRequete.getBytes(StandardCharsets.UTF_8));
				}

				int retryResponseCode = retryCon.getResponseCode();
				if (retryResponseCode == HttpURLConnection.HTTP_OK
						|| retryResponseCode == HttpURLConnection.HTTP_ACCEPTED
						|| retryResponseCode == HttpURLConnection.HTTP_CREATED) {
					String locationHeader = retryCon.getHeaderField("Location");
					Utility.writeFile(getNewFilePath().toFile().getAbsolutePath(), locationHeader);
					handleResponse(locationHeader);
				} else {
					Utility.writeFile(getNewFilePath().toFile().getAbsolutePath(),
							LocalUtility.getString("message.error.server"));
				}

			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
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
			final String fileF = SaxonCrossValidator.class.getResource("/API/svrl-to-html.xsl").toExternalForm();
			dest = new File(Constant.INTEROPFOLDER + "\\svrl-to-html.xsl");
			try {
				FileUtils.copyURLToFile(new URL(fileF), dest);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
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
	 * getNewFilePath
	 * 
	 * @return
	 */
	public static Path getNewFilePath() {
		if (new File(Constant.CONFIG).exists()) {
			try (InputStream input = Files.newInputStream(Paths.get(Constant.CONFIG))) {
				final java.util.Properties prop = new java.util.Properties();
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
		SaxonCrossValidator.newFilePath = newFilePath;
	}

	/**
	 * getNewFilePath1
	 * 
	 * @return
	 */
	public static Path getNewFilePath1() {
		if (new File(Constant.CONFIG).exists()) {
			try (InputStream input = Files.newInputStream(Paths.get(Constant.CONFIG))) {
				final java.util.Properties prop = new java.util.Properties();
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
		SaxonCrossValidator.newFilePath1 = newFilePath1;
	}

	/**
	 * getNewFilePath2
	 * 
	 * @return
	 */
	public static Path getNewFilePath2() {
		if (new File(Constant.CONFIG).exists()) {
			try (InputStream input = Files.newInputStream(Paths.get(Constant.CONFIG))) {
				final java.util.Properties prop = new java.util.Properties();
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
		SaxonCrossValidator.newFilePath2 = newFilePath2;
	}

	/**
	 * getNewFilePath3
	 * 
	 * @return
	 */
	public static Path getNewFilePath3() {
		if (new File(Constant.CONFIG).exists()) {
			try (InputStream input = Files.newInputStream(Paths.get(Constant.CONFIG))) {
				final java.util.Properties prop = new java.util.Properties();
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
		SaxonCrossValidator.newFilePath3 = newFilePath3;
	}

	/**
	 * getNewFilePath4
	 * 
	 * @return
	 */
	public static Path getNewFilePath4() {
		if (new File(Constant.CONFIG).exists()) {
			try (InputStream input = Files.newInputStream(Paths.get(Constant.CONFIG))) {
				final java.util.Properties prop = new java.util.Properties();
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
		SaxonCrossValidator.newFilePath4 = newFilePath4;
	}
}