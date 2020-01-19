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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.ArtifactsResolver;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ArtifactOperation artifactOperation;
    @Mock
    public ComponentsUtils componentsUtils;
    @Mock
    private UserAdminOperation userOperation;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    public ToscaOperationFacade toscaOperationFacade;
    @Mock
    private NodeTemplateOperation nodeTemplateOperation;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    JanusGraphDao janusGraphDao;
    @Mock
    private IInterfaceLifecycleOperation interfaceLifecycleOperation;

    // public static final InformationDeployedArtifactsBusinessLogic
    // informationDeployedArtifactsBusinessLogic =
    // Mockito.mock(InformationDeployedArtifactsBusinessLogic.class);
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

        when(userBusinessLogic.getUser(eq("jh0003"), anyBoolean())).thenReturn(USER);


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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact, ArtifactDefinition.class, false);
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
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class, false);
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
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class, false);
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
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("timeout", " 80");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class, false);
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
        jsonArtifact.getAsJsonObject().addProperty("artifactLabel", " label");
        jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", " DEPLOYMENT");
        jsonArtifact.getAsJsonObject().addProperty("artifactType", " HEAT");

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class, true);
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

        DAOArtifactData DAOArtifactData =new DAOArtifactData(esArtifactId);
        DAOArtifactData.setDataAsArray(payload);
        Either<DAOArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(DAOArtifactData);
        when(artifactCassandraDao.getArtifact(esArtifactId)).thenReturn(artifactfromESres);
        List<org.openecomp.sdc.be.model.Component> serviceList = new ArrayList<>();
        serviceList.add(service);
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> getServiceRes = Either.left(serviceList);
        when(toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName)).thenReturn(getServiceRes);
        byte[] downloadServiceArtifactByNamesRes =
        artifactBL.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
        assertThat(downloadServiceArtifactByNamesRes !=null &&
                downloadServiceArtifactByNamesRes.length == payload.length).isTrue();
    }

    @Test
    public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2"))
                .build();

        Resource component = new Resource();
        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(USER);
        when(artifactToscaOperation.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class), eq(component), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));
        ArtifactDefinition heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(new ArrayList<>(),heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component, Collections.emptyMap());
        assertThat(heatEnvPlaceHolder.getListHeatParameters()).isNull();
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


        ArtifactDefinition heatEnvArtifact = artifactBL.createHeatEnvPlaceHolder(new ArrayList<>(),heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component, Collections.emptyMap());
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
                , any(String.class), eq(true))).thenReturn(Either.left(artifactDefinition));
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
                , any(String.class), eq(true))).thenReturn(Either.left(artifactDefinition));
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
        result = artifactBL.handleDelete("parentId", "artifactId", USER, AuditingActionEnum.ARTIFACT_DELETE,
                ComponentTypeEnum.RESOURCE, resource,
                true, false);
        assertThat(result.isRight());
    }

    private void verifyHeatParam(HeatParameterDefinition heatEnvParam, HeatParameterDefinition heatYamlParam) {
        assertThat(heatYamlParam.getCurrentValue()).isEqualTo(heatEnvParam.getDefaultValue());
        assertThat(heatEnvParam.getCurrentValue()).isNull();
    }
}
