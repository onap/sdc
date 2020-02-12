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

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtilsTest;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ResourceImportManagerTest {

    private static ConfigurationManager configurationManager;
    static ResourceImportManager importManager;
    static AuditingManager auditingManager = Mockito.mock(AuditingManager.class);
    static ResponseFormatManager responseFormatManager = Mockito.mock(ResponseFormatManager.class);
    static ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
    static InterfaceOperationBusinessLogic interfaceOperationBusinessLogic = Mockito.mock(InterfaceOperationBusinessLogic.class);

    static UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
    static ToscaOperationFacade toscaOperationFacade =  Mockito.mock(ToscaOperationFacade.class);

    protected static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private static final CapabilityTypeOperation capabilityTypeOperation = Mockito.mock(CapabilityTypeOperation.class);

    @BeforeClass
    public static void beforeClass() {
        importManager = new ResourceImportManager(componentsUtils, capabilityTypeOperation);
        importManager.setAuditingManager(auditingManager);
        when(toscaOperationFacade.getLatestByToscaResourceName(Mockito.anyString())).thenReturn(Either.left(null));
        importManager.setResponseFormatManager(responseFormatManager);
        importManager.setResourceBusinessLogic(resourceBusinessLogic);
        importManager.setInterfaceOperationBusinessLogic(interfaceOperationBusinessLogic);
        importManager.setToscaOperationFacade(toscaOperationFacade);

        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);

        Configuration configuration = new Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
    }

    @Before
    public void beforeTest() {
        Mockito.reset(auditingManager, responseFormatManager, resourceBusinessLogic, userAdmin);
    }

    @Test
    public void testBasicResourceCreation() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();

        User user = new User();
        user.setUserId(resourceMD.getContactId());
        user.setRole("ADMIN");
        user.setFirstName("Jhon");
        user.setLastName("Doh");
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-blockStorage.yml");

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        Resource resource = createResource.left;

        testSetConstantMetaData(resource);
        testSetMetaDataFromJson(resource, resourceMD);

        testSetDerivedFrom(resource);
        testSetProperties(resource);

        Mockito.verify(resourceBusinessLogic, Mockito.times(1)).propagateStateToCertified(Mockito.eq(user), Mockito.eq(resource), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));
    }

    @Test()
    public void testResourceCreationFailed() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);
        ResponseFormat dummyResponseFormat = createGeneralErrorInfo();

        when(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(dummyResponseFormat);
        setResourceBusinessLogicMock();

        String jsonContent = "this is an invalid yml!";
        ComponentException errorInfoFromTest = null;
        try {
            importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        }catch (ComponentException e){
            errorInfoFromTest = e;
        }
        assertNotNull(errorInfoFromTest);
        assertEquals(errorInfoFromTest.getActionStatus(), ActionStatus.GENERAL_ERROR);

        Mockito.verify(resourceBusinessLogic, Mockito.times(0)).createOrUpdateResourceByImport(Mockito.any(Resource.class), Mockito.eq(user), Mockito.eq(true), Mockito.eq(false), Mockito.eq(true), Mockito.eq(null), Mockito.eq(null), Mockito.eq(false));
        Mockito.verify(resourceBusinessLogic, Mockito.times(0)).propagateStateToCertified(Mockito.eq(user), Mockito.any(Resource.class), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));
    }

    @Test
    public void testResourceCreationWithCapabilities() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-webServer.yml");

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        Resource resource = createResource.left;
        testSetCapabilities(resource);

        Mockito.verify(resourceBusinessLogic, Mockito.times(1)).propagateStateToCertified(Mockito.eq(user), Mockito.eq(resource), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));
        Mockito.verify(resourceBusinessLogic, Mockito.times(1)).createOrUpdateResourceByImport(resource, user, true, false, true, null, null, false);

    }

    @Test
    public void testResourceCreationWithRequirments() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-port.yml");

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        testSetRequirments(createResource.left);

    }
    
    @Test
    public void testResourceCreationWithInterfaceImplementation() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-interface-impl.yml");
        
        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
		interfaceDefinition.setOperations(operations );
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
		when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes()).thenReturn(Either.left(interfaceTypes));

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        testSetInterfaceImplementation(createResource.left);
    }
    
    @Test
    public void testResourceCreationWithInterfaceImplementation_UnknownInterface() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-unknown-interface-impl.yml");
        
        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
		interfaceDefinition.setOperations(operations );
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
		when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes()).thenReturn(Either.left(interfaceTypes));

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        assertNull(createResource.left.getInterfaces());
    }
    
    @Test
    public void testResourceCreationWitInterfaceImplementation_UnknownOperation() throws IOException {
        UploadResourceInfo resourceMD = createDummyResourceMD();
        User user = new User();
        user.setUserId(resourceMD.getContactId());
        when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(user);

        setResourceBusinessLogicMock();

        String jsonContent = ImportUtilsTest.loadCustomTypeFileNameToJsonString("custom-types-node-type-with-interface-impl-unknown-operation.yml");
        
        Map<String, InterfaceDefinition> interfaceTypes = new HashMap<>();
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.node.lifecycle.Standard");
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        operations.put("configure", new OperationDataDefinition());
		interfaceDefinition.setOperations(operations );
        interfaceTypes.put("tosca.interfaces.node.lifecycle.standard", interfaceDefinition);
		when(interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes()).thenReturn(Either.left(interfaceTypes));

        ImmutablePair<Resource, ActionStatus> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
        assertNull(createResource.left.getInterfaces());
    }

    private void setResourceBusinessLogicMock() {
        when(resourceBusinessLogic.getUserAdmin()).thenReturn(userAdmin);
        when(resourceBusinessLogic.createOrUpdateResourceByImport(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.eq(null), Mockito.eq(null), Mockito.eq(false)))
                .thenAnswer((Answer<ImmutablePair<Resource, ActionStatus>>) invocation -> {
                    Object[] args = invocation.getArguments();
                    return new ImmutablePair<>((Resource) args[0], ActionStatus.CREATED);

                });
        when(resourceBusinessLogic.propagateStateToCertified(Mockito.any(User.class), Mockito.any(Resource.class), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false)))
                .thenAnswer((Answer<Resource>) invocation -> {
                    Object[] args = invocation.getArguments();
                    return (Resource) args[1];

                });
        when(resourceBusinessLogic.createResourceByDao(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.any(AuditingActionEnum.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenAnswer((Answer<Either<Resource, ResponseFormat>>) invocation -> {
            Object[] args = invocation.getArguments();
            return Either.left((Resource) args[0]);

        });
        when(resourceBusinessLogic.validateResourceBeforeCreate(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.any(AuditingActionEnum.class), Mockito.eq(false), Mockito.eq(null))).thenAnswer((Answer<Either<Resource, ResponseFormat>>) invocation -> {
            Object[] args = invocation.getArguments();
            return Either.left((Resource) args[0]);

        });

        Boolean either = true;
        when(resourceBusinessLogic.validatePropertiesDefaultValues(Mockito.any(Resource.class))).thenReturn(either);
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
        resourceMD.setDescription("Represents a server-local block storage device (i.e., not shared) offering evenly sized blocks of data from which raw storage volumes can be created.");
        resourceMD.setResourceVendorModelNumber("vendorReleaseNumber");
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
        assertTrue(!propertyDefinition.isRequired());

        assertTrue(properties.containsKey("snapshot_id"));
        propertyDefinition = properties.get("snapshot_id");
        assertEquals("string", propertyDefinition.getType());
        assertTrue(!propertyDefinition.isRequired());

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

    private void testSetRequirments(Resource resource) {
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
    
    private void testSetInterfaceImplementation(Resource resource) {
    	Map<String, InterfaceDefinition> interfaces = resource.getInterfaces();
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.containsKey("Standard"));
        
        InterfaceDefinition interfaceDefinition = interfaces.get("Standard");
        assertEquals("tosca.interfaces.node.lifecycle.Standard", interfaceDefinition.getType());
        assertEquals("tosca.interfaces.node.lifecycle.standard", interfaceDefinition.getUniqueId());
        Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
        assertEquals(1, operations.size());

        OperationDataDefinition operation = operations.get("configure");
        assertEquals("\"camunda/vnfConfigure\"", operation.getImplementation().getArtifactName());

    }

    private void testSetDerivedFrom(Resource resource) {
        assertEquals(1, resource.getDerivedFrom().size());
        assertEquals("tosca.nodes.Root", resource.getDerivedFrom().get(0));

    }

    private void testSetMetaDataFromJson(Resource resource, UploadResourceInfo resourceMD) {

        // assertTrue( resource.getCategory().equals(resourceMD.getCategory())
        // );
        assertEquals(resource.getDescription(), resourceMD.getDescription());
        assertEquals(resource.getIcon(), resourceMD.getResourceIconPath());
        assertEquals(resource.getName(), resourceMD.getName());
        assertEquals(resource.getResourceVendorModelNumber(), resourceMD.getResourceVendorModelNumber());
        assertEquals(resource.getContactId(), resourceMD.getContactId());
        assertEquals(resource.getCreatorUserId(), resourceMD.getContactId());

        // assertTrue( resource.isAbstract() ==
        // Constants.ABSTRACT_CATEGORY.equals(resourceMD.getCategory()));

        assertEquals(resourceMD.getTags().size(), resource.getTags().size());
        for (String tag : resource.getTags()) {
            assertTrue(resourceMD.getTags().contains(tag));
        }

    }

    private void testSetConstantMetaData(Resource resource) {
        assertEquals(resource.getVersion(), TypeUtils.getFirstCertifiedVersionVersion());
        assertSame(resource.getLifecycleState(), ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE);
        assertEquals(resource.isHighestVersion(), ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);
        assertEquals(resource.getVendorName(), ImportUtils.Constants.VENDOR_NAME);
        assertEquals(resource.getVendorRelease(), ImportUtils.Constants.VENDOR_RELEASE);
    }

}
