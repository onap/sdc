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
import io.cucumber.java.hu.Ha;
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
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
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
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
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

        sIB1.setServiceBusinessLogic(serviceBusinessLogic);
        sIB1.setCsarBusinessLogic(csarBusinessLogic);
        sIB1.setServiceImportParseLogic(serviceImportParseLogic);
        sIB1.setToscaOperationFacade(toscaOperationFacade);

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
    public void testCreateService() {
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
        preparedResource.setResourceType(ResourceTypeEnum.VF);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle= new HashMap<>();
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> enumListEnumMap =
                new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinitions.add(artifactDefinition);
        enumListEnumMap.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                artifactDefinitions);
        nodeTypesArtifactsToHandle.put(nodeName,enumListEnumMap);
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,nodeTypesArtifactsToHandle);
        nodeTypeInfoToUpdateArtifacts.setNodeName(nodeName);
        nodeTypeInfoToUpdateArtifacts.setNodeTypesArtifactsToHandle(nodeTypesArtifactsToHandle);

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
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts",artifactDefinition);
        resource.setDeploymentArtifacts(deploymentArtifacts);
        try {
            sIB1.createOrUpdateSingleNonMetaArtifactToComstants(resource,csarInfo,artifactOperation,true,true);
        }catch (Exception e) {
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
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts",artifactDefinition);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put("artifacts",artifactDefinition);
        List<GroupDefinition> groups = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId("groupDefinitionUniqueId");
        groupDefinition.setName("groupDefinition");
        groups.add(groupDefinition);
        resource.setDeploymentArtifacts(deploymentArtifacts);
        resource.setArtifacts(artifacts);
        resource.setGroups(groups);
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
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.AAI_SERVICE_MODEL.name());
        artifactDefinition.setArtifactChecksum("artifactChecksum");
        existingArtifactsToHandle.add(artifactDefinition);
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
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = new
                EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        vfCsarArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,artifactPathAndNameList);
        Either<Resource, ResponseFormat> rrfe = sIB1.processCsarArtifacts(csarInfo,
                resource, createdArtifacts, true, true, resStatus, vfCsarArtifactsToHandle);
        assertNull(rrfe);
    }

    @Test
    public void testCreateOrUpdateSingleNonMetaArtifact(){
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = csarInfo.getCsar();
        String rootPath = System.getProperty("user.dir");
        Path path;
        byte[] data = new byte[0];
        path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        csar.put("valid_vf.csar",data);
        String artifactPath = "valid_vf.csar", artifactFileName = "", artifactType = "";
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
        ResourceMetadataDataDefinition componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
        componentMetadataDataDefinition.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
        ComponentMetadataDefinition componentMetadataDefinition = new ComponentMetadataDefinition(componentMetadataDataDefinition);
        nodeTypeResource.setComponentMetadataDefinition(componentMetadataDefinition);
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinitions.add(artifactDefinition);
        nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                artifactDefinitions);

        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        try {
            sIB1.handleNodeTypeArtifacts(nodeTypeResource, nodeTypeArtifactsToHandle,
                    createdArtifacts, user, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateOrUpdateServiceArtifacts(){
        ArtifactsBusinessLogic.ArtifactOperationEnum operation = ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        CsarInfo csarInfo =getCsarInfo();
        Service preparedService = createServiceObject(false);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts",artifactDefinition);
        preparedService.setDeploymentArtifacts(deploymentArtifacts);
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
        Service service = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(Constants.VENDOR_LICENSE_MODEL);
        artifactDefinition.setUniqueId("uniqueId");
        deploymentArtifacts.put("deploymentArtifacts",artifactDefinition);
        service.setDeploymentArtifacts(deploymentArtifacts);
        CsarInfo csarInfo = getCsarInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
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
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true,true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);

        Either<Service, ResponseFormat> result = sIB1.createOrUpdateNonMetaArtifacts(csarInfo,
                service, createdArtifacts, true, true, artifactOperation);
        assertEquals(result.left().value(),service);
    }

    @Test
    public void testFindServiceCsarArtifactsToHandle(){
        Service service = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts",artifactDefinition);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put("artifacts",artifactDefinition);
        List<GroupDefinition> groups = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId("groupDefinitionUniqueId");
        groupDefinition.setName("groupDefinition");
        groups.add(groupDefinition);
        service.setDeploymentArtifacts(deploymentArtifacts);
        service.setArtifacts(artifacts);
        service.setGroups(groups);
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();

        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>,
                ResponseFormat> result = sIB1.findVfCsarArtifactsToHandle(service, artifactPathAndNameList, user);
        assertNotNull(result.left().value());
    }

    @Test
    public void testOrganizeVfCsarArtifactsByServiceArtifactOperation(){
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.AAI_SERVICE_MODEL.name());
        artifactDefinition.setArtifactChecksum("artifactChecksum");
        existingArtifactsToHandle.add(artifactDefinition);
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
        Map<String, GroupDefinition> groups = getGroups();

        try {
            Either<Service, ResponseFormat> result = sIB1.createGroupsOnResource(service, groups);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void testCreateRIAndRelationsFromResourceYaml(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = new HashMap<>();
        String topologyTemplateYaml = getMainTemplateContent();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.createRIAndRelationsFromYaml(yamlName,resource,uploadComponentInstanceInfoMap,topologyTemplateYaml,nodeTypesNewCreatedArtifacts,
                    nodeTypesInfo,csarInfo,nodeTypesArtifactsToCreate,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateResourceInstancesRelations(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        resource.setComponentInstances(creatComponentInstances());
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        uploadResInstancesMap.put("uploadResInstancesMap",getuploadComponentInstanceInfo());

        try {
            sIB1.createResourceInstancesRelations(user,yamlName,resource,uploadResInstancesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessComponentInstance1(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List< AttributeDataDefinition >> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        originCompMap.put("componentUid",originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");

        try {
            sIB1.processComponentInstance(yamlName, resource, componentInstancesList,allDataTypes,instProperties,instCapabilties,instRequirements,
                    instDeploymentArtifacts,instArtifacts,instAttributes,originCompMap,instInputs,uploadComponentInstanceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddInputsValuesToRi(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        List<UploadPropInfo> uploadPropInfoList = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        uploadPropInfo.setName("uploadPropInfo");
        uploadPropInfoList.add(uploadPropInfo);
        uploadPropInfoList.add(uploadPropInfo);
        properties.put("propertiesMap",uploadPropInfoList);
        uploadComponentInstanceInfo.setProperties(properties);
        Resource resource = createParseResourceObject(true);
        Resource originResource =createParseResourceObject(false);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("uniqueId");
        inputs.add(inputDefinition);
        originResource.setInputs(inputs);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        try {
            sIB1.addInputsValuesToRi(uploadComponentInstanceInfo,resource,originResource,
                    currentCompInstance,instInputs,allDataTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessProperty(){
        Resource resource = createParseResourceObject(true);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("uniqueId");
        inputs.add(inputDefinition);
        resource.setInputs(inputs);
        ComponentInstance currentCompInstance = null;
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        inputDefinition.setType("inputDefinitionType");
        currPropertiesMap.put("propertyInfoName",inputDefinition);
        List<ComponentInstanceInput> instPropList = new ArrayList<>();
        List<UploadPropInfo> propertyList = getPropertyList();

        try {
            sIB1.processProperty(resource, currentCompInstance, allDataTypes,
                    currPropertiesMap, instPropList, propertyList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleSubstitutionMappings(){
        Resource resource = createParseResourceObject(true);
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();

        try {
            sIB1.handleSubstitutionMappings(resource, uploadResInstancesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateResourceInstances(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        nodesInfoValue.setName("zxjTestImportServiceAb");
        nodesInfoValue.setRequirements(gerRequirements());
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources",originResource);

        try {
            sIB1.createResourceInstances(yamlName,resource,uploadResInstancesMap,nodeNamespaceMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleNodeTypes(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        String topologyTemplateYaml = getMainTemplateContent();
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypeInfo.setNested(false);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        nodeTypesInfo.put(nodeName,nodeTypeInfo);
        CsarInfo csarInfo = getCsarInfo();

        try {
            sIB1.handleNodeTypes(yamlName,resource,topologyTemplateYaml,needLock,
                    nodeTypesArtifactsToHandle,nodeTypesNewCreatedArtifacts,nodeTypesInfo,
                    csarInfo,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleNestedVfc1(){
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Resource resource = createParseResourceObject(false);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypeInfo.setTemplateFileName("groups.yml");
        nodeTypeInfo.setMappedToscaTemplate(new HashMap<>());
        nodesInfo.put(nodeName,nodeTypeInfo);
        CsarInfo csarInfo = getCsarInfo();

        try {
            sIB1.handleNestedVfc(resource,nodeTypesArtifactsToHandle,createdArtifacts,
                    nodesInfo,csarInfo,nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleComplexVfc1(){
        Resource resource = createParseResourceObject(true);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";

        try {
            sIB1.handleComplexVfc(resource,nodeTypesArtifactsToHandle,createdArtifacts,
                    nodesInfo,csarInfo,nodeName,yamlName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateResourcesFromYamlNodeTypesList1(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(false);
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();

        try {
            sIB1.createResourcesFromYamlNodeTypesList(yamlName,resource,mappedToscaTemplate,
                    needLock, nodeTypesArtifactsToHandle,nodeTypesNewCreatedArtifacts,
                    nodeTypesInfo,csarInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNodeTypes1(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(false);
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> enumListEnumMap =
                new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinitions.add(artifactDefinition);
        enumListEnumMap.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                artifactDefinitions);
        nodeTypesArtifactsToHandle.put("nodeTyp",enumListEnumMap);
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypesInfo.put("nodeTyp",nodeTypeInfo);
        CsarInfo csarInfo = getCsarInfo();
        Map<String, Object> mapToConvert =new HashMap<>();
        Map<String, Object> nodeTypes =new HashMap<>();
        nodeTypes.put("nodeTyp",nodeTypeInfo);

        try {
            sIB1.createNodeTypes(yamlName, resource, needLock, nodeTypesArtifactsToHandle,
                    nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert,
                    nodeTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNodeTypeResourceFromYaml(){
        String yamlName = "group.yml";
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String,Object> nodeMap = new HashMap<>();
        nodeMap.put(nodeName,getGroupsYaml());
        Map.Entry<String, Object> nodeNameValue = nodeMap.entrySet().iterator().next();
        Map<String, Object> mapToConvert = new HashedMap();
        Resource resourceVf = createParseResourceObject(false);
        boolean needLock = true;
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        boolean forceCertificationAllowed = true;
        CsarInfo csarInfo = getCsarInfo();
        boolean isNested = true;

        try {
            sIB1.createNodeTypeResourceFromYaml(yamlName,nodeNameValue,user,mapToConvert,resourceVf,
                    needLock,nodeTypeArtifactsToHandle,nodeTypesNewCreatedArtifacts,
                    forceCertificationAllowed,csarInfo,isNested);
        } catch (Exception e) {
            e.printStackTrace();
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
        service.setComponentInstances(creatComponentInstances());
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        uploadResInstancesMap.put("uploadResInstancesMap",getuploadComponentInstanceInfo());

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
        Resource originResource = createParseResourceObject(false);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List< AttributeDataDefinition >> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        originCompMap.put("componentUid",originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");

        try {
            sIB1.processComponentInstance(yamlName, service, componentInstancesList,allDataTypes,instProperties,instCapabilties,instRequirements,
                    instDeploymentArtifacts,instArtifacts,instAttributes,originCompMap,instInputs,uploadComponentInstanceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddInputsValuesToRi2(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        List<UploadPropInfo> uploadPropInfoList = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        uploadPropInfo.setName("uploadPropInfo");
        uploadPropInfoList.add(uploadPropInfo);
        uploadPropInfoList.add(uploadPropInfo);
        properties.put("propertiesMap",uploadPropInfoList);
        uploadComponentInstanceInfo.setProperties(properties);
        Service resource = createServiceObject(true);
        Resource originResource =createParseResourceObject(false);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("uniqueId");
        inputs.add(inputDefinition);
        originResource.setInputs(inputs);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        try {
            sIB1.addInputsValuesToRi(uploadComponentInstanceInfo,resource,originResource,
                        currentCompInstance,instInputs,allDataTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessProperty2(){
        Service resource = createServiceObject(true);
        ComponentInstance currentCompInstance = null;
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setType("inputDefinitionType");
        currPropertiesMap.put("propertyInfoName",inputDefinition);
        List<ComponentInstanceInput> instPropList = new ArrayList<>();
        List<UploadPropInfo> propertyList = new ArrayList<>();
        List<GetInputValueDataDefinition> get_input = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        get_input.add(getInputValueDataDefinition);
        UploadPropInfo propertyInfo = new UploadPropInfo();
        propertyInfo.setValue("value");
        propertyInfo.setGet_input(get_input);
        propertyInfo.setName("propertyInfoName");
        propertyList.add(propertyInfo);

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
        uploadComponentInstanceInfo.setProperties(getUploadPropInfoProperties());
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
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
    public void testAddPropertyValuesToRi2(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setProperties(getUploadPropInfoProperties());
        Service service = createServiceObject(false);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        try {
            sIB1.addPropertyValuesToRi(uploadComponentInstanceInfo, service, originResource, currentCompInstance,
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
        uploadComponentInstanceInfo.setProperties(getUploadPropInfoProperties());        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
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
    public void testHandleSubstitutionMappings2(){
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
        List<ComponentInstance> componentInstances = creatComponentInstances();
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = getUploadResInstancesMap();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirement = new HashMap<>();

        sIB1.fillUpdatedInstCapabilitiesRequirements(componentInstances,uploadResInstancesMap,
                updatedInstCapabilities,updatedInstRequirement);
    }

    @Test
    public void testFillUpdatedInstCapabilities(){
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties = new HashMap<>();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node",capabilityDefinitionList);
        ComponentInstance instance = new ComponentInstance();
        instance.setCapabilities(capabilities);
        Map<String, String> capabilitiesNamesToUpdate = new HashMap<>();
        capabilitiesNamesToUpdate.put("mme_ipu_vdu.feature","capabilitiesNamesToUpdate");

        sIB1.fillUpdatedInstCapabilities(updatedInstCapabilties,instance,capabilitiesNamesToUpdate);
    }

    @Test
    public void testFillUpdatedInstRequirements(){
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new
                HashMap<>();
        ComponentInstance instance = new ComponentInstance();
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node",requirementDefinitionList);
        instance.setRequirements(requirements);
        Map<String, String> requirementsNamesToUpdate = new HashMap<>();
        requirementsNamesToUpdate.put("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test",
                "requirementsNamesToUpdate");


        sIB1.fillUpdatedInstRequirements(updatedInstRequirements,instance,requirementsNamesToUpdate);
    }

    @Test
    public void testAddRelationsToRI(){
        String yamlName = "group.yml";
        Service service = createServiceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
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

        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
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
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadResInstancesMap",nodesInfoValue);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        Resource resource = createParseResourceObject(true);
        resource.setToscaResourceName("toscaResourceName");
        nodeNamespaceMap.put("nodeNamespaceMap",resource);

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
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
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
        Map<String, NodeTypeInfo> nodesInfo = getNodeTypesInfo();
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
        Map<String, NodeTypeInfo> nodesInfo = getNodeTypesInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        csar.put(yamlName,yamlName.getBytes());
        csarInfo.setCsar(csar);
        Resource oldComplexVfc = createParseResourceObject(false);
        Resource newComplexVfc = createParseResourceObject(true);

        try {
            sIB1.handleComplexVfc(nodeTypesArtifactsToHandle,createdArtifacts,nodesInfo,
                    csarInfo,nodeName,yamlName,oldComplexVfc,newComplexVfc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateResourceFromYaml(){
        Resource oldRresource = createParseResourceObject(false);
        Resource newRresource = createParseResourceObject(true);
        AuditingActionEnum actionEnum = AuditingActionEnum.CREATE_RESOURCE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        String yamlFileContent = getYamlFileContent();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        boolean isNested = true;

        try {
            sIB1.updateResourceFromYaml(oldRresource,newRresource,actionEnum,createdArtifacts,yamlFileName,yamlFileContent,
                    csarInfo,nodeTypesInfo,nodeTypesArtifactsToHandle,nodeName,isNested);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateResourceFromYaml(){
        Resource resource = createParseResourceObject(true);
        String topologyTemplateYaml = getMainTemplateContent();
        String yamlName = "group.yml";
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        boolean shouldLock = true;
        boolean inTransaction =true;
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.createResourceFromYaml(resource,topologyTemplateYaml,yamlName,nodeTypesInfo,csarInfo,
                    nodeTypesArtifactsToCreate,shouldLock,inTransaction,nodeName);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateResourceAndRIsFromYaml(){
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(false);
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        AuditingActionEnum actionEnum = AuditingActionEnum.CREATE_RESOURCE;
        boolean isNormative = true;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String topologyTemplateYaml = getMainTemplateContent();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        boolean shouldLock = true;
        boolean inTransaction = true;
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        try {
            sIB1.createResourceAndRIsFromYaml(yamlName,resource,parsedToscaYamlInfo,actionEnum,
                    isNormative,createdArtifacts,topologyTemplateYaml,nodeTypesInfo,csarInfo,
                    nodeTypesArtifactsToCreate,shouldLock,inTransaction,nodeName);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateGroupsOnResource2(){
        Resource resource = createParseResourceObject(false);
        Map<String, GroupDefinition> groups = new HashMap<>();

        Either<Resource, ResponseFormat> result = sIB1.createGroupsOnResource(resource, groups);
        assertEquals(result.left().value(),resource);
    }

    @Test
    public void testUpdateGroupsMembersUsingResource2(){
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = null;

        List<GroupDefinition> groupDefinitions = sIB1.updateGroupsMembersUsingResource(groups,resource);
        for (GroupDefinition groupDefinition : groupDefinitions) {
            assertNull(groupDefinition);
        }
    }

    @Test
    public void testUpdateGroupMembers(){
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition updatedGroupDefinition = new GroupDefinition();
        Resource component = createParseResourceObject(true);
        List<ComponentInstance> componentInstances = creatComponentInstances();
        String groupName = "tosca_simple_yaml_1_1";
        Map<String, String> members = new HashMap<>();
        members.put("zxjTestImportServiceAb",getGroupsYaml());

        try {
            sIB1.updateGroupMembers(groups,updatedGroupDefinition,component,componentInstances,
                    groupName,members);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setCreateResourceTransaction(){
        Resource resource = createParseResourceObject(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        boolean isNormative = true;

        try {
            sIB1.createResourceTransaction(resource,user,isNormative);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateExistingResourceByImport(){
        Resource newResource = createParseResourceObject(false);
        Resource oldResource = createParseResourceObject(true);

        try {
            sIB1.updateExistingResourceByImport(newResource,oldResource,user,
                    true,true,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateResourcesFromYamlNodeTypesList(){
        String yamlName = "group.yml";
        Service service =createServiceObject(false);
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();

        try {
            sIB1.createResourcesFromYamlNodeTypesList(yamlName,service,mappedToscaTemplate,needLock,
                    nodeTypesArtifactsToHandle,nodeTypesNewCreatedArtifacts,nodeTypesInfo,csarInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNodeTypes(){
        String yamlName = "group.yml";
        Service service =createServiceObject(false);
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> enumListEnumMap =
                new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinitions.add(artifactDefinition);
        enumListEnumMap.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                artifactDefinitions);
        nodeTypesArtifactsToHandle.put("nodeTyp",enumListEnumMap);        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, Object> mapToConvert =new HashMap<>();
        Map<String, Object> nodeTypes =new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypesInfo.put("nodeTyp",nodeTypeInfo);
        nodeTypes.put("org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test",
                nodeTypeInfo);

        try {
            sIB1.createNodeTypes(yamlName, service, needLock, nodeTypesArtifactsToHandle,
                    nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert,
                    nodeTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected List<UploadPropInfo> getPropertyList() {
        List<UploadPropInfo> propertyList = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        List<GetInputValueDataDefinition> get_input = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        get_input.add(getInputValueDataDefinition);
        uploadPropInfo.setName("propertiesName");
        uploadPropInfo.setValue("value");
        uploadPropInfo.setGet_input(get_input);
        propertyList.add(uploadPropInfo);
        return propertyList;
    }

    protected Map<String, GroupDefinition> getGroups() {
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("groupDefinitionName");
        groups.put("groupsMap",groupDefinition);
        return groups;
    }

    protected Map<String, NodeTypeInfo> getNodeTypesInfo(){
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        nodeTypeInfo.setTemplateFileName("templateFileName");
        nodeTypeInfo.setMappedToscaTemplate(mappedToscaTemplate);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        nodeTypesInfo.put(nodeName,nodeTypeInfo);
        return nodeTypesInfo;
    }

    protected Map<String, UploadComponentInstanceInfo> getUploadResInstancesMap(){
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getuploadComponentInstanceInfo();
        Map<String, String> capabilitiesNamesToUpdate = new HashMap<>();
        capabilitiesNamesToUpdate.put("mme_ipu_vdu.feature","capabilitiesNamesToUpdate");
        Map<String, String> requirementsNamesToUpdate = new HashMap<>();
        requirementsNamesToUpdate.put("mme_ipu_vdu.feature","capabilitiesNamesToUpdate");
        uploadResInstancesMap.put("zxjTestImportServiceAb",uploadComponentInstanceInfo);
        return uploadResInstancesMap;
    }

    protected Map<String, List<UploadPropInfo>> getUploadPropInfoProperties(){
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        List<UploadPropInfo> uploadPropInfoList = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        List<GetInputValueDataDefinition> get_input = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        get_input.add(getInputValueDataDefinition);
        uploadPropInfo.setName("propertiesName");
        uploadPropInfo.setValue("value");
        uploadPropInfo.setGet_input(get_input);
        uploadPropInfoList.add(uploadPropInfo);
        properties.put("uploadComponentInstanceInfo",uploadPropInfoList);
        return properties;
    }

    protected List<PropertyDefinition> getProperties(){
        List<PropertyDefinition> properties = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("propertiesName");
        properties.add(propertyDefinition);
        return properties;
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
        Map<String, List<UploadReqInfo>> requirements = new HashMap<>();
        List<UploadReqInfo> uploadReqInfoList = new ArrayList<>();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        uploadReqInfo.setName("uploadReqInfo");
        uploadReqInfo.setCapabilityName("tosca.capabilities.Node");
        uploadReqInfoList.add(uploadReqInfo);
        requirements.put("requirements",uploadReqInfoList);
        uploadNodeFilterInfo.setName("mme_ipu_vdu.virtualbinding");
        uploadComponentInstanceInfo.setCapabilities(getCapabilities());
        uploadComponentInstanceInfo.setRequirements(requirements);
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
    protected Map<String, byte[]> crateCsarFromPayload() {
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

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node",requirementDefinitionList);
        componentInstance.setRequirements(requirements);
        componentInstance.setCapabilities(capabilities);
        componentInstance.setUniqueId("uniqueId");
        componentInstance.setComponentUid("componentUid");
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


	protected CsarUtils.NonMetaArtifactInfo getNonMetaArtifactInfo(){
        String artifactName = "artifactName",path = "/src/test/resources/valid_vf.csar",artifactType = "AAI_SERVICE_MODEL";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String rootPath = System.getProperty("user.dir");
        Path path2;
        byte[] data = new byte[0];
        path2 = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
        try {
            data = Files.readAllBytes(path2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String artifactUniqueId = "artifactUniqueId";
        boolean isFromCsar = true;
        CsarUtils.NonMetaArtifactInfo  nonMetaArtifactInfo = new CsarUtils.NonMetaArtifactInfo(artifactName,
                path,artifactType,artifactGroupType,data,artifactUniqueId,isFromCsar);
        return nonMetaArtifactInfo;

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