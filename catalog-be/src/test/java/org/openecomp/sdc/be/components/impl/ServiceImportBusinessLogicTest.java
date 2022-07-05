/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.ServiceImportBusinessLogic.CREATE_RESOURCE;

import fj.data.Either;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CreateServiceFromYamlParameter;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.ArtifactExternalServlet;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.IPropertyInputCommon;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

class ServiceImportBusinessLogicTest extends ServiceImportBussinessLogicBaseTestSetup {

    private static final String DEFAULT_ICON = "defaulticon";

    private final ArtifactDefinition artifactDefinition = mock(ArtifactDefinition.class);
    private final ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private final ServletUtils servletUtils = mock(ServletUtils.class);
    private final AbstractValidationsServlet servlet = new ArtifactExternalServlet(userBusinessLogic,
        componentInstanceBusinessLogic, componentsUtils, servletUtils, resourceImportManager, artifactsBusinessLogic);
    @InjectMocks
    private ServiceImportBusinessLogic sIBL;

    public static String loadFileNameToJsonString(String fileName) throws IOException {
        String sourceDir = "src/test/resources/normativeTypes";
        return loadFileNameToJsonString(sourceDir, fileName);
    }

    private static String loadFileNameToJsonString(String sourceDir, String fileName) throws IOException {
        java.nio.file.Path filePath = FileSystems.getDefault().getPath(sourceDir, fileName);
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
        when(artifactDefinition.getMandatory()).thenReturn(true);
        when(artifactDefinition.getArtifactName()).thenReturn("creatorFullName");
        when(artifactDefinition.getArtifactType()).thenReturn("TOSCA_CSAR");
    }

