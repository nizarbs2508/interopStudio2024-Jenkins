package com.ans.cda.service.artdecor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

/**
 * ArtDecorServiceTest
 * 
 * @author bensalem Nizar
 */
final class ArtDecorServiceTest {

	/**
	 * ArtDecorServiceTest
	 */
	private ArtDecorServiceTest() {
		// empty constructor
	}

	/**
	 * ArtDecorService
	 */

	/**
	 * testArtDecorCleaning
	 */
	@org.junit.jupiter.api.Test
	void testArtDecorCleaning() {
		final File file = ArtDecorService.transform("C:\\Users\\User\\Downloads\\Backup BBR 2024-06-21.xml");
		assertNotNull(file, "File should not be null");
	}

	/**
	 * testConvertXmlToJson
	 */
	@org.junit.jupiter.api.Test
	void testConvertXmlToJson() {
		final String str = ArtDecorService.convertXmlToJson("C:\\Users\\User\\Downloads\\Backup BBR 2024-06-21.xml");
		assertNotNull("String should not be null", str);
	}
}
