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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ResourceInstanceBusinessLogicTest {

	private static final String RESOURCE_ID_WITH_HEAT_PARAMS = "MyResourceId";
	private static final String RESOURCE_ID_NO_PAYLOAD = "NoHeatPayload";
	private static final String RESOURCE_ID_NO_HEAT_PARAMS = "NoHeatParams";
	private static final String RESOURCE_INSTANCE_ID = "MyResourceInstanceId";
	private static final String SERVICE_ID = "MyServiceId";
	private static final String HEAT_LABEL = "myHeat";
	private static final String HEAT_ENV_LABEL = HEAT_LABEL + "Env";
	private static final String USER_ID = "jh0003";
	private static final long ARTIFACT_CREATION_TIME = System.currentTimeMillis();

	static User adminUser = new User("John", "Doh", USER_ID, "", "ADMIN", null);

	@InjectMocks
	static ServiceComponentInstanceBusinessLogic bl = new ServiceComponentInstanceBusinessLogic();

	public static final ArtifactsBusinessLogic artifactBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);
	public static final UserBusinessLogic userAdminManager = Mockito.mock(UserBusinessLogic.class);
//	public static final ServiceOperation serviceOperation = Mockito.mock(ServiceOperation.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
	public static final IGroupInstanceOperation groupInstanceOperation = Mockito.mock(IGroupInstanceOperation.class);
	public static final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);

	static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
	static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

	// @BeforeClass
	public static void setup() {

		Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getDeploymentResourceInstanceArtifacts();
		Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(ArtifactsBusinessLogic.HEAT_ENV_NAME);

		ArtifactDefinition heatArtifact = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_WITH_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, true);
		Map<String, ArtifactDefinition> artifacts = new HashMap<String, ArtifactDefinition>();
		artifacts.put(HEAT_LABEL.toLowerCase(), heatArtifact);
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifact = Either.left(artifacts);
		Mockito.when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_WITH_HEAT_PARAMS, NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifact);

		ArtifactDefinition heatArtifactNoPayload = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_PAYLOAD, HEAT_LABEL, ARTIFACT_CREATION_TIME, true, false);
		Map<String, ArtifactDefinition> artifactsNoPayload = new HashMap<String, ArtifactDefinition>();
		artifactsNoPayload.put(HEAT_LABEL.toLowerCase(), heatArtifactNoPayload);
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifactNoPayload = Either.left(artifactsNoPayload);
		Mockito.when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_NO_PAYLOAD, NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifactNoPayload);

		ArtifactDefinition heatArtifactNoParams = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, false);
		Map<String, ArtifactDefinition> artifactsNoParams = new HashMap<String, ArtifactDefinition>();
		artifactsNoParams.put(HEAT_LABEL.toLowerCase(), heatArtifactNoParams);
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifactNoParams = Either.left(artifactsNoParams);
		Mockito.when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_NO_HEAT_PARAMS, NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifactNoParams);

		Either<ArtifactDefinition, ResponseFormat> eitherPlaceHolder = Either.left(getArtifactPlaceHolder(RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL));
		Mockito.when(artifactBusinessLogic.createArtifactPlaceHolderInfo(RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL.toLowerCase(), placeHolderData, USER_ID, ArtifactGroupTypeEnum.DEPLOYMENT, false)).thenReturn(eitherPlaceHolder);

		Mockito.when(artifactBusinessLogic.createArtifactAuditingFields(Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.anyString())).thenReturn(new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class));

		Either<ArtifactDefinition, StorageOperationStatus> eitherArtifact = Either.left(getHeatArtifactDefinition(USER_ID, RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL, ARTIFACT_CREATION_TIME, true, false));
		Mockito.when(artifactBusinessLogic.addHeatEnvArtifact(Mockito.any(ArtifactDefinition.class), Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.any(NodeTypeEnum.class), Mockito.anyString())).thenReturn(eitherArtifact);

		Either<User, ActionStatus> eitherUser = Either.left(adminUser);
		Mockito.when(userAdminManager.getUser(USER_ID, false)).thenReturn(eitherUser);

		Object lightService = new Service();
		Either<Object, StorageOperationStatus> eitherLightService = Either.left(lightService);
