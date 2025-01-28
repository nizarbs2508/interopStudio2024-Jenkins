package com.ans.cda.utilities.general;

import java.io.File;

import com.spire.pdf.conversion.PdfStandardsConverter;

/**
 * ConvertPdfToPdfAB1
 * 
 * @author bensa
 */
public final class ConvertPdfToPdfAB1 {

	/**
	 * ConvertPdfToPdfAB1
	 */
	private ConvertPdfToPdfAB1() {
		// empty constructor
	}

	/**
	 * main
	 * 
	 * @param file
	 */
	public static void main(final String file, final File fileFinal) {
		final PdfStandardsConverter converter = new PdfStandardsConverter(file);
		// Convert to PdfA1B
		converter.toPdfA1B(fileFinal.getAbsolutePath());

	}
}
