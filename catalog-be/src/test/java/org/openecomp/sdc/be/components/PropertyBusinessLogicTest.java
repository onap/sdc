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
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @InjectMocks
    private PropertyBusinessLogic bl = new PropertyBusinessLogic();
    private User user = null;
    private String resourceId = "resourceforproperty.0.1";

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
        when(userValidations.validateUserExists(eq("jh003"), anyString(), eq(false))).thenReturn(Either.left(user));

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
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> nonExistingProperty = bl.getProperty(resourceId, "NonExistingProperty", user.getUserId());
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
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> notFoundProperty = bl.getProperty(resourceId, "invalidId", user.getUserId());
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
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> foundProperty = bl.getProperty(resourceId, property1.getUniqueId(), user.getUserId());
        assertTrue(foundProperty.isLeft());
        assertEquals(foundProperty.left().value().getValue().getUniqueId(), property1.getUniqueId());
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
}
