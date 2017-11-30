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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;

public class PropertyOperationTest extends ModelTestBase {

	TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);
	
	PropertyOperation propertyOperation = new PropertyOperation(titanGenericDao);

	@Before
	public void setup() {
		propertyOperation.setTitanGenericDao(titanGenericDao);

	}

	/*
	 * @Test public void addPropertyToResourceTest() {
	 * 
	 * String propName = "myProp"; PropertyDefinition property = buildPropertyDefinition(); List<PropertyConstraint> constraints = buildConstraints(); property.setConstraints(constraints);
	 * 
	 * PropertyData propertyData = new PropertyData(property, propertyOperation.convertConstraintsToString(constraints));
	 * 
	 * Either<PropertyData, TitanOperationStatus> either = Either.left(propertyData); //when(propertyDao.create((GraphNeighbourTable)anyObject(), eq(PropertyData.class), eq(NodeTypeEnum.Property))).thenReturn(either); GraphRelation graphRelation =
	 * new GraphRelation(); Either<GraphRelation, TitanOperationStatus> relationResult = Either.left(graphRelation);
	 * 
	 * when(titanGenericDao.createNode((PropertyData)anyObject(), eq(PropertyData.class))).thenReturn(either); when(titanGenericDao.createRelation((GraphNode)anyObject(), (GraphNode)anyObject(), eq(GraphEdgeLabels.PROPERTY),
	 * anyMap())).thenReturn(relationResult);
	 * 
	 * Either<PropertyDefinition, StorageOperationStatus> result = propertyOperation.addPropertyToResource(propName, property, NodeTypeEnum.Resource, "my-resource.1.0");
	 * 
	 * assertTrue(result.isLeft()); System.out.println(result.left().value()); PropertyDefinition propertyDefinition = result.left().value();
	 * 
	 * List<PropertyConstraint> originalConstraints = property.getConstraints(); List<PropertyConstraint> propertyConstraintsResult = propertyDefinition.getConstraints(); assertEquals(propertyConstraintsResult.size(), originalConstraints.size());
	 * 
	 * }
	 */
	private PropertyDefinition buildPropertyDefinition() {
		PropertyDefinition property = new PropertyDefinition();
		property.setDefaultValue("10");
		property.setDescription("Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
		property.setType(ToscaType.INTEGER.name().toLowerCase());
		return property;
	}

	@Test
	public void addPropertiesToGraphTableTest() {

		// Map<String, PropertyDefinition> properties = new HashMap<String,
		// PropertyDefinition>();
		// String propName = "myProp";
		// PropertyDefinition property = buildPropertyDefinition();
		//
		// List<PropertyConstraint> constraints = buildConstraints();
		// property.setConstraints(constraints);
		//
		// properties.put(propName, property);
		//
		// GraphNeighbourTable graphNeighbourTable = new GraphNeighbourTable();
		// ResourceData resourceData = new ResourceData();
		// String resourceName = "my-resource";
		// String resourceVersion = "1.0";
		// String resourceId = resourceName + "." + resourceVersion;
		// resourceData.setUniqueId(resourceId);
		// int resourceIndex = graphNeighbourTable.addNode(resourceData);
		//
		// heatParametersOperation.addPropertiesToGraphTable(properties,
		// graphNeighbourTable, resourceIndex, resourceId);
		//
		// assertEquals(2, graphNeighbourTable.getNodes().size());
		// assertEquals(1, graphNeighbourTable.getDirectedEdges().size());
		// List<GraphNode> nodes = graphNeighbourTable.getNodes();
		// boolean nodeFound = false;
		// for (GraphNode neo4jNode : nodes) {
		// if (neo4jNode instanceof PropertyData) {
		// PropertyData propertyData = (PropertyData)neo4jNode;
		// assertEquals("check property unique id", resourceId + "." + propName,
		// propertyData.getUniqueId());
		// assertEquals(property.getDescription(),
		// propertyData.getPropertyDataDefinition().getDescription());
		// nodeFound = true;
		// }
		// }
		// assertEquals("looking for PropertyData object in table", true,
		// nodeFound);
		//
		// NodeRelation nodeRelation =
		// graphNeighbourTable.getDirectedEdges().get(0);
		// assertEquals("check from index to index edge", 0,
		// nodeRelation.getFromIndex());
		// assertEquals("check from index to index edge", 1,
		// nodeRelation.getToIndex());
		// assertEquals("check edge type",
		// GraphEdgePropertiesDictionary.PROPERTY,
		// nodeRelation.getEdge().getEdgeType());
		// assertEquals("check propert name on edge", true,
		// nodeRelation.getEdge().getProperties().values().contains(propName));
	}

	@Test
	public void convertConstraintsTest() {

		List<PropertyConstraint> constraints = buildConstraints();
		List<String> convertedStringConstraints = propertyOperation.convertConstraintsToString(constraints);
		assertEquals("constraints size", constraints.size(), convertedStringConstraints.size());

		List<PropertyConstraint> convertedConstraints = propertyOperation.convertConstraints(convertedStringConstraints);
		assertEquals("check size of constraints", constraints.size(), convertedConstraints.size());

		Set<String> constraintsClasses = new HashSet<String>();
		for (PropertyConstraint propertyConstraint : constraints) {
			constraintsClasses.add(propertyConstraint.getClass().getName());
		}

		for (PropertyConstraint propertyConstraint : convertedConstraints) {
			assertTrue("check all classes generated", constraintsClasses.contains(propertyConstraint.getClass().getName()));
		}
	}

	@Test
	public void testIsPropertyDefaultValueValid_NoDefault() {
		PropertyDefinition property = new PropertyDefinition();
		property.setName("myProperty");
		property.setType(ToscaPropertyType.BOOLEAN.getType());
		assertTrue(propertyOperation.isPropertyDefaultValueValid(property, null));
	}

	@Test
	public void testIsPropertyDefaultValueValid_ValidDefault() {
		PropertyDefinition property = new PropertyDefinition();
		property.setName("myProperty");
		property.setType(ToscaPropertyType.INTEGER.getType());
		property.setDefaultValue("50");
		assertTrue(propertyOperation.isPropertyDefaultValueValid(property, null));
	}

	@Test
	public void testIsPropertyDefaultValueValid_InvalidDefault() {
		PropertyDefinition property = new PropertyDefinition();
		property.setName("myProperty");
		property.setType(ToscaPropertyType.BOOLEAN.getType());
		property.setDefaultValue("50");
		assertFalse(propertyOperation.isPropertyDefaultValueValid(property, null));
	}

	private List<PropertyConstraint> buildConstraints() {
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
		LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
		List<String> range = new ArrayList<String>();
		range.add("0");
		range.add("100");
		InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
		constraints.add(propertyConstraint1);
		constraints.add(propertyConstraint2);
		constraints.add(propertyConstraint3);
		return constraints;
	}

	@Test
	public void findPropertyValueBestMatch1() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");
		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");
		instanceIdToValue.put("node2", instanceProperty2);

		ComponentInstanceProperty instanceProperty3 = new ComponentInstanceProperty();
		instanceProperty3.setValue("v1node3");
		instanceIdToValue.put("node3", instanceProperty3);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", "v1node1", instanceProperty.getValue());
		assertEquals("check default value", "v1node2", instanceProperty.getDefaultValue());

	}

	@Test
	public void findPropertyValueBestMatch2() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");
		instanceProperty2.setValueUniqueUid("aaaa");
		instanceIdToValue.put("node2", instanceProperty2);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", "v1node2", instanceProperty.getValue());
		assertEquals("check default value", "vv1", instanceProperty.getDefaultValue());
		assertNull("check value unique id is null", instanceProperty.getValueUniqueUid());

	}

	@Test
	public void findPropertyValueBestMatch3() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");
		instanceProperty1.setValueUniqueUid("aaaa");
		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty3 = new ComponentInstanceProperty();
		instanceProperty3.setValue("v1node3");
		instanceIdToValue.put("node3", instanceProperty3);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", "v1node1", instanceProperty.getValue());
		assertEquals("check default value", "v1node3", instanceProperty.getDefaultValue());
		assertEquals("check valid unique id", instanceProperty1.getValueUniqueUid(), instanceProperty.getValueUniqueUid());

	}

	@Test
	public void findPropertyValueBestMatch1Rules() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");

		List<PropertyRule> rules = new ArrayList<>();
		PropertyRule propertyRule = new PropertyRule();
		String[] ruleArr = { "node1", ".+", "node3" };
		List<String> rule1 = new ArrayList<>(Arrays.asList(ruleArr));
		propertyRule.setRule(rule1);
		propertyRule.setValue("88");
		rules.add(propertyRule);
		instanceProperty1.setRules(rules);

		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");
		instanceIdToValue.put("node2", instanceProperty2);

		ComponentInstanceProperty instanceProperty3 = new ComponentInstanceProperty();
		instanceProperty3.setValue("v1node3");
		instanceIdToValue.put("node3", instanceProperty3);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", propertyRule.getValue(), instanceProperty.getValue());
		assertEquals("check default value", "v1node2", instanceProperty.getDefaultValue());

	}

	@Test
	public void findPropertyValueBestMatch2Rules() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");

		List<PropertyRule> rules = new ArrayList<>();
		PropertyRule propertyRule1 = new PropertyRule();
		String[] ruleArr1 = { "node1", "node2", ".+" };
		List<String> rule1 = new ArrayList<>(Arrays.asList(ruleArr1));
		propertyRule1.setRule(rule1);
		propertyRule1.setValue("88");

		PropertyRule propertyRule2 = new PropertyRule();
		String[] ruleArr2 = { "node1", "node2", "node3" };
		List<String> rule2 = new ArrayList<>(Arrays.asList(ruleArr2));
		propertyRule2.setRule(rule2);
		propertyRule2.setValue("99");

		rules.add(propertyRule2);
		rules.add(propertyRule1);

		instanceProperty1.setRules(rules);

		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");
		instanceIdToValue.put("node2", instanceProperty2);

		ComponentInstanceProperty instanceProperty3 = new ComponentInstanceProperty();
		instanceProperty3.setValue("v1node3");
		instanceIdToValue.put("node3", instanceProperty3);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", propertyRule2.getValue(), instanceProperty.getValue());
		assertEquals("check default value", "v1node2", instanceProperty.getDefaultValue());

	}

	@Test
	public void findPropertyValueBestMatch1RuleLowLevel() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");

		List<PropertyRule> rules = new ArrayList<>();
		PropertyRule propertyRule1 = new PropertyRule();
		String[] ruleArr1 = { "node1", "node2", ".+" };
		List<String> rule1 = new ArrayList<>(Arrays.asList(ruleArr1));
		propertyRule1.setRule(rule1);
		propertyRule1.setValue("88");

		PropertyRule propertyRule2 = new PropertyRule();
		String[] ruleArr2 = { "node1", "node2", "node3" };
		List<String> rule2 = new ArrayList<>(Arrays.asList(ruleArr2));
		propertyRule2.setRule(rule2);
		propertyRule2.setValue("99");

		rules.add(propertyRule2);
		rules.add(propertyRule1);

		instanceProperty1.setRules(rules);

		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");

		List<PropertyRule> rules3 = new ArrayList<>();
		PropertyRule propertyRule3 = new PropertyRule();
		String[] ruleArr3 = { "node2", "node3" };
		List<String> rule3 = new ArrayList<>(Arrays.asList(ruleArr3));
		propertyRule3.setRule(rule3);
		propertyRule3.setValue("77");
		rules3.add(propertyRule3);

		instanceProperty2.setRules(rules3);
		instanceIdToValue.put("node2", instanceProperty2);

		ComponentInstanceProperty instanceProperty3 = new ComponentInstanceProperty();
		instanceProperty3.setValue("v1node3");
		instanceIdToValue.put("node3", instanceProperty3);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", propertyRule2.getValue(), instanceProperty.getValue());
		assertEquals("check default value", propertyRule3.getValue(), instanceProperty.getDefaultValue());

	}

	@Test
	public void findPropertyValueBestMatchDefaultValueNotChanged() {

		String propertyUniqueId = "x1";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		instanceProperty.setValue("v1");
		instanceProperty.setDefaultValue("vv1");
		List<String> path = new ArrayList<>();
		path.add("node1");
		path.add("node2");
		path.add("node3");
		instanceProperty.setPath(path);

		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<String, ComponentInstanceProperty>();
		ComponentInstanceProperty instanceProperty1 = new ComponentInstanceProperty();
		instanceProperty1.setValue("v1node1");

		List<PropertyRule> rules = new ArrayList<>();
		PropertyRule propertyRule1 = new PropertyRule();
		String[] ruleArr1 = { "node1", "node2", ".+" };
		List<String> rule1 = new ArrayList<>(Arrays.asList(ruleArr1));
		propertyRule1.setRule(rule1);
		propertyRule1.setValue("88");

		PropertyRule propertyRule2 = new PropertyRule();
		String[] ruleArr2 = { "node1", "node2", "node3" };
		List<String> rule2 = new ArrayList<>(Arrays.asList(ruleArr2));
		propertyRule2.setRule(rule2);
		propertyRule2.setValue("99");

		rules.add(propertyRule2);
		rules.add(propertyRule1);

		instanceProperty1.setRules(rules);

		instanceIdToValue.put("node1", instanceProperty1);

		ComponentInstanceProperty instanceProperty2 = new ComponentInstanceProperty();
		instanceProperty2.setValue("v1node2");

		List<PropertyRule> rules3 = new ArrayList<>();
		PropertyRule propertyRule3 = new PropertyRule();
		String[] ruleArr3 = { "node2", "node333" };
		List<String> rule3 = new ArrayList<>(Arrays.asList(ruleArr3));
		propertyRule3.setRule(rule3);
		propertyRule3.setValue("77");
		rules3.add(propertyRule3);

		instanceProperty2.setRules(rules3);
		instanceIdToValue.put("node2", instanceProperty2);

		propertyOperation.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);

		assertEquals("check value", propertyRule2.getValue(), instanceProperty.getValue());
		assertEquals("check default value", "vv1", instanceProperty.getDefaultValue());

	}

	// add all rule types
	// add rule with size = 1(instance itself = ALL). relevant for VLi. equals
	// to X.*.*.* in all paths size
}
