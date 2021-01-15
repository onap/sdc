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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;

public class ServiceImportParseLogicTest extends ServiceImportBussinessLogicBaseTestSetup {

    ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    ICapabilityTypeOperation capabilityTypeOperation = Mockito.mock(ICapabilityTypeOperation.class);
    IElementOperation elementDao = Mockito.mock(IElementOperation.class);
    IInterfaceLifecycleOperation interfaceTypeOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
    InputsBusinessLogic inputsBusinessLogic = Mockito.mock(InputsBusinessLogic.class);
    LifecycleBusinessLogic lifecycleBusinessLogic = Mockito.mock(LifecycleBusinessLogic.class);

    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
    private static final String GENERIC_ROOT_NAME = "tosca.nodes.Root";
    private static final String GENERIC_VF_NAME = "org.openecomp.resource.abstract.nodes.VF";
    private static final String GENERIC_CR_NAME = "org.openecomp.resource.abstract.nodes.CR";
    private static final String GENERIC_PNF_NAME = "org.openecomp.resource.abstract.nodes.PNF";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";

    @InjectMocks
    private ServiceImportParseLogic serviceImportParseLogic;
    ResponseFormatManager responseManager = null;
    User user = null;

    private ServiceImportParseLogic createTestSubject() {
        return new ServiceImportParseLogic();
    }

