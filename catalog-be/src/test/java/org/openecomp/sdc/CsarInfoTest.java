package org.openecomp.sdc;

import java.util.Map;
import java.util.Queue;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.junit.Test;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;


public class CsarInfoTest {

	private CsarInfo createTestSubject() {
		return new CsarInfo( new User(), "", null, "","","", false);
	}

	
	@Test
	public void testGetVfResourceName() throws Exception {
		CsarInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfResourceName();
	}

	
	@Test
	public void testSetVfResourceName() throws Exception {
		CsarInfo testSubject;
		String vfResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfResourceName(vfResourceName);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		CsarInfo testSubject;
		User result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		CsarInfo testSubject;
		User modifier = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetCsarUUID() throws Exception {
		CsarInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCsarUUID();
	}

	
	@Test
	public void testSetCsarUUID() throws Exception {
		CsarInfo testSubject;
		String csarUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCsarUUID(csarUUID);
	}

	
	@Test
	public void testGetCsar() throws Exception {
		CsarInfo testSubject;
		Map<String, byte[]> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCsar();
	}

	
	@Test
	public void testSetCsar() throws Exception {
		CsarInfo testSubject;
		Map<String, byte[]> csar = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCsar(csar);
	}

	
	@Test
	public void testGetMainTemplateContent() throws Exception {
		CsarInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMainTemplateContent();
	}

	
	@Test
	public void testGetMappedToscaMainTemplate() throws Exception {
		CsarInfo testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMappedToscaMainTemplate();
	}

	
	@Test
	public void testGetCreatedNodesToscaResourceNames() throws Exception {
		CsarInfo testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatedNodesToscaResourceNames();
	}


	
	@Test
	public void testIsUpdate() throws Exception {
		CsarInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isUpdate();
	}

	
	@Test
	public void testSetUpdate() throws Exception {
		CsarInfo testSubject;
		boolean isUpdate = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setUpdate(isUpdate);
	}

	
	@Test
	public void testGetCreatedNodes() throws Exception {
		CsarInfo testSubject;
		Map<String, Resource> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatedNodes();
	}
}