//		Mockito.when(serviceOperation.getLightComponent(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(eitherLightService);

		Mockito.doNothing().when(componentsUtils).auditComponent(Mockito.any(ResponseFormat.class), Mockito.any(User.class), Mockito.any(Component.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(AuditingActionEnum.class),
				Mockito.any(ComponentTypeEnum.class), Mockito.any(EnumMap.class));

		Either<ArtifactDefinition, ResponseFormat> heatEnvEither = Either.left(getHeatArtifactDefinition(USER_ID, RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL, ARTIFACT_CREATION_TIME, true, false));

		Mockito.when(artifactBusinessLogic.createHeatEnvPlaceHolder(Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(NodeTypeEnum.class), Mockito.anyString(), Mockito.any(User.class),
				Mockito.any(Component.class), Mockito.anyObject())).thenReturn(heatEnvEither);

		Either<List<GroupInstance>, StorageOperationStatus>  groupInstanceEitherLeft = Either.left(new ArrayList<GroupInstance>());
		Mockito.when(groupInstanceOperation.getAllGroupInstances(Mockito.anyString(),  Mockito.any(NodeTypeEnum.class))).thenReturn(groupInstanceEitherLeft);
		
		bl.setToscaOperationFacade(toscaOperationFacade);
		
		StorageOperationStatus status = StorageOperationStatus.OK;
		Mockito.when(toscaOperationFacade.addDeploymentArtifactsToInstance(Mockito.any(String.class), Mockito.any(ComponentInstance.class), Mockito.any(Map.class))).thenReturn(status);
		Mockito.when(toscaOperationFacade.addInformationalArtifactsToInstance(Mockito.any(String.class), Mockito.any(ComponentInstance.class), Mockito.any())).thenReturn(status);
		Mockito.when(toscaOperationFacade.addGroupInstancesToComponentInstance(Mockito.any(Component.class), Mockito.any(ComponentInstance.class), Mockito.any(), Mockito.any(Map.class))).thenReturn(status);
		
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
//		Mockito.reset(artifactBusinessLogic, serviceOperation, componentsUtils, userAdminManager);
		setup();
	}

	@Test
	public void testAddResourceInstanceArtifacts() throws Exception {
		ComponentInstance resourceInstance = new ComponentInstance();
		resourceInstance.setName(RESOURCE_INSTANCE_ID);
		resourceInstance.setComponentUid(RESOURCE_ID_WITH_HEAT_PARAMS);
		resourceInstance.setUniqueId(RESOURCE_INSTANCE_ID);
		Service service = new Service();
		service.setUniqueId(SERVICE_ID);
		
		Map<String, String> existingEnvVersions = new HashMap<>();
		Resource originResource = new Resource();
		originResource.setUniqueId(RESOURCE_ID_NO_PAYLOAD);
		Either<ActionStatus, ResponseFormat> addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
		assertTrue(addArtifactsRes.isLeft());

		Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
//		assertTrue(deploymentArtifacts.size() == 2);

		ArtifactDefinition heatDefinition = deploymentArtifacts.get(HEAT_LABEL.toLowerCase());
		assertNotNull(heatDefinition);
//		assertEquals(getHeatArtifactDefinition(USER_ID, RESOURCE_ID_WITH_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, true), heatDefinition);
//
//		ArtifactDefinition heatEnvDefinition = deploymentArtifacts.get(HEAT_ENV_LABEL.toLowerCase());
//		assertNotNull(heatEnvDefinition);
//
//		List<HeatParameterDefinition> heatParameters = heatDefinition.getListHeatParameters();
//		assertNotNull(heatParameters);
//
//		List<HeatParameterDefinition> heatEnvParameters = heatEnvDefinition.getListHeatParameters();
//		assertNotNull(heatEnvParameters);
//
//		assertEquals(heatParameters.size(), heatEnvParameters.size());
//
//		int index = 0;
//		for (HeatParameterDefinition heatEnvParameter : heatEnvParameters) {
//			HeatParameterDefinition heatParameterDefinition = heatParameters.get(index);
//			assertEquals(heatEnvParameter.getUniqueId(), heatParameterDefinition.getUniqueId());
//			assertEquals(heatEnvParameter.getType(), heatParameterDefinition.getType());
//			assertEquals(heatEnvParameter.getName(), heatParameterDefinition.getName());
//			assertEquals(heatEnvParameter.getDescription(), heatParameterDefinition.getDescription());
//			assertEquals(heatEnvParameter.getCurrentValue(), heatParameterDefinition.getCurrentValue());
//			// current of heat parameter should be the default for heat env
//			// parameter
//			assertEquals(heatEnvParameter.getDefaultValue(), heatParameterDefinition.getCurrentValue());
//
//			index++;
//		}
	}

	 @Test
	public void testAddResourceInstanceArtifactsNoParams() throws Exception {
		ComponentInstance resourceInstance = new ComponentInstance();
		resourceInstance.setName(RESOURCE_INSTANCE_ID);
		resourceInstance.setComponentUid(RESOURCE_ID_NO_HEAT_PARAMS);
		resourceInstance.setUniqueId(RESOURCE_INSTANCE_ID);
		Service service = new Service();
		service.setUniqueId(SERVICE_ID);
		Map<String, String> existingEnvVersions = new HashMap<>();
		Resource originResource = new Resource();
		originResource.setUniqueId(RESOURCE_ID_NO_PAYLOAD);
		Either<ActionStatus, ResponseFormat> addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
		assertTrue(addArtifactsRes.isLeft());

		Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
//		assertTrue(deploymentArtifacts.size() == 2);

		ArtifactDefinition heatDefinition = deploymentArtifacts.get(HEAT_LABEL.toLowerCase());
		assertNotNull(heatDefinition);
//		assertEquals(getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, false), heatDefinition);

//		ArtifactDefinition heatEnvDefinition = deploymentArtifacts.get(HEAT_ENV_LABEL.toLowerCase());
//		assertNotNull(heatEnvDefinition);

		List<HeatParameterDefinition> heatParameters = heatDefinition.getListHeatParameters();
		assertNull(heatParameters);

//		List<HeatParameterDefinition> heatEnvParameters = heatEnvDefinition.getListHeatParameters();
//		assertNull(heatEnvParameters);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddResourceInstanceArtifactsNoArtifacts() throws Exception {
		ComponentInstance resourceInstance = new ComponentInstance();
		resourceInstance.setName(RESOURCE_INSTANCE_ID);
		resourceInstance.setComponentUid(RESOURCE_ID_NO_PAYLOAD);
		resourceInstance.setUniqueId(RESOURCE_INSTANCE_ID);
		Service service = new Service();
		service.setUniqueId(SERVICE_ID);
		Map<String, String> existingEnvVersions = new HashMap<>();
		Resource originResource = new Resource();
		originResource.setUniqueId(RESOURCE_ID_NO_PAYLOAD);
		
		Either<ActionStatus, ResponseFormat> addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
		assertTrue(addArtifactsRes.isLeft());

		Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		assertTrue(deploymentArtifacts.size() == 0);

		Mockito.verify(artifactBusinessLogic, Mockito.times(0)).addHeatEnvArtifact(Mockito.any(ArtifactDefinition.class), Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.any(NodeTypeEnum.class), Mockito.anyString());
	}

	private static ArtifactDefinition getHeatArtifactDefinition(String userId, String resourceId, String artifactName, long time, boolean placeholderOnly, boolean withHeatParams) {
		ArtifactDefinition artifactInfo = new ArtifactDefinition();

		artifactInfo.setArtifactName(artifactName + ".yml");
		artifactInfo.setArtifactType("HEAT");
		artifactInfo.setDescription("hdkfhskdfgh");
		artifactInfo.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

		artifactInfo.setUserIdCreator(userId);
		String fullName = "Jim H";
		artifactInfo.setUpdaterFullName(fullName);
		// long time = System.currentTimeMillis();
		artifactInfo.setCreatorFullName(fullName);
		artifactInfo.setCreationDate(time);
		artifactInfo.setLastUpdateDate(time);
		artifactInfo.setUserIdLastUpdater(userId);
		artifactInfo.setArtifactLabel(HEAT_LABEL.toLowerCase());
		artifactInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, artifactInfo.getArtifactLabel()));

		if (!placeholderOnly) {
			artifactInfo.setEsId(artifactInfo.getUniqueId());
			artifactInfo.setArtifactChecksum("UEsDBAoAAAAIAAeLb0bDQz");

			if (withHeatParams) {
				List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
				HeatParameterDefinition heatParam = new HeatParameterDefinition();
				heatParam.setCurrentValue("11");
				heatParam.setDefaultValue("22");
				heatParam.setDescription("desc");
				heatParam.setName("myParam");
				heatParam.setType("number");
				heatParams.add(heatParam);
				artifactInfo.setListHeatParameters(heatParams);
			}
		}

		return artifactInfo;
	}

	private static ArtifactDefinition getArtifactPlaceHolder(String resourceId, String logicalName) {
		ArtifactDefinition artifact = new ArtifactDefinition();

		artifact.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, logicalName.toLowerCase()));
		artifact.setArtifactLabel(logicalName.toLowerCase());

		return artifact;
	}
}
