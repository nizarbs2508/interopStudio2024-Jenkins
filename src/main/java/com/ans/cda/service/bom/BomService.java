package com.ans.cda.service.bom;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

/**
 * BomService
 * 
 * @author bensalem Nizar
 */
public final class BomService {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(BomService.class);

	/**
	 * BomService
	 */
	private BomService() {

	}

	/**
	 * save
	 * 
	 * @param file
	 */
	public static void save(final String file) {
		try {
			final File xmlFile = new File(file);
			final byte[] fileBytes = readFileWithoutBOM(xmlFile);
			final String xmlContent = new String(fileBytes, StandardCharsets.UTF_8);
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(xmlContent.getBytes());
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
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
	 * saveAsUTF8WithoutBOM
	 * 
	 * @param fileName
	 * @param encoding
	 * @throws IOException
	 */
	public static void saveAsUTF8WithoutBOM(final String fileName, final Charset encoding) throws IOException {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName");
		}
		final String content = new String(Files.readAllBytes(Paths.get(fileName)), encoding);
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
			writer.write(content);
			writer.close();
		}
	}

	/**
	 * readFileWithoutBOM
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static byte[] readFileWithoutBOM(final File file) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
			if (bis.markSupported()) {
				bis.mark(3);
				final byte[] bom = new byte[3];
				final int read = bis.read(bom, 0, 3);
				if (read == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
					if (LOG.isInfoEnabled()) {
						LOG.info("BOM detected and removed.");
					}
				} else {
					bis.reset();
				}
			}
			final byte[] buffer = new byte[1024];
			int len;
			while ((len = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.info(error);
			}
		}
		return baos.toByteArray();
	}
}