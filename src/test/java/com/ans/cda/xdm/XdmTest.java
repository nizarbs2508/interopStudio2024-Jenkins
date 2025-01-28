package com.ans.cda.xdm;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import com.ans.cda.service.xdm.XdmService;

/**
 * XdmTest
 * 
 * @author bensalem Nizar
 */
public class XdmTest {
	/**
	 * testGenerateMeta
	 */
	@org.junit.jupiter.api.Test
	void testGenerateMeta() {
		final List<String> pList = new ArrayList<>();
		pList.add(
				"C:\\Users\\bensa\\Downloads\\TestContenuCDA-3-0-main(8)\\TestContenuCDA-3-0-main\\ExemplesCDA\\AVC-AUNV_2022.01.xml");
		XdmService.generateMeta(pList, "https://smt.esante.gouv.fr/fhir/ValueSet?_summary=true",
				"https://smt.esante.gouv.fr/fhir/CodeSystem?_summary=true",
				"https://smt.esante.gouv.fr/fhir/CodeSystem/?url=", null, null, null);
		assertNotNull("error", pList);
	}
}
