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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
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
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_ENV_NAME;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_VF_ENV_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactBusinessLogicTest extends BaseBusinessLogicMock{

    public static final User USER = new User("John", "Doh", "jh0003", "jh0003@gmail.com", "ADMIN", System.currentTimeMillis());
    private final static String RESOURCE_INSTANCE_NAME = "Service-111";
    private final static String INSTANCE_ID = "S-123-444-ghghghg";

    private final static String ARTIFACT_NAME = "service-Myservice-template.yml";
    private final static String ARTIFACT_LABEL = "assettoscatemplate";
    private final static String ES_ARTIFACT_ID = "123123dfgdfgd0";
    private final static byte[] PAYLOAD = "some payload".getBytes();

    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @InjectMocks
    private ArtifactsBusinessLogic artifactBL;
    @Mock
    private ArtifactOperation artifactOperation;
    @Mock
    public ComponentsUtils componentsUtils;
    @Mock
    private IInterfaceLifecycleOperation lifecycleOperation;
    @Mock
    private IUserAdminOperation userOperation;
    @Mock
    private IElementOperation elementOperation;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    public ToscaOperationFacade toscaOperationFacade;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private NodeTemplateOperation nodeTemplateOperation;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    JanusGraphDao janusGraphDao;
    @Mock
    private IInterfaceLifecycleOperation interfaceLifecycleOperation;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private User user;
    @Mock
    private ToscaExportHandler toscaExportHandler;
    @Mock
    private CsarUtils csarUtils;
    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private ArtifactsResolver artifactsResolver;

    public static final Resource resource = Mockito.mock(Resource.class);
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();


    private static List<ArtifactType> getAllTypes() {
        List<ArtifactType> artifactTypes = new ArrayList<>();
        List<String> artifactTypesList = ConfigurationManager.getConfigurationManager().getConfiguration().getArtifactTypes();
        for (String artifactType : artifactTypesList) {
            ArtifactType artifactT = new ArtifactType();
            artifactT.setName(artifactType);
            artifactTypes.add(artifactT);
        }
        return artifactTypes;
    }

    @Before
    public void initMocks() {
//        MockitoAnnotations.initMocks(this);
        Either<ArtifactDefinition, StorageOperationStatus> NotFoundResult = Either.right(StorageOperationStatus.NOT_FOUND);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> NotFoundResult2 = Either.right(StorageOperationStatus.NOT_FOUND);
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> notFoundInterfaces = Either.right(StorageOperationStatus.NOT_FOUND);
        Either<User, ActionStatus> getUserResult = Either.left(USER);

        Either<List<ArtifactType>, ActionStatus> getType = Either.left(getAllTypes());

        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VFC);
    }

    @Test
    public void testValidJson() {
        ArtifactDefinition ad = createArtifactDef("artifact1.yml", ArtifactGroupTypeEnum.DEPLOYMENT);

        String jsonArtifact  = "";

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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact, ArtifactDefinition.class);
        assertThat(afterConvert).isEqualTo(ad);
    }

    private ArtifactDefinition createArtifactDef(String artifactName, ArtifactGroupTypeEnum groupTypeEnum) {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setArtifactName(artifactName);
        ad.setArtifactLabel("label1");
        ad.setDescription("description");
        ad.setArtifactType(ArtifactTypeEnum.HEAT.getType());
        ad.setArtifactGroupType(groupTypeEnum);
        ad.setCreationDate(System.currentTimeMillis());
        ad.setMandatory(false);
        ad.setTimeout(15);
        return ad;
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
        assertThat(afterConvert).isNull();
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
        assertThat(afterConvert).isNull();
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
        assertThat(afterConvert).isNull();
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
        assertThat(afterConvert).isNull();
    }

    @Test
    public void validateArtifactNameUniqueness_Unique() {

        ArtifactDefinition artifactInfo = createArtifactDef("artifactName3.yml", ArtifactGroupTypeEnum.DEPLOYMENT);
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put("informationArtifact", informationArtifactDefinition1);
        artifactDefinitionMap.put("DeploymentArtifact", deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getAllInstanceArtifacts(anyString(), anyString())).thenReturn(artifacts);

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent,
                artifactInfo , ComponentTypeEnum.RESOURCE_INSTANCE)).isTrue();
    }

    @Test
    public void validateArtifactNameUniqueness_nonUniqueResourceInterfaces() {

        ArtifactDefinition artifactInfo = createArtifactDef("artifactName3.yml", ArtifactGroupTypeEnum.DEPLOYMENT);
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put("informationArtifact", informationArtifactDefinition1);
        artifactDefinitionMap.put("DeploymentArtifact", deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getArtifacts(anyString())).thenReturn(artifacts);


        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource =
                Either.left(createInterfaceDefinitionMap("artifactName3.yml"));
        when(interfaceLifecycleOperation.getAllInterfacesOfResource("componentId", true, true))
                .thenReturn(allInterfacesOfResource);

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent,
                artifactInfo, ComponentTypeEnum.RESOURCE)).isFalse();
    }

    @Test
    public void validateArtifactNameUniqueness_UniqueInterface() {

        ArtifactDefinition artifactInfo = createArtifactDef("artifactName2.yml", ArtifactGroupTypeEnum.DEPLOYMENT);
        artifactInfo.setArtifactLabel("uniqueLabel");
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put(informationArtifactDefinition1.getArtifactLabel(), informationArtifactDefinition1);
        artifactDefinitionMap.put(deploymentArtifactDefinition2.getArtifactLabel(), deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getArtifacts(anyString())).thenReturn(artifacts);


        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> allInterfacesOfResource =
                Either.left(createInterfaceDefinitionMap("artifactName3.yml"));

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent,
                artifactInfo, ComponentTypeEnum.RESOURCE)).isFalse();
    }

    @Test
    public void validateArtifactNameUniqueness_updateName() {
        //artifacts with the same name have the same label
        ArtifactDefinition artifactInfo = createArtifactDef("artifactName2.yml", ArtifactGroupTypeEnum.DEPLOYMENT);
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        informationArtifactDefinition1.setArtifactLabel("label2");
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put(artifactInfo.getArtifactLabel(), artifactInfo);
        artifactDefinitionMap.put(informationArtifactDefinition1.getArtifactLabel(), informationArtifactDefinition1);
        artifactDefinitionMap.put(deploymentArtifactDefinition2.getArtifactLabel(), deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getAllInstanceArtifacts(anyString(), anyString())).thenReturn(artifacts);

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent,
                artifactInfo, ComponentTypeEnum.RESOURCE_INSTANCE)).isTrue();
    }

    @Test
    public void validateArtifactNameUniqueness_UniqueInGroupType() {

        ArtifactDefinition artifactInfo = createArtifactDef("artifactName2.yml", ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put("informationArtifact", informationArtifactDefinition1);
        artifactDefinitionMap.put("DeploymentArtifact", deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getAllInstanceArtifacts(anyString(), anyString())).thenReturn(artifacts);

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent, artifactInfo,
                ComponentTypeEnum.RESOURCE_INSTANCE)).isTrue();
    }

    @Test
    public void validateArtifactNameUniqueness_NonUnique() {

        ArtifactDefinition artifactInfo = createArtifactDef("artifactName1.yml", ArtifactGroupTypeEnum.INFORMATIONAL);
        artifactInfo.setArtifactLabel("artifactLabel");
        ArtifactDefinition informationArtifactDefinition1 = createArtifactDef("artifactName1.yml",
                ArtifactGroupTypeEnum.INFORMATIONAL);
        ArtifactDefinition deploymentArtifactDefinition2 = createArtifactDef("artifactName2.yml",
                ArtifactGroupTypeEnum.DEPLOYMENT);

        Map<String, ArtifactDefinition> artifactDefinitionMap = new HashMap<>();
        artifactDefinitionMap.put("informationArtifact", informationArtifactDefinition1);
        artifactDefinitionMap.put("DeploymentArtifact", deploymentArtifactDefinition2);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifacts = Either.left(artifactDefinitionMap);
        when(artifactToscaOperation.getAllInstanceArtifacts(anyString(), anyString())).thenReturn(artifacts);

        Resource parent = new Resource();
        parent.setUniqueId("uniqueId");

        assertThat(artifactBL.validateArtifactNameUniqueness("componentId", parent, artifactInfo,
                ComponentTypeEnum.RESOURCE_INSTANCE)).isFalse();
    }


    private Map<String, InterfaceDefinition> createInterfaceDefinitionMap(String artifactName) {

        InterfaceDefinition id1 = new InterfaceDefinition();
        Map<String, Operation> operationMap = new HashMap<>();
        Operation operation1 = new Operation();
        ArtifactDataDefinition dataImplementation = new ArtifactDataDefinition();
        dataImplementation.setArtifactName(artifactName);
        operation1.setImplementation(dataImplementation);
        operationMap.put("operation1", operation1);
        id1.setOperationsMap(operationMap);
        Map<String, InterfaceDefinition> interfaceDefMap = new HashMap<>();
        interfaceDefMap.put("id1",id1);
        return interfaceDefMap;
    }

    @Test
    public void testValidMibAritactsConfiguration() {
        Map<String, ArtifactTypeConfig> componentDeploymentArtifacts =
                ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts();
        Map<String, ArtifactTypeConfig> componentInstanceDeploymentArtifacts =
                ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
        assertThat(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType())).isTrue();
        assertThat(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType())).isTrue();
        assertThat(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType())).isTrue();
        assertThat(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType())).isTrue();
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

        ESArtifactData esArtifactData =new ESArtifactData(esArtifactId);
        esArtifactData.setDataAsArray(payload);
        Either<ESArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(esArtifactData);
        when(artifactCassandraDao.getArtifact(esArtifactId)).thenReturn(artifactfromESres);
        List<org.openecomp.sdc.be.model.Component> serviceList = new ArrayList<>();
        serviceList.add(service);
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> getServiceRes = Either.left(serviceList);
        when(toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName)).thenReturn(getServiceRes);
        Either<byte[], ResponseFormat> downloadServiceArtifactByNamesRes =
                artifactBL.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
        assertThat(downloadServiceArtifactByNamesRes.isLeft()).isTrue();
        assertThat(downloadServiceArtifactByNamesRes.left().value() !=null &&
                downloadServiceArtifactByNamesRes.left().value().length == payload.length).isTrue();
    }

    @Test
    public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2"))
                .build();

        Resource component = new Resource();
        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
        when(artifactToscaOperation.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class), eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));
        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component, Collections.emptyMap());
        assertThat(heatEnvPlaceHolder.isLeft()).isTrue();
        assertThat(heatEnvPlaceHolder.left().value().getListHeatParameters()).isNull();
    }

    @Test
    public void createHeatEnvPlaceHolder_resourceInstance_copyHeatParamasCurrValuesToHeatEnvDefaultVal() throws Exception {
        HeatParameterDefinition heatParam1 = ObjectGenerator.buildHeatParam("defVal1", "val1");
        HeatParameterDefinition heatParam2 = ObjectGenerator.buildHeatParam("defVal2", "val2");
        HeatParameterDefinition heatParam3 = ObjectGenerator.buildHeatParam("defVal3", "val3");
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
                .addHeatParam(heatParam1)
                .addHeatParam(heatParam2)
                .addHeatParam(heatParam3)
                .build();

        Resource component = new Resource();

        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));

        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component, Collections.emptyMap());

        assertThat(heatEnvPlaceHolder.isLeft()).isTrue();
        ArtifactDefinition heatEnvArtifact = heatEnvPlaceHolder.left().value();
        List<HeatParameterDefinition> listHeatParameters = heatEnvArtifact.getListHeatParameters();
        assertThat(listHeatParameters.size()).isEqualTo(3);
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
        //TODO Remove if passes
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class)
                , any(String.class))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD), ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME,
                USER, INSTANCE_ID, true, true);
    }

    private ArtifactsBusinessLogic getArtifactsBusinessLogic() {
        ArtifactsBusinessLogic artifactsBusinessLogic = new ArtifactsBusinessLogic(artifactCassandraDao,
            toscaExportHandler, csarUtils, lifecycleBusinessLogic,
            userBusinessLogic, artifactsResolver, elementDao, groupOperation, groupInstanceOperation,
            groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        artifactsBusinessLogic.setGraphLockOperation(graphLockOperation);
        artifactsBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        return artifactsBusinessLogic;
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
        //TODO Remove if passes
        when(artifactToscaOperation.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class)
                , any(String.class))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD), ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME,
                USER, INSTANCE_ID, true, false);
        verify(janusGraphDao, times(1)).commit();
    }

    @Test
    public void testDeleteComponent_ArtifactNotFound(){
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> result;
        Resource resource = new Resource();
        String uniqueId = "uniqueId";
        resource.setUniqueId(uniqueId);
        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setArtifactName("artifactName");
        artifact.setEsId("esId");
        artifact.setArtifactUUID("artifactUUID");
        artifact.setArtifactType("YANG");
        artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        artifact.setDescription("description");
        artifact.setArtifactLabel("artifactLabel");
        toscaArtifacts.put("artifactId", artifact);
        resource.setArtifacts(toscaArtifacts);
        resource.setToscaArtifacts(toscaArtifacts);
        when(graphLockOperation.lockComponent(uniqueId, NodeTypeEnum.Resource))
                .thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(uniqueId, NodeTypeEnum.Resource))
                .thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.getToscaElement(uniqueId)).thenReturn(Either.left(resource));
        when(componentsUtils.getResponseFormatByArtifactId(ActionStatus.ARTIFACT_NOT_FOUND, "artifactId")).
                thenReturn(responseFormat);
        result = artifactBL.handleDelete("parentId", "artifactId", user, AuditingActionEnum.ARTIFACT_DELETE,
                ComponentTypeEnum.RESOURCE, resource,
                true, false);
        assertThat(result.isRight());
    }

    @Test
    public void validateHandleArtifactRequestReturnsProperResponseMessage() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final String componentId = "testComponent";
        final String userId = "testUser";
        final String artifactId = "testArtifact";
        final String origMd5 = "testOrigMd5";
        final String originData = "testOriginData";
        final String interfaceUuid = "testInterfaceUuid";
        final String operationUuid = "testOperationUuid";
        final String parentId = "testParentId";
        final String containerComponentType = "services";
        User testUser = new User();
        ComponentTypeEnum componentType = ComponentTypeEnum.SERVICE_INSTANCE;

        ArtifactsBusinessLogic.ArtifactOperationInfo operation = Mockito.mock(ArtifactsBusinessLogic.ArtifactOperationInfo.class);
        when(operation.getArtifactOperationEnum()).thenReturn(ArtifactsBusinessLogic.ArtifactOperationEnum.DOWNLOAD);

        UserValidations testUserValidation = Mockito.mock(UserValidations.class);
        when(testUserValidation.validateUserExists(eq(userId),any(String.class),anyBoolean())).thenReturn(testUser);

        ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);

        ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class),eq(componentId)) ).thenReturn(responseFormat);

        ArtifactDefinition artifactInfo = new ArtifactDefinition();

        Either<Component, StorageOperationStatus> storageStatus = Either.right(StorageOperationStatus.OK);//.RightProjection<Component, StorageOperationStatus>() ;
        when(toscaOperationFacade.getToscaFullElement(eq(componentId))).thenReturn(storageStatus);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);
        testArtifactsBusinessLogic.setUserValidations(testUserValidation);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> response = testArtifactsBusinessLogic.handleArtifactRequest(
                componentId, userId,  componentType,  operation,
                artifactId,  artifactInfo, origMd5, originData,
                interfaceUuid, operationUuid, parentId, containerComponentType);

        assertTrue(response.isRight());
        assertEquals(response.right().value(), responseFormat);
    }


    @Test
    public void validateHandleArtifactRequestWithNoUserReturnsMissingInformationResponseMessage() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final String componentId = "testComponent";
        final String artifactId = "testArtifact";
        final String origMd5 = "testOrigMd5";
        final String originData = "testOriginData";
        final String interfaceUuid = "testInterfaceUuid";
        final String operationUuid = "testOperationUuid";
        final String parentId = "testParentId";
        final String containerComponentType = "services";
        ArtifactDefinition artifactInfo = new ArtifactDefinition();
        ComponentTypeEnum componentType = ComponentTypeEnum.SERVICE_INSTANCE;

        ArtifactsBusinessLogic.ArtifactOperationInfo operation =
                Mockito.mock(ArtifactsBusinessLogic.ArtifactOperationInfo.class);
        when(operation.getArtifactOperationEnum()).thenReturn(ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);

        ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);

        when(componentsUtils.getResponseFormat(eq(ActionStatus.MISSING_INFORMATION)) ).thenReturn(responseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> response =
                testArtifactsBusinessLogic.handleArtifactRequest(
                    componentId, null,  componentType,  operation,
                    artifactId,  artifactInfo, origMd5, originData,
                    interfaceUuid, operationUuid, parentId, containerComponentType
                );

        assertTrue(response.isRight());
        assertEquals(response.right().value(), responseFormat);
    }

    @Test
    public void validateValidateAndHandleArtifactWillCallAuditResourceWithProperParameters() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final String componentUniqueId = "testComponentId";
        final ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        final ArtifactsBusinessLogic.ArtifactOperationInfo operation = Mockito.mock(ArtifactsBusinessLogic.ArtifactOperationInfo.class);
        final String artifactUniqueId = "testArtifactId";
        final String artifactName = "testArtifact";
        final String artifactType = "testData";
        final ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactType("testArtifact");
        final String origMd5 = GeneralUtility.calculateMD5Base64EncodedByString(artifactType);
        final String interfaceUuid = "testInterfaceUUID";
        final String operationName = "testOperation";
        final User user = new User();
        final Resource component = Mockito.mock(Resource.class);
        when(component.getName()).thenReturn(artifactName);
        final boolean shouldLock = false;
        final boolean inTransaction = false;
        final boolean needUpdateGroup = false;

        when(operation.getArtifactOperationEnum()).thenReturn(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);

        when(componentsUtils.isExternalApiEvent(AuditingActionEnum.ARTIFACT_UPLOAD)).thenReturn(false);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> response =
                testArtifactsBusinessLogic.validateAndHandleArtifact(
                    componentUniqueId,  componentType, operation,
                    artifactUniqueId, artifactDefinition, origMd5,
                    artifactType, interfaceUuid, operationName,
                    user, component, shouldLock, inTransaction, needUpdateGroup
                );

        assertTrue(response.isRight());
        assertNull(response.right().value());
        verify(componentsUtils).auditResource(
                eq(null), eq(user), eq(component),
                eq(artifactName), eq(AuditingActionEnum.ARTIFACT_UPLOAD), any(ResourceVersionInfo.class),
                eq(null), eq(null));
    }

    @Test
    public void validateGenerateAndSaveToscaArtifactStoresProperArtifact() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);

        final ArtifactDefinition artifactDefinition = Mockito.mock(ArtifactDefinition.class);
        when(artifactDefinition.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_CSAR.getType());
        final Component component = Mockito.mock(Component.class);
        final User user = new User();
        final boolean isInCertificationRequest = false;
        final boolean shouldLock = false;
        final boolean inTransaction= false;
        final boolean fetchTemplatesFromDB = false;


        when(csarUtils.createCsar(eq(component), eq(false), eq(false))).thenReturn(Either.right(expectedResponseFormat));
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> response =
                testArtifactsBusinessLogic.generateAndSaveToscaArtifact(
                        artifactDefinition, component, user,
                        isInCertificationRequest, shouldLock, inTransaction, fetchTemplatesFromDB);

        assertTrue(response.isRight());
        assertEquals(response.right().value(), expectedResponseFormat);
    }

    @Test
    public void validateGenerateAndSaveToscaArtifactResponseProperlyToGenerationFail() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);

        final byte[] byteResponse= "testBytes".getBytes();
        final byte[] testPayloadData = "testPayloadData".getBytes();
        final String testESId = "testEsId";
        final ArtifactDefinition artifactDefinition = Mockito.mock(ArtifactDefinition.class);
        when(artifactDefinition.getArtifactType()).thenReturn(ArtifactTypeEnum.TOSCA_CSAR.getType());
        when(artifactDefinition.getPayloadData()).thenReturn(testPayloadData);
        when(artifactDefinition.getEsId()).thenReturn(testESId);
        final String artifactName = "testArtifact";
        final String componentUniqueId = "testUniqueId";
        final Resource component = Mockito.mock(Resource.class);
        when(component.getComponentType()).thenReturn(ComponentTypeEnum.RESOURCE);
        when(component.getUniqueId()).thenReturn(componentUniqueId);
        when(component.getName()).thenReturn(artifactName);
        final User user = new User();
        final boolean isInCertificationRequest = false;
        final boolean shouldLock = false;
        final boolean inTransaction= false;
        final boolean fetchTemplatesFromDB = false;
        final ComponentsUtils testComponentUtils = Mockito.mock(ComponentsUtils.class);
        when(testComponentUtils.getResponseFormat(eq(ActionStatus.OK))).thenReturn(expectedResponseFormat);

        when(artifactCassandraDao.saveArtifact(any(ESArtifactData.class))).thenReturn(CassandraOperationStatus.OK);
        when(artifactToscaOperation.updateArtifactOnResource(
                eq(artifactDefinition), eq(componentUniqueId), eq(null),
                eq(NodeTypeEnum.Resource), eq(componentUniqueId)
        )).thenReturn(Either.left(artifactDefinition));
        when(csarUtils.createCsar(eq(component), eq(false), eq(false))).thenReturn(Either.left(byteResponse));
        testArtifactsBusinessLogic.setComponentsUtils(testComponentUtils);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> response =
                testArtifactsBusinessLogic.generateAndSaveToscaArtifact(
                        artifactDefinition, component, user,
                        isInCertificationRequest, shouldLock, inTransaction, fetchTemplatesFromDB);
        assertTrue(response.isLeft());
        assertTrue(response.isLeft());
        assertEquals(response.left().value().left().value(), artifactDefinition);
    }

    @Test
    public void validateHandleDownloadToscaModelRequestReturnsProperResponseFormat() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);

        final Component component = Mockito.mock(Component.class);
        final String testESId = "testEsId";
        final String artifactName = "testArtifact";
        final ArtifactDefinition artifactDefinition = Mockito.mock(ArtifactDefinition.class);
        when(artifactDefinition.getEsId()).thenReturn(testESId);
        when(artifactDefinition.getArtifactDisplayName()).thenReturn(artifactName);
        final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.OK))).thenReturn(ActionStatus.OK);
        when(componentsUtils.getResponseFormatByArtifactId(
                eq(ActionStatus.OK), eq(artifactName))).thenReturn(expectedResponseFormat);

        when(artifactCassandraDao.getArtifact(eq(testESId))).thenReturn(Either.right(CassandraOperationStatus.OK));

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        Either<ImmutablePair<String, byte[]>, ResponseFormat> response =
                testArtifactsBusinessLogic.handleDownloadToscaModelRequest(component,artifactDefinition);

        assertTrue(response.isRight());
        assertEquals(response.right().value(), expectedResponseFormat);
    }

    @Test
    public void validateHandleDownloadRequestByIdReturnsProperResponseFormat() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);

        final String componentId = "testComponent";
        final String artifactId = "testArtifact";
        final String userId = "testUser";
        final ComponentTypeEnum componentType = ComponentTypeEnum.SERVICE;
        final String parentId = "testParent";
        final String containerComponentType = "products";
        final User user = new User();
        final Service component = Mockito.mock(Service.class);
        when(component.getUniqueId()).thenReturn(componentId);
        final UserValidations userValidations = Mockito.mock(UserValidations.class);
        when(userValidations.validateUserExists(
                eq(userId), eq("ArtifactDownload"), eq(false))).thenReturn(user);

        when(toscaOperationFacade.getToscaFullElement(eq(componentId))).thenReturn(Either.left(component));
        when(artifactToscaOperation.getArtifactById(componentId, artifactId, componentType, componentId)).
                thenReturn(Either.right(StorageOperationStatus.OK));
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.OK))).thenReturn(ActionStatus.OK);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK))).thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);
        testArtifactsBusinessLogic.setUserValidations(userValidations);

        Either<ImmutablePair<String, byte[]>, ResponseFormat> response =
                testArtifactsBusinessLogic.handleDownloadRequestById(
                        componentId, artifactId, userId,
                        componentType, parentId, containerComponentType);

        assertTrue(response.isRight());
        assertEquals(response.right().value(), expectedResponseFormat);
    }

    @Test
    public void testIfValidateArtifactTypeExistsRespondsWithNotSupportedFormat() {

        final Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getArtifactType()).thenReturn("WrongFormat");

        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        testArtifactsBusinessLogic.validateArtifactTypeExists(responseWrapper,artifactInfo);

        assertEquals(responseWrapper.getInnerElement().getStatus().intValue(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testIfValidateFileExtensionRespondsWithCorrectResult() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getArtifactName()).thenReturn("test.heat");
        final ArtifactTypeConfig artifactTypeConfig =
                Mockito.mock(ArtifactTypeConfig.class);
        when(artifactTypeConfig.getAcceptedTypes()).thenReturn(Collections.singletonList("heat"));
        final IDeploymentArtifactTypeConfigGetter deploymentConfigGetter =
                Mockito.mock(IDeploymentArtifactTypeConfigGetter.class);
        when(deploymentConfigGetter.getDeploymentArtifactConfig()).
                thenReturn(artifactTypeConfig);
        final NodeTypeEnum parentType = NodeTypeEnum.Service;
        final ArtifactTypeEnum artifactType = ArtifactTypeEnum.HEAT;

        testArtifactsBusinessLogic.validateFileExtension(
                responseWrapper, deploymentConfigGetter, artifactInfo,
                parentType, artifactType);

        assertTrue(responseWrapper.isEmpty());
    }

    @Test
    public void testIfValidateFileExtensionRespondsWithGeneralErrorIfNodeTypeIsWrong() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        final IDeploymentArtifactTypeConfigGetter deploymentConfigGetter =
                Mockito.mock(IDeploymentArtifactTypeConfigGetter.class);
        final NodeTypeEnum parentType = NodeTypeEnum.Group;
        final ArtifactTypeEnum artifactType = ArtifactTypeEnum.HEAT;

        when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR))).thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        testArtifactsBusinessLogic.validateFileExtension(
                responseWrapper, deploymentConfigGetter, artifactInfo,
                parentType, artifactType);

        assertFalse(responseWrapper.isEmpty());
        assertEquals(responseWrapper.getInnerElement(),expectedResponseFormat);
    }

    @Test
    public void testIfValidateFileExtensionRespondsWithArtifactTypeNotSupportedIfAcceptedTypeIsNull() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        final String testArtifactType = "testArtifact";
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getArtifactType()).thenReturn(testArtifactType);
        final IDeploymentArtifactTypeConfigGetter deploymentConfigGetter =
                Mockito.mock(IDeploymentArtifactTypeConfigGetter.class);
        final NodeTypeEnum parentType = NodeTypeEnum.Resource;
        final ArtifactTypeEnum artifactType = ArtifactTypeEnum.HEAT;

        when(componentsUtils.getResponseFormat(eq(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED), eq(testArtifactType))).thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        testArtifactsBusinessLogic.validateFileExtension(
                responseWrapper, deploymentConfigGetter, artifactInfo,
                parentType, artifactType);

        assertFalse(responseWrapper.isEmpty());
        assertEquals(responseWrapper.getInnerElement(),expectedResponseFormat);
    }

    @Test
    public void testIfValidateFileExtensionRespondsWithWrongArtifactTypeExtensionIfExtensionIsNotAccepted() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getArtifactName()).thenReturn("test.heat");
        final ArtifactTypeConfig artifactTypeConfig =
                Mockito.mock(ArtifactTypeConfig.class);
        when(artifactTypeConfig.getAcceptedTypes()).thenReturn(Collections.singletonList("yaml"));
        final IDeploymentArtifactTypeConfigGetter deploymentConfigGetter =
                Mockito.mock(IDeploymentArtifactTypeConfigGetter.class);
        when(deploymentConfigGetter.getDeploymentArtifactConfig()).
                thenReturn(artifactTypeConfig);
        final NodeTypeEnum parentType = NodeTypeEnum.Service;
        final ArtifactTypeEnum artifactType = ArtifactTypeEnum.HEAT;

        when(componentsUtils.getResponseFormat(eq(ActionStatus.WRONG_ARTIFACT_FILE_EXTENSION), eq(artifactType.getType()))).thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        testArtifactsBusinessLogic.validateFileExtension(
                responseWrapper, deploymentConfigGetter, artifactInfo,
                parentType, artifactType);

        assertFalse(responseWrapper.isEmpty());
        assertEquals(responseWrapper.getInnerElement(),expectedResponseFormat);
    }

    @Test
    public void validateFillArtifactPayloadValidationReturnsNoErrorIfCalledWithProperParameters() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getPayloadData()).thenReturn("artifactInfoPayload".getBytes());
        final Wrapper<byte[]> payloadWrapper = new Wrapper<>();

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        testArtifactsBusinessLogic.fillArtifactPayloadValidation(errorWrapper, payloadWrapper, artifactInfo);

        assertEquals(artifactInfo.getPayloadData(),payloadWrapper.getInnerElement());
    }

    @Test
    public void validateFillArtifactPayloadValidationReturnsNoErrorIfCalledWithEmptyArtifactPayloadButPayloadIsInCasandraDao() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        final String esId = "testEsId";
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getPayloadData()).thenReturn("".getBytes());
        when(artifactInfo.getEsId()).thenReturn(esId);
        final Wrapper<byte[]> payloadWrapper = new Wrapper<>();
        final byte[] payloadArtifactData = "testArtifactData".getBytes();
        final byte[] base64PayloadArtifactData = Base64.getDecoder().decode(payloadArtifactData);
        final ESArtifactData artifactData = Mockito.mock(ESArtifactData.class);
        when(artifactData.getDataAsArray()).thenReturn(base64PayloadArtifactData);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        when(artifactCassandraDao.getArtifact(esId)).thenReturn(Either.left(artifactData));

        testArtifactsBusinessLogic.fillArtifactPayloadValidation(errorWrapper, payloadWrapper, artifactInfo);

        assertFalse(payloadWrapper.isEmpty());
        assertArrayEquals(payloadWrapper.getInnerElement(), payloadArtifactData);
    }

    @Test
    public void validateFillArtifactPayloadValidationReturnsErrorIfCalledWithEmptyArtifactPayloadAndNoPayloadInCasandraDao() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        final String esId = "testEsId";
        final ArtifactDefinition artifactInfo = Mockito.mock(ArtifactDefinition.class);
        when(artifactInfo.getPayloadData()).thenReturn("".getBytes());
        when(artifactInfo.getEsId()).thenReturn(esId);
        final Wrapper<byte[]> payloadWrapper = new Wrapper<>();

        when(artifactCassandraDao.getArtifact(esId)).thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.ERROR_DURING_CSAR_CREATION);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.ERROR_DURING_CSAR_CREATION))).thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);
        testArtifactsBusinessLogic.fillArtifactPayloadValidation(errorWrapper, payloadWrapper, artifactInfo);

        assertFalse(errorWrapper.isEmpty());
        assertEquals(errorWrapper.getInnerElement(),expectedResponseFormat);
    }

    @Test
    public void validateGetDeploymentArtifactsReturnsCorrectArtifactLists() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final Component parentComponent = Mockito.mock(Component.class);
        final ArtifactDefinition artifactDefinition = Mockito.mock(ArtifactDefinition.class);
        when(parentComponent.getDeploymentArtifacts()).thenReturn(Collections.singletonMap("testService", artifactDefinition));
        final NodeTypeEnum parentType = NodeTypeEnum.Service;
        final String ciId = "testCiId";

        List<ArtifactDefinition> result = testArtifactsBusinessLogic.getDeploymentArtifacts(parentComponent, parentType, ciId);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), artifactDefinition);
    }

    @Test
    public void validateGetDeploymentArtifactsReturnsCorrectArtifactListsForResourceInstance() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final String ciId = "testCiId";
        final ArtifactDefinition deploymentArtifact = Mockito.mock(ArtifactDefinition.class);
        final  Map<String, ArtifactDefinition> deploymentArtifacts = Collections.singletonMap("",deploymentArtifact);
        final ComponentInstance componentInstance = Mockito.mock(ComponentInstance.class);
        when(componentInstance.getUniqueId()).thenReturn(ciId);
        when(componentInstance.getDeploymentArtifacts()).thenReturn(deploymentArtifacts);
        final Component parentComponent = Mockito.mock(Component.class);
        when(parentComponent.getComponentInstances()).thenReturn(Collections.singletonList(componentInstance));
        final NodeTypeEnum parentType = NodeTypeEnum.ResourceInstance;

        List<ArtifactDefinition> result = testArtifactsBusinessLogic.getDeploymentArtifacts(parentComponent, parentType, ciId);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), deploymentArtifact);
    }

    @Test
    public void validateHandleGetArtifactsByTypeReturnsProperArtifact() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final String containerComponentType = "services";
        final String parentId = "testParentId";
        final ComponentTypeEnum componentType = ComponentTypeEnum.SERVICE;
        final String componentId = "testComponentId";
        final String artifactGroupType = "testArtifactGroupType";
        final String userId = "testUserId";
        final User user = new User();

        final UserValidations userValidations = Mockito.mock(UserValidations.class);
        when(userValidations.validateUserExists(eq(userId), eq("get artifacts"), eq(false)))
                .thenReturn(user);


        when(toscaOperationFacade.getToscaElement(eq(componentId), any(ComponentParametersView.class)))
                .thenReturn(Either.right(StorageOperationStatus.OK));
        when(componentsUtils.convertFromStorageResponse(
                eq(StorageOperationStatus.OK), eq(ComponentTypeEnum.SERVICE)))
                .thenReturn(ActionStatus.OK);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK), eq(componentId)))
                .thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setUserValidations(userValidations);
        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        Either<Map<String, ArtifactDefinition>, ResponseFormat> response =
                testArtifactsBusinessLogic.handleGetArtifactsByType(
                        containerComponentType, parentId, componentType,
                        componentId, artifactGroupType, userId
                );

        assertTrue(response.isRight());
        assertEquals(response.right().value(), expectedResponseFormat);
    }

    @Test
    public void validateHandleGetArtifactsByTypeReturnsMissingInformationIfUserIdIsNull() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final String containerComponentType = "services";
        final String parentId = "testParentId";
        final ComponentTypeEnum componentType = ComponentTypeEnum.SERVICE;
        final String componentId = "testComponentId";
        final String artifactGroupType = "testArtifactGroupType";
        final String userId = null;

        when(componentsUtils.getResponseFormat(eq(ActionStatus.MISSING_INFORMATION)))
                .thenReturn(expectedResponseFormat);

        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        Either<Map<String, ArtifactDefinition>, ResponseFormat> response =
                testArtifactsBusinessLogic.handleGetArtifactsByType(
                        containerComponentType, parentId, componentType,
                        componentId, artifactGroupType, userId
                );

        assertTrue(response.isRight());
        assertEquals(response.right().value(), expectedResponseFormat);
    }

    @Test
    public void validateDeleteArtifactByInterfaceReturnsProperResponse() {
        ArtifactsBusinessLogic testArtifactsBusinessLogic = getArtifactsBusinessLogic();

        final ResponseFormat expectedResponseFormat = Mockito.mock(ResponseFormat.class);
        final String resourceId = "testResources";
        final String userId = "testUser";
        final String artifactId = "testArtifact";
        final boolean inTransaction = false;
        final String serviceId = "testService";
        final Resource resource =  Mockito.mock(Resource.class);
        when(resource.getUniqueId()).thenReturn(serviceId);

        when(toscaOperationFacade.getToscaElement(resourceId, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(resource));
        when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.right(StorageOperationStatus.OK));
        when(componentsUtils.getResponseFormat(ActionStatus.OK))
                .thenReturn(expectedResponseFormat);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.OK))
                .thenReturn(ActionStatus.OK);
        when(componentsUtils.getResponseFormatByArtifactId(ActionStatus.OK, artifactId))
                .thenReturn(expectedResponseFormat);
        testArtifactsBusinessLogic.setComponentsUtils(componentsUtils);

        Either<Operation, ResponseFormat> response =
                testArtifactsBusinessLogic.deleteArtifactByInterface(
                        resourceId, userId, artifactId, inTransaction
                );

        assertTrue(response.isRight());
        assertEquals(response.right().value(),expectedResponseFormat);
    }

    private void verifyHeatParam(HeatParameterDefinition heatEnvParam, HeatParameterDefinition heatYamlParam) {
        assertThat(heatYamlParam.getCurrentValue()).isEqualTo(heatEnvParam.getDefaultValue());
        assertThat(heatEnvParam.getCurrentValue()).isNull();
    }
}
