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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fj.data.Either;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.RelationshipTypeData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class RelationshipTypeOperationTest extends ModelTestBase {

    private static final String PROP = "prop";

    @Mock
    HealingJanusGraphGenericDao janusGraphGenericDao;

    @Mock
    PropertyOperation propertyOperation;

    @Mock
    DerivedFromOperation derivedFromOperation;

    @InjectMocks
    @Spy
    private RelationshipTypeOperation relationshipTypeOperation;

    private RelationshipTypeDefinition relationshipTypeDefinition = new RelationshipTypeDefinition();

    {
        relationshipTypeDefinition.setDescription("desc1");
        relationshipTypeDefinition.setType("tosca.relationships.Container1");
        relationshipTypeDefinition.setDerivedFrom("tosca.relationships.Root");
        relationshipTypeDefinition.setProperties(createPropertyData("prop1"));
        relationshipTypeDefinition.setUniqueId("tosca.relationships.Container1");
    }

    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.doReturn(JanusGraphOperationStatus.OK).when(janusGraphGenericDao).commit();
        Mockito.doReturn(JanusGraphOperationStatus.OK).when(janusGraphGenericDao).rollback();
    }

    @Test
    public void getRelationshipTypeByNameNotCreated() {
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_CREATED))
                .when(relationshipTypeOperation).getRelationshipTypeByUid(Mockito.anyString());

        Either<RelationshipTypeDefinition, JanusGraphOperationStatus> either =
                relationshipTypeOperation.getRelationshipTypeByName("name");
        assertTrue(either.isRight());
    }

    @Test
    public void testDummy() {
        assertNotNull(relationshipTypeOperation);
    }

    @Test
    public void testAddRelationshipTypeValidationFailStatusNullInTransactionFalse() {
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_CONNECTED))
                .when(propertyOperation)
                .getAllTypePropertiesFromAllDerivedFrom(Mockito.anyString(), Mockito.any(), Mockito.any());


        Either<RelationshipTypeDefinition, StorageOperationStatus> addRelationshipType =
                relationshipTypeOperation.addRelationshipType(relationshipTypeDefinition, false);

        assertTrue(addRelationshipType.isRight());
    }

    @Test
    public void testAddRelationshipTypeValidationFailStatusPropertiesReturnedInTransactionFalse() {
        Mockito.doReturn(Either.left(Collections.singletonMap("prop1", new PropertyDefinition()))).when(propertyOperation)
                .getAllTypePropertiesFromAllDerivedFrom(Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(propertyOperation)
                .validatePropertiesUniqueness(Mockito.any(), Mockito.any());

        Either<RelationshipTypeDefinition, StorageOperationStatus> addRelationshipType =
                relationshipTypeOperation.addRelationshipType(relationshipTypeDefinition, false);

        assertTrue(addRelationshipType.isRight());
    }

    @Test
    public void testGetAllRelationshipTypesNotFound() {
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(
            janusGraphGenericDao).getByCriteria(NodeTypeEnum.RelationshipType, null,
                RelationshipTypeData.class);
        Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> either = relationshipTypeOperation.getAllRelationshipTypes();

        assertTrue(either.isLeft() && MapUtils.isEmpty(either.left().value()));
    }

    @Test
    public void testGetAllRelationshipTypesNotConnnected() {
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_CONNECTED)).when(
            janusGraphGenericDao).getByCriteria(NodeTypeEnum.RelationshipType, null,
                RelationshipTypeData.class);
        Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> either = relationshipTypeOperation.getAllRelationshipTypes();

        assertTrue(either.isRight() && JanusGraphOperationStatus.NOT_CONNECTED == either.right().value());
    }

    @Test
    public void testGetAllRelationshipTypesSuccess() {
        List<RelationshipTypeData> relationshipTypeDataList = new ArrayList<>();

        RelationshipTypeData relationshipTypeData1 = new RelationshipTypeData();
        RelationshipInstDataDefinition relationshipInstDataDefinition1 = new RelationshipInstDataDefinition();
        relationshipInstDataDefinition1.setUniqueId("tosca.relationships.Root1");
        relationshipInstDataDefinition1.setType("tosca.relationships.Root1");
        relationshipTypeData1.setRelationshipTypeDataDefinition(relationshipInstDataDefinition1);

        relationshipTypeDataList.add(relationshipTypeData1);

        Mockito.doReturn(Either.left(relationshipTypeDataList))
                .when(janusGraphGenericDao).getByCriteria(NodeTypeEnum.RelationshipType, null,
                RelationshipTypeData.class);

        Mockito.doReturn(Either.left(relationshipTypeData1)).when(janusGraphGenericDao)
                .getNode(Mockito.anyString(), Mockito.anyString(), Mockito.eq(RelationshipTypeData.class));

        Mockito.doReturn(Either.left(createPropertyData("prop1"))).when(propertyOperation)
                .findPropertiesOfNode(NodeTypeEnum.RelationshipType, "tosca.relationships.Root1");

        RelationshipInstDataDefinition derivedFromRelationshipTypeDefinition = new RelationshipInstDataDefinition();
        derivedFromRelationshipTypeDefinition.setUniqueId("tosca.relationships.Root1");
        derivedFromRelationshipTypeDefinition.setType("tosca.relationships.Parent");

        Mockito.doReturn(Either.left(new RelationshipTypeData(derivedFromRelationshipTypeDefinition)))
                .when(derivedFromOperation)
                .getDerivedFromChild("tosca.relationships.Root1", NodeTypeEnum.RelationshipType, RelationshipTypeData.class);

        Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> either =
                                            relationshipTypeOperation.getAllRelationshipTypes();

        assertTrue(either.isLeft());
        RelationshipTypeDefinition relationshipTypeDefinition = either.left().value().get("tosca.relationships.Root1");
        assertEquals("tosca.relationships.Parent", relationshipTypeDefinition.getDerivedFrom());
    }

    public RelationshipTypeDefinition createRelationship(String relationshipTypeName) {

        RelationshipTypeDefinition relationshipTypeDefinition = new RelationshipTypeDefinition();
        relationshipTypeDefinition.setDescription("desc1");
        relationshipTypeDefinition.setType(relationshipTypeName);

        Map<String, PropertyDefinition> properties = new HashMap<>();

        String propName1 = "disk_size";
        String propName2 = "num_cpus";

        PropertyDefinition property1 = buildProperty1();

        properties.put(propName1, property1);

        PropertyDefinition property2 = buildProperty2();

        properties.put(propName2, property2);

        relationshipTypeDefinition.setProperties(properties);

        Either<RelationshipTypeDefinition, StorageOperationStatus> addRelationshipType1 =
                relationshipTypeOperation.addRelationshipType(relationshipTypeDefinition, true);

        RelationshipTypeDefinition relationshipTypeDefinitionCreated = addRelationshipType1.left().value();
        Either<RelationshipTypeDefinition, StorageOperationStatus> relationshipType =
                relationshipTypeOperation.getRelationshipType(relationshipTypeDefinitionCreated.getUniqueId(), true);
        assertTrue("check relationship type fetched", relationshipType.isLeft());
        RelationshipTypeDefinition fetchedCTD = relationshipType.left().value();

        Map<String, PropertyDefinition> fetchedProps = fetchedCTD.getProperties();

        compareProperties(fetchedProps, properties);

        return fetchedCTD;

    }

    private void compareProperties(Map<String, PropertyDefinition> first, Map<String, PropertyDefinition> second) {

        assertTrue("check properties are full or empty",
                ((first == null && second == null) || (first != null && second != null)));
        if (first != null) {
            assertEquals("check properties size", first.size(), second.size());

            for (Entry<String, PropertyDefinition> entry : first.entrySet()) {

                String propName = entry.getKey();
                PropertyDefinition secondPD = second.get(propName);
                assertNotNull("Cannot find property " + propName + " in " + second, secondPD);

                PropertyDefinition firstPD = entry.getValue();

                comparePropertyDefinition(firstPD, secondPD);
            }

        }

    }

    private void comparePropertyDefinition(PropertyDefinition first, PropertyDefinition second) {

        assertTrue("check objects are full or empty",
                ((first == null && second == null) || (first != null && second != null)));
        if (first != null) {
            assertTrue("check property description", compareValue(first.getDescription(), second.getDescription()));
            assertTrue("check property default value", compareValue((String) first.getDefaultValue(),
                    (String) second.getDefaultValue()));
            assertTrue("check property type", compareValue(first.getType(), second.getType()));
            compareList(first.getConstraints(), second.getConstraints());
        }

    }

    private void compareList(List<PropertyConstraint> first, List<PropertyConstraint> second) {

        assertTrue("check lists are full or empty",
                ((first == null && second == null) || (first != null && second != null)));
        if (first != null) {
            assertEquals("check list size", first.size(), second.size());
        }
    }

    private PropertyDefinition buildProperty2() {
        PropertyDefinition property2 = new PropertyDefinition();
        property2.setDefaultValue("2");
        property2.setDescription("Number of (actual or virtual) CPUs associated with the Compute node.");
        property2.setType(ToscaType.INTEGER.name().toLowerCase());
        List<PropertyConstraint> constraints3 = new ArrayList<>();
        List<String> range = new ArrayList<>();
        range.add("4");
        range.add("1");
        InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
        constraints3.add(propertyConstraint3);
        property2.setConstraints(constraints3);
        return property2;
    }

    private PropertyDefinition buildProperty1() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setDefaultValue("10");
        property1.setDescription("Size of the local disk, in Gigabytes (GB), "
                + "available to applications running on the Compute node.");
        property1.setType(ToscaType.INTEGER.name().toLowerCase());
        List<PropertyConstraint> constraints = new ArrayList<>();
        GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
        constraints.add(propertyConstraint1);

        LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
        constraints.add(propertyConstraint2);

        property1.setConstraints(constraints);
        return property1;
    }

    private boolean compareValue(String first, String second) {

        if (first == null && second == null) {
            return true;
        }
        if (first != null) {
            return first.equals(second);
        } else {
            return false;
        }
    }

    public void setOperations(RelationshipTypeOperation relationshipTypeOperation) {
        this.relationshipTypeOperation = relationshipTypeOperation;
    }

    @Test
    public void testAddRelationshipType() {

        RelationshipTypeData relationshipTypeData = new RelationshipTypeData();
        RelationshipInstDataDefinition relationshipInstDataDefinition1 = new RelationshipInstDataDefinition();
        relationshipInstDataDefinition1.setUniqueId("tosca.relationships.Root");
        relationshipInstDataDefinition1.setType("tosca.relationships.Root");
        relationshipTypeData.setRelationshipTypeDataDefinition(relationshipInstDataDefinition1);

        RelationshipTypeDefinition relationshipTypeDefinition = new RelationshipTypeDefinition(relationshipTypeData);
        relationshipTypeDefinition.setProperties(createPropertyData("prop1"));
        relationshipTypeDefinition.setDerivedFrom("tosca.relationships.Root");

        Mockito.doReturn(Either.left(Collections.singletonMap("prop1", new PropertyDefinition()))).when(propertyOperation)
                .getAllTypePropertiesFromAllDerivedFrom(Mockito.anyString(), Mockito.any(), Mockito.any());

        Mockito.doReturn(Either.left(new ArrayList<>(relationshipTypeDefinition.getProperties().values()))).when(propertyOperation)
                .validatePropertiesUniqueness(Mockito.any(), Mockito.any());

        Mockito.doReturn(Either.left(relationshipTypeData)).when(janusGraphGenericDao)
                .createNode(Mockito.any(), Mockito.eq(RelationshipTypeData.class));

        Mockito.doReturn(Either.left(new HashMap())).when(propertyOperation)
                .addPropertiesToElementType(Mockito.anyString(), Mockito.any(), Mockito.anyMap());

        Mockito.doReturn(Either.left(relationshipTypeDefinition))
                .when(relationshipTypeOperation).getRelationshipTypeByUid(Mockito.anyString());

        Mockito.doReturn(Either.left(new GraphRelation()))
                .when(derivedFromOperation)
                .addDerivedFromRelation(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        Mockito.doReturn(Either.left(relationshipTypeDefinition))
                .when(relationshipTypeOperation).getRelationshipType(Mockito.anyString(), Mockito.anyBoolean());

        Either<RelationshipTypeDefinition, StorageOperationStatus> either =
                relationshipTypeOperation.addRelationshipType(relationshipTypeDefinition, true);

        assertTrue(either.isLeft());
    }

    @Test
    public void testGetRelationshipTypeNotConnected() {
        Mockito.doReturn(Either.right(JanusGraphOperationStatus.NOT_CONNECTED))
                .when(relationshipTypeOperation).getRelationshipTypeByUid(Mockito.anyString());

        Either<RelationshipTypeDefinition, StorageOperationStatus> either =
                relationshipTypeOperation.getRelationshipType(Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(either.isRight());
    }

    @Test
    public void testGetRelationshipTypeSuccess() {
        Mockito.doReturn(Either.left(relationshipTypeDefinition))
                .when(relationshipTypeOperation).getRelationshipTypeByUid(Mockito.anyString());

        Either<RelationshipTypeDefinition, StorageOperationStatus> either =
                relationshipTypeOperation.getRelationshipType(Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(either.isLeft());
    }

    @Test
    public void testUpdateRelationshipType() {
        RelationshipTypeDefinition newRelationshipTypeDefinition = new RelationshipTypeDefinition();
        newRelationshipTypeDefinition.setUniqueId("tosca.relationships.Container2");
        newRelationshipTypeDefinition.setDescription("desc2");
        newRelationshipTypeDefinition.setType("tosca.relationships.Container2");
        newRelationshipTypeDefinition.setDerivedFrom("tosca.relationships.Root");
        newRelationshipTypeDefinition.setProperties(createPropertyData("prop1"));

        Mockito.doReturn(Either.left(new RelationshipTypeData(newRelationshipTypeDefinition))).when(
            janusGraphGenericDao)
                .updateNode(Mockito.any(), Mockito.eq(RelationshipTypeData.class));

        Mockito.doReturn(Either.left(newRelationshipTypeDefinition.getProperties()))
                .when(propertyOperation).deletePropertiesAssociatedToNode(Mockito.any(), Mockito.anyString());

        Mockito.doReturn(Either.left(newRelationshipTypeDefinition.getProperties()))
                .when(propertyOperation).addPropertiesToElementType(Mockito.anyString(), Mockito.any(), Mockito.anyMap());

        Mockito.doReturn(Either.left(newRelationshipTypeDefinition)).when(relationshipTypeOperation)
                .getRelationshipTypeByUid(Mockito.anyString());

        Mockito.doReturn(StorageOperationStatus.OK).when(derivedFromOperation)
                .removeDerivedFromRelation(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        Mockito.doReturn(Either.left(new GraphRelation()))
                .when(derivedFromOperation)
                .addDerivedFromRelation(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        Either<RelationshipTypeDefinition, StorageOperationStatus> either =
                relationshipTypeOperation.updateRelationshipType(relationshipTypeDefinition,
                        newRelationshipTypeDefinition, false);

        assertTrue(either.isLeft());
    }

    @Test
    public void testGetRelationshipTypeByUid() {
        RelationshipTypeData relationshipTypeData = new RelationshipTypeData(relationshipTypeDefinition);

        Mockito.doReturn(Either.left(relationshipTypeData)).when(janusGraphGenericDao)
                .getNode(Mockito.anyString(), Mockito.any(), Mockito.eq(RelationshipTypeData.class));

        Mockito.doReturn(Either.left(relationshipTypeDefinition.getProperties()))
                .when(propertyOperation).findPropertiesOfNode(Mockito.any(), Mockito.anyString());

        RelationshipTypeDefinition childRelationshipTypeDefinition = new RelationshipTypeDefinition();
        childRelationshipTypeDefinition.setType("tosca.relationships.ContainerChild");

        Mockito.doReturn(Either.left(new ImmutablePair(new RelationshipTypeData(childRelationshipTypeDefinition), null))).when(
            janusGraphGenericDao)
                .getChild(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(),
                        Mockito.eq(RelationshipTypeData.class));

        Either<RelationshipTypeDefinition, JanusGraphOperationStatus> either =
                relationshipTypeOperation.getRelationshipTypeByUid("tosca.relationships.Container1");

        assertTrue(either.isLeft()
                && "tosca.relationships.ContainerChild".equals(either.left().value().getDerivedFrom()));
    }

    private Map<String, PropertyDefinition> createPropertyData(String value) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setDefaultValue(value);
        propertyDefinition.setDescription(PROP + "_" + value);
        propertyDefinition.setType(ToscaType.INTEGER.name().toLowerCase());
        List<PropertyConstraint> constraints = new ArrayList<>();
        List<String> range = new ArrayList<>();
        range.add("1");
        range.add("4");
        InRangeConstraint propertyConstraint = new InRangeConstraint(range);
        constraints.add(propertyConstraint);
        propertyDefinition.setConstraints(constraints);
        Map<String, PropertyDefinition> propertiesMap = new HashMap<>();
        propertiesMap.put(PROP, propertyDefinition);
        return propertiesMap;
    }

}
