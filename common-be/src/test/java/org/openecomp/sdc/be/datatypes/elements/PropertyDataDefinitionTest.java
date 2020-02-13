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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class PropertyDataDefinitionTest {

	private PropertyDataDefinition propDef;

	@Before
	public void setUp() {
		propDef = new PropertyDataDefinition();
	}

	@Test
	public void setStringField() {
		final String name = "name";
		assertNull(propDef.getName());
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.NAME));
		propDef.setToscaPresentationValue(JsonPresentationFields.NAME, name);
		assertEquals(name, propDef.getName());
		assertEquals(name, propDef.getToscaPresentationValue(JsonPresentationFields.NAME));
	}

	@Test
	public void setDefaultValue() {
		final String defaultValue = "text";
		assertNull(propDef.getDefaultValue());
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
		propDef.setToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE, defaultValue);
		assertEquals(defaultValue, propDef.getDefaultValue());
		assertEquals(defaultValue, propDef.getToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE));
	}

	@Test
	public void setValueNotDefinedInPropDataDefinition() {
		final String defaultValue = "VF";
		assertNull(propDef.getToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE));
		propDef.setToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE, defaultValue);
		assertEquals(defaultValue, propDef.getToscaPresentationValue(JsonPresentationFields.COMPONENT_TYPE));
	}

	@Test
	public void setBooleanField() {
		assertFalse((Boolean) propDef.getToscaPresentationValue(JsonPresentationFields.PASSWORD));
		assertFalse(propDef.isPassword());
		propDef.setToscaPresentationValue(JsonPresentationFields.PASSWORD, Boolean.TRUE);
		assertTrue(propDef.isPassword());
		assertTrue((Boolean) propDef.getToscaPresentationValue(JsonPresentationFields.PASSWORD));
	}

	@Test
	public void mergeDefaultValueWhenItWasNullBeforeMerge() {
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
	public void mergeDefaultValueAndOverrideIt() {
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
	public void mergeDefaultValueWhenOverridingIsNotAllowed() {
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
	public void testConstructor() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new PropertyDataDefinition(testSubject);
	}

	@Test
	public void testGetInputPath() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputPath();
	}


	@Test
	public void testSetInputPath() throws Exception {
		PropertyDataDefinition testSubject;
		String inputPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputPath(inputPath);
	}


	@Test
	public void testGetName() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}


	@Test
	public void testSetName() throws Exception {
		PropertyDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}


	@Test
	public void testGetValue() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}


	@Test
	public void testSetValue() throws Exception {
		PropertyDataDefinition testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}


	@Test
	public void testIsDefinition() throws Exception {
		PropertyDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}


	@Test
	public void testSetDefinition() throws Exception {
		PropertyDataDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}


	@Test
	public void testGetType() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}


	@Test
	public void testGetDefaultValue() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultValue();
	}


	@Test
	public void testSetDefaultValue() throws Exception {
		PropertyDataDefinition testSubject;
		String defaultValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultValue(defaultValue);
	}


	@Test
	public void testSetType() throws Exception {
		PropertyDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}


	@Test
	public void testIsRequired() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isRequired();
	}


	@Test
	public void testSetRequired() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean required = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequired(required);
	}


	@Test
	public void testGetDescription() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}


	@Test
	public void testSetDescription() throws Exception {
		PropertyDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}


	@Test
	public void testIsPassword() throws Exception {
		PropertyDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isPassword();
	}


	@Test
	public void testSetPassword() throws Exception {
		PropertyDataDefinition testSubject;
		boolean password = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setPassword(password);
	}


	@Test
	public void testGetUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}


	@Test
	public void testSetUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}


	@Test
	public void testGetSchema() throws Exception {
		PropertyDataDefinition testSubject;
		SchemaDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSchema();
	}


	@Test
	public void testSetSchema() throws Exception {
		PropertyDataDefinition testSubject;
		SchemaDefinition entrySchema = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSchema(entrySchema);
	}


	@Test
	public void testGetLabel() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}


	@Test
	public void testSetLabel() throws Exception {
		PropertyDataDefinition testSubject;
		String label = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}


	@Test
	public void testIsHidden() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isHidden();
	}


	@Test
	public void testSetHidden() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean hidden = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHidden(hidden);
	}


	@Test
	public void testIsImmutable() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isImmutable();
	}


	@Test
	public void testSetImmutable() throws Exception {
		PropertyDataDefinition testSubject;
		Boolean immutable = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImmutable(immutable);
	}


	@Test
	public void testGetParentUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentUniqueId();
	}


	@Test
	public void testSetParentUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String parentUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentUniqueId(parentUniqueId);
	}


	@Test
	public void testGetGetInputValues() throws Exception {
		PropertyDataDefinition testSubject;
		List<GetInputValueDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGetInputValues();
	}


	@Test
	public void testSetGetInputValues() throws Exception {
		PropertyDataDefinition testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGetInputValues(getInputValues);
	}


	@Test
	public void testGetStatus() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}


	@Test
	public void testSetStatus() throws Exception {
		PropertyDataDefinition testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}


	@Test
	public void testGetInputId() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputId();
	}


	@Test
	public void testSetInputId() throws Exception {
		PropertyDataDefinition testSubject;
		String inputId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputId(inputId);
	}


	@Test
	public void testGetInstanceUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstanceUniqueId();
	}


	@Test
	public void testSetInstanceUniqueId() throws Exception {
		PropertyDataDefinition testSubject;
		String instanceUniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInstanceUniqueId(instanceUniqueId);
	}


	@Test
	public void testGetPropertyId() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyId();
	}


	@Test
	public void testSetPropertyId() throws Exception {
		PropertyDataDefinition testSubject;
		String propertyId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyId(propertyId);
	}


	@Test
	public void testToString() throws Exception {
		PropertyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}


	@Test
	public void testHashCode() throws Exception {
		PropertyDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}


	@Test
	public void testEquals() throws Exception {
		PropertyDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		PropertyDataDefinition other = createTestSubject();
		result = testSubject.equals(other);
		Assert.assertEquals(true, result);
	}

	@Test
	public void testConvertPropertyDataToInstancePropertyData() throws Exception {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.convertPropertyDataToInstancePropertyData();
	}

	@Test
	public void testTypeEquals() throws Exception {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.typeEquals(testSubject);
		testSubject.typeEquals(null);
		testSubject.typeEquals(createTestSubject());
	}

	@Test
	public void testMergeFunction() throws Exception {
		PropertyDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.mergeFunction(createTestSubject(), false);

	}

	@Test
	public void schemaTypeNullWhenSchemaIsNull() {
		String sampleSchemaType = "sampleSchemaType";
		PropertyDataDefinition testSubject = createTestSubject();
		testSubject.setSchemaType(sampleSchemaType);
		assertNull(testSubject.getSchemaType());
	}

	@Test
	public void schemaTypeIsReturnedWhenSchemaisPresent() {
		String sampleSchemaType = "sampleSchemaType";
		SchemaDefinition schemaDefinition = new SchemaDefinition();
		schemaDefinition.setProperty(new PropertyDataDefinition());

		PropertyDataDefinition testSubject = createTestSubject();
		testSubject.setSchema(schemaDefinition);
		testSubject.setSchemaType(sampleSchemaType);

		assertThat(testSubject.getSchemaType(), is(equalTo(sampleSchemaType)));
	}
}
