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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.instance.ComponentInstanceChangeOperationOrchestrator;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceMergeDataBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ResourceInstanceBusinessLogicTest extends BaseBusinessLogicMock {

    private static final String RESOURCE_ID_WITH_HEAT_PARAMS = "MyResourceId";
    private static final String RESOURCE_ID_NO_PAYLOAD = "NoHeatPayload";
    private static final String RESOURCE_ID_NO_HEAT_PARAMS = "NoHeatParams";
    private static final String RESOURCE_INSTANCE_ID = "MyResourceInstanceId";
    private static final String SERVICE_ID = "MyServiceId";
    private static final String HEAT_LABEL = "myHeat";
    private static final String HEAT_ENV_LABEL = HEAT_LABEL + "Env";
    private static final String USER_ID = "jh0003";
    private static final long ARTIFACT_CREATION_TIME = System.currentTimeMillis();

    private final ComponentInstanceOperation componentInstanceOperation = Mockito.mock(ComponentInstanceOperation.class);
    private final ArtifactsBusinessLogic artifactBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);
    private final ComponentInstanceMergeDataBusinessLogic compInstMergeDataBL = Mockito.mock(ComponentInstanceMergeDataBusinessLogic.class);
    private final ComponentInstanceChangeOperationOrchestrator onChangeInstanceOperationOrchestrator = Mockito.mock(ComponentInstanceChangeOperationOrchestrator.class);
    private final ForwardingPathOperation forwardingPathOperation = Mockito.mock(ForwardingPathOperation.class);
    private final NodeFilterOperation serviceFilterOperation = Mockito.mock(NodeFilterOperation.class);

    private static final UserBusinessLogic userAdminManager = Mockito.mock(UserBusinessLogic.class);
    public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    public static final IGroupInstanceOperation groupInstanceOperation = Mockito.mock(IGroupInstanceOperation.class);
    public static final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);

    static User adminUser = new User("John", "Doh", USER_ID, "", "ADMIN", null);

    private ComponentInstanceBusinessLogic bl = new ComponentInstanceBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
        groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
        componentInstanceOperation, artifactBusinessLogic, compInstMergeDataBL, onChangeInstanceOperationOrchestrator,
        forwardingPathOperation, serviceFilterOperation, artifactToscaOperation);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getDeploymentResourceInstanceArtifacts();
        Map<String, Object> placeHolderData = (Map<String, Object>) deploymentResourceArtifacts.get(ArtifactsBusinessLogic.HEAT_ENV_NAME);

        ArtifactDefinition heatArtifact = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_WITH_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, true);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put(HEAT_LABEL.toLowerCase(), heatArtifact);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifact = Either.left(artifacts);
        when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_WITH_HEAT_PARAMS, NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifact);

        ArtifactDefinition heatArtifactNoPayload = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_PAYLOAD, HEAT_LABEL, ARTIFACT_CREATION_TIME, true, false);
        Map<String, ArtifactDefinition> artifactsNoPayload = new HashMap<>();
        artifactsNoPayload.put(HEAT_LABEL.toLowerCase(), heatArtifactNoPayload);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifactNoPayload = Either.left(artifactsNoPayload);
        when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_NO_PAYLOAD, NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifactNoPayload);

        ArtifactDefinition heatArtifactNoParams = getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, false);
        Map<String, ArtifactDefinition> artifactsNoParams = new HashMap<>();
        artifactsNoParams.put(HEAT_LABEL.toLowerCase(), heatArtifactNoParams);
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherGetResourceArtifactNoParams = Either.left(artifactsNoParams);
        when(artifactBusinessLogic.getArtifacts(RESOURCE_ID_NO_HEAT_PARAMS, NodeTypeEnum.Resource, ArtifactGroupTypeEnum.DEPLOYMENT, null)).thenReturn(eitherGetResourceArtifactNoParams);

        ArtifactDefinition eitherPlaceHolder = getArtifactPlaceHolder(RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL);
        when(artifactBusinessLogic.createArtifactPlaceHolderInfo(RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL.toLowerCase(), placeHolderData, USER_ID, ArtifactGroupTypeEnum.DEPLOYMENT, false)).thenReturn(eitherPlaceHolder);

        //   Mockito.when(artifactBusinessLogic.createArtifactAuditingFields(Mockito.any(ArtifactDefinition.class), Mockito.anyString(), Mockito.anyString())).thenReturn(new EnumMap<AuditingFieldsKey, Object>(AuditingFieldsKey.class));

        when(userAdminManager.getUser(USER_ID, false)).thenReturn(adminUser);

        Mockito.doNothing().when(componentsUtils).auditComponent(any(ResponseFormat.class), any(User.class), any(Component.class), any(AuditingActionEnum.class),
                any(ResourceCommonInfo.class), any(ResourceVersionInfo.class));

        ArtifactDefinition heatEnvEither = getHeatArtifactDefinition(USER_ID, RESOURCE_INSTANCE_ID, HEAT_ENV_LABEL, ARTIFACT_CREATION_TIME, true, false);

        when(artifactBusinessLogic.createHeatEnvPlaceHolder(any(ArrayList.class),any(ArtifactDefinition.class), Mockito.anyString(), Mockito.anyString(), any(NodeTypeEnum.class), Mockito.anyString(), any(User.class),
                any(Component.class), any())).thenReturn(heatEnvEither);

        Either<List<GroupInstance>, StorageOperationStatus>  groupInstanceEitherLeft = Either.left(new ArrayList<>());
        when(groupInstanceOperation.getAllGroupInstances(Mockito.anyString(),  any(NodeTypeEnum.class))).thenReturn(groupInstanceEitherLeft);

        bl.setToscaOperationFacade(toscaOperationFacade);

        StorageOperationStatus status = StorageOperationStatus.OK;
        Mockito.when(toscaOperationFacade.addDeploymentArtifactsToInstance(Mockito.any(String.class), Mockito.any(ComponentInstance.class), Mockito.any(Map.class))).thenReturn(status);
        Mockito.when(toscaOperationFacade.addInformationalArtifactsToInstance(Mockito.any(String.class), Mockito.any(ComponentInstance.class), Mockito.any())).thenReturn(status);
        Mockito.when(toscaOperationFacade.addGroupInstancesToComponentInstance(Mockito.any(Component.class), Mockito.any(ComponentInstance.class), Mockito.any(), Mockito.any(Map.class))).thenReturn(status);
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
        ActionStatus addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
        assertTrue(addArtifactsRes.equals(ActionStatus.OK));

        Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
        assertNotNull(deploymentArtifacts);
