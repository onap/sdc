/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.components.impl;


import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.TrueTraversal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CreateServiceFromYamlParameter;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.ArtifactExternalServlet;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServiceImportBusinessLogicTest extends ServiceImportBussinessLogicBaseTestSetup {
    private final static String DEFAULT_ICON = "defaulticon";

    @InjectMocks
    static ServiceImportBusinessLogic serviceImportBusinessLogic;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;
    @Mock
    private CsarBusinessLogic csarBusinessLogic;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ServiceImportParseLogic serviceImportParseLogic;
    @Mock
    ArtifactDefinition artifactDefinition =new ArtifactDefinition();



    private static UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private static ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
    private static ServletUtils servletUtils = mock(ServletUtils.class);
    private static ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private static ArtifactsBusinessLogic artifactsBusinessLogic = mock(ArtifactsBusinessLogic.class);

    private static AbstractValidationsServlet servlet = new ArtifactExternalServlet(userBusinessLogic,
            componentInstanceBusinessLogic, componentsUtils, servletUtils, resourceImportManager, artifactsBusinessLogic);


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(artifactDefinition.getMandatory()).thenReturn(true);
        when(artifactDefinition.getArtifactName()).thenReturn("creatorFullName");
        when(artifactDefinition.getArtifactType()).thenReturn("TOSCA_CSAR");

    }

    @Test
    public void testGetComponentsUtils() {
        ComponentsUtils result;
        result = serviceImportBusinessLogic.getComponentsUtils();
    }

    @Test
    public void testSetComponentsUtils() {
        ComponentsUtils componentsUtils = null;

        serviceImportBusinessLogic.setComponentsUtils(componentsUtils);
    }

    @Test
    public void testInvalidEnvironmentContext() {
        Service oldService = createServiceObject(false);
        String payloadName = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        Service newService = createServiceObject(true);
        try {
            when(serviceBusinessLogic.validateServiceBeforeCreate(newService,user,AuditingActionEnum.CREATE_RESOURCE))
                    .thenReturn(Either.left(newService));
            when(toscaOperationFacade.validateCsarUuidUniqueness(payloadName)).thenReturn(StorageOperationStatus.OK);
            sIB1.createService(oldService, AuditingActionEnum.CREATE_RESOURCE, user, payload, payloadName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceFromCsar(){
        Service oldService = createServiceObject(false);
        String csarUUID = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        try {
            sIB1.createServiceFromCsar(oldService, user, payload, csarUUID);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceFromYaml(){
        Service oldService = createServiceObject(false);
        String topologyTemplateYaml = getMainTemplateContent();
        String yamlName = "group.yml";
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();
        CsarInfo csarInfo =getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        try {
            sIB1.createServiceFromYaml(oldService,topologyTemplateYaml,yamlName,nodeTypesInfo,
                    csarInfo,nodeTypesArtifactsToCreate,true,true,nodeName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceAndRIsFromYaml(){
        Service oldService = createServiceObject(false);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        CreateServiceFromYamlParameter csfyp = getCsfyp();
        try {
            sIB1.createServiceAndRIsFromYaml(oldService,false,nodeTypesArtifactsToCreate,true,true,csfyp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateOrUpdateArtifacts(){
        ArtifactsBusinessLogic.ArtifactOperationEnum operation = ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        CsarInfo csarInfo =getCsarInfo();
        Resource preparedResource = createParseResourceObject(false);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle= new HashMap<>();
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,nodeTypesArtifactsToHandle);
        try {
            sIB1.createOrUpdateArtifacts(operation,createdArtifacts,yamlFileName,csarInfo,
                    preparedResource,nodeTypeInfoToUpdateArtifacts,true,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleVfCsarArtifacts(){
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        try {
            sIB1.handleVfCsarArtifacts(resource,csarInfo,createdArtifacts,artifactOperation,true,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateOrUpdateSingleNonMetaArtifactToComstants(){
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        try {
            sIB1.createOrUpdateSingleNonMetaArtifactToComstants(resource,csarInfo,artifactOperation,true,true);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetResourceResponseFormatEither(){
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        try {
            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus =CsarValidationUtils.getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);
            //sIB1.getResourceResponseFormatEither(resource,csarInfo,createdArtifacts,artifactOperation,
            //        true,true,artifacsMetaCsarStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateOrUpdateNonMetaArtifacts(){
        CsarInfo csarInfo = getCsarInfo();
        Resource resource = createParseResourceObject(false);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);

        Either<Resource, ResponseFormat> result = sIB1.createOrUpdateNonMetaArtifacts(csarInfo, resource,
                createdArtifacts, true, true, artifactOperation);
        assertEquals(result.left().value(),resource);
    }

    @Test
    public void testFindVfCsarArtifactsToHandle(){
        Resource resource = createParseResourceObject(false);
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();

        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>,
                ResponseFormat> result = sIB1.findVfCsarArtifactsToHandle(resource,artifactPathAndNameList,user);
        assertNotNull(result.left().value());
    }


    @Test
    public void testIsNonMetaArtifact() {
        ArtifactDefinition artifactDefinition =new ArtifactDefinition();
        artifactDefinition.setMandatory(false);
        artifactDefinition.setArtifactName("creatorFullName");
        artifactDefinition.setArtifactType("TOSCA_CSAR");

        boolean nonMetaArtifact = sIB1.isNonMetaArtifact(artifactDefinition);
        assertTrue(nonMetaArtifact);

    }

    @Test
    public void testOrganizeVfCsarArtifactsByArtifactOperation(){
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        Resource resource = createParseResourceObject(false);

        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat>
                enumMapResponseFormatEither = sIB1.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifactsToHandle, resource, user);
        assertNotNull(enumMapResponseFormatEither.left().value());
    }

    @Test
    public void testProcessCsarArtifacts(){
        CsarInfo csarInfo = getCsarInfo();
        Resource resource = createParseResourceObject(false);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Either<Resource, ResponseFormat> resStatus = null;
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = new
                EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        Either<Resource, ResponseFormat> rrfe = sIB1.processCsarArtifacts(csarInfo,
                resource, createdArtifacts, true, true, resStatus, vfCsarArtifactsToHandle);
        assertNull(rrfe);
    }

    @Test
    public void testCreateOrUpdateSingleNonMetaArtifact(){
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        String rootPath = System.getProperty("user.dir");
        Path path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
        String artifactPath = path.toString(), artifactFileName = "", artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String artifactLabel = "", artifactDisplayName = "", artifactDescription = "", artifactId = "artifactId";
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        try {
            sIB1.createOrUpdateSingleNonMetaArtifact(resource, csarInfo, artifactPath, artifactFileName, artifactType, artifactGroupType,
                    artifactLabel, artifactDisplayName, artifactDescription, artifactId, artifactOperation, createdArtifacts,
                    true, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleNodeTypeArtifacts(){
        Resource nodeTypeResource = createParseResourceObject(false);
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        try {
            sIB1.handleNodeTypeArtifacts(nodeTypeResource, nodeTypeArtifactsToHandle,
                    createdArtifacts, user, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckoutResource(){
        Resource resource = createParseResourceObject(true);
        Either<Resource, ResponseFormat> result = sIB1.checkoutResource(resource, user, true);
        assertEquals(result.left().value(),resource);
    }

    @Test
    public void testCreateOrUpdateServiceArtifacts(){
        ArtifactsBusinessLogic.ArtifactOperationEnum operation = ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        CsarInfo csarInfo =getCsarInfo();
        Service preparedService = createServiceObject(false);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle= new HashMap<>();
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,nodeTypesArtifactsToHandle);

        try {
            sIB1.createOrUpdateArtifacts(operation,createdArtifacts,yamlFileName,csarInfo,
                    preparedService,nodeTypeInfoToUpdateArtifacts,true,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleVfCsarServiceArtifacts(){
        Service service = createServiceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        try {
            sIB1.handleVfCsarArtifacts(service,csarInfo,createdArtifacts,artifactOperation,true,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateOrUpdateNonMetaServiceArtifacts(){
        CsarInfo csarInfo = getCsarInfo();
        Service service = createServiceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);

        Either<Service, ResponseFormat> result = sIB1.createOrUpdateNonMetaArtifacts(csarInfo,
                service, createdArtifacts, true, true, artifactOperation);
        assertEquals(result.left().value(),service);
    }

    @Test
    public void testFindServiceCsarArtifactsToHandle(){
        Service service = createServiceObject(true);
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();

        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>,
                ResponseFormat> result = sIB1.findVfCsarArtifactsToHandle(service, artifactPathAndNameList, user);
        assertNotNull(result.left().value());
    }

    @Test
    public void testOrganizeVfCsarArtifactsByServiceArtifactOperation(){
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        Service service = createServiceObject(true);

        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat>
                enumMapResponseFormatEither = sIB1.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList,
                existingArtifactsToHandle, service, user);
        assertNotNull(enumMapResponseFormatEither.left().value());
    }

    @Test
    public void testProcessServiceCsarArtifacts(){
        CsarInfo csarInfo = getCsarInfo();
        Service service = createServiceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Either<Service, ResponseFormat> resStatus = null;
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = new
                EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        Either<Service, ResponseFormat> srfe = sIB1.processCsarArtifacts(csarInfo,
                service, createdArtifacts, true, true, resStatus, vfCsarArtifactsToHandle);
        assertNull(srfe);
    }

    @Test
    public void testGetValidArtifactNames(){
        CsarInfo csarInfo = getCsarInfo();
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
        Either<List<CsarUtils.NonMetaArtifactInfo>, String> result = sIB1.getValidArtifactNames(csarInfo, collectedWarningMessages);
        System.out.println(result.left().value());
        assertNotNull(result.left().value());
    }

    @Test
    public void testCreateOrUpdateSingleNonMetaServiceArtifact(){
        Service service = createServiceObject(true);
        CsarInfo csarInfo = getCsarInfo();
        String rootPath = System.getProperty("user.dir");
        Path path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
        String artifactPath = path.toString(), artifactFileName = "", artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String artifactLabel = "", artifactDisplayName = "", artifactDescription = "", artifactId = "artifactId";
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        try {
            sIB1.createOrUpdateSingleNonMetaArtifact(service, csarInfo, artifactPath, artifactFileName, artifactType, artifactGroupType,
                    artifactLabel, artifactDisplayName, artifactDescription, artifactId, artifactOperation, createdArtifacts,
                    true, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateGroupsOnResource(){
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        Either<Service, ResponseFormat> result = sIB1.createGroupsOnResource(service, groups);
        assertEquals(result.left().value(),service);
    }

    @Test
    public void testUpdateGroupsMembersUsingResource(){
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = null;

        List<GroupDefinition> groupDefinitions = sIB1.updateGroupsMembersUsingResource(groups, service);
        for (GroupDefinition groupDefinition : groupDefinitions) {
            assertNull(groupDefinition);
        }
    }

    @Test
    public void testCreateRIAndRelationsFromYaml(){
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = new HashMap<>();
        String topologyTemplateYaml = getMainTemplateContent();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        try {
            sIB1.createRIAndRelationsFromYaml(yamlName,service,uploadComponentInstanceInfoMap,topologyTemplateYaml,nodeTypesNewCreatedArtifacts,
                    nodeTypesInfo,csarInfo,nodeTypesArtifactsToCreate,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceInstancesRelations(){
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();

        try {
            sIB1.createServiceInstancesRelations(user, yamlName, service, uploadResInstancesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessComponentInstance(){
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List< AttributeDataDefinition >> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();

        try {
            sIB1.processComponentInstance(yamlName, service, componentInstancesList,allDataTypes,instProperties,instCapabilties,instRequirements,
                    instDeploymentArtifacts,instArtifacts,instAttributes,originCompMap,instInputs,uploadComponentInstanceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddInputsValuesToRi(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Service resource = createServiceObject(true);
        Resource originResource =createParseResourceObject(false);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        sIB1.addInputsValuesToRi(uploadComponentInstanceInfo,resource,originResource,
                    currentCompInstance,instInputs,allDataTypes);
    }

    @Test
    public void testProcessProperty(){
        Service resource = createServiceObject(true);
        ComponentInstance currentCompInstance = null;
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        List<ComponentInstanceInput> instPropList = new ArrayList<>();
        List<UploadPropInfo> propertyList = new ArrayList<>();

        try {
            sIB1.processProperty(resource, currentCompInstance, allDataTypes,
                    currPropertiesMap, instPropList, propertyList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessGetInput(){
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        List<InputDefinition> inputs = new ArrayList<>();
        GetInputValueDataDefinition getInputIndex = new GetInputValueDataDefinition();

        try {
            sIB1.processGetInput(getInputValues,inputs,getInputIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddPropertyValuesToRi(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        uploadComponentInstanceInfo.setProperties(properties);
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        try {
            sIB1.addPropertyValuesToRi(uploadComponentInstanceInfo, resource, originResource, currentCompInstance,
                    instProperties, allDataTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessComponentInstanceCapabilities(){
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        uploadComponentInstanceInfo.setProperties(properties);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Resource originResource = createParseResourceObject(false);

        sIB1.processComponentInstanceCapabilities(allDataTypes, instCapabilties, uploadComponentInstanceInfo,
                currentCompInstance, originResource);
    }

    @Test
    public void testUpdateCapabilityPropertiesValues(){
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();

        sIB1.updateCapabilityPropertiesValues(allDataTypes, originCapabilities, newPropertiesMap);
    }

    @Test
    public void testUpdatePropertyValues(){
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        Map<String, UploadPropInfo> newProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        sIB1.updatePropertyValues(properties,newProperties,allDataTypes);
    }

    @Test
    public void testUpdatePropertyValue(){
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setType("services");
        UploadPropInfo propertyInfo = new UploadPropInfo();
        propertyInfo.setValue("value");
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        try {
            sIB1.updatePropertyValue(property,propertyInfo,allDataTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetOriginResource(){
        String yamlName = "group.yml";
        Map<String, Resource> originCompMap = new HashMap<>();
        ComponentInstance currentCompInstance = new ComponentInstance();
        currentCompInstance.setComponentUid("currentCompInstance");

        try {
            sIB1.getOriginResource(yamlName,originCompMap,currentCompInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleSubstitutionMappings(){
        Service service = createServiceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();

        sIB1.handleSubstitutionMappings(service, uploadResInstancesMap);
    }

    @Test
    public void testUpdateCalculatedCapReqWithSubstitutionMappings(){
        Resource resource = createParseResourceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();

        when(toscaOperationFacade.deleteAllCalculatedCapabilitiesRequirements(any())).thenReturn(StorageOperationStatus.OK);

        try {
            sIB1.updateCalculatedCapReqWithSubstitutionMappings(resource,uploadResInstancesMap);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFillUpdatedInstCapabilitiesRequirements(){
        List<ComponentInstance> componentInstances = new ArrayList<>();
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirement = new HashMap<>();

        sIB1.fillUpdatedInstCapabilitiesRequirements(componentInstances,uploadResInstancesMap,
                updatedInstCapabilities,updatedInstRequirement);
    }

    @Test
    public void testFillUpdatedInstCapabilities(){
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties = new HashMap<>();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setCapabilities(capabilities);
        Map<String, String> capabilitiesNamesToUpdate = new HashMap<>();

        sIB1.fillUpdatedInstCapabilities(updatedInstCapabilties,instance,capabilitiesNamesToUpdate);
    }

    @Test
    public void testFillUpdatedInstRequirements(){
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new
                HashMap<>();
        ComponentInstance instance = new ComponentInstance();
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        instance.setRequirements(requirements);
        Map<String, String> requirementsNamesToUpdate = new HashMap<>();

        sIB1.fillUpdatedInstRequirements(updatedInstRequirements,instance,requirementsNamesToUpdate);
    }




    /*protected void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
        ResponseFormat actualResponse = e.getResponseFormat() != null ?
                e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
        assertParseResponse(actualResponse, expectedStatus, variables);
    }*/

    private Map<String, byte[]> crateCsarFromPayload() {
        String payloadName = "valid_vf.csar";
        String rootPath = System.getProperty("user.dir");
        Path path;
        byte[] data;
        String payloadData;
        Map<String, byte[]> returnValue = null;
        try {
            path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
            data = Files.readAllBytes(path);
            payloadData = Base64.encodeBase64String(data);
            UploadResourceInfo resourceInfo = new UploadResourceInfo();
            resourceInfo.setPayloadName(payloadName);
            resourceInfo.setPayloadData(payloadData);
            Method privateMethod = null;
            privateMethod = AbstractValidationsServlet.class.getDeclaredMethod("getCsarFromPayload", UploadResourceInfo.class);
            privateMethod.setAccessible(true);
            returnValue = (Map<String, byte[]>) privateMethod.invoke(servlet, resourceInfo);
        } catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    protected CreateServiceFromYamlParameter getCsfyp() {
        CreateServiceFromYamlParameter csfyp = new CreateServiceFromYamlParameter();
        List<ArtifactDefinition> createdArtifacts =new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();

        csfyp.setNodeName("org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test");
        csfyp.setTopologyTemplateYaml(getMainTemplateContent());
        csfyp.setCreatedArtifacts(createdArtifacts);
        csfyp.setInTransaction(true);
        csfyp.setShouldLock(true);
        csfyp.setCsarInfo(getCsarInfo());
        csfyp.setParsedToscaYamlInfo(getParsedToscaYamlInfo());
        csfyp.setNodeTypesInfo(nodeTypesInfo);
        csfyp.setYamlName("group.yml");
        return csfyp;
    }

    protected ParsedToscaYamlInfo getParsedToscaYamlInfo(){
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        Map<String, InputDefinition> inputs = new HashMap<>();
        Map<String, UploadComponentInstanceInfo> instances=new HashMap<>();
        Map<String, GroupDefinition> groups=new HashMap<>();
        Map<String, PolicyDefinition> policies=new HashMap<>();
        parsedToscaYamlInfo.setGroups(groups);
        parsedToscaYamlInfo.setInputs(inputs);
        parsedToscaYamlInfo.setInstances(instances);
        parsedToscaYamlInfo.setPolicies(policies);
        return parsedToscaYamlInfo;
    }


    protected CsarInfo getCsarInfo ()
    {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = crateCsarFromPayload();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent();
        final Service service = createServiceObject(false);
        CsarInfo csarInfo = new CsarInfo(user, csarUuid,  csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
        return csarInfo;
    }

    public String getMainTemplateContent(){
        return "tosca_definitions_version: tosca_simple_yaml_1_1\n"
                + "metadata:\n"
                + "  invariantUUID: 6d17f281-683b-4198-a676-0faeecdc9025\n"
                + "  UUID: bfeab6b4-199b-4a2b-b724-de416c5e9811\n"
                + "  name: ser09080002\n"
                + "  description: ser09080002\n"
                + "  type: Service\n"
                + "  category: E2E Service\n"
                + "  serviceType: ''\n"
                + "  serviceRole: ''\n"
                + "  instantiationType: A-la-carte\n"
                + "  serviceEcompNaming: true\n"
                + "  ecompGeneratedNaming: true\n"
                + "  namingPolicy: ''\n"
                + "  environmentContext: General_Revenue-Bearing\n"
                + "  serviceFunction: ''\n"
                + "imports:\n"
                + "- nodes:\n"
                + "    file: nodes.yml\n"
                + "- datatypes:\n"
                + "    file: data.yml\n"
                + "- capabilities:\n"
                + "    file: capabilities.yml\n"
                + "- relationships:\n"
                + "    file: relationships.yml\n"
                + "- groups:\n"
                + "    file: groups.yml\n"
                + "- policies:\n"
                + "    file: policies.yml\n"
                + "- annotations:\n"
                + "    file: annotations.yml\n"
                + "- service-ser09080002-interface:\n"
                + "    file: service-Ser09080002-template-interface.yml\n"
                + "- resource-ExtCP:\n"
                + "    file: resource-Extcp-template.yml\n"
                + "- resource-zxjTestImportServiceAb:\n"
                + "    file: resource-Zxjtestimportserviceab-template.yml\n"
                + "- resource-zxjTestImportServiceAb-interface:\n"
                + "    file: resource-Zxjtestimportserviceab-template-interface.yml\n"
                + "- resource-zxjTestServiceNotAbatract:\n"
                + "    file: resource-Zxjtestservicenotabatract-template.yml\n"
                + "- resource-zxjTestServiceNotAbatract-interface:\n"
                + "    file: resource-Zxjtestservicenotabatract-template-interface.yml\n"
                + "- resource-ext ZTE VL:\n"
                + "    file: resource-ExtZteVl-template.yml\n"
                + "topology_template:\n"
                + "  inputs:\n"
                + "    skip_post_instantiation_configuration:\n"
                + "      default: true\n"
                + "      type: boolean\n"
                + "      required: false\n"
                + "    controller_actor:\n"
                + "      default: SO-REF-DATA\n"
                + "      type: string\n"
                + "      required: false\n"
                + "    cds_model_version:\n"
                + "      type: string\n"
                + "      required: false\n"
                + "    cds_model_name:\n"
                + "      type: string\n"
                + "      required: false\n"
                + "  node_templates:\n"
                + "    ext ZTE VL 0:\n"
                + "      type: tosca.nodes.nfv.ext.zte.VL\n"
                + "      metadata:\n"
                + "        invariantUUID: 27ab7610-1a97-4daa-938a-3b48e7afcfd0\n"
                + "        UUID: 9ea63e2c-4b8a-414f-93e3-5703ca5cee0d\n"
                + "        customizationUUID: e45e79b0-07ab-46b4-ac26-1e9f155ce53c\n"
                + "        version: '1.0'\n"
                + "        name: ext ZTE VL\n"
                + "        description: Ext ZTE VL\n"
                + "        type: VL\n"
                + "        category: Generic\n"
                + "        subcategory: Network Elements\n"
                + "        resourceVendor: ONAP (Tosca)\n"
                + "        resourceVendorRelease: 1.0.0.wd03\n"
                + "        resourceVendorModelNumber: ''\n"
                + "    zxjTestServiceNotAbatract 0:\n"
                + "      type: org.openecomp.resource.vf.Zxjtestservicenotabatract\n"
                + "      metadata:\n"
                + "        invariantUUID: ce39ce8d-6f97-4e89-8555-ae6789cdcf1c\n"
                + "        UUID: 4ac822be-f1ae-4ace-a4b8-bf6b5d977005\n"
                + "        customizationUUID: ee34e1e8-68e2-480f-8ba6-f257bbe90d6a\n"
                + "        version: '1.0'\n"
                + "        name: zxjTestServiceNotAbatract\n"
                + "        description: zxjTestServiceNotAbatract\n"
                + "        type: VF\n"
                + "        category: Network L4+\n"
                + "        subcategory: Common Network Resources\n"
                + "        resourceVendor: zxjImportService\n"
                + "        resourceVendorRelease: '1.0'\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        nf_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "        skip_post_instantiation_configuration: true\n"
                + "        multi_stage_design: 'false'\n"
                + "        controller_actor: SO-REF-DATA\n"
                + "        availability_zone_max_count: 1\n"
                + "      capabilities:\n"
                + "        mme_ipu_vdu.scalable:\n"
                + "          properties:\n"
                + "            max_instances: 1\n"
                + "            min_instances: 1\n"
                + "        mme_ipu_vdu.nfv_compute:\n"
                + "          properties:\n"
                + "            num_cpus: '2'\n"
                + "            flavor_extra_specs: {\n"
                + "              }\n"
                + "            mem_size: '8192'\n"
                + "    ExtCP 0:\n"
                + "      type: org.openecomp.resource.cp.extCP\n"
                + "      metadata:\n"
                + "        invariantUUID: 9b772728-93f5-424f-bb07-f4cae2783614\n"
                + "        UUID: 424ac220-4864-453e-b757-917fe4568ff8\n"
                + "        customizationUUID: 6e65d8a8-4379-4693-87aa-82f9e34b92fd\n"
                + "        version: '1.0'\n"
                + "        name: ExtCP\n"
                + "        description: The AT&T Connection Point base type all other CP derive from\n"
                + "        type: CP\n"
                + "        category: Generic\n"
                + "        subcategory: Network Elements\n"
                + "        resourceVendor: ONAP (Tosca)\n"
                + "        resourceVendorRelease: 1.0.0.wd03\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        mac_requirements:\n"
                + "          mac_count_required:\n"
                + "            is_required: false\n"
                + "        exCP_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "    zxjTestImportServiceAb 0:\n"
                + "      type: org.openecomp.resource.vf.Zxjtestimportserviceab\n"
                + "      metadata:\n"
                + "        invariantUUID: 41474f7f-3195-443d-a0a2-eb6020a56279\n"
                + "        UUID: 92e32e49-55f8-46bf-984d-a98c924037ec\n"
                + "        customizationUUID: 98c7a6c7-a867-45fb-8597-dd464f98e4aa\n"
                + "        version: '1.0'\n"
                + "        name: zxjTestImportServiceAb\n"
                + "        description: zxjTestImportServiceAbstract\n"
                + "        type: VF\n"
                + "        category: Generic\n"
                + "        subcategory: Abstract\n"
                + "        resourceVendor: zxjImportService\n"
                + "        resourceVendorRelease: '1.0'\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        nf_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "        skip_post_instantiation_configuration: true\n"
                + "        multi_stage_design: 'false'\n"
                + "        controller_actor: SO-REF-DATA\n"
                + "        availability_zone_max_count: 1\n"
                + "      requirements:\n"
                + "      - mme_ipu_vdu.dependency:\n"
                + "          capability: feature\n"
                + "          node: ExtCP 0\n"
                + "      - imagefile.dependency:\n"
                + "          capability: feature\n"
                + "          node: ext ZTE VL 0\n"
                + "      capabilities:\n"
                + "        mme_ipu_vdu.scalable:\n"
                + "          properties:\n"
                + "            max_instances: 1\n"
                + "            min_instances: 1\n"
                + "        mme_ipu_vdu.nfv_compute:\n"
                + "          properties:\n"
                + "            num_cpus: '2'\n"
                + "            flavor_extra_specs: {\n"
                + "              }\n"
                + "            mem_size: '8192'\n"
                + "  substitution_mappings:\n"
                + "    node_type: org.openecomp.service.Ser09080002\n"
                + "    capabilities:\n"
                + "      extcp0.feature:\n"
                + "      - ExtCP 0\n"
                + "      - feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.monitoring_parameter:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.monitoring_parameter\n"
                + "      zxjtestimportserviceab0.imagefile.guest_os:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.guest_os\n"
                + "      zxjtestimportserviceab0.imagefile.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.feature\n"
                + "      zxjtestservicenotabatract0.imagefile.guest_os:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.guest_os\n"
                + "      zxjtestimportserviceab0.ipu_cpd.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.virtualbinding:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.virtualbinding\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.feature\n"
                + "      extztevl0.feature:\n"
                + "      - ext ZTE VL 0\n"
                + "      - feature\n"
                + "      zxjtestimportserviceab0.imagefile.image_fle:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.image_fle\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.monitoring_parameter:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.monitoring_parameter\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.nfv_compute:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.nfv_compute\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.scalable:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.scalable\n"
                + "      extcp0.internal_connectionPoint:\n"
                + "      - ExtCP 0\n"
                + "      - internal_connectionPoint\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.virtualbinding:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.virtualbinding\n"
                + "      zxjtestservicenotabatract0.imagefile.image_fle:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.image_fle\n"
                + "      extztevl0.virtual_linkable:\n"
                + "      - ext ZTE VL 0\n"
                + "      - virtual_linkable\n"
                + "      zxjtestservicenotabatract0.imagefile.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.feature\n"
                + "      zxjtestimportserviceab0.localstorage.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.feature\n"
                + "      zxjtestservicenotabatract0.localstorage.local_attachment:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.local_attachment\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.scalable:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.scalable\n"
                + "      zxjtestservicenotabatract0.localstorage.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.feature\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.nfv_compute:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.nfv_compute\n"
                + "      zxjtestimportserviceab0.localstorage.local_attachment:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.local_attachment\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.feature\n"
                + "      zxjtestimportserviceab0.ipu_cpd.forwarder:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.forwarder:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "    requirements:\n"
                + "      zxjtestservicenotabatract0.imagefile.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.dependency\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.local_storage:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.local_storage\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.dependency\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.volume_storage:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.volume_storage\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.virtualbinding:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.virtualbinding\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.dependency\n"
                + "      zxjtestservicenotabatract0.localstorage.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.dependency\n"
                + "      zxjtestimportserviceab0.imagefile.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.volume_storage:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.volume_storage\n"
                + "      zxjtestimportserviceab0.ipu_cpd.virtualbinding:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.virtualbinding\n"
                + "      extcp0.virtualLink:\n"
                + "      - ExtCP 0\n"
                + "      - virtualLink\n"
                + "      extcp0.virtualBinding:\n"
                + "      - ExtCP 0\n"
                + "      - virtualBinding\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.guest_os:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.guest_os\n"
                + "      extcp0.dependency:\n"
                + "      - ExtCP 0\n"
                + "      - dependency\n"
                + "      zxjtestimportserviceab0.localstorage.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.dependency\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.virtualLink:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.virtualLink\n"
                + "      extztevl0.dependency:\n"
                + "      - ext ZTE VL 0\n"
                + "      - dependency\n"
                + "      zxjtestimportserviceab0.ipu_cpd.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.local_storage:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.local_storage\n"
                + "      zxjtestimportserviceab0.ipu_cpd.virtualLink:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.virtualLink\n"
                + "      extcp0.external_virtualLink:\n"
                + "      - ExtCP 0\n"
                + "      - external_virtualLink\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.guest_os:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.guest_os\n"
                + "      zxjtestimportserviceab0.ipu_cpd.forwarder:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.forwarder:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.forwarder\n";
    }

    protected void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
        ResponseFormat actualResponse = e.getResponseFormat() != null ?
                e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
        assertParseResponse(actualResponse, expectedStatus, variables);
    }

    private void assertParseResponse(ResponseFormat actualResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        assertThat(expectedResponse.getStatus()).isEqualTo(actualResponse.getStatus());
        assertThat(expectedResponse.getFormattedMessage()).isEqualTo(actualResponse.getFormattedMessage());
    }
}