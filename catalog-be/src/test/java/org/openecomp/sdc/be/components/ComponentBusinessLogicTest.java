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

package org.openecomp.sdc.be.components;

import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

@RunWith(MockitoJUnitRunner.class)
public class ComponentBusinessLogicTest {

    private static final String ARTIFACT_LABEL = "toscaArtifact1";
    private static final String ARTIFACT_LABEL2 = "toscaArtifact2";
    public static final String TEST_RESOURCE_UNIQUE_ID = "TestResourceUniqueId";
    public static final String TEST_RESOURCE_NAME = "TestResource";
    public static final String TEST_RESOURCE_INVARIANTUUID = "TestResourceInvariantUUID";
    public static final String TEST_SERVICE_UNIQUE_ID = "TestServiceUniqueId";
    public static final String TEST_SERVICE_NAME = "TestService";
    public static final String TEST_SERVICE_INVARIANTUUID = "TestServiceInvariantUUID";
    private static User USER;
    private static Resource resource;
    private static Service service;

    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private UserValidations userValidations;
    @Mock
    private ArtifactsBusinessLogic artifactsBusinessLogic;
    @Mock
    private TitanDao titanDao;

    @InjectMocks
    private ComponentBusinessLogic testInstance = new ComponentBusinessLogic() {
        @Override
        public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
            return null;
        }

        @Override
        public ComponentInstanceBusinessLogic getComponentInstanceBL() {
            return null;
        }

