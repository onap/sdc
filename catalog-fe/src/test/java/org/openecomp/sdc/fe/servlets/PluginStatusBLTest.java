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

package org.openecomp.sdc.fe.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.openecomp.sdc.fe.impl.PluginStatusBL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PluginStatusBLTest {

    final static CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
    PluginStatusBL pluginStatusBL = new PluginStatusBL(httpClient);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
    final static PluginsConfiguration pluginsConfiguration = Mockito.mock(PluginsConfiguration.class);
    final static Plugin offlinePlugin = new Plugin();
    final static Plugin onlinePlugin = new Plugin();
    final static CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
    final static StatusLine statusLine = Mockito.mock(StatusLine.class);
    static List<Plugin> testPluginsList = new ArrayList<>();
    static List<Plugin> assertPluginList = new ArrayList<>();

    final static String offlinePluginsDisplayName = "offlinePlugin";
    final static String offlinePluginDiscoveryPath = "http://192.168.10.1:1000/offline";

    final static String onlinePluginDisplayName = "onlinePlugin";
    final static String onlinePluginDiscoveryPath = "http://192.168.10.1:2000/online";

    @BeforeClass
    public static void beforeClass() {
        ConfigurationManager.setTestInstance(configurationManager);
        when(configurationManager.getPluginsConfiguration()).thenReturn(pluginsConfiguration);

        offlinePlugin.setPluginId(offlinePluginsDisplayName);
        offlinePlugin.setPluginDiscoveryUrl(offlinePluginDiscoveryPath);

        onlinePlugin.setPluginId(onlinePluginDisplayName);
        onlinePlugin.setPluginDiscoveryUrl(onlinePluginDiscoveryPath);
    }

    @Before
    public void beforeTest() {
        testPluginsList = new ArrayList<>();
        assertPluginList = new ArrayList<>();
    }

    @Test
    public void TestPluginsConfigurationListReturnsWithWantedPlugins() {
        testPluginsList.add(offlinePlugin);
        testPluginsList.add(onlinePlugin);
        when(pluginsConfiguration.getPluginsList()).thenReturn(testPluginsList);

        assertPluginList.add(offlinePlugin);
        assertPluginList.add(onlinePlugin);

        String result = gson.toJson(assertPluginList);
        String actualResult = pluginStatusBL.getPluginsList();

        assertEquals(actualResult, result);
    }

    @Test
    public void TestGetPluginAvailabilityShouldReturnFalseWhenPluginIsOffline() throws ClientProtocolException, IOException {
        testPluginsList.add(offlinePlugin);
        when(pluginsConfiguration.getPluginsList()).thenReturn(testPluginsList);

        when(statusLine.getStatusCode()).thenReturn(404);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);

        String result = gson.toJson(false);
        String actualResult = pluginStatusBL.getPluginAvailability(offlinePlugin.getPluginId());

        assertEquals(actualResult, result);
    }

    @Test
    public void TestOnlinePluginBeingReturnedWithIsOnlineValueTrue() throws ClientProtocolException, IOException {
        testPluginsList.add(onlinePlugin);
        when(pluginsConfiguration.getPluginsList()).thenReturn(testPluginsList);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);

        String result = gson.toJson(true);
        String actualResult = pluginStatusBL.getPluginAvailability(onlinePlugin.getPluginId());

        assertEquals(actualResult, result);
    }
}
