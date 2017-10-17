package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class NodeTypeInfoTest {

	private NodeTypeInfo createTestSubject() {
		return new NodeTypeInfo();
	}

	
	@Test
	public void testGetUnmarkedCopy() throws Exception {
		NodeTypeInfo testSubject;
		NodeTypeInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUnmarkedCopy();
	}

	
	@Test
	public void testGetType() throws Exception {
		NodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		NodeTypeInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetTemplateFileName() throws Exception {
		NodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTemplateFileName();
	}

	
	@Test
	public void testSetTemplateFileName() throws Exception {
		NodeTypeInfo testSubject;
		String templateFileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTemplateFileName(templateFileName);
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		NodeTypeInfo testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		NodeTypeInfo testSubject;
		List<String> derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testIsNested() throws Exception {
		NodeTypeInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isNested();
	}

	
	@Test
	public void testSetNested() throws Exception {
		NodeTypeInfo testSubject;
		boolean isNested = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setNested(isNested);
	}

	
	@Test
	public void testGetMappedToscaTemplate() throws Exception {
		NodeTypeInfo testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMappedToscaTemplate();
	}

	
	@Test
	public void testSetMappedToscaTemplate() throws Exception {
		NodeTypeInfo testSubject;
		Map<String, Object> mappedToscaTemplate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMappedToscaTemplate(mappedToscaTemplate);
	}
}