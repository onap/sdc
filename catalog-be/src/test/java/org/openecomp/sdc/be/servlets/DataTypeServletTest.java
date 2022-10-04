/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.operations.impl.DataTypeOperation;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(MockitoExtension.class)
class DataTypeServletTest extends JerseySpringBaseTest {

    private static final String USER_ID = "cs0008";
    private static final String DATA_TYPE_UID = "ETSI SOL001 v2.5.1.tosca.datatypes.nfv.L3AddressData.datatype";
    private static final String PATH = "/v1/catalog/data-types/" + DATA_TYPE_UID;
    private static final String DATA_TYPE_PROPERTIES_PATH = "/v1/catalog/data-types/%s/properties";

    @InjectMocks
    private DataTypeServlet dataTypeServlet;
    private ComponentsUtils componentsUtils;
    private DataTypeOperation dataTypeOperation;
    private ServletContext servletContext;
    private WebApplicationContext webApplicationContext;
    private WebAppContextWrapper webAppContextWrapper;

    @Override
    protected ResourceConfig configure() {
        initMocks();
        MockitoAnnotations.openMocks(this);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return super.configure().register(dataTypeServlet);
    }

    private void initMocks() {
        componentsUtils = mock(ComponentsUtils.class);
        dataTypeOperation = mock(DataTypeOperation.class);
        servletContext = Mockito.mock(ServletContext.class);
        webApplicationContext = Mockito.mock(WebApplicationContext.class);
        webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    }

    @BeforeEach
    void before() throws Exception {
        super.setUp();
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void fetchDataTypeTest_Success() {
        final DataTypeDataDefinition expectedDataType = new DataTypeDataDefinition();
        expectedDataType.setUniqueId(DATA_TYPE_UID);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        when(dataTypeOperation.getDataTypeByUid(DATA_TYPE_UID)).thenReturn(Optional.of(expectedDataType));

        final Response response = target()
            .path(PATH)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        final DataTypeDataDefinition actualDataType = response.readEntity(DataTypeDataDefinition.class);
        assertEquals(expectedDataType.getUniqueId(), actualDataType.getUniqueId());
    }

    @Test
    void fetchDataTypeTest_Fail_OperationException() {
        when(dataTypeOperation.getDataTypeByUid(DATA_TYPE_UID)).thenThrow(OperationException.class);

        final Response response = target()
            .path(PATH)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void fetchDataTypeTest_Fail_EmptyOptional() {
        when(dataTypeOperation.getDataTypeByUid(DATA_TYPE_UID)).thenReturn(Optional.empty());

        final Response response = target()
            .path(PATH)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void fetchDataTypeTest_Fail_RuntimeException() {
        when(dataTypeOperation.getDataTypeByUid(DATA_TYPE_UID)).thenThrow(RuntimeException.class);

        final Response response = target()
            .path(PATH)
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void fetchDataTypePropertiesTest_Success() {
        final DataTypeDataDefinition expectedDataType = new DataTypeDataDefinition();
        final PropertyDefinition expectedProperty1 = new PropertyDefinition();
        expectedProperty1.setName("property1");
        final PropertyDefinition expectedProperty2 = new PropertyDefinition();
        expectedProperty2.setName("property2");
        expectedDataType.setUniqueId(DATA_TYPE_UID);
        when(dataTypeOperation.findAllProperties(DATA_TYPE_UID)).thenReturn(List.of(expectedProperty1, expectedProperty2));

        final Response response = target()
            .path(String.format(DATA_TYPE_PROPERTIES_PATH, DATA_TYPE_UID))
            .request(MediaType.APPLICATION_JSON)
            .header("USER_ID", USER_ID)
            .get(Response.class);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        final List<Map<String, Object>> actualResponse = response.readEntity(List.class);
        assertEquals(2, actualResponse.size());
        assertEquals(expectedProperty1.getName(), actualResponse.get(0).get("name"));
        assertEquals(expectedProperty2.getName(), actualResponse.get(1).get("name"));
    }

}
