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

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;

import java.util.*;

import static org.junit.Assert.*;

public class PropertyOperationTest extends ModelTestBase {

    HealingTitanGenericDao titanGenericDao = Mockito.mock(HealingTitanGenericDao.class);

    PropertyOperation propertyOperation = new PropertyOperation(titanGenericDao, null);

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

        Set<String> constraintsClasses = new HashSet<>();
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
        List<PropertyConstraint> constraints = new ArrayList<>();
        GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
        LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
        List<String> range = new ArrayList<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();

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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

        Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
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

	private PropertyOperation createTestSubject() {
		return new PropertyOperation(new HealingTitanGenericDao(new TitanGraphClient()), null);
	}

	
	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		PropertyOperation.main(args);
	}

	
	@Test
	public void testConvertPropertyDataToPropertyDefinition() throws Exception {
		PropertyOperation testSubject;
		PropertyData propertyDataResult = new PropertyData();
		String propertyName = "";
		String resourceId = "";
		PropertyDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertPropertyDataToPropertyDefinition(propertyDataResult, propertyName, resourceId);
	}
	
	@Test
	public void testAddProperty() throws Exception {
		PropertyOperation testSubject;
		String propertyName = "";
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		String resourceId = "";
		Either<PropertyData, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addProperty(propertyName, propertyDefinition, resourceId);
	}

	
	@Test
	public void testValidateAndUpdateProperty() throws Exception {
		PropertyOperation testSubject;
		IComplexDefaultValue propertyDefinition = new PropertyDefinition();
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
		dataTypes.put("", new DataTypeDefinition());
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateAndUpdateProperty(propertyDefinition, dataTypes);
	}

	
	@Test
	public void testAddPropertyToGraph() throws Exception {
		PropertyOperation testSubject;
		String propertyName = "";
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		String resourceId = "";
		Either<PropertyData, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addPropertyToGraph(propertyName, propertyDefinition, resourceId);
	}

	
	@Test
	public void testAddPropertyToGraphByVertex() throws Exception {
		PropertyOperation testSubject;
		TitanVertex metadataVertex = null;
		String propertyName = "";
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		String resourceId = "";
		TitanOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addPropertyToGraphByVertex(metadataVertex, propertyName, propertyDefinition, resourceId);
	}

	
	@Test
	public void testGetTitanGenericDao() throws Exception {
		PropertyOperation testSubject;
		TitanGenericDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTitanGenericDao();
	}

	@Test
	public void testDeletePropertyFromGraph() throws Exception {
		PropertyOperation testSubject;
		String propertyId = "";
		Either<PropertyData, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deletePropertyFromGraph(propertyId);
	}

	
	@Test
	public void testUpdateProperty() throws Exception {
		PropertyOperation testSubject;
		String propertyId = "";
		PropertyDefinition newPropertyDefinition = new PropertyDefinition();
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
		Either<PropertyData, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateProperty(propertyId, newPropertyDefinition, dataTypes);
	}

	
	@Test
	public void testUpdatePropertyFromGraph() throws Exception {
		PropertyOperation testSubject;
		String propertyId = "";
		PropertyDefinition propertyDefinition = null;
		Either<PropertyData, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updatePropertyFromGraph(propertyId, propertyDefinition);
	}


	@Test
	public void testSetTitanGenericDao()  {

		PropertyOperation testSubject;
        HealingTitanGenericDao titanGenericDao = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanGenericDao(titanGenericDao);
	}

	
	@Test
	public void testAddPropertyToNodeType()  {
		PropertyOperation testSubject;
		String propertyName = "";
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		NodeTypeEnum nodeType = NodeTypeEnum.Attribute;
		String uniqueId = "";
		Either<PropertyData, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addPropertyToNodeType(propertyName, propertyDefinition, nodeType, uniqueId);
	}

	
	@Test
	public void testFindPropertiesOfNode() throws Exception {
		PropertyOperation testSubject;
		NodeTypeEnum nodeType = null;
		String uniqueId = "";
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findPropertiesOfNode(nodeType, uniqueId);
	}

	
	@Test
	public void testDeletePropertiesAssociatedToNode() throws Exception {
		PropertyOperation testSubject;
		NodeTypeEnum nodeType = null;
		String uniqueId = "";
		Either<Map<String, PropertyDefinition>, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deletePropertiesAssociatedToNode(nodeType, uniqueId);
	}

	
	@Test
	public void testDeleteAllPropertiesAssociatedToNode() throws Exception {
		PropertyOperation testSubject;
		NodeTypeEnum nodeType = null;
		String uniqueId = "";
		Either<Map<String, PropertyDefinition>, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteAllPropertiesAssociatedToNode(nodeType, uniqueId);
	}

	
	@Test
	public void testIsPropertyExist() throws Exception {
		PropertyOperation testSubject;
		List<PropertyDefinition> properties = null;
		String resourceUid = "";
		String propertyName = "";
		String propertyType = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isPropertyExist(properties, resourceUid, propertyName, propertyType);
	}

	
	@Test
	public void testValidateAndUpdateRules() throws Exception {
		PropertyOperation testSubject;
		String propertyType = "";
		List<PropertyRule> rules = null;
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		boolean isValidate = false;
		ImmutablePair<String, Boolean> result;

		// test 1
		testSubject = createTestSubject();
		rules = null;
		result = testSubject.validateAndUpdateRules(propertyType, rules, innerType, dataTypes, isValidate);
	}

	
	@Test
	public void testAddRulesToNewPropertyValue() throws Exception {
		PropertyOperation testSubject;
		PropertyValueData propertyValueData = new PropertyValueData();
		ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
		String resourceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addRulesToNewPropertyValue(propertyValueData, resourceInstanceProperty, resourceInstanceId);
	}

	
	@Test
	public void testFindPropertyValue() throws Exception {
		PropertyOperation testSubject;
		String resourceInstanceId = "";
		String propertyId = "";
		ImmutablePair<TitanOperationStatus, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findPropertyValue(resourceInstanceId, propertyId);
	}

	
	@Test
	public void testUpdateRulesInPropertyValue() throws Exception {
		PropertyOperation testSubject;
		PropertyValueData propertyValueData = new PropertyValueData();
		ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
		String resourceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.updateRulesInPropertyValue(propertyValueData, resourceInstanceProperty, resourceInstanceId);
	}

	
	@Test
	public void testGetAllPropertiesOfResourceInstanceOnlyPropertyDefId() throws Exception {
		PropertyOperation testSubject;
		String resourceInstanceUid = "";
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid);
	}

	
	@Test
	public void testRemovePropertyOfResourceInstance() throws Exception {
		PropertyOperation testSubject;
		String propertyValueUid = "";
		String resourceInstanceId = "";
		Either<PropertyValueData, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.removePropertyOfResourceInstance(propertyValueUid, resourceInstanceId);
	}

	
	@Test
	public void testRemovePropertyValueFromResourceInstance() throws Exception {
		PropertyOperation testSubject;
		String propertyValueUid = "";
		String resourceInstanceId = "";
		boolean inTransaction = false;
		Either<ComponentInstanceProperty, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.removePropertyValueFromResourceInstance(propertyValueUid, resourceInstanceId,
				inTransaction);
	}

	
	@Test
	public void testBuildResourceInstanceProperty() throws Exception {
		PropertyOperation testSubject;
		PropertyValueData propertyValueData = new PropertyValueData();
		ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
		ComponentInstanceProperty result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildResourceInstanceProperty(propertyValueData, resourceInstanceProperty);
	}

	
	@Test
	public void testIsPropertyDefaultValueValid() throws Exception {
		PropertyOperation testSubject;
		IComplexDefaultValue propertyDefinition = null;
		Map<String, DataTypeDefinition> dataTypes = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		propertyDefinition = null;
		result = testSubject.isPropertyDefaultValueValid(propertyDefinition, dataTypes);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testIsPropertyTypeValid() throws Exception {
		PropertyOperation testSubject;
		IComplexDefaultValue property = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		property = null;
		result = testSubject.isPropertyTypeValid(property);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testIsPropertyInnerTypeValid() throws Exception {
		PropertyOperation testSubject;
		IComplexDefaultValue property = null;
		Map<String, DataTypeDefinition> dataTypes = null;
		ImmutablePair<String, Boolean> result;

		// test 1
		testSubject = createTestSubject();
		property = null;
		result = testSubject.isPropertyInnerTypeValid(property, dataTypes);
	}

	
	@Test
	public void testGetAllPropertiesOfResourceInstanceOnlyPropertyDefId_1() throws Exception {
		PropertyOperation testSubject;
		String resourceInstanceUid = "";
		NodeTypeEnum instanceNodeType = null;
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid, instanceNodeType);
	}

	
	@Test
	public void testFindDefaultValueFromSecondPosition() throws Exception {
		PropertyOperation testSubject;
		List<String> pathOfComponentInstances = null;
		String propertyUniqueId = "";
		String defaultValue = "";
		Either<String, TitanOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		pathOfComponentInstances = null;
		result = testSubject.findDefaultValueFromSecondPosition(pathOfComponentInstances, propertyUniqueId,
				defaultValue);
	}

	
	@Test
	public void testUpdatePropertyByBestMatch() throws Exception {
		PropertyOperation testSubject;
		String propertyUniqueId = "";
		ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
		List<String> path = new ArrayList<>();
		path.add("path");
		instanceProperty.setPath(path);
		Map<String, ComponentInstanceProperty> instanceIdToValue = new HashMap<>();
		instanceIdToValue.put("123", instanceProperty);

		// default test
		testSubject = createTestSubject();
		testSubject.updatePropertyByBestMatch(propertyUniqueId, instanceProperty, instanceIdToValue);
	}

	
	@Test
	public void testGetDataTypeByUid() throws Exception {
		PropertyOperation testSubject;
		String uniqueId = "";
		Either<DataTypeDefinition, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeByUid(uniqueId);
	}

	
	@Test
	public void testAddDataType() throws Exception {
		PropertyOperation testSubject;
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
		Either<DataTypeDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addDataType(dataTypeDefinition);
	}

	
	@Test
	public void testGetDataTypeByName() throws Exception {
		PropertyOperation testSubject;
		String name = "";
		boolean inTransaction = false;
		Either<DataTypeDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeByName(name, inTransaction);
	}

	
	@Test
	public void testGetDataTypeByName_1() throws Exception {
		PropertyOperation testSubject;
		String name = "";
		Either<DataTypeDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeByName(name);
	}

	
	@Test
	public void testGetDataTypeByNameWithoutDerived() throws Exception {
		PropertyOperation testSubject;
		String name = "";
		Either<DataTypeDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeByNameWithoutDerived(name);
	}

	
	@Test
	public void testGetDataTypeByUidWithoutDerivedDataTypes() throws Exception {
		PropertyOperation testSubject;
		String uniqueId = "";
		Either<DataTypeDefinition, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataTypeByUidWithoutDerivedDataTypes(uniqueId);
	}

	
	@Test
	public void testIsDefinedInDataTypes() throws Exception {
		PropertyOperation testSubject;
		String propertyType = "";
		Either<Boolean, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinedInDataTypes(propertyType);
	}

	
	@Test
	public void testGetAllDataTypes() throws Exception {
		PropertyOperation testSubject;
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllDataTypes();
	}

	
	@Test
	public void testCheckInnerType() throws Exception {
		PropertyOperation testSubject;
		PropertyDataDefinition propDataDef = new PropertyDataDefinition();
		Either<String, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.checkInnerType(propDataDef);
	}

	
	@Test
	public void testGetAllDataTypeNodes() throws Exception {
		PropertyOperation testSubject;
		Either<List<DataTypeData>, TitanOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllDataTypeNodes();
	}

	
	@Test
	public void testValidateAndUpdatePropertyValue() throws Exception {
		PropertyOperation testSubject;
		String propertyType = "";
		String value = "";
		boolean isValidate = false;
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = null;
		Either<Object, Boolean> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, dataTypes);
	}

	
	@Test
	public void testValidateAndUpdatePropertyValue_1() throws Exception {
		PropertyOperation testSubject;
		String propertyType = "";
		String value = "";
		String innerType = "";
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
		dataTypes.put("", new DataTypeDefinition());
		Either<Object, Boolean> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateAndUpdatePropertyValue(propertyType, value, innerType, dataTypes);
	}

	


	
	@Test
	public void testAddPropertiesToElementType() throws Exception {
		PropertyOperation testSubject;
		String uniqueId = "";
		NodeTypeEnum elementType = null;
		List<PropertyDefinition> properties = null;
		Either<Map<String, PropertyData>, TitanOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		properties = null;
		result = testSubject.addPropertiesToElementType(uniqueId, elementType, properties);
	}

	
	@Test
	public void testUpdateDataType() throws Exception {
		PropertyOperation testSubject;
		DataTypeDefinition newDataTypeDefinition = new DataTypeDefinition();
		DataTypeDefinition oldDataTypeDefinition = new DataTypeDefinition();
		Either<DataTypeDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateDataType(newDataTypeDefinition, oldDataTypeDefinition);
	}
}
