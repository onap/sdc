/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.HeatParametersOperation;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;

import fj.data.Either;

public class HeatParametersOperationTest extends ModelTestBase {

	HeatParametersOperation heatParametersOperation = new HeatParametersOperation();

	TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);

	@Before
	public void setup() {
		heatParametersOperation.setTitanGenericDao(titanGenericDao);

	}

	@Test
	public void addPropertyToResourceTest() {

		String propName = "myProp";
		HeatParameterDefinition property = buildHeatPropertyDefinition();

		HeatParameterData propertyData = new HeatParameterData(property);

		Either<HeatParameterData, TitanOperationStatus> either = Either.left(propertyData);

		GraphRelation graphRelation = new GraphRelation();
		Either<GraphRelation, TitanOperationStatus> relationResult = Either.left(graphRelation);

		when(titanGenericDao.createNode(any(HeatParameterData.class), eq(HeatParameterData.class))).thenReturn(either);
		when(titanGenericDao.createRelation(any(GraphNode.class), (GraphNode) any(GraphNode.class), eq(GraphEdgeLabels.HEAT_PARAMETER), anyMap())).thenReturn(relationResult);

		Either<HeatParameterData, TitanOperationStatus> result = heatParametersOperation.addPropertyToGraph(propName, property, "resourceId.artifactId", NodeTypeEnum.ArtifactRef);

		assertTrue(result.isLeft());

	}

	@Test
	public void addPropertyListToResourceTest() {

		HeatParameterDefinition property = buildHeatPropertyDefinition();
		HeatParameterDefinition property2 = buildHeatPropertyDefinition();
		property2.setName("p2");

		List<HeatParameterDefinition> parameters = new ArrayList<HeatParameterDefinition>();
		parameters.add(property);
		parameters.add(property2);

		HeatParameterData propertyData = new HeatParameterData(property);

		Either<HeatParameterData, TitanOperationStatus> either = Either.left(propertyData);

		GraphRelation graphRelation = new GraphRelation();
		Either<GraphRelation, TitanOperationStatus> relationResult = Either.left(graphRelation);

		when(titanGenericDao.createNode(any(HeatParameterData.class), eq(HeatParameterData.class))).thenReturn(either);
		when(titanGenericDao.createRelation(any(GraphNode.class), any(GraphNode.class), eq(GraphEdgeLabels.HEAT_PARAMETER), anyMap())).thenReturn(relationResult);

		StorageOperationStatus result = heatParametersOperation.addPropertiesToGraph(parameters, "resourceId.artifactId", NodeTypeEnum.ArtifactRef);

		assertEquals(StorageOperationStatus.OK, result);

	}

	@Test
	public void testStringValues() {
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.STRING, "50aaa"));
	}

	@Test
	public void testNumberValues() {
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.NUMBER, "50"));
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.NUMBER, "50.5"));
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.NUMBER, "0x11"));

		assertFalse(heatParametersOperation.isValidValue(HeatParameterType.NUMBER, "aaa"));
		assertFalse(heatParametersOperation.isValidValue(HeatParameterType.NUMBER, "?>!"));
	}

	@Test
	public void testJsonValues() {
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.JSON, "{ \"member\" : \"50\"}"));
		HeatParameterDefinition propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.JSON.getType(), "{ \"member\" : \"50\"}");
		StorageOperationStatus operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
		assertEquals(StorageOperationStatus.OK, operationStatus);
		assertEquals(HeatParameterType.JSON.getType(), propertyDefinition.getType());

	}

	@Test
	public void testListValues() {
		assertTrue(heatParametersOperation.isValidValue(HeatParameterType.COMMA_DELIMITED_LIST, "one, two"));
		HeatParameterDefinition propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.COMMA_DELIMITED_LIST.getType(), "one, two");
		StorageOperationStatus operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
		assertEquals(StorageOperationStatus.OK, operationStatus);
		assertEquals(HeatParameterType.COMMA_DELIMITED_LIST.getType(), propertyDefinition.getType());
		assertEquals("one, two", propertyDefinition.getDefaultValue());
	}

	@Test
	public void testBooleanValues() {

		String[] trueArray = { "true", "t", "1", "on", "y", "yes" };
		String[] falseArray = { "false", "f", "0", "off", "n", "no" };

		for (int i = 0; i < trueArray.length; i++) {
			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, trueArray[i]));
			HeatParameterDefinition propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), trueArray[i]);
			StorageOperationStatus operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("true", propertyDefinition.getDefaultValue());

			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, trueArray[i]));
			propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), trueArray[i].toUpperCase());
			operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("true", propertyDefinition.getDefaultValue());

			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, trueArray[i]));
			propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), trueArray[i].toLowerCase());
			operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("true", propertyDefinition.getDefaultValue());
		}

		for (int i = 0; i < falseArray.length; i++) {
			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, falseArray[i]));
			HeatParameterDefinition propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), falseArray[i]);
			StorageOperationStatus operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("false", propertyDefinition.getDefaultValue());

			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, falseArray[i]));
			propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), falseArray[i].toUpperCase());
			operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("false", propertyDefinition.getDefaultValue());

			assertTrue(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, falseArray[i]));
			propertyDefinition = buildHeatBooleanPropertyDefinition(HeatParameterType.BOOLEAN.getType(), falseArray[i].toLowerCase());
			operationStatus = heatParametersOperation.validateAndUpdateProperty(propertyDefinition);
			assertEquals(StorageOperationStatus.OK, operationStatus);
			assertEquals("false", propertyDefinition.getDefaultValue());
		}

		assertFalse(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, "blabla"));
		assertFalse(heatParametersOperation.isValidValue(HeatParameterType.BOOLEAN, "2"));
	}

	private HeatParameterDefinition buildHeatPropertyDefinition() {
		HeatParameterDefinition parameter = new HeatParameterDefinition();

		parameter.setName("p1");
		parameter.setType("string");
		parameter.setDefaultValue("def");
		parameter.setCurrentValue("current");
		parameter.setDescription("description");

		return parameter;
	}

	private HeatParameterDefinition buildHeatBooleanPropertyDefinition(String type, String boolValue) {
		HeatParameterDefinition parameter = new HeatParameterDefinition();

		parameter.setName("parameter1");
		parameter.setType(type);
		parameter.setDefaultValue(boolValue);
		parameter.setDescription("description");

		return parameter;
	}

	@Test
	public void addPropertyToResourceInstanceTest() {

		HeatParameterDefinition property = buildHeatPropertyDefinition();

		HeatParameterValueData propertyData = new HeatParameterValueData();
		propertyData.setUniqueId("bla");
		propertyData.setValue("value1");

		Either<HeatParameterValueData, TitanOperationStatus> either = Either.left(propertyData);

		GraphRelation graphRelation = new GraphRelation();
		Either<GraphRelation, TitanOperationStatus> relationResult = Either.left(graphRelation);

		when(titanGenericDao.createNode(any(HeatParameterValueData.class), eq(HeatParameterValueData.class))).thenReturn(either);
		when(titanGenericDao.createRelation(any(GraphNode.class), any(GraphNode.class), eq(GraphEdgeLabels.PARAMETER_VALUE), anyMap())).thenReturn(relationResult);
		when(titanGenericDao.createRelation(any(GraphNode.class), any(GraphNode.class), eq(GraphEdgeLabels.PARAMETER_IMPL), isNull())).thenReturn(relationResult);

		Either<HeatParameterValueData, TitanOperationStatus> result = heatParametersOperation.addHeatValueToGraph(property, "artifactLabel", "resourceInstanceId.artifactId", "resourceInstanceId");

		assertTrue(result.isLeft());

	}

}
