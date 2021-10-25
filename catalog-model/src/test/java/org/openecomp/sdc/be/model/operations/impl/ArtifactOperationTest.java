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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class ArtifactOperationTest extends ModelTestBase {

    private static final String ARTIFACT_NAME = "myHeatArtifact";
    private static final String RESOURCE_ID = "resourceId";
    private static final String RESOURCE_ID_2 = "resourceId2";
    private static final String USER_ID = "muUserId";
    @javax.annotation.Resource(name = "janusgraph-generic-dao")
    private JanusGraphGenericDao janusGraphDao;
    @javax.annotation.Resource
    private ArtifactOperation artifactOperation;

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    public void createUserAndCategory() {
        String CATEGORY_NAME = "category/mycategory";
        deleteAndCreateCategory(CATEGORY_NAME);
        deleteAndCreateUser();
    }

    @Test
    public void testCreateDeleteArtifactWithHeatParams() {
        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID,
            artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        assertTrue(removeArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterDelete = janusGraphDao.getNode(artifactData.getUniqueIdKey(),
            artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterDelete.isRight());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isRight());

        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
    }

    @Test
    public void testUpdateArtifactWithHeatParams() {
        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        // update to artifact without params
        ArtifactDefinition artifactNoParams = createArtifactDefinition();
        artifactNoParams.setUniqueId(artifactWithHeat.getUniqueId());
        artifactNoParams.setArtifactType("HEAT");
        artifactNoParams.setArtifactVersion("2");
        artifactNoParams.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(artifactNoParams, RESOURCE_ID,
            artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterUpdate = janusGraphDao.getNode(artifactData.getUniqueIdKey(),
            artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterUpdate.isLeft());
        ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
        assertEquals(artifactNoParams.getArtifactVersion(), artifactAfterUpdateValue.getArtifactDataDefinition()
            .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isRight());

        artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
    }

    @Test
    public void testUpdateArtifactMetadataWithHeatParams() {

        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        // update to artifact without params
        artifactWithHeat.setArtifactVersion("2");
        artifactWithHeat.setArtifactChecksum(null);
        artifactWithHeat.setPayloadData(null);

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(artifactWithHeat, RESOURCE_ID,
            artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterUpdate = janusGraphDao.getNode(artifactData.getUniqueIdKey(),
            artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterUpdate.isLeft());
        ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
        assertEquals(artifactWithHeat.getArtifactVersion(), artifactAfterUpdateValue.getArtifactDataDefinition()
            .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isLeft());

        artifactOperation.removeArifactFromResource(RESOURCE_ID_2, artifactAfterUpdateValue.getUniqueId(), NodeTypeEnum.Resource, true, false);
        artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
    }

    @Test
    public void updateHeatArtifactWithTwoResources() {
        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        ResourceMetadataData resource2 = createResource(RESOURCE_ID_2);
        Map<String, Object> props = new HashMap<>();
        props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), ArtifactGroupTypeEnum.DEPLOYMENT.name());
        Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphDao.createRelation(resource2, new ArtifactData(artifactWithHeat),
            GraphEdgeLabels.ARTIFACT_REF, props);
        assertTrue(createRelation.isLeft());

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        ArtifactDefinition atifactToUpdate = new ArtifactDefinition(artifactWithHeat);

        // update to artifact without params
        atifactToUpdate.setArtifactVersion("2");
        atifactToUpdate.setArtifactChecksum(null);
        atifactToUpdate.setPayloadData(null);

        HeatParameterDefinition heatParamUpdate = new HeatParameterDefinition(parameter);
        List<HeatParameterDefinition> heatParametersUpdated = new ArrayList<>();
        heatParamUpdate.setCurrentValue("55");
        heatParametersUpdated.add(heatParamUpdate);
        atifactToUpdate.setListHeatParameters(heatParametersUpdated);

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(atifactToUpdate, RESOURCE_ID_2,
            atifactToUpdate.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        // verify old artifact and parameter still exist
        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> origArtifact = janusGraphDao.getNode(artifactData.getUniqueIdKey(),
            artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(origArtifact.isLeft());
        ArtifactData origArtifactData = origArtifact.left().value();
        assertEquals(artifactWithHeat.getArtifactVersion(), origArtifactData.getArtifactDataDefinition()
            .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(),
            parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isLeft());

        // verify new artifact and new parameter
        ArtifactDefinition artifactDefinitionUpdated = updateArifact.left().value();
        ArtifactData artifactDataUpdated = new ArtifactData(artifactDefinitionUpdated);
        Either<ArtifactData, JanusGraphOperationStatus> updatedArtifact = janusGraphDao.getNode(artifactDataUpdated.getUniqueIdKey(),
            artifactDataUpdated.getUniqueId(), ArtifactData.class);
        assertTrue(updatedArtifact.isLeft());
        ArtifactData updatedArtifactData = updatedArtifact.left().value();
        assertEquals(atifactToUpdate.getArtifactVersion(), updatedArtifactData.getArtifactDataDefinition()
            .getArtifactVersion());
        assertFalse(updatedArtifactData.getUniqueId().equalsIgnoreCase(origArtifactData.getUniqueId()));

        List<HeatParameterDefinition> heatParametersAfterUpdate = artifactDefinitionUpdated.getListHeatParameters();
        assertNotNull(heatParametersAfterUpdate);
        assertEquals(1, heatParametersAfterUpdate.size());
        HeatParameterDefinition UpdatedHeatParameter = heatParametersAfterUpdate.get(0);
        assertFalse(UpdatedHeatParameter.getUniqueId().equalsIgnoreCase(parameterData.getUniqueId()));
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterUpdate = janusGraphDao.getNode(
            new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(), UpdatedHeatParameter.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterUpdate.isLeft());

        // delete new artifact
        Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID_2,
            artifactDefinitionUpdated.getUniqueId(), NodeTypeEnum.Resource, true, false);
        assertTrue(removeArifact.isLeft());

        // verify old artifact and parameter still exist
        origArtifact = janusGraphDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(origArtifact.isLeft());
        origArtifactData = origArtifact.left().value();
        assertEquals(artifactWithHeat.getArtifactVersion(), origArtifactData.getArtifactDataDefinition()
            .getArtifactVersion());

        parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isLeft());

        // verify new artifact is deleted
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterDelete = janusGraphDao.getNode(artifactDataUpdated.getUniqueIdKey(),
            artifactDataUpdated.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterDelete.isRight());

        parameterNodeAfterDelete = janusGraphDao.getNode(new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(),
            new HeatParameterData(UpdatedHeatParameter).getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isRight());

        artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
    }

    private ArtifactDefinition createResourceWithHeat() {
        createResource(RESOURCE_ID);
        ArtifactDefinition artifactDefinition = createArtifactDefinition();
        artifactDefinition.setArtifactType("HEAT");
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        List<HeatParameterDefinition> heatParams = new ArrayList<>();
        HeatParameterDefinition heatParam = new HeatParameterDefinition();
        heatParam.setCurrentValue("11");
        heatParam.setDefaultValue("22");
        heatParam.setDescription("desc");
        heatParam.setName("myParam");
        heatParam.setType("number");
        heatParams.add(heatParam);
        artifactDefinition.setListHeatParameters(heatParams);

        Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation.addArifactToComponent(artifactDefinition, RESOURCE_ID,
            NodeTypeEnum.Resource, true, false);
        assertTrue(artifact.isLeft());
        return artifact.left().value();
    }

    private ArtifactDefinition createArtifactDefinition() {
        ArtifactDefinition artifactInfo = new ArtifactDefinition();

        artifactInfo.setArtifactName(ArtifactOperationTest.ARTIFACT_NAME + ".sh");
        artifactInfo.setArtifactType("SHELL");
        artifactInfo.setDescription("hdkfhskdfgh");
        artifactInfo.setArtifactChecksum("UEsDBAoAAAAIAAeLb0bDQz");

        artifactInfo.setUserIdCreator(ArtifactOperationTest.USER_ID);
        String fullName = "Jim H";
        artifactInfo.setUpdaterFullName(fullName);
        long time = System.currentTimeMillis();
        artifactInfo.setCreatorFullName(fullName);
        artifactInfo.setCreationDate(time);
        artifactInfo.setLastUpdateDate(time);
        artifactInfo.setUserIdLastUpdater(ArtifactOperationTest.USER_ID);
        artifactInfo.setArtifactLabel(ArtifactOperationTest.ARTIFACT_NAME);
        artifactInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(ArtifactOperationTest.RESOURCE_ID, artifactInfo.getArtifactLabel()));
        return artifactInfo;
    }

    private void deleteAndCreateCategory(String category) {
        String[] names = category.split("/");
        OperationTestsUtil.deleteAndCreateServiceCategory(category, janusGraphDao);
        OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], janusGraphDao);
    }

    private void deleteAndCreateUser() {
        UserData userData = new UserData();
        userData.setUserId(ArtifactOperationTest.USER_ID);
        userData.setFirstName("first_muUserId");
        userData.setLastName("last_muUserId");
        userData.setRole("ADMIN");

        janusGraphDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), ArtifactOperationTest.USER_ID, UserData.class);
        janusGraphDao.createNode(userData, UserData.class);
        janusGraphDao.commit();
    }

    public ResourceMetadataData createResource(String resourceName) {

        ResourceMetadataData serviceData1 = new ResourceMetadataData();
        serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
        Either<ResourceMetadataData, JanusGraphOperationStatus> createNode = janusGraphDao.createNode(serviceData1, ResourceMetadataData.class);

        assertTrue("check resource created", createNode.isLeft());
        return createNode.left().value();
    }
}
