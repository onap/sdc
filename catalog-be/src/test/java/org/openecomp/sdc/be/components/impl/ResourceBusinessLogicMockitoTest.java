/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

@RunWith(MockitoJUnitRunner.class)
public class ResourceBusinessLogicMockitoTest {

    private static final String RESOURCE_NAME = "resourceName";
    private static final String USER_ID = "userId";
    private static final String VALIDATE_RESOURCE_NAME_EXISTS = "validate Resource Name Exists";
    private static final String CSAR_UUID = "CsarUUID";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String ANY = "ANY";

    @Mock
    private IElementOperation elementDao;
    @Mock
    private IGroupOperation groupOperation;
    @Mock
    private IGroupInstanceOperation groupInstanceOperation;
    @Mock
    private IGroupTypeOperation groupTypeOperation;
    @Mock
    private GroupBusinessLogic groupBusinessLogic;
    @Mock
    private InterfaceOperation interfaceOperation;
    @Mock
    private InterfaceLifecycleOperation interfaceLifecycleTypeOperation;
    @Mock
    private ArtifactsBusinessLogic artifactsBusinessLogic;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ResourceImportManager resourceImportManager;
    @Mock
    private InputsBusinessLogic inputsBusinessLogic;
    @Mock
    private CompositionBusinessLogic compositionBusinessLogic;
    @Mock
    private ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;
    @Mock
    private CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic;
    @Mock
    private MergeInstanceUtils mergeInstanceUtils;
    @Mock
    private UiComponentDataConverter uiComponentDataConverter;
    @Mock
    private CsarBusinessLogic csarBusinessLogic;
    @Mock
    private ArtifactsOperations artifactToscaOperation;
    @Mock
    private LifecycleBusinessLogic lifecycleManager;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private ComponentsUtils componentUtils;
    @Mock
    private ICapabilityTypeOperation capabilityTypeOperation;
    @Mock
    private UserValidations userValidations;
    @Mock
    private User user;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private CsarInfo csarInfo;
    @Mock
    private Map<String, NodeTypeInfo> nodeTypeInfo;
    @Mock
    private ParsedToscaYamlInfo parsedToscaYamlInfo;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private GenericTypeBusinessLogic genericTypeBusinessLogic;
    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private SoftwareInformationBusinessLogic softwareInformationBusinessLogic;

    private ResourceBusinessLogic resourceBusinessLogic;

    @Before
    public void setUp() throws Exception {
        resourceBusinessLogic = new ResourceBusinessLogic(elementDao,
            groupOperation,
            groupInstanceOperation,
            groupTypeOperation,
            groupBusinessLogic,
            interfaceOperation,
            interfaceLifecycleTypeOperation,
            artifactsBusinessLogic,
            componentInstanceBusinessLogic,
            resourceImportManager,
            inputsBusinessLogic,
            compositionBusinessLogic,
            resourceDataMergeBusinessLogic,
            csarArtifactsAndGroupsBusinessLogic,
            mergeInstanceUtils,
            uiComponentDataConverter,
            csarBusinessLogic,
            artifactToscaOperation,
            propertyBusinessLogic,
            softwareInformationBusinessLogic);

        resourceBusinessLogic.setLifecycleManager(lifecycleManager);
        resourceBusinessLogic.setApplicationDataTypeCache(applicationDataTypeCache);
        resourceBusinessLogic.setComponentsUtils(componentUtils);
        resourceBusinessLogic.setCapabilityTypeOperation(capabilityTypeOperation);
        resourceBusinessLogic.setUserValidations(userValidations);
        resourceBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
        resourceBusinessLogic.setJanusGraphDao(janusGraphDao);
        resourceBusinessLogic.setGraphLockOperation(graphLockOperation);
        resourceBusinessLogic.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
    }

    @Test
    public void testGetters() {
        assertEquals(resourceBusinessLogic.getComponentInstanceBL(), componentInstanceBusinessLogic);
        assertEquals(resourceBusinessLogic.getElementDao(), elementDao);
        assertEquals(resourceBusinessLogic.getLifecycleBusinessLogic(), lifecycleManager);
        assertEquals(resourceBusinessLogic.getApplicationDataTypeCache(), applicationDataTypeCache);
        assertEquals(resourceBusinessLogic.getComponentsUtils(), componentUtils);
        assertEquals(resourceBusinessLogic.getCapabilityTypeOperation(), capabilityTypeOperation);
    }

    @Test
    public void shouldValidateResourceNameExistsIfDataModelResponseIsRight() {
        Mockito.when(userValidations.validateUserExists(USER_ID, VALIDATE_RESOURCE_NAME_EXISTS, false))
            .thenReturn(user);
        Mockito.when(toscaOperationFacade
            .validateComponentNameUniqueness(RESOURCE_NAME, ResourceTypeEnum.ABSTRACT, ComponentTypeEnum.RESOURCE))
            .thenReturn(Either.right(StorageOperationStatus.DECLARED_INPUT_USED_BY_OPERATION));
        Mockito.when(componentUtils.convertFromStorageResponse(StorageOperationStatus.DECLARED_INPUT_USED_BY_OPERATION))
            .thenReturn(ActionStatus.DECLARED_INPUT_USED_BY_OPERATION);
        Either<Map<String, Boolean>, ResponseFormat> response = resourceBusinessLogic
            .validateResourceNameExists(RESOURCE_NAME, ResourceTypeEnum.ABSTRACT, USER_ID);
        assertTrue(response.isRight());
    }

    @Test(expected = ByResponseFormatComponentException.class)
    public void shouldThrowExceptionOnCreateResourceIfCsarUUIDIsNotEmptyIfAlreadyExist() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any())).thenReturn(Either.left(true));
        Resource resource = getResource();
        Map<String, byte[]> csarUIPayload = Collections.emptyMap();
        Mockito.when(csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, PAYLOAD)).thenReturn(csarInfo);
        Mockito.when(csarInfo.extractNodeTypesInfo()).thenReturn(nodeTypeInfo);
        Map<String, UploadComponentInstanceInfo> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put(ANY, new UploadComponentInstanceInfo());
        resource.setResourceType(ResourceTypeEnum.ABSTRACT);
        Mockito.when(graphLockOperation.lockComponentByName(Mockito.any(), Mockito.any()))
            .thenReturn(StorageOperationStatus.OK);
        Mockito.when(parsedToscaYamlInfo.getInstances()).thenReturn(nonEmptyMap);
        Mockito.when(csarBusinessLogic.getParsedToscaYamlInfo(null, null, nodeTypeInfo, csarInfo, null)).thenReturn(
            parsedToscaYamlInfo);
        Mockito.when(toscaOperationFacade.validateComponentNameExists(
            resource.getName(), resource.getResourceType(), resource.getComponentType())).thenReturn(Either.left(true));
        Mockito.when(genericTypeBusinessLogic.fetchDerivedFromGenericType(Mockito.any()))
            .thenReturn(Either.left(resource));
        resourceBusinessLogic.createResource(resource, AuditingActionEnum.ADD_USER, user, csarUIPayload, PAYLOAD);
    }

    @Test(expected = ByResponseFormatComponentException.class)
    public void shouldThrowExceptionOnCreateResourceIfCsarUUIDIsNotEmptyButComponentNameNotExists() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any())).thenReturn(Either.left(true));
        Resource resource = getResource();
        Map<String, byte[]> csarUIPayload = Collections.emptyMap();
        Mockito.when(csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, PAYLOAD)).thenReturn(csarInfo);
        Mockito.when(csarInfo.extractNodeTypesInfo()).thenReturn(nodeTypeInfo);
        Map<String, UploadComponentInstanceInfo> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put(ANY, new UploadComponentInstanceInfo());
        resource.setResourceType(ResourceTypeEnum.ABSTRACT);
        Mockito.when(graphLockOperation.lockComponentByName(Mockito.any(), Mockito.any()))
            .thenReturn(StorageOperationStatus.OK);
        Mockito.when(parsedToscaYamlInfo.getInstances()).thenReturn(nonEmptyMap);
        Mockito.when(csarBusinessLogic.getParsedToscaYamlInfo(null, null, nodeTypeInfo, csarInfo, null)).thenReturn(
            parsedToscaYamlInfo);
        Mockito.when(toscaOperationFacade.validateComponentNameExists(
            resource.getName(), resource.getResourceType(), resource.getComponentType()))
            .thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Mockito.when(genericTypeBusinessLogic.fetchDerivedFromGenericType(Mockito.any()))
            .thenReturn(Either.left(resource));
        resourceBusinessLogic.createResource(resource, AuditingActionEnum.ADD_USER, user, csarUIPayload, PAYLOAD);
    }

    @Test(expected = ByActionStatusComponentException.class)
    public void shouldThrowExceptionOnCreateResourceIfCsarUUIDIsNotEmptyButEmptyDerivedFromGenericType() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any())).thenReturn(Either.left(true));
        Resource resource = getResource();
        Map<String, byte[]> csarUIPayload = Collections.emptyMap();
        Mockito.when(csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, PAYLOAD)).thenReturn(csarInfo);
        Mockito.when(csarInfo.extractNodeTypesInfo()).thenReturn(nodeTypeInfo);
        Map<String, UploadComponentInstanceInfo> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put(ANY, new UploadComponentInstanceInfo());
        resource.setResourceType(ResourceTypeEnum.ABSTRACT);
        Mockito.when(graphLockOperation.lockComponentByName(Mockito.any(), Mockito.any()))
            .thenReturn(StorageOperationStatus.OK);
        Mockito.when(parsedToscaYamlInfo.getInstances()).thenReturn(nonEmptyMap);
        Mockito.when(genericTypeBusinessLogic.fetchDerivedFromGenericType(Mockito.any()))
            .thenReturn(Either.right(new ResponseFormat()));
        Mockito.when(csarBusinessLogic.getParsedToscaYamlInfo(null, null, nodeTypeInfo, csarInfo, null)).thenReturn(
            parsedToscaYamlInfo);
        resourceBusinessLogic.createResource(resource, AuditingActionEnum.ADD_USER, user, csarUIPayload, PAYLOAD);
    }

    @Test(expected = ByResponseFormatComponentException.class)
    public void shouldThrowExceptionOnCreateResourceIfCsarUUIDIsNotEmptyButInvalidLockResponse() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any())).thenReturn(Either.left(true));
        Resource resource = getResource();
        Map<String, byte[]> csarUIPayload = Collections.emptyMap();
        Mockito.when(csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, PAYLOAD)).thenReturn(csarInfo);
        Mockito.when(csarInfo.extractNodeTypesInfo()).thenReturn(nodeTypeInfo);
        Map<String, UploadComponentInstanceInfo> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put(ANY, new UploadComponentInstanceInfo());
        resource.setResourceType(ResourceTypeEnum.ABSTRACT);
        Mockito.when(parsedToscaYamlInfo.getInstances()).thenReturn(nonEmptyMap);
        Mockito.when(graphLockOperation.lockComponentByName(Mockito.any(), Mockito.any()))
            .thenReturn(StorageOperationStatus.BAD_REQUEST);
        Mockito.when(csarBusinessLogic.getParsedToscaYamlInfo(null, null, nodeTypeInfo, csarInfo, null)).thenReturn(
            parsedToscaYamlInfo);
        resourceBusinessLogic.createResource(resource, AuditingActionEnum.ADD_USER, user, csarUIPayload, PAYLOAD);
    }


    @Test(expected = ByActionStatusComponentException.class)
    public void shouldThrowExceptionOnNonPnfResource() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any())).thenReturn(Either.left(true));
        Resource resource = getResource();
        Map<String, byte[]> csarUIPayload = Collections.emptyMap();
        Mockito.when(csarBusinessLogic.getCsarInfo(resource, null, user, csarUIPayload, PAYLOAD)).thenReturn(csarInfo);
        Mockito.when(csarInfo.extractNodeTypesInfo()).thenReturn(nodeTypeInfo);
        Mockito.when(csarBusinessLogic.getParsedToscaYamlInfo(null, null, nodeTypeInfo, csarInfo, null)).thenReturn(
            parsedToscaYamlInfo);
        resourceBusinessLogic.createResource(resource, AuditingActionEnum.ADD_USER, user, csarUIPayload, PAYLOAD);
    }

    @Test(expected = ByActionStatusComponentException.class)
    public void shouldThrowExceptionOnFailedToRetrieveResourceCategoriesFromJanusGraph() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.right(ActionStatus.ARTIFACT_NOT_FOUND));
        Resource resource = getResource();
        resourceBusinessLogic
            .createResource(resource, AuditingActionEnum.ADD_USER, user, Collections.emptyMap(), PAYLOAD);
    }

    @Test(expected = ByActionStatusComponentException.class)
    public void shouldThrowExceptionOnRightDataModelResponse() {
        Mockito.when(elementDao
            .getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
            .thenReturn(Either.left(getCategoryDefinitions()));
        Mockito.when(toscaOperationFacade.validateToscaResourceNameExists(Mockito.any()))
            .thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Resource resource = getResource();
        resourceBusinessLogic
            .createResource(resource, AuditingActionEnum.ADD_USER, user, Collections.emptyMap(), PAYLOAD);
    }

    private Resource getResource() {
        Resource resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        resource.setName(ANY);
        resource.setDescription(ANY);
        resource.setCategories(getCategoryDefinitions());
        resource.setVendorName(ANY);
        resource.setVendorRelease(ANY);
        List<String> tags = new ArrayList<>();
        tags.add(ANY);
        resource.setTags(tags);
        resource.setContactId(ANY);
        resource.setIcon(ANY);
        List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add(ANY);
        resource.setDerivedFrom(derivedFrom);
        return resource;
    }

    private List<CategoryDefinition> getCategoryDefinitions() {
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName(ANY);
        SubCategoryDefinition subcategory = new SubCategoryDefinition();
        subcategory.setName(ANY);
        categoryDefinition.addSubCategory(subcategory);
        categories.add(categoryDefinition);
        return categories;
    }
}