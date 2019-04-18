/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

import fj.data.Either;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openecomp.sdc.be.components.impl.DataTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class InputsServletTest extends JerseySpringBaseTest {

    /* Constants */
    private static final String RESOURCE_ID = "serviceId";
    private static final String USER_ID = "userId";
    private static final String COMPONENT_INSTANCE_ID = "instanceId";
    private static final String COMPONENT_ID = "componentId";
    private static final String INSTANCE_INPUT_ID = "inputId";
    private static final String LISTINPUT_NAME = "listInput";
    private static final String LISTINPUT_SCHEMA_TYPE = "org.onap.datatypes.listinput";
    private static final String LISTINPUT_PROP1_NAME = "prop1";
    private static final String LISTINPUT_PROP1_TYPE = "string";
    private static final String LISTINPUT_PROP2_NAME = "prop2";
    private static final String LISTINPUT_PROP2_TYPE = "integer";

    /* Test subject */
    private InputsServletForTest testSubject;

    /* Mocks */
    private InputsBusinessLogic inputsBusinessLogic;
    private DataTypeBusinessLogic dataTypeBusinessLogic;
    private HttpSession httpSession;
    private ServletContext servletContext;
    private WebApplicationContext webApplicationContext;
    private ComponentsUtils componentsUtils;
    private ServletUtils servletUtils;

    /**
     * This class extends the original InputsServlet
     * and provides methods to inject mocks
     */
    class InputsServletForTest extends InputsServlet {
        public void setComponentsUtils(ComponentsUtils componentsUtils) {
            this.componentsUtils = componentsUtils;
        }

        public void setServletUtils(ServletUtils servletUtils) {
            this.servletUtils = servletUtils;
        }
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        inputsBusinessLogic = mock(InputsBusinessLogic.class);
        dataTypeBusinessLogic = mock(DataTypeBusinessLogic.class);
        servletContext = mock(ServletContext.class);
        httpSession = mock(HttpSession.class);
        webApplicationContext = mock(WebApplicationContext.class);
        componentsUtils = mock(ComponentsUtils.class);
        servletUtils = mock(ServletUtils.class);
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(new WebAppContextWrapper());
        when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(InputsBusinessLogic.class)).thenReturn(inputsBusinessLogic);
        when(webApplicationContext.getBean(DataTypeBusinessLogic.class)).thenReturn(dataTypeBusinessLogic);
        testSubject.setComponentsUtils(componentsUtils);
        testSubject.setServletUtils(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
    }


    @Override
    protected ResourceConfig configure() {
        testSubject = new InputsServletForTest();
        return super.configure().register(testSubject);
    }


    private InputDefinition setUpListInput() {
        InputDefinition listInput = new InputDefinition();
        listInput.setName(LISTINPUT_NAME);
        listInput.setType("list");
        SchemaDefinition listInputSchema = new SchemaDefinition();
        listInputSchema.setProperty(new PropertyDataDefinitionBuilder()
                .setType(LISTINPUT_SCHEMA_TYPE)
                .setIsRequired(false)
                .build()
        );
        listInput.setSchema(listInputSchema);
        return listInput;
    }

    private ComponentInstListInput setUpCreateListInputParams() {
        ComponentInstListInput componentInstListInput = new ComponentInstListInput();

        // Create a "list input"
        InputDefinition listInput = setUpListInput();
        componentInstListInput.setListInput(listInput);

        // Create ComponentInstancePropInputs
        // for inputs in the ComponentInstance
        Map<String, List<ComponentInstancePropInput>> propInputsListMap = new HashMap<>();
        // Add 2 PropInputs. property owner is COMPONENT_INSTANCE_ID
        List<ComponentInstancePropInput> propInputsList = new ArrayList<>();
        ComponentInstancePropInput propInput = new ComponentInstancePropInput();
        propInput.setName(LISTINPUT_PROP1_NAME);
        propInput.setType(LISTINPUT_PROP1_TYPE);
        propInput.setUniqueId(COMPONENT_INSTANCE_ID + "." + LISTINPUT_PROP1_NAME);
        propInputsList.add(propInput);
        propInput = new ComponentInstancePropInput();
        propInput.setName(LISTINPUT_PROP2_NAME);
        propInput.setType(LISTINPUT_PROP2_TYPE);
        propInput.setUniqueId(COMPONENT_INSTANCE_ID + "." + LISTINPUT_PROP2_NAME);
        propInputsList.add(propInput);
        propInputsListMap.put(COMPONENT_INSTANCE_ID, propInputsList);
        ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
        componentInstInputsMap.setComponentInstanceInputsMap(propInputsListMap);
        componentInstListInput.setComponentInstInputsMap(componentInstInputsMap);

        return componentInstListInput;
    }

    @Test
    public void test_createListInput_success() throws Exception {
        ComponentInstListInput requestBodyObj = setUpCreateListInputParams();
        Entity<ComponentInstListInput> entity = Entity.entity(requestBodyObj, MediaType.APPLICATION_JSON);

        // for parseToComponentInstListInput
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(any(), any(), eq(ComponentInstListInput.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.SERVICE)))
                .thenReturn(Either.left(requestBodyObj));

        when(inputsBusinessLogic.createListInput(eq(USER_ID), eq(RESOURCE_ID), eq(ComponentTypeEnum.SERVICE),
                any(), eq(true), eq(false)))
                .thenReturn(Either.left(Collections.emptyList()));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));

        Response response = buildCreateListInputCall().post(entity);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
        verify(inputsBusinessLogic, times(1)).createListInput(USER_ID, RESOURCE_ID,
                ComponentTypeEnum.SERVICE, requestBodyObj, true, false);
    }

    @Test
    public void test_createListInput_fail_parse() throws Exception {
        ComponentInstListInput requestBodyObj = setUpCreateListInputParams();
        Entity<ComponentInstListInput> entity = Entity.entity(requestBodyObj, MediaType.APPLICATION_JSON);

        // for parseToComponentInstListInput
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(any(), userCaptor.capture(), eq(ComponentInstListInput.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.SERVICE)))
                .thenReturn(Either.right(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode())));

        when(inputsBusinessLogic.createListInput(eq(USER_ID), eq(RESOURCE_ID), eq(ComponentTypeEnum.SERVICE),
                any(), eq(true), eq(false)))
                .thenReturn(Either.left(Collections.emptyList()));

        Response response = buildCreateListInputCall().post(entity);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, times(1))
                .convertJsonToObjectUsingObjectMapper(any(), any(), eq(ComponentInstListInput.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.SERVICE));
        assertThat(userCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        verify(inputsBusinessLogic, never()).createListInput(USER_ID, RESOURCE_ID,
                ComponentTypeEnum.SERVICE, requestBodyObj, true, false);
    }


    @Test
    public void test_createListInput_fail_createInput() throws Exception {
        ComponentInstListInput requestBodyObj = setUpCreateListInputParams();
        Entity<ComponentInstListInput> entity = Entity.entity(requestBodyObj, MediaType.APPLICATION_JSON);

        // for parseToComponentInstListInput
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(any(), userCaptor.capture(), eq(ComponentInstListInput.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.SERVICE)))
                .thenReturn(Either.left(requestBodyObj));

        when(inputsBusinessLogic.createListInput(eq(USER_ID), eq(RESOURCE_ID), eq(ComponentTypeEnum.SERVICE),
                any(), eq(true), eq(false)))
                .thenReturn(Either.right(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode())));

        Response response = buildCreateListInputCall().post(entity);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(inputsBusinessLogic, times(1))
                .createListInput(eq(USER_ID), eq(RESOURCE_ID), eq(ComponentTypeEnum.SERVICE),
                        any(), eq(true), eq(false));
    }


    @Test
    public void test_createListInput_fail_exception() throws Exception {
        ComponentInstListInput requestBodyObj = setUpCreateListInputParams();
        Entity<ComponentInstListInput> entity = Entity.entity(requestBodyObj, MediaType.APPLICATION_JSON);

        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode()));

        Response response = buildCreateListInputCall().post(entity);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void test_getDataType_success() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataType(eq(RESOURCE_ID),eq(LISTINPUT_SCHEMA_TYPE))).thenReturn(Either.left(new DataTypeDefinition()));

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.OK_200.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK))).thenReturn(responseFormat);

        Response response = buildGetDataTypeCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void test_getDataType_fail() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataType(eq(RESOURCE_ID),eq(LISTINPUT_SCHEMA_TYPE))).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.BAD_REQUEST))).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR))).thenReturn(responseFormat);

        Response response = buildGetDataTypeCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
    }

    @Test
    public void test_getDataType_fail_exception() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataType(eq(RESOURCE_ID),eq(LISTINPUT_SCHEMA_TYPE))).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR))).thenReturn(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode()));

        Response response = buildGetDataTypeCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }

    @Test
    public void test_getDataTypes_success() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataTypes(eq(RESOURCE_ID))).thenReturn(Either.left(Collections.emptyList()));

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.OK_200.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.OK))).thenReturn(responseFormat);

        Response response = buildGetDataTypesCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void test_getDataTypes_fail() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataTypes(eq(RESOURCE_ID))).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.BAD_REQUEST))).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR))).thenReturn(responseFormat);

        Response response = buildGetDataTypesCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
    }

    @Test
    public void test_getDataTypes_fail_exception() throws Exception {
        when(dataTypeBusinessLogic.getPrivateDataType(eq(RESOURCE_ID),eq(LISTINPUT_SCHEMA_TYPE))).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        when(componentsUtils.getResponseFormat(eq(ActionStatus.GENERAL_ERROR))).thenReturn(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode()));

        Response response = buildGetDataTypesCall().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
    }


    @Test
    public void test_deleteDataType_success() throws Exception {
        when(dataTypeBusinessLogic.deletePrivateDataType(RESOURCE_ID, LISTINPUT_SCHEMA_TYPE)).thenReturn(Either.left(new DataTypeDefinition()));

        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));

        Response response = buildGetDataTypeCall().delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
    }

    @Test
    public void test_deleteDataType_failure_exception() throws Exception {
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
        Response response = buildGetDataTypeCall().delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode());
        verify(componentsUtils, never()).getResponseFormat(ActionStatus.OK);
    }

    @Test
    public void test_deleteDataType_failure_notFound() throws Exception {
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));
        when(dataTypeBusinessLogic.deletePrivateDataType(RESOURCE_ID, LISTINPUT_SCHEMA_TYPE)).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND)).thenReturn(ActionStatus.ARTIFACT_NOT_FOUND);
        when(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND)).thenReturn(new ResponseFormat(HttpStatus.NOT_FOUND_404.getStatusCode()));
        Response response = buildGetDataTypeCall().delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404.getStatusCode());
        verify(componentsUtils, never()).getResponseFormat(ActionStatus.OK);
    }

    @Test
    public void test_deleteInput_success() throws Exception {
        when(inputsBusinessLogic.deleteInput(RESOURCE_ID, USER_ID, LISTINPUT_NAME))
                .thenReturn(Either.left(new InputDefinition()));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));

        // invoke delete call
        Response response = target("/v1/catalog/services/{id}/delete/{inputId}/input")
                .resolveTemplate("id", RESOURCE_ID)
                .resolveTemplate("inputId", LISTINPUT_NAME)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
        verify(inputsBusinessLogic, times(1)).deleteInput(RESOURCE_ID, USER_ID, LISTINPUT_NAME);
    }


    @Test
    public void test_deleteInput_failure_deleteInput() throws Exception {
        when(inputsBusinessLogic.deleteInput(RESOURCE_ID, USER_ID, LISTINPUT_NAME))
                .thenReturn(Either.right(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode())));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));

        // invoke delete call
        Response response = target("/v1/catalog/services/{id}/delete/{inputId}/input")
                .resolveTemplate("id", RESOURCE_ID)
                .resolveTemplate("inputId", LISTINPUT_NAME)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, never()).getResponseFormat(ActionStatus.OK);
    }


    @Test
    public void test_deleteInput_failure_exception() throws Exception {
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.OK_200.getStatusCode()));
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat(HttpStatus.BAD_REQUEST_400.getStatusCode()));

        // invoke delete call
        Response response = target("/v1/catalog/services/{id}/delete/{inputId}/input")
                .resolveTemplate("id", RESOURCE_ID)
                .resolveTemplate("inputId", LISTINPUT_NAME)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .delete();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400.getStatusCode());
        verify(componentsUtils, never()).getResponseFormat(ActionStatus.OK);
    }


    private Invocation.Builder buildCreateListInputCall() {
        return target("/v1/catalog/services/{id}/create/listInput")
                .resolveTemplate("id", RESOURCE_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

    private Invocation.Builder buildGetDataTypeCall() {
        return target("/v1/catalog/services/{id}/dataType/{dataTypeName}")
                .resolveTemplate("id", RESOURCE_ID)
                .resolveTemplate("dataTypeName", LISTINPUT_SCHEMA_TYPE)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

    private Invocation.Builder buildGetDataTypesCall() {
        return target("/v1/catalog/services/{id}/dataTypes")
                .resolveTemplate("id", RESOURCE_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID);
    }

}
