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

import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.*;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ArtifactOperationTest extends ModelTestBase {

    private static final String ARTIFACT_NAME = "myHeatArtifact";

    @javax.annotation.Resource(name = "janusgraph-generic-dao")
    private JanusGraphGenericDao janusGraphDao;

    @javax.annotation.Resource(name = "tosca-operation-facade")
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource
    private ArtifactOperation artifactOperation;

    private static final Logger log = LoggerFactory.getLogger(ToscaOperationFacade.class);

    private static String RESOURCE_ID = "resourceId";
    private static String RESOURCE_ID_2 = "resourceId2";

    private static String USER_ID = "muUserId";
    private static String CATEGORY_NAME = "category/mycategory";

    @BeforeClass
    public static void setupBeforeClass() {

        ModelTestBase.init();
    }

    @Before
    public void createUserAndCategory() {
        deleteAndCreateCategory(CATEGORY_NAME);
        deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID, null);
    }

    @Test
    public void testCreateDeleteArtifactWithHeatParams() {

        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        assertTrue(removeArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterDelete = janusGraphDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterDelete.isRight());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
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
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        // update to artifact without params
        ArtifactDefinition artifactNoParams = createArtifactDefinition(USER_ID, RESOURCE_ID, ARTIFACT_NAME);
        artifactNoParams.setUniqueId(artifactWithHeat.getUniqueId());
        artifactNoParams.setArtifactType("HEAT");
        artifactNoParams.setArtifactVersion("2");
        artifactNoParams.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(artifactNoParams, RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterUpdate = janusGraphDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterUpdate.isLeft());
        ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
        assertEquals(artifactNoParams.getArtifactVersion(), artifactAfterUpdateValue.getArtifactDataDefinition()
                                                                                    .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
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
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNode.isLeft());

        // update to artifact without params
        artifactWithHeat.setArtifactVersion("2");
        artifactWithHeat.setArtifactChecksum(null);
        artifactWithHeat.setPayloadData(null);

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(artifactWithHeat, RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterUpdate = janusGraphDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterUpdate.isLeft());
        ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
        assertEquals(artifactWithHeat.getArtifactVersion(), artifactAfterUpdateValue.getArtifactDataDefinition()
                                                                                    .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isLeft());

        Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID_2, (String) artifactAfterUpdateValue.getUniqueId(), NodeTypeEnum.Resource, true, false);
        removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);

    }

    @Test
    public void updateHeatArtifactWithTwoResources() {
        ArtifactDefinition artifactWithHeat = createResourceWithHeat();

        ResourceMetadataData resource2 = createResource(RESOURCE_ID_2);
        Map<String, Object> props = new HashMap<>();
        props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), ArtifactGroupTypeEnum.DEPLOYMENT.name());
        Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphDao.createRelation(resource2, new ArtifactData(artifactWithHeat), GraphEdgeLabels.ARTIFACT_REF, props);
        assertTrue(createRelation.isLeft());

        List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
        assertNotNull(heatParameters);
        assertEquals(1, heatParameters.size());
        HeatParameterDefinition parameter = heatParameters.get(0);
        HeatParameterData parameterData = new HeatParameterData(parameter);
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNode = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
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

        Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(atifactToUpdate, RESOURCE_ID_2, atifactToUpdate.getUniqueId(), NodeTypeEnum.Resource, false);
        assertTrue(updateArifact.isLeft());

        // verify old artifact and parameter still exist
        ArtifactData artifactData = new ArtifactData(artifactWithHeat);
        Either<ArtifactData, JanusGraphOperationStatus> origArtifact = janusGraphDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        assertTrue(origArtifact.isLeft());
        ArtifactData origArtifactData = origArtifact.left().value();
        assertEquals(artifactWithHeat.getArtifactVersion(), origArtifactData.getArtifactDataDefinition()
                                                                            .getArtifactVersion());

        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterDelete = janusGraphDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isLeft());

        // verify new artifact and new parameter
        ArtifactDefinition artifactDefinitionUpdated = updateArifact.left().value();
        ArtifactData artifactDataUpdated = new ArtifactData(artifactDefinitionUpdated);
        Either<ArtifactData, JanusGraphOperationStatus> updatedArtifact = janusGraphDao.getNode(artifactDataUpdated.getUniqueIdKey(), artifactDataUpdated.getUniqueId(), ArtifactData.class);
        assertTrue(updatedArtifact.isLeft());
        ArtifactData updatedArtifactData = updatedArtifact.left().value();
        assertEquals(atifactToUpdate.getArtifactVersion(), updatedArtifactData.getArtifactDataDefinition()
                                                                              .getArtifactVersion());
        assertFalse(((String) updatedArtifactData.getUniqueId()).equalsIgnoreCase((String) origArtifactData.getUniqueId()));

        List<HeatParameterDefinition> heatParametersAfterUpdate = artifactDefinitionUpdated.getListHeatParameters();
        assertNotNull(heatParametersAfterUpdate);
        assertEquals(1, heatParametersAfterUpdate.size());
        HeatParameterDefinition UpdatedHeatParameter = heatParametersAfterUpdate.get(0);
        assertFalse(UpdatedHeatParameter.getUniqueId().equalsIgnoreCase((String) parameterData.getUniqueId()));
        Either<HeatParameterData, JanusGraphOperationStatus> parameterNodeAfterUpdate = janusGraphDao.getNode(new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(), UpdatedHeatParameter.getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterUpdate.isLeft());

        // delete new artifact
        Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID_2, artifactDefinitionUpdated.getUniqueId(), NodeTypeEnum.Resource, true, false);
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
        Either<ArtifactData, JanusGraphOperationStatus> artifactAfterDelete = janusGraphDao.getNode(artifactDataUpdated.getUniqueIdKey(), artifactDataUpdated.getUniqueId(), ArtifactData.class);
        assertTrue(artifactAfterDelete.isRight());

        parameterNodeAfterDelete = janusGraphDao.getNode(new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(), new HeatParameterData(UpdatedHeatParameter).getUniqueId(), HeatParameterData.class);
        assertTrue(parameterNodeAfterDelete.isRight());

        artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
        janusGraphDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
    }

    private ArtifactDefinition createResourceWithHeat() {
        ResourceMetadataData resource = createResource(RESOURCE_ID);
        ArtifactDefinition artifactDefinition = createArtifactDefinition(USER_ID, RESOURCE_ID, ARTIFACT_NAME);
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

        Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation.addArifactToComponent(artifactDefinition, RESOURCE_ID, NodeTypeEnum.Resource, true, false);
        assertTrue(artifact.isLeft());
        return artifact.left().value();
    }

    private ArtifactDefinition addArtifactToService(String userId, String serviceId, String artifactName) {
        ArtifactDefinition artifactInfo = createArtifactDefinition(userId, serviceId, artifactName);

        Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation.addArifactToComponent(artifactInfo, serviceId, NodeTypeEnum.Service, true, true);
        assertTrue(artifact.isLeft());
        return artifact.left().value();
    }

    private ArtifactDefinition createArtifactDefinition(String userId, String serviceId, String artifactName) {
        ArtifactDefinition artifactInfo = new ArtifactDefinition();

        artifactInfo.setArtifactName(artifactName + ".sh");
        artifactInfo.setArtifactType("SHELL");
        artifactInfo.setDescription("hdkfhskdfgh");
        artifactInfo.setArtifactChecksum("UEsDBAoAAAAIAAeLb0bDQz");

        artifactInfo.setUserIdCreator(userId);
        String fullName = "Jim H";
        artifactInfo.setUpdaterFullName(fullName);
        long time = System.currentTimeMillis();
        artifactInfo.setCreatorFullName(fullName);
        artifactInfo.setCreationDate(time);
        artifactInfo.setLastUpdateDate(time);
        artifactInfo.setUserIdLastUpdater(userId);
        artifactInfo.setArtifactLabel(artifactName);
        artifactInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(serviceId, artifactInfo.getArtifactLabel()));
        return artifactInfo;
    }

    public Service createService(String userId, CategoryDefinition category, String serviceName, String serviceVersion, boolean isHighestVersion) {

        Service service = buildServiceMetadata(userId, category, serviceName, serviceVersion);

        service.setHighestVersion(isHighestVersion);

        Either<Service, StorageOperationStatus> result = toscaOperationFacade.createToscaComponent(service);

        log.info(result.toString());
        assertTrue(result.isLeft());
        Service resultService = result.left().value();

        assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, resultService.getLifecycleState());

        return resultService;
    }

    private Service buildServiceMetadata(String userId, CategoryDefinition category, String serviceName, String serviceVersion) {

        Service service = new Service();
        service.setName(serviceName);
        service.setVersion(serviceVersion);
        service.setDescription("description 1");

        service.setCreatorUserId(userId);
        service.setContactId("contactId@sdc.com");
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setIcon("images/my.png");
        List<String> tags = new ArrayList<>();
        tags.add("TAG1");
        tags.add("TAG2");
        service.setTags(tags);
        return service;
    }

    private void deleteAndCreateCategory(String category) {
        String[] names = category.split("/");
        OperationTestsUtil.deleteAndCreateServiceCategory(category, janusGraphDao);
        OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], janusGraphDao);
    }

    private UserData deleteAndCreateUser(String userId, String firstName, String lastName, String role) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);
        if (role != null && !role.isEmpty()) {
            userData.setRole(role);
        } else {
            userData.setRole("ADMIN");
        }

        janusGraphDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
        janusGraphDao.createNode(userData, UserData.class);
        janusGraphDao.commit();

        return userData;
    }

    public ResourceMetadataData createResource(String resourceName) {

        ResourceMetadataData serviceData1 = new ResourceMetadataData();
        serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
        Either<ResourceMetadataData, JanusGraphOperationStatus> createNode = janusGraphDao.createNode(serviceData1, ResourceMetadataData.class);

        assertTrue("check resource created", createNode.isLeft());
        return createNode.left().value();
    }
}
