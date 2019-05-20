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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.MockGenerator;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

public class ArtifactsBusinessLogicTest {

    public static final User USER = new User("John", "Doh", "jh0003", "jh0003@gmail.com", "ADMIN",
            System.currentTimeMillis());
    public static final String RESOURCE_NAME = "My-Resource_Name with   space";
    public static final String RESOURCE_CATEGORY = "Network Layer 2-3/Router";
    public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    public static final String RESOURCE_SUBCATEGORY = "Router";
    public static final Resource resource = Mockito.mock(Resource.class);
    private static final String RESOURCE_INSTANCE_NAME = "Service-111";
    private static final String INSTANCE_ID = "S-123-444-ghghghg";
    private static final String ARTIFACT_NAME = "service-Myservice-template.yml";
    private static final String ARTIFACT_LABEL = "assettoscatemplate";
    private static final String ES_ARTIFACT_ID = "123123dfgdfgd0";
    private static final byte[] PAYLOAD = "some payload".getBytes();
    static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
    @InjectMocks
    private static ArtifactsBusinessLogic artifactBL;
    private static User user = null;
    private static Resource resourceResponse = null;
    private static ResponseFormatManager responseManager = null;
    final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    @Mock
    public ComponentsUtils componentsUtils;
    @Mock
    public ToscaOperationFacade toscaOperationFacade;
    JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    @Mock
    JanusGraphDao janusGraphDao;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ArtifactOperation artifactOperation;
    @Mock
    private IInterfaceLifecycleOperation lifecycleOperation;
    @Mock
    private IUserAdminOperation userOperation;
    @Mock
    private IElementOperation elementOperation;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    private NodeTemplateOperation nodeTemplateOperation;
    @Mock
    private ArtifactsOperations artifactsOperations;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private InterfaceOperation interfaceOperation;
    @Mock
    private UserValidations userValidations;
    @Mock
    private ArtifactsResolver artifactsResolver;
    @Mock
    private CsarUtils csarUtils;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static List<ArtifactType> getAllTypes() {
        List<ArtifactType> artifactTypes = new ArrayList<>();
        List<String> artifactTypesList = ConfigurationManager.getConfigurationManager().getConfiguration()
                .getArtifactTypes();
        for (String artifactType : artifactTypesList) {
            ArtifactType artifactT = new ArtifactType();
            artifactT.setName(artifactType);
            artifactTypes.add(artifactT);
        }
        return artifactTypes;
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
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

        Either<User, ActionStatus> getUserResult = Either.left(USER);

        when(userOperation.getUserData("jh0003", false)).thenReturn(getUserResult);

        Either<List<ArtifactType>, ActionStatus> getType = Either.left(getAllTypes());
        when(elementOperation.getAllArtifactTypes()).thenReturn(getType);

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
        Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<>();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));
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
                ArtifactDefinition.class);
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
    public void testInvalidStringGroupType() {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName("artifact1");
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);

        JsonElement jsonArtifact = gson.toJsonTree(ad);
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", "www");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class);
        assertNull(afterConvert);
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class);
        assertNull(afterConvert);
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class);
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
                ArtifactDefinition.class);
        assertNull(afterConvert);
    }

    @Test
    public void testValidMibAritactsConfiguration() {
        Map<String, ArtifactTypeConfig> componentDeploymentArtifacts = ConfigurationManager.getConfigurationManager()
                .getConfiguration().getResourceDeploymentArtifacts();
        Map<String, ArtifactTypeConfig> componentInstanceDeploymentArtifacts = ConfigurationManager
                .getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
        assertTrue(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType()));
        assertTrue(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType()));
        assertTrue(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType()));
        assertTrue(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType()));
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

        ESArtifactData esArtifactData = new ESArtifactData(esArtifactId);
        esArtifactData.setDataAsArray(payload);
        Either<ESArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(esArtifactData);
        when(artifactCassandraDao.getArtifact(esArtifactId)).thenReturn(artifactfromESres);
        List<org.openecomp.sdc.be.model.Component> serviceList = new ArrayList<>();
        serviceList.add(service);
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> getServiceRes = Either
                .left(serviceList);
        when(toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName)).thenReturn(getServiceRes);
        Either<byte[], ResponseFormat> downloadServiceArtifactByNamesRes = artifactBL
                .downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
        assertTrue(downloadServiceArtifactByNamesRes.isLeft());
        assertTrue(downloadServiceArtifactByNamesRes.left().value() != null
                && downloadServiceArtifactByNamesRes.left().value().length == payload.length);
    }

    @Test
    public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2")).build();

        Resource component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
        when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
                eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));
        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
                heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component,
                Collections.emptyMap());
        assertTrue(heatEnvPlaceHolder.isLeft());
        assertNull(heatEnvPlaceHolder.left().value().getListHeatParameters());
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

        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
        when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
                eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));

        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
                heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component,
                Collections.emptyMap());

        assertTrue(heatEnvPlaceHolder.isLeft());
        ArtifactDefinition heatEnvArtifact = heatEnvPlaceHolder.left().value();
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
        when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
                any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
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
        when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
                any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
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
        when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
                any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
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

    private ArtifactsBusinessLogic createTestSubject() {
        return new ArtifactsBusinessLogic();
    }

    @Test
    public void testBuildJsonStringForCsarVfcArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifact = new ArtifactDefinition();
        String result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "buildJsonStringForCsarVfcArtifact", new Object[]{artifact});
    }

    @Test
    public void testCheckArtifactInComponent() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Component component = new Resource();
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        String artifactId = "";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "checkArtifactInComponent",
                new Object[]{component, artifactId});
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
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String artifactId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;

        Component component = createResourceObject(true);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        boolean shouldLock = false;
        boolean inTransaction = false;
        ArtifactDefinition result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateArtifact", new Object[]{componentId, componentType, operation, artifactId, artifactInfo, auditingAction, user, component, component, errorWrapper, shouldLock, inTransaction});
    }

    @Test
    public void testHandleHeatEnvDownload() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String componentId = "";
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;

        Component component = createResourceObject(true);
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Either<ArtifactDefinition, ResponseFormat> validateArtifact = Either.left(artifactInfo);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        boolean shouldLock = false;
        boolean inTransaction = false;


        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "handleHeatEnvDownload", componentId, componentType, user, component, validateArtifact, errorWrapper, shouldLock, inTransaction);
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
        ActionStatus result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "updateGroupForHeat", new Object[]{artifactInfo,
                artifactInfo, component, componentType});
    }

    @Test
    public void testUpdateGroupForHeat_1() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Component component = createResourceObject(true);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        ActionStatus result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "updateGroupForHeat",
                new Object[]{artifactInfo, artifactInfo, artifactInfo,
                        artifactInfo, component, componentType});
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
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifactInfo = null;

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "ignoreUnupdateableFieldsInUpdate",
                operation, artifactInfo, artifactInfo);
    }

    @Test
    public void testFindArtifactOnParentComponent() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        String parentId = "";
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String artifactId = "";
        Either<ArtifactDefinition, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findArtifactOnParentComponent", new Object[]{component,
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
        Deencapsulation.invoke(testSubject, "fetchArtifactsFromInstance", artifactId, artifacts, instance);
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
    public void testFindComponentInstance() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String componentInstanceId = "";
        Component component = createResourceObject(true);
        ComponentInstance result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "findComponentInstance",
                new Object[]{componentInstanceId, component});
    }


    @Test
    public void testValidateDeploymentArtifactConf() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.AAI_SERVICE_MODEL;
        Map<String, ArtifactTypeConfig> resourceDeploymentArtifacts = new HashMap<>();


        // test 1
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "validateDeploymentArtifactConf", artifactInfo, responseWrapper, artifactType, resourceDeploymentArtifacts);
    }


    @Test
    public void testFillDeploymentArtifactTypeConf() throws Exception {
        ArtifactsBusinessLogic testSubject;
        NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
        Map<String, ArtifactTypeConfig> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "fillDeploymentArtifactTypeConf",
                new Object[]{parentType});
    }


    @Test
    public void testValidateArtifactTypeExists() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Wrapper<ResponseFormat> responseWrapper = null;
        ArtifactDefinition artifactInfo = buildArtifactPayload();

        // default test
        testSubject = createTestSubject();
        testSubject.validateArtifactTypeExists(responseWrapper, artifactInfo);
    }


    @Test
    public void testGetDeploymentArtifactTypeConfig() throws Exception {
        ArtifactsBusinessLogic testSubject;
        NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.AAI_SERVICE_MODEL;
        ArtifactTypeConfig result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "getDeploymentArtifactTypeConfig",
                new Object[]{parentType, artifactType});
    }


    @Test
    public void testValidateHeatEnvDeploymentArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Component component = createResourceObject(true);
        String parentId = "";
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateHeatEnvDeploymentArtifact",
                new Object[]{component, parentId, artifactInfo, parentType});
    }

    @Test
    public void testFillArtifactPayloadValidation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        Wrapper<byte[]> payloadWrapper = new Wrapper<>();
        ArtifactDefinition artifactDefinition = buildArtifactPayload();

        // default test
        testSubject = createTestSubject();
        testSubject.fillArtifactPayloadValidation(errorWrapper, payloadWrapper, artifactDefinition);
    }

    @Test
    public void testValidateValidYaml() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        ArtifactDefinition artifactInfo = buildArtifactPayload();


        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "validateValidYaml", errorWrapper, artifactInfo);
    }

    @Test
    public void testIsValidXml() throws Exception {
        ArtifactsBusinessLogic testSubject;
        byte[] xmlToParse = new byte[]{' '};
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isValidXml", new Object[]{xmlToParse});
    }

    @Test
    public void testValidateSingleDeploymentArtifactName() throws Exception {
        ArtifactsBusinessLogic testSubject;
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        String artifactName = "";
        Component component = createResourceObject(true);
        NodeTypeEnum parentType = null;


        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "validateSingleDeploymentArtifactName", errorWrapper, artifactName, component, NodeTypeEnum.class);
    }

    @Test
    public void testValidateHeatDeploymentArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        boolean isCreate = false;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        ArtifactDefinition currentArtifact = null;
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateHeatDeploymentArtifact",
                new Object[]{isCreate, artifactInfo, artifactInfo});
    }

    @Test
    public void testValidateResourceType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ResourceTypeEnum resourceType = ResourceTypeEnum.VF;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        List<String> typeList = new ArrayList<>();
        Either<Boolean, ResponseFormat> result;

        // test 1
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateResourceType", new Object[]{resourceType, artifactInfo, typeList});
    }


    @Test
    public void testValidateAndConvertHeatParamers() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        String artifactType = "";
        Either<ArtifactDefinition, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateAndConvertHeatParamers",
                new Object[]{artifactInfo, artifactType});
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
        result = testSubject.getDeploymentArtifacts(component, parentType, ciId);
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
        result = Deencapsulation.invoke(testSubject, "validateAndSetArtifactname",
                new Object[]{artifactInfo});
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
        Deencapsulation.invoke(testSubject, "checkAndSetUnupdatableHeatParams", heatParameters, currentParameters);
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
        ArtifactsBusinessLogic testSubject = new ArtifactsBusinessLogic();

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
        ArtifactsBusinessLogic testSubject = new ArtifactsBusinessLogic();
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
        ArtifactsBusinessLogic testSubject = new ArtifactsBusinessLogic();
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
        ArtifactsBusinessLogic testSubject = new ArtifactsBusinessLogic();

        Either<byte[], ResponseFormat> result = Deencapsulation.invoke(testSubject, "handlePayload",
                new Object[]{artifactInfo, isArtifactMetadataUpdate});
        assertArrayEquals(payload, result.left().value());
    }

    @Test
    public void testGivenInValidHeatArtifactPayload_WhenHandlePayload_ThenResultIsInvalidYaml() {
        final int expectedStatus = 1000;
        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT), any(String.class))).thenReturn(new ResponseFormat(expectedStatus));
        final byte[] payload = "validYaml: butNoHeatTemplateVersion".getBytes();
        ArtifactDefinition artifactInfo = createArtifactInfo(payload, "heat_template.yaml", ArtifactTypeEnum.HEAT);

        final boolean isArtifactMetadataUpdate = false;
        ArtifactsBusinessLogic testSubject = new ArtifactsBusinessLogic();
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
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        Either<Boolean, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "validateUserRole",
                new Object[]{user, auditingAction, componentId, artifactId, componentType,
                        operation});
    }

    @Test
    public void testDetectAuditingType() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        String origMd5 = "";
        AuditingActionEnum result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "detectAuditingType",
                new Object[]{operation, origMd5});
    }

    @Test
    public void testCreateEsArtifactData() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDataDefinition artifactInfo = buildArtifactPayload();
        byte[] artifactPayload = new byte[]{' '};
        ESArtifactData result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createEsArtifactData(artifactInfo, artifactPayload);
    }

    @Test
    public void testIsArtifactMetadataUpdate() throws Exception {
        ArtifactsBusinessLogic testSubject;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isArtifactMetadataUpdate",
                new Object[]{auditingActionEnum});
    }

    @Test
    public void testIsDeploymentArtifact() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "isDeploymentArtifact", new Object[]{artifactInfo});
    }


    @Test
    public void testSetArtifactPlaceholderCommonFields() throws Exception {
        ArtifactsBusinessLogic testSubject;
        String resourceId = "";

        ArtifactDefinition artifactInfo = buildArtifactPayload();

        // test 1
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setArtifactPlaceholderCommonFields",
                resourceId, user, artifactInfo);

    }


    @Test
    public void testCreateEsHeatEnvArtifactDataFromString() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactDefinition = buildArtifactPayload();
        String payloadStr = "";
        Either<ESArtifactData, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = Deencapsulation.invoke(testSubject, "createEsHeatEnvArtifactDataFromString",
                new Object[]{artifactDefinition, payloadStr});
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
        prevUUID = "";
        result = Deencapsulation.invoke(testSubject, "updateArtifactOnGroupInstance",
                new Object[]{componentType, component, instanceId, prevUUID, artifactInfo,
                        artifactInfo});

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
    }

    @Test
    public void testReplaceCurrHeatValueWithUpdatedValue() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<HeatParameterDefinition> currentHeatEnvParams = new ArrayList<>();
        List<HeatParameterDefinition> updatedHeatEnvParams = new ArrayList<>();


        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "replaceCurrHeatValueWithUpdatedValue", currentHeatEnvParams, updatedHeatEnvParams);
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
    }

    @Test
    public void testSetHeatCurrentValuesOnHeatEnvDefaultValues() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifact = null;
        ArtifactDefinition artifactInfo = buildArtifactPayload();

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setHeatCurrentValuesOnHeatEnvDefaultValues",
                artifactInfo, artifactInfo);
    }

    @Test
    public void testBuildHeatEnvFileName() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition heatArtifact = null;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Map<String, Object> placeHolderData = new HashMap<>();


        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "buildHeatEnvFileName", artifactInfo, artifactInfo, placeHolderData);
    }

    @Test
    public void testHandleEnvArtifactVersion() throws Exception {
        ArtifactsBusinessLogic testSubject;
        ArtifactDefinition artifactInfo = buildArtifactPayload();
        Map<String, String> existingEnvVersions = new HashMap<>();


        // test 1
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "handleEnvArtifactVersion", artifactInfo, existingEnvVersions);
    }

    @Test
    public void testHandleArtifactsRequestForInnerVfcComponent() throws Exception {
        ArtifactsBusinessLogic testSubject;
        List<ArtifactDefinition> artifactsToHandle = new ArrayList<>();
        Resource component = createResourceObject(true);

        List<ArtifactDefinition> vfcsNewCreatedArtifacts = new ArrayList<>();
        ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
        ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
        boolean shouldLock = false;
        boolean inTransaction = false;
        Either<List<ArtifactDefinition>, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.handleArtifactsRequestForInnerVfcComponent(artifactsToHandle, component, user,
                vfcsNewCreatedArtifacts, operation, shouldLock, inTransaction);
    }

    @Test
    public void testSetNodeTemplateOperation() throws Exception {
        ArtifactsBusinessLogic testSubject;
        NodeTemplateOperation nodeTemplateOperation = null;

        // default test
        testSubject = createTestSubject();
        Deencapsulation.invoke(testSubject, "setNodeTemplateOperation", NodeTemplateOperation.class);
    }

    @Test
    public void testHandleArtifactRequest() {

        String componentId = "componentId";
        ArtifactOperationInfo operationInfo = artifactBL.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE);
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("other");
        artifactDefinition.setUniqueId("artifactId");
        artifactDefinition.setPayload("Test".getBytes());
        artifactDefinition.setArtifactLabel("other");
        artifactDefinition.setDescription("Test artifact");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.OTHER.name());
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

        when(userValidations.validateUserExists(Mockito.eq("userId"), any(), anyBoolean()))
                .thenReturn(user);
        when(toscaOperationFacade.getToscaFullElement(any()))
                .thenReturn(Either.left(resource));
        when(artifactsOperations.getArtifactById(any(), any(), any(), any()))
                .thenReturn(Either.left(artifactDefinition));
        when(artifactsResolver.findArtifactOnComponent(any(), any(ComponentTypeEnum.class), anyString()))
                .thenReturn(artifactDefinition);
        when(graphLockOperation.lockComponent(eq(resource.getUniqueId()), any(NodeTypeEnum.class)))
                .thenReturn(StorageOperationStatus.OK);
        when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), anyString(), any(NodeTypeEnum.class), any()))
                .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any(ESArtifactData.class)))
                .thenReturn(CassandraOperationStatus.OK);
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(Either.left(resource));
        when(interfaceOperation.updateInterfaces(anyString(), anyList()))
                .thenReturn(Either.left(interfaceDefinitionsList));

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = artifactBL.handleArtifactRequest(componentId, user.getUserId(), ComponentTypeEnum.RESOURCE_INSTANCE
                , operationInfo, artifactDefinition.getUniqueId(), artifactDefinition, requestMd5, "data", "iuuid",
                "ouuid", componentId, "resources");

        Assert.assertTrue(result.isLeft());
        Either<ArtifactDefinition, Operation> leftResult = result.left().value();
        assertTrue(leftResult.isLeft());
        ArtifactDefinition leftValue = leftResult.left().value();
        assertEquals(artifactDefinition.getArtifactName(), leftValue.getArtifactName());
    }

    @Test
    public void testGenerateToscaArtifact() {

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setUniqueId("resourceId");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setUniqueId("artifactId");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_CSAR.name());
        User user = new User();
        boolean inCertificationRequest = false;
        boolean fetchTemplatesFromDB = false;
        boolean shouldLock = false;
        boolean inTransaction = false;

        byte[] csar = "test.csar".getBytes();

        when(csarUtils.createCsar(any(Component.class), anyBoolean(), anyBoolean()))
                .thenReturn(Either.left(csar));
        when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), anyString(), anyString(), any(NodeTypeEnum.class), anyString()))
                .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any(ESArtifactData.class)))
                .thenReturn(CassandraOperationStatus.OK);

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result
                = artifactBL.generateAndSaveToscaArtifact(artifactDefinition, resource, user, inCertificationRequest,
                shouldLock, inTransaction, fetchTemplatesFromDB);

        Assert.assertTrue(result.isLeft());
        Either<ArtifactDefinition, Operation> leftResult = result.left().value();

        Assert.assertEquals(artifactDefinition.getUniqueId(), leftResult.left().value().getUniqueId());
    }

    @Test
    public void testHandleDownloadToscaModelRequest() {

        byte[] generatedCsar = "test.csar".getBytes();

        Resource resource = new Resource();
        resource.setComponentType(ComponentTypeEnum.RESOURCE);

        ArtifactDefinition csarArtifact = new ArtifactDefinition();
        csarArtifact.setArtifactName("csarArtifact");
        csarArtifact.setArtifactType(ArtifactTypeEnum.HEAT_ENV.name());
        csarArtifact.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        when(csarUtils.createCsar(any(Component.class), anyBoolean(), anyBoolean()))
                .thenReturn(Either.left(generatedCsar));

        Either<ImmutablePair<String, byte[]>, ResponseFormat> result =
                artifactBL.handleDownloadToscaModelRequest(resource, csarArtifact);

        ImmutablePair<String, byte[]> leftResult = result.left().value();
        assertEquals(csarArtifact.getArtifactName(), leftResult.getKey());
    }

    @Test
    public void testHandleDownloadRequestById_returnsSuccessful() {
        String componentId = "componentId";
        String artifactId = "artifactId";
        String parentId = "parentId";

        ESArtifactData esArtifactData = new ESArtifactData();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        Operation operation = new Operation();
        operation.setUniqueId("op1");

        artifactDefinition.setArtifactName("test.csar");
        artifactDefinition.setArtifactType(ComponentTypeEnum.RESOURCE.name());
        artifactDefinition.setArtifactType(ArtifactTypeEnum.HEAT.name());
        artifactDefinition.setUniqueId(artifactId);
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

        esArtifactData.setDataAsArray("data".getBytes());

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

        when(userValidations.validateUserExists(eq(user.getUserId()), any(), anyBoolean()))
                .thenReturn(user);
        when(toscaOperationFacade.getToscaFullElement(eq(componentId)))
                .thenReturn(Either.left(resource));
        when(artifactsOperations.getArtifactById(anyString(), anyString(), any(ComponentTypeEnum.class), anyString()))
                .thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.getArtifact(any()))
                .thenReturn(Either.left(esArtifactData));

        Either<ImmutablePair<String, byte[]>, ResponseFormat> result =
                artifactBL.handleDownloadRequestById(componentId, artifactId, user.getUserId(), ComponentTypeEnum.RESOURCE,
                        parentId, null);
        ImmutablePair<String, byte[]> leftResult = result.left().value();
        Assert.assertEquals(artifactDefinition.getArtifactName(), leftResult.getKey());
    }

    @Test
    public void testHandleDownloadRequestById_givenUserIdIsNull_thenReturnsError() {
        String componentId = "componentId";
        String userId = null;
        String artifactId = "artifactId";

        Either<ImmutablePair<String, byte[]>, ResponseFormat> result =
                artifactBL.handleDownloadRequestById(componentId, artifactId, userId, ComponentTypeEnum.RESOURCE, componentId
                        , null);
        Assert.assertTrue(result.isRight());
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
        when(artifactsOperations.getArtifacts(any(), any(NodeTypeEnum.class), any(ArtifactGroupTypeEnum.class), any()))
                .thenReturn(Either.left(artifactDefinitionMap));

        Either<Map<String, ArtifactDefinition>, ResponseFormat> result =
                artifactBL.handleGetArtifactsByType(ComponentTypeEnum.SERVICE.name(), parentId, ComponentTypeEnum.SERVICE,
                        componentId, artifactGroupType, userId);
        Map<String, ArtifactDefinition> leftResult = result.left().value();
        Assert.assertEquals(artifactDefinition.getArtifactName(), leftResult.get("artifact1").getArtifactName());
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

        List<ArtifactDefinition> result = artifactBL.getDeploymentArtifacts(resource, parentType, ciId);
        Assert.assertTrue(result.size() == 1);
        Assert.assertEquals(artifactDefinition.getArtifactName(), result.get(0).getArtifactName());
    }

    @Test
    public void testHandleDelete_returnsSuccessful() {

        String parentId = "parentId";
        String artifactId = "artifactId";
        AuditingActionEnum auditingAction = AuditingActionEnum.ARTIFACT_DELETE;
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Resource resource = new Resource();
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        boolean shouldUnlock = true;
        boolean inTransaction = false;
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
        when(artifactsOperations.isCloneNeeded(any(), any(ArtifactDefinition.class), any(NodeTypeEnum.class)))
                .thenReturn(Either.left(Boolean.FALSE));
        when(artifactsOperations.removeArtifactOnGraph(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class), anyBoolean()))
                .thenReturn(Either.left(artifactDataDefinition));
        when(artifactCassandraDao.deleteArtifact(any()))
                .thenReturn(CassandraOperationStatus.OK);

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result = artifactBL.handleDelete(parentId, artifactId, user, auditingAction, componentType, resource, shouldUnlock, inTransaction);
        Either<ArtifactDefinition, Operation> leftValue = result.left().value();
        Assert.assertEquals(artifactDefinition.getArtifactName(), leftValue.left().value().getArtifactName());
    }

    @Test
    public void testDownloadRsrcArtifactByNames_givenServiceNameNull_thenReturnsError() {
        String serviceName = null;
        String serviceVersion = "2.0";
        String resourceName = "resource";
        String resourceVersion = "1.0";
        String artifactName = "artifactName";
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(007);

        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_CONTENT)))
                .thenReturn(responseFormat);
        Either<byte[], ResponseFormat> result = artifactBL.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName, resourceVersion, artifactName);
        assertEquals(responseFormat.getStatus(), result.right().value().getStatus());
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

        ESArtifactData esArtifactData = new ESArtifactData();
        esArtifactData.setDataAsArray("test".getBytes());

        artifactDefinition.setArtifactName(artifactName);
        List<Service> serviceList = new ArrayList<>();
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put(artifactName, artifactDefinition);

        serviceList.add(service);
        resource.setDeploymentArtifacts(artifacts);

        when(toscaOperationFacade.getComponentByNameAndVersion(eq(ComponentTypeEnum.RESOURCE), eq(resourceName), eq(version), eq(JsonParseFlagEnum.ParseMetadata)))
                .thenReturn(Either.left(resource));
        doReturn(Either.left(serviceList)).when(toscaOperationFacade).getBySystemName(eq(ComponentTypeEnum.SERVICE), eq(serviceName));
        when(artifactCassandraDao.getArtifact(any()))
                .thenReturn(Either.left(esArtifactData));

        Either<byte[], ResponseFormat> result = artifactBL.downloadRsrcArtifactByNames(serviceName, version, resourceName, version, artifactName);
        byte[] data = result.left().value();
        Assert.assertEquals(esArtifactData.getDataAsArray(), data);
    }
}