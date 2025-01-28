package com.ans.cda.service.crossvalidation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

import com.ans.cda.service.validation.SaxonValidator;
import com.ans.cda.utilities.general.Constant;
import com.ans.cda.utilities.general.LocalUtility;

/**
 * CrossValidationService
 * 
 * @author bensalem Nizar
 */
public final class CrossValidationService {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(CrossValidationService.class);

	/**
	 * CrossValidationService
	 */
	private CrossValidationService() {
		// empty constructor
	}

	/**
	 * crossValidate
	 * 
	 * @param textfield
	 * @param listeTypeValidation
	 */
	public static String crossValidate(final File textfield, final File textfieldMeta, final String validationUrl) {
		String console = "";
		if (Files.exists(Paths.get(textfield.getAbsolutePath()))) {
			try {
				final String contents = new String(Files.readAllBytes(Paths.get(textfield.toURI())),
						StandardCharsets.UTF_8);
				try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(textfield.toURI()),
						StandardCharsets.UTF_8)) {
					writer.write(contents);
					writer.close();
				}
				final String contentsMeta = new String(Files.readAllBytes(Paths.get(textfieldMeta.toURI())),
						StandardCharsets.UTF_8);
				try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(textfieldMeta.toURI()),
						StandardCharsets.UTF_8)) {
					writer.write(contentsMeta);
					writer.close();
				}
				final Instant startTime = Instant.now();
				// Récupérer le nom du validateur META
				String content = Files.readString(textfield.toPath(), StandardCharsets.UTF_8);
				String contentMeta = Files.readString(textfieldMeta.toPath(), StandardCharsets.UTF_8);
				// call valideDOCUMENT function
				int count = 0;
				String validationResult = SaxonCrossValidator.crossValidateDocument(content, contentMeta,
						validationUrl);
				while (validationResult == null && count <= 5) {
					validationResult = SaxonCrossValidator.crossValidateDocument(content, contentMeta, validationUrl);
					count++;
				}
				final Instant endTime = Instant.now();
				if (validationResult != null) {

					console = LocalUtility.getString("message.start.treatment") + Instant.now() + "\n"
							+ LocalUtility.getString("message.valid.treatment") + validationResult + "\n"
							+ LocalUtility.getString("message.duration.treatment")
							+ Duration.between(startTime, endTime).getSeconds()
							+ LocalUtility.getString("message.second.treatment") + "\n"
							+ LocalUtility.getString("message.completed.treatment") + "\n\n"
							+ LocalUtility.getString("message.created.treatment") + "\n"
							+ SaxonCrossValidator.getNewFilePath() + "\n" + SaxonCrossValidator.getNewFilePath1() + "\n"
							+ SaxonCrossValidator.getNewFilePath2() + "\n" + SaxonCrossValidator.getNewFilePath3()
							+ "\n";
				} else {
					console = LocalUtility.getString("message.start.treatment") + Instant.now() + "\n"
							+ LocalUtility.getString("message.valid.treatment") + validationResult + "\n"
							+ LocalUtility.getString("message.duration.treatment")
							+ Duration.between(startTime, endTime).getSeconds()
							+ LocalUtility.getString("message.second.treatment") + "\n"
							+ LocalUtility.getString("message.error.server") + "\n\n"
							+ LocalUtility.getString("message.created.treatment") + "\n"
							+ SaxonValidator.getNewFilePath() + "\n" + SaxonValidator.getNewFilePath1() + "\n"
							+ SaxonValidator.getNewFilePath2() + "\n" + SaxonValidator.getNewFilePath3() + "\n";
				}
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} else {
			console = LocalUtility.getString("message.invalid.meta.file");
		}
		if (!Constant.LOGFILE.exists()) {
			try {
				Constant.LOGFILE.createNewFile();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
		try (BufferedWriter myWriter = Files.newBufferedWriter(Paths.get(Constant.LOGFILE.getAbsolutePath()))) {
			myWriter.write(console);
			myWriter.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}

		return console;
	}

	/**
	 * displayLastReport
	 * 
	 * @param webEngine
	 */
	public static String displayLastReport() {
		return SaxonCrossValidator.displayLastReport();
	}

}
