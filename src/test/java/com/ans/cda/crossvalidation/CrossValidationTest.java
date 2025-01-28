package com.ans.cda.crossvalidation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import com.ans.cda.service.crossvalidation.CrossValidationService;
import com.ans.cda.utilities.general.Constant;

/**
 * CrossValidationTest
 * 
 * @author bensalem Nizar
 */
final class CrossValidationTest {
	/**
	 * MSG
	 */
	public static final String MSG = "The map should be not null";

	/**
	 * CrossValidationTest
	 */
	private CrossValidationTest() {
		// empty constructor
	}

	/**
	 * crossValidationService
	 */

	/**
	 * testCrossValidate
	 */
	@org.junit.jupiter.api.Test
	void testCrossValidate() {
		final String result = CrossValidationService.crossValidate(new File(Constant.FILEPATHCDA),
				new File(Constant.FILEPATH), Constant.URLVALIDATION);
		assertNotNull(MSG, result);
	}
}
