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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_ENV_NAME;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_VF_ENV_NAME;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fj.data.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import mockit.Deencapsulation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.MockGenerator;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ArtifactConfigManager;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.ComponentType;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.ResourceMetadataDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ArtifactsBusinessLogicTest extends BaseBusinessLogicMock {

    public static final Resource resource = Mockito.mock(Resource.class);
    private static final User USER = new User("John", "Doh", "jh0003", "jh0003@gmail.com", "ADMIN",
        System.currentTimeMillis());
    private static final String RESOURCE_INSTANCE_NAME = "Service-111";
    private static final String INSTANCE_ID = "S-123-444-ghghghg";
    private static final String ARTIFACT_NAME = "service-Myservice-template.yml";
    private static final String ARTIFACT_LABEL = "assettoscatemplate";
    private static final String ES_ARTIFACT_ID = "123123dfgdfgd0";
    private static final byte[] PAYLOAD = "some payload".getBytes();
    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";
    private static final String ARTIFACT_PLACEHOLDER_FILE_EXTENSION = "fileExtension";
    private static User user = null;
    private static Resource resourceResponse = null;
    final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    @Mock
    public ComponentsUtils componentsUtils;
    @Mock
    public ToscaOperationFacade toscaOperationFacade;
    JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    @Mock
    JanusGraphDao janusGraphDao;
    @InjectMocks
    private ArtifactsBusinessLogic artifactBL;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ArtifactOperation artifactOperation;
    @Mock
    private IInterfaceLifecycleOperation lifecycleOperation;
    @Mock
    private UserAdminOperation userOperation;
    @Mock
    private IElementOperation elementOperation;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    private NodeTemplateOperation nodeTemplateOperation;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private UserValidations userValidations;
    @Mock
    private ArtifactsResolver artifactsResolver;
    @Mock
    private CsarUtils csarUtils;
    @Mock
    private ToscaExportHandler toscaExportHandler;
    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private ArtifactTypeOperation artifactTypeOperation;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static List<ArtifactType> getAllTypes() {
        final List<ArtifactConfiguration> artifactConfigurationList = ConfigurationManager.getConfigurationManager()
            .getConfiguration().getArtifacts();
        return artifactConfigurationList.stream().map(artifactConfiguration -> {
            final ArtifactType artifactType = new ArtifactType();
            artifactType.setName(artifactConfiguration.getType());
            return artifactType;
        }).collect(Collectors.toList());
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
        Either<ArtifactDefinition, StorageOperationStatus> NotFoundResult = Either
            .right(StorageOperationStatus.NOT_FOUND);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> NotFoundResult2 = Either
            .right(StorageOperationStatus.NOT_FOUND);
        when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Service), Mockito.anyBoolean()))
            .thenReturn(NotFoundResult2);
        when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Resource), Mockito.anyBoolean()))
            .thenReturn(NotFoundResult2);

        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> notFoundInterfaces = Either
            .right(StorageOperationStatus.NOT_FOUND);
        when(lifecycleOperation.getAllInterfacesOfResource(Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(notFoundInterfaces);

        when(userOperation.getUserData("jh0003", false)).thenReturn(Either.left(USER));

        when(elementOperation.getAllArtifactTypes()).thenReturn(getAllTypes());

        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VFC);

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        // createResource
        resourceResponse = createResourceObject(true);
        Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
        when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);
        when(toscaOperationFacade.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(StorageOperationStatus.OK);
        Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(emptyDataTypes));
        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        Either<Component, StorageOperationStatus> resourceStorageOperationStatusEither = Either
            .right(StorageOperationStatus.BAD_REQUEST);
        when(toscaOperationFacade.getToscaElement(resourceResponse.getUniqueId()))
            .thenReturn(resourceStorageOperationStatusEither);
    }

    @Test
    public void testValidJson() {
        ArtifactDefinition ad = createArtifactDef();

        String jsonArtifact = "";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            jsonArtifact = mapper.writeValueAsString(ad);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact,
            ArtifactDefinition.class, false);
        assertEquals(ad, afterConvert);
    }

    private ArtifactDefinition createArtifactDef() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1.yaml");
        ad.setArtifactLabel("label1");
        ad.setDescription("description");
        ad.setArtifactType(ArtifactTypeEnum.HEAT.getType());
        ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);
        return ad;
    }

    private Resource createResourceObject(boolean afterCreate) {
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<String>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);
        List<String> template = new ArrayList<String>();
        template.add("Root");
        resource.setDerivedFrom(template);
        resource.setVendorName("Motorola");
        resource.setVendorRelease("1.0.0");
        resource.setContactId("ya5467");
        resource.setIcon("MyIcon");

        if (afterCreate) {
            resource.setName(resource.getName());
            resource.setVersion("0.1");
            resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
            resource.setCreatorUserId(user.getUserId());
            resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        }
        return resource;
    }

    @Test
    public void testUpdateCIDeploymentArtifactTimeout() {
        ArtifactDefinition heatArtifact = new ArtifactDefinition();
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        ArtifactDefinition origEnvArtifact = new ArtifactDefinition();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId("ciid");
        ci.setDeploymentArtifacts(fillDeploymentArtifacts(heatArtifact, envArtifact, origEnvArtifact));
        GroupInstance groupInstance = new GroupInstance();
        groupInstance.setGroupInstanceArtifacts(new ArrayList<>(Arrays.asList(heatArtifact.getUniqueId(), envArtifact.getUniqueId())));
        groupInstance.setCustomizationUUID("custUid");
        groupInstance.setUniqueId("guid");
        List<GroupInstance> groupInstances = new ArrayList<>();
        groupInstances.addAll(Arrays.asList(groupInstance));
        ci.setGroupInstances(groupInstances);
        Service service = new Service();
        service.setComponentInstances(Collections.singletonList(ci));
        service.setUniqueId("suid");

        when(artifactToscaOperation.updateArtifactOnResource(heatArtifact, service,
            heatArtifact.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE.getNodeType(), ci.getUniqueId(), false)).thenReturn(
            Either.left(heatArtifact));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(service.getUniqueId(), ci.getUniqueId()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateGroupInstancesOnComponent(eq(service), eq(ci.getUniqueId()), any(List.class)))
            .thenReturn(Either.left(new ArrayList()));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstanceGroup(service.getUniqueId(), ci.getUniqueId(),
            new ArrayList<>(Arrays.asList("guid"))))
            .thenReturn(StorageOperationStatus.OK);
        artifactBL.handleUpdate(ci.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE,
            new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
            "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, service, true);
        assertThat(ci.getDeploymentArtifacts().get("HEAT").getTimeout()).isEqualTo(envArtifact.getTimeout());
        assertThat(ci.getDeploymentArtifacts().get("HEAT_ENV").getTimeout()).isEqualTo(origEnvArtifact.getTimeout());
    }

    @Test
    public void testUpdateCIDeploymentTimeout_invalidTimeout() {
        ArtifactDefinition heatArtifact = new ArtifactDefinition();
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        ArtifactDefinition origEnvArtifact = new ArtifactDefinition();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId("ciid");
        ci.setDeploymentArtifacts(fillDeploymentArtifacts(heatArtifact, envArtifact, origEnvArtifact));
        GroupInstance groupInstance = new GroupInstance();
        groupInstance.setGroupInstanceArtifacts(new ArrayList<>(Arrays.asList(heatArtifact.getUniqueId(), envArtifact.getUniqueId())));
        groupInstance.setCustomizationUUID("custUid");
        groupInstance.setUniqueId("guid");
        List<GroupInstance> groupInstances = new ArrayList<>();
        groupInstances.addAll(Arrays.asList(groupInstance));
        ci.setGroupInstances(groupInstances);
        Service service = new Service();
        service.setComponentInstances(Collections.singletonList(ci));
        service.setUniqueId("suid");
        envArtifact.setTimeout(130);

        when(artifactToscaOperation.updateArtifactOnResource(heatArtifact, service,
            heatArtifact.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE.getNodeType(), ci.getUniqueId(), false)).thenReturn(
            Either.left(heatArtifact));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(service.getUniqueId(), ci.getUniqueId()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateGroupInstancesOnComponent(eq(service), eq(ci.getUniqueId()), any(List.class)))
            .thenReturn(Either.left(new ArrayList()));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstanceGroup(service.getUniqueId(), ci.getUniqueId(),
            new ArrayList<>(Arrays.asList("guid"))))
            .thenReturn(StorageOperationStatus.OK);
        try {
            artifactBL.handleUpdate(ci.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE,
                new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
                "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, service, true);
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.ARTIFACT_INVALID_TIMEOUT);
            return;
        }
        fail();
    }

    @Test
    public void testUpdateCIDeploymentTimeout_negativeTimeout() {
        ArtifactDefinition heatArtifact = new ArtifactDefinition();
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        ArtifactDefinition origEnvArtifact = new ArtifactDefinition();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId("ciid");
        ci.setDeploymentArtifacts(fillDeploymentArtifacts(heatArtifact, envArtifact, origEnvArtifact));
        GroupInstance groupInstance = new GroupInstance();
        groupInstance.setGroupInstanceArtifacts(new ArrayList<>(Arrays.asList(heatArtifact.getUniqueId(), envArtifact.getUniqueId())));
        groupInstance.setCustomizationUUID("custUid");
        groupInstance.setUniqueId("guid");
        List<GroupInstance> groupInstances = new ArrayList<>();
        groupInstances.addAll(Arrays.asList(groupInstance));
        ci.setGroupInstances(groupInstances);
        Service service = new Service();
        service.setComponentInstances(Collections.singletonList(ci));
        service.setUniqueId("suid");
        envArtifact.setTimeout(-1);

        when(artifactToscaOperation.updateArtifactOnResource(heatArtifact, service,
            heatArtifact.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE.getNodeType(), ci.getUniqueId(), false)).thenReturn(
            Either.left(heatArtifact));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(service.getUniqueId(), ci.getUniqueId()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateGroupInstancesOnComponent(eq(service), eq(ci.getUniqueId()), any(List.class)))
            .thenReturn(Either.left(new ArrayList()));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstanceGroup(service.getUniqueId(), ci.getUniqueId(),
            new ArrayList<>(Arrays.asList("guid"))))
            .thenReturn(StorageOperationStatus.OK);
        try {
            artifactBL.handleUpdate(ci.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE,
                new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
                "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, service, true);
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.ARTIFACT_INVALID_TIMEOUT);
            return;
        }
        fail();
    }

    @Test
    public void testUpdateCIDeploymentArtifactTimeout_noUpdate() {
        ArtifactDefinition heatArtifact = new ArtifactDefinition();
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        ArtifactDefinition origEnvArtifact = new ArtifactDefinition();
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId("ciid");
        ci.setDeploymentArtifacts(fillDeploymentArtifacts(heatArtifact, envArtifact, origEnvArtifact));
        envArtifact.setTimeout(heatArtifact.getTimeout());
        GroupInstance groupInstance = new GroupInstance();
        groupInstance.setGroupInstanceArtifacts(new ArrayList<>(Arrays.asList(heatArtifact.getUniqueId(), envArtifact.getUniqueId())));
        groupInstance.setCustomizationUUID("custUid");
        groupInstance.setUniqueId("guid");
        List<GroupInstance> groupInstances = new ArrayList<>();
        groupInstances.addAll(Arrays.asList(groupInstance));
        ci.setGroupInstances(groupInstances);
        Service service = new Service();
        service.setComponentInstances(Collections.singletonList(ci));
        service.setUniqueId("suid");

        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(service.getUniqueId(), ci.getUniqueId()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateGroupInstancesOnComponent(eq(service), eq(ci.getUniqueId()), any(List.class)))
            .thenReturn(Either.left(new ArrayList()));
        artifactBL.handleUpdate(ci.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE,
            new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
            "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, service, true);
        assertThat(ci.getDeploymentArtifacts().get("HEAT").getTimeout()).isEqualTo(origEnvArtifact.getTimeout());
    }

    @Test
    public void testUpdateCIDeploymentArtifactTimeout_nonExistingArtifact() {
        ArtifactDefinition heatArtifact = new ArtifactDefinition();
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        ArtifactDefinition origEnvArtifact = new ArtifactDefinition();
        envArtifact.setTimeout(heatArtifact.getTimeout());
        envArtifact.setArtifactType("HEAT_ENV");
        envArtifact.setGeneratedFromId("uid1");
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId("ciid");
        ci.setDeploymentArtifacts(new HashMap<>());
        Service service = new Service();
        service.setComponentInstances(Collections.singletonList(ci));
        service.setUniqueId("suid");

        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(service.getUniqueId(), ci.getUniqueId()))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.updateGroupInstancesOnComponent(eq(service), eq(ci.getUniqueId()), any(List.class)))
            .thenReturn(Either.left(new ArrayList()));
        assertThatThrownBy(() -> {
            artifactBL.handleUpdate(ci.getUniqueId(), ComponentTypeEnum.RESOURCE_INSTANCE,
                new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
                "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, service, true);
        }).isInstanceOf(ComponentException.class);
    }

    @Test
    public void testUpdateCIDeploymentArtifactTimeout_invalidArtifactType() {
        ArtifactDefinition envArtifact = new ArtifactDefinition();
        envArtifact.setArtifactType("invalid");

        try {
            artifactBL.handleUpdate("uid", ComponentTypeEnum.RESOURCE_INSTANCE, new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE),
                "uid2", envArtifact, null, null, null, null, null, AuditingActionEnum.ARTIFACT_METADATA_UPDATE, user, null, true);
            fail();
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED);
            assertThat(exp.getParams()[0]).isEqualTo("invalid");
        }
    }

    private Map<String, ArtifactDefinition> fillDeploymentArtifacts(ArtifactDefinition heatArtifact, ArtifactDefinition envArtifact,
                                                                    ArtifactDefinition origEnvArtifact) {
        heatArtifact.setArtifactType("HEAT");
        heatArtifact.setTimeout(60);
        heatArtifact.setEsId("es");
        heatArtifact.setArtifactUUID("uuid1");
        heatArtifact.setUniqueId("uid1");
        envArtifact.setArtifactUUID("uuid2");
        envArtifact.setArtifactType("HEAT_ENV");
        envArtifact.setTimeout(30);
        envArtifact.setGenerated(true);
        envArtifact.setGeneratedFromId("uid1");
        envArtifact.setUniqueId("uid2");
        origEnvArtifact.setUniqueId("uid2");
        origEnvArtifact.setGeneratedFromId("uid1");
        origEnvArtifact.setArtifactType("HEAT_ENV");
        origEnvArtifact.setTimeout(60);
        origEnvArtifact.setGenerated(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        deploymentArtifacts.put(heatArtifact.getArtifactType(), heatArtifact);
        //deploymentArtifacts.put(envArtifact.getArtifactType(), envArtifact);
        deploymentArtifacts.put(envArtifact.getArtifactType(), origEnvArtifact);
        return deploymentArtifacts;
    }

    @Test
    public void testInvalidStringGroupType() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1");
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", "www");
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
            ArtifactDefinition.class, false);
        assertNull(afterConvert);
    }

    @Test
    public void testUpdateArtifactWithEmptyBody() {
        try {
            RepresentationUtils.convertJsonToArtifactDefinition("", ArtifactDefinition.class, true);
            fail();
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.MISSING_BODY);
        }
    }

    @Test
    public void testInvalidNumberGroupType() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1");
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", 123);
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
            ArtifactDefinition.class, false);
        assertNull(afterConvert);
    }

    @Test
    public void testMissingArtifactTypeValue() {
        ArtifactDefinition ad = new ArtifactDefinition();

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", ArtifactGroupTypeEnum.DEPLOYMENT.toString());
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().add("artifactType", null);
        try {
            RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class, true);
            fail();
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.MANDATORY_PROPERTY_MISSING_VALUE);
            assertThat(exp.getParams()[0]).isEqualTo("artifactType");
        }
    }

    @Test
    public void testMissingArtifactLabel() {
        ArtifactDefinition ad = new ArtifactDefinition();

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", ArtifactGroupTypeEnum.DEPLOYMENT.toString());
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        try {
            RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class, false);
            fail();
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.MISSING_MANDATORY_PROPERTY);
            assertThat(exp.getParams()[0]).isEqualTo("artifactLabel");
        }
    }

    @Test
    public void testMissingArtifactTimeout() {
        ArtifactDefinition ad = new ArtifactDefinition();

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", ArtifactGroupTypeEnum.DEPLOYMENT.toString());
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        try {
            RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class, true);
            fail();
        } catch (ComponentException exp) {
            assertThat(exp.getActionStatus()).isEqualTo(ActionStatus.MISSING_MANDATORY_PROPERTY);
            assertThat(exp.getParams()[0]).isEqualTo("timeout");
        }
    }


    @Test
    public void testInvalidGroupTypeWithSpace() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1");
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", " DEPLOYMENT");
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
            ArtifactDefinition.class, false);
        assertNull(afterConvert);
    }

    @Test
    public void testInvalidTimeoutWithSpace() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1");
        ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("timeout", "dfsdf15");
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", " DEPLOYMENT");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
            ArtifactDefinition.class, true);
        assertNull(afterConvert);
    }

    @Test
    public void testValidMibArtifactsConfiguration() {
        final ArtifactConfigManager artifactConfigManager = ArtifactConfigManager.getInstance();
        Optional<ArtifactConfiguration> artifactConfiguration = artifactConfigManager
            .find(ArtifactTypeEnum.SNMP_POLL.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, ComponentType.RESOURCE);
        assertThat(artifactConfiguration.isPresent()).isTrue();

        artifactConfiguration = artifactConfigManager
            .find(ArtifactTypeEnum.SNMP_TRAP.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, ComponentType.RESOURCE);
        assertThat(artifactConfiguration.isPresent()).isTrue();

        artifactConfiguration = artifactConfigManager
            .find(ArtifactTypeEnum.SNMP_POLL.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, ComponentType.RESOURCE_INSTANCE);
        assertThat(artifactConfiguration.isPresent()).isTrue();
        artifactConfiguration = artifactConfigManager
            .find(ArtifactTypeEnum.SNMP_TRAP.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, ComponentType.RESOURCE_INSTANCE);
        assertThat(artifactConfiguration.isPresent()).isTrue();
    }

    @Test
    public void testDownloadServiceArtifactByNames() {
        Service service = new Service();
        String serviceName = "myService";
        String serviceVersion = "1.0";
        String serviceId = "serviceId";
        service.setName(serviceName);
        service.setVersion(serviceVersion);
        service.setUniqueId(serviceId);

        String artifactName = "service-Myservice-template.yml";
        String artifactLabel = "assettoscatemplate";
        String esArtifactId = "123123dfgdfgd0";
        byte[] payload = "some payload".getBytes();
        ArtifactDefinition toscaTemplateArtifact = new ArtifactDefinition();
        toscaTemplateArtifact.setArtifactName(artifactName);
        toscaTemplateArtifact.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        toscaTemplateArtifact.setArtifactLabel(artifactLabel);
        toscaTemplateArtifact.setEsId(esArtifactId);
        toscaTemplateArtifact.setPayload(payload);

        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        toscaArtifacts.put(artifactLabel, toscaTemplateArtifact);
        service.setToscaArtifacts(toscaArtifacts);

        DAOArtifactData DAOArtifactData = new DAOArtifactData(esArtifactId);
        DAOArtifactData.setDataAsArray(payload);
        Either<DAOArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(DAOArtifactData);
        when(artifactCassandraDao.getArtifact(esArtifactId)).thenReturn(artifactfromESres);
        List<org.openecomp.sdc.be.model.Component> serviceList = new ArrayList<>();
        serviceList.add(service);
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> getServiceRes = Either
            .left(serviceList);
        when(toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName)).thenReturn(getServiceRes);
        byte[] downloadServiceArtifactByNamesRes = artifactBL
            .downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
        assertThat(downloadServiceArtifactByNamesRes != null
            && downloadServiceArtifactByNamesRes.length == payload.length).isTrue();
    }

    @Test
    public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
            .addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
            .addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2")).build();

        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(USER);
        when(artifactToscaOperation.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
            eq(component), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
            .thenReturn(Either.left(new ArtifactDefinition()));
        ArtifactDefinition heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
            new ArrayList<>(), heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component,
            Collections.emptyMap());
        assertNull(heatEnvPlaceHolder.getListHeatParameters());
    }

    @Test
    public void createHeatEnvPlaceHolder_resourceInstance_copyHeatParamasCurrValuesToHeatEnvDefaultVal()
        throws Exception {
        HeatParameterDefinition heatParam1 = ObjectGenerator.buildHeatParam("defVal1", "val1");
        HeatParameterDefinition heatParam2 = ObjectGenerator.buildHeatParam("defVal2", "val2");
        HeatParameterDefinition heatParam3 = ObjectGenerator.buildHeatParam("defVal3", "val3");
        ArtifactDefinition heatArtifact = new ArtifactBuilder().addHeatParam(heatParam1).addHeatParam(heatParam2)
            .addHeatParam(heatParam3).build();

        Resource component = new Resource();

        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(USER);
        when(artifactToscaOperation.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
            eq(component), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
            .thenReturn(Either.left(new ArtifactDefinition()));

        ArtifactDefinition heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
            new ArrayList<>(), heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component,
            Collections.emptyMap());

        ArtifactDefinition heatEnvArtifact = heatEnvPlaceHolder;
        List<HeatParameterDefinition> listHeatParameters = heatEnvArtifact.getListHeatParameters();
        assertEquals(listHeatParameters.size(), 3);
        verifyHeatParam(listHeatParameters.get(0), heatParam1);
        verifyHeatParam(listHeatParameters.get(1), heatParam2);
        verifyHeatParam(listHeatParameters.get(2), heatParam3);
    }

    @Test
    public void buildArtifactPayloadWhenShouldLockAndInTransaction() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(ARTIFACT_NAME);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
        artifactDefinition.setEsId(ES_ARTIFACT_ID);
        artifactDefinition.setPayload(PAYLOAD);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
            any(NodeTypeEnum.class), any(String.class), eq(true))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
            ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, true);
    }

    @Test
    public void buildArtifactPayloadWhenShouldLockAndNotInTransaction() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(ARTIFACT_NAME);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
        artifactDefinition.setEsId(ES_ARTIFACT_ID);
        artifactDefinition.setPayload(PAYLOAD);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
            any(NodeTypeEnum.class), any(String.class), eq(true))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
            ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, false);
        verify(janusGraphDao, times(1)).commit();
    }

    private ArtifactDefinition buildArtifactPayload() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(ARTIFACT_NAME);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
        artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
        artifactDefinition.setEsId(ES_ARTIFACT_ID);
        artifactDefinition.setPayload(PAYLOAD);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
            any(NodeTypeEnum.class), any(String.class), eq(true))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
            ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, false);
        verify(janusGraphDao, times(1)).commit();
        return artifactDefinition;
    }

    private void verifyHeatParam(HeatParameterDefinition heatEnvParam, HeatParameterDefinition heatYamlParam) {
        assertEquals(heatEnvParam.getDefaultValue(), heatYamlParam.getCurrentValue());
        assertNull(heatEnvParam.getCurrentValue());
    }

    //////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////new tests///////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    private ArtifactsBusinessLogic createTestSubject() {
        return getTestSubject();
    }

    @Test
    public void testCheckCreateFields() throws Exception {
        ArtifactsBusinessLogic testSubject;
        // User user = USER;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactGroupTypeEnum type = ArtifactGroupTypeEnum.DEPLOYMENT;

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "checkCreateFields", user, artifactInfo, type);
    }

    @Test
    public void testComposeArtifactId() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String resourceId = "";
        String artifactId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        String interfaceName = "";
        String operationName = "";
        String result;

        // test 1
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "composeArtifactId",
            new Object[]{resourceId, artifactId, artifactInfo, interfaceName, operationName});
    }

    @Test
    public void testConvertParentType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        NodeTypeEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "convertParentType", new Object[]{componentType});
    }

    @Test
    public void testConvertToOperation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        String operationName = "";
        Operation result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "convertToOperation",
            new Object[]{artifactInfo, operationName});
    }

    @Test
    public void testCreateInterfaceArtifactNameFromOperation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String operationName = "";
        String artifactName = "";
        String result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "createInterfaceArtifactNameFromOperation",
            new Object[]{operationName, artifactName});
    }

    @Test
    public void testFetchArtifactsFromComponent() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        Component component = createResourceObject(true);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "fetchArtifactsFromComponent",
            artifactId, component, artifacts);
    }

    @Test
    public void testValidateArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String componentId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String artifactId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;

        Component component = createResourceObject(true);
        boolean shouldLock = false;
        boolean inTransaction = false;

        // default test
        testSubject = createTestSubject();
        testSubject.validateArtifact(componentId, componentType, operation, artifactId, artifactInfo, auditingAction, user, component, shouldLock,
            inTransaction);
    }

    @Test
    public void testHandleHeatEnvDownload() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String componentId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;

        Component component = createResourceObject(true);
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        boolean shouldLock = false;
        boolean inTransaction = false;

        // default test
        testSubject = createTestSubject();
        testSubject.handleHeatEnvDownload(componentId, componentType, user, component, artifactInfo, shouldLock, inTransaction);
    }

    @Test
    public void testArtifactGenerationRequired() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "artifactGenerationRequired",
            new Object[]{component, artifactInfo});
    }

    @Test
    public void testUpdateGroupForHeat() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition artAfterUpdate = null;
        Component component = createResourceObject(true);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;

        // default test
        testSubject = createTestSubject();
        testSubject.updateGroupForHeat(artifactInfo, artifactInfo, component);
    }

    @Test
    public void testUpdateGroupForHeat_1() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Component component = createResourceObject(true);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;

        // default test
        testSubject = createTestSubject();
        testSubject.updateGroupForHeat(artifactInfo, artifactInfo, artifactInfo,
            artifactInfo, component);
    }


    @Test
    public void testHandleAuditing() throws Exception {
        ArtifactsBusinessLogic testSubject;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
        Component component = createResourceObject(true);
        String componentId = "";

        ArtifactDefinition artifactDefinition = buildArtifactPayload();
        String prevArtifactUuid = "";
        String currentArtifactUuid = "";
        ResponseFormat responseFormat = new ResponseFormat();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        String resourceInstanceName = "";

        // test 1
        testSubject = createTestSubject();
        testSubject.setComponentsUtils(MockGenerator.mockComponentUtils());
        testSubject.handleAuditing(auditingActionEnum, component, componentId, user, artifactDefinition,
            prevArtifactUuid, currentArtifactUuid, responseFormat, componentTypeEnum, resourceInstanceName);
    }

    @Test
    public void testIgnoreUnupdateableFieldsInUpdate() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifactInfo = null;

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "ignoreUnupdateableFieldsInUpdate",
            operation, artifactInfo, artifactInfo);
    }

    @Test
    public void testFindArtifactOnParentComponent() {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        String parentId = "";
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String artifactId = "";
        Either<ArtifactDefinition, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findArtifact", new Object[]{component,
            componentType, parentId, operation, artifactId});
    }


    @Test
    public void testValidateInformationalArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Component component = createResourceObject(true);
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateInformationalArtifact",
            new Object[]{artifactInfo, component});
    }


    @Test
    public void testGetUpdatedGroups() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        List<GroupDefinition> groups = new ArrayList<>();
        List<GroupDataDefinition> result;

        // test 1
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getUpdatedGroups", new Object[]{artifactId, artifactInfo, groups});
    }


    @Test
    public void testGetUpdatedGroupInstances() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        List<GroupDefinition> groups = new ArrayList<>();
        List<GroupInstance> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getUpdatedGroupInstances", new Object[]{artifactId, artifactInfo, groups});
    }


    @Test
    public void testFindArtifact_1() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        Component component = createResourceObject(true);
        String parentId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findArtifact",
            new Object[]{artifactId, component, parentId, componentType});
    }


    @Test
    public void testFetchArtifactsFromInstance() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        ComponentInstance instance = new ComponentInstance();

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "fetchArtifactsFromInstance", new Object[]{artifactId, artifacts, instance});
    }


    @Test
    public void testGenerateCustomizationUUIDOnInstance() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String componentId = "";
        String instanceId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        StorageOperationStatus result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "generateCustomizationUUIDOnInstance",
            new Object[]{componentId, instanceId, componentType});
    }

    @Test
    public void testFindComponentInstance() {
        ArtifactsBusinessLogic testSubject;
        String componentInstanceId = "";
        Component component = createResourceObject(true);
        ComponentInstance result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findComponentInstance",
            new Object[]{componentInstanceId, component});
    }

    @Test(expected = ComponentException.class)
    public void testDeploymentArtifactTypeIsLegalForParent_shouldThrowException() {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.AAI_SERVICE_MODEL;
        Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts = new HashMap<>();
        // test 1
        testSubject = createTestSubject();
        testSubject.validateDeploymentArtifactTypeIsLegalForParent(artifactInfo, artifactType, resourceDeploymentArtifacts);
    }

    @Test
    public void testLoadArtifactTypeConfig() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        //null artifactType
        Optional<ArtifactConfiguration> artifactConfiguration = artifactsBusinessLogic.loadArtifactTypeConfig(null);
        assertThat(artifactConfiguration.isPresent()).isFalse();
        //not configured artifactType
        artifactConfiguration = artifactsBusinessLogic.loadArtifactTypeConfig("NotConfiguredArtifactType");
        assertThat(artifactConfiguration.isPresent()).isFalse();

        //valid artifactType
        final String artifactType = ArtifactTypeEnum.YANG.getType();
        artifactConfiguration = artifactsBusinessLogic.loadArtifactTypeConfig(artifactType);
        assertThat(artifactConfiguration.isPresent()).isTrue();
        final ArtifactConfiguration artifactConfiguration1 = artifactConfiguration.get();
        assertThat(artifactConfiguration1.getType()).isEqualTo(artifactType);
        assertThat(artifactConfiguration1.getCategories()).hasSize(1);
        assertThat(artifactConfiguration1.getCategories()).contains(ArtifactGroupTypeEnum.INFORMATIONAL);
        assertThat(artifactConfiguration1.getComponentTypes()).hasSize(1);
        assertThat(artifactConfiguration1.getComponentTypes()).contains(ComponentType.RESOURCE);
        assertThat(artifactConfiguration1.getResourceTypes()).hasSize(11);
        assertThat(artifactConfiguration1.getResourceTypes())
            .contains(ResourceTypeEnum.VFC.getValue(), ResourceTypeEnum.CP.getValue(), ResourceTypeEnum.VL.getValue(),
                ResourceTypeEnum.VF.getValue(), ResourceTypeEnum.VFCMT.getValue(), "Abstract",
                ResourceTypeEnum.CVFC.getValue());
    }

    @Test
    public void testValidateArtifactExtension_acceptedExtension() {
        final ArtifactDefinition artifactInfo = new ArtifactDefinition();
        artifactInfo.setArtifactName("artifact.yml");
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        //empty accepted types
        assertThatCode(() -> artifactsBusinessLogic.validateArtifactExtension(new ArtifactConfiguration(), artifactInfo))
            .doesNotThrowAnyException();

        final ArtifactConfiguration artifactConfiguration = new ArtifactConfiguration();
        artifactConfiguration.setAcceptedTypes(Arrays.asList("yml", "yaml"));
        assertThatCode(() -> artifactsBusinessLogic.validateArtifactExtension(artifactConfiguration, artifactInfo))
            .doesNotThrowAnyException();
    }

    @Test(expected = ComponentException.class)
    public void testValidateArtifactExtension_notAcceptedExtension() {
        final ArtifactConfiguration artifactConfiguration = new ArtifactConfiguration();
        artifactConfiguration.setAcceptedTypes(Arrays.asList("yml", "yaml"));
        final ArtifactDefinition artifactInfo = new ArtifactDefinition();
        //not accepted extension
        artifactInfo.setArtifactName("artifact.xml");

        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();

        artifactsBusinessLogic.validateArtifactExtension(artifactConfiguration, artifactInfo);
    }

    @Test(expected = ComponentException.class)
    public void testValidateArtifactExtension_noExtension() {
        final ArtifactConfiguration artifactConfiguration = new ArtifactConfiguration();
        artifactConfiguration.setAcceptedTypes(Arrays.asList("yml", "yaml"));
        final ArtifactDefinition artifactInfo = new ArtifactDefinition();
        //no extension in the artifact name
        artifactInfo.setArtifactName("artifact");

        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();

        artifactsBusinessLogic.validateArtifactExtension(artifactConfiguration, artifactInfo);
    }

    @Test(expected = ComponentException.class)
    public void testValidateHeatEnvDeploymentArtifact_shouldThrowException() {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        String parentId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        // default test
        testSubject = createTestSubject();
        testSubject.validateHeatEnvDeploymentArtifact(component, parentId, artifactInfo);
    }

    @Test
    public void testFillArtifactPayloadValidation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Wrapper<byte[]> payloadWrapper = new Wrapper<>();
        ArtifactDefinition artifactDefinition = buildArtifactPayload();

        // default test
        testSubject = createTestSubject();
        testSubject.fillArtifactPayload(payloadWrapper, artifactDefinition);
    }

    @Test(expected = ByActionStatusComponentException.class)
    public void testHeatTimeoutValue() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        artifactInfo.setTimeout(1);
        artifactsBusinessLogic.validateHeatTimeoutValue(artifactInfo);
        artifactInfo.setTimeout(0);
        artifactsBusinessLogic.validateHeatTimeoutValue(artifactInfo);
    }

    @Test
    public void testValidateResourceType_resourceTypeIsAccepted() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        final List<String> typeList = Arrays
            .asList(ResourceTypeEnum.VF.getValue(), ResourceTypeEnum.PNF.getValue(), ResourceTypeEnum.VFC.getValue());
        assertThatCode(() -> {
            artifactsBusinessLogic.validateResourceType(ResourceTypeEnum.VF, artifactInfo, typeList);
        }).doesNotThrowAnyException();
    }

    @Test(expected = ComponentException.class)
    public void testValidateResourceType_invalidResourceType() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        final List<String> typeList = Collections.singletonList(ResourceTypeEnum.PNF.getValue());
        artifactsBusinessLogic.validateResourceType(ResourceTypeEnum.VF, artifactInfo, typeList);
    }

    @Test
    public void testValidateResourceType_emptyResourceTypeConfig_resourceTypeIsAccepted() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        assertThatCode(() -> {
            artifactsBusinessLogic.validateResourceType(ResourceTypeEnum.VF, artifactInfo, null);
        }).doesNotThrowAnyException();
        assertThatCode(() -> {
            artifactsBusinessLogic.validateResourceType(ResourceTypeEnum.VF, artifactInfo, new ArrayList<>());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testValidateAndConvertHeatParameters() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        String artifactType = "";
        Either<ArtifactDefinition, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        testSubject.validateAndConvertHeatParameters(artifactInfo, artifactType);
    }

    @Test
    public void testGetDeploymentArtifacts() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        NodeTypeEnum parentType = null;
        String ciId = "";
        List<ArtifactDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getDeploymentArtifacts(component, ciId);
    }


    @Test
    public void testValidateFirstUpdateHasPayload() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifact = null;
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateFirstUpdateHasPayload",
            new Object[]{artifactInfo, artifactInfo});
    }

    @Test
    public void testValidateAndSetArtifactname() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        testSubject.validateAndSetArtifactName(artifactInfo);
    }

    @Test(expected = ComponentException.class)
    public void testValidateArtifactType_notConfiguredArtifactType() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        artifactInfo.setArtifactType("notConfiguredType");
        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateArtifactType", artifactInfo, ComponentTypeEnum.RESOURCE);
    }

    @Test(expected = ComponentException.class)
    public void testValidateArtifactType_componentTypeNotSupportedByArtifactType() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        artifactInfo.setArtifactType(ArtifactTypeEnum.WORKFLOW.getType());

        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateArtifactType", artifactInfo, ComponentTypeEnum.RESOURCE);
    }

    @Test(expected = ComponentException.class)
    public void testValidateArtifactType_groupTypeNotSupportedByArtifactType() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = createTestSubject();
        final ArtifactDefinition artifactInfo = buildArtifactPayload();
        artifactInfo.setArtifactType(ArtifactTypeEnum.WORKFLOW.getType());
        artifactInfo.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);

        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateArtifactType", artifactInfo, ComponentTypeEnum.SERVICE);
    }

    @Test
    public void testValidateArtifactTypeNotChanged() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifact = null;
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateArtifactTypeNotChanged",
            new Object[]{artifactInfo, artifactInfo});
    }


    @Test
    public void testValidateOrSetArtifactGroupType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifact = null;
        Either<ArtifactDefinition, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateOrSetArtifactGroupType",
            new Object[]{artifactInfo, artifactInfo});
    }

    @Test
    public void testCheckAndSetUnUpdatableFields() throws Exception {
        ArtifactsBusinessLogic testSubject;

        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifact = null;
        ArtifactGroupTypeEnum type = null;

        // test 1
        testSubject = createTestSubject();
        type = null;
        Deencapsulation.invoke(testSubject, "checkAndSetUnUpdatableFields", user,
            artifactInfo, artifactInfo, ArtifactGroupTypeEnum.class);
    }

    @Test
    public void testCheckAndSetUnupdatableHeatParams() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<HeatParameterDefinition> heatParameters = new ArrayList<>();
        List<HeatParameterDefinition> currentParameters = new ArrayList<>();

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "checkAndSetUnupdatableHeatParams", new Object[]{heatParameters, currentParameters});
    }

    @Test
    public void testGetMapOfParameters() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<HeatParameterDefinition> currentParameters = new ArrayList<>();
        Map<String, HeatParameterDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getMapOfParameters", new Object[]{currentParameters});
    }

    @Test
    public void testGivenValidVesEventsArtifactPayload_WhenHandlePayload_ThenResultIsDecodedPayload() {
        final byte[] payload = "validYaml: yes".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "ves_events_file.yaml", ArtifactTypeEnum.VES_EVENTS);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = getTestSubject();

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
            new Object[]{artifactInfo, isArtifactMetadataUpdate});
        assertArrayEquals(payload, result.left().value());
    }


    @Test
    public void testGivenInValidVesEventsArtifactPayload_WhenHandlePayload_ThenResultIsInvalidYaml() {
        final int expectedStatus = 100;
        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_YAML), any(String.class))).thenReturn(new ResponseFormat(expectedStatus));
        final byte[] payload = "invalidYaml".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "ves_events_file.yaml", ArtifactTypeEnum.VES_EVENTS);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = getTestSubject();
        testSubject.setComponentsUtils(componentsUtils);

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
            new Object[]{artifactInfo, isArtifactMetadataUpdate});

        int status = result.right().value().getStatus();
        assertEquals(expectedStatus, status);
    }

    @Test
    public void testGivenEmptyVesEventsArtifactPayload_WhenHandlePayload_ThenResultIsMissingData() {
        final int expectedStatus = 101;
        when(componentsUtils.getResponseFormat(eq(ActionStatus.MISSING_DATA), any(String.class))).thenReturn(new ResponseFormat(expectedStatus));
        final byte[] payload = "".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "ves_events_file.yaml", ArtifactTypeEnum.VES_EVENTS);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = getTestSubject();
        testSubject.setComponentsUtils(componentsUtils);

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
            new Object[]{artifactInfo, isArtifactMetadataUpdate});

        int status = result.right().value().getStatus();
        assertEquals(expectedStatus, status);
    }


    @Test
    public void testGivenValidHeatArtifactPayload_WhenHandlePayload_ThenResultIsDecodedPayload() {
        final byte[] payload = "heat_template_version: 1.0".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "heat_template.yaml", ArtifactTypeEnum.HEAT);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = getTestSubject();

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
            new Object[]{artifactInfo, isArtifactMetadataUpdate});
        assertArrayEquals(payload, result.left().value());
    }

    @Test
    public void testGivenInValidHeatArtifactPayload_WhenHandlePayload_ThenResultIsInvalidYaml() {
        final int expectedStatus = 1000;
        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT), any(String.class))).thenReturn(
            new ResponseFormat(expectedStatus));
        final byte[] payload = "validYaml: butNoHeatTemplateVersion".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "heat_template.yaml", ArtifactTypeEnum.HEAT);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = getTestSubject();
        testSubject.setComponentsUtils(componentsUtils);

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
            new Object[]{artifactInfo, isArtifactMetadataUpdate});

        int status = result.right().value().getStatus();
        assertEquals(expectedStatus, status);
    }

    private ArtifactDefinition createArtifactInfo(byte[] payload, String artifactName, ArtifactTypeEnum artifactType) {
        ArtifactDefinition artifactInfo = new ArtifactDefinition();
        artifactInfo.setArtifactName(artifactName);
        artifactInfo.setArtifactType(artifactType.getType());
        artifactInfo.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        artifactInfo.setPayload(Base64.encodeBase64(payload));
        return artifactInfo;
    }

    @Test
    public void testValidateUserRole() throws Exception {
        ArtifactsBusinessLogic testSubject;

        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;
        String componentId = "";
        String artifactId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateUserRole",
            new Object[]{user, auditingAction, componentId, artifactId, componentType,
                operation});
        assertNull(result);
    }

    @Test
    public void testDetectAuditingType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String origMd5 = "";
        AuditingActionEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "detectAuditingType",
            new Object[]{operation, origMd5});
        assertNotNull(result);
    }

    @Test
    public void testDetectNoAuditingType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.LINK);
        String origMd5 = "";
        AuditingActionEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "detectAuditingType",
            new Object[]{operation, origMd5});
        assertNull(result);
    }

    @Test
    public void testCreateEsArtifactData() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDataDefinition artifactInfo = buildArtifactPayload();
        byte[] artifactPayload = new byte[]{' '};
        DAOArtifactData result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createEsArtifactData(artifactInfo, artifactPayload);
        assertNotNull(result);
    }


    @Test
    public void testIsArtifactMetadataUpdateTrue() throws Exception {
        ArtifactsBusinessLogic testSubject;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.ARTIFACT_METADATA_UPDATE;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isArtifactMetadataUpdate",
            new Object[]{auditingActionEnum});
        assertThat(result).isTrue();
    }

    @Test
    public void testIsArtifactMetadataUpdateFalse() throws Exception {
        ArtifactsBusinessLogic testSubject;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isArtifactMetadataUpdate",
            new Object[]{auditingActionEnum});
        assertThat(result).isFalse();
    }

    @Test
    public void testIsDeploymentArtifactTrue() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        artifactInfo.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isDeploymentArtifact", new Object[]{artifactInfo});
        assertThat(result).isTrue();
    }

    @Test
    public void testIsDeploymentArtifactFalse() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload(); // artifactGroupType == ArtifactGroupTypeEnum.TOSCA
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isDeploymentArtifact", new Object[]{artifactInfo});
        assertThat(result).isFalse();
    }

    @Test
    public void testSetArtifactPlaceholderCommonFields() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String resourceId = ES_ARTIFACT_ID;

        ArtifactDefinition artifactInfo = buildArtifactPayload();

        // test 1
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setArtifactPlaceholderCommonFields",
            resourceId, user, artifactInfo);
        assertEquals(resourceId + "." + ARTIFACT_LABEL, artifactInfo.getUniqueId());
        assertEquals(user.getFullName(), artifactInfo.getCreatorFullName());
    }


    @Test
    public void testCreateEsHeatEnvArtifactDataFromString() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactDefinition = buildArtifactPayload();
        String payloadStr = "";
        Either<DAOArtifactData, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "createEsHeatEnvArtifactDataFromString",
            new Object[]{artifactDefinition, payloadStr});
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    public void testUpdateArtifactOnGroupInstance() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        Component component = createResourceObject(true);
        String instanceId = "";
        String prevUUID = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Either<ArtifactDefinition, ResponseFormat> result;

        // test 1
        testSubject = createTestSubject();
        result = testSubject.updateArtifactOnGroupInstance(component, instanceId, prevUUID, artifactInfo, artifactInfo);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    public void testGenerateHeatEnvPayload() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactDefinition = buildArtifactPayload();
        String result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "generateHeatEnvPayload",
            new Object[]{artifactDefinition});
        assertThat(result.isEmpty()).isFalse();
    }


    @Test
    public void testBuildJsonForUpdateArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
        List<ArtifactTemplateInfo> updatedRequiredArtifacts = new ArrayList<>();
        Map<String, Object> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.buildJsonForUpdateArtifact(artifactInfo, artifactGroupType, updatedRequiredArtifacts);
        assertThat(MapUtils.isNotEmpty(result)).isTrue();
    }

    @Test
    public void testBuildJsonForUpdateArtifact_1() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String artifactId = "";
        String artifactName = "";
        String artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
        String label = "";
        String displayName = "";
        String description = "";
        byte[] artifactContent = new byte[]{' '};
        List<ArtifactTemplateInfo> updatedRequiredArtifacts = new ArrayList<>();
        List<HeatParameterDefinition> heatParameters = new ArrayList<>();
        Map<String, Object> result;

        // test 1
        testSubject = createTestSubject();
        artifactId = "";
        result = testSubject.buildJsonForUpdateArtifact(artifactId, artifactName, artifactType, artifactGroupType,
            label, displayName, description, artifactContent, updatedRequiredArtifacts, heatParameters);
        assertThat(MapUtils.isNotEmpty(result)).isTrue();
    }


    @Test
    public void testNotReplaceCurrHeatValueWithUpdatedValue() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<HeatParameterDefinition> currentHeatEnvParams = new ArrayList<>();
        List<HeatParameterDefinition> updatedHeatEnvParams = new ArrayList<>();

        // default test
        testSubject = createTestSubject();
        boolean result = Deencapsulation.invoke(testSubject, "replaceCurrHeatValueWithUpdatedValue",
            new Object[]{currentHeatEnvParams, updatedHeatEnvParams});
        assertThat(result).isFalse();
    }


    @Test
    public void testReplaceCurrHeatValueWithUpdatedValue() throws Exception {
        ArtifactsBusinessLogic testSubject;
        HeatParameterDefinition hpdOrig = new HeatParameterDefinition();
        hpdOrig.setName("param1");
        hpdOrig.setCurrentValue("value1");

        HeatParameterDefinition hpdUpd = new HeatParameterDefinition();
        hpdUpd.setName("param1");
        hpdUpd.setCurrentValue("value2");

        List<HeatParameterDefinition> currentHeatEnvParams = new ArrayList<>();
        currentHeatEnvParams.add(hpdOrig);

        List<HeatParameterDefinition> updatedHeatEnvParams = new ArrayList<>();
        updatedHeatEnvParams.add(hpdUpd);

        // default test
        testSubject = createTestSubject();
        boolean result = Deencapsulation.invoke(testSubject, "replaceCurrHeatValueWithUpdatedValue",
            new Object[]{currentHeatEnvParams, updatedHeatEnvParams});
        assertThat(result).isTrue();
        assertEquals(hpdUpd.getCurrentValue(), hpdOrig.getCurrentValue());
    }


    @Test
    public void testExtractArtifactDefinition() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactDefinition = buildArtifactPayload();
        Either<ArtifactDefinition, Operation> eitherArtifact = Either.left(artifactDefinition);
        ArtifactDefinition result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.extractArtifactDefinition(eitherArtifact);
        assertNotNull(result);
        assertEquals(artifactDefinition, result);
    }


    @Test
    public void testSetHeatCurrentValuesOnHeatEnvDefaultValues() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifact = buildArtifactPayload();
        ArtifactDefinition artifactInfo = new ArtifactDefinition();

        HeatParameterDefinition hpdOrig = new HeatParameterDefinition();
        hpdOrig.setName("param1");
        hpdOrig.setCurrentValue("value1");
        List<HeatParameterDefinition> currentHeatEnvParams = new ArrayList<>();
        currentHeatEnvParams.add(hpdOrig);
        artifact.setListHeatParameters(currentHeatEnvParams);

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setHeatCurrentValuesOnHeatEnvDefaultValues",
            artifact, artifactInfo);

        assertNotEquals(artifact, artifactInfo);
        assertEquals(1, artifact.getListHeatParameters().size());
        assertEquals(1, artifactInfo.getListHeatParameters().size());

        String hpdOrigCurrValue = artifact.getListHeatParameters().get(0).getCurrentValue();
        String hpdNewDefaultValue = artifactInfo.getListHeatParameters().get(0).getDefaultValue();

        assertEquals(hpdOrigCurrValue, hpdNewDefaultValue);
    }

    @Test
    public void testBuildHeatEnvFileNameArtifactNameNotNull() throws Exception {
        String heatEnvExt = "zip";
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition heatArtifact = buildArtifactPayload();
        ArtifactDefinition heatEnvArtifact = new ArtifactDefinition();
        Map<String, Object> placeHolderData = new HashMap<>();
        placeHolderData.put(ARTIFACT_PLACEHOLDER_FILE_EXTENSION, heatEnvExt);
        String artName = ARTIFACT_NAME.split("\\.")[0];

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "buildHeatEnvFileName", new Object[]{heatArtifact, heatEnvArtifact, placeHolderData});
        assertThat(heatEnvArtifact.getArtifactName().startsWith(artName)).isTrue();
        assertThat(heatEnvArtifact.getArtifactName().endsWith(heatEnvExt)).isTrue();
    }

    @Test
    public void testBuildHeatEnvFileNameArtifactNameIsNull() throws Exception {
        String heatEnvExt = "zip";
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition heatArtifact = buildArtifactPayload();
        heatArtifact.setArtifactName(null);
        ArtifactDefinition heatEnvArtifact = new ArtifactDefinition();
        Map<String, Object> placeHolderData = new HashMap<>();
        placeHolderData.put(ARTIFACT_PLACEHOLDER_FILE_EXTENSION, heatEnvExt);

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "buildHeatEnvFileName", new Object[]{heatArtifact, heatEnvArtifact, placeHolderData});
        assertThat(heatEnvArtifact.getArtifactName().startsWith(ARTIFACT_LABEL)).isTrue();
        assertThat(heatEnvArtifact.getArtifactName().endsWith(heatEnvExt)).isTrue();
    }

    @Test
    public void testHandleEnvArtifactVersion() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String existingVersion = "1.0";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Map<String, String> existingEnvVersions = new HashMap<>();
        existingEnvVersions.put(artifactInfo.getArtifactName(), existingVersion);

        // test 1
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "handleEnvArtifactVersion", artifactInfo, existingEnvVersions);
        assertEquals(existingVersion, artifactInfo.getArtifactVersion());
    }

    @Test
    public void testHandleArtifactsRequestForInnerVfcComponent() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<ArtifactDefinition> artifactsToHandle = new ArrayList<>();
        Resource component = createResourceObject(true);

        List<ArtifactDefinition> vfcsNewCreatedArtifacts = new ArrayList<>();
        ArtifactsBusinessLogic arb = getTestSubject();
        ArtifactOperationInfo operation = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        boolean shouldLock = false;
        boolean inTransaction = false;
        List<ArtifactDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.handleArtifactsForInnerVfcComponent(artifactsToHandle, component, user,
            vfcsNewCreatedArtifacts, operation, shouldLock, inTransaction);

        assertThat(CollectionUtils.isEmpty(result)).isTrue();
    }

    @Test
    public void testSetNodeTemplateOperation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        NodeTemplateOperation nodeTemplateOperation = new NodeTemplateOperation();

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setNodeTemplateOperation", nodeTemplateOperation);
        assertEquals(Deencapsulation.getField(testSubject, "nodeTemplateOperation"), nodeTemplateOperation);
    }


    @Test(expected = ComponentException.class)
    public void validateDeploymentArtifact_invalidComponentType() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Component component = new Resource();
        component.setComponentType(ComponentTypeEnum.PRODUCT);
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateDeploymentArtifact", artifactDefinition, component);
    }

    @Test(expected = ComponentException.class)
    public void validateDeploymentArtifact_notConfiguredArtifactType() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Component component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        artifactDefinition.setArtifactType("NotConfiguredType");
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateDeploymentArtifact", artifactDefinition, component);
    }

    @Test(expected = ComponentException.class)
    public void validateDeploymentArtifact_unsupportedResourceType() {
        final ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactType(ArtifactTypeEnum.ANSIBLE_PLAYBOOK.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        final Resource resourceComponent = new Resource();
        resourceComponent.setComponentType(ComponentTypeEnum.RESOURCE);
        resourceComponent.setResourceType(ResourceTypeEnum.ServiceProxy);
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        Deencapsulation
            .invoke(artifactsBusinessLogic, "validateDeploymentArtifact", artifactDefinition, resourceComponent);
    }

    @Test
    public void validateDeploymentArtifact_validArtifact() {
        final ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactType(ArtifactTypeEnum.YANG.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        final Resource resourceComponent = new Resource();
        resourceComponent.setComponentType(ComponentTypeEnum.RESOURCE);
        resourceComponent.setResourceType(ResourceTypeEnum.VF);
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        assertThatCode(() -> {
            Deencapsulation
                .invoke(artifactsBusinessLogic, "validateDeploymentArtifact", artifactDefinition, resourceComponent);
        }).doesNotThrowAnyException();

    }

    @Test
    public void validateHeatArtifact_validArtifact() {
        final ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactType(ArtifactTypeEnum.HEAT.getType());
        artifactDefinition.setTimeout(1);
        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        assertThatCode(() -> {
            Deencapsulation
                .invoke(artifactsBusinessLogic, "validateHeatArtifact", new Resource(), "componentId", artifactDefinition);
        }).doesNotThrowAnyException();
    }

    @Test
    public void validateInputForResourceInstance() {
        final String artifactId = "artifactId";
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setUniqueId(artifactId);
        artifactDefinition.setArtifactName(ARTIFACT_NAME);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.SNMP_POLL.getType());
        artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
        artifactDefinition.setEsId(ES_ARTIFACT_ID);
        artifactDefinition.setPayload(PAYLOAD);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL);
        artifactDefinition.setDescription("artifact description");
        artifactDefinition.setServiceApi(true);
        artifactDefinition.setApiUrl("dumbUrl");

        final User user = new User();
        user.setUserId("userId");
        user.setRole(Role.ADMIN.name());

        final String parentId = "parentId";
        final Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setUniqueId(parentId);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setLastUpdaterUserId(user.getUserId());

        final ArtifactOperationInfo operationInfo =
            new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);

        final String componentId = "componentId";
        final ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentId);
        componentInstance.setComponentUid(componentId);
        service.setComponentInstances(Collections.singletonList(componentInstance));

        final Resource resource = new Resource();
        when(toscaOperationFacade.getToscaFullElement(componentId)).thenReturn(Either.left(resource));
        when(artifactToscaOperation.getAllInstanceArtifacts(parentId, componentId)).thenReturn(Either.left(new HashMap<>()));

        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        artifactsBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        Object result = Deencapsulation
            .invoke(artifactsBusinessLogic, "validateInput", componentId, artifactDefinition, operationInfo, artifactId,
                user, "interfaceName", ARTIFACT_LABEL, ComponentTypeEnum.RESOURCE_INSTANCE, service);
        assertTrue(result instanceof Either<?, ?>);
        assertTrue(((Either<?, ?>) result).isLeft());

        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);

        result = Deencapsulation
            .invoke(artifactsBusinessLogic, "validateInput", componentId, artifactDefinition, operationInfo, artifactId,
                user, "interfaceName", ARTIFACT_LABEL, ComponentTypeEnum.RESOURCE_INSTANCE, service);
        assertTrue(result instanceof Either<?, ?>);
        assertTrue(((Either<?, ?>) result).isLeft());
    }

    @Test
    public void validateInputForResourceInstanceDeploymentArtifact() {
        final String artifactId = "artifactId";
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setUniqueId(artifactId);
        artifactDefinition.setArtifactName(ARTIFACT_NAME);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.SNMP_POLL.getType());
        artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
        artifactDefinition.setEsId(ES_ARTIFACT_ID);
        artifactDefinition.setPayload(PAYLOAD);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        artifactDefinition.setDescription("artifact description");

        final User user = new User();
        user.setUserId("userId");
        user.setRole(Role.ADMIN.name());

        final String parentId = "parentId";
        final Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setUniqueId(parentId);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setLastUpdaterUserId(user.getUserId());

        final ArtifactOperationInfo operationInfo =
            new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);

        final String componentId = "componentId";
        final ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentId);
        componentInstance.setComponentUid(componentId);
        service.setComponentInstances(Collections.singletonList(componentInstance));

        final Resource resource = new Resource();
        when(toscaOperationFacade.getToscaFullElement(componentId)).thenReturn(Either.left(resource));
        when(artifactToscaOperation.getAllInstanceArtifacts(parentId, componentId)).thenReturn(Either.left(new HashMap<>()));

        final ArtifactsBusinessLogic artifactsBusinessLogic = getTestSubject();
        artifactsBusinessLogic.setToscaOperationFacade(toscaOperationFacade);

        final Object result = Deencapsulation
            .invoke(artifactsBusinessLogic, "validateInput", componentId, artifactDefinition, operationInfo, artifactId,
                user, "interfaceName", ARTIFACT_LABEL, ComponentTypeEnum.RESOURCE_INSTANCE, service);
        assertTrue(result instanceof Either<?, ?>);
        assertTrue(((Either<?, ?>) result).isLeft());
    }


    @Test
    public void testHandleArtifactRequest() {

        String componentId = "componentId";
        ArtifactOperationInfo operationInfo = new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE);
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("other");
        artifactDefinition.setUniqueId("artifactId");
        artifactDefinition.setPayload("Test".getBytes());
        artifactDefinition.setArtifactLabel("other");
        artifactDefinition.setDescription("Test artifact");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.OTHER.getType());
        artifactDefinition.setArtifactUUID("artifactUId");
        artifactDefinition.setArtifactLabel("test");
        artifactDefinition.setArtifactDisplayName("Test");
        artifactDefinition.setEsId("esId");

        String requestMd5 = GeneralUtility.calculateMD5Base64EncodedByString("data");
        User user = new User();
        user.setUserId("userId");

        List<ComponentInstance> componentInstanceList = new ArrayList<>();
        List<InterfaceDefinition> interfaceDefinitionsList = new ArrayList<>();
        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        Map<String, Operation> operationsMap = new HashMap<>();
        artifactDefinitionMap.put("sample", artifactDefinition);

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(componentId);
        componentInstance.setDeploymentArtifacts(artifactDefinitionMap);
        componentInstanceList.add(componentInstance);

        Operation operation = new Operation();
        operation.setUniqueId("ouuid");
        operation.setName("operation1");
        operation.setImplementation(artifactDefinition);
        operationsMap.put("op1", operation);

        Map<String, InterfaceDefinition> interfaceDefinitions = new HashMap<>();

        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("iuuid");
        interfaceDefinition.setOperationsMap(operationsMap);
        interfaceDefinitions.put("iuuid", interfaceDefinition);

        interfaceDefinitionsList.add(interfaceDefinition);

        ResourceMetadataDataDefinition resourceMetadaData = new ResourceMetadataDataDefinition();
        resourceMetadaData.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
        resourceMetadaData.setLastUpdaterUserId(user.getUserId());
        Resource resource = new Resource(new ResourceMetadataDefinition(resourceMetadaData));
        resource.setComponentInstances(componentInstanceList);
        resource.setUniqueId(componentId);
        resource.setInterfaces(interfaceDefinitions);

        user.setRole(Role.ADMIN.name());

        when(userValidations.validateUserExists(Mockito.eq("userId")))
            .thenReturn(user);
        when(toscaOperationFacade.getToscaFullElement(any()))
            .thenReturn(Either.left(resource));
        when(artifactToscaOperation.getArtifactById(any(), any(), any(), any()))
            .thenReturn(Either.left(artifactDefinition));
        when(artifactsResolver.findArtifactOnComponent(any(), any(ComponentTypeEnum.class), anyString()))
            .thenReturn(artifactDefinition);
        when(graphLockOperation.lockComponent(eq(resource.getUniqueId()), any(NodeTypeEnum.class)))
            .thenReturn(StorageOperationStatus.OK);
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), anyString(), any(NodeTypeEnum.class), any(),
            anyBoolean()))
            .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any(DAOArtifactData.class))).thenReturn(CassandraOperationStatus.OK);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(resource));
        when(interfaceOperation.updateInterfaces(any(Component.class), anyList())).thenReturn(Either.left(interfaceDefinitionsList));
        when(artifactToscaOperation.getAllInstanceArtifacts(resource.getUniqueId(), componentId)).thenReturn(Either.left(artifactDefinitionMap));
        when(toscaOperationFacade.generateCustomizationUUIDOnInstance(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<ArtifactDefinition, Operation> result = artifactBL.handleArtifactRequest(componentId, user.getUserId(),
            ComponentTypeEnum.RESOURCE_INSTANCE
            , operationInfo, artifactDefinition.getUniqueId(), artifactDefinition, requestMd5, "data", "iuuid",
            null, componentId, "resources");

        assertThat(result.isLeft()).isTrue();
        ArtifactDefinition leftValue = result.left().value();
        assertEquals(artifactDefinition.getArtifactName(), leftValue.getArtifactName());
    }

    @Test
    public void testGenerateToscaArtifact() {

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setUniqueId("resourceId");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setUniqueId("artifactId");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_CSAR.getType());
        User user = new User();
        boolean inCertificationRequest = false;
        boolean fetchTemplatesFromDB = false;
        boolean shouldLock = false;
        boolean inTransaction = false;

        byte[] csar = "test.csar".getBytes();

        when(csarUtils.createCsar(any(Component.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(csar));
        when(
            artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(Component.class), anyString(), any(NodeTypeEnum.class),
                anyString(), anyBoolean()))
            .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any(DAOArtifactData.class)))
            .thenReturn(CassandraOperationStatus.OK);

        Either<ArtifactDefinition, Operation> result
            = artifactBL.generateAndSaveToscaArtifact(artifactDefinition, resource, user, inCertificationRequest,
            shouldLock, inTransaction, fetchTemplatesFromDB);

        Assert.assertEquals(artifactDefinition.getUniqueId(), result.left().value().getUniqueId());
    }

    @Test
    public void testHandleDownloadToscaModelRequest() {
        ArtifactsBusinessLogic testSubject = getTestSubject();
        byte[] generatedCsar = "test.csar".getBytes();

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);

        ArtifactDefinition csarArtifact = new ArtifactDefinition();
        csarArtifact.setArtifactName("csarArtifact");
        csarArtifact.setArtifactType(ArtifactTypeEnum.HEAT_ENV.getType());
        csarArtifact.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        when(csarUtils.createCsar(any(Component.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(generatedCsar));

        ImmutablePair<String, byte[]> result =
            testSubject.handleDownloadToscaModelRequest(resource, csarArtifact);

        assertEquals(csarArtifact.getArtifactName(), result.getKey());
    }

    @Test
    public void testHandleDownloadRequestById_returnsSuccessful() {
        String componentId = "componentId";
        String artifactId = "artifactId";
        String parentId = "parentId";

        DAOArtifactData daoArtifactData = new DAOArtifactData();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        Operation operation = new Operation();
        operation.setUniqueId("op1");

        artifactDefinition.setArtifactName("test.csar");
        artifactDefinition.setArtifactType(ComponentTypeEnum.RESOURCE.name());
        artifactDefinition.setArtifactType(ArtifactTypeEnum.HEAT.getType());
        artifactDefinition.setUniqueId(artifactId);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        daoArtifactData.setDataAsArray("data".getBytes());

        Resource resource = new Resource();
        resource.setUniqueId("resourceId");
        resource.setAbstract(false);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("interDef1", interfaceDefinition);

        artifactDefinitionMap.put("artifact1", artifactDefinition);

        resource.setDeploymentArtifacts(artifactDefinitionMap);

        User user = new User();
        user.setUserId("userId");

        when(userValidations.validateUserExists(eq(user.getUserId())))
            .thenReturn(user);
        when(toscaOperationFacade.getToscaFullElement(eq(componentId)))
            .thenReturn(Either.left(resource));
        when(artifactToscaOperation.getArtifactById(anyString(), anyString(), any(ComponentTypeEnum.class), anyString()))
            .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.getArtifact(any()))
            .thenReturn(Either.left(daoArtifactData));
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        ImmutablePair<String, byte[]> result =
            artifactBL.handleDownloadRequestById(componentId, artifactId, user.getUserId(), ComponentTypeEnum.RESOURCE,
                parentId, null);
        Assert.assertEquals(artifactDefinition.getArtifactName(), result.getKey());
    }

    @Test
    public void testHandleDownloadRequestById_givenUserIdIsNull_thenReturnsError() {
        String componentId = "componentId";
        String userId = null;
        String artifactId = "artifactId";

        try {
            ImmutablePair<String, byte[]> result =
                artifactBL.handleDownloadRequestById(componentId, artifactId, userId, ComponentTypeEnum.RESOURCE, componentId
                    , null);
        } catch (ComponentException e) {
            assertEquals(e.getActionStatus(), ActionStatus.MISSING_INFORMATION);
            return;
        }
        fail();
    }

    @Test
    public void testHandleGetArtifactByType_returnsSuccessful() {
        String parentId = "parentId";
        String componentId = "componentId";
        String artifactGroupType = ArtifactGroupTypeEnum.OTHER.name();
        String userId = "userId";

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("test.csar");

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put("artifact1", artifactDefinition);

        Service service = new Service();
        service.setUniqueId(componentId);

        when(toscaOperationFacade.getToscaElement(eq(componentId), any(ComponentParametersView.class)))
            .thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(eq(componentId), any(NodeTypeEnum.class)))
            .thenReturn(StorageOperationStatus.OK);
        when(artifactToscaOperation.getArtifacts(any(), any(NodeTypeEnum.class), any(ArtifactGroupTypeEnum.class), any()))
            .thenReturn(Either.left(artifactDefinitionMap));

        Map<String, ArtifactDefinition> result =
            artifactBL.handleGetArtifactsByType(ComponentTypeEnum.SERVICE.name(), parentId, ComponentTypeEnum.SERVICE,
                componentId, artifactGroupType, userId);
        Assert.assertEquals(artifactDefinition.getArtifactName(), result.get("artifact1").getArtifactName());
    }

    @Test
    public void testGetDeployment_returnsSuccessful() {

        Resource resource = new Resource();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        ComponentInstance componentInstance = new ComponentInstance();
        NodeTypeEnum parentType = NodeTypeEnum.ResourceInstance;
        String ciId = "ciId";

        artifactDefinition.setArtifactName("test.csar");
        componentInstance.setUniqueId(ciId);
        List<ComponentInstance> componentInstanceList = new ArrayList<>();
        componentInstanceList.add(componentInstance);

        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        deploymentArtifacts.put("test.csar", artifactDefinition);

        resource.setDeploymentArtifacts(deploymentArtifacts);
        resource.setComponentInstances(componentInstanceList);
        componentInstance.setDeploymentArtifacts(deploymentArtifacts);

        List<ArtifactDefinition> result = artifactBL.getDeploymentArtifacts(resource, ciId);
        assertThat(result.size() == 1).isTrue();
        Assert.assertEquals(artifactDefinition.getArtifactName(), result.get(0).getArtifactName());
    }

    @Test
    public void testHandleDelete_returnsSuccessful() {

        String parentId = "parentId";
        String artifactId = "artifactId";
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Resource resource = new Resource();
        User user = new User();

        artifactDefinition.setArtifactName("test.csar");
        artifactDefinition.setUniqueId(artifactId);
        artifactDefinition.setEsId("esId");

        ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();

        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        deploymentArtifacts.put(artifactId, artifactDefinition);

        resource.setUniqueId(parentId);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setDeploymentArtifacts(deploymentArtifacts);

        when(graphLockOperation.lockComponent(eq(parentId), any(NodeTypeEnum.class)))
            .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.getToscaElement(eq(parentId)))
            .thenReturn(Either.left(resource));
        when(artifactToscaOperation.isCloneNeeded(any(), any(ArtifactDefinition.class), any(NodeTypeEnum.class)))
            .thenReturn(Either.left(Boolean.FALSE));
        when(artifactToscaOperation.removeArtifactOnGraph(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class), anyBoolean()))
            .thenReturn(Either.left(artifactDataDefinition));
        when(artifactCassandraDao.deleteArtifact(any()))
            .thenReturn(CassandraOperationStatus.OK);

        Either<ArtifactDefinition, ResponseFormat> result = artifactBL.handleDelete(
            parentId, artifactId, user, resource, true, false);
        Assert.assertEquals(artifactDefinition.getArtifactName(), result.left().value().getArtifactName());
    }

    @Test
    public void testDownloadRsrcArtifactByNames_givenServiceNameNull_thenReturnsError() {
        String serviceName = null;
        String serviceVersion = "2.0";
        String resourceName = "resource";
        String resourceVersion = "1.0";
        String artifactName = "artifactName";

        try {
            artifactBL.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName, resourceVersion, artifactName);
        } catch (ComponentException e) {
            assertEquals(ActionStatus.INVALID_CONTENT, e.getActionStatus());
            return;
        }
        fail();

    }

    @Test
    public void testDownloadRsrcArtifactByNames_returnsSuccessful() {

        String serviceName = "service1";
        String resourceName = "resource1";
        String artifactName = "artifact1";
        String version = "1.0";

        Resource resource = new Resource();
        resource.setName(resourceName);
        resource.setVersion(version);

        Service service = new Service();
        service.setVersion(version);
        service.setName(serviceName);

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setEsId("esId");

        DAOArtifactData esArtifactData = new DAOArtifactData();
        esArtifactData.setDataAsArray("test".getBytes());

        artifactDefinition.setArtifactName(artifactName);
        List<Service> serviceList = new ArrayList<>();
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put(artifactName, artifactDefinition);

        serviceList.add(service);
        resource.setDeploymentArtifacts(artifacts);

        when(toscaOperationFacade.getComponentByNameAndVersion(eq(ComponentTypeEnum.RESOURCE), eq(resourceName), eq(version),
            eq(JsonParseFlagEnum.ParseMetadata)))
            .thenReturn(Either.left(resource));
        doReturn(Either.left(serviceList)).when(toscaOperationFacade).getBySystemName(eq(ComponentTypeEnum.SERVICE), eq(serviceName));
        when(artifactCassandraDao.getArtifact(any()))
            .thenReturn(Either.left(esArtifactData));

        byte[] result = artifactBL.downloadRsrcArtifactByNames(serviceName, version, resourceName, version, artifactName);
        Assert.assertEquals(esArtifactData.getDataAsArray(), result);
    }

    private ArtifactsBusinessLogic getTestSubject() {
        final ArtifactsBusinessLogic artifactsBusinessLogic = new ArtifactsBusinessLogic(artifactCassandraDao,
            toscaExportHandler, csarUtils, lifecycleBusinessLogic,
            userBusinessLogic, artifactsResolver, elementDao, groupOperation, groupInstanceOperation,
            groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation, artifactTypeOperation);
        artifactsBusinessLogic.setComponentsUtils(componentsUtils);
        return artifactsBusinessLogic;
    }
}