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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ArtifactOperationTest extends ModelTestBase {

	private static final String ARTIFACT_NAME = "myHeatArtifact";

	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "tosca-operation-facade")
	private ToscaOperationFacade toscaOperationFacade;

	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;

	private static Logger log = LoggerFactory.getLogger(ToscaOperationFacade.class.getName());

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
	@Ignore
	public void testAddArtifactToServiceVersionAndUUIDNotNull() {
		CategoryDefinition category = new CategoryDefinition();
		category.setName(CATEGORY_NAME);

		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();

		ArtifactDefinition artifactInfo = addArtifactToService(userId, serviceId, "install_apache");

		assertEquals("add informational artifact version : " + artifactInfo.getArtifactVersion(), "1", artifactInfo.getArtifactVersion());

		assertNotNull("add informational artifact version : " + artifactInfo.getArtifactUUID(), artifactInfo.getArtifactUUID());

		Either<Service, StorageOperationStatus> service = toscaOperationFacade.getToscaFullElement(serviceId);
		assertTrue(service.isLeft());

		Map<String, ArtifactDefinition> artifacts = service.left().value().getArtifacts();
		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			String artifactId = entry.getValue().getUniqueId();
			String description = entry.getValue().getDescription();

			artifactOperation.removeArifactFromResource(serviceId, artifactId, NodeTypeEnum.Service, true, false);
		}
		service = toscaOperationFacade.getToscaFullElement(serviceId);
		assertTrue(service.isLeft());

		artifacts = service.left().value().getArtifacts();
		assertEquals(0, artifacts.size());

		Either<Service, StorageOperationStatus> serviceDelete = toscaOperationFacade.deleteToscaComponent(serviceId);

		Either<List<ArtifactData>, TitanOperationStatus> byCriteria = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef, null, ArtifactData.class);
		assertTrue(byCriteria.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, byCriteria.right().value());

		toscaOperationFacade.deleteToscaComponent(serviceId);

	}

	@Test
	@Ignore
	public void testUpdateArtifactToServiceVersionNotChanged() {
		CategoryDefinition category = new CategoryDefinition();
		category.setName(CATEGORY_NAME);
		String serviceName = "servceTest2";
		String serviceVersion = "0.1";
		String userId = USER_ID;
		Service serviceAfterSave = createService(userId, category, serviceName, serviceVersion, true);
		log.debug("{}", serviceAfterSave);
		String serviceId = serviceAfterSave.getUniqueId();

		ArtifactDefinition artifactInfo = addArtifactToService(userId, serviceId, "install_apache");

		String version = artifactInfo.getArtifactVersion();
		String artUuid = artifactInfo.getArtifactUUID();
		assertEquals("add informational artifact version : " + version, "1", version);

		artifactInfo.setDescription("jghlsk new desfnjdh");

		Either<ArtifactDefinition, StorageOperationStatus> artifact = artifactOperation.updateArifactOnResource(artifactInfo, serviceId, artifactInfo.getUniqueId(), NodeTypeEnum.Service, false);
		String newVersion = artifact.left().value().getArtifactVersion();
		String newArtUuid = artifactInfo.getArtifactUUID();
		assertEquals("add informational artifact version : " + newVersion, newVersion, version);
		assertEquals("add informational artifact uuid : " + newArtUuid, newArtUuid, artUuid);

		Either<Service, StorageOperationStatus> service = toscaOperationFacade.getToscaFullElement(serviceId);
		assertTrue(service.isLeft());

		Map<String, ArtifactDefinition> artifacts = service.left().value().getArtifacts();
		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			String artifactId = entry.getValue().getUniqueId();
			String description = entry.getValue().getDescription();

			artifactOperation.removeArifactFromResource(serviceId, artifactId, NodeTypeEnum.Service, true, false);
		}
		service = toscaOperationFacade.getToscaFullElement(serviceId);
		assertTrue(service.isLeft());

		artifacts = service.left().value().getArtifacts();
		assertEquals(0, artifacts.size());

		Either<Service, StorageOperationStatus> serviceDelete = toscaOperationFacade.deleteToscaComponent(serviceId);

		Either<List<ArtifactData>, TitanOperationStatus> byCriteria = titanDao.getByCriteria(NodeTypeEnum.ArtifactRef, null, ArtifactData.class);
		assertTrue(byCriteria.isRight());
		assertEquals(TitanOperationStatus.NOT_FOUND, byCriteria.right().value());

		toscaOperationFacade.deleteToscaComponent(serviceAfterSave.getUniqueId());

	}

	@Test
	public void testCreateDeleteArtifactWithHeatParams() {

		ArtifactDefinition artifactWithHeat = createResourceWithHeat();

		List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);
		HeatParameterDefinition parameter = heatParameters.get(0);
		HeatParameterData parameterData = new HeatParameterData(parameter);
		Either<HeatParameterData, TitanOperationStatus> parameterNode = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNode.isLeft());

		Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
		assertTrue(removeArifact.isLeft());

		ArtifactData artifactData = new ArtifactData(artifactWithHeat);
		Either<ArtifactData, TitanOperationStatus> artifactAfterDelete = titanDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		assertTrue(artifactAfterDelete.isRight());

		Either<HeatParameterData, TitanOperationStatus> parameterNodeAfterDelete = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isRight());

		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
	}

	@Test
	public void testUpdateArtifactWithHeatParams() {

		ArtifactDefinition artifactWithHeat = createResourceWithHeat();

		List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);
		HeatParameterDefinition parameter = heatParameters.get(0);
		HeatParameterData parameterData = new HeatParameterData(parameter);
		Either<HeatParameterData, TitanOperationStatus> parameterNode = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
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
		Either<ArtifactData, TitanOperationStatus> artifactAfterUpdate = titanDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		assertTrue(artifactAfterUpdate.isLeft());
		ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
		assertTrue(artifactNoParams.getArtifactVersion().equals(artifactAfterUpdateValue.getArtifactDataDefinition().getArtifactVersion()));

		Either<HeatParameterData, TitanOperationStatus> parameterNodeAfterDelete = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isRight());

		artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
	}

	@Test
	public void testUpdateArtifactMetadataWithHeatParams() {

		ArtifactDefinition artifactWithHeat = createResourceWithHeat();

		List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);
		HeatParameterDefinition parameter = heatParameters.get(0);
		HeatParameterData parameterData = new HeatParameterData(parameter);
		Either<HeatParameterData, TitanOperationStatus> parameterNode = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNode.isLeft());

		// update to artifact without params
		artifactWithHeat.setArtifactVersion("2");
		artifactWithHeat.setArtifactChecksum(null);
		artifactWithHeat.setPayloadData(null);

		Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(artifactWithHeat, RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, false);
		assertTrue(updateArifact.isLeft());

		ArtifactData artifactData = new ArtifactData(artifactWithHeat);
		Either<ArtifactData, TitanOperationStatus> artifactAfterUpdate = titanDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		assertTrue(artifactAfterUpdate.isLeft());
		ArtifactData artifactAfterUpdateValue = artifactAfterUpdate.left().value();
		assertTrue(artifactWithHeat.getArtifactVersion().equals(artifactAfterUpdateValue.getArtifactDataDefinition().getArtifactVersion()));

		Either<HeatParameterData, TitanOperationStatus> parameterNodeAfterDelete = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isLeft());

		Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID_2, (String) artifactAfterUpdateValue.getUniqueId(), NodeTypeEnum.Resource, true, false);
		removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);

	}

	@Test
	public void updateHeatArtifactWithTwoResources() {
		ArtifactDefinition artifactWithHeat = createResourceWithHeat();

		ResourceMetadataData resource2 = createResource(RESOURCE_ID_2);
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), ArtifactGroupTypeEnum.DEPLOYMENT.name());
		Either<GraphRelation, TitanOperationStatus> createRelation = titanDao.createRelation(resource2, new ArtifactData(artifactWithHeat), GraphEdgeLabels.ARTIFACT_REF, props);
		assertTrue(createRelation.isLeft());

		List<HeatParameterDefinition> heatParameters = artifactWithHeat.getListHeatParameters();
		assertNotNull(heatParameters);
		assertTrue(heatParameters.size() == 1);
		HeatParameterDefinition parameter = heatParameters.get(0);
		HeatParameterData parameterData = new HeatParameterData(parameter);
		Either<HeatParameterData, TitanOperationStatus> parameterNode = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNode.isLeft());

		ArtifactDefinition atifactToUpdate = new ArtifactDefinition(artifactWithHeat);

		// update to artifact without params
		atifactToUpdate.setArtifactVersion("2");
		atifactToUpdate.setArtifactChecksum(null);
		atifactToUpdate.setPayloadData(null);

		HeatParameterDefinition heatParamUpdate = new HeatParameterDefinition(parameter);
		List<HeatParameterDefinition> heatParametersUpdated = new ArrayList<HeatParameterDefinition>();
		heatParamUpdate.setCurrentValue("55");
		heatParametersUpdated.add(heatParamUpdate);
		atifactToUpdate.setListHeatParameters(heatParametersUpdated);

		Either<ArtifactDefinition, StorageOperationStatus> updateArifact = artifactOperation.updateArifactOnResource(atifactToUpdate, RESOURCE_ID_2, atifactToUpdate.getUniqueId(), NodeTypeEnum.Resource, false);
		assertTrue(updateArifact.isLeft());

		// verify old artifact and parameter still exist
		ArtifactData artifactData = new ArtifactData(artifactWithHeat);
		Either<ArtifactData, TitanOperationStatus> origArtifact = titanDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		assertTrue(origArtifact.isLeft());
		ArtifactData origArtifactData = origArtifact.left().value();
		assertTrue(artifactWithHeat.getArtifactVersion().equals(origArtifactData.getArtifactDataDefinition().getArtifactVersion()));

		Either<HeatParameterData, TitanOperationStatus> parameterNodeAfterDelete = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isLeft());

		// verify new artifact and new parameter
		ArtifactDefinition artifactDefinitionUpdated = updateArifact.left().value();
		ArtifactData artifactDataUpdated = new ArtifactData(artifactDefinitionUpdated);
		Either<ArtifactData, TitanOperationStatus> updatedArtifact = titanDao.getNode(artifactDataUpdated.getUniqueIdKey(), artifactDataUpdated.getUniqueId(), ArtifactData.class);
		assertTrue(updatedArtifact.isLeft());
		ArtifactData updatedArtifactData = updatedArtifact.left().value();
		assertTrue(atifactToUpdate.getArtifactVersion().equals(updatedArtifactData.getArtifactDataDefinition().getArtifactVersion()));
		assertFalse(((String) updatedArtifactData.getUniqueId()).equalsIgnoreCase((String) origArtifactData.getUniqueId()));

		List<HeatParameterDefinition> heatParametersAfterUpdate = artifactDefinitionUpdated.getListHeatParameters();
		assertNotNull(heatParametersAfterUpdate);
		assertTrue(heatParametersAfterUpdate.size() == 1);
		HeatParameterDefinition UpdatedHeatParameter = heatParametersAfterUpdate.get(0);
		assertFalse(UpdatedHeatParameter.getUniqueId().equalsIgnoreCase((String) parameterData.getUniqueId()));
		Either<HeatParameterData, TitanOperationStatus> parameterNodeAfterUpdate = titanDao.getNode(new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(), UpdatedHeatParameter.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterUpdate.isLeft());

		// delete new artifact
		Either<ArtifactDefinition, StorageOperationStatus> removeArifact = artifactOperation.removeArifactFromResource(RESOURCE_ID_2, artifactDefinitionUpdated.getUniqueId(), NodeTypeEnum.Resource, true, false);
		assertTrue(removeArifact.isLeft());

		// verify old artifact and parameter still exist
		origArtifact = titanDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		assertTrue(origArtifact.isLeft());
		origArtifactData = origArtifact.left().value();
		assertTrue(artifactWithHeat.getArtifactVersion().equals(origArtifactData.getArtifactDataDefinition().getArtifactVersion()));

		parameterNodeAfterDelete = titanDao.getNode(parameterData.getUniqueIdKey(), parameterData.getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isLeft());

		// verify new artifact is deleted
		Either<ArtifactData, TitanOperationStatus> artifactAfterDelete = titanDao.getNode(artifactDataUpdated.getUniqueIdKey(), artifactDataUpdated.getUniqueId(), ArtifactData.class);
		assertTrue(artifactAfterDelete.isRight());

		parameterNodeAfterDelete = titanDao.getNode(new HeatParameterData(UpdatedHeatParameter).getUniqueIdKey(), new HeatParameterData(UpdatedHeatParameter).getUniqueId(), HeatParameterData.class);
		assertTrue(parameterNodeAfterDelete.isRight());

		artifactOperation.removeArifactFromResource(RESOURCE_ID, artifactWithHeat.getUniqueId(), NodeTypeEnum.Resource, true, false);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID), ResourceMetadataData.class);
		titanDao.deleteNode(new UniqueIdData(NodeTypeEnum.Resource, RESOURCE_ID_2), ResourceMetadataData.class);
	}

	private ArtifactDefinition createResourceWithHeat() {
		ResourceMetadataData resource = createResource(RESOURCE_ID);
		ArtifactDefinition artifactDefinition = createArtifactDefinition(USER_ID, RESOURCE_ID, ARTIFACT_NAME);
		artifactDefinition.setArtifactType("HEAT");
		artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

		List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
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
		ArtifactDefinition artifactWithHeat = artifact.left().value();
		return artifactWithHeat;
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

		// assertEquals("check resource unique id",
		// UniqueIdBuilder.buildServiceUniqueId(serviceName, serviceVersion),
		// resultService.getUniqueId());
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
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		service.setTags(tags);
		return service;
	}

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateServiceCategory(category, titanDao);
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);

		/*
		 * CategoryData categoryData = new CategoryData(); categoryData.setName(category);
		 * 
		 * titanDao.deleteNode(categoryData, CategoryData.class); Either<CategoryData, TitanOperationStatus> createNode = titanDao.createNode(categoryData, CategoryData.class); System.out.println("after creating caetgory " + createNode);
		 */

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

		titanDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanDao.createNode(userData, UserData.class);
		titanDao.commit();

		return userData;
	}

	public ResourceMetadataData createResource(String resourceName) {

		ResourceMetadataData serviceData1 = new ResourceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
		Either<ResourceMetadataData, TitanOperationStatus> createNode = titanDao.createNode(serviceData1, ResourceMetadataData.class);

		assertTrue("check resource created", createNode.isLeft());
		return createNode.left().value();
	}
}