    @Test
    void testCreateService_OK() {
        Service oldService = createServiceObject(true);
        String payloadName = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        Service newService = createServiceObject(true);
        newService.setComponentInstances(creatComponentInstances());

        when(serviceBusinessLogic.validateServiceBeforeCreate(eq(newService), any(User.class), any(AuditingActionEnum.class)))
            .thenReturn(Either.left(newService));
        when(toscaOperationFacade.validateCsarUuidUniqueness(anyString())).thenReturn(StorageOperationStatus.OK);
        CsarInfo csarInfo = getCsarInfo();
        when(csarBusinessLogic.getCsarInfo(any(Service.class), any(), any(User.class), any(Map.class), anyString())).thenReturn(csarInfo);
        when(serviceImportParseLogic.findNodeTypesArtifactsToHandle(any(Map.class), any(CsarInfo.class), any(Service.class)))
            .thenReturn(Either.left(new HashMap<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>>()));
        when(csarBusinessLogic.getParsedToscaYamlInfo(anyString(), anyString(), any(), any(CsarInfo.class), any(), any(Service.class)))
            .thenReturn(getParsedToscaYamlInfo());
        when(serviceBusinessLogic.lockComponentByName(newService.getSystemName(), oldService, CREATE_RESOURCE)).thenReturn(Either.left(true));
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(createOldResource()));
        when(serviceImportParseLogic.createServiceTransaction(oldService, csarInfo.getModifier(), false)).thenReturn(newService);
        when(serviceImportParseLogic.createInputsOnService(eq(oldService), anyMap())).thenReturn(newService);
        Assertions.assertDoesNotThrow(() -> {
            when(serviceImportParseLogic.createSubstitutionFilterOnService(eq(oldService), any())).thenReturn(newService);
        });
        when(serviceImportParseLogic.getNodeTypesFromTemplate(anyMap())).thenReturn(getNodeTypes());
        when(serviceImportParseLogic.createNodeTypeResourceFromYaml(anyString(), any(Map.Entry.class), any(User.class), anyMap(), any(Service.class),
            anyBoolean(), any(), anyList(), anyBoolean(), any(CsarInfo.class), anyBoolean())).thenReturn(
            new ImmutablePair<>(new Resource(), ActionStatus.OK));
        when(serviceImportParseLogic.getComponentWithInstancesFilter()).thenReturn(new ComponentParametersView());
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class))).thenReturn(Either.left(newService));
        when(serviceImportParseLogic.getComponentFilterAfterCreateRelations()).thenReturn(new ComponentParametersView());
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class))).thenReturn(Either.left(newService));
        when(serviceImportParseLogic.findAvailableRequirement(anyString(), anyString(), any(UploadComponentInstanceInfo.class),
            any(ComponentInstance.class), anyString())).thenReturn(Either.left(new RequirementDefinition()));
        when(serviceImportParseLogic.findAvailableCapabilityByTypeOrName(any(RequirementDefinition.class), any(ComponentInstance.class),
            any(UploadReqInfo.class))).thenReturn(new CapabilityDefinition());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK), anyString())).thenReturn(new ResponseFormat(200));
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(newService));
        doNothing().when(compositionBusinessLogic).setPositionsForComponentInstances(any(Service.class), anyString());
        when(groupBusinessLogic.validateUpdateVfGroupNames(anyMap(), anyString())).thenReturn(Either.left(new HashMap<>()));
        when(csarArtifactsAndGroupsBusinessLogic.deleteVFModules(any(Service.class), any(CsarInfo.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(newService));
        when(serviceImportParseLogic.getServiceWithGroups(anyString())).thenReturn(newService);
        when(mockJanusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(graphLockOperation.unlockComponentByName(anyString(), anyString(), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
        when(serviceImportParseLogic.createOutputsOnService(any(Service.class), any(), anyString())).thenReturn(newService);

        Service result = sIBL.createService(oldService, AuditingActionEnum.CREATE_RESOURCE, user, payload, payloadName);
        assertNotNull(result);
        assertNotNull(result.getComponentInstances());
        assertEquals(1, result.getComponentInstances().size());
        assertNotNull(result.getComponentInstances().get(0));
        assertNotNull(result.getComponentInstances().get(0).getCapabilities());
        assertEquals(1, result.getComponentInstances().get(0).getCapabilities().size());
        assertNotNull(result.getComponentInstances().get(0).getRequirements());
        assertEquals(1, result.getComponentInstances().get(0).getRequirements().size());
        assertNotNull(result.getCategories());
        assertEquals(1, result.getCategories().size());
    }

    @Test
    void testCreateService_Fail_NotFoundNodeTypesArtifacts() {
        Service oldService = createServiceObject(true);
        String payloadName = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        Service newService = createServiceObject(true);

        when(serviceBusinessLogic.validateServiceBeforeCreate(any(Service.class), any(User.class), any(AuditingActionEnum.class)))
            .thenReturn(Either.left(newService));
        when(toscaOperationFacade.validateCsarUuidUniqueness(anyString())).thenReturn(StorageOperationStatus.OK);
        when(csarBusinessLogic.getCsarInfo(any(Service.class), any(), any(User.class), any(Map.class), anyString())).thenReturn(getCsarInfo());
        when(serviceImportParseLogic.findNodeTypesArtifactsToHandle(any(Map.class), any(CsarInfo.class), any(Service.class)))
            .thenReturn(Either.right(ActionStatus.GENERAL_ERROR));
        when(csarBusinessLogic.getParsedToscaYamlInfo(anyString(), anyString(), any(), any(CsarInfo.class), anyString(),
            any(Service.class))).thenReturn(getParsedToscaYamlInfo());

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.createService(oldService, AuditingActionEnum.CREATE_RESOURCE, user, payload, payloadName));
    }

    @Test
    void testCreateServiceFromCsar() {
        Service oldService = createServiceObject(true);
        String csarUUID = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> map =
            new HashedMap();

        when(csarBusinessLogic.getCsarInfo(any(Service.class), any(), any(User.class),
            any(Map.class), anyString())).thenReturn(csarInfo);
        when(serviceImportParseLogic.findNodeTypesArtifactsToHandle(any(Map.class), any(CsarInfo.class),
            any(Service.class))).thenReturn(Either.left(map));
        Assertions.assertThrows(ComponentException.class, () -> sIBL.createServiceFromCsar(oldService,
            user, payload, csarUUID));
    }

    @Test
    void testCreateServiceFromYaml() {
        Service oldService = createServiceObject(true);
        Resource resource = createOldResource();
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");
        String yamlName = "group.yml";
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(resource));
        when(csarBusinessLogic.getParsedToscaYamlInfo(anyString(), anyString(), any(Map.class),
            eq(csarInfo), anyString(), any(Component.class))).thenReturn(parsedToscaYamlInfo);
        when(serviceBusinessLogic.lockComponentByName(anyString(), any(Service.class), anyString()))
            .thenReturn(Either.left(true));

        Assertions.assertThrows(ComponentException.class, () -> sIBL.createServiceFromYaml(oldService,
            topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo,
            nodeTypesArtifactsToCreate, false, true, nodeName, user.getUserId()));
    }

    @Test
    void testCreateServiceAndRIsFromYaml() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Service oldService = createServiceObject(true);
        Resource resource = createOldResource();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        CreateServiceFromYamlParameter csfyp = getCsfyp();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();
        csfyp.setNodeTypesInfo(nodeTypesInfo);
        csfyp.setParsedToscaYamlInfo(parsedToscaYamlInfo);
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(resource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL.createServiceAndRIsFromYaml(oldService,
            false, nodeTypesArtifactsToCreate, false, true, csfyp, user.getUserId()));
    }

    @Test
    void testCreateServiceAndRIsFromYamlShoudLook() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Service oldService = createServiceObject(true);
        Resource resource = createOldResource();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        CreateServiceFromYamlParameter csfyp = getCsfyp();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();
        csfyp.setNodeTypesInfo(nodeTypesInfo);
        csfyp.setParsedToscaYamlInfo(parsedToscaYamlInfo);
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(resource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL.createServiceAndRIsFromYaml(oldService,
            false, nodeTypesArtifactsToCreate, false, true, csfyp, user.getUserId()));
    }

    @Test
    void testCreateOrUpdateArtifacts() {
        ArtifactsBusinessLogic.ArtifactOperationEnum operation = ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        CsarInfo csarInfo = getCsarInfo();
        Resource preparedResource = createParseResourceObject(false);
        preparedResource.setResourceType(ResourceTypeEnum.VF);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> enumListEnumMap =
            new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        List<ArtifactDefinition> artifactDefinitions = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinitions.add(artifactDefinition);
        enumListEnumMap.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
            artifactDefinitions);
        nodeTypesArtifactsToHandle.put(nodeName, enumListEnumMap);
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToHandle);
        nodeTypeInfoToUpdateArtifacts.setNodeName(nodeName);
        nodeTypeInfoToUpdateArtifacts.setNodeTypesArtifactsToHandle(nodeTypesArtifactsToHandle);

        Assertions.assertNotNull(
            sIBL.createOrUpdateArtifacts(operation, createdArtifacts, yamlFileName, csarInfo,
                preparedResource, nodeTypeInfoToUpdateArtifacts, true, true)
        );
    }

    @Test
    void testHandleVfCsarArtifacts() {
        Resource resource = createParseResourceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(Constants.VENDOR_LICENSE_MODEL);
        artifactDefinition.setUniqueId("uniqueId");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        resource.setDeploymentArtifacts(deploymentArtifacts);
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        String csarKey = CsarUtils.ARTIFACTS_PATH + "HEAT.meta";
        byte[] artifactsMetaBytes = "src/test/resources/normativeTypes/valid_vf.csar".getBytes();
        csar.put(csarKey, artifactsMetaBytes);
        csarInfo.setCsar(csar);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(resource));
        when(csarArtifactsAndGroupsBusinessLogic
            .createResourceArtifactsFromCsar(any(CsarInfo.class), any(Resource.class), anyString(), anyString(),
                anyList())).thenReturn(Either.left(resource));
        Assertions.assertNotNull(
            sIBL.handleVfCsarArtifacts(resource,
                csarInfo, createdArtifacts, artifactOperation, true, true));
    }

    @Test
    void testHandleVfCsarArtifactsGetToscaElement() {
        Resource resource = createParseResourceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(Constants.VENDOR_LICENSE_MODEL);
        artifactDefinition.setUniqueId("uniqueId");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        resource.setDeploymentArtifacts(deploymentArtifacts);
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        String csarKey = CsarUtils.ARTIFACTS_PATH + "HEAT.meta";
        byte[] artifactsMetaBytes = "src/test/resources/normativeTypes/valid_vf.csar".getBytes();
        csar.put(csarKey, artifactsMetaBytes);
        csarInfo.setCsar(csar);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(resource));
        when(csarArtifactsAndGroupsBusinessLogic
            .createResourceArtifactsFromCsar(any(CsarInfo.class), any(Resource.class), anyString(), anyString(),
                anyList())).thenReturn(Either.left(resource));
        Assertions.assertNotNull(
            sIBL.handleVfCsarArtifacts(resource,
                csarInfo, createdArtifacts, artifactOperation, true, true));
    }

    @Test
    void testCreateOrUpdateSingleNonMetaArtifactToComstants() {
        Resource resource = createParseResourceObject(false);
        CsarInfo csarInfo = getCsarInfo();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        resource.setDeploymentArtifacts(deploymentArtifacts);
        Assertions.assertNotNull(resource);
        Assertions.assertNotNull(csarInfo);
        sIBL.createOrUpdateSingleNonMetaArtifactToComstants(resource, csarInfo, artifactOperation, true, true);

    }

    @Test
    void testCreateOrUpdateNonMetaArtifacts() {
        CsarInfo csarInfo = getCsarInfo();
        Resource resource = createParseResourceObject(false);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);

        Either<Resource, ResponseFormat> result = sIBL.createOrUpdateNonMetaArtifacts(csarInfo, resource,
            createdArtifacts, true, true, artifactOperation);
        assertEquals(result.left().value(), resource);
    }

    @Test
    void testFindVfCsarArtifactsToHandle() {
        Resource resource = createParseResourceObject(false);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put("artifacts", artifactDefinition);
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
            ResponseFormat> result = sIBL.findVfCsarArtifactsToHandle(resource, artifactPathAndNameList, user);
        assertNotNull(result.left().value());
    }

    @Test
    void testOrganizeVfCsarArtifactsByArtifactOperation() {
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
            enumMapResponseFormatEither = sIBL.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList,
            existingArtifactsToHandle,
            resource, user);
        assertNotNull(enumMapResponseFormatEither.left().value());
    }

    @Test
    void testOrganizeVfCsarArtifactsByArtifactOperationElse() {
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.AAI_VF_MODEL.name());
        artifactDefinition.setArtifactChecksum("artifactChecksum");
        existingArtifactsToHandle.add(artifactDefinition);
        Resource resource = createParseResourceObject(false);
        Assertions.assertNotNull(
            sIBL.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifactsToHandle, resource,
                user));
    }

    @Test
    void testProcessCsarArtifacts() {
        CsarInfo csarInfo = getCsarInfo();
        Resource resource = createParseResourceObject(false);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Either<Resource, ResponseFormat> resStatus = Either.left(resource);
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = new
            EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        vfCsarArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, artifactPathAndNameList);
        Assertions.assertNotNull(
            sIBL.processCsarArtifacts(csarInfo,
                resource, createdArtifacts, true, true, resStatus, vfCsarArtifactsToHandle));
    }

    @Test
    void testCreateOrUpdateSingleNonMetaArtifact() {
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
        csar.put("valid_vf.csar", data);
        String artifactPath = "valid_vf.csar", artifactFileName = "", artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String artifactLabel = "", artifactDisplayName = "", artifactDescription = "", artifactId = "artifactId";
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        Either<ArtifactDefinition, Operation> artifactDefinitionOperationEither = Either.left(artifactDefinition);
        when(csarArtifactsAndGroupsBusinessLogic.createOrUpdateCsarArtifactFromJson(any(Resource.class), any(User.class),
            any(Map.class), any(ArtifactOperationInfo.class))).thenReturn(Either.left(artifactDefinitionOperationEither));
        when(artifactsBusinessLogic.handleDelete(anyString(), anyString(), any(User.class), any(Component.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(artifactDefinition));
        Assertions.assertNotNull(
            sIBL.createOrUpdateSingleNonMetaArtifact(resource, csarInfo, artifactPath,
                artifactFileName, artifactType, artifactGroupType, artifactLabel,
                artifactDisplayName, artifactDescription, artifactId, artifactOperation,
                createdArtifacts, true, true, true));
    }

    @Test
    void testCreateOrUpdateServiceArtifacts() throws IOException {
        ArtifactsBusinessLogic.ArtifactOperationEnum operation = ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        String csarKey = CsarUtils.ARTIFACTS_PATH + "HEAT.meta";
        byte[] artifactsMetaBytes = "src/test/resources/normativeTypes/valid_vf.csar".getBytes();
        csar.put(csarKey, artifactsMetaBytes);
        csarInfo.setCsar(csar);
        Service preparedService = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        preparedService.setDeploymentArtifacts(deploymentArtifacts);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToHandle);

        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(createServiceObject(true)));
        when(csarArtifactsAndGroupsBusinessLogic.updateResourceArtifactsFromCsar(any(CsarInfo.class), any(Service.class),
            anyString(), anyString(), anyList(), anyBoolean(), anyBoolean())).thenReturn(Either.left(preparedService));
        Assertions.assertNotNull(
            sIBL.createOrUpdateArtifacts(operation, createdArtifacts, yamlFileName, csarInfo,
                preparedService, nodeTypeInfoToUpdateArtifacts, true, true));
    }

    @Test
    void testHandleVfCsarServiceArtifacts() {
        Service service = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(Constants.VENDOR_LICENSE_MODEL);
        artifactDefinition.setUniqueId("uniqueId");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        service.setDeploymentArtifacts(deploymentArtifacts);
        CsarInfo csarInfo = getCsarInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(service));
        when(csarArtifactsAndGroupsBusinessLogic.deleteVFModules(any(Service.class), any(CsarInfo.class), anyBoolean(), anyBoolean())).thenReturn(
            Either.left(service));
        Assertions.assertNotNull(
            sIBL.handleVfCsarArtifacts(service, csarInfo, createdArtifacts, artifactOperation, true, true));
    }

    @Test
    void testHandleVfCsarServiceArtifactsGetToscaElement() throws IOException {
        Service service = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName(Constants.VENDOR_LICENSE_MODEL);
        artifactDefinition.setUniqueId("uniqueId");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        service.setDeploymentArtifacts(deploymentArtifacts);
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        String csarKey = CsarUtils.ARTIFACTS_PATH + "HEAT.meta";
        byte[] artifactsMetaBytes = "src/test/resources/normativeTypes/valid_vf.csar".getBytes();
        csar.put(csarKey, artifactsMetaBytes);
        csarInfo.setCsar(csar);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(service));
        when(csarArtifactsAndGroupsBusinessLogic.createResourceArtifactsFromCsar(any(CsarInfo.class), any(Service.class),
            anyString(), anyString(), anyList())).thenReturn(Either.left(service));
        Assertions.assertNotNull(
            sIBL.handleVfCsarArtifacts(service,
                csarInfo, createdArtifacts, artifactOperation, true, true));
    }

    @Test
    void testCreateOrUpdateNonMetaServiceArtifacts() {
        CsarInfo csarInfo = getCsarInfo();
        Service service = createServiceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);

        Either<Service, ResponseFormat> result = sIBL.createOrUpdateNonMetaArtifacts(csarInfo,
            service, createdArtifacts, true, true, artifactOperation);
        assertEquals(result.left().value(), service);
    }

    @Test
    void testFindServiceCsarArtifactsToHandle() {
        Service service = createServiceObject(true);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        deploymentArtifacts.put("deploymentArtifacts", artifactDefinition);
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        artifacts.put("artifacts", artifactDefinition);
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
            ResponseFormat> result = sIBL.findVfCsarArtifactsToHandle(service, artifactPathAndNameList, user);
        assertNotNull(result.left().value());
    }

    @Test
    void testOrganizeVfCsarArtifactsByServiceArtifactOperation() {
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
            enumMapResponseFormatEither = sIBL.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList,
            existingArtifactsToHandle, service, user);
        assertNotNull(enumMapResponseFormatEither.left().value());
    }

    @Test
    void testOrganizeVfCsarArtifactsByServiceArtifactOperationElse() {
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = new ArrayList<>();
        artifactPathAndNameList.add(getNonMetaArtifactInfo());
        List<ArtifactDefinition> existingArtifactsToHandle = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        artifactDefinition.setArtifactType(ArtifactTypeEnum.AAI_VF_MODEL.name());
        artifactDefinition.setArtifactChecksum("artifactChecksum");
        existingArtifactsToHandle.add(artifactDefinition);
        Service service = createServiceObject(true);
        Assertions.assertNotNull(
            sIBL.organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifactsToHandle, service, user));

    }

    @Test
    void testProcessServiceCsarArtifacts() {
        CsarInfo csarInfo = getCsarInfo();
        Service service = createServiceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Either<Service, ResponseFormat> resStatus = Either.left(service);
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = new
            EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        List<CsarUtils.NonMetaArtifactInfo> objects = new ArrayList<>();
        objects.add(getNonMetaArtifactInfo());
        vfCsarArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, objects);
        Assertions.assertNotNull(
            sIBL.processCsarArtifacts(csarInfo, service, createdArtifacts, true, true, resStatus, vfCsarArtifactsToHandle));
    }

    @Test
    void testGetValidArtifactNames() {
        CsarInfo csarInfo = getCsarInfo();
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
        Either<List<CsarUtils.NonMetaArtifactInfo>, String> result = sIBL.getValidArtifactNames(csarInfo,
            collectedWarningMessages);
        assertNotNull(result.left().value());
    }

    @Test
    void testCreateOrUpdateSingleNonMetaServiceArtifact() {
        Service service = createServiceObject(true);
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
        csar.put("valid_vf.csar", data);
        csarInfo.setCsar(csar);
        String artifactPath = "valid_vf.csar", artifactFileName = "", artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String artifactLabel = "", artifactDisplayName = "", artifactDescription = "", artifactId = "artifactId";
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        Either<ArtifactDefinition, Operation> artifactDefinitionOperationEither = Either.left(artifactDefinition);
        when(csarArtifactsAndGroupsBusinessLogic.createOrUpdateCsarArtifactFromJson(any(Service.class), any(User.class),
            anyMap(), any(ArtifactOperationInfo.class))).thenReturn(Either.left(artifactDefinitionOperationEither));
        when(artifactsBusinessLogic.handleDelete(anyString(), anyString(), any(User.class), any(Component.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(artifactDefinition));
        Assertions.assertNotNull(
            sIBL.createOrUpdateSingleNonMetaArtifact(service, csarInfo, artifactPath, artifactFileName,
                artifactType, artifactGroupType, artifactLabel, artifactDisplayName,
                artifactDescription, artifactId, artifactOperation, createdArtifacts,
                true, true, true));
    }

    @Test
    void testCreateOrUpdateSingleNonMetaServiceArtifactNull() {
        Service service = createServiceObject(true);
        CsarInfo csarInfo = getCsarInfo();
        String artifactPath = "valid_vf.csar", artifactFileName = "", artifactType = "";
        ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.TOSCA;
        String artifactLabel = "", artifactDisplayName = "", artifactDescription = "", artifactId = "artifactId";
        ArtifactOperationInfo artifactOperation = new ArtifactOperationInfo(true, true, ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        when(artifactsBusinessLogic.handleDelete(anyString(), anyString(), any(User.class), any(Component.class), anyBoolean(), anyBoolean()))
            .thenReturn(Either.left(artifactDefinition));
        Assertions.assertNotNull(
            sIBL.createOrUpdateSingleNonMetaArtifact(service, csarInfo, artifactPath, artifactFileName,
                artifactType, artifactGroupType, artifactLabel, artifactDisplayName,
                artifactDescription, artifactId, artifactOperation, createdArtifacts,
                true, true, true));
    }

    @Test
    void testCreateGroupsOnResource() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        Assertions.assertNotNull(sIBL.createGroupsOnResource(service, groups));
    }

    @Test
    void testCreateGroupsOnResourceNull() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        Assertions.assertNotNull(
            sIBL.createGroupsOnResource(service, groups));
    }

    @Test
    void testUpdateGroupsMembersUsingResource() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        Assertions.assertNotNull(
            sIBL.updateGroupsMembersUsingResource(groups, service));
    }

    @Test
    void testUpdateGroupsMembersUsingResource_left() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        Assertions.assertNotNull(
            sIBL.updateGroupsMembersUsingResource(groups, service));
    }

    @Test
    void testCreateRIAndRelationsFromResourceYaml() throws IOException {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = new HashMap<>();
        String topologyTemplateYaml = getMainTemplateContent();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();

        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);

        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createRIAndRelationsFromYaml(yamlName, resource, uploadComponentInstanceInfoMap,
                topologyTemplateYaml, nodeTypesNewCreatedArtifacts, nodeTypesInfo,
                csarInfo, nodeTypesArtifactsToCreate, nodeName));
    }

    @Test
    void testCreateResourceInstancesRelations() {
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        resource.setComponentInstances(creatComponentInstances());
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        uploadResInstancesMap.put("uploadResInstancesMap", getUploadComponentInstanceInfo());
        when(serviceImportParseLogic.getResourceAfterCreateRelations(any(Resource.class))).thenReturn(resource);
        when(toscaOperationFacade.getToscaFullElement(anyString())).thenReturn(Either.left(resource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createResourceInstancesRelations(user, yamlName, resource, uploadResInstancesMap));
    }

    @Test
    void testCreateResourceInstancesRelations_Empty() {
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        resource.setComponentInstances(creatComponentInstances());
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        uploadResInstancesMap.put("uploadResInstancesMap", getUploadComponentInstanceInfo());
        when(serviceImportParseLogic.getResourceAfterCreateRelations(any(Resource.class))).thenReturn(resource);
        when(toscaOperationFacade.getToscaFullElement(anyString())).thenReturn(Either.left(resource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createResourceInstancesRelations(user, yamlName, resource, uploadResInstancesMap));
    }

    @Test
    void testProcessComponentInstance1() {
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        originResource.setResourceType(ResourceTypeEnum.VF);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        Map<String, DataTypeDefinition> dataTypeDefinitionMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("dataTypeDefinitionName");
        dataTypeDefinitionMap.put("dataTypeDefinitionMap", dataTypeDefinition);
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(dataTypeDefinitionMap);
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        originCompMap.put("componentUid", originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");
        Assertions.assertNotNull(resource);
        Assertions.assertNotNull(yamlName);
        sIBL.processComponentInstance(yamlName, resource, componentInstancesList, allDataTypes.left().value(), instProperties,
            instCapabilties, instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes,
            originCompMap, instInputs, instNodeFilter, uploadComponentInstanceInfo);
    }

    @Test
    void testProcessComponentInstance_null() {
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
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        originCompMap.put("componentUid", originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb0");

        Assertions.assertThrows(ComponentException.class, () -> sIBL.processComponentInstance(yamlName,
            resource, componentInstancesList, null, instProperties, instCapabilties,
            instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap,
            instInputs, instNodeFilter, uploadComponentInstanceInfo));
    }

    @Test
    void testAddInputsValuesToRi() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        List<UploadPropInfo> uploadPropInfoList = getPropertyList();
        properties.put("propertiesMap", uploadPropInfoList);
        uploadComponentInstanceInfo.setProperties(properties);
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setType("inputDefinitionType");
        inputs.add(inputDefinition);
        originResource.setInputs(inputs);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("dataTypeDefinitionName");
        allDataTypes.put("dataTypeDefinitionMap", dataTypeDefinition);

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .addInputsValuesToRi(uploadComponentInstanceInfo, resource, originResource,
                currentCompInstance, instInputs, allDataTypes));
    }

    @Test
    void testProcessProperty() {
        Resource resource = createParseResourceObject(true);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setType("inputDefinitionType");
        inputs.add(inputDefinition);
        resource.setInputs(inputs);
        ComponentInstance currentCompInstance = null;
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        currPropertiesMap.put("propertyInfoName", inputDefinition);
        List<ComponentInstanceInput> instPropList = new ArrayList<>();
        List<UploadPropInfo> propertyList = getPropertyList();
        Assertions.assertNotNull(resource);
        Assertions.assertNotNull(currPropertiesMap);
        sIBL.processProperty(resource, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
    }

    @Test
    void testHandleSubstitutionMappings() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        when(toscaOperationFacade.getToscaFullElement(anyString()))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.handleSubstitutionMappings(resource, uploadResInstancesMap));
    }

    @Test
    void testHandleSubstitutionMappings_left() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        when(toscaOperationFacade.getToscaFullElement(anyString()))
            .thenReturn(Either.left(resource));

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.handleSubstitutionMappings(resource, uploadResInstancesMap));
    }

    @Test
    void testCreateResourceInstances() {
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        nodesInfoValue.setName("zxjTestImportServiceAb");
        nodesInfoValue.setRequirements(gerRequirements());
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.createResourceInstances(yamlName, resource, uploadResInstancesMap, nodeNamespaceMap));
    }

    @Test
    void testHandleNodeTypes() throws IOException {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        String topologyTemplateYaml = getMainTemplateContent();
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();
        CsarInfo csarInfo = getCsarInfo();
        Assertions.assertNotNull(resource);

        sIBL.handleNodeTypes(yamlName, resource, topologyTemplateYaml, needLock, nodeTypesArtifactsToHandle,
            nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeName);
    }

    @Test
    void testHandleNestedVfc1() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Resource resource = createParseResourceObject(false);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypeInfo.setTemplateFileName("groups.yml");
        nodeTypeInfo.setMappedToscaTemplate(new HashMap<>());
        nodesInfo.put(nodeName, nodeTypeInfo);
        CsarInfo csarInfo = getCsarInfo();

        Assertions.assertThrows(ComponentException.class, () -> sIBL.handleNestedVfc(resource,
            nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName));
    }

    @Test
    void testHandleComplexVfc1() {
        Resource resource = createParseResourceObject(true);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        when(serviceImportParseLogic.buildValidComplexVfc(any(Resource.class), any(CsarInfo.class), anyString(),
            anyMap())).thenReturn(createParseResourceObject(false));
        when(toscaOperationFacade.getFullLatestComponentByToscaResourceName(anyString()))
            .thenReturn(Either.left(resource));
        when(serviceImportParseLogic.validateNestedDerivedFromDuringUpdate(any(Resource.class), any(Resource.class),
            anyBoolean())).thenReturn(Either.left(true));

        Assertions.assertThrows(ComponentException.class, () -> sIBL.handleComplexVfc(resource,
            nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName));
    }

    @Test
    void testCreateNodeTypes1() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
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
        nodeTypesArtifactsToHandle.put("nodeTyp", enumListEnumMap);
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();

        CsarInfo csarInfo = getCsarInfo();
        Map<String, Object> mapToConvert = new HashMap<>();
        Map<String, Object> nodeTypes = new HashMap<>();
        nodeTypes.put(nodeName, "");
        Assertions.assertNotNull(resource);

        sIBL.createNodeTypes(yamlName,
            resource, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, mapToConvert, nodeTypes);
    }

    @Test
    void testCreateNodeTypeResourceFromYaml() throws IOException {
        String yamlName = "group.yml";
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put(nodeName, getGroupsYaml());
        Map.Entry<String, Object> nodeNameValue = nodeMap.entrySet().iterator().next();
        Map<String, Object> mapToConvert = new HashedMap();
        Resource resourceVf = createParseResourceObject(false);
        boolean needLock = true;
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        boolean forceCertificationAllowed = true;
        CsarInfo csarInfo = getCsarInfo();
        boolean isNested = true;
        UploadResourceInfo resourceMetaData = new UploadResourceInfo();
        resourceMetaData.setResourceType("VFC");
        ImmutablePair<Resource, ActionStatus> immutablePair = new ImmutablePair<>(resourceVf, ActionStatus.CREATED);
        when(serviceImportParseLogic.fillResourceMetadata(anyString(), any(Resource.class), anyString(), any(User.class)))
            .thenReturn(resourceMetaData);
        when(serviceImportParseLogic.buildNodeTypeYaml(any(Map.Entry.class), anyMap(), anyString(), any(CsarInfo.class)))
            .thenReturn(nodeName);
        when(serviceBusinessLogic.validateUser(any(User.class), anyString(), any(Component.class), any(AuditingActionEnum.class),
            anyBoolean())).thenReturn(user);
        when(serviceImportParseLogic.createResourceFromNodeType(anyString(), any(UploadResourceInfo.class), any(User.class), anyBoolean(),
            anyBoolean(),
            anyMap(), anyList(), anyBoolean(), any(CsarInfo.class),
            anyString(), anyBoolean())).thenReturn(immutablePair);
        Assertions.assertNotNull(
            sIBL.createNodeTypeResourceFromYaml(yamlName, nodeNameValue, user, mapToConvert,
                resourceVf, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts,
                forceCertificationAllowed, csarInfo, isNested));
    }

    @Test
    void testCreateRIAndRelationsFromYaml() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = new HashMap<>();
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");
        ;
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        Assertions.assertNotNull(sIBL
            .createRIAndRelationsFromYaml(yamlName, service, uploadComponentInstanceInfoMap,
                topologyTemplateYaml, nodeTypesNewCreatedArtifacts, nodeTypesInfo,
                csarInfo, nodeTypesArtifactsToCreate, nodeName));
    }

    @Test
    void testCreateServiceInstancesRelations() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        service.setComponentInstances(creatComponentInstances());
        Resource newResource = createNewResource();
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = getUploadResInstancesMap();
        ComponentParametersView componentParametersView = new ComponentParametersView();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("as");
        capabilityDefinition.setUniqueId("1");
        capabilityDefinition.setOwnerId("2");
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(200);
        when(serviceImportParseLogic.getResourceAfterCreateRelations(any(Resource.class))).thenReturn(newResource);
        when(serviceImportParseLogic.getComponentFilterAfterCreateRelations()).thenReturn(componentParametersView);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class))).thenReturn(Either.left(service));
        when(serviceImportParseLogic.findAvailableRequirement(anyString(),
            anyString(), any(UploadComponentInstanceInfo.class), any(ComponentInstance.class),
            anyString())).thenReturn(Either.left(requirementDefinition));
        when(serviceImportParseLogic.findAvailableCapabilityByTypeOrName(any(RequirementDefinition.class),
            any(ComponentInstance.class), any(UploadReqInfo.class))).thenReturn(capabilityDefinition);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class), anyString())).thenReturn(responseFormat);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(service));
        Assertions.assertNotNull(sIBL.createServiceInstancesRelations(user, yamlName, service, uploadResInstancesMap));
    }

    @Test
    void testCreateServiceInstancesRelations_Empty() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        service.setComponentInstances(creatComponentInstances());
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.createServiceInstancesRelations(user, yamlName, service, uploadResInstancesMap));
    }

    @Test
    void testProcessComponentInstance() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Resource originResource = createParseResourceObject(false);
        originResource.setResourceType(ResourceTypeEnum.VF);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        Map<String, DataTypeDefinition> dataTypeDefinitionMap = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName("dataTypeDefinitionName");
        dataTypeDefinitionMap.put("dataTypeDefinitionMap", dataTypeDefinition);
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(dataTypeDefinitionMap);
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        originCompMap.put("componentUid", originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");
        Assertions.assertNotNull(service);

        sIBL.processComponentInstance(yamlName, service, componentInstancesList, allDataTypes.left().value(),
            instProperties, instCapabilties, instRequirements, instDeploymentArtifacts,
            instArtifacts, instAttributes, originCompMap, instInputs, instNodeFilter,
            uploadComponentInstanceInfo);
    }

    @Test
    void testProcessComponentInstance_null2() {
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
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        originCompMap.put("componentUid", originResource);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb0");

        Assertions.assertThrows(ComponentException.class, () -> sIBL.processComponentInstance(yamlName,
            service, componentInstancesList, null, instProperties, instCapabilties,
            instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap,
            instInputs, instNodeFilter, uploadComponentInstanceInfo));
    }

    @Test
    void testAddInputsValuesToRi2() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Map<String, List<UploadPropInfo>> properties = new HashMap<>();
        List<UploadPropInfo> uploadPropInfoList = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        uploadPropInfo.setName("uploadPropInfo");
        uploadPropInfoList.add(uploadPropInfo);
        uploadPropInfoList.add(uploadPropInfo);
        properties.put("propertiesMap", uploadPropInfoList);
        uploadComponentInstanceInfo.setProperties(properties);
        Service resource = createServiceObject(true);
        Resource originResource = createParseResourceObject(false);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("uniqueId");
        inputs.add(inputDefinition);
        originResource.setInputs(inputs);
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .addInputsValuesToRi(uploadComponentInstanceInfo, resource, originResource,
                currentCompInstance, instInputs, allDataTypes));
    }

    @Test
    void testProcessProperty2() {
        Service resource = createServiceObject(true);
        List<InputDefinition> inputs = new ArrayList<>();
        ComponentInstance currentCompInstance = null;
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setType("inputDefinitionType");
        inputs.add(inputDefinition);
        currPropertiesMap.put("propertyInfoName", inputDefinition);
        resource.setInputs(inputs);
        List<ComponentInstanceInput> instPropList = new ArrayList<>();
        List<UploadPropInfo> propertyList = new ArrayList<>();
        List<GetInputValueDataDefinition> get_input = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        getInputValueDataDefinition.setInputName("inputDefinitionName");
        get_input.add(getInputValueDataDefinition);
        UploadPropInfo propertyInfo = new UploadPropInfo();
        propertyInfo.setValue("value");
        propertyInfo.setGet_input(get_input);
        propertyInfo.setName("propertyInfoName");
        propertyList.add(propertyInfo);
        Assertions.assertNotNull(resource);

        sIBL.processProperty(resource, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
    }

    @Test
    void testProcessGetInput() {
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        List<InputDefinition> inputs = new ArrayList<>();
        GetInputValueDataDefinition getInputIndex = new GetInputValueDataDefinition();

        Assertions.assertThrows(ComponentException.class, () -> sIBL.processGetInput(getInputValues, inputs, getInputIndex));
    }

    @Test
    void testProcessGetInput_optional() {
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setName("InputName");
        inputs.add(inputDefinition);
        GetInputValueDataDefinition getInputIndex = new GetInputValueDataDefinition();
        getInputIndex.setInputName("InputName");
        Assertions.assertNotNull(inputs);

        sIBL.processGetInput(getInputValues, inputs, getInputIndex);
    }

    @Test
    void testAddPropertyValuesToRi() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setProperties(getUploadPropInfoProperties());
        Resource resource = createParseResourceObject(true);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setType("inputDefinitionType");
        inputs.add(inputDefinition);
        resource.setInputs(inputs);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        ResponseFormat responseFormat = new ResponseFormat();
        when(serviceImportParseLogic.findInputByName(eq(inputs), any(GetInputValueDataDefinition.class)))
            .thenReturn(inputDefinition);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(responseFormat);
        Assertions.assertNotNull(
            sIBL.addPropertyValuesToRi(uploadComponentInstanceInfo, resource, originResource,
                currentCompInstance, instProperties, allDataTypes));
    }

    @Test
    void testAddPropertyValuesToRi_else() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Resource resource = createParseResourceObject(true);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        ResponseFormat responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(responseFormat);
        Assertions.assertNotNull(
            sIBL.addPropertyValuesToRi(uploadComponentInstanceInfo, resource, originResource, currentCompInstance,
                instProperties, allDataTypes));
    }

    @Test
    void testAddPropertyValuesToRi2() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setProperties(getUploadPropInfoProperties());
        Service service = createServiceObject(true);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setType("inputDefinitionType");
        inputs.add(inputDefinition);
        service.setInputs(inputs);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        ResponseFormat responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(responseFormat);
        when(serviceImportParseLogic.findInputByName(anyList(), any(GetInputValueDataDefinition.class)))
            .thenReturn(inputDefinition);
        Assertions.assertNotNull(
            sIBL.addPropertyValuesToRi(uploadComponentInstanceInfo, service, originResource,
                currentCompInstance, instProperties, allDataTypes));
    }

    @Test
    void testAddPropertyValuesToRi2_else() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        Service service = createServiceObject(true);
        Resource originResource = createParseResourceObject(false);
        originResource.setProperties(getProperties());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        ResponseFormat responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(responseFormat);
        Assertions.assertNotNull(
            sIBL.addPropertyValuesToRi(uploadComponentInstanceInfo, service, originResource, currentCompInstance,
                instProperties, allDataTypes));
    }

    @Test
    void testProcessComponentInstanceCapabilities() {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setCapabilities(getCapabilities());
        ComponentInstance currentCompInstance = new ComponentInstance();
        Resource originResource = createParseResourceObject(false);
        Assertions.assertNotNull(originResource);
        sIBL.processComponentInstanceCapabilities(null, instCapabilties, uploadComponentInstanceInfo, currentCompInstance,
            originResource);
    }

    @Test
    void testProcessComponentInstanceCapabilities_null() {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        ComponentInstance currentCompInstance = new ComponentInstance();
        Resource originResource = createParseResourceObject(false);
        Assertions.assertNotNull(originResource);

        sIBL.processComponentInstanceCapabilities(null, instCapabilties, uploadComponentInstanceInfo, currentCompInstance,
            originResource);
    }

    @Test
    void testUpdateCapabilityPropertiesValues() {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = null;
        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
        Assertions.assertNull(allDataTypes);
        sIBL.updateCapabilityPropertiesValues(null, originCapabilities, newPropertiesMap);
    }

    @Test
    void testUpdatePropertyValues() {
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        Map<String, UploadPropInfo> newProperties = new HashMap<>();
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        Assertions.assertNotNull(allDataTypes);
        sIBL.updatePropertyValues(properties, newProperties, allDataTypes);
    }

    @Test
    void testUpdatePropertyValue() {
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setType("services");
        UploadPropInfo propertyInfo = new UploadPropInfo();
        propertyInfo.setValue("value");
        Map<String, DataTypeDefinition> allDataTypes = new HashMap<>();
        when(serviceBusinessLogic.validatePropValueBeforeCreate(any(IPropertyInputCommon.class), anyString(), anyBoolean(), anyMap())).thenReturn(
            "qw");
        Assertions.assertNotNull(sIBL.updatePropertyValue(property, propertyInfo, allDataTypes));
    }

    @Test
    void testGetOriginResource() {
        String yamlName = "group.yml";
        Map<String, Resource> originCompMap = new HashMap<>();
        ComponentInstance currentCompInstance = new ComponentInstance();
        currentCompInstance.setComponentUid("currentCompInstance");
        when(toscaOperationFacade.getToscaFullElement(anyString()))
            .thenReturn(Either.left(createParseResourceObject(true)));
        Assertions.assertNotNull(sIBL.getOriginResource(yamlName, originCompMap, currentCompInstance));
    }

    @Test
    void testUpdateCalculatedCapReqWithSubstitutionMappings() {
        Resource resource = createParseResourceObject(false);
        resource.setComponentInstances(creatComponentInstances());
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = getUploadResInstancesMap();

        when(toscaOperationFacade.deleteAllCalculatedCapabilitiesRequirements(any())).thenReturn(StorageOperationStatus.OK);
        Assertions.assertNotNull(sIBL.updateCalculatedCapReqWithSubstitutionMappings(resource, uploadResInstancesMap));
    }

    @Test
    void testFillUpdatedInstCapabilitiesRequirements() {
        List<ComponentInstance> componentInstances = creatComponentInstances();
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = getUploadResInstancesMap();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirement = new HashMap<>();
        Assertions.assertNotNull(componentInstances);

        sIBL.fillUpdatedInstCapabilitiesRequirements(componentInstances, uploadResInstancesMap,
            updatedInstCapabilities, updatedInstRequirement);
    }

    @Test
    void testFillUpdatedInstCapabilities() {
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties = new HashMap<>();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node", capabilityDefinitionList);
        ComponentInstance instance = new ComponentInstance();
        instance.setCapabilities(capabilities);
        Map<String, String> capabilitiesNamesToUpdate = new HashMap<>();
        capabilitiesNamesToUpdate.put("mme_ipu_vdu.feature", "capabilitiesNamesToUpdate");
        Assertions.assertNotNull(instance);

        sIBL.fillUpdatedInstCapabilities(updatedInstCapabilties, instance, capabilitiesNamesToUpdate);
    }

    @Test
    void testFillUpdatedInstRequirements() {
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new
            HashMap<>();
        ComponentInstance instance = new ComponentInstance();
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node", requirementDefinitionList);
        instance.setRequirements(requirements);
        Map<String, String> requirementsNamesToUpdate = new HashMap<>();
        requirementsNamesToUpdate.put("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test",
            "requirementsNamesToUpdate");
        Assertions.assertNotNull(instance);

        sIBL.fillUpdatedInstRequirements(updatedInstRequirements, instance, requirementsNamesToUpdate);
    }

    @Test
    void testAddRelationsToRI() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);

        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getUploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName("zxjTestImportServiceAb");
        componentInstancesList.add(componentInstance);
        service.setComponentInstances(componentInstancesList);
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setOwnerId("1");
        requirementDefinition.setUniqueId("2");
        requirementDefinition.setCapability("3");
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("4");
        capabilityDefinition.setUniqueId("5");
        capabilityDefinition.setOwnerId("6");
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(200);
        when(serviceImportParseLogic.findAvailableRequirement(anyString(),
            anyString(), any(UploadComponentInstanceInfo.class), any(ComponentInstance.class),
            anyString())).thenReturn(Either.left(requirementDefinition));
        when(serviceImportParseLogic.findAvailableCapabilityByTypeOrName(any(RequirementDefinition.class),
            any(ComponentInstance.class), any(UploadReqInfo.class))).thenReturn(capabilityDefinition);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class), anyString())).thenReturn(responseFormat);
        Assertions.assertNotNull(service);

        sIBL.addRelationsToRI(yamlName, service, uploadResInstancesMap, componentInstancesList, relations);
    }

    @Test
    void testAddRelationsToRI_null() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getUploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        Assertions.assertThrows(ComponentException.class, () -> sIBL.addRelationsToRI(yamlName,
            service, uploadResInstancesMap, componentInstancesList, relations));
    }

    @Test
    void testAddRelationToRI() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        service.setComponentInstances(creatComponentInstances());

        UploadComponentInstanceInfo nodesInfoValue = getUploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("capabilityDefinitionName");
        capabilityDefinition.setUniqueId("capabilityDefinitionUniqueId");
        capabilityDefinition.setOwnerId("capabilityDefinitionOwnerId");
        ResponseFormat responseFormat = new ResponseFormat();
        when(serviceImportParseLogic.findAvailableRequirement(anyString(), anyString(), any(UploadComponentInstanceInfo.class),
            any(ComponentInstance.class), anyString())).thenReturn(Either.left(requirementDefinition));
        when(serviceImportParseLogic.findAvailableCapabilityByTypeOrName(any(RequirementDefinition.class),
            any(ComponentInstance.class), any(UploadReqInfo.class))).thenReturn(capabilityDefinition);
        when(componentsUtils.getResponseFormat(any(ActionStatus.class), anyString())).thenReturn(responseFormat);
        Assertions.assertNotNull(sIBL.addRelationToRI(yamlName, service, nodesInfoValue, relations));
        Assertions.assertNotNull(relations.get(0).getRelationships().get(0).getOperations());
    }

    @Test
    void testAddRelationToRI_null() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        service.setComponentInstances(componentInstancesList);
        ResponseFormat responseFormat = new ResponseFormat();
        UploadComponentInstanceInfo nodesInfoValue = getUploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        when(componentsUtils.getResponseFormat(any(ActionStatus.class), anyString())).thenReturn(responseFormat);
        Assertions.assertNotNull(sIBL.addRelationToRI(yamlName, service, nodesInfoValue, relations));
    }

    @Test
    void testGetResourceAfterCreateRelations() {
        Service service = createServiceObject(true);
        ComponentParametersView componentParametersView = createComponentParametersView();
        when(serviceImportParseLogic.getComponentFilterAfterCreateRelations()).thenReturn(componentParametersView);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
            .thenReturn(Either.left(createServiceObject(true)));
        Assertions.assertNotNull(sIBL.getResourceAfterCreateRelations(service));
    }

    @Test
    void testCreateServiceInstances() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getUploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadResInstancesMap", nodesInfoValue);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        Resource resource = createParseResourceObject(true);
        resource.setToscaResourceName("toscaResourceName");
        nodeNamespaceMap.put("nodeNamespaceMap", resource);

        Assertions.assertThrows(ComponentException.class,
            () -> sIBL.createServiceInstances(yamlName, service, uploadResInstancesMap, nodeNamespaceMap));
    }

    @Test
    void testCreateAndAddResourceInstance() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getUploadComponentInstanceInfo();
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(false);
        Resource originResource = createParseResourceObject(true);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);
        Map<String, Resource> existingnodeTypeMap = new HashMap<>();
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createAndAddResourceInstance(uploadComponentInstanceInfo, yamlName, resource,
                nodeNamespaceMap, existingnodeTypeMap, resourcesInstancesMap));
    }

    @Test
    void testCreateAndAddResourceInstances() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getUploadComponentInstanceInfo();
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        service.setServiceType("services");
        Resource originResource = createParseResourceObject(true);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);
        Map<String, Resource> existingnodeTypeMap = new HashMap<>();
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createAndAddResourceInstance(uploadComponentInstanceInfo, yamlName, service,
                nodeNamespaceMap, existingnodeTypeMap, resourcesInstancesMap));
    }

    @Test
    void testValidateResourceInstanceBeforeCreate() {
        String yamlName = "group.yml";
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getUploadComponentInstanceInfo();
        Resource originResource = createParseResourceObject(true);
        ResourceMetadataDataDefinition componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
        componentMetadataDataDefinition.setState(LifecycleStateEnum.CERTIFIED.name());
        ComponentMetadataDefinition componentMetadataDefinition = new ComponentMetadataDefinition(componentMetadataDataDefinition);
        originResource.setComponentMetadataDefinition(componentMetadataDefinition);
        originResource.setComponentType(ComponentTypeEnum.RESOURCE);
        originResource.setToscaResourceName("toscaResourceName");
        originResource.setResourceType(ResourceTypeEnum.VF);
        originResource.setResourceType(ResourceTypeEnum.VF);
        Map<String, Resource> nodeNamespaceMap = new HashMap<>();
        nodeNamespaceMap.put("resources", originResource);
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(originResource));
        Assertions.assertNotNull(
            sIBL.validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo, nodeNamespaceMap));
    }

    @Test
    void testHandleServiceNodeTypes() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        String topologyTemplateYaml = getMainTemplateContent("service_import_template.yml");
        ;
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        when(toscaOperationFacade.getLatestResourceByToscaResourceName(anyString())).thenReturn(Either.left(createOldResource()));
        Assertions.assertNotNull(service);

        sIBL.handleServiceNodeTypes(yamlName, service, topologyTemplateYaml, needLock, nodeTypesArtifactsToHandle,
            nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, nodeName);
    }

    @Test
    void testValidateResourceNotExisted() {
        String type = "org.openecomp.resource.vf";

        Assertions.assertThrows(ComponentException.class, () -> sIBL.validateResourceNotExisted(type));
    }

    @Test
    void testHandleNestedVF() {
        Service service = createServiceObject(true);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = getNodeTypesInfo();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        Assertions.assertThrows(ComponentException.class, () -> sIBL.handleNestedVF(service,
            nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName));
    }

    @Test
    void testHandleNestedVfc() {
        Service service = createServiceObject(true);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";

        Assertions.assertThrows(ComponentException.class, () -> sIBL.handleNestedVfc(service,
            nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName));
    }

    @Test
    void testHandleComplexVfc() {
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        when(serviceImportParseLogic.buildValidComplexVfc(any(CsarInfo.class), anyString(), anyMap()))
            .thenReturn(createNewResource());
        when(toscaOperationFacade.getFullLatestComponentByToscaResourceName(anyString()))
            .thenReturn(Either.left(createNewResource()));
        when(serviceImportParseLogic.validateNestedDerivedFromDuringUpdate(any(Resource.class), any(Resource.class), anyBoolean()))
            .thenReturn(Either.left(true));

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .handleComplexVfc(nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName));
    }

    @Test
    void testHandleComplexVfcStatus() {
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        when(serviceImportParseLogic.buildValidComplexVfc(any(CsarInfo.class), anyString(), anyMap()))
            .thenReturn(createNewResource());
        when(toscaOperationFacade.getFullLatestComponentByToscaResourceName(anyString()))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .handleComplexVfc(nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName));
    }

    @Test
    void testHandleComplexVfc2() {
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodesInfo = getNodeTypesInfo();
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        CsarInfo csarInfo = getCsarInfo();
        Map<String, byte[]> csar = new HashMap<>();
        csar.put(yamlName, yamlName.getBytes());
        csarInfo.setCsar(csar);
        Resource oldComplexVfc = createParseResourceObject(false);
        Resource newComplexVfc = createParseResourceObject(true);

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .handleComplexVfc(nodeTypesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName, oldComplexVfc, newComplexVfc));
    }

    @Test
    void testUpdateResourceFromYaml() throws IOException {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Resource newResource = createNewResource();
        Resource oldResource = createOldResource();
        AuditingActionEnum actionEnum = AuditingActionEnum.CREATE_RESOURCE;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String yamlFileName = "group.yml";
        String yamlFileContent = getYamlFileContent();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        boolean isNested = true;

        when(csarBusinessLogic.getParsedToscaYamlInfo(anyString(), anyString(), anyMap(), any(CsarInfo.class),
            anyString(), any(Component.class))).thenReturn(getParsedToscaYamlInfo());
        when(serviceImportParseLogic.prepareResourceForUpdate(any(Resource.class), any(Resource.class),
            any(User.class), anyBoolean(), anyBoolean())).thenReturn(oldResource);
        when(serviceImportParseLogic.validateCapabilityTypesCreate(any(User.class), any(ICapabilityTypeOperation.class),
            any(Resource.class), any(AuditingActionEnum.class), anyBoolean())).thenReturn(Either.left(true));
        when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
            .thenReturn(Either.left(newResource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .updateResourceFromYaml(oldResource, newResource, actionEnum, createdArtifacts,
                yamlFileName, yamlFileContent, csarInfo, nodeTypesInfo,
                nodeTypesArtifactsToHandle, nodeName, isNested));
    }

    @Test
    void testCreateResourceFromYaml() throws IOException {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Resource resource = createParseResourceObject(true);
        String topologyTemplateYaml = getMainTemplateContent();
        String yamlName = "group.yml";

        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);

        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        boolean shouldLock = false;
        boolean inTransaction = true;

        when(csarBusinessLogic.getParsedToscaYamlInfo(anyString(), anyString(), anyMap(), any(CsarInfo.class),
            anyString(), any(Component.class))).thenReturn(getParsedToscaYamlInfo());
        when(serviceBusinessLogic.fetchAndSetDerivedFromGenericType(any(Component.class))).thenReturn(resource);
        when(toscaOperationFacade.validateComponentNameExists(anyString(), any(ResourceTypeEnum.class), any(ComponentTypeEnum.class)))
            .thenReturn(Either.left(false));
        when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(Either.left(resource));
        Assertions.assertThrows(ComponentException.class, () -> sIBL.createResourceFromYaml(resource,
            topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo,
            nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName));
    }

    @Test
    void testCreateResourceAndRIsFromYaml() throws IOException {
        String yamlName = "group.yml";
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Resource resource = createParseResourceObject(true);
        resource.setSystemName("SystemName");
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        AuditingActionEnum actionEnum = AuditingActionEnum.CREATE_RESOURCE;
        boolean isNormative = true;
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        String topologyTemplateYaml = getMainTemplateContent();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();

        CsarInfo csarInfo = getCsarInfo();
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate = new HashMap<>();
        boolean shouldLock = false;
        boolean inTransaction = true;
        when(serviceBusinessLogic.fetchAndSetDerivedFromGenericType(any(Resource.class)))
            .thenReturn(resource);

        when(serviceBusinessLogic.lockComponentByName(anyString(), any(), anyString()))
            .thenReturn(Either.left(true));

        when(toscaOperationFacade.validateComponentNameExists(anyString(), any(ResourceTypeEnum.class), any(ComponentTypeEnum.class)))
            .thenReturn(Either.left(false));

        when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(Either.left(resource));

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createResourceAndRIsFromYaml(yamlName, resource, parsedToscaYamlInfo, actionEnum,
                isNormative, createdArtifacts, topologyTemplateYaml, nodeTypesInfo, csarInfo,
                nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName));
    }

    @Test
    void testCreateGroupsOnResource2() {
        Resource resource = createParseResourceObject(false);
        Map<String, GroupDefinition> groups = null;
        List<GroupDefinition> groupDefinitionList = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId("groupDefinitionUniqueId");
        groupDefinition.setName("groupDefinition");
        groupDefinitionList.add(groupDefinition);

        Assertions.assertNotNull(
            sIBL.createGroupsOnResource(resource, groups));
    }

    @Test
    void testCreateGroupsOnResource2_null() {
        Resource resource = createParseResourceObject(false);
        Map<String, GroupDefinition> groups = null;

        Either<Resource, ResponseFormat> result = sIBL.createGroupsOnResource(resource, groups);
        assertEquals(result.left().value(), resource);
    }

    @Test
    void testUpdateGroupsMembersUsingResource2() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        Assertions.assertNotNull(
            sIBL.updateGroupsMembersUsingResource(groups, resource));
    }

    @Test
    void testUpdateGroupsMembersUsingResource_left2() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        Assertions.assertNotNull(
            sIBL.updateGroupsMembersUsingResource(groups, resource));
    }

    @Test
    void testUpdateGroupMembers() throws IOException {
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition updatedGroupDefinition = new GroupDefinition();
        Resource component = createParseResourceObject(true);
        List<ComponentInstance> componentInstances = creatComponentInstances();
        String groupName = "tosca_simple_yaml_1_1";
        Map<String, String> members = new HashMap<>();
        members.put("zxjTestImportServiceAb", getGroupsYaml());
        Assertions.assertNotNull(component);

        sIBL.updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
    }

    @Test
    void testUpdateGroupMembers_null() throws IOException {
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition updatedGroupDefinition = new GroupDefinition();
        Resource component = createParseResourceObject(true);
        List<ComponentInstance> componentInstances = new ArrayList<>();
        String groupName = "tosca_simple_yaml_1_1";
        Map<String, String> members = new HashMap<>();
        members.put("zxjTestImportServiceAb", getGroupsYaml());

        Assertions.assertThrows(ComponentException.class, () -> sIBL.updateGroupMembers(groups,
            updatedGroupDefinition, component, componentInstances, groupName, members));
    }

    @Test
    void setCreateResourceTransaction() {
        Resource resource = createParseResourceObject(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        boolean isNormative = true;
        when(toscaOperationFacade.validateComponentNameExists(anyString(), any(ResourceTypeEnum.class), any(ComponentTypeEnum.class)))
            .thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        Assertions.assertThrows(ComponentException.class, () -> sIBL.createResourceTransaction(resource, user, isNormative));
    }

    @Test
    void setCreateResourceTransaction_leftTrue() {
        Resource resource = createParseResourceObject(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        boolean isNormative = true;
        when(toscaOperationFacade.validateComponentNameExists(anyString(), any(), any())).thenReturn(Either.left(true));

        Assertions.assertThrows(ComponentException.class, () -> sIBL.createResourceTransaction(resource, user, isNormative));
    }

    @Test
    void setCreateResourceTransaction_Left() {
        Resource resource = createParseResourceObject(false);
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        when(toscaOperationFacade.validateComponentNameExists(anyString(), any(), any())).thenReturn(Either.left(false));
        when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(Either.left(resource));
        Assertions.assertNotNull(sIBL.createResourceTransaction(resource, user, false));
    }

    @Test
    void testUpdateExistingResourceByImport() {
        Resource newResource = createNewResource();
        Resource oldResource = createOldResource();
        when(serviceImportParseLogic.prepareResourceForUpdate(any(Resource.class), any(Resource.class),
            any(User.class), anyBoolean(), anyBoolean())).thenReturn(oldResource);
        when(serviceImportParseLogic.validateCapabilityTypesCreate(any(User.class), any(ICapabilityTypeOperation.class),
            any(Resource.class), any(AuditingActionEnum.class), anyBoolean())).thenReturn(Either.left(true));
        when(toscaOperationFacade.overrideComponent(any(Resource.class), any(Resource.class)))
            .thenReturn(Either.left(newResource));
        Assertions.assertNotNull(sIBL.updateExistingResourceByImport(newResource, oldResource, user, true, false, true));
    }

    @Test
    void testCreateNewResourceToOldResource() {
        Resource newResource = createNewResource();
        Resource oldResource = createOldResource();

        sIBL.createNewResourceToOldResource(newResource, oldResource, user);
        assertEquals(newResource.getSystemName(), oldResource.getSystemName());
    }

    @Test
    void testCreateResourcesFromYamlNodeTypesList() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        boolean needLock = true;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        CsarInfo csarInfo = getCsarInfo();

        Assertions.assertThrows(ComponentException.class, () -> sIBL
            .createResourcesFromYamlNodeTypesList(yamlName, service, mappedToscaTemplate, needLock,
                nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo));
    }

    @Test
    void testCreateNodeTypes() {
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
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
        nodeTypesArtifactsToHandle.put("nodeTyp", enumListEnumMap);
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        CsarInfo csarInfo = getCsarInfo();
        Map<String, Object> mapToConvert = new HashMap<>();
        Map<String, Object> nodeTypes = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypesInfo.put("nodeTyp", nodeTypeInfo);
        nodeTypes.put("org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test",
            nodeTypeInfo);

        Assertions.assertThrows(ComponentException.class, () -> sIBL.createNodeTypes(yamlName,
            service, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, mapToConvert, nodeTypes));
    }

    @Test
    void testCreateNodeTypesElse() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        String yamlName = "group.yml";
        Service service = createServiceObject(true);
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
        nodeTypesArtifactsToHandle.put("nodeTyp", enumListEnumMap);
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("tosca_definitions_version", "123");
        nodeTypesInfo.get(nodeName).setMappedToscaTemplate(map);
        ParsedToscaYamlInfo parsedToscaYamlInfo = getParsedToscaYamlInfo();

        CsarInfo csarInfo = getCsarInfo();
        Map<String, Object> mapToConvert = new HashMap<>();
        Map<String, Object> nodeTypes = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        nodeTypes.put("org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test",
            nodeTypeInfo);
        when(serviceImportParseLogic.createNodeTypeResourceFromYaml(anyString(), any(Map.Entry.class), any(User.class),
            anyMap(), any(Service.class), anyBoolean(), anyMap(), anyList(), anyBoolean(), any(CsarInfo.class),
            anyBoolean())).thenReturn(getResourceCreated());
        Assertions.assertNotNull(service);

        sIBL.createNodeTypes(yamlName,
            service, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, mapToConvert, nodeTypes);
    }

    protected ImmutablePair<Resource, ActionStatus> getResourceCreated() {
        Resource resource = createOldResource();
        ImmutablePair<Resource, ActionStatus> resourceCreated = new ImmutablePair<>(resource, ActionStatus.OK);

        return resourceCreated;
    }

    protected Resource createNewResource() {
        Resource newResource = createParseResourceObject(false);
        newResource.setVersion("1.0");
        newResource.setInvariantUUID("");
        newResource.setLifecycleState(null);
        newResource.setUUID("");
        newResource.setNormalizedName("");
        newResource.setSystemName("");
        newResource.setCsarUUID("");
        newResource.setImportedToscaChecksum("");
        newResource.setDerivedFromGenericType("");
        newResource.setDerivedFromGenericVersion("");
        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinition");
        toscaArtifacts.put("toscaArtifactsMap", artifactDefinition);
        Map<String, InterfaceDefinition> interfaces = new HashMap<>();
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setOwnerId("OwnerId");
        interfaces.put("interfacesMap", interfaceDefinition);
        newResource.setInterfaces(interfaces);
        newResource.setToscaArtifacts(toscaArtifacts);
        newResource.setProperties(getProperties());
        return newResource;
    }

    protected Resource createOldResource() {
        Resource newResource = createParseResourceObject(false);
        newResource.setVersion("1.0");
        newResource.setUniqueId("ResourceUniqueId");
        newResource.setInvariantUUID("552e8f6c-340c-4fb4-8a82-fe7732fd8010");
        newResource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        newResource.setUUID("13065b80-ca96-4331-b643-d28aeaf961cb");
        newResource.setNormalizedName("NormalizedName");
        newResource.setSystemName("default");
        newResource.setCsarUUID("CsarUUID");
        newResource.setImportedToscaChecksum("ImportedToscaChecksum");
        newResource.setDerivedFromGenericType("DerivedFromGenericType");
        newResource.setDerivedFromGenericVersion("0.1");
        Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("tosca_simple_yaml_1_1");
        toscaArtifacts.put("tosca_definitions_version", artifactDefinition);
        Map<String, InterfaceDefinition> interfaces = new HashMap<>();
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setDescription("Invoked upon receipt of an Instantiate VNF request");
        interfaces.put("tosca_simple_yaml_1_1", interfaceDefinition);
        newResource.setInterfaces(interfaces);
        newResource.setToscaArtifacts(toscaArtifacts);
        List<PropertyDefinition> properties = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("tosca_simple_yaml_1_1");
        properties.add(propertyDefinition);
        newResource.setProperties(properties);
        return newResource;
    }

    protected Map<String, InputDefinition> getCurrPropertiesMap() {
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputDefinition.setUniqueId("uniqueId");
        inputDefinition.setType("inputDefinitionType");
        currPropertiesMap.put("propertyInfoName", inputDefinition);
        return currPropertiesMap;
    }

    protected List<UploadPropInfo> getPropertyList() {
        List<UploadPropInfo> propertyList = new ArrayList<>();
        List<GetInputValueDataDefinition> get_input = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        getInputValueDataDefinition.setInputName("inputDefinitionName");
        get_input.add(getInputValueDataDefinition);
        UploadPropInfo propertyInfo = new UploadPropInfo();
        propertyInfo.setValue("value");
        propertyInfo.setGet_input(get_input);
        propertyInfo.setName("propertyInfoName");
        propertyList.add(propertyInfo);
        return propertyList;
    }

    private Map<String, NodeTypeInfo> getNodeTypesInfo() {
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        nodeTypeInfo.setNested(true);
        nodeTypeInfo.setTemplateFileName("templateFileName");
        nodeTypeInfo.setMappedToscaTemplate(mappedToscaTemplate);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        nodeTypesInfo.put(nodeName, nodeTypeInfo);
        return nodeTypesInfo;
    }

    private Map<String, Object> getNodeTypes() {
        Map<String, Object> nodeTypesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        Map<String, Object> mappedToscaTemplate = new HashMap<>();
        nodeTypeInfo.setNested(true);
        nodeTypeInfo.setTemplateFileName("templateFileName");
        nodeTypeInfo.setMappedToscaTemplate(mappedToscaTemplate);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        nodeTypesInfo.put(nodeName, nodeTypeInfo);
        return nodeTypesInfo;
    }

    private Map<String, UploadComponentInstanceInfo> getUploadResInstancesMap() {
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getUploadComponentInstanceInfo();
        Map<String, String> capabilitiesNamesToUpdate = new HashMap<>();
        capabilitiesNamesToUpdate.put("mme_ipu_vdu.feature", "capabilitiesNamesToUpdate");
        Map<String, String> requirementsNamesToUpdate = new HashMap<>();
        requirementsNamesToUpdate.put("mme_ipu_vdu.feature", "capabilitiesNamesToUpdate");
        uploadResInstancesMap.put("zxjTestImportServiceAb", uploadComponentInstanceInfo);
        return uploadResInstancesMap;
    }

    protected Map<String, List<UploadPropInfo>> getUploadPropInfoProperties() {
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
        properties.put("uploadComponentInstanceInfo", uploadPropInfoList);
        return properties;
    }

    protected List<PropertyDefinition> getProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("propertiesName");
        properties.add(propertyDefinition);
        return properties;
    }

    protected Map<String, List<UploadReqInfo>> gerRequirements() {
        Map<String, List<UploadReqInfo>> uploadReqInfoMap = new HashMap<>();
        String requirementName = "tosca.capabilities.Node";
        List<UploadReqInfo> uploadReqInfoList = new ArrayList<>();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        uploadReqInfo.setCapabilityName("tosca.capabilities.Node");
        uploadReqInfoMap.put(requirementName, uploadReqInfoList);
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
        } catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    protected List<ComponentInstance> creatComponentInstances() {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node", capabilityDefinitionList);

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node", requirementDefinitionList);
        componentInstance.setRequirements(requirements);
        componentInstance.setCapabilities(capabilities);
        componentInstance.setUniqueId("uniqueId");
        componentInstance.setComponentUid("componentUid");
        componentInstance.setName("zxjTestImportServiceAb");
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    private CreateServiceFromYamlParameter getCsfyp() {
        CreateServiceFromYamlParameter csfyp = new CreateServiceFromYamlParameter();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Map<String, NodeTypeInfo> nodeTypesInfo = getNodeTypesInfo();

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

    private ParsedToscaYamlInfo getParsedToscaYamlInfo() {
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        Map<String, InputDefinition> inputs = new HashMap<>();
        Map<String, UploadComponentInstanceInfo> instances = new HashMap<>();
        UploadComponentInstanceInfo uploadComponentInstanceInfo = getUploadComponentInstanceInfo();
        instances.put("instances", uploadComponentInstanceInfo);
        Map<String, GroupDefinition> groups = new HashMap<>();
        Map<String, PolicyDefinition> policies = new HashMap<>();
        parsedToscaYamlInfo.setGroups(groups);
        parsedToscaYamlInfo.setInputs(inputs);
        parsedToscaYamlInfo.setInstances(instances);
        parsedToscaYamlInfo.setPolicies(policies);
        return parsedToscaYamlInfo;
    }

    String getMainTemplateContent(String fileName) {
        String mainTemplateContent = null;
        try {
            mainTemplateContent = loadFileNameToJsonString(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mainTemplateContent;
    }

    protected CsarInfo getCsarInfo() {
        String csarUuid = "0010";
        User user = new User("jh0003");
        Map<String, byte[]> csar = crateCsarFromPayload();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent("service_import_template.yml");
        return new CsarInfo(user, csarUuid, csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
    }

    protected CsarUtils.NonMetaArtifactInfo getNonMetaArtifactInfo() {
        String artifactName = "artifactName", path = "/src/test/resources/valid_vf.csar", artifactType = "AAI_SERVICE_MODEL";
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
        CsarUtils.NonMetaArtifactInfo nonMetaArtifactInfo = new CsarUtils.NonMetaArtifactInfo(artifactName,
            path, artifactType, artifactGroupType, data, artifactUniqueId, isFromCsar);
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
