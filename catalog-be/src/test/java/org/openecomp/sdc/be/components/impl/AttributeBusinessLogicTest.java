package org.openecomp.sdc.be.components.impl;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;


public class AttributeBusinessLogicTest {

	private AttributeBusinessLogic createTestSubject() {
		return new AttributeBusinessLogic();
	}

	
	@Test
	public void testCreateAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		PropertyDefinition newAttributeDef = null;
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testIsAttributeExist() throws Exception {
	AttributeBusinessLogic testSubject;List<PropertyDefinition> attributes = null;
	String resourceUid = "";
	String propertyName = "";
	boolean result;
	
	// test 1
	testSubject=createTestSubject();attributes = null;
	}

	
	@Test
	public void testGetAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		PropertyDefinition newAttDef = null;
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteAttribute() throws Exception {
		AttributeBusinessLogic testSubject;
		String resourceId = "";
		String attributeId = "";
		String userId = "";
		Either<PropertyDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}
}