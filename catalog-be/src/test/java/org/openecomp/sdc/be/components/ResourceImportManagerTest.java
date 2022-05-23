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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtilsTest;
import org.openecomp.sdc.be.components.impl.InterfaceDefinitionHandler;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.NodeTypeMetadata;
import org.openecomp.sdc.be.model.NodeTypesMetadataList;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.PolicyException;
import org.openecomp.sdc.exception.ResponseFormat;

public class ResourceImportManagerTest {

    private ResourceImportManager importManager;

    private final AuditingManager auditingManager = mock(AuditingManager.class);
    private final ResponseFormatManager responseFormatManager = mock(ResponseFormatManager.class);
    private final ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic = mock(InterfaceOperationBusinessLogic.class);
    private final InterfaceDefinitionHandler interfaceDefinitionHandler = new InterfaceDefinitionHandler(interfaceOperationBusinessLogic);
    private final JanusGraphDao janusGraphDao = mock(JanusGraphDao.class);
    private final UserBusinessLogic userAdmin = mock(UserBusinessLogic.class);
    private final ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);
    private final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private final CapabilityTypeOperation capabilityTypeOperation = mock(CapabilityTypeOperation.class);
    private UploadResourceInfo resourceMD;

    @BeforeAll
    public static void beforeClass() {
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        Configuration configuration = new Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
    }

    @BeforeEach
    public void beforeTest() {
        importManager = new ResourceImportManager(componentsUtils, capabilityTypeOperation, interfaceDefinitionHandler, janusGraphDao);
        importManager.setAuditingManager(auditingManager);
        when(toscaOperationFacade.getLatestByToscaResourceName(anyString(), any())).thenReturn(Either.left(null));
        when(toscaOperationFacade.getLatestByToscaResourceNameAndModel(anyString(), any())).thenReturn(Either.left(null));
        importManager.setResponseFormatManager(responseFormatManager);
        importManager.setResourceBusinessLogic(resourceBusinessLogic);
        importManager.setToscaOperationFacade(toscaOperationFacade);
        Either<Component, StorageOperationStatus> notFound = Either.right(StorageOperationStatus.NOT_FOUND);
        when(toscaOperationFacade.getComponentByNameAndVendorRelease(any(ComponentTypeEnum.class), anyString(), anyString(),
            any(JsonParseFlagEnum.class), any())).thenReturn(notFound);
        when(janusGraphDao.getByCriteria(any(VertexTypeEnum.class), anyMap(), any(JsonParseFlagEnum.class)))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        resourceMD = createDummyResourceMD();
    }

    @Test
    void testBasicResourceCreation() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        user.setRole("ADMIN");
        user.setFirstName("Jhon");
        user.setLastName("Doh");
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-blockStorage.yml");

        ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        Resource resource = createResource.left;

        testSetConstantMetaData(resource);
        testSetMetaDataFromJson(resource, resourceMD);

        testSetDerivedFrom(resource);
        testSetProperties(resource);

        verify(resourceBusinessLogic).propagateStateToCertified(eq(user), eq(resource), any(LifecycleChangeInfoWithAction.class), eq(false), eq(true),
            eq(false));
    }

    @Test
    void importAllNormativeResourceSuccessTest() {
        final List<NodeTypeMetadata> nodeMetadataList = new ArrayList<>();
        var nodeTypeMetadata1 = new NodeTypeMetadata();
        nodeTypeMetadata1.setToscaName("my.tosca.Type");
        nodeTypeMetadata1.setName("Type");
        nodeMetadataList.add(nodeTypeMetadata1);
        var nodeTypeMetadata2 = new NodeTypeMetadata();
        nodeTypeMetadata2.setToscaName("my.tosca.not.in.the.Yaml");
        nodeMetadataList.add(nodeTypeMetadata2);
        var nodeTypesMetadataList = new NodeTypesMetadataList();
        nodeTypesMetadataList.setNodeMetadataList(nodeMetadataList);
        var user = new User();
        var yaml = "node_types:\n"
            + "  my.tosca.Type:\n"
            + "    description: a description";

        when(toscaOperationFacade.getLatestByName(any(), any())).thenReturn(Either.left(null));
        when(resourceBusinessLogic
            .createOrUpdateResourceByImport(any(Resource.class), any(User.class), eq(true), eq(true), eq(false), eq(null), eq(null), eq(false)))
            .thenReturn(new ImmutablePair<>(new Resource(), ActionStatus.OK));

        importManager.importAllNormativeResource(yaml, nodeTypesMetadataList, user, false, false);
        verify(janusGraphDao).commit();
    }

    @Test
    void importAllNormativeResourceTest_invalidYaml() {
        var invalidYaml = "node_types: my.tosca.Type:";

        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> importManager.importAllNormativeResource(invalidYaml, new NodeTypesMetadataList(), new User(), false, false));
        assertEquals(ActionStatus.INVALID_NODE_TYPES_YAML, actualException.getActionStatus());
    }

    @Test
    void importAllNormativeResourceTest_exceptionDuringImportShouldTriggerRolback() {
        when(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(mock(ResponseFormat.class));
        when(toscaOperationFacade.getLatestByName(any(), any())).thenThrow(new RuntimeException());

        final List<NodeTypeMetadata> nodeMetadataList = new ArrayList<>();
        var nodeTypeMetadata1 = new NodeTypeMetadata();
        nodeTypeMetadata1.setToscaName("my.tosca.Type");
        nodeMetadataList.add(nodeTypeMetadata1);
        var nodeTypeMetadata2 = new NodeTypeMetadata();
        nodeTypeMetadata2.setToscaName("my.tosca.not.in.the.Yaml");
        nodeMetadataList.add(nodeTypeMetadata2);
        var nodeTypesMetadataList = new NodeTypesMetadataList();
        nodeTypesMetadataList.setNodeMetadataList(nodeMetadataList);
        var user = new User();
        var yaml = "node_types:\n"
            + "  my.tosca.Type:\n"
            + "    description: a description";

        assertThrows(ComponentException.class,
            () -> importManager.importAllNormativeResource(yaml, nodeTypesMetadataList, user, false, false));
        verify(janusGraphDao).rollback();
    }


    @Test()
    void testResourceCreationFailed() {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);
        ResponseFormat dummyResponseFormat = createGeneralErrorInfo();

        when(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(dummyResponseFormat);
        setResourceBusinessLogicMock();

        String jsonContent = "this is an invalid yml!";
        ComponentException errorInfoFromTest = null;
        try {
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        } catch (ComponentException e) {
            errorInfoFromTest = e;
        }
        assertNotNull(errorInfoFromTest);
        assertEquals(ActionStatus.GENERAL_ERROR, errorInfoFromTest.getActionStatus());

        verify(resourceBusinessLogic, times(0))
            .createOrUpdateResourceByImport(any(Resource.class), eq(user), eq(true), eq(false), eq(true), eq(null), eq(null), eq(false));
        verify(resourceBusinessLogic, times(0))
            .propagateStateToCertified(eq(user), any(Resource.class), any(LifecycleChangeInfoWithAction.class), eq(false), eq(true), eq(false));
    }

    @Test
    void testResourceCreationWithCapabilities() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-webServer.yml");

        ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        Resource resource = createResource.left;
        testSetCapabilities(resource);

        verify(resourceBusinessLogic)
            .propagateStateToCertified(eq(user), eq(resource), any(LifecycleChangeInfoWithAction.class), eq(false), eq(true), eq(false));
        verify(resourceBusinessLogic).createOrUpdateResourceByImport(resource, user, true, false, true, null, null, false);

    }

    @Test
    void testResourceCreationWithRequirements() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-port.yml");

        ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        testSetRequirements(createResource.left);

    }

    @Test
    void testResourceCreationWithInterfaceImplementation() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-interface-impl.yml");

        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
        interfaceDefinition.setOperations(operations);
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
        when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(interfaceTypes));

        final ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        assertSetInterfaceImplementation(createResource.left);
    }

    @Test
    void testResourceCreationWithInterfaceImplementation_UnknownInterface() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-unknown-interface-impl.yml");

        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
        interfaceDefinition.setOperations(operations);
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
        when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(interfaceTypes));

        ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        assertNull(createResource.left.getInterfaces());
    }

    @Test
    void testResourceCreationWitInterfaceImplementation_UnknownOperation() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-interface-impl-unknown-operation.yml");

        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
        interfaceDefinition.setOperations(operations);
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
        when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes(any())).thenReturn(Either.left(interfaceTypes));

        ImmutablePair<Resource, ActionStatus> createResource =
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false);
        assertNull(createResource.left.getInterfaces());
    }

    @Test
    void testResourceCreationFailedVendorReleaseAlreadyExists() throws IOException {
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        user.setRole("ADMIN");
        user.setFirstName("Jhon");
        user.setLastName("Doh");
        when(userAdmin.getUser(anyString(), anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();
        final Either<Component, StorageOperationStatus> foundResourceEither = Either.left(mock(Resource.class));
        when(toscaOperationFacade.getComponentByNameAndVendorRelease(any(ComponentTypeEnum.class), anyString(), anyString(),
            any(JsonParseFlagEnum.class), any())).thenReturn(foundResourceEither);
        when(toscaOperationFacade.isNodeAssociatedToModel(eq(null), any(Resource.class))).thenReturn(true);

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-blockStorage.yml");

        var actualException = assertThrows(ByActionStatusComponentException.class,
            () -> importManager.importNormativeResource(jsonContent, resourceMD, user, true, true, false));
        assertEquals(ActionStatus.COMPONENT_WITH_VENDOR_RELEASE_ALREADY_EXISTS, actualException.getActionStatus());
    }

    private void setResourceBusinessLogicMock() {
        when(resourceBusinessLogic.getUserAdmin()).thenReturn(userAdmin);
        when(resourceBusinessLogic.createOrUpdateResourceByImport(any(Resource.class), any(User.class), anyBoolean(), anyBoolean(), anyBoolean(),
            eq(null), eq(null), eq(false)))
            .thenAnswer((Answer<ImmutablePair<Resource, ActionStatus>>) invocation -> {
                Object[] args = invocation.getArguments();
                return new ImmutablePair<>((Resource) args[0], ActionStatus.CREATED);

            });
        when(
            resourceBusinessLogic.propagateStateToCertified(any(User.class), any(Resource.class), any(LifecycleChangeInfoWithAction.class), eq(false),
                eq(true), eq(false)))
            .thenAnswer((Answer<Resource>) invocation -> {
                Object[] args = invocation.getArguments();
                return (Resource) args[1];

            });
        when(resourceBusinessLogic.createResourceByDao(
            any(Resource.class), any(User.class), any(AuditingActionEnum.class), anyBoolean(), anyBoolean())).thenAnswer(
            (Answer<Either<Resource, ResponseFormat>>) invocation -> {
                Object[] args = invocation.getArguments();
                return Either.left((Resource) args[0]);

            });
        when(resourceBusinessLogic.validateResourceBeforeCreate(
            any(Resource.class), any(User.class), any(AuditingActionEnum.class), eq(false), eq(null))).thenAnswer(
            (Answer<Either<Resource, ResponseFormat>>) invocation -> {
                Object[] args = invocation.getArguments();
                return Either.left((Resource) args[0]);

            });

        when(resourceBusinessLogic.validatePropertiesDefaultValues(any(Resource.class))).thenReturn(true);
    }

    private ResponseFormat createGeneralErrorInfo() {
        ResponseFormat responseFormat = new ResponseFormat(500);
        responseFormat.setPolicyException(new PolicyException("POL5000", "Error: Internal Server Error. Please try again later", null));
        return responseFormat;
    }

    private UploadResourceInfo createDummyResourceMD() {
        UploadResourceInfo resourceMD = new UploadResourceInfo();
        resourceMD.setName("tosca.nodes.BlockStorage");
        resourceMD.setPayloadName("payLoad");
        resourceMD.addSubCategory("Generic", "Infrastructure");
        resourceMD.setContactId("ya107f");
        resourceMD.setResourceIconPath("defaulticon");
        resourceMD.setTags(Collections.singletonList("BlockStorage"));
        resourceMD.setDescription(
            "Represents a server-local block storage device (i.e., not shared) offering evenly sized blocks of data from which raw storage volumes can be created.");
        resourceMD.setResourceVendorModelNumber("vendorReleaseNumber");
        resourceMD.setNormative(true);
        return resourceMD;
    }

    private void testSetProperties(Resource resource) {
        List<PropertyDefinition> propertiesList = resource.getProperties();

        Map<String, PropertyDefinition> properties = new HashMap<>();
        for (PropertyDefinition propertyDefinition : propertiesList) {
            properties.put(propertyDefinition.getName(), propertyDefinition);
        }

        assertEquals(3, properties.size());
        assertTrue(properties.containsKey("size"));
        PropertyDefinition propertyDefinition = properties.get("size");
        assertEquals("scalar-unit.size", propertyDefinition.getType());
        assertEquals(1, propertyDefinition.getConstraints().size());
        PropertyConstraint propertyConstraint = propertyDefinition.getConstraints().get(0);
        assertTrue(propertyConstraint instanceof GreaterOrEqualConstraint);

        assertTrue(properties.containsKey("volume_id"));
        propertyDefinition = properties.get("volume_id");
        assertEquals("string", propertyDefinition.getType());
        assertFalse(propertyDefinition.isRequired());

        assertTrue(properties.containsKey("snapshot_id"));
        propertyDefinition = properties.get("snapshot_id");
        assertEquals("string", propertyDefinition.getType());
        assertFalse(propertyDefinition.isRequired());

    }

    private void testSetCapabilities(Resource resource) {
        Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
        assertEquals(3, capabilities.size());
        assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint"));
        List<CapabilityDefinition> capabilityList = capabilities.get("tosca.capabilities.Endpoint");
        CapabilityDefinition capability = capabilityList.get(0);
        assertEquals("tosca.capabilities.Endpoint", capability.getType());
        assertEquals("data_endpoint", capability.getName());

        assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint.Admin"));
        capabilityList = capabilities.get("tosca.capabilities.Endpoint.Admin");
        capability = capabilityList.get(0);
        assertEquals("tosca.capabilities.Endpoint.Admin", capability.getType());
        assertEquals("admin_endpoint", capability.getName());

        assertTrue(capabilities.containsKey("tosca.capabilities.Container"));
        capabilityList = capabilities.get("tosca.capabilities.Container");
        capability = capabilityList.get(0);
        assertEquals("tosca.capabilities.Container", capability.getType());
        assertEquals("host", capability.getName());

        List<String> validSourceTypes = capability.getValidSourceTypes();
        assertEquals(1, validSourceTypes.size());
        assertEquals("tosca.nodes.WebApplication", validSourceTypes.get(0));

    }

    private void testSetRequirements(Resource resource) {
        Map<String, List<RequirementDefinition>> requirements = resource.getRequirements();
        assertEquals(2, requirements.size());

        assertTrue(requirements.containsKey("tosca.capabilities.network.Linkable"));
        List<RequirementDefinition> requirementList = requirements.get("tosca.capabilities.network.Linkable");
        RequirementDefinition requirement = requirementList.get(0);
        assertEquals("tosca.capabilities.network.Linkable", requirement.getCapability());
        assertEquals("tosca.relationships.network.LinksTo", requirement.getRelationship());
        assertEquals("link", requirement.getName());

        assertTrue(requirements.containsKey("tosca.capabilities.network.Bindable"));
        requirementList = requirements.get("tosca.capabilities.network.Bindable");
        requirement = requirementList.get(0);
        assertEquals("tosca.capabilities.network.Bindable", requirement.getCapability());
        assertEquals("tosca.relationships.network.BindsTo", requirement.getRelationship());
        assertEquals("binding", requirement.getName());

    }

    private void assertSetInterfaceImplementation(final Resource resource) {
        final Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.size());
        final InterfaceDefinition interfaceDefinition = interfaces.get("tosca.interfaces.node.lifecycle.Standard");
        assertTrue(interfaces.containsKey(interfaceDefinition.getType()));
        Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
        operations.values().forEach(operationDataDefinition ->
            assertTrue(operations.containsKey(operationDataDefinition.getName())));
    }

    private void testSetDerivedFrom(Resource resource) {
        assertEquals(1, resource.getDerivedFrom().size());
        assertEquals("tosca.nodes.Root", resource.getDerivedFrom().get(0));

    }

    private void testSetMetaDataFromJson(Resource resource, UploadResourceInfo resourceMD) {
        assertEquals(resource.getDescription(), resourceMD.getDescription());
        assertEquals(resource.getIcon(), resourceMD.getResourceIconPath());
        assertEquals(resource.getName(), resourceMD.getName());
        assertEquals(resource.getResourceVendorModelNumber(), resourceMD.getResourceVendorModelNumber());
        assertEquals(resource.getContactId(), resourceMD.getContactId());
        assertEquals(resource.getCreatorUserId(), resourceMD.getContactId());
        assertEquals(resourceMD.getTags().size(), resource.getTags().size());
        for (String tag : resource.getTags()) {
            assertTrue(resourceMD.getTags().contains(tag));
        }
        assertEquals(resourceMD.isNormative(), resource.getComponentMetadataDefinition().getMetadataDataDefinition().isNormative());
    }

    private void testSetConstantMetaData(Resource resource) {
        assertEquals(resource.getVersion(), TypeUtils.getFirstCertifiedVersionVersion());
        assertSame(ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE, resource.getLifecycleState());
        assertEquals(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION, resource.isHighestVersion());
        assertEquals(ImportUtils.Constants.VENDOR_NAME, resource.getVendorName());
        assertEquals(ImportUtils.Constants.VENDOR_RELEASE, resource.getVendorRelease());
    }

}
