package com.ans.cda.xdm;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ans.cda.service.xdm.IheXdmService;

/**
 * IheXdmTest
 * 
 * @author bensalem Nizar
 */
public class IheXdmTest {

	/**
	 * generateIheXdmZip
	 */
	@org.junit.jupiter.api.Test
	void generateIheXdmZip() {
		final List<String> pList = new ArrayList<>();
		pList.add("D:\\TestContenuCDA-3-0-main\\TestContenuCDA-3-0-main\\ExemplesCDA\\AVC-AUNV_2022.01.xml");
		IheXdmService.generateIheXdmZip(pList, "D:\\", null);
		assertNotNull("error", pList);
	}

	/**
	 * generateAllIheXdmZip
	 */
	@org.junit.jupiter.api.Test
	void generateAllIheXdmZip() {
		final String path = "D:\\TestContenuCDA-3-0-main\\TestContenuCDA-3-0-main\\ExemplesCDA";
		for (final File file : new File(path).listFiles()) {
			IheXdmService.generateAllIheXdmZip(file.getAbsolutePath(),
					"D:\\TestContenuCDA-3-0-main\\TestContenuCDA-3-0-main\\IHE_XDM\\VALID_CDA", null, null, true, true,
					"https://smt.esante.gouv.fr/fhir/ValueSet?_summary=true",
					"https://smt.esante.gouv.fr/fhir/CodeSystem?_summary=true",
					"https://smt.esante.gouv.fr/fhir/CodeSystem/?url=", null, null, null, null);
		}
		assertNotNull("error", path);

	}

}
