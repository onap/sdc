/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;


class PropertyDataDefinitionTest {

	private PropertyDataDefinition propDef;

	@BeforeEach
	public void setUp() {
		propDef = new PropertyDataDefinition();
	}

	@Test
    void setStringField() {
		final String name = "name";
		assertNull(propDef.getName());
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.NAME));
		propDef.setToscaPresentationValue(JsonPresentationFields.NAME, name);
		assertEquals(name, propDef.getName());
		assertEquals(name, propDef.getToscaPresentationValue(JsonPresentationFields.NAME));
	}

	@Test
    void setDefaultValue() {
		final String defaultValue = "text";
		assertNull(propDef.getDefaultValue());
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
		propDef.setToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE, defaultValue);
		assertEquals(defaultValue, propDef.getDefaultValue());
		assertEquals(defaultValue, propDef.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
	}

	@Test
    void setValueNotDefinedInPropDataDefinition() {
		final String defaultValue = "VF";
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE));
		propDef.setToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE, defaultValue);
		assertEquals(defaultValue, propDef.getToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE));
	}

	@Test
    void setBooleanField() {
		assertFalse((Boolean) propDef.getToscaPresentationValue(JsonPresentationFields.PASSWORD));
		assertFalse(propDef.isPassword());
		propDef.setToscaPresentationValue(JsonPresentationFields.PASSWORD, Boolean.TRUE);
		assertTrue(propDef.isPassword());
		assertTrue((Boolean) propDef.getToscaPresentationValue(JsonPresentationFields.PASSWORD));
	}

	@Test
    void mergeDefaultValueWhenItWasNullBeforeMerge() {
		final String defaultValue = "12345";
		final String type = "1";
		PropertyDataDefinition propForMerge = new PropertyDataDefinition();
		propForMerge.setType(type);

		propDef.setType(type);
		propDef.setDefaultValue(defaultValue);
		assertNull(propForMerge.getDefaultValue());
		assertNull(propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
		propDef.mergeFunction(propForMerge, true);
		assertEquals(defaultValue, propForMerge.getDefaultValue());
		assertEquals(defaultValue, propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
	}

	@Test
    void mergeDefaultValueAndOverrideIt() {
		final String defaultValue = "12345";
		final String defaultValueForOther = "7890";
		final String type = "1";
		PropertyDataDefinition propForMerge = new PropertyDataDefinition();
		propForMerge.setType(type);
		propForMerge.setDefaultValue(defaultValueForOther);

		propDef.setType(type);
		propDef.setDefaultValue(defaultValue);
		assertEquals(defaultValueForOther, propForMerge.getDefaultValue());
		assertEquals(defaultValueForOther, propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
		propDef.mergeFunction(propForMerge, true);
		assertEquals(defaultValue, propForMerge.getDefaultValue());
		assertEquals(defaultValue, propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
	}

	@Test
    void mergeDefaultValueWhenOverridingIsNotAllowed() {
		final String defaultValue = "12345";
		final String defaultValueForOther = "7890";
		final String type = "1";
		PropertyDataDefinition propForMerge = new PropertyDataDefinition();
		propForMerge.setType(type);
		propForMerge.setDefaultValue(defaultValueForOther);

		propDef.setType(type);
		propDef.setDefaultValue(defaultValue);
		assertEquals(defaultValueForOther, propForMerge.getDefaultValue());
		assertEquals(defaultValueForOther, propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
		propDef.mergeFunction(propForMerge, false);
		assertEquals(defaultValueForOther, propForMerge.getDefaultValue());
		assertEquals(defaultValueForOther, propForMerge.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
	}

	private PropertyDataDefinition createTestSubject() {
		return new PropertyDataDefinition();
	}

	@Test
    void testConstructor() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new PropertyDataDefinition(testSubject);
	}

	@Test
    void testGetInputPath() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputPath();
	}


	@Test
    void testSetInputPath() {
		PropertyDataDefinition testSubject;
		String inputPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputPath(inputPath);
	}


	@Test
    void testGetName() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}


	@Test
    void testSetName() {
		PropertyDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}


	@Test
    void testGetValue() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}


	@Test
    void testSetValue() {
		PropertyDataDefinition testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}


	@Test
    void testIsDefinition() {
		PropertyDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}


	@Test
    void testSetDefinition() {
		PropertyDataDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}


	@Test
    void testGetType() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}


	@Test
    void testGetDefaultValue() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultValue();
	}


	@Test
    void testSetDefaultValue() {
		PropertyDataDefinition testSubject;
		String defaultValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultValue(defaultValue);
	}


	@Test
    void testSetType() {
		PropertyDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}


	@Test
    void testIsRequired() {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isRequired();
	}


	@Test
    void testSetRequired() {
		PropertyDataDefinition testSubject;
		Boolean required = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequired(required);
	}


	@Test
    void testGetDescription() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}


	@Test
    void testSetDescription() {
		PropertyDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}


	@Test
    void testIsPassword() {
		PropertyDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isPassword();
	}


	@Test
    void testSetPassword() {
		PropertyDataDefinition testSubject;
		boolean password = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setPassword(password);
	}


	@Test
    void testGetUniqueId() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}


	@Test
    void testSetUniqueId() {
		PropertyDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}


	@Test
    void testGetSchema() {
		PropertyDataDefinition testSubject;
		SchemaDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSchema();
	}


	@Test
    void testSetSchema() {
		PropertyDataDefinition testSubject;
		SchemaDefinition entrySchema = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSchema(entrySchema);
	}


	@Test
    void testGetLabel() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}


	@Test
    void testSetLabel() {
		PropertyDataDefinition testSubject;
		String label = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}


	@Test
    void testIsHidden() {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isHidden();
	}


	@Test
    void testSetHidden() {
		PropertyDataDefinition testSubject;
		Boolean hidden = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHidden(hidden);
	}


	@Test
    void testIsImmutable() {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isImmutable();
	}


	@Test
    void testSetImmutable() {
		PropertyDataDefinition testSubject;
		Boolean immutable = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImmutable(immutable);
	}


	@Test
    void testGetParentUniqueId() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentUniqueId();
	}


	@Test
    void testSetParentUniqueId() {
		PropertyDataDefinition testSubject;
		String parentUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentUniqueId(parentUniqueId);
	}


	@Test
    void testGetGetInputValues() {
		PropertyDataDefinition testSubject;
		List<GetInputValueDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGetInputValues();
	}


	@Test
    void testSetGetInputValues() {
		PropertyDataDefinition testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGetInputValues(getInputValues);
	}


	@Test
    void testGetStatus() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}


	@Test
    void testSetStatus() {
		PropertyDataDefinition testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}


	@Test
    void testGetInputId() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputId();
	}


	@Test
    void testSetInputId() {
		PropertyDataDefinition testSubject;
		String inputId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputId(inputId);
	}


	@Test
    void testGetInstanceUniqueId() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstanceUniqueId();
	}


	@Test
    void testSetInstanceUniqueId() {
		PropertyDataDefinition testSubject;
		String instanceUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInstanceUniqueId(instanceUniqueId);
	}


	@Test
    void testGetPropertyId() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyId();
	}


	@Test
    void testSetPropertyId() {
		PropertyDataDefinition testSubject;
		String propertyId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyId(propertyId);
	}


	@Test
    void testToString() {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}


	@Test
    void testHashCode() {
		PropertyDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}


	@Test
    void testEquals() {
		PropertyDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		assertEquals(false, result);
		result = testSubject.equals(testSubject);
		assertEquals(true, result);
		PropertyDataDefinition other = createTestSubject();
		result = testSubject.equals(other);
		assertEquals(true, result);
	}

	@Test
    void testConvertPropertyDataToInstancePropertyData() {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.convertPropertyDataToInstancePropertyData();
	}

	@Test
    void testTypeEquals() {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.typeEquals(testSubject);
		testSubject.typeEquals(null);
		testSubject.typeEquals(createTestSubject());
	}

	@Test
    void testMergeFunction() {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.mergeFunction(createTestSubject(), false);

	}

	@Test
    void schemaTypeNullWhenSchemaIsNull() {
		String sampleSchemaType = "sampleSchemaType";
		PropertyDataDefinition testSubject = createTestSubject();
		testSubject.setSchemaType(sampleSchemaType);
		assertNull(testSubject.getSchemaType());
	}

	@Test
    void schemaTypeIsReturnedWhenSchemaIsPresent() {
		String sampleSchemaType = "sampleSchemaType";
		SchemaDefinition schemaDefinition = new SchemaDefinition();
		schemaDefinition.setProperty(new PropertyDataDefinition());

		PropertyDataDefinition testSubject = createTestSubject();
		testSubject.setSchema(schemaDefinition);
		testSubject.setSchemaType(sampleSchemaType);

		assertThat(testSubject.getSchemaType(), is(equalTo(sampleSchemaType)));
	}

	@Test
    void getToscaGetFunctionTypeTest() {
		var propertyDataDefinition = new PropertyDataDefinition();
		assertNull(propertyDataDefinition.getToscaGetFunctionType());

		final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
		propertyDataDefinition.setToscaFunction(toscaGetFunction);

		toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
		assertEquals(ToscaGetFunctionType.GET_INPUT, propertyDataDefinition.getToscaGetFunctionType());

		toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
		assertEquals(ToscaGetFunctionType.GET_PROPERTY, propertyDataDefinition.getToscaGetFunctionType());

		toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_ATTRIBUTE);
		assertEquals(ToscaGetFunctionType.GET_ATTRIBUTE, propertyDataDefinition.getToscaGetFunctionType());

		propertyDataDefinition = new PropertyDataDefinition();
		propertyDataDefinition.setToscaGetFunctionType(ToscaGetFunctionType.GET_INPUT);
		assertEquals(ToscaGetFunctionType.GET_INPUT, propertyDataDefinition.getToscaGetFunctionType());

		propertyDataDefinition.setToscaGetFunctionType(ToscaGetFunctionType.GET_PROPERTY);
		assertEquals(ToscaGetFunctionType.GET_PROPERTY, propertyDataDefinition.getToscaGetFunctionType());

		propertyDataDefinition.setToscaGetFunctionType(ToscaGetFunctionType.GET_ATTRIBUTE);
		assertEquals(ToscaGetFunctionType.GET_ATTRIBUTE, propertyDataDefinition.getToscaGetFunctionType());
	}

	@Test
	void isToscaFunctionTest() {
		var propertyDataDefinition = new PropertyDataDefinition();
		assertFalse(propertyDataDefinition.isToscaFunction());

		propertyDataDefinition.setToscaGetFunctionType(ToscaGetFunctionType.GET_PROPERTY);
		assertTrue(propertyDataDefinition.isToscaFunction());

		propertyDataDefinition = new PropertyDataDefinition();
		propertyDataDefinition.setToscaFunction(new ToscaConcatFunction());
		assertTrue(propertyDataDefinition.isToscaFunction());
	}

	@Test
	void isToscaGetFunctionTest() {
		var propertyDataDefinition = new PropertyDataDefinition();
		propertyDataDefinition.setToscaGetFunctionType(ToscaGetFunctionType.GET_PROPERTY);
		assertTrue(propertyDataDefinition.isToscaGetFunction());

		propertyDataDefinition = new PropertyDataDefinition();
		final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
		toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
		propertyDataDefinition.setToscaFunction(toscaGetFunction);
		assertTrue(propertyDataDefinition.isToscaGetFunction());

		propertyDataDefinition.setToscaFunction(new ToscaConcatFunction());
		assertFalse(propertyDataDefinition.isToscaGetFunction());
	}

}
