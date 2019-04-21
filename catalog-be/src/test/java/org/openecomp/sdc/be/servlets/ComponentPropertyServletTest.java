package org.openecomp.sdc.be.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import fj.data.Either;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class ComponentPropertyServletTest extends JerseySpringBaseTest {
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
    private final static String USER_ID = "jh0003";
    private static final String VALID_PROPERTY_NAME = "valid_name_123";
    private static final String INVALID_PROPERTY_NAME = "invalid_name_$.&";
    private static final String STRING_TYPE = "string";

    @Before
    public void initClass() {
        initMockitoStubbings();
    }

    @Test
    public void testCreatePropertyOnService_success() {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(VALID_PROPERTY_NAME);
        property.setType(STRING_TYPE);

        EntryData<String, PropertyDefinition> propertyEntry = new EntryData<>(VALID_PROPERTY_NAME, property);
        when(propertyBl.addPropertyToComponent(eq(SERVICE_ID), any(), any(), any())).thenReturn(Either.left(propertyEntry));

        Response propertyInService =
                componentPropertyServlet.createPropertyInService(SERVICE_ID, getValidProperty(), request, USER_ID);

        Assert.assertEquals(HttpStatus.OK_200.getStatusCode(), propertyInService.getStatus());
    }

    @Test
    public void testCreatePropertyInvalidName_failure() {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(INVALID_PROPERTY_NAME);
        property.setType(STRING_TYPE);

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setStatus(HttpStatus.BAD_REQUEST_400.getStatusCode());

        when(componentsUtils.getResponseFormat(eq(ActionStatus.INVALID_PROPERTY_NAME))).thenReturn(responseFormat);


        Response propertyInService =
                componentPropertyServlet.createPropertyInService(SERVICE_ID, getInvalidProperty(), request, USER_ID);

        Assert.assertEquals(HttpStatus.BAD_REQUEST_400.getStatusCode(), propertyInService.getStatus());
    }

    private static void initMockitoStubbings() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(context);
        when(context.getAttribute(eq(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))).thenReturn(wrapper);
        when(wrapper.getWebAppContext(any())).thenReturn(webAppContext);
        when(webAppContext.getBean(eq(PropertyBusinessLogic.class))).thenReturn(propertyBl);
        when(webAppContext.getBean(eq(ComponentsUtils.class))).thenReturn(componentsUtils);
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
