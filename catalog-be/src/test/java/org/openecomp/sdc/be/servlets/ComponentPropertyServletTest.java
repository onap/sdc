/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(MockitoExtension.class)
class ComponentPropertyServletTest extends JerseySpringBaseTest {

    @Mock
    private static HttpSession session;
    @Mock
    private static ServletContext context;
    @Mock
    private static WebAppContextWrapper wrapper;
    @Mock
    private static WebApplicationContext webAppContext;
    @Mock
    private static PropertyBusinessLogic propertyBl;
    @Mock
    private static ComponentsUtils componentsUtils;
    @InjectMocks
    @Spy
    private ComponentPropertyServlet componentPropertyServlet;

    private static final String SERVICE_ID = "service1";
    private static final String USER_ID = "jh0003";
    private static final String VALID_PROPERTY_NAME = "valid_name_123";
    private static final String INVALID_PROPERTY_NAME = "invalid_name_$.&";
    private static final String STRING_TYPE = "string";

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
        when(request.getSession()).thenReturn(session);
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void testCreatePropertyOnService_success() {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(VALID_PROPERTY_NAME);
        property.setType(STRING_TYPE);

        EntryData<String, PropertyDefinition> propertyEntry = new EntryData<>(VALID_PROPERTY_NAME, property);
        when(propertyBl.addPropertyToComponent(eq(SERVICE_ID), any(), any())).thenReturn(Either.left(propertyEntry));

        Response propertyInService =
            componentPropertyServlet.createPropertyInService(SERVICE_ID, getValidProperty(), request, USER_ID);

        Assert.assertEquals(HttpStatus.OK_200.getStatusCode(), propertyInService.getStatus());
    }

    @Test
    void testCreatePropertyInvalidName_failure() {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(INVALID_PROPERTY_NAME);
        property.setType(STRING_TYPE);

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.BAD_REQUEST_400.getStatusCode());

        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_PROPERTY_NAME))).thenReturn(responseFormat);
        when(session.getServletContext()).thenReturn(context);
        when(context.getAttribute(eq(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))).thenReturn(wrapper);
        when(wrapper.getWebAppContext(any())).thenReturn(webAppContext);
        when(webAppContext.getBean(eq(ComponentsUtils.class))).thenReturn(componentsUtils);

        Response propertyInService =
            componentPropertyServlet.createPropertyInService(SERVICE_ID, getInvalidProperty(), request, USER_ID);

        Assert.assertEquals(HttpStatus.BAD_REQUEST_400.getStatusCode(), propertyInService.getStatus());
    }

    private String getValidProperty() {
        return "{\n"
            + "  \"valid_name_123\": {\n"
            + "    \"schema\": {\n"
            + "      \"property\": {\n"
            + "        \"type\": \"\"\n"
            + "      }\n" + "    },\n"
            + "    \"type\": \"string\",\n"
            + "    \"name\": \"valid_name_123\"\n"
            + "  }\n"
            + "}";
    }

    private String getInvalidProperty() {
        return "{\n"
            + "  \"invalid_name_$.&\": {\n"
            + "    \"schema\": {\n"
            + "      \"property\": {\n"
            + "        \"type\": \"\"\n"
            + "      }\n" + "    },\n"
            + "    \"type\": \"string\",\n"
            + "    \"name\": \"invalid_name_$.&\"\n"
            + "  }\n"
            + "}";
    }

}
