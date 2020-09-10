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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_NAME;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.User;

import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

public class ServiceImportParseLogicTest extends ServiceImportBussinessLogicBaseTestSetup {

    ComponentsUtils componentsUtils =  new ComponentsUtils(Mockito.mock(AuditingManager.class));
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    ICapabilityTypeOperation capabilityTypeOperation = Mockito.mock(ICapabilityTypeOperation.class);
    IElementOperation elementDao = Mockito.mock(IElementOperation.class);

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
        CsarInfo csarInfo = new CsarInfo(user, "csar_UUID", csar, "vfResourceName", "mainTemplateName", "mainTemplateContent", true);
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
            bl.findAddNodeTypeArtifactsToHandle(getCsarInfo(), nodeTypesArtifactsToHandle, service, extractedVfcsArtifacts, namespace, p1);;
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_TOSCA_TEMPLATE,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testHandleAndAddExtractedVfcsArtifacts() {

        List<ArtifactDefinition> vfcArtifacts = new ArrayList<>();
        List<ArtifactDefinition> artifactsToAdd = new ArrayList<>();
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
            bl.processExistingNodeTypeArtifacts(extractedArtifacts, artifactsToUpload, artifactsToUpdate, artifactsToDelete, existingArtifacts);
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
        ArtifactDefinition currNewArtifact = new ArtifactDefinition();
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
            bl.validateResourceBeforeCreate(resource, user, AuditingActionEnum.IMPORT_RESOURCE,false, getCsarInfo());
        } catch (ComponentException e) {

        }

    }

    @Test
    public void testValidateResourceType() {
        Resource resource = createParseResourceObject(true);
        bl.validateResourceType( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }


    @Test
    public void testValidateLifecycleTypesCreate() {
        Resource resource = createParseResourceObject(true);
        bl.validateLifecycleTypesCreate( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateCapabilityTypesCreate() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateCapabilityTypesCreate( user, bl.getCapabilityTypeOperation(), resource, AuditingActionEnum.IMPORT_RESOURCE, true);
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

            bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource, AuditingActionEnum.IMPORT_RESOURCE,
                eitherResult, typeEntry, false);
        }
    }

    @Test
    public void testValidateCapabilityTypeExists2() {
        Resource resource = createParseResourceObject(true);
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        try {
            for (String type : resource.getRequirements().keySet()) {
                bl.validateCapabilityTypeExists(user, bl.getCapabilityTypeOperation(), resource,
                    resource.getRequirements().get(type), AuditingActionEnum.IMPORT_RESOURCE, eitherResult, type, false);
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
            bl.validateResourceFieldsBeforeCreate( user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
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
            bl.validateDerivedFromExist( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
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
            bl.validateDerivedFromExist( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
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
            bl.validateDerivedFromExist( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateLicenseType() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.validateLicenseType( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_CONTENT,
                ComponentTypeEnum.RESOURCE.getValue());
        }


    }

    @Test
    public void testValidateCost() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateCost( resource);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_CONTENT,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateResourceVendorModelNumber() {
        Resource resource = createParseResourceObject(true);
        bl.validateResourceVendorModelNumber( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateResourceVendorModelNumberWrongLen() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceVendorModelNumber("000000000011122221111222333444443222556677788778889999998776554332340");
        try {
            bl.validateResourceVendorModelNumber( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {

        }
    }

    @Test
    public void testValidateResourceVendorModelNumberWrongValue() {
        Resource resource = createParseResourceObject(true);
        resource.setResourceVendorModelNumber("");
        try {
            bl.validateResourceVendorModelNumber( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateVendorReleaseName() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorRelease("0.1");
        bl.validateVendorReleaseName( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
    }

    @Test
    public void testValidateVendorReleaseNameFailure() {
        Resource resource = createParseResourceObject(true);
        resource.setVendorRelease("");
        try {
            bl.validateVendorReleaseName( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_VENDOR_RELEASE,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testValidateCategory() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.validateCategory( user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateCategoryListed() {
        Resource resource = createParseResourceObject(true);
        CategoryDefinition category = resource.getCategories().get(0);
        SubCategoryDefinition subcategory = category.getSubcategories().get(0);
        try {
            bl.validateCategoryListed( category, subcategory, user, resource, AuditingActionEnum.IMPORT_RESOURCE, true);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFailOnInvalidCategory() {
        Resource resource = createParseResourceObject(true);
        try {
            bl.failOnInvalidCategory( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }

    }

    @Test
    public void testValidateVendorName() {
        Resource resource = createParseResourceObject(true);
        CategoryDefinition category = resource.getCategories().get(0);
        SubCategoryDefinition subcategory = category.getSubcategories().get(0);
        try {
            bl.validateVendorName( user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testValidateVendorName2() {
        Resource resource = createParseResourceObject(true);
        CategoryDefinition category = resource.getCategories().get(0);
        SubCategoryDefinition subcategory = category.getSubcategories().get(0);
        String vendorName = "vendorName";
        try {
            bl.validateVendorName( vendorName, user, resource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.GENERAL_ERROR,
                ComponentTypeEnum.RESOURCE.getValue());
        }
    }

    @Test
    public void testFillResourceMetadata2() {
        String yamlName = "yamlName";
        Resource resourceVf = createParseResourceObject(true);
        String nodeName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX+"test";
        try {
            bl.fillResourceMetadata( yamlName, resourceVf, nodeName, user);
        } catch (ComponentException e) {

        }
    }

    @Test
    public void testGetNodeTypeActualName() {
        String fullName = Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX+"test";
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
        String regName = "regName";
        String yamlName = "yamlName";
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        ComponentInstance currentCompInstance = new ComponentInstance();
        String capName = "capName";
        try {
            bl.findAviableRequiremen(regName, yamlName, uploadComponentInstanceInfo, currentCompInstance, capName);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFindAvailableCapabilityByTypeOrName() {
        RequirementDefinition validReq = new RequirementDefinition();
        ComponentInstance currentCapCompInstance = new ComponentInstance();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();

        try {
            bl.findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadReqInfo);
        } catch (Exception e) {

        }
    }


    @Test
    public void testFindAvailableCapability() {
        RequirementDefinition validReq = new RequirementDefinition();
        ComponentInstance instance = new ComponentInstance();
        try {
            bl.findAvailableCapability(validReq, instance);
        } catch (Exception e) {

        }
    }

    @Test
    public void testfindAvailableCapability2() {
        RequirementDefinition validReq = new RequirementDefinition();
        ComponentInstance instance = new ComponentInstance();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        try {
            bl.findAvailableCapability(validReq, instance, uploadReqInfo);
        } catch (Exception e) {

        }
    }

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
        String key = "key";
        List<UploadCapInfo> capabilities = new ArrayList<>();
        String resourceId = "resourceId";
        Map<String, List<CapabilityDefinition>> defaultCapabilities = new HashMap<>();
        Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();
        InputDefinition prop = new InputDefinition();
        try {
            bl.addValidComponentInstanceCapabilities(key, capabilities, resourceId, defaultCapabilities, validCapabilitiesMap);
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetCapability() {
        String resourceId = "resourceId";
        Map<String, List<CapabilityDefinition>> defaultCapabilities = new HashMap<>();
        String capabilityType = "capabilityType";

        try {
            bl.getCapability(resourceId, defaultCapabilities, capabilityType);
        } catch (Exception e) {

        }
    }


    @Test
    public void testValidateCapabilityProperties() {
        List<UploadCapInfo> capabilities = new ArrayList<>();
        String resourceId = "resourceId";
        CapabilityDefinition defaultCapability = new CapabilityDefinition();
        String capabilityType = "capabilityType";

        try {
            bl.validateCapabilityProperties(capabilities, resourceId, defaultCapability);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateUniquenessUpdateUploadedComponentInstanceCapability() {
        List<UploadCapInfo> capabilities = new ArrayList<>();
        String resourceId = "resourceId";
        CapabilityDefinition defaultCapability = new CapabilityDefinition();
        UploadCapInfo uploadedCapability = new UploadCapInfo();
        String capabilityType = "capabilityType";

        try {
            bl.validateUniquenessUpdateUploadedComponentInstanceCapability(defaultCapability, uploadedCapability);
        } catch (Exception e) {

        }
    }

    @Test
    public void testSetDeploymentArtifactsPlaceHolderByResource() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.setDeploymentArtifactsPlaceHolder(resource, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testSetDeploymentArtifactsPlaceHolderByService() {
        Service Service = createServiceObject(true);

        try {
            bl.setDeploymentArtifactsPlaceHolder(Service, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testProcessDeploymentResourceArtifacts() {
        Resource resource = createParseResourceObject(true);
        Map<String, ArtifactDefinition> artifactMap = new HashMap<>();
        String k = "key";
        Object v = new Object();
        try {
            bl.processDeploymentResourceArtifacts(user, resource, artifactMap, k, v);
        } catch (Exception e) {

        }
    }

    @Test
    public void testMergeOldResourceMetadataWithNew() {
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = createParseResourceObject(true);

        try {
            bl.mergeOldResourceMetadataWithNew(oldResource, newResource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testBuildComplexVfcMetadata() {
        String nodeName = "nodeName";
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();

        try {
            bl.buildComplexVfcMetadata(getCsarInfo(), nodeName, nodesInfo);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateResourceCreationFromNodeType() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.validateResourceCreationFromNodeType(resource, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateInputsOnResource() {
        Resource resource = createParseResourceObject(true);
        Map<String, InputDefinition> inputs = new HashMap<>();

        try {
            bl.createInputsOnResource(resource, inputs);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateInputsOnService() {
        Service service = createServiceObject(true);
        Map<String, InputDefinition> inputs = new HashMap<>();

        try {
            bl.createInputsOnService(service, inputs);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateServiceTransaction() {
        Service service = createServiceObject(true);

        try {
            bl.createServiceTransaction(service, user, true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateArtifactsPlaceHolderData() {
        Service service = createServiceObject(true);

        try {
            bl.createArtifactsPlaceHolderData(service, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testSetInformationalArtifactsPlaceHolder() {
        Service service = createServiceObject(true);

        try {
            bl.setInformationalArtifactsPlaceHolder(service, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateNestedDerivedFromDuringUpdate() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.validateNestedDerivedFromDuringUpdate(currentResource, updateInfoResource, true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateDerivedFromExtending() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.validateDerivedFromExtending(user, currentResource, updateInfoResource, AuditingActionEnum.IMPORT_RESOURCE);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateResourceFieldsBeforeUpdate() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.validateResourceFieldsBeforeUpdate(currentResource, updateInfoResource, true, true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateResourceName() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.validateResourceName(currentResource, updateInfoResource, true, true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testIsResourceNameEquals() {
        Resource currentResource = createParseResourceObject(true);
        Resource updateInfoResource = createParseResourceObject(true);

        try {
            bl.isResourceNameEquals(currentResource, updateInfoResource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testPrepareResourceForUpdate() {
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = createParseResourceObject(true);

        try {
            bl.prepareResourceForUpdate(oldResource, newResource, user, true, true);
        } catch (Exception e) {

        }
    }

   @Test
    public void testFailOnChangeState() {
        ResponseFormat response = new ResponseFormat();
        Resource oldResource = createParseResourceObject(true);
        Resource newResource = createParseResourceObject(true);

        try {
            bl.failOnChangeState(response,  user, oldResource, newResource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testHandleResourceGenericType() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.handleResourceGenericType(resource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testUpdateOrCreateGroups() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.updateOrCreateGroups(resource, groups);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAddGroupsToCreateOrUpdate() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        List<GroupDefinition> groupsFromResource = new ArrayList<>();
        List<GroupDefinition> groupsAsList = new ArrayList<>();
        List<GroupDefinition> groupsToUpdate = new ArrayList<>();
        List<GroupDefinition> groupsToCreate = new ArrayList<>();

        try {
            bl.addGroupsToCreateOrUpdate(groupsFromResource, groupsAsList, groupsToUpdate, groupsToCreate);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAddGroupsToDelete() {
        Map<String, GroupDefinition> groups = new HashMap<>();

        List<GroupDefinition> groupsFromResource = new ArrayList<>();
        List<GroupDefinition> groupsAsList = new ArrayList<>();
        List<GroupDefinition> groupsToDelete = new ArrayList<>();

        try {
            bl.addGroupsToDelete(groupsFromResource, groupsAsList, groupsToDelete);
        } catch (Exception e) {

        }
    }

    @Test
    public void testUpdateGroupsMembersUsingResource() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.updateGroupsMembersUsingResource(groups, component);
        } catch (Exception e) {

        }
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
        } catch (Exception e) {

        }
    }

    @Test
    public void testValidateCyclicGroupsDependencies() {
        Service component = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.validateCyclicGroupsDependencies(groups);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFillAllGroupMemebersRecursivly() {
        Map<String, GroupDefinition> allGroups = new HashMap<>();
        Set<String> allGroupMembers = new HashSet<>();
        String groupName = "groupName";

        try {
            bl.fillAllGroupMemebersRecursivly(groupName, allGroups, allGroupMembers);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFillResourceMetadataForService() {
        String yamlName = "yamlName";
        Service resourceVf = createServiceObject(true);
        String nodeName = "nodeName";

        try {
            bl.fillResourceMetadata(yamlName, resourceVf, nodeName, user);
        } catch (Exception e) {

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

        }
    }

    @Test
    public void testBuildValidComplexVfc2() {
        String nodeName = "nodeName";
        Resource resource = createParseResourceObject(true);
        Map<String, NodeTypeInfo> nodesInfo = new HashMap<>();

        try {
            bl.buildValidComplexVfc(getCsarInfo(), nodeName, nodesInfo);
        } catch (Exception e) {

        }
    }

    @Test
    public void testUpdateGroupsOnResource() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();

        try {
            bl.updateGroupsOnResource(resource, groups);
        } catch (Exception e) {

        }
    }

    @Test
    public void testSetInformationalArtifactsPlaceHolder2() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.setInformationalArtifactsPlaceHolder(resource, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateArtifactsPlaceHolderData2() {
        Resource resource = createParseResourceObject(true);

        try {
            bl.createArtifactsPlaceHolderData(resource, user);
        } catch (Exception e) {

        }
    }

    @Test
    public void testHandleGroupsProperties() {
        Service service = createServiceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        try {
            bl.handleGroupsProperties(service, groups);
        } catch (Exception e) {

        }
    }

     @Test
    public void testHandleGroupsProperties2() {
        Resource resource = createParseResourceObject(true);
        Map<String, GroupDefinition> groups = new HashMap<>();
        try {
            bl.handleGroupsProperties(resource, groups);
        } catch (Exception e) {

        }
    }

    @Test
    public void testHandleGetInputs() {
        PropertyDataDefinition property = new PropertyDataDefinition();
        List<InputDefinition> inputs = new ArrayList<>();
        try {
            bl.handleGetInputs(property, inputs);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFindInputByName() {
        GetInputValueDataDefinition getInput = new GetInputValueDataDefinition();
        List<InputDefinition> inputs = new ArrayList<>();
        try {
            bl.findInputByName(inputs, getInput);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateComponentInstancePropertiesToComponent() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        List<InputDefinition> inputs = new ArrayList<>();
        try {
            bl.associateComponentInstancePropertiesToComponent(yamlName, resource, instProperties);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateComponentInstanceInputsToComponent() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();

        try {
            bl.associateComponentInstanceInputsToComponent(yamlName, resource, instInputs);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateDeploymentArtifactsToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateArtifactsToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateArtifactsToInstances2() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateArtifactsToInstances(yamlName, resource, instDeploymentArtifacts);
        } catch (Exception e) {

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
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateInstAttributeToComponentToInstances() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, List<AttributeDataDefinition>> instAttributes = new HashMap<>();
        try {
            bl.associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        } catch (Exception e) {

        }
    }

    @Test
    public void testThrowComponentExceptionByResource() {
        StorageOperationStatus status = StorageOperationStatus.OK;
        Resource resource = createParseResourceObject(true);
        try {
            bl.throwComponentExceptionByResource(status, resource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetResourceAfterCreateRelations() {

        Resource resource = createParseResourceObject(true);

        try {
            bl.getResourceAfterCreateRelations(resource);
        } catch (Exception e) {

        }
    }

    @Test
    public void testSetCapabilityNamesTypes() {

        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        Map<String, List<UploadCapInfo>> uploadedCapabilities = new HashMap<>();

        try {
            bl.setCapabilityNamesTypes(originCapabilities, uploadedCapabilities);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateComponentInstanceInputsToComponent2() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();

        try {
            bl.associateComponentInstanceInputsToComponent(yamlName, service, instInputs);
        } catch (Exception e) {

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

        }
    }

    @Test
    public void testAssociateDeploymentArtifactsToInstances2() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();

        try {
            bl.associateDeploymentArtifactsToInstances(user, yamlName, service, instDeploymentArtifacts);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateArtifactsToInstances3() {
        String yamlName = "yamlName";
        Service service = createServiceObject(true);
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();

        try {
            bl.associateArtifactsToInstances(yamlName, service, instArtifacts);
        } catch (Exception e) {

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
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateInstAttributeToComponentToInstances2() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, List<AttributeDataDefinition>> instAttributes = new HashMap<>();
        try {
            bl.associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateRequirementsToService() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, ListRequirementDataDefinition> requirements = new HashMap<>();
        try {
            bl.associateRequirementsToService(yamlName, resource, requirements);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAssociateCapabilitiesToService() {
        String yamlName = "yamlName";
        Service resource = createServiceObject(true);
        Map<String, ListCapabilityDataDefinition> capabilities = new HashMap<>();
        try {
            bl.associateCapabilitiesToService(yamlName, resource, capabilities);
        } catch (Exception e) {

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

        }
    }

    @Test
    public void testAddCapabilities() {
        Map<String, List<CapabilityDefinition>> originCapabilities = new HashMap<>();
        String type = "type";
        List<CapabilityDefinition> capabilities = new ArrayList<>();
           try {
            bl.addCapabilities(originCapabilities, type, capabilities);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAddCapabilitiesProperties() {
        Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
        List<UploadCapInfo> capabilities = new ArrayList<>();
        try {
            bl.addCapabilitiesProperties(newPropertiesMap, capabilities);
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetServiceWithGroups() {
        String resourceId = "resourceId";
        try {
            bl.getServiceWithGroups(resourceId);
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetResourceWithGroups() {
        String resourceId = "resourceId";
        try {
            bl.getResourceWithGroups(resourceId);
        } catch (Exception e) {

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

        }
    }

    @Test
    public void testAddRelationsToRI() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap = new HashMap<>();
        List<ComponentInstance> componentInstancesList = new ArrayList<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList, relations);
        } catch (Exception e) {

        }
    }

    @Test
    public void testAddRelationsToRI2() {
        String yamlName = "yamlName";
        Resource resource = createParseResourceObject(true);
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.addRelationToRI(yamlName, resource, nodesInfoValue, relations);
        } catch (Exception e) {

        }
    }

    @Test
    public void testFindVfcResource() {
        Service service = createServiceObject(true);
        String currVfcToscaName = "currVfcToscaName";
        String previousVfcToscaName = "previousVfcToscaName";
        UploadComponentInstanceInfo nodesInfoValue = new UploadComponentInstanceInfo();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        try {
            bl.findVfcResource(getCsarInfo(), service, currVfcToscaName, previousVfcToscaName, StorageOperationStatus.OK);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.OK,
                ComponentTypeEnum.RESOURCE.getValue());
        }
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

    protected CsarInfo getCsarInfo ()
    {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = new HashMap<>();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent();
        final Service service = createServiceObject(false);
        CsarInfo csarInfo = new CsarInfo(user, csarUuid,  csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
        return csarInfo;
    }

    protected Service createServiceObject (boolean afterCreate) {
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
        // service.setVendorName("Motorola");
        // service.setVendorRelease("1.0.0");
        service.setIcon("defaulticon");
        // service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
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