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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogicMock;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ToscaOperationException;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;
import org.springframework.web.context.WebApplicationContext;

class PropertyBusinessLogicTest extends BaseBusinessLogicMock {

    @Mock
    private ServletContext servletContext;
    @Mock
    private IPropertyOperation propertyOperation;
    @Mock
    private WebAppContextWrapper webAppContextWrapper;
    @Mock
    private UserBusinessLogic mockUserAdmin;
    @Mock
    private WebApplicationContext webAppContext;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private UserValidations userValidations;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private JanusGraphDao janusGraphDao;

    @InjectMocks
    private PropertyBusinessLogic propertyBusinessLogic = new PropertyBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
        groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
    private User user = null;
    private String resourceId = "resourceforproperty.0.1";
    private String serviceId = "serviceForProperty.0.1";
    private static final String interfaceType = "interfaceType";
    private static final String operationType = "operationType";
    private static final String operationId = "operationId";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ExternalConfiguration.setAppName("catalog-be");

        // User data and management
        user = new User();
        user.setUserId("jh003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        when(mockUserAdmin.getUser("jh003", false)).thenReturn(user);
        when(userValidations.validateUserExists("jh003")).thenReturn(user);

        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.PROPERTY_OPERATION_MANAGER)).thenReturn(propertyOperation);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
    }

    @Test
    void getProperty_propertyNotFound() throws Exception {
        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");
        PropertyDefinition property2 = createPropertyObject("someProperty2", "myResource");
        resource.setProperties(Arrays.asList(property1, property2));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> nonExistingProperty = propertyBusinessLogic
            .getComponentProperty(resourceId, "NonExistingProperty", user.getUserId());
        assertTrue(nonExistingProperty.isRight());
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, "");
    }

    @Test
    void getProperty_propertyNotBelongsToResource() throws Exception {
        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");
        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> notFoundProperty = propertyBusinessLogic
            .getComponentProperty(resourceId, "invalidId", user.getUserId());
        assertTrue(notFoundProperty.isRight());
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, "");
    }

    @Test
    void getProperty() throws Exception {
        Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        resource.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> foundProperty = propertyBusinessLogic
            .getComponentProperty(resourceId, property1.getUniqueId(), user.getUserId());
        assertTrue(foundProperty.isLeft());
        assertEquals(foundProperty.left().value().getValue().getUniqueId(), property1.getUniqueId());
    }

    @Test
    void testGetPropertyFromService() {
        Service service = new Service();
        service.setUniqueId(serviceId);

        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        service.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.left(service));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> serviceProperty =
            propertyBusinessLogic.getComponentProperty(serviceId, property1.getUniqueId(), user.getUserId());

        assertTrue(serviceProperty.isLeft());
        assertEquals(serviceProperty.left().value().getValue().getUniqueId(), property1.getUniqueId());
    }

    @Test
    void testPropertyNotFoundOnService() {
        Service service = new Service();
        service.setUniqueId(serviceId);

        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        service.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.left(service));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> serviceProperty =
            propertyBusinessLogic.getComponentProperty(serviceId, "notExistingPropId", user.getUserId());

        assertTrue(serviceProperty.isRight());
    }

    @Test
    void isPropertyUsedByComponentInterface() {
        Service service = new Service();
        service.setUniqueId(serviceId);
        service.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType, operationId, operationType));

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        assertTrue(propertyBusinessLogic.isPropertyUsedByOperation(service, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(propertyBusinessLogic.isPropertyUsedByOperation(service, propDef2));
    }

    @Test
    void isPropertyUsedByComponentInstanceInterface() {
        Map<String, InterfaceDefinition> newInterfaceDefinition = InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType,
            operationId, operationType);
        ComponentInstanceInterface componentInstanceInterface = new ComponentInstanceInterface(interfaceType,
            newInterfaceDefinition.get(interfaceType));

        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = new HashMap<>();
        componentInstanceInterfaces.put("Test", Arrays.asList(componentInstanceInterface));

        Service service = new Service();
        service.setUniqueId(serviceId);
        service.setComponentInstancesInterfaces(componentInstanceInterfaces);

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        assertTrue(propertyBusinessLogic.isPropertyUsedByOperation(service, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(propertyBusinessLogic.isPropertyUsedByOperation(service, propDef2));
    }

    @Test
    void isPropertyUsedByComponentParentComponentInstanceInterface() {
        Map<String, InterfaceDefinition> newInterfaceDefinition = InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType,
            operationId, operationType);
        ComponentInstanceInterface componentInstanceInterface = new ComponentInstanceInterface(interfaceType,
            newInterfaceDefinition.get(interfaceType));

        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = new HashMap<>();
        componentInstanceInterfaces.put("Test", Arrays.asList(componentInstanceInterface));

        Service parentService = new Service();
        parentService.setComponentInstancesInterfaces(componentInstanceInterfaces);
        Service childService = new Service();
        childService.setUniqueId(serviceId);

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(Arrays.asList(parentService)));
        assertTrue(propertyBusinessLogic.isPropertyUsedByOperation(childService, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(propertyBusinessLogic.isPropertyUsedByOperation(childService, propDef2));
    }

    private PropertyDefinition createPropertyObject(final String propertyName, final String resourceId) {
        final PropertyDefinition pd = new PropertyDefinition();
        List<PropertyConstraint> constraints = new ArrayList<>();
        pd.setConstraints(null);
        pd.setDefaultValue("100");
        pd.setDescription("Size of thasdasdasdasde local disk, in Gigabytes (GB), available to applications running on the Compute node");
        pd.setPassword(false);
        pd.setRequired(true);
        pd.setType("Integer");
        pd.setOwnerId(resourceId);
        pd.setName(propertyName);
        pd.setUniqueId(resourceId + "." + propertyName);
        return pd;
    }

    @Test
    void deleteProperty_CONNECTION_FAILURE() {
        StorageOperationStatus lockResult = StorageOperationStatus.CONNECTION_FAILURE;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(new Resource()));
        assertTrue(propertyBusinessLogic.deletePropertyFromComponent("resourceforproperty.0.1", "someProperty", "i726").isRight());
    }

    @Test
    void deleteProperty_RESOURCE_NOT_FOUND() throws Exception {

        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");

        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Field baseBusinessLogic3;
        baseBusinessLogic3 = propertyBusinessLogic.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(propertyBusinessLogic, janusGraphDao);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        StorageOperationStatus lockResult = StorageOperationStatus.OK;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);

        Component resourcereturn = new Resource();
        resourcereturn.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resourcereturn.setIsDeleted(false);
        resourcereturn.setLastUpdaterUserId("USR01");

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        assertTrue(propertyBusinessLogic.deletePropertyFromComponent("RES01", "someProperty", "i726").isRight());
    }

    @Test
    void deleteProperty_RESTRICTED_OPERATION() throws Exception {
        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");

        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Field baseBusinessLogic3;
        baseBusinessLogic3 = propertyBusinessLogic.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(propertyBusinessLogic, janusGraphDao);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        StorageOperationStatus lockResult = StorageOperationStatus.OK;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        assertTrue(propertyBusinessLogic.deletePropertyFromComponent("RES01", "someProperty", "i726").isRight());
    }

    @Test
    void deleteProperty_RESTRICTED_() throws Exception {
        final PropertyDefinition property1 = createPropertyObject("PROP", "RES01");
        final Resource resource = new Resource();
        final String resourceId = "myResource";
        resource.setUniqueId(resourceId);
        resource.setProperties(Arrays.asList(property1));

        final Field baseBusinessLogic3 =
            propertyBusinessLogic.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(propertyBusinessLogic, janusGraphDao);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(Either.left(resource));
        when(toscaOperationFacade.deletePropertyOfComponent(anyObject(), anyString())).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.getParentComponents(anyString())).thenReturn(Either.left(new ArrayList<>()));

        assertTrue(propertyBusinessLogic.deletePropertyFromComponent("RES01", "PROP", "USR01").isRight());
    }

    @Test
    void findComponentByIdTest() throws BusinessLogicException {
        //give
        final Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        //when
        final Component actualResource = propertyBusinessLogic.findComponentById(resourceId).orElse(null);
        //then
        assertThat("Actual resource should not be null", actualResource, is(notNullValue()));
        assertThat("Actual resource must have the expected id",
            actualResource.getUniqueId(), is(equalTo(resource.getUniqueId())));
    }

    @Test
    void findComponentById_resourceNotFoundTest() throws BusinessLogicException {
        //given
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.right(null));
        Mockito.when(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, "")).thenReturn(new ResponseFormat());
        //when
        assertThrows(BusinessLogicException.class, () -> {
            propertyBusinessLogic.findComponentById(resourceId);
        });
    }

    @Test
    void updateComponentPropertyTest() throws BusinessLogicException {
        //given
        final Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        final PropertyDefinition propertyDefinition = createPropertyObject("testProperty", resourceId);
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        when(toscaOperationFacade.updatePropertyOfComponent(resource, propertyDefinition)).thenReturn(Either.left(propertyDefinition));
        //when
        final PropertyDefinition actualPropertyDefinition = propertyBusinessLogic
            .updateComponentProperty(resourceId, propertyDefinition);
        //then
        assertThat("Actual property definition should not be null", actualPropertyDefinition, is(notNullValue()));
        assertThat("Actual property definition must have the expected id",
            actualPropertyDefinition.getOwnerId(), is(equalTo(resource.getUniqueId())));
        assertThat("Actual property definition must have the expected id",
            actualPropertyDefinition.getName(), is(equalTo(propertyDefinition.getName())));
    }

    @Test
    void updateComponentProperty_updateFailedTest() throws BusinessLogicException {
        //given
        final Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        final PropertyDefinition propertyDefinition = createPropertyObject("testProperty", resourceId);
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        when(toscaOperationFacade.updatePropertyOfComponent(resource, propertyDefinition)).thenReturn(Either.right(null));
        when(componentsUtils.getResponseFormatByResource(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseFormat());
        when(componentsUtils.convertFromStorageResponse(Mockito.any())).thenReturn(null);
        //when
        assertThrows(BusinessLogicException.class, () -> {
            propertyBusinessLogic.updateComponentProperty(resourceId, propertyDefinition);
        });
    }

    @Test
    void copyPropertyToComponentTest() throws ToscaOperationException {
        //given
        final Resource expectedResource = new Resource();
        expectedResource.setUniqueId(resourceId);
        final List<PropertyDefinition> propertiesToCopyList = new ArrayList<>();
        final PropertyDefinition property1 = createPropertyObject("property1", resourceId);
        propertiesToCopyList.add(property1);
        final PropertyDefinition property2 = createPropertyObject("property2", resourceId);
        propertiesToCopyList.add(property2);

        final PropertyDefinition copiedProperty1 = new PropertyDefinition(property1);
        copiedProperty1.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, copiedProperty1.getName()));
        expectedResource.addProperty(copiedProperty1);
        final PropertyDefinition copiedProperty2 = new PropertyDefinition(property2);
        copiedProperty2.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, copiedProperty2.getName()));
        expectedResource.addProperty(copiedProperty2);

        Mockito.when(toscaOperationFacade
                .addPropertyToComponent(Mockito.any(PropertyDefinition.class), eq(expectedResource)))
            .thenReturn(Either.left(copiedProperty1));
        Mockito.when(toscaOperationFacade
                .addPropertyToComponent(Mockito.any(PropertyDefinition.class), eq(expectedResource)))
            .thenReturn(Either.left(copiedProperty2));
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(expectedResource));
        //when
        final Component actualComponent = propertyBusinessLogic.copyPropertyToComponent(expectedResource, propertiesToCopyList, true);
        //then
        assertThat("Actual component should not be null", actualComponent, is(notNullValue()));
        assertThat("Actual component should be an instance of Resource", actualComponent, is(instanceOf(Resource.class)));
        assertThat("Actual component should have the expected id", actualComponent.getUniqueId(), is(equalTo(expectedResource.getUniqueId())));
        assertThat("Actual component should have 2 properties", actualComponent.getProperties(), hasSize(2));
        assertThat("Actual component should have the expected properties", actualComponent.getProperties(),
            hasItems(copiedProperty1, copiedProperty2));
    }

    @Test
    void copyPropertyToComponent1() throws ToscaOperationException {
        //given
        final Resource expectedResource = new Resource();
        expectedResource.setUniqueId(resourceId);
        //when
        final Component actualComponent = propertyBusinessLogic.copyPropertyToComponent(expectedResource, null);
        //then
        assertThat("Actual component should not be null", actualComponent, is(notNullValue()));
        assertThat("Actual component should be an instance of Resource", actualComponent, is(instanceOf(Resource.class)));
        assertThat("Actual component should have the expected id", actualComponent.getUniqueId(), is(equalTo(expectedResource.getUniqueId())));
        assertThat("Actual component should have no properties", actualComponent.getProperties(), is(nullValue()));
    }

    @Test
    void copyPropertyToComponent_copyFailed() throws ToscaOperationException {
        //given
        final Resource expectedResource = new Resource();
        expectedResource.setUniqueId(resourceId);
        final List<PropertyDefinition> propertiesToCopyList = new ArrayList<>();
        final PropertyDefinition property1 = createPropertyObject("property1", resourceId);
        propertiesToCopyList.add(property1);
        Mockito.when(toscaOperationFacade
                .addPropertyToComponent(Mockito.any(PropertyDefinition.class), eq(expectedResource)))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(expectedResource));
        //when
        assertThrows(ToscaOperationException.class, () -> {
            propertyBusinessLogic.copyPropertyToComponent(expectedResource, propertiesToCopyList, true);
        });
    }
}
