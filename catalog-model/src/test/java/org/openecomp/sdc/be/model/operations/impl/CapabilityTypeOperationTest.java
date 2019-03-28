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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class CapabilityTypeOperationTest extends ModelTestBase {

    @Resource(name = "titan-generic-dao")
    private HealingTitanGenericDao titanDao;

    @Resource(name = "capability-type-operation")
    private CapabilityTypeOperation capabilityTypeOperation;

    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }
    
    @Before
    public void cleanUp() {
        HealingTitanGenericDao titanGenericDao = capabilityTypeOperation.titanGenericDao;
        Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
        TitanGraph graph = graphResult.left().value();

        Iterable<TitanVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<TitanVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                TitanVertex vertex = iterator.next();
                vertex.remove();
            }

        }
        titanGenericDao.commit();
    }

    @Test
    public void testDummy() {
        assertNotNull(capabilityTypeOperation);
    }

    @Test
    public void testAddCapabilityType() {

        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription("desc1");
        capabilityTypeDefinition.setType("tosca.capabilities.Container1");

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
        assertTrue("check capability type added", addCapabilityType1.isLeft());

        CapabilityTypeDefinition capabilityTypeAdded = addCapabilityType1.left().value();
        compareBetweenCreatedToSent(capabilityTypeDefinition, capabilityTypeAdded);

        Either<CapabilityTypeDefinition, TitanOperationStatus> capabilityTypeByUid = capabilityTypeOperation.getCapabilityTypeByUid(capabilityTypeAdded.getUniqueId());
        compareBetweenCreatedToSent(capabilityTypeByUid.left().value(), capabilityTypeDefinition);

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType2 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
        assertTrue("check capability type failed", addCapabilityType2.isRight());
        assertEquals("check returned error", StorageOperationStatus.SCHEMA_VIOLATION, addCapabilityType2.right().value());

    }

    @Test
    public void testAddDerviedCapabilityType() {

        CapabilityTypeDefinition capabilityTypeDefinition = createCapabilityTypeDef("tosca.capabilities.Container2", "desc1", "derivedFrom");

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
        assertEquals("check capability type parent not exist", StorageOperationStatus.NOT_FOUND, addCapabilityType1.right().value());
    }

    public CapabilityTypeDefinition createCapability(String capabilityTypeName) {

        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription("desc1");
        capabilityTypeDefinition.setType(capabilityTypeName);

        Map<String, PropertyDefinition> properties = new HashMap<>();

        String propName1 = "disk_size";
        String propName2 = "num_cpus";

        PropertyDefinition property1 = buildProperty1();

        properties.put(propName1, property1);

        PropertyDefinition property2 = buildProperty2();

        properties.put(propName2, property2);

        capabilityTypeDefinition.setProperties(properties);

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);

        CapabilityTypeDefinition capabilityTypeDefinitionCreated = addCapabilityType1.left().value();
        Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType(capabilityTypeDefinitionCreated.getUniqueId(), true);
        assertTrue("check capability type fetched", capabilityType.isLeft());
        CapabilityTypeDefinition fetchedCTD = capabilityType.left().value();

        Map<String, PropertyDefinition> fetchedProps = fetchedCTD.getProperties();

        compareProperties(fetchedProps, properties);

        return fetchedCTD;

    }

    @Test
    public void testAddCapabilityTypeWithProperties() {

        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription("desc1");
        capabilityTypeDefinition.setType("tosca.capabilities.Container3");

        Map<String, PropertyDefinition> properties = new HashMap<>();

        String propName1 = "disk_size";
        String propName2 = "num_cpus";

        PropertyDefinition property1 = buildProperty1();

        properties.put(propName1, property1);

        PropertyDefinition property2 = buildProperty2();

        properties.put(propName2, property2);

        capabilityTypeDefinition.setProperties(properties);

        Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);

        CapabilityTypeDefinition capabilityTypeDefinitionCreated = addCapabilityType1.left().value();
        Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType(capabilityTypeDefinitionCreated.getUniqueId());
        assertTrue("check capability type fetched", capabilityType.isLeft());
        CapabilityTypeDefinition fetchedCTD = capabilityType.left().value();

        Map<String, PropertyDefinition> fetchedProps = fetchedCTD.getProperties();

        compareProperties(fetchedProps, properties);
    }

    private void compareProperties(Map<String, PropertyDefinition> first, Map<String, PropertyDefinition> second) {

        assertTrue("check properties are full or empty", ((first == null && second == null) || (first != null && second != null)));
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

    @Test
    public void testGetCapabilityTypeNotFound() {

        Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType("not_exists");
        assertEquals("check not found is returned", StorageOperationStatus.NOT_FOUND, capabilityType.right().value());

    }
    
    
    
    @Test
    public void updateCapabilityType_returnNotFoundErrorIfTryingToUpdateANonExistingType() {
        CapabilityTypeDefinition currType = createCapabilityTypeDef();
        CapabilityTypeDefinition updatedType = createCapabilityTypeDef();
        Either<CapabilityTypeDefinition, StorageOperationStatus> updateCapabilityTypeRes = capabilityTypeOperation.updateCapabilityType(updatedType, currType);
        assertThat(updateCapabilityTypeRes.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void updateCapabilityType_basicFields() {
        CapabilityTypeDefinition createdType = createCapabilityTypeDef("type1", "description1");
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(createdType);

        CapabilityTypeDefinition updatedType = createCapabilityTypeDef("type1", "description2");
        capabilityTypeOperation.updateCapabilityType(updatedType, currCapabilityType.left().value());

        Either<CapabilityTypeDefinition, StorageOperationStatus> fetchedUpdatedType = capabilityTypeOperation.getCapabilityType(createdType.getType());
        CapabilityTypeDefinition fetchedCapabilityType = fetchedUpdatedType.left().value();
        assertThat(fetchedCapabilityType.getProperties()).isNullOrEmpty();
        assertThat(fetchedCapabilityType.getDerivedFrom()).isNullOrEmpty();
        assertEquals(fetchedCapabilityType.getCreationTime(), updatedType.getCreationTime());
        assertEquals(fetchedCapabilityType.getType(), updatedType.getType());
        assertEquals(fetchedCapabilityType.getDescription(), updatedType.getDescription());
        assertEquals(fetchedCapabilityType.getValidSourceTypes(), updatedType.getValidSourceTypes());
        assertEquals(fetchedCapabilityType.getVersion(), updatedType.getVersion());
    }

    @Test
    public void updateCapabilityType_updatePropertiesFailedDueTypeChange() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        CapabilityTypeDefinition capabilityType = createCapabilityTypeDef(asMap(prop1));
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType);

        PropertyDefinition updatedProp1 = duplicateProperty(prop1, "newVal1", "int");
        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef(asMap(updatedProp1, prop3));

        Either<CapabilityTypeDefinition, StorageOperationStatus> updatedCapabilityTypeRes = 
                capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());
        
        assertTrue(updatedCapabilityTypeRes.isRight());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, updatedCapabilityTypeRes.right().value());

        Either<CapabilityTypeDefinition, StorageOperationStatus> fetchedUpdatedType = capabilityTypeOperation.getCapabilityType(capabilityType.getType());
        assertEquals(fetchedUpdatedType.left().value().getProperties(), asMap(prop1));

    }
    
    @Test
    public void updateCapabilityType_updatePropertiesFailedDueDeletedProp() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        CapabilityTypeDefinition capabilityType = createCapabilityTypeDef(asMap(prop1));
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType);

        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef(asMap(prop3));

        Either<CapabilityTypeDefinition, StorageOperationStatus> updatedCapabilityTypeRes = 
                capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());
        
        assertTrue(updatedCapabilityTypeRes.isRight());
        assertEquals(StorageOperationStatus.MATCH_NOT_FOUND, updatedCapabilityTypeRes.right().value());

        Either<CapabilityTypeDefinition, StorageOperationStatus> fetchedUpdatedType = capabilityTypeOperation.getCapabilityType(capabilityType.getType());
        assertEquals(fetchedUpdatedType.left().value().getProperties(), asMap(prop1));

    }
    
    @Test
    public void updateCapabilityType_updateProperties() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        CapabilityTypeDefinition capabilityType = createCapabilityTypeDef(asMap(prop1));
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType);

        PropertyDefinition updatedProp1 = duplicateProperty(prop1, "newVal1", "string");
        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef(asMap(updatedProp1, prop3));

        Either<CapabilityTypeDefinition, StorageOperationStatus> updatedCapabilityTypeRes = 
                capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());
        assertTrue(updatedCapabilityTypeRes.isLeft());
       
        Either<CapabilityTypeDefinition, StorageOperationStatus> fetchedUpdatedType = capabilityTypeOperation.getCapabilityType(capabilityType.getType());
        assertEquals(fetchedUpdatedType.left().value().getProperties(), asMap(updatedProp1, prop3));

    }

    @Test
    public void updateCapabilityType_derivedFrom_whenNoPrevDerivedFrom_updateToNewDerivedFrom() {
        CapabilityTypeDefinition rootCapabilityType = createCapabilityTypeDef();
        CapabilityTypeDefinition capabilityType1 = createCapabilityTypeDef("type1", "descr");
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef("type1", "descr", rootCapabilityType.getType());
        capabilityTypeOperation.addCapabilityType(rootCapabilityType);
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType1);
        capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());

        Either<CapabilityTypeDefinition, StorageOperationStatus> latestCapabilityType = capabilityTypeOperation.getCapabilityType(capabilityType1.getType());
        assertThat(latestCapabilityType.left().value().getDerivedFrom()).isEqualTo(rootCapabilityType.getType());
        verifyDerivedFromNodeEqualsToRootCapabilityType(rootCapabilityType, latestCapabilityType.left().value().getUniqueId());
    }

    @Test
    public void updateCapabilityType_derivedFrom_updateToNullDerivedFrom_Failed() {
        CapabilityTypeDefinition rootCapabilityType = createCapabilityTypeDef();
        CapabilityTypeDefinition capabilityType1 = createCapabilityTypeDef("type1", "descr", rootCapabilityType.getType());
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef("type1", "descr", null, new HashMap<>());
        capabilityTypeOperation.addCapabilityType(rootCapabilityType);
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType1);

        Either<CapabilityTypeDefinition, StorageOperationStatus> updateRes = 
                capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());
        
        assertThat(updateRes.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);

        Either<CapabilityTypeDefinition, StorageOperationStatus> latestCapabilityType = capabilityTypeOperation.getCapabilityType(capabilityType1.getType());
        assertThat(latestCapabilityType.left().value().getDerivedFrom()).isEqualTo(rootCapabilityType.getType());
    }

    @Test
    public void updateCapabilityType_updateDerivedFrom() {
        CapabilityTypeDefinition rootCapabilityType = createCapabilityTypeDef();
        CapabilityTypeDefinition derivedType1 = createCapabilityTypeDef("derivedType1", "descr", rootCapabilityType.getType());
        CapabilityTypeDefinition capabilityType1 = createCapabilityTypeDef("type1", "descr", rootCapabilityType.getType());
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef("type1", "descr", derivedType1.getType());

        capabilityTypeOperation.addCapabilityType(rootCapabilityType);
        capabilityTypeOperation.addCapabilityType(derivedType1);
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType1);

        capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());

        Either<CapabilityTypeDefinition, StorageOperationStatus> latestCapabilityType = capabilityTypeOperation.getCapabilityType(capabilityType1.getType());
        assertThat(latestCapabilityType.left().value().getDerivedFrom()).isEqualTo(derivedType1.getType());
        verifyDerivedFromNodeEqualsToRootCapabilityType(derivedType1, latestCapabilityType.left().value().getUniqueId());
    }
    
    @Test
    public void updateCapabilityType_updateDerivedFrom_Failed_NewParentIsNotChildOfOldOne() {
        CapabilityTypeDefinition rootCapabilityType = createCapabilityTypeDef();
        CapabilityTypeDefinition notDerivedType = createCapabilityTypeDef("derivedType1", "descr");
        CapabilityTypeDefinition capabilityType1 = createCapabilityTypeDef("type1", "descr", rootCapabilityType.getType());
        CapabilityTypeDefinition updatedCapabilityType = createCapabilityTypeDef("type1", "descr", notDerivedType.getType());

        capabilityTypeOperation.addCapabilityType(rootCapabilityType);
        capabilityTypeOperation.addCapabilityType(notDerivedType);
        Either<CapabilityTypeDefinition, StorageOperationStatus> currCapabilityType = capabilityTypeOperation.addCapabilityType(capabilityType1);

        Either<CapabilityTypeDefinition, StorageOperationStatus> result = capabilityTypeOperation.updateCapabilityType(updatedCapabilityType, currCapabilityType.left().value());
        assertThat(result.right().value()).isEqualTo(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY);

        Either<CapabilityTypeDefinition, StorageOperationStatus> latestCapabilityType = capabilityTypeOperation.getCapabilityType(capabilityType1.getType());
        assertThat(latestCapabilityType.left().value().getDerivedFrom()).isEqualTo(rootCapabilityType.getType());
        verifyDerivedFromNodeEqualsToRootCapabilityType(rootCapabilityType, latestCapabilityType.left().value().getUniqueId());
    }
    
    private CapabilityTypeDefinition createCapabilityTypeDef() {
        return createCapabilityTypeDef("tosca.capabilities.Root", "The TOSCA root Capability Type all other TOSCA base Capability Types derived from", null, new HashMap<>());
    }

    private CapabilityTypeDefinition createCapabilityTypeDef(Map<String, PropertyDefinition> properties) {
        return createCapabilityTypeDef("tosca.capabilities.Root",
                "The TOSCA root Capability Type all other TOSCA base Capability Types derived from", null, properties);
    }
    
    private CapabilityTypeDefinition createCapabilityTypeDef(String type, String description) {
        return createCapabilityTypeDef(type, description, null, null);
    }

    private CapabilityTypeDefinition createCapabilityTypeDef(String type, String description, String derivedFrom) {
        return createCapabilityTypeDef(type, description, derivedFrom, null);
    }

    
    private CapabilityTypeDefinition createCapabilityTypeDef(String type, String description, String derivedFrom,  Map<String, PropertyDefinition> properties) {
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        capabilityTypeDefinition.setDescription(description);
        capabilityTypeDefinition.setType(type);
        capabilityTypeDefinition.setDerivedFrom(derivedFrom);
        capabilityTypeDefinition.setVersion("1.0");
        capabilityTypeDefinition.setValidSourceTypes(null);
        if (properties != null) {
            capabilityTypeDefinition.setProperties(properties);
        }
        return capabilityTypeDefinition;
    }

    private PropertyDefinition duplicateProperty(PropertyDefinition prop1, String updatedValue, String updatedType) {
        PropertyDefinition updatedProp1 = new PropertyDefinition(prop1);
        updatedProp1.setDefaultValue(updatedValue);
        updatedProp1.setType(updatedType);
        return updatedProp1;
    }

    private static Map<String, PropertyDefinition> asMap(PropertyDefinition ... props) {
        return Stream.of(props).collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
    }

    private void verifyDerivedFromNodeEqualsToRootCapabilityType(CapabilityTypeDefinition rootCapabilityType, String parentCapabilityId) {
        Either<ImmutablePair<CapabilityTypeData, GraphEdge>, TitanOperationStatus> derivedFromRelation = titanDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), parentCapabilityId, GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
        assertThat(derivedFromRelation.left().value().getLeft().getCapabilityTypeDataDefinition())
                .isEqualToComparingFieldByField(rootCapabilityType);
    }


    private void comparePropertyDefinition(PropertyDefinition first, PropertyDefinition second) {

        assertTrue("check objects are full or empty", ((first == null && second == null) || (first != null && second != null)));
        if (first != null) {
            assertTrue("check property default value", compareValue(first.getDefaultValue(), second.getDefaultValue()));
            assertTrue("check property description", compareValue(first.getDescription(), second.getDescription()));
            assertTrue("check property type", compareValue(first.getType(), second.getType()));
            compareList(first.getConstraints(), second.getConstraints());
        }

    }

    private void compareList(List<PropertyConstraint> first, List<PropertyConstraint> second) {

        assertTrue("check lists are full or empty", ((first == null && second == null) || (first != null && second != null)));
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
        range.add("1");
        range.add("4");

        InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
        constraints3.add(propertyConstraint3);
        // property2.setConstraints(constraints3);
        property2.setConstraints(constraints3);
        return property2;
    }

    private PropertyDefinition buildProperty1() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setDefaultValue("10");
        property1.setDescription("Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
        property1.setType(ToscaType.INTEGER.name().toLowerCase());
        List<PropertyConstraint> constraints = new ArrayList<>();
        GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
        constraints.add(propertyConstraint1);

        LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
        constraints.add(propertyConstraint2);

        property1.setConstraints(constraints);
        return property1;
    }

    private void compareBetweenCreatedToSent(CapabilityTypeDefinition x, CapabilityTypeDefinition y) {
        assertTrue(compareValue(x.getDerivedFrom(), y.getDerivedFrom()));
        assertTrue(compareValue(x.getType(), y.getType()));
        assertTrue(compareValue(x.getDescription(), y.getDescription()));

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
}
