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

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.PolicyTypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class PolicyTypeOperationTest extends ModelTestBase {

    private static final String NULL_STRING = null;
    @Autowired
    private PolicyTypeOperation policyTypeOperation;

    @Autowired
    private HealingJanusGraphGenericDao janusGraphGenericDao;

    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();

    }

    @Before
    public void cleanUp() {
        JanusGraphGenericDao janusGraphGenericDao = policyTypeOperation.janusGraphGenericDao;
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphGenericDao.getGraph();
        JanusGraph graph = graphResult.left().value();

        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }

        }
        janusGraphGenericDao.commit();
    }

    @Test
    public void testAddPolicyType() {
        PolicyTypeDefinition policyTypePreCreate = createPolicyTypeDef();
        assertTrue(StringUtils.isEmpty(policyTypePreCreate.getUniqueId()));
        Either<PolicyTypeDefinition, StorageOperationStatus> addPolicyType = policyTypeOperation.addPolicyType(policyTypePreCreate);
        assertTrue(addPolicyType.isLeft());
        PolicyTypeDefinition policyTypePostCreate = addPolicyType.left().value();
        assertThat(policyTypePostCreate.getUniqueId()).isNotEmpty();
        assertThat(policyTypePostCreate)
                .isEqualToComparingOnlyGivenFields(policyTypePreCreate, "name", "icon", "description", "type");
    }

    @Test
    public void testGetLatestPolicyTypeByType() {
        PolicyTypeDefinition policyTypeCreated = policyTypeOperation.addPolicyType(createPolicyTypeDef()).left().value();
        Either<PolicyTypeDefinition, StorageOperationStatus> eitherPolicyTypeFetched = policyTypeOperation.getLatestPolicyTypeByType(policyTypeCreated.getType());
        assertTrue(eitherPolicyTypeFetched.isLeft());
        PolicyTypeDefinition policyTypeFetched = eitherPolicyTypeFetched.left().value();
        assertEquals(policyTypeFetched.toString(), policyTypeCreated.toString());
    }

    @Test
    public void testGetLatestPolicyTypeByType_derivedFromFetchedCorrectly() {
        PolicyTypeDefinition rootPolicyType = createRootPolicyTypeOnGraph();
        String derivedFromRootType = rootPolicyType.getType();
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("tosca.policies.type1", "desc1", derivedFromRootType);
        policyTypeOperation.addPolicyType(policyType1);
        Either<PolicyTypeDefinition, StorageOperationStatus> eitherPolicyTypeFetched = policyTypeOperation.getLatestPolicyTypeByType(policyType1.getType());
        assertThat(eitherPolicyTypeFetched.left().value().getDerivedFrom()).isEqualTo(rootPolicyType.getType());
    }

    @Test
    public void testGetLatestPolicyTypeByType_whenGettingTypeGetPropertiesFromAllDerivedFromChain_policyTypeHasNoDirectProps() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        PropertyDefinition prop2 = createSimpleProperty("val2", "prop2", "string");
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("tosca.policies.type1", null, prop1, prop2);
        PolicyTypeDefinition policyType2 = createPolicyTypeDef("tosca.policies.type2", "desc3", policyType1.getType(), null);
        addPolicyTypesToDB(policyType1, policyType2);
        Either<PolicyTypeDefinition, StorageOperationStatus> latestPolicyType2 = policyTypeOperation.getLatestPolicyTypeByType(policyType2.getType());
        assertThat(latestPolicyType2.isLeft()).isTrue();
        assertThat(latestPolicyType2.left().value().getProperties())
                .usingElementComparatorOnFields("defaultValue", "name", "type")
                .containsExactlyInAnyOrder(prop1, prop2);
    }

    @Test
    public void testGetLatestPolicyTypeByType_whenGettingTypeGetPropertiesFromAllDerivedFromChain() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        PropertyDefinition prop2 = createSimpleProperty("val2", "prop2", "string");
        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");

        PolicyTypeDefinition rootPolicyType = createPolicyTypeDef(prop1);
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("tosca.policies.type1", "desc1", rootPolicyType.getType(), null);
        PolicyTypeDefinition policyType2 = createPolicyTypeDef("tosca.policies.type2", "desc2", policyType1.getType(), prop2);
        PolicyTypeDefinition policyType3 = createPolicyTypeDef("tosca.policies.type3", "desc3", policyType2.getType(), null);
        PolicyTypeDefinition policyType4 = createPolicyTypeDef("tosca.policies.type4", "desc4", policyType3.getType(), prop3);

        addPolicyTypesToDB(rootPolicyType, policyType1, policyType2, policyType3, policyType4);

        Either<PolicyTypeDefinition, StorageOperationStatus> latestPolicyType3 = policyTypeOperation.getLatestPolicyTypeByType(policyType4.getType());
        assertThat(latestPolicyType3.isLeft()).isTrue();
        assertThat(latestPolicyType3.left().value().getProperties())
                .usingElementComparatorOnFields("defaultValue", "name", "type")
                .containsExactlyInAnyOrder(prop1, prop2, prop3);
    }

    @Test(expected = StorageException.class)
    public void getAllPolicyTypes_noPolicies() {
        policyTypeOperation.getAllPolicyTypes(null);
    }

    @Test
    public void getAllPolicyTypes() {
        PolicyTypeDefinition policyType1 = createPolicyTypeDef();
        PolicyTypeDefinition policyType2 = createPolicyTypeDef("tosca.policies.test1", "desc1", "tosca.policies.Root");
        addPolicyTypesToDB(policyType1, policyType2);
        List<PolicyTypeDefinition> allPolicyTypesWithNoExcluded = policyTypeOperation.getAllPolicyTypes(null);
        assertThat(allPolicyTypesWithNoExcluded).hasSize(2);
        assertThat(allPolicyTypesWithNoExcluded).usingElementComparatorOnFields("uniqueId", "description", "version", "type")
                .containsExactlyInAnyOrder(policyType1, policyType2);
    }

    @Test
    public void getAllPolicyTypes_whenPassingExcludedTypeList_dontReturnExcludedTypes() {
        PolicyTypeDefinition policyType1 = createPolicyTypeDef();
        PolicyTypeDefinition policyType2 = createPolicyTypeDef("tosca.policies.test1", "desc1", "tosca.policies.Root");
        PolicyTypeDefinition policyType3 = createPolicyTypeDef("tosca.policies.test2", "desc2", "tosca.policies.Root");
        policyTypeOperation.addPolicyType(policyType1);
        policyTypeOperation.addPolicyType(policyType2);
        policyTypeOperation.addPolicyType(policyType3);
        List<PolicyTypeDefinition> allPolicyTypes = policyTypeOperation.getAllPolicyTypes(newHashSet("tosca.policies.test1", "tosca.policies.test2"));
        assertThat(allPolicyTypes).hasSize(1);
        assertThat(allPolicyTypes).usingElementComparatorOnFields("type")
                                                 .containsExactly(policyType1);
    }

    @Test
    public void addPolicyType_whenDerivedFromNodeNotExist_returnNotFound() {
        PolicyTypeDefinition type1 = createPolicyTypeDef("tosca.policies.type1", "desc1", "derivedFrom");
        Either<PolicyTypeDefinition, StorageOperationStatus> addedPolicyTypeResult = policyTypeOperation.addPolicyType(type1);
        assertThat(addedPolicyTypeResult.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test//bug379696
    public void addPolicyType_derivedFromAddedCorrectly() {
        PolicyTypeDefinition rootPolicyType = createRootPolicyTypeOnGraph();
        String derivedFromRootType = rootPolicyType.getType();
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("tosca.policies.type1", "desc1", derivedFromRootType);
        Either<PolicyTypeDefinition, StorageOperationStatus> addedPolicyTypeResult = policyTypeOperation.addPolicyType(policyType1);
        assertThat(addedPolicyTypeResult.isLeft()).isTrue();

        Either<PolicyTypeDefinition, StorageOperationStatus> fetchedPolicyType = policyTypeOperation.getLatestPolicyTypeByType(policyType1.getType());
        PolicyTypeDefinition fetchedPolicyTypeVal = fetchedPolicyType.left().value();
        assertThat(fetchedPolicyTypeVal.getDerivedFrom()).isEqualTo(derivedFromRootType);
        verifyDerivedFromNodeEqualsToRootPolicyType(rootPolicyType, fetchedPolicyTypeVal.getUniqueId());

    }

    @Test
    public void updatePolicyType_returnNotFoundErrorIfTryingToUpdateANonExistingType() {
        PolicyTypeDefinition currType = createPolicyTypeDef();
        PolicyTypeDefinition updatedType = createPolicyTypeDef();
        Either<PolicyTypeDefinition, StorageOperationStatus> updatePolicyTypeRes = policyTypeOperation.updatePolicyType(updatedType, currType);
        assertThat(updatePolicyTypeRes.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void updatePolicyType_basicFields() {
        PolicyTypeDefinition createdType = createPolicyTypeDef("type1", "description1", NULL_STRING);
        Either<PolicyTypeDefinition, StorageOperationStatus> currPolicyType = policyTypeOperation.addPolicyType(createdType);

        PolicyTypeDefinition updatedType = createPolicyTypeDef("type1", "description2", NULL_STRING);
        updatedType.setName("newName");
        updatedType.setIcon("icon");
        policyTypeOperation.updatePolicyType(updatedType, currPolicyType.left().value());

        Either<PolicyTypeDefinition, StorageOperationStatus> fetchedUpdatedType = policyTypeOperation.getLatestPolicyTypeByType(createdType.getType());
        PolicyTypeDefinition fetchedPolicyType = fetchedUpdatedType.left().value();
        assertThat(fetchedPolicyType.getProperties()).isEmpty();
        assertThat(fetchedPolicyType)
                .isEqualToIgnoringGivenFields(updatedType, "properties");

    }

    @Test
    public void updatePolicyType_updateProperties() {
        PropertyDefinition prop1 = createSimpleProperty("val1", "prop1", "string");
        PropertyDefinition prop2 = createSimpleProperty("val2", "prop2", "string");
        PolicyTypeDefinition policyType = createPolicyTypeDef(prop1);
        Either<PolicyTypeDefinition, StorageOperationStatus> currPolicyType = policyTypeOperation.addPolicyType(policyType);

        PropertyDefinition updatedProp1 = duplicateProperty(prop1, "newVal1", "int");
        PropertyDefinition prop3 = createSimpleProperty("val3", "prop3", "string");
        PolicyTypeDefinition updatedPolicyType = createPolicyTypeDef(updatedProp1, prop3);

        policyTypeOperation.updatePolicyType(updatedPolicyType, currPolicyType.left().value());

        Either<PolicyTypeDefinition, StorageOperationStatus> fetchedUpdatedType = policyTypeOperation.getLatestPolicyTypeByType(policyType.getType());
        assertThat(fetchedUpdatedType.left().value().getProperties())
                .usingElementComparatorOnFields("name", "defaultValue", "type")
                .containsExactlyInAnyOrder(updatedProp1, prop3);

    }

    @Test
    public void updatePolicyType_derivedFrom_whenNoPrevDerivedFrom_updateToNewDerivedFrom() {
        PolicyTypeDefinition rootPolicyType = createPolicyTypeDef();
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("type1", "descr", NULL_STRING);
        PolicyTypeDefinition updatedPolicyType = createPolicyTypeDef("type1", "descr", rootPolicyType.getType());
        policyTypeOperation.addPolicyType(rootPolicyType);
        Either<PolicyTypeDefinition, StorageOperationStatus> currPolicyType = policyTypeOperation.addPolicyType(policyType1);
        policyTypeOperation.updatePolicyType(updatedPolicyType, currPolicyType.left().value());

        Either<PolicyTypeDefinition, StorageOperationStatus> latestPolicyType = policyTypeOperation.getLatestPolicyTypeByType(policyType1.getType());
        assertThat(latestPolicyType.left().value().getDerivedFrom()).isEqualTo(rootPolicyType.getType());
        verifyDerivedFromNodeEqualsToRootPolicyType(rootPolicyType, latestPolicyType.left().value().getUniqueId());
    }

    @Test
    public void updatePolicyType_derivedFrom_updateToNullDerivedFrom_derivedFromDeleted() {
        PolicyTypeDefinition rootPolicyType = createPolicyTypeDef();
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("type1", "descr", rootPolicyType.getType());
        PolicyTypeDefinition updatedPolicyType = createPolicyTypeDef("type1", "descr", null, new PropertyDefinition[]{});
        policyTypeOperation.addPolicyType(rootPolicyType);
        Either<PolicyTypeDefinition, StorageOperationStatus> currPolicyType = policyTypeOperation.addPolicyType(policyType1);

        policyTypeOperation.updatePolicyType(updatedPolicyType, currPolicyType.left().value());

        Either<PolicyTypeDefinition, StorageOperationStatus> latestPolicyType = policyTypeOperation.getLatestPolicyTypeByType(policyType1.getType());
        assertThat(latestPolicyType.left().value().getDerivedFrom()).isNull();
        verifyDerivedFromRelationDoesntExist(latestPolicyType.left().value().getUniqueId());
    }

    @Test
    public void updatePolicyType_updateDerivedFrom() {
        PolicyTypeDefinition rootPolicyType = createPolicyTypeDef();
        PolicyTypeDefinition derivedType1 = createPolicyTypeDef("derivedType1", "descr", NULL_STRING);
        PolicyTypeDefinition policyType1 = createPolicyTypeDef("type1", "descr", derivedType1.getType());
        PolicyTypeDefinition updatedPolicyType = createPolicyTypeDef("type1", "descr", rootPolicyType.getType());

        policyTypeOperation.addPolicyType(rootPolicyType);
        policyTypeOperation.addPolicyType(derivedType1);
        Either<PolicyTypeDefinition, StorageOperationStatus> currPolicyType = policyTypeOperation.addPolicyType(policyType1);

        policyTypeOperation.updatePolicyType(updatedPolicyType, currPolicyType.left().value());

        Either<PolicyTypeDefinition, StorageOperationStatus> latestPolicyType = policyTypeOperation.getLatestPolicyTypeByType(policyType1.getType());
        assertThat(latestPolicyType.left().value().getDerivedFrom()).isEqualTo(rootPolicyType.getType());
        verifyDerivedFromNodeEqualsToRootPolicyType(rootPolicyType, latestPolicyType.left().value().getUniqueId());
    }

    private PropertyDefinition duplicateProperty(PropertyDefinition prop1, String updatedValue, String updatedType) {
        PropertyDefinition updatedProp1 = new PropertyDefinition(prop1);
        updatedProp1.setUniqueId(null);
        updatedProp1.setDefaultValue(updatedValue);
        updatedProp1.setType(updatedType);
        return updatedProp1;
    }

    private void verifyDerivedFromNodeEqualsToRootPolicyType(PolicyTypeDefinition rootPolicyType, String parentPolicyId) {
        Either<ImmutablePair<PolicyTypeData, GraphEdge>, JanusGraphOperationStatus> derivedFromRelation = janusGraphGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PolicyType), parentPolicyId, GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.PolicyType, PolicyTypeData.class);
        assertThat(derivedFromRelation.left().value().getLeft().getPolicyTypeDataDefinition())
                .isEqualToComparingFieldByField(rootPolicyType);
    }

    private void verifyDerivedFromRelationDoesntExist(String parentPolicyId) {
        Either<ImmutablePair<PolicyTypeData, GraphEdge>, JanusGraphOperationStatus> derivedFromRelation = janusGraphGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PolicyType), parentPolicyId, GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.PolicyType, PolicyTypeData.class);
        assertThat(derivedFromRelation.right().value())
                .isEqualTo(JanusGraphOperationStatus.NOT_FOUND);
    }

    private PolicyTypeDefinition createRootPolicyTypeOnGraph() {
        PolicyTypeDefinition rootPolicyType = createPolicyTypeDef();
        policyTypeOperation.addPolicyType(rootPolicyType);
        return rootPolicyType;

    }

    private PolicyTypeDefinition createPolicyTypeDef() {
        return createPolicyTypeDef("tosca.policies.Root", "description: The TOSCA Policy Type all other TOSCA Policy Types derive from", null, new PropertyDefinition[]{});
    }

    private PolicyTypeDefinition createPolicyTypeDef(PropertyDefinition ... props) {
        return createPolicyTypeDef("tosca.policies.Root",  null, props);
    }

    private PolicyTypeDefinition createPolicyTypeDef(String type, String derivedFrom, PropertyDefinition ... props) {
        PolicyTypeDefinition policyType = createPolicyTypeDef(type, "description: The TOSCA Policy Type all other TOSCA Policy Types derive from", derivedFrom);
        policyType.setProperties(asList(props));
        return policyType;
    }

    private PolicyTypeDefinition createPolicyTypeDef(String type, String description, String derivedFrom) {
        return createPolicyTypeDef(type, description, derivedFrom, null);
    }

    private PolicyTypeDefinition createPolicyTypeDef(String type, String description, String derivedFrom,  PropertyDefinition ... props) {
        PolicyTypeDataDefinition policyTypeDataDefinition = new PolicyTypeDataDefinition();
        policyTypeDataDefinition.setDescription(description);
        policyTypeDataDefinition.setType(type);
        policyTypeDataDefinition.setName(type + "name");
        policyTypeDataDefinition.setIcon(type + "icon");
        policyTypeDataDefinition.setDerivedFrom(derivedFrom);
        PolicyTypeDefinition policyTypeDefinition = new PolicyTypeDefinition(policyTypeDataDefinition);
        policyTypeDefinition.setHighestVersion(true);
        policyTypeDefinition.setVersion("1.0");
        if (props != null) {
            policyTypeDefinition.setProperties(asList(props));
        }
        return policyTypeDefinition;
    }

    private void addPolicyTypesToDB(PolicyTypeDefinition ... policyTypeDefinitions) {
        Stream.of(policyTypeDefinitions).forEach(policyTypeOperation::addPolicyType);
    }


}
