package com.ans.cda.control;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import com.ans.cda.service.control.ControlCdaService;

/**
 * ControlCdaTest
 * 
 * @author bensalem Nizar
 */
final class ControlCdaTest {
	/**
	 * UUIDPATH
	 */
	public static final String UUIDPATH = "D:\\TestContenuCDA-3-0-main\\TestContenuCDA-3-0-main\\ExemplesCDA\\ANEST-CR-CPA_2022.01.xml";
	/**
	 * LOINCPATH
	 */
	public static final String LOINCPATH = "D:\\TestContenuCDA-3-0-main\\TestContenuCDA-3-0-main\\ExemplesCDA\\ANEST-CR-CPA_2022.01.xml";

	/**
	 * ControlCdaTest
	 */
	private ControlCdaTest() {
		// empty constructor
	}

	/**
	 * testControlCda
	 */
	@org.junit.jupiter.api.Test
	void testControlCda() {
		ControlCdaService.checkUUID(UUIDPATH);
	}

	/**
	 * testcontroleLoincCodes
	 */
	@org.junit.jupiter.api.Test
	void testcontroleLoincCodes() {
		final Map<List<String>, List<String>> map = ControlCdaService.controleLoincCodes(LOINCPATH);
		assertNotNull("The map should be not null", map);
	}

}
