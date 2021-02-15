/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
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
import org.openecomp.sdc.be.components.impl.AttributeBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(MockitoExtension.class)
class ComponentAttributeServletTest extends JerseySpringBaseTest {

    @Mock
    private ServletContext context;
    @Mock
    private WebAppContextWrapper wrapper;
    @Mock
    private WebApplicationContext webAppContext;
    @Mock
    private HttpSession session;
    @Mock
    private AttributeBusinessLogic attributeBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @InjectMocks
    @Spy
    private ComponentAttributeServlet componentAttributeServlet;

    private static final String SERVICE_ID = "service1";
    private static final String RESOURCE_ID = "resource1";
    private static final String USER_ID = "jh0003";
    private static final String VALID_PROPERTY_NAME = "valid_name_123";
    private static final String INVALID_PROPERTY_NAME = "invalid_name_$.&";
    private static final String STRING_TYPE = "string";

    @BeforeEach
    public void initClass() throws Exception {
        super.setUp();
        when(request.getSession()).thenReturn(session);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void getAttributeListInService_success() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName(VALID_PROPERTY_NAME);
        attributeDefinition.setType(STRING_TYPE);

        List<AttributeDefinition> attributeDefinitionEntryData = Arrays.asList(attributeDefinition);
        when(attributeBusinessLogic.getAttributesList(any(), any())).thenReturn(Either.left(attributeDefinitionEntryData));

        Response attributeInService = componentAttributeServlet.getAttributeListInService(SERVICE_ID, request, USER_ID);

        Assert.assertEquals(HttpStatus.OK_200.getStatusCode(), attributeInService.getStatus());
    }

    @Test
    void getAttributeListInService_fail() {
        when(attributeBusinessLogic.getAttributesList(any(), any()))
            .thenReturn(Either.right(new ResponseFormat(Response.Status.NOT_FOUND.getStatusCode())));

        Response attributeInService = componentAttributeServlet.getAttributeListInService(SERVICE_ID, request, USER_ID);

        Assertions.assertThat(attributeInService).isNotNull();
        Assertions.assertThat(attributeInService.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getAttributeListInResource_success() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName(VALID_PROPERTY_NAME);
        attributeDefinition.setType(STRING_TYPE);

        List<AttributeDefinition> attributeDefinitionEntryData = Arrays.asList(attributeDefinition);
        when(attributeBusinessLogic.getAttributesList(any(), any())).thenReturn(Either.left(attributeDefinitionEntryData));
        when(attributeBusinessLogic.getAttributesList(any(), any())).thenReturn(Either.left(attributeDefinitionEntryData));

        Response attributeInService =
            componentAttributeServlet.getAttributeListInResource(RESOURCE_ID, request, USER_ID);

        Assert.assertEquals(HttpStatus.OK_200.getStatusCode(), attributeInService.getStatus());
    }

    @Test
    void getAttributeListInResource_fail() {
        when(session.getServletContext()).thenReturn(context);
        when(context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(wrapper);
        when(wrapper.getWebAppContext(any())).thenReturn(webAppContext);
        when(webAppContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
        when(attributeBusinessLogic.getAttributesList(any(), any())).thenThrow(new RuntimeException());

        ResponseFormat responseFormat = new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode());

        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(responseFormat);

        Response attributeInService = componentAttributeServlet.getAttributeListInResource(SERVICE_ID, request, USER_ID);

        Assertions.assertThat(attributeInService).isNotNull();
        Assertions.assertThat(attributeInService.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

}
