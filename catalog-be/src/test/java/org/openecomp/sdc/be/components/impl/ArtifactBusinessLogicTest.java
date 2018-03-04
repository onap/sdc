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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_ENV_NAME;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_VF_ENV_NAME;

public class ArtifactBusinessLogicTest {

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
    private static ArtifactsBusinessLogic artifactBL;
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
    private ArtifactsOperations artifactsOperations;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    TitanDao titanDao;

    // public static final InformationDeployedArtifactsBusinessLogic
    // informationDeployedArtifactsBusinessLogic =
    // Mockito.mock(InformationDeployedArtifactsBusinessLogic.class);

    public static final Resource resource = Mockito.mock(Resource.class);
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();


    private static List<ArtifactType> getAllTypes() {
        List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();
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
        MockitoAnnotations.initMocks(this);
        Either<ArtifactDefinition, StorageOperationStatus> NotFoundResult = Either.right(StorageOperationStatus.NOT_FOUND);

        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> NotFoundResult2 = Either.right(StorageOperationStatus.NOT_FOUND);
        when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Service), Mockito.anyBoolean())).thenReturn(NotFoundResult2);
        when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Resource), Mockito.anyBoolean())).thenReturn(NotFoundResult2);

        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> notFoundInterfaces = Either.right(StorageOperationStatus.NOT_FOUND);
        when(lifecycleOperation.getAllInterfacesOfResource(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(notFoundInterfaces);

        Either<User, ActionStatus> getUserResult = Either.left(USER);

        when(userOperation.getUserData("jh0003", false)).thenReturn(getUserResult);

        Either<List<ArtifactType>, ActionStatus> getType = Either.left(getAllTypes());
        when(elementOperation.getAllArtifactTypes()).thenReturn(getType);

        when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VFC);
    }

    @Test
    public void testValidJson() {
        ArtifactDefinition ad = createArtifactDef();

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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
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

        ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
        assertNull(afterConvert);
    }

    @Test
    public void testValidMibAritactsConfiguration() {
        Map<String, ArtifactTypeConfig> componentDeploymentArtifacts =
                    ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts();
        Map<String, ArtifactTypeConfig> componentInstanceDeploymentArtifacts =
                    ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
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
        assertTrue(downloadServiceArtifactByNamesRes.isLeft());
        assertTrue(downloadServiceArtifactByNamesRes.left().value() !=null && downloadServiceArtifactByNamesRes.left().value().length == payload.length);
    }

    @Test
    public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
        ArtifactDefinition heatArtifact = new ArtifactBuilder()
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
                .addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2"))
                .build();

        Resource component = new Resource();
        when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
        when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class), eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));
        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component, Collections.emptyMap());
        assertTrue(heatEnvPlaceHolder.isLeft());
        assertNull(heatEnvPlaceHolder.left().value().getListHeatParameters());
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
        when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class), eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
                .thenReturn(Either.left(new ArtifactDefinition()));

        Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component, Collections.emptyMap());

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
        when(artifactsOperations.updateArifactOnResource(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class)
                , any(String.class))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD), ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME,
                USER, INSTANCE_ID, true, true);
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
        when(artifactsOperations.updateArifactOnResource(any(ArtifactDefinition.class), any(), any(), any(NodeTypeEnum.class)
                , any(String.class))).thenReturn(Either.left(artifactDefinition));
        when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD), ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME,
                USER, INSTANCE_ID, true, false);
        verify(titanDao, times(1)).commit();
    }

    private void verifyHeatParam(HeatParameterDefinition heatEnvParam, HeatParameterDefinition heatYamlParam) {
        assertEquals(heatEnvParam.getDefaultValue(), heatYamlParam.getCurrentValue());
        assertNull(heatEnvParam.getCurrentValue());
    }



// @Test
    // public void convertAndValidateDeploymentArtifactNonHeatSuccess(){
    // ArtifactDefinition createArtifactDef = createArtifactDef();
    // createArtifactDef.setArtifactType(ArtifactTypeEnum.YANG_XML.getType());
    //
    // Either<ArtifactDefinition, ResponseFormat> validateResult = artifactBL
    //
    // assertTrue(validateResult.isLeft());
    // ArtifactDefinition validatedArtifact = validateResult.left().value();
    //
    // assertEquals(createArtifactDef.getArtifactGroupType(),
    // validatedArtifact.getArtifactGroupType());
    // assertEquals(new Integer(0), validatedArtifact.getTimeout());
    // assertFalse(validatedArtifact.getMandatory());
    // assertFalse(validatedArtifact.getServiceApi());
    //
    // }
}