        @Override
        public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, String userId) {
            return null;
        }

        @Override
        public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn) {
            return null;
        }
    };

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        new DummyConfigurationManager();

        // User data and management
        USER = new User();
        USER.setUserId("jh003");
        USER.setFirstName("Jimmi");
        USER.setLastName("Hendrix");
        USER.setRole(Role.ADMIN.name());

        resource = new ResourceBuilder()
            .setComponentType(ComponentTypeEnum.RESOURCE)
            .setInvariantUUid(TEST_RESOURCE_INVARIANTUUID)
            .setUniqueId(TEST_RESOURCE_UNIQUE_ID)
            .setName(TEST_RESOURCE_NAME)
            .build();
        Mockito.when(toscaOperationFacade.getComponentListByInvariantUuid(TEST_RESOURCE_INVARIANTUUID, null)).thenReturn(Either.left(Arrays.asList(resource)));

        service = new ServiceBuilder()
            .setComponentType(ComponentTypeEnum.SERVICE)
            .setInvariantUUid(TEST_SERVICE_INVARIANTUUID)
            .setUniqueId(TEST_SERVICE_UNIQUE_ID)
            .setName(TEST_SERVICE_NAME)
            .build();
        Mockito.when(toscaOperationFacade.getComponentListByInvariantUuid(TEST_SERVICE_INVARIANTUUID, null)).thenReturn(Either.left(Arrays.asList(service)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setToscaArtifactsPlaceHolders_normalizeArtifactName() throws Exception {
        Resource resource = new ResourceBuilder().setUniqueId("uid")
                .setComponentType(ComponentTypeEnum.RESOURCE)
                .setSystemName("myResource")
                .build();
        Map<String, Object> artifactsFromConfig = new HashMap<>();
        artifactsFromConfig.put(ARTIFACT_LABEL, buildArtifactMap("artifact:not normalized.yml"));
        artifactsFromConfig.put(ARTIFACT_LABEL2, buildArtifactMap("alreadyNormalized.csar"));
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts()).thenReturn(artifactsFromConfig);
        when(artifactsBusinessLogic.createArtifactPlaceHolderInfo(resource.getUniqueId(), ARTIFACT_LABEL, (Map<String, Object>) artifactsFromConfig.get(ARTIFACT_LABEL), USER, ArtifactGroupTypeEnum.TOSCA))
                .thenReturn(buildArtifactDef(ARTIFACT_LABEL));
        when(artifactsBusinessLogic.createArtifactPlaceHolderInfo(resource.getUniqueId(), ARTIFACT_LABEL2, (Map<String, Object>) artifactsFromConfig.get(ARTIFACT_LABEL2), USER, ArtifactGroupTypeEnum.TOSCA))
                .thenReturn(buildArtifactDef(ARTIFACT_LABEL2));
        testInstance.setToscaArtifactsPlaceHolders(resource, USER);

        Map<String, ArtifactDefinition> toscaArtifacts = resource.getToscaArtifacts();
        Assert.assertEquals(2, toscaArtifacts.size());
        ArtifactDefinition artifactDefinition = toscaArtifacts.get(ARTIFACT_LABEL);
        Assert.assertEquals("resource-myResourceartifactnot-normalized.yml", artifactDefinition.getArtifactName());
        ArtifactDefinition artifactDefinition2 = toscaArtifacts.get(ARTIFACT_LABEL2);
        Assert.assertEquals("resource-myResourcealreadyNormalized.csar", artifactDefinition2.getArtifactName());
    }

    private Map<String, Object> buildArtifactMap(String artifactName) {
        Map<String, Object> artifact = new HashMap<>();
        artifact.put("artifactName", artifactName);
        return artifact;
    }

    private ArtifactDefinition buildArtifactDef(String artifactLabel) {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactLabel(artifactLabel);
        return artifactDefinition;
    }

    @Test
    public void testDeleteResourceNotInUse(){
        Mockito.when(toscaOperationFacade.isContainedComponent(TEST_RESOURCE_UNIQUE_ID)).thenReturn(Either.left(Boolean.FALSE));
        Mockito.when(toscaOperationFacade.deleteToscaComponent(TEST_RESOURCE_UNIQUE_ID)).thenReturn(Either.left(resource));
        Either<List<String>, ResponseFormat> deleteEither = testInstance.deleteComponent(TEST_RESOURCE_INVARIANTUUID, ComponentTypeEnum.RESOURCE, USER);
        Assert.assertEquals(deleteEither.left().value(), Arrays.asList(TEST_RESOURCE_UNIQUE_ID));
    }

    @Test
    public void testDeleteResourceInUse(){
        Mockito.when(toscaOperationFacade.isContainedComponent(TEST_RESOURCE_UNIQUE_ID)).thenReturn(Either.left(Boolean.TRUE));
        testInstance.deleteComponent(TEST_RESOURCE_INVARIANTUUID, ComponentTypeEnum.RESOURCE, USER);
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.COMPONENT_DELETION_NOT_ALLOWED_CONTAINED, ComponentTypeEnum.RESOURCE.getValue(), TEST_RESOURCE_NAME);
    }

    @Test
    public void testDeleteServiceNotInUse(){
        ComponentMetadataData serviceMetadataData = new ServiceMetadataData(new ServiceMetadataDataDefinition());
        Mockito.when(toscaOperationFacade.getComponentMetadata(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(serviceMetadataData));
        Mockito.when(toscaOperationFacade.isContainedComponent(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(Boolean.FALSE));
        Mockito.when(toscaOperationFacade.deleteToscaComponent(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(service));
        Either<List<String>, ResponseFormat> deleteEither = testInstance.deleteComponent(TEST_SERVICE_INVARIANTUUID, ComponentTypeEnum.SERVICE, USER);
        Assert.assertEquals(deleteEither.left().value(), Arrays.asList(TEST_SERVICE_UNIQUE_ID));
    }

    @Test
    public void testDeleteDistributedService(){
        ServiceMetadataDataDefinition smdd = new ServiceMetadataDataDefinition();
        smdd.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED.name());
        ComponentMetadataData serviceMetadataData = new ServiceMetadataData(smdd);
        Mockito.when(toscaOperationFacade.getComponentMetadata(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(serviceMetadataData));
        testInstance.deleteComponent(TEST_SERVICE_INVARIANTUUID, ComponentTypeEnum.SERVICE, USER);
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.COMPONENT_DELETION_NOT_ALLOWED_DISTRIBUTED, ComponentTypeEnum.SERVICE.getValue(), TEST_SERVICE_NAME);
    }

    @Test
    public void testDeleteServiceInUse(){
        ComponentMetadataData serviceMetadataData = new ServiceMetadataData(new ServiceMetadataDataDefinition());
        Mockito.when(toscaOperationFacade.getComponentMetadata(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(serviceMetadataData));
        Mockito.when(toscaOperationFacade.isContainedComponent(TEST_SERVICE_UNIQUE_ID)).thenReturn(Either.left(Boolean.TRUE));
        testInstance.deleteComponent(TEST_SERVICE_INVARIANTUUID, ComponentTypeEnum.SERVICE, USER);
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.COMPONENT_DELETION_NOT_ALLOWED_CONTAINED, ComponentTypeEnum.SERVICE.getValue(), TEST_SERVICE_NAME);
    }
}
