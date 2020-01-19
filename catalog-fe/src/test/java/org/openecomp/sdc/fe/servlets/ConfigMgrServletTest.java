/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RunWith(MockitoJUnitRunner.class)
public class ConfigMgrServletTest {

    private static final String VERSION_1 = "VERSION 1";
    private static final String PROTOCOL = "PROTO";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletContext context;
    @Mock
    private ConfigurationManager configManager;

    @Before
    public void setUp() {
        Mockito.when(request.getSession()).thenReturn(session);
        Mockito.when(session.getServletContext()).thenReturn(context);
        Mockito.when(context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configManager);
    }

    @Test
    public void shouldGetConfigForRestType() {
        String wantedResult = "{\n"
            + "  \"connectionPoolSize\": 5\n"
            + "}";
        RestConfigurationInfo restConfiguration = new RestConfigurationInfo();
        restConfiguration.setConnectionPoolSize(5);
        Mockito.when(configManager.getRestClientConfiguration()).thenReturn(restConfiguration);

        ConfigMgrServlet configMgrServlet = new ConfigMgrServlet();
        String config = configMgrServlet.getConfig(request, "rest");
        Assert.assertEquals(wantedResult, config);
    }

    @Test
    public void shouldGetConfigForConfigurationType() {
        checkForConfigurationTypeOrEmpty("configuration");
    }

    @Test
    public void shouldGetConfigForNullType() {
        checkForConfigurationTypeOrEmpty(null);
    }

    private void checkForConfigurationTypeOrEmpty(String type) {
        String wantedResult = "{\n"
            + "  \"beProtocol\": \"PROTO\",\n"
            + "  \"version\": \"VERSION 1\",\n"
            + "  \"threadpoolSize\": 0,\n"
            + "  \"requestTimeout\": 0\n"
            + "}";

        Configuration configuration = new Configuration();
        configuration.setVersion(VERSION_1);
        configuration.setBeProtocol(PROTOCOL);
        Mockito.when(configManager.getConfiguration()).thenReturn(configuration);

        ConfigMgrServlet configMgrServlet = new ConfigMgrServlet();
        String config = configMgrServlet.getConfig(request, type);
        Assert.assertEquals(wantedResult, config);
    }

}