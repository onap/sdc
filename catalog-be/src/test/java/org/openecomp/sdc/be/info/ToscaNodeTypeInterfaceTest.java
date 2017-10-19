package org.openecomp.sdc.be.info;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ToscaNodeTypeInterfaceTest {

	private ToscaNodeTypeInterface createTestSubject() {
		return new ToscaNodeTypeInterface();
	}

	
	@Test
	public void testGetScripts() throws Exception {
		ToscaNodeTypeInterface testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getScripts();
	}

	
	@Test
	public void testSetScripts() throws Exception {
		ToscaNodeTypeInterface testSubject;
		List<String> scripts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setScripts(scripts);
	}
}