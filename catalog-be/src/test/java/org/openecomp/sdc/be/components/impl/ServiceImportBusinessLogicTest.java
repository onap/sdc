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
import java.nio.file.FileSystems;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
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
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");;
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
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");;
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

    @Test
    public void testAddRelationsToRI(){
        String yamlName = "group.yml";
        Service service = createServiceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        nodesInfoValue.setName("zxjTestImportServiceAb");
        nodesInfoValue.setRequirements(gerRequirements());
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        try {
            sIB1.addRelationsToRI(yamlName,service,uploadResInstancesMap,componentInstancesList,
                    relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddRelationToRI(){
        String yamlName = "group.yml";
        Service service = createServiceObject(false);
        service.setComponentInstances(creatComponentInstances());

        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        nodesInfoValue.setName("zxjTestImportServiceAb");
        nodesInfoValue.setRequirements(gerRequirements());
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        try {
            sIB1.addRelationToRI(yamlName,service,nodesInfoValue,relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetResourceAfterCreateRelations(){
        Service service = createServiceObject(false);
        ComponentParametersView componentParametersView = createComponentParametersView();
        when(serviceImportParseLogic.getComponentFilterAfterCreateRelations()).thenReturn(componentParametersView);
        try {
            sIB1.getResourceAfterCreateRelations(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceInstances(){
        String yamlName = "group.yml";
        Service service = createServiceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();

        try {
            sIB1.createServiceInstances(yamlName,service,uploadResInstancesMap,nodeNamespaceMap);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndAddResourceInstance(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getuploadComponentInstanceInfo();
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(false);
        Resource originResource = createParseResourceObject(true);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources",originResource);
        Map<String, Resource> existingnodeTypeMap = new HashMap<>();
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();

        try {
            sIB1.createAndAddResourceInstance(uploadComponentInstanceInfo,yamlName,resource,nodeNamespaceMap,
                    existingnodeTypeMap,resourcesInstancesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndAddResourceInstances(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getuploadComponentInstanceInfo();
        String yamlName = "group.yml";
        Service service = createServiceObject(false);
        service.setServiceType("services");
        Resource originResource = createParseResourceObject(true);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);
        Map<String, Resource> existingnodeTypeMap = new HashMap<>();
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();

        try {
            sIB1.createAndAddResourceInstance(uploadComponentInstanceInfo, yamlName, service, nodeNamespaceMap,
                    existingnodeTypeMap, resourcesInstancesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateResourceInstanceBeforeCreate(){
        String yamlName = "group.yml";
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getuploadComponentInstanceInfo();
        Resource originResource = createParseResourceObject(true);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);

        try {
            sIB1.validateResourceInstanceBeforeCreate(yamlName,uploadComponentInstanceInfo,nodeNamespaceMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleServiceNodeTypes(){
        String yamlName = "group.yml";
        Service service =createServiceObject(false);
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");;
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.handleServiceNodeTypes(yamlName,service,topologyTemplateYaml,needLock,
                    nodeTypesArtifactsToHandle,nodeTypesNewCreatedArtifacts,nodeTypesInfo,
                    csarInfo,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateResourceNotExisted(){
        String type = "org.openecomp.resource.vf";
        boolean b = false;
        try {
            b = sIB1.validateResourceNotExisted(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleNestedVF(){
        Service service =createServiceObject(false);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.handleNestedVF(service,nodeTypesArtifactsToHandle,createdArtifacts,
                    nodesInfo,csarInfo,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleNestedVfc(){
        Service service =createServiceObject(false);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.handleNestedVfc(service,nodeTypesArtifactsToHandle,createdArtifacts,
                    nodesInfo,csarInfo,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleComplexVfc(){
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";

        try {
            sIB1.handleComplexVfc(nodeTypesArtifactsToHandle,createdArtifacts,
                    nodesInfo,csarInfo,nodeName,yamlName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testHandleComplexVfc2(){
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        Resource oldComplexVfc = createParseResourceObject(false);
        Resource newComplexVfc = createParseResourceObject(true);

        try {
            sIB1.handleComplexVfc(nodeTypesArtifactsToHandle,createdArtifacts,nodesInfo,
                    csarInfo,nodeName,yamlName,oldComplexVfc,newComplexVfc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected UploadComponentInstanceInfo getuploadComponentInstanceInfo(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setType("resources");
        Collection<String> directives = new Collection<String>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends String> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }
        };
        uploadComponentInstanceInfo.setDirectives(directives);
        UploadNodeFilterInfo uploadNodeFilterInfo = new UploadNodeFilterInfo();
        uploadNodeFilterInfo.setName("mme_ipu_vdu.virtualbinding");
        uploadComponentInstanceInfo.setCapabilities(getCapabilities());
        return uploadComponentInstanceInfo;
    }

    protected Map<String, List<UploadCapInfo>> getCapabilities(){
        List<UploadCapInfo> uploadCapInfoList = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        uploadCapInfo.setNode("tosca.nodes.Root");
        uploadCapInfo.setName("mme_ipu_vdu.dependency");
        uploadCapInfoList.add(uploadCapInfo);
        Map<String, List<UploadCapInfo>> uploadCapInfoMap = new HashMap<>();
        uploadCapInfoMap.put("tosca.capabilities.Node",uploadCapInfoList);
        return uploadCapInfoMap;
    }

    protected Map<String, List<UploadReqInfo>> gerRequirements(){
        Map<String, List<UploadReqInfo>> uploadReqInfoMap = new HashMap<>();
        String requirementName = "tosca.capabilities.Node";
        List<UploadReqInfo> uploadReqInfoList = new ArrayList<>();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        uploadReqInfo.setCapabilityName("tosca.capabilities.Node");
        uploadReqInfoMap.put(requirementName,uploadReqInfoList);
        return uploadReqInfoMap;
    }

    protected ComponentParametersView createComponentParametersView() {
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreComponentInstancesProperties(false);
        parametersView.setIgnoreCapabilities(false);
        parametersView.setIgnoreRequirements(false);
        parametersView.setIgnoreGroups(false);
        return parametersView;
    }
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


    protected List<ComponentInstance> creatComponentInstances(){
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node",capabilityDefinitionList);
        componentInstance.setCapabilities(capabilities);
        componentInstance.setName("zxjTestImportServiceAb");
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    protected CreateServiceFromYamlParameter getCsfyp() {
        CreateServiceFromYamlParameter csfyp = new CreateServiceFromYamlParameter();
        List<ArtifactDefinition> createdArtifacts =new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();

        csfyp.setNodeName("org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test");
        csfyp.setTopologyTemplateYaml(getMainTemplateContent("service_import_template.yml"));
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

    String getMainTemplateContent(String fileName){
        String mainTemplateContent = null;
        try {
            mainTemplateContent = loadFileNameToJsonString(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mainTemplateContent;
    }

    protected CsarInfo getCsarInfo ()
    {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = crateCsarFromPayload();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent("service_import_template.yml");
        CsarInfo csarInfo = new CsarInfo(user, csarUuid,  csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
        return csarInfo;
    }

    public static String loadFileNameToJsonString(String fileName) throws IOException {
        String sourceDir = "src/test/resources/normativeTypes";
        return loadFileNameToJsonString(sourceDir, fileName);
    }

    private static String loadFileNameToJsonString(String sourceDir, String fileName) throws IOException {
        java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir, fileName);
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
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