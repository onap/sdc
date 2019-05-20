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
 */

package org.openecomp.sdc.be.components;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PropertyBusinessLogicTest {

    private static final Logger log = LoggerFactory.getLogger(PropertyBusinessLogicTest.class);
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
    IGraphLockOperation graphLockOperation;
    @Mock
    JanusGraphDao janusGraphDao;

    @InjectMocks
    private PropertyBusinessLogic bl = new PropertyBusinessLogic();
    private User user = null;
    private String resourceId = "resourceforproperty.0.1";
    private String serviceId = "serviceForProperty.0.1";
    private static final String interfaceType = "interfaceType";
    private static final String operationType = "operationType";
    private static final String operationId = "operationId";
    private static final String operationId2 = "operationId2";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ExternalConfiguration.setAppName("catalog-be");

        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        // User data and management
        user = new User();
        user.setUserId("jh003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        Either<User, ActionStatus> eitherGetUser = Either.left(user);
        when(mockUserAdmin.getUser("jh003", false)).thenReturn(eitherGetUser);
        when(userValidations.validateUserExists(eq("jh003"), anyString(), eq(false))).thenReturn(user);

        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.PROPERTY_OPERATION_MANAGER)).thenReturn(propertyOperation);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);


    }

    @Test
    public void getProperty_propertyNotFound() throws Exception {
        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");
        PropertyDefinition property2 = createPropertyObject("someProperty2", "myResource");
        resource.setProperties(Arrays.asList(property1, property2));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> nonExistingProperty = bl.getComponentProperty(resourceId, "NonExistingProperty", user.getUserId());
        assertTrue(nonExistingProperty.isRight());
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, "");
    }

    @Test
    public void getProperty_propertyNotBelongsToResource() throws Exception {
        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");
        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> notFoundProperty = bl.getComponentProperty(resourceId, "invalidId", user.getUserId());
        assertTrue(notFoundProperty.isRight());
        Mockito.verify(componentsUtils).getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, "");
    }

    @Test
    public void getProperty() throws Exception {
        Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        resource.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> foundProperty = bl.getComponentProperty(resourceId, property1.getUniqueId(), user.getUserId());
        assertTrue(foundProperty.isLeft());
        assertEquals(foundProperty.left().value().getValue().getUniqueId(), property1.getUniqueId());
    }

    @Test
    public void testGetPropertyFromService() {
        Service service = new Service();
        service.setUniqueId(serviceId);

        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        service.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.left(service));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> serviceProperty =
            bl.getComponentProperty(serviceId, property1.getUniqueId(), user.getUserId());

        assertTrue(serviceProperty.isLeft());
        assertEquals(serviceProperty.left().value().getValue().getUniqueId(), property1.getUniqueId());
    }

    @Test
    public void testPropertyNotFoundOnService() {
        Service service = new Service();
        service.setUniqueId(serviceId);

        PropertyDefinition property1 = createPropertyObject("someProperty", null);
        service.setProperties(Arrays.asList(property1));

        Mockito.when(toscaOperationFacade.getToscaElement(serviceId)).thenReturn(Either.left(service));
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> serviceProperty =
            bl.getComponentProperty(serviceId, "notExistingPropId", user.getUserId());

        assertTrue(serviceProperty.isRight());
    }

    @Test
    public void isPropertyUsedByComponentInterface(){
        Service service = new Service();
        service.setUniqueId(serviceId);
        service.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType, operationId, operationType));

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        assertTrue(bl.isPropertyUsedByOperation(service, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(bl.isPropertyUsedByOperation(service, propDef2));
    }

    @Test
    public void isPropertyUsedByComponentInstanceInterface(){
        Map<String, InterfaceDefinition> newInterfaceDefinition = InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType, operationId, operationType);
        ComponentInstanceInterface componentInstanceInterface = new ComponentInstanceInterface(interfaceType, newInterfaceDefinition.get(interfaceType));

        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = new HashMap<>();
        componentInstanceInterfaces.put("Test", Arrays.asList(componentInstanceInterface));

        Service service = new Service();
        service.setUniqueId(serviceId);
        service.setComponentInstancesInterfaces(componentInstanceInterfaces);

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        assertTrue(bl.isPropertyUsedByOperation(service, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(bl.isPropertyUsedByOperation(service, propDef2));
    }

    @Test
    public void isPropertyUsedByComponentParentComponentInstanceInterface(){
        Map<String, InterfaceDefinition> newInterfaceDefinition = InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceType, operationId, operationType);
        ComponentInstanceInterface componentInstanceInterface = new ComponentInstanceInterface(interfaceType, newInterfaceDefinition.get(interfaceType));

        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = new HashMap<>();
        componentInstanceInterfaces.put("Test", Arrays.asList(componentInstanceInterface));

        Service parentService = new Service();
        parentService.setComponentInstancesInterfaces(componentInstanceInterfaces);
        Service childService = new Service();
        childService.setUniqueId(serviceId);

        PropertyDefinition propDef1 = new PropertyDefinition();
        propDef1.setUniqueId("ComponentInput1_uniqueId");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(Arrays.asList(parentService)));
        assertTrue(bl.isPropertyUsedByOperation(childService, propDef1));

        PropertyDefinition propDef2 = new PropertyDefinition();
        propDef1.setUniqueId("inputId2");
        Mockito.when(toscaOperationFacade.getParentComponents(serviceId)).thenReturn(Either.left(new ArrayList<>()));
        assertFalse(bl.isPropertyUsedByOperation(childService, propDef2));
    }


    private PropertyDefinition createPropertyObject(String propertyName, String resourceId) {
        PropertyDefinition pd = new PropertyDefinition();
        pd.setConstraints(null);
        pd.setDefaultValue("100");
        pd.setDescription("Size of thasdasdasdasde local disk, in Gigabytes (GB), available to applications running on the Compute node");
        pd.setPassword(false);
        pd.setRequired(true);
        pd.setType("Integer");
        pd.setOwnerId(resourceId);
        pd.setUniqueId(resourceId + "." + propertyName);
        return pd;
    }

    @Test
    public void deleteProperty_CONNECTION_FAILURE() {
        StorageOperationStatus lockResult = StorageOperationStatus.CONNECTION_FAILURE;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);
        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(new Resource()));
        assertTrue(bl.deletePropertyFromComponent("resourceforproperty.0.1", "someProperty","i726").isRight());
    }

    @Test
    public void deleteProperty_RESOURCE_NOT_FOUND() throws Exception {

        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");

        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Field baseBusinessLogic3;
        baseBusinessLogic3 = bl.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(bl, janusGraphDao);


        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        StorageOperationStatus lockResult = StorageOperationStatus.OK;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);
        //doNothing().when(janusGraphDao).commit();

        Either<PropertyDefinition, ResponseFormat> result;

        Component resourcereturn= new Resource();
        resourcereturn.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resourcereturn.setIsDeleted(false);
        resourcereturn.setLastUpdaterUserId("USR01");

        Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);


        assertTrue(bl.deletePropertyFromComponent("RES01", "someProperty","i726").isRight());
    }

    @Test
    public void deleteProperty_RESTRICTED_OPERATION() throws Exception {

        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("someProperty", "someResource");

        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Field baseBusinessLogic3;
        baseBusinessLogic3 = bl.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(bl, janusGraphDao);


        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        StorageOperationStatus lockResult = StorageOperationStatus.OK;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);
        //doNothing().when(janusGraphDao).commit();

        Either<PropertyDefinition, ResponseFormat> result;

        Component resourcereturn= new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);


        assertTrue(bl.deletePropertyFromComponent("RES01", "someProperty","i726").isRight());
    }

    @Test
    public void deleteProperty_RESTRICTED_() throws Exception {

        Resource resource = new Resource();
        PropertyDefinition property1 = createPropertyObject("PROP", "RES01");
        property1.setUniqueId("PROP");
        resource.setProperties(Arrays.asList(property1));
        String resourceId = "myResource";
        resource.setUniqueId(resourceId);

        Field baseBusinessLogic3;
        baseBusinessLogic3 = bl.getClass().getSuperclass().getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(bl, janusGraphDao);


        Mockito.when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        StorageOperationStatus lockResult = StorageOperationStatus.OK;
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(lockResult);
        //doNothing().when(janusGraphDao).commit();

        Either<PropertyDefinition, ResponseFormat> result;

        Component resourcereturn= new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        Either<Component, StorageOperationStatus> toscastatus=Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
        when(toscaOperationFacade.deletePropertyOfComponent(anyObject(),anyString())).thenReturn(StorageOperationStatus.OK);
        when(toscaOperationFacade.getParentComponents(anyString())).thenReturn(Either.left(new ArrayList<>()));

        assertTrue(bl.deletePropertyFromComponent("RES01", "PROP","USR01").isRight());
    }
}
