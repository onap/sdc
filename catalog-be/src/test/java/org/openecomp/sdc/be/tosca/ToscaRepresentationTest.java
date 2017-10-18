package org.openecomp.sdc.be.tosca;

import java.util.List;

import javax.annotation.Generated;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.openecomp.sdc.be.model.Component;


public class ToscaRepresentationTest {

	private ToscaRepresentation createTestSubject() {
		return new ToscaRepresentation();
	}

	
	@Test
	public void testGetMainYaml() throws Exception {
		ToscaRepresentation testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMainYaml();
	}

	
	@Test
	public void testSetMainYaml() throws Exception {
		ToscaRepresentation testSubject;
		String mainYaml = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMainYaml(mainYaml);
	}

	
	@Test
	public void testGetDependencies() throws Exception {
		ToscaRepresentation testSubject;
		List<Triple<String, String, Component>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDependencies();
	}

	
	@Test
	public void testSetDependencies() throws Exception {
		ToscaRepresentation testSubject;
		List<Triple<String, String, Component>> dependancies = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDependencies(dependancies);
	}
}