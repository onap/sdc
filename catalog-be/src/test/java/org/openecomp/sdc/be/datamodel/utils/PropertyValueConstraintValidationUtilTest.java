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

package org.openecomp.sdc.be.datamodel.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;

class PropertyValueConstraintValidationUtilTest {

	@Mock
	ApplicationDataTypeCache applicationDataTypeCache;

	@Spy
	@InjectMocks
	PropertyValueConstraintValidationUtil propertyValueConstraintValidationUtil;

	private Map<String, DataTypeDefinition> dataTypeDefinitionMap;

	@BeforeEach
	void init() throws IOException {
		MockitoAnnotations.openMocks(this);
		ResponseFormatManager responseFormatManagerMock = mock(ResponseFormatManager.class);
		when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
		when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
		when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
		when(propertyValueConstraintValidationUtil.getResponseFormatManager()).thenReturn(responseFormatManagerMock);

		createDataTypeMap();
	}

	@Test
	void primitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("integer");
		propertyDefinition.setValue("10");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void primitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("integer");
		propertyDefinition.setValue("abcd");

		Either<Boolean, ResponseFormat> responseEither = propertyValueConstraintValidationUtil.validatePropertyConstraints(
			Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void complexWithValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"prefixlen\":\"4\"}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
					Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void complexWithValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"prefixlen\":\"5\"}");

		Either<Boolean, ResponseFormat> responseEither = propertyValueConstraintValidationUtil
			.validatePropertyConstraints(Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void complexWithListWithPrimitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"allocation_pools\":[\"slaac\"]}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void complexWithListWithPrimitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"allocation_pools\":[\"value\"]}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void complexWithMapWithPrimitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"value_specs\":{\"key\":\"slaac\"}}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void complexWithMapWithPrimitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"value_specs\":{\"key\":\"value\"}}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void inputValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setInputPath("propetyName#ipv6_ra_mode");
		inputDefinition.setDefaultValue("slaac");
		inputDefinition.setType("string");
		ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		inputDefinition.setProperties(Collections.singletonList(propertyDefinition));

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(inputDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void inputValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setInputPath("propetyName#ipv6_ra_mode");
		inputDefinition.setDefaultValue("incorrectValue");
		ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		inputDefinition.setProperties(Collections.singletonList(propertyDefinition));

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(inputDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void serviceConsumptionValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"ipv6_ra_mode\":\"slaac\"}");
		propertyDefinition.setName("ipv6_ra_mode");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void listOfComplexSuccessTest() {
		when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypeDefinitionMap));

		final var propertyDefinition = new PropertyDefinition();
		final String type = "list";
		propertyDefinition.setType(type);
		final SchemaDefinition schemaDefinition = new SchemaDefinition();
		final PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
		final String schemaType = "org.openecomp.datatypes.heat.network.neutron.Subnet";
		schemaProperty.setType(schemaType);
		schemaDefinition.setProperty(schemaProperty);
		propertyDefinition.setSchema(schemaDefinition);
		final String value = "[{\"ipv6_address_mode\": \"dhcpv6-stateful\"}, {\"ipv6_address_mode\": \"dhcpv6-stateless\"}]";
		propertyDefinition.setValue(value);
		final String name = "listOfComplex";
		propertyDefinition.setName(name);

		Either<Boolean, ResponseFormat> responseEither =
			propertyValueConstraintValidationUtil
				.validatePropertyConstraints(Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
		//original object values should not be changed
		assertEquals(name, propertyDefinition.getName());
		assertEquals(type, propertyDefinition.getType());
		assertEquals(value, propertyDefinition.getValue());
		assertEquals(schemaType, propertyDefinition.getSchemaType());
	}

	@Test
	void listOfComplexSuccessTest1() {
		when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypeDefinitionMap));

		final var propertyDefinition = new PropertyDefinition();
		final String type = "list";
		propertyDefinition.setType(type);
		final String listSchemaType = "org.openecomp.datatypes.heat.network.neutron.Subnet";
		final PropertyDataDefinition listSchemaProperty = new PropertyDataDefinition();
		listSchemaProperty.setType(listSchemaType);
		final SchemaDefinition listSchemaDefinition = new SchemaDefinition();
		listSchemaDefinition.setProperty(listSchemaProperty);
		final PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
		schemaProperty.setSchema(listSchemaDefinition);
		final String schemaType = "list";
		schemaProperty.setType(schemaType);
		final SchemaDefinition schemaDefinition = new SchemaDefinition();
		schemaDefinition.setProperty(schemaProperty);
		propertyDefinition.setSchema(schemaDefinition);
		final String value = "[[{\"ipv6_address_mode\": \"dhcpv6-stateful\"}, {\"ipv6_address_mode\": \"dhcpv6-stateless\"}], [{\"ipv6_address_mode\": \"dhcpv6-stateful\"}]]";
		propertyDefinition.setValue(value);
		final String name = "listOfComplex";
		propertyDefinition.setName(name);

		Either<Boolean, ResponseFormat> responseEither =
			propertyValueConstraintValidationUtil
				.validatePropertyConstraints(Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
		//original object values should not be changed
		assertEquals(name, propertyDefinition.getName());
		assertEquals(type, propertyDefinition.getType());
		assertEquals(value, propertyDefinition.getValue());
		assertEquals(schemaType, propertyDefinition.getSchemaType());
	}

	@Test
	void mapOfComplexSuccessTest() {
		when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(dataTypeDefinitionMap));

		final var propertyDefinition = new PropertyDefinition();
		final String type = "map";
		propertyDefinition.setType(type);
		final SchemaDefinition schemaDefinition = new SchemaDefinition();
		final PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
		final String schemaType = "org.openecomp.datatypes.heat.network.neutron.Subnet";
		schemaProperty.setType(schemaType);
		schemaDefinition.setProperty(schemaProperty);
		propertyDefinition.setSchema(schemaDefinition);
		final String value = "{\"key1\": {\"ipv6_address_mode\": \"dhcpv6-stateful\"}, \"key2\": {\"ipv6_address_mode\": \"dhcpv6-stateless\"}}";
		propertyDefinition.setValue(value);
		final String name = "mapOfComplex";
		propertyDefinition.setName(name);

		Either<Boolean, ResponseFormat> responseEither =
			propertyValueConstraintValidationUtil.validatePropertyConstraints(
				Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
		//original object values should not be changed
		assertEquals(name, propertyDefinition.getName());
		assertEquals(type, propertyDefinition.getType());
		assertEquals(value, propertyDefinition.getValue());
		assertEquals(schemaType, propertyDefinition.getSchemaType());
	}

	@Test
	void serviceConsumptionValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"ipv6_ra_mode\":\"incorrectValue\"}");
		propertyDefinition.setName("ipv6_ra_mode");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void bandwidthTypeValueSuccessTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"bandwidth_type\":\"guaranteed\"}");
		propertyDefinition.setName("bandwidth_type");

		Either<Boolean, ResponseFormat> responseEither = propertyValueConstraintValidationUtil.validatePropertyConstraints(
			Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);
		assertTrue(responseEither.isLeft());
	}

	@Test
	void bandwidthTypeValueFailTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"bandwidth_type\":\"incorrectValue\"}");
		propertyDefinition.setName("bandwidth_type");

		Either<Boolean, ResponseFormat> responseEither = propertyValueConstraintValidationUtil.validatePropertyConstraints(
			Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void bandwidthDownstreamValueSuccessTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"downstream\":\"128\"}");
		propertyDefinition.setName("downstream");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void bandwidthDownstreamValueFailTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"downstream\":\"incorrectValue\"}");
		propertyDefinition.setName("downstream");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void bandwidthUpstreamValueSuccessTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"upstream\":\"128\"}");
		propertyDefinition.setName("upstream");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void bandwidthUpstreamValueFailTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"upstream\":\"incorrectValue\"}");
		propertyDefinition.setName("upstream");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	@Test
	void bandwidthUnitsValueSuccessTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"units\":\"M\"}");
		propertyDefinition.setName("units");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isLeft());
	}

	@Test
	void bandwidthUnitsValueFailTest(){
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		when(applicationDataTypeCache.getAll(null)).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("onap.datatypes.partner.bandwidth");
		propertyDefinition.setValue("{\"units\":\"incorrectValue\"}");
		propertyDefinition.setName("units");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache, null);

		assertTrue(responseEither.isRight());
	}

	private void createDataTypeMap() throws IOException {
		Type constraintType = new TypeToken<PropertyConstraint>() {}.getType();
		Type typeOfHashMap = new TypeToken<Map<String, DataTypeDefinition>>() { }.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType,
				new PropertyOperation.PropertyConstraintDeserialiser()).create();

		dataTypeDefinitionMap = gson.fromJson(readDataTypeDefinitionFile(), typeOfHashMap);

		DataTypeDefinition dataTypeDefinition = dataTypeDefinitionMap.get("org.openecomp.datatypes.heat.network"
				+ ".neutron.Subnet");

		PropertyDefinition mapProperty = null;
		PropertyDefinition listProperty = null;
		List<PropertyConstraint> constraints = null;
		for (PropertyDefinition propertyDefinition : dataTypeDefinition.getProperties()) {
			if ("value_specs".equals(propertyDefinition.getName())) {
				mapProperty = propertyDefinition;
			} else if ("allocation_pools".equals(propertyDefinition.getName())) {
				listProperty = propertyDefinition;
			} else if ("ipv6_ra_mode".equals(propertyDefinition.getName())) {
				constraints = propertyDefinition.getConstraints();
			}
		}

		PropertyDefinition definition = new PropertyDefinition(mapProperty.getSchema().getProperty());
		definition.setConstraints(constraints);
		mapProperty.getSchema().setProperty(definition);

		definition = new PropertyDefinition(listProperty.getSchema().getProperty());
		definition.setConstraints(constraints);
		listProperty.getSchema().setProperty(definition);
	}

	private static String readDataTypeDefinitionFile() throws IOException {
		return Files.readString(Paths.get("src/test/resources/types/datatypes/constraintTest.json"));
	}

}

