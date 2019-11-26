/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.fe.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.fe.config.ConfigurationManager;
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
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpSession httpSession;
    @Mock
    private ServletContext mockedContext;
    @Mock
    private PluginStatusBL pluginStatusBL;
    @Mock
    private ConfigurationManager configManager;

    @Before
    public void setUp() {
        initMocks(this);
        String appConfigDir = "src/test/resources/config/catalog-fe";
        ConfigurationSource configurationSource =
                new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configManager = new ConfigurationManager(configurationSource);
        configServlet = new ConfigServlet();
    }

    @Test
    public void validateWorkspaceConfiguration() {

        prepareMocks();

        Response response = configServlet.getUIWorkspaceConfiguration(httpServletRequest);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String expected = gson.toJson(configManager.getWorkspaceConfiguration());
        assertEquals(expected, response.getEntity().toString());
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
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
        when(mockedContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configManager);
        when(mockedContext.getAttribute(Constants.PLUGIN_BL_COMPONENT)).thenReturn(pluginStatusBL);
    }

}