//        assertTrue(deploymentArtifacts.size() == 2);

        ArtifactDefinition heatDefinition = deploymentArtifacts.get(HEAT_LABEL.toLowerCase());
        assertNotNull(heatDefinition);
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
        ActionStatus addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
        assertTrue(addArtifactsRes.equals(ActionStatus.OK));

        Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
        assertNotNull(deploymentArtifacts);
//        assertTrue(deploymentArtifacts.size() == 2);

        ArtifactDefinition heatDefinition = deploymentArtifacts.get(HEAT_LABEL.toLowerCase());
        assertNotNull(heatDefinition);
//        assertEquals(getHeatArtifactDefinition(USER_ID, RESOURCE_ID_NO_HEAT_PARAMS, HEAT_LABEL, ARTIFACT_CREATION_TIME, false, false), heatDefinition);

//        ArtifactDefinition heatEnvDefinition = deploymentArtifacts.get(HEAT_ENV_LABEL.toLowerCase());
//        assertNotNull(heatEnvDefinition);

        List<HeatParameterDefinition> heatParameters = heatDefinition.getListHeatParameters();
        assertNull(heatParameters);

//        List<HeatParameterDefinition> heatEnvParameters = heatEnvDefinition.getListHeatParameters();
//        assertNull(heatEnvParameters);

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

        ActionStatus addArtifactsRes = bl.addComponentInstanceArtifacts(service, resourceInstance, originResource, adminUser, existingEnvVersions);
        assertTrue(addArtifactsRes.equals(ActionStatus.OK));

        Map<String, ArtifactDefinition> deploymentArtifacts = resourceInstance.getDeploymentArtifacts();
        assertNotNull(deploymentArtifacts);
        assertEquals(0, deploymentArtifacts.size());

        Mockito.verify(artifactBusinessLogic, Mockito.times(0)).addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class), any(Service.class), any(NodeTypeEnum.class), Mockito.anyString());
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
                List<HeatParameterDefinition> heatParams = new ArrayList<>();
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