    ServiceImportParseLogic bl;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Elements
        mockElementDao = new ElementOperationMock();

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());
        responseManager = ResponseFormatManager.getInstance();

        bl = new ServiceImportParseLogic();
        bl.setComponentsUtils(componentsUtils);
        bl.setToscaOperationFacade(toscaOperationFacade);
        serviceBusinessLogic.setElementDao(elementDao);
        bl.setServiceBusinessLogic(serviceBusinessLogic);
        bl.setCapabilityTypeOperation(capabilityTypeOperation);
        bl.setInterfaceTypeOperation(interfaceTypeOperation);
        bl.setInputsBusinessLogic(inputsBusinessLogic);
        bl.setLifecycleBusinessLogic(lifecycleBusinessLogic);
    }

    @Test
    public void testGetServiceBusinessLogic() {
        ServiceImportParseLogic testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
    }

    @Test
    public void testSetServiceBusinessLogic() {
        ServiceImportParseLogic testSubject;
        ServiceBusinessLogic serviceBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(serviceBusinessLogic);
    }

    @Test
    public void testGetCapabilityTypeOperation() {
        ServiceImportParseLogic testSubject;
        ICapabilityTypeOperation result;

        testSubject = createTestSubject();
        result = testSubject.getCapabilityTypeOperation();
    }

    @Test
    public void testSetCapabilityTypeOperation() {
        ServiceImportParseLogic testSubject;
        ICapabilityTypeOperation iCapabilityTypeOperation = null;

        testSubject = createTestSubject();
        testSubject.setCapabilityTypeOperation(iCapabilityTypeOperation);
    }

    private CsarInfo createCsarInfo() {
        Map<String, byte[]> csar = new HashMap<>();
        User user = new User();
        CsarInfo csarInfo = new CsarInfo(user, "csar_UUID", csar, "vfResourceName", "mainTemplateName",
            "mainTemplateContent", true);
        csarInfo.setVfResourceName("vfResourceName");
        csarInfo.setCsar(csar);
        csarInfo.setCsarUUID("csarUUID");
        csarInfo.setModifier(user);
        csarInfo.setUpdate(true);
        return csarInfo;
    }

    @Test
    public void testFindNodeTypesArtifactsToHandle() {
        ServiceImportParseLogic testSubject = createTestSubject();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();
        final Service service = createServiceObject(false);

        bl.findNodeTypesArtifactsToHandle(
            nodeTypesInfo, getCsarInfo(), service);
    }

    @Test
    public void testBuildNodeTypeYaml() {
        Map.Entry<String, Object> nodeNameValue = new Entry<String, Object>() {
            @Override
            public String getKey() {
                return null;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
        Map<String, Object> mapToConvert = new HashMap<>();
        String nodeResourceType = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX;

        try {
            bl.buildNodeTypeYaml(
                nodeNameValue, mapToConvert, nodeResourceType, getCsarInfo());
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFindAddNodeTypeArtifactsToHandle() {

        Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = new HashMap<>();
        Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        String namespace = "namespace";

        ImmutablePair p1 = ImmutablePair.<String, String>of("s", "sd");
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();
        final Service service = createServiceObject(false);
        Resource resource = new Resource();
        Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(resource);
        when(toscaOperationFacade.getLatestByToscaResourceName(anyString()))
            .thenReturn(getCompLatestResult);
        try {
            bl.findAddNodeTypeArtifactsToHandle(getCsarInfo(), nodeTypesArtifactsToHandle, service,
                extractedVfcsArtifacts, namespace, p1);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFindAddNodeTypeArtifactsToHandleNotNull() {

        Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = new HashMap<>();
        Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        String namespace = "namespace";
        List<ArtifactDefinition> vfcArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinitionName");
        vfcArtifacts.add(artifactDefinition);
        extractedVfcsArtifacts.put(namespace, vfcArtifacts);
        ImmutablePair p1 = ImmutablePair.<String, String>of("s", "sd");
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashedMap();
        final Service service = createServiceObject(false);
        Resource resource = new Resource();
        Either<Component, StorageOperationStatus> getCompLatestResult = Either.left(resource);
        when(toscaOperationFacade.getLatestByToscaResourceName(anyString()))
            .thenReturn(getCompLatestResult);
        try {
            bl.findAddNodeTypeArtifactsToHandle(getCsarInfo(), nodeTypesArtifactsToHandle, service,
                extractedVfcsArtifacts, namespace, p1);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testHandleAndAddExtractedVfcsArtifacts() {
        List<ArtifactDefinition> vfcArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactDefinitionName");
        vfcArtifacts.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToAdd = new ArrayList<>();
        ArtifactDefinition artifactDefinitionToAdd = new ArtifactDefinition();
        artifactDefinitionToAdd.setArtifactName("artifactDefinitionToAddName");
        artifactsToAdd.add(artifactDefinitionToAdd);
        bl.handleAndAddExtractedVfcsArtifacts(vfcArtifacts, artifactsToAdd);
    }

    @Test
    public void testFindNodeTypeArtifactsToHandle() {

        Resource curNodeType = createParseResourceObject(true);
        List<ArtifactDefinition> extractedArtifacts = new ArrayList<>();
        bl.findNodeTypeArtifactsToHandle(curNodeType, extractedArtifacts);
    }

    @Test
    public void testCollectExistingArtifacts() {

        Resource curNodeType = createParseResourceObject(true);
        bl.collectExistingArtifacts(curNodeType);
    }

    @Test
    public void testPutFoundArtifacts() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        List<ArtifactDefinition> artifactsToUpload = new ArrayList<>();
        artifactsToUpload.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
        artifactsToUpdate.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
        artifactsToDelete.add(artifactDefinition);
        bl.putFoundArtifacts(artifactsToUpload, artifactsToUpdate, artifactsToDelete);
    }

    @Test
    public void testProcessExistingNodeTypeArtifacts() {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        List<ArtifactDefinition> extractedArtifacts = new ArrayList<>();
        extractedArtifacts.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToUpload = new ArrayList<>();
        artifactsToUpload.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
        artifactsToUpdate.add(artifactDefinition);
        List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
        artifactsToDelete.add(artifactDefinition);
        Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
        existingArtifacts.put("test", artifactDefinition);
        try {
            bl.processExistingNodeTypeArtifacts(extractedArtifacts, artifactsToUpload, artifactsToUpdate,
                artifactsToDelete, existingArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testProcessNodeTypeArtifact() {
        List<ArtifactDefinition> artifactsToUpload = new ArrayList<>();
        List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
        Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
        ArtifactDefinition existingArtifact = new ArtifactDefinition();
        existingArtifact.setArtifactName("ArtifactName");
        existingArtifact.setArtifactType("ArtifactType");
        existingArtifact.setArtifactChecksum("ArtifactChecksum");
        existingArtifacts.put("existingArtifactMap", existingArtifact);
        ArtifactDefinition currNewArtifact = new ArtifactDefinition();
        currNewArtifact.setArtifactName("ArtifactName");
        currNewArtifact.setArtifactType("ArtifactType");
        currNewArtifact.setPayload("Payload".getBytes());
        bl.processNodeTypeArtifact(artifactsToUpload, artifactsToUpdate, existingArtifacts, currNewArtifact);
    }

    @Test
    public void testUpdateFoundArtifact() {
        List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
        ArtifactDefinition currNewArtifact = new ArtifactDefinition();
        currNewArtifact.setArtifactChecksum("090909");
        currNewArtifact.setPayloadData("data");
        ArtifactDefinition foundArtifact = new ArtifactDefinition();
        foundArtifact.setArtifactChecksum("08767");
        bl.updateFoundArtifact(artifactsToUpdate, currNewArtifact, foundArtifact);
    }

    @Test
    public void testIsArtifactDeletionRequired() {
        String artifactId = "artifactId";
        byte[] artifactFileBytes = new byte[100];
        boolean isFromCsar = true;
        bl.isArtifactDeletionRequired(artifactId, artifactFileBytes, isFromCsar);
    }

    @Test
    public void testFillGroupsFinalFields() {
        List<GroupDefinition> groupsAsList = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("groupDefinitionName");
        groupsAsList.add(groupDefinition);
        bl.fillGroupsFinalFields(groupsAsList);
    }

    @Test
    public void testGetComponentTypeForResponse() {
        Resource resource = createParseResourceObject(true);
        bl.getComponentTypeForResponse(resource);
    }

    @Test
    public void testGetComponentTypeForResponseByService() {
        Service service = createServiceObject(true);
        bl.getComponentTypeForResponse(service);
    }

    @Test
    public void testIsfillGroupMemebersRecursivlyStopCondition() {
        String groupName = "groupName";
        Map<String, GroupDefinition> allGroups = new HashMap<>();
        Set<String> allGroupMembers = new HashSet<>();
        bl.isfillGroupMemebersRecursivlyStopCondition(groupName, allGroups, allGroupMembers);
    }

    @Test
    public void testIsfillGroupMemebersRecursivlyStopCondition2() {
        String groupName = "groupName";
        Map<String, GroupDefinition> allGroups = new HashMap<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        Map<String, String> members = new HashMap<>();
        members.put("members", "members");
        groupDefinition.setMembers(members);
        allGroups.put(groupName, groupDefinition);
        Set<String> allGroupMembers = new HashSet<>();
        bl.isfillGroupMemebersRecursivlyStopCondition(groupName, allGroups, allGroupMembers);
    }

    @Test
    public void testBuildValidComplexVfc() {
        Resource resource = createParseResourceObject(true);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add("derivedFrom");
        nodeTypeInfo.setDerivedFrom(derivedFrom);
        nodesInfo.put(nodeName, nodeTypeInfo);

        try {
            bl.buildValidComplexVfc(resource, getCsarInfo(), nodeName, nodesInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateResourceBeforeCreate() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.getServiceBusinessLogic().setElementDao(elementDao);
            bl.validateResourceBeforeCreate(resource, user, AuditingActionEnum.IMPORT_RESOURCE, false, getCsarInfo());
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testValidateResourceType() {
        Resource resource = createParseResourceObject(true);
        bl.validateResourceType(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateResourceTypeIsEmpty() {
        Resource resource = new Resource();
        resource.setResourceType(null);
        bl.validateResourceType(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateLifecycleTypesCreate() {
        Resource resource = createParseResourceObject(true);
        Map<String, InterfaceDefinition> mapInterfaces = new HashMap<>();
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        String uniqueId = "01932342212";
        interfaceDefinition.setUniqueId(uniqueId);
        mapInterfaces.put("uniqueId", interfaceDefinition);
        resource.setInterfaces(mapInterfaces);
        when(interfaceTypeOperation.getInterface(anyString()))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        bl.validateLifecycleTypesCreate(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateCapabilityTypesCreate() {
        Resource resource = createParseResourceObject(true);
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        String uniqueId = "18982938994";
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put(uniqueId, capabilityDefinitionList);
        resource.setCapabilities(capabilities);
        when(capabilityTypeOperation.getCapabilityType(anyString(), anyBoolean())).
            thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        try {
            bl.validateCapabilityTypesCreate(user, bl.getCapabilityTypeOperation(), resource,
                AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateCapabilityTypesCreateWhenHaveCapability() {
        Resource resource = createParseResourceObject(true);
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        String uniqueId = "18982938994";
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put(uniqueId, capabilityDefinitionList);
        resource.setCapabilities(capabilities);
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
        when(capabilityTypeOperation.getCapabilityType(anyString(), anyBoolean())).
            thenReturn(Either.left(capabilityTypeDefinition));
        try {
            bl.validateCapabilityTypesCreate(user, bl.getCapabilityTypeOperation(), resource,
                AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateCapabilityTypeExists() {
        Resource resource = createParseResourceObject(true);
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        for (Map.Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {

            bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource,
                AuditingActionEnum.IMPORT_RESOURCE,
                eitherResult, typeEntry, false);
        }
    }

    @Test
    public void testValidateCapabilityTypeExistsWhenPropertiesIsNull() {
        Resource resource = createParseResourceObject(true);
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();

        String uniqueId = "0987348532";
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        Map<String, PropertyDefinition> properties = new HashMap<>();
        properties.put(uniqueId, propertyDefinition);
        capabilityTypeDefinition.setProperties(properties);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put(uniqueId, capabilityDefinitionList);
        resource.setCapabilities(capabilities);

        when(capabilityTypeOperation.getCapabilityType(anyString(), anyBoolean())).
            thenReturn(Either.left(capabilityTypeDefinition));
        for (Map.Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {

            bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource,
                AuditingActionEnum.IMPORT_RESOURCE,
                eitherResult, typeEntry, false);
        }
    }

    @Test
    public void testValidateCapabilityTypeExistsWhenPropertiesNotNull() {
        Resource resource = createParseResourceObject(true);
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();

        String uniqueId = "0987348532";
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(uniqueId);
        Map<String, PropertyDefinition> properties = new HashMap<>();
        properties.put(uniqueId, propertyDefinition);
        capabilityTypeDefinition.setProperties(properties);

        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();

        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        List<ComponentInstanceProperty> componentInstancePropertyList = new ArrayList<>();
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setValueUniqueUid(uniqueId);
        componentInstanceProperty.setName(uniqueId);
        componentInstancePropertyList.add(componentInstanceProperty);
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setProperties(componentInstancePropertyList);
        capabilityDefinitionList.add(capabilityDefinition);

        capabilities.put(uniqueId, capabilityDefinitionList);
        resource.setCapabilities(capabilities);

        when(capabilityTypeOperation.getCapabilityType(anyString(), anyBoolean())).
            thenReturn(Either.left(capabilityTypeDefinition));
        for (Map.Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {

            bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource,
                AuditingActionEnum.IMPORT_RESOURCE,
                eitherResult, typeEntry, false);
        }
    }

    @Test
    public void testValidateCapabilityTypeExists2() {
        Resource resource = createParseResourceObject(true);
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        when(capabilityTypeOperation.getCapabilityType(anyString(), anyBoolean())).
            thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        try {
            for (String type : resource.getRequirements().keySet()) {
                bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource,
                    resource.getRequirements().get(type), AuditingActionEnum.IMPORT_RESOURCE, eitherResult, type,
                    false);
            }
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }


    }

    @Test
    public void testValidateResourceFieldsBeforeCreate() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateResourceFieldsBeforeCreate(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateDerivedFromExist() {
        Resource resource = createParseResourceObject(true);
        try {
            when(toscaOperationFacade.validateToscaResourceNameExists(anyString()))
                .thenReturn(Either.left(true));
            bl.validateDerivedFromExist(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateDerivedFromExistFailure1() {
        Resource resource = createParseResourceObject(true);
        try {
            when(toscaOperationFacade.validateToscaResourceNameExists(anyString()))
                .thenReturn(Either.left(false));
            bl.validateDerivedFromExist(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.PARENT_RESOURCE_NOT_FOUND,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateDerivedFromExistFailure2() {
        Resource resource = createParseResourceObject(true);
        try {
            when(toscaOperationFacade.validateToscaResourceNameExists(anyString()))
                .thenReturn(Either.right(StorageOperationStatus.OK));
            bl.validateDerivedFromExist(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateLicenseType() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.validateLicenseType(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_CONTENT,
                ComponentTypeEnum.RESOURCE.getValue());
        }


    }

    @Test
    public void testValidateCost() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateCost(resource);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_CONTENT,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateResourceVendorModelNumber() {
        Resource resource = createParseResourceObject(true);
        bl.validateResourceVendorModelNumber(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateResourceVendorModelNumberWrongLen() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceVendorModelNumber("000000000011122221111222333444443222556677788778889999998776554332340");
        try {
            bl.validateResourceVendorModelNumber(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateResourceVendorModelNumberWrongValue() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceVendorModelNumber("");
        try {
            bl.validateResourceVendorModelNumber(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateVendorReleaseName() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorRelease("0.1");
        bl.validateVendorReleaseName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateVendorReleaseNameFailure() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorRelease("");
        try {
            bl.validateVendorReleaseName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_VENDOR_RELEASE,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testValidateVendorReleaseNameWrongLen() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorRelease("000000000011122221111222333444443222556677788778889999998776554332340");
        try {
            bl.validateVendorReleaseName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateCategory() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateEmptyCategory() {
        Resource resource = createParseResourceObject(true);
        resource.setCategories(null);
        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateCategorySizeBiggerThan1() {
        Resource resource = createParseResourceObject(true);
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition categoryDefinition1 = new CategoryDefinition();
        CategoryDefinition categoryDefinition2 = new CategoryDefinition();
        categories.add(categoryDefinition1);
        categories.add(categoryDefinition2);

        resource.setCategories(categories);
        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateEmptySubCategory() {
        Resource resource = createParseResourceObject(true);
        List<CategoryDefinition> categories = resource.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        categoryDefinition.setSubcategories(null);

        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_SUBCATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateEmptySubCategorySizeBiggerThan1() {
        Resource resource = createParseResourceObject(true);
        List<CategoryDefinition> categories = resource.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
        SubCategoryDefinition subCategoryDefinition1 = new SubCategoryDefinition();
        SubCategoryDefinition subCategoryDefinition2 = new SubCategoryDefinition();
        subcategories.add(subCategoryDefinition1);
        subcategories.add(subCategoryDefinition2);

        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.RESOURCE_TOO_MUCH_SUBCATEGORIES,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateEmptyCategoryName() {
        Resource resource = createParseResourceObject(true);
        List<CategoryDefinition> categories = resource.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        categoryDefinition.setName(null);

        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateEmptySubCategoryName() {
        Resource resource = createParseResourceObject(true);
        List<CategoryDefinition> categories = resource.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
        SubCategoryDefinition subCategoryDefinition1 = subcategories.get(0);
        subCategoryDefinition1.setName(null);

        try {
            bl.validateCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_SUBCATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateCategoryListed() {
        Resource resource = createParseResourceObject(true);
        CategoryDefinition category = resource.getCategories().get(0);
        SubCategoryDefinition subcategory = category.getSubcategories().get(0);
        try {
            bl.validateCategoryListed(category, subcategory, user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFailOnInvalidCategory() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.failOnInvalidCategory(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testValidateVendorName() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateVendorName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateVendorNameEmpty() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorName(null);
        try {
            bl.validateVendorName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_VENDOR_NAME,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateVendorNameWrongLen() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorName("000000000011122221111222333444443222556677788778889999998776554332340");
        try {
            bl.validateVendorName(user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateVendorName2() {
        Resource resource = createParseResourceObject(true);
        CategoryDefinition category = resource.getCategories().get(0);
        SubCategoryDefinition subcategory = category.getSubcategories().get(0);
        String vendorName = "vendorName";
        try {
            bl.validateVendorName(vendorName, user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFillResourceMetadata2() {
        String yamlName = "yamlName";
        Resource resourceVf = createParseResourceObject(true);
        String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "test";
        resourceVf.setSystemName("systemName");
        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFillResourceMetadataWrongStart() {
        String yamlName = "yamlName";
        Resource resourceVf = createParseResourceObject(true);
        String nodeName = "WrongStart" + "test";
        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFillResourceMetadataResourceTypeIsAbs() {
        String yamlName = "yamlName";
        Resource resourceVf = createParseResourceObject(true);
        String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + Constants.ABSTRACT;
        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_NODE_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testGetNodeTypeActualName() {
        String fullName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "test";
        try {
            bl.getNodeTypeActualName(fullName);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAddInput() {
        Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
        InputDefinition prop = new InputDefinition();
        try {
            bl.addInput(currPropertiesMap, prop);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFindAviableRequirement() {
        String uniqueId = "101929382910";
        String regName = uniqueId;
        String yamlName = uniqueId;
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        ComponentInstance currentCompInstance = new ComponentInstance();

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName(uniqueId);
        requirementDefinition.setMaxOccurrences("10");
        requirementDefinition.setLeftOccurrences("3");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put(uniqueId, requirementDefinitionList);
        currentCompInstance.setRequirements(requirements);

        String capName = "capName";
        try {
            bl.findAviableRequiremen(regName, yamlName, uploadComponentInstanceInfo, currentCompInstance, capName);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_NODE_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFindAviableRequirementSameCapName() {
        String uniqueId = "101929382910";
        String regName = uniqueId;
        String yamlName = uniqueId;
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        ComponentInstance currentCompInstance = new ComponentInstance();

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName(uniqueId);
        requirementDefinition.setMaxOccurrences("10");
        requirementDefinition.setLeftOccurrences("3");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put(uniqueId, requirementDefinitionList);
        currentCompInstance.setRequirements(requirements);

        String capName = uniqueId;
        try {
            bl.findAviableRequiremen(regName, yamlName, uploadComponentInstanceInfo, currentCompInstance, capName);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_NODE_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFindAvailableCapabilityByTypeOrName() {
        RequirementDefinition validReq = new RequirementDefinition();
        ComponentInstance currentCapCompInstance = new ComponentInstance();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();

        try {
            bl.findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadReqInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }


    @Test
    public void testFindAvailableCapability() {
        String uniqueId = "23422345677";
        RequirementDefinition validReq = new RequirementDefinition();
        validReq.setCapability(uniqueId);
        ComponentInstance instance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setMaxOccurrences("3");
        capabilityDefinition.setLeftOccurrences("2");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilityMap.put(uniqueId, capabilityDefinitionList);
        instance.setCapabilities(capabilityMap);

        try {
            bl.findAvailableCapability(validReq, instance);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testfindAvailableCapability2() {
        String uniqueId = "23422345677";
        RequirementDefinition validReq = new RequirementDefinition();
        validReq.setCapability(uniqueId);
        ComponentInstance instance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(uniqueId);
        capabilityDefinition.setMaxOccurrences("3");
        capabilityDefinition.setLeftOccurrences("2");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilityMap.put(uniqueId, capabilityDefinitionList);
        instance.setCapabilities(capabilityMap);
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        uploadReqInfo.setCapabilityName(uniqueId);
        try {
            bl.findAvailableCapability(validReq, instance, uploadReqInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testGetComponentWithInstancesFilter() {
        try {
            bl.getComponentWithInstancesFilter();
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testCreateParseResourceObject() {
        String key = "0923928394";
        List<UploadCapInfo> capabilities = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        uploadCapInfo.setType(key);
        capabilities.add(uploadCapInfo);

        String resourceId = "resourceId";
        Map<String, List<CapabilityDefinition>> defaultCapabilities = new HashMap<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(key);
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        capabilityDefinitionList.add(capabilityDefinition);
        defaultCapabilities.put(key, capabilityDefinitionList);

        Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();
        InputDefinition prop = new InputDefinition();

        Resource resource = createParseResourceObject(true);
        Map<String, List<CapabilityDefinition>> capabilitiesMap = resource.getCapabilities();
        capabilitiesMap.put(key, capabilityDefinitionList);

        when(toscaOperationFacade.getToscaFullElement(anyString()))
            .thenReturn(Either.left(resource));

        try {
            bl.addValidComponentInstanceCapabilities(key, capabilities, resourceId, defaultCapabilities,
                validCapabilitiesMap);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testGetCapabilityFailure() {
        String resourceId = "resourceId";
        String key = "0923928394";
        Map<String, List<CapabilityDefinition>> defaultCapabilities = new HashMap<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(key);
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        capabilityDefinitionList.add(capabilityDefinition);
        defaultCapabilities.put(key, capabilityDefinitionList);
        String capabilityType = key;
        when(toscaOperationFacade.getToscaFullElement(anyString()))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        try {
            bl.getCapability(resourceId, defaultCapabilities, capabilityType);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_NOT_FOUND,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }


    @Test
    public void testValidateCapabilityProperties() {
        List<UploadCapInfo> capabilities = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        List<UploadPropInfo> properties = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        properties.add(uploadPropInfo);
        uploadCapInfo.setProperties(properties);
        capabilities.add(uploadCapInfo);
        String resourceId = "resourceId";
        CapabilityDefinition defaultCapability = new CapabilityDefinition();
        defaultCapability.setProperties(null);
        defaultCapability.setName("test");

        try {
            bl.validateCapabilityProperties(capabilities, resourceId, defaultCapability);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateUniquenessUpdateUploadedComponentInstanceCapability() {
        String key = "02124568";
        List<UploadCapInfo> capabilities = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        List<UploadPropInfo> properties = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        uploadPropInfo.setName(key);
        properties.add(uploadPropInfo);
        uploadCapInfo.setProperties(properties);
        capabilities.add(uploadCapInfo);
        String resourceId = "resourceId";
        CapabilityDefinition defaultCapability = new CapabilityDefinition();
        List<ComponentInstanceProperty> componentInstancePropertyList = new ArrayList<>();
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstancePropertyList.add(componentInstanceProperty);
        defaultCapability.setProperties(componentInstancePropertyList);
        defaultCapability.setName(key);

        try {
            bl.validateUniquenessUpdateUploadedComponentInstanceCapability(defaultCapability, uploadCapInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testSetDeploymentArtifactsPlaceHolderByResource() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.setDeploymentArtifactsPlaceHolder(resource, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testSetDeploymentArtifactsPlaceHolderByService() {
        Service Service = createServiceObject(true);

        try {
            bl.setDeploymentArtifactsPlaceHolder(Service, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testProcessDeploymentResourceArtifacts() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceType(ResourceTypeEnum.VF);
        Map<String, ArtifactDefinition> artifactMap = new HashMap<>();
        String k = "key";
        Object v = new Object();
        Map<String, List<String>> artifactDetails = new HashMap<>();
        List<String> artifactTypes = new ArrayList<>();
        artifactTypes.add(ResourceTypeEnum.VF.name());
        artifactDetails.put("validForResourceTypes", artifactTypes);
        v = artifactDetails;

        bl.processDeploymentResourceArtifacts(user, resource, artifactMap, k, v);
    }

    @Test
    public void testMergeOldResourceMetadataWithNew() {
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = new Resource();

        try {
            bl.mergeOldResourceMetadataWithNew(oldResource, newResource);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testBuildComplexVfcMetadata() {
        Resource resource = createParseResourceObject(true);
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add("derivedFrom");
        nodeTypeInfo.setDerivedFrom(derivedFrom);
        nodesInfo.put(nodeName, nodeTypeInfo);

        try {
            bl.buildComplexVfcMetadata(getCsarInfo(), nodeName, nodesInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateResourceCreationFromNodeType() {
        Resource resource = createParseResourceObject(true);
        resource.setDerivedFrom(null);
        try {
            bl.validateResourceCreationFromNodeType(resource, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_DERIVED_FROM_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testCreateInputsOnResource() {
        Resource resource = createParseResourceObject(true);
        Map<String, InputDefinition> inputs = new HashMap<>();

        try {
            bl.createInputsOnResource(resource, inputs);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testCreateInputsOnResourceWhenIsNotEmpty() {
        String key = "12345667";
        Resource resource = createParseResourceObject(true);
        List<InputDefinition> inputDefinitionList = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinitionList.add(inputDefinition);
        resource.setInputs(inputDefinitionList);
        Map<String, InputDefinition> inputs = new HashMap<>();
        inputs.put(key, inputDefinition);

        try {
            bl.createInputsOnResource(resource, inputs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateInputsOnService() {
        Service service = createServiceObject(true);
        List<InputDefinition> resourceProperties = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        service.setInputs(resourceProperties);
        Map<String, InputDefinition> inputs = new HashMap<>();
        InputDefinition inputDefinitionMap = new InputDefinition();
        inputDefinition.setName("inputDefinitionName");
        inputs.put("inputsMap", inputDefinitionMap);

        try {
            bl.createInputsOnService(service, inputs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateServiceTransaction() {
        Service service = createServiceObject(true);

        try {
            bl.createServiceTransaction(service, user, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateArtifactsPlaceHolderData() {
        Service service = createServiceObject(true);

        try {
            bl.createArtifactsPlaceHolderData(service, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetInformationalArtifactsPlaceHolder() {
        Service service = createServiceObject(true);

        try {
            bl.setInformationalArtifactsPlaceHolder(service, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateNestedDerivedFromDuringUpdate() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);
        String key = "2323456";

        List<String> currentDerivedFromList = new ArrayList<>();
        currentDerivedFromList.add(key);
        currentResource.setDerivedFrom(currentDerivedFromList);
        List<String> updatedDerivedFromList = new ArrayList<>();
        updatedDerivedFromList.add("23344567778");
        updateInfoResource.setDerivedFrom(updatedDerivedFromList);

        when(toscaOperationFacade.validateToscaResourceNameExtends(anyString(), anyString()))
            .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        try {
            bl.validateNestedDerivedFromDuringUpdate(currentResource, updateInfoResource, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateDerivedFromExtending() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        when(toscaOperationFacade.validateToscaResourceNameExtends(anyString(), anyString()))
            .thenReturn(Either.left(false));
        try {
            bl.validateDerivedFromExtending(user, currentResource, updateInfoResource,
                AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateResourceFieldsBeforeUpdate() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.validateResourceFieldsBeforeUpdate(currentResource, updateInfoResource, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateResourceName() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);
        currentResource.setName("test1");
        updateInfoResource.setName("test2");

        try {
            bl.validateResourceName(currentResource, updateInfoResource, true, false);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testIsResourceNameEquals() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.isResourceNameEquals(currentResource, updateInfoResource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrepareResourceForUpdate() {
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = createParseResourceObject(true);

        try {
            bl.prepareResourceForUpdate(oldResource, newResource, user, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFailOnChangeState() {
        ResponseFormat response = new ResponseFormat();
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = createParseResourceObject(true);

        try {
            bl.failOnChangeState(response, user, oldResource, newResource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleResourceGenericType() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.handleResourceGenericType(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateOrCreateGroups() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.updateOrCreateGroups(resource, groups);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddGroupsToCreateOrUpdate() {
        List<GroupDefinition> groupsFromResource = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setInvariantName("groupDefinitionName");
        groupsFromResource.add(groupDefinition);
        List<GroupDefinition> groupsAsList = new ArrayList<>();
        GroupDefinition groupNewDefinition = getGroupDefinition();
        groupsAsList.add(groupNewDefinition);
        List<GroupDefinition> groupsToUpdate = new ArrayList<>();
        List<GroupDefinition> groupsToCreate = new ArrayList<>();

        bl.addGroupsToCreateOrUpdate(groupsFromResource, groupsAsList, groupsToUpdate, groupsToCreate);
    }

    @Test
    public void testAddGroupsToDelete() {
        List<GroupDefinition> groupsFromResource = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("groupDefinitionName");
        groupsFromResource.add(groupDefinition);
        List<GroupDefinition> groupsAsList = new ArrayList<>();
        GroupDefinition groupNewDefinition = new GroupDefinition();
        groupNewDefinition.setName("groupNewDefinitionName");
        groupsAsList.add(groupNewDefinition);
        List<GroupDefinition> groupsToDelete = new ArrayList<>();

        bl.addGroupsToDelete(groupsFromResource, groupsAsList, groupsToDelete);
    }

    @Test
    public void testUpdateGroupsMembersUsingResource() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition groupDefinition = getGroupDefinition();
        groupDefinition.setMembers(null);
        groups.put("groupsMap", groupDefinition);

        bl.updateGroupsMembersUsingResource(groups, component);
    }

    @Test
    public void testupdateGroupMembers() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition updatedGroupDefinition = new GroupDefinition();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        String groupName = "groupName";
        Map<String, String> members = new HashMap<>();

        try {
            bl.updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testupdateGroupMembersNotNull() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = getGroups();
        GroupDefinition updatedGroupDefinition = new GroupDefinition();
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName("componentInstanceName");
        componentInstance.setUniqueId("componentInstanceUniqueId");
        componentInstances.add(componentInstance);
        String groupName = "groupName";
        Map<String, String> members = new HashMap<>();
        members.put("members", "members");
        members.put("componentInstanceName", "members");

        try {
            bl.updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateCyclicGroupsDependencies() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        String key = "098738485";
        GroupDefinition groupDefinition = new GroupDefinition();
        groups.put(key, groupDefinition);

        try {
            bl.validateCyclicGroupsDependencies(groups);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFillAllGroupMemebersRecursivly() {
        Map<String, GroupDefinition> allGroups = new HashMap<>();
        Set<String> allGroupMembers = new HashSet<>();
        String groupName = "groupName";

        bl.fillAllGroupMemebersRecursivly(groupName, allGroups, allGroupMembers);
    }

    @Test
    public void testFillAllGroupMemebersRecursivlyAllGroups() {
        String groupName = "groupName";
        Map<String, GroupDefinition> allGroups = new HashMap<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        Map<String, String> members = new HashMap<>();
        members.put("members", "members");
        groupDefinition.setMembers(members);
        allGroups.put(groupName, groupDefinition);
        allGroups.put("members", groupDefinition);
        Set<String> allGroupMembers = new HashSet<>();
        allGroupMembers.add("allGroupMembers");

        bl.fillAllGroupMemebersRecursivly(groupName, allGroups, allGroupMembers);
    }

    @Test
    public void testFillResourceMetadataForServiceFailure() {
        String yamlName = "yamlName";
        Service resourceVf = createServiceObject(true);
        String nodeName = "nodeName";

        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFillResourceMetadataForServiceWrongType() {
        String yamlName = "yamlName";
        Service resourceVf = createServiceObject(true);
        String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "nodeName";

        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFillResourceMetadataForServiceSuccess() {
        String yamlName = "yamlName";
        Service resourceVf = createServiceObject(true);
        String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "VFC";

        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_NODE_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testpropagateStateToCertified() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();

        try {
            bl.propagateStateToCertified(user, resource, lifecycleChangeInfo, true, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testpropagateStateToCertifiedIsTrue() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction();
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        try {
            bl.propagateStateToCertified(user, resource, lifecycleChangeInfo, true, true, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testBuildValidComplexVfc2() {
        String nodeName = "org.openecomp.resource.derivedFrom.zxjTestImportServiceAb.test";
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();
        NodeTypeInfo nodeTypeInfo = new NodeTypeInfo();
        List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add("derivedFrom");
        nodeTypeInfo.setDerivedFrom(derivedFrom);
        nodesInfo.put(nodeName, nodeTypeInfo);

        try {
            bl.buildValidComplexVfc(getCsarInfo(), nodeName, nodesInfo);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testUpdateGroupsOnResourceEmptyGroups() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.updateGroupsOnResource(resource, groups);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testSetInformationalArtifactsPlaceHolder2() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.setInformationalArtifactsPlaceHolder(resource, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRollback() {
        Resource resource = createParseResourceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();

        try {
            bl.rollback(false, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRollback_NotNull() {
        Resource resource = createParseResourceObject(true);
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactName("artifactName");
        createdArtifacts.add(artifactDefinition);
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        ArtifactDefinition artifactDefinition2 = new ArtifactDefinition();
        artifactDefinition2.setArtifactChecksum("artifactChecksum");
        nodeTypesNewCreatedArtifacts.add(artifactDefinition2);

        bl.rollback(true, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
    }

    @Test
    public void testCreateArtifactsPlaceHolderData2() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.createArtifactsPlaceHolderData(resource, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleGroupsProperties() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        bl.handleGroupsProperties(service, groups);
    }

    @Test
    public void testHandleGroupsProperties2() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = getGroups();

        bl.handleGroupsProperties(resource, groups);
    }

    @Test
    public void testHandleGetInputs() {
        PropertyDataDefinition property = new PropertyDataDefinition();
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        GetInputValueDataDefinition getInput = new GetInputValueDataDefinition();
        getInput.setInputId("inputId");
        getInput.setInputName("inputName");
        getInputValueDataDefinition.setInputName("inputName");
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        getInputValueDataDefinition.setGetInputIndex(getInputValueDataDefinition);
        getInputValues.add(getInputValueDataDefinition);
        property.setGetInputValues(getInputValues);
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputName");
        inputDefinition.setUniqueId("abc12345");
        inputs.add(inputDefinition);

        bl.handleGetInputs(property, inputs);
    }

    @Test
    public void testHandleGetInputs_null() {
        PropertyDataDefinition property = new PropertyDataDefinition();
        List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputName("inputName");
        getInputValueDataDefinition.setPropName("getInputValueDataDefinitionName");
        getInputValues.add(getInputValueDataDefinition);
        property.setGetInputValues(getInputValues);
        List<InputDefinition> inputs = new ArrayList<>();
        try {
            bl.handleGetInputs(property, inputs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindInputByName() {
        GetInputValueDataDefinition getInput = new GetInputValueDataDefinition();
        getInput.setInputId("inputId");
        getInput.setInputName("inputName");
        List<InputDefinition> inputs = new ArrayList<>();
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("inputName");
        inputDefinition.setUniqueId("abc12345");
        inputs.add(inputDefinition);

        bl.findInputByName(inputs, getInput);
    }

    @Test
    public void testAssociateComponentInstancePropertiesToComponent() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        try {
            bl.associateComponentInstancePropertiesToComponent(yamlName, resource, instProperties);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateComponentInstanceInputsToComponent() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        List<ComponentInstanceInput> componentInstanceInputList = new ArrayList<>();
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setName("componentInstanceInputName");
        componentInstanceInputList.add(componentInstanceInput);

        bl.associateComponentInstanceInputsToComponent(yamlName, resource, instInputs);
    }

    @Test
    public void testAssociateDeploymentArtifactsToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateArtifactsToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateArtifactsToInstances2() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateArtifactsToInstances(yamlName, resource, instDeploymentArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateOrAddCalculatedCapReq() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        try {
            bl.associateOrAddCalculatedCapReq(yamlName, resource, instCapabilities, instRequirements);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateInstAttributeToComponentToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        try {
            bl.associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testThrowComponentExceptionByResource() {
        StorageOperationStatus status = StorageOperationStatus.OK;
        Resource resource = createParseResourceObject(true);
        try {
            bl.throwComponentExceptionByResource(status, resource);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testGetResourceAfterCreateRelations() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.getResourceAfterCreateRelations(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetCapabilityNamesTypes() {
        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("Capability");
        capabilityDefinition.setType("Resource");
        capabilityDefinitionList.add(capabilityDefinition);
        originCapabilities.put("Capability", capabilityDefinitionList);
        Map<String, List<UploadCapInfo>> uploadedCapabilities = new HashMap<>();
        List<UploadCapInfo> uploadCapInfoList = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        uploadCapInfoList.add(uploadCapInfo);
        uploadedCapabilities.put("Capability", uploadCapInfoList);

        bl.setCapabilityNamesTypes(originCapabilities, uploadedCapabilities);
    }

    @Test
    public void testAssociateComponentInstanceInputsToComponent2() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();

        try {
            bl.associateComponentInstanceInputsToComponent(yamlName, service, instInputs);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateComponentInstancePropertiesToComponent2() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, List<ComponentInstanceProperty>> instInputs = new HashMap<>();

        try {
            bl.associateComponentInstancePropertiesToComponent(yamlName, service, instInputs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAssociateDeploymentArtifactsToInstances2() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, service, instDeploymentArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateArtifactsToInstances3() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();

        try {
            bl.associateArtifactsToInstances(yamlName, service, instArtifacts);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateOrAddCalculatedCapReq2() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        try {
            bl.associateOrAddCalculatedCapReq(yamlName, resource, instCapabilities, instRequirements);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateInstAttributeToComponentToInstances2() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        try {
            bl.associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateRequirementsToService() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, ListRequirementDataDefinition> requirements = new HashMap<>();
        try {
            bl.associateRequirementsToService(yamlName, resource, requirements);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateCapabilitiesToService() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, ListCapabilityDataDefinition> capabilities = new HashMap<>();
        try {
            bl.associateCapabilitiesToService(yamlName, resource, capabilities);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testAssociateResourceInstances() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.associateResourceInstances(yamlName, resource, relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddCapabilities() {
        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        String type = "type";
        List<CapabilityDefinition> capabilities = new ArrayList<>();

        bl.addCapabilities(originCapabilities, type, capabilities);
    }

    @Test
    public void testAddCapabilitiesProperties() {
        Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
        List<UploadCapInfo> capabilities = new ArrayList<>();
        UploadCapInfo capability = new UploadCapInfo();
        List<UploadPropInfo> properties = new ArrayList<>();
        UploadPropInfo uploadPropInfo = new UploadPropInfo();
        uploadPropInfo.setName("uploadPropInfoName");
        properties.add(uploadPropInfo);
        capability.setProperties(properties);
        capability.setName("capabilityName");
        capabilities.add(capability);

        bl.addCapabilitiesProperties(newPropertiesMap, capabilities);
    }

    @Test
    public void testGetServiceWithGroups() {
        String resourceId = "resourceId";
        try {
            bl.getServiceWithGroups(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetResourceWithGroups() {
        String resourceId = "resourceId";
        try {
            bl.getResourceWithGroups(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAssociateResourceInstances2() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.associateResourceInstances(yamlName, resource, relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddRelationsToRI() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        List<ComponentInstance> componentInstancesList = creatComponentInstances();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList, relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddRelationsToRI_null() {
        String yamlName = "group.yml";
        Resource resource = createParseResourceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
        uploadResInstancesMap.put("uploadComponentInstanceInfo", nodesInfoValue);
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        try {
            bl.addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList,
                relations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddRelationToRI() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        resource.setComponentInstances(creatComponentInstances());
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        bl.addRelationToRI(yamlName, resource, nodesInfoValue, relations);
    }

    @Test
    public void testAddRelationToRI_null() {
        String yamlName = "yamlName.yml";
        Resource resource = createParseResourceObject(false);
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        resource.setComponentInstances(componentInstancesList);
        UploadComponentInstanceInfo nodesInfoValue = getuploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();

        bl.addRelationToRI(yamlName, resource, nodesInfoValue, relations);
    }

    @Test
    public void testFindVfcResource() {
        Service service = createServiceObject(true);
        String currVfcToscaName = "currVfcToscaName";
        String previousVfcToscaName = "previousVfcToscaName";
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.findVfcResource(getCsarInfo(), service, currVfcToscaName, previousVfcToscaName,
                StorageOperationStatus.OK);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    protected GroupDefinition getGroupDefinition() {
        GroupDefinition groupDefinition = new GroupDefinition();
        Map<String, String> members = new HashMap<>();
        members.put("members", "members");
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<PropertyDataDefinition> properties = new ArrayList<>();
        groupDefinition.setInvariantName("groupDefinitionName");
        groupDefinition.setMembers(members);
        groupDefinition.setProperties(properties);
        groupDefinition.setCapabilities(capabilities);
        return groupDefinition;
    }

    protected Resource createParseResourceObject(boolean afterCreate) {
        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setToscaResourceName(RESOURCE_TOSCA_NAME);
        resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        tgs.add(resource.getName());
        resource.setTags(tgs);
        List<String> template = new ArrayList<>();
        template.add("tosca.nodes.Root");
        resource.setDerivedFrom(template);
        resource.setVendorName("Motorola");
        resource.setVendorRelease("1.0.0");
        resource.setContactId("ya5467");
        resource.setIcon("defaulticon");
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        requirements.put("test", requirementDefinitionList);
        resource.setRequirements(requirements);
        resource.setCost("cost");
        resource.setResourceVendorModelNumber("02312233");

        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifacts.put("artifact", artifactDefinition);
        resource.setArtifacts(artifacts);

        resource.setLicenseType("licType");

        if (afterCreate) {
            resource.setName(resource.getName());
            resource.setVersion("0.1");
            resource.setUniqueId(resource.getName()
                .toLowerCase() + ":" + resource.getVersion());
            resource.setCreatorUserId(user.getUserId());
            resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        }
        return resource;
    }

    protected CsarInfo getCsarInfo() {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = new HashMap<>();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = null;
        try {
            mainTemplateContent = loadFileNameToJsonString("service_import_template.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        CsarInfo csarInfo = new CsarInfo(user, csarUuid, csar, vfReousrceName, mainTemplateName, mainTemplateContent,
            false);
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

    protected Service createServiceObject(boolean afterCreate) {
        Service service = new Service();
        service.setUniqueId("sid");
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        category.setIcons(Collections.singletonList("defaulticon"));
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setInstantiationType(INSTANTIATION_TYPE);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(service.getName());
        service.setTags(tgs);
        service.setIcon("defaulticon");
        service.setContactId("aa1234");
        service.setProjectCode("12345");
        service.setEcompGeneratedNaming(true);

        if (afterCreate) {
            service.setVersion("0.1");
            service.setUniqueId(service.getName() + ":" + service.getVersion());
            service.setCreatorUserId(user.getUserId());
            service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        }
        return service;
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