package org.openecomp.sdc.fe.servlets;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.impl.PluginStatusBL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConfigServletTest {

    private ConfigServlet configServlet;

    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    HttpSession httpSession;
    @Mock
    ServletContext mockedContext;
    @Mock
    PluginStatusBL pluginStatusBL;

    @Before
    public void setUp() {
        initMocks(this);
        configServlet = new ConfigServlet();
    }

    @Test
    public void validateGetPluginsConfigurationReturnsCorrectConfiguration() {

        final String expectedEntity = "testPluginsList";
        prepareMocks();
        when(pluginStatusBL.getPluginsList()).thenReturn(expectedEntity);

        Response response = configServlet.getPluginsConfiguration(httpServletRequest);

        assertEquals(response.getEntity().toString(),expectedEntity);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
    @Test
    public void validateGetPluginsConfigurationResponsesWithServerErrorIfExceptionIsThrown() {

        prepareMocks();
        when(pluginStatusBL.getPluginsList()).thenThrow(new RuntimeException());

        Response response = configServlet.getPluginsConfiguration(httpServletRequest);

        assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    @Test
    public void validateGetPluginOnlineStateReturnsCorrectState() {

        final String testPluginName = "testPlugin";
        final String pluginAvailability = "forTesting";
        prepareMocks();
        when(pluginStatusBL.getPluginAvailability(eq(testPluginName))).thenReturn(pluginAvailability);

        Response response = configServlet.getPluginOnlineState(testPluginName,httpServletRequest);

        assertEquals(response.getEntity().toString(),pluginAvailability);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
    @Test
    public void validateGetPluginOnlineStateResponsesWithServerErrorIfExceptionIsThrown() {

        final String testPluginName = "testPlugin";
        prepareMocks();
        when(pluginStatusBL.getPluginAvailability(any(String.class))).thenThrow(new RuntimeException());

        Response response = configServlet.getPluginOnlineState(testPluginName, httpServletRequest);

        assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    @Test
    public void validateGetPluginOnlineStateResponsesWithNotFoundIfThereIsNoPlugin() {

        final String testPluginName = "testPlugin";
        prepareMocks();
        when(pluginStatusBL.getPluginAvailability(any(String.class))).thenReturn(null);

        Response response = configServlet.getPluginOnlineState(testPluginName, httpServletRequest);

        assertEquals(response.getStatus(), HttpStatus.SC_NOT_FOUND);
        assertTrue(response.getEntity().toString().contains(testPluginName));
    }

    private void prepareMocks() {
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getServletContext()).thenReturn(mockedContext);
        when(mockedContext.getAttribute(Constants.PLUGIN_BL_COMPONENT)).thenReturn(pluginStatusBL);
    }

}
