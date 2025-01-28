package com.ans.cda.validation;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import com.ans.cda.service.validation.ValidationService;
import com.ans.cda.utilities.general.Constant;

/**
 * ValidationTest
 * 
 * @author bensalem Nizar
 */
public class ValidationTest {
	/**
	 * testValidateMeta
	 */
	@org.junit.jupiter.api.Test
	void testValidateMeta() {
		final String str = ValidationService.validateMeta(new File(Constant.FILEPATH), Constant.MODEL, Constant.ASIPXDM,
				Constant.URLVALIDATION);
		assertNotNull("error", str);
	}

	/**
	 * testValidateCda
	 */
	@org.junit.jupiter.api.Test
	void testValidateCda() {
		final String str = ValidationService.validateCda(new File(Constant.FILEPATHCDA), Constant.MODELCDA,
				Constant.URLVALIDATION, Constant.API, null);
		assertNotNull("error", str);
	}

	/**
	 * displayLastReport
	 */
	@org.junit.jupiter.api.Test
	void displayLastReport() {
		final String str = ValidationService.displayLastReport();
		assertNotNull("error", str);
	}
}
