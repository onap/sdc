package org.openecomp.sdc.fe.servlets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.openecomp.sdc.fe.impl.PluginStatusBL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PluginStatusBLTest {

	final static CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
	PluginStatusBL pluginStatusBL = new PluginStatusBL(httpClient);
	private static  Gson gson = new GsonBuilder().setPrettyPrinting().create();

	final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
	final static PluginsConfiguration pluginsConfiguration = Mockito.mock(PluginsConfiguration.class);
	final static Plugin offlinePlugin = new Plugin();
	final static Plugin onlinePlugin = new Plugin();
	final static CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
	final static StatusLine statusLine = Mockito.mock(StatusLine.class); 
	final static List<Plugin> testPluginsList = new ArrayList<>();

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

	@Test
	public void TestOfflinePluginNotBeingReturnedWhenCallingCheckPluginsListAvailability() throws ClientProtocolException, IOException {
		testPluginsList.add(offlinePlugin);
		when(pluginsConfiguration.getPluginsList()).thenReturn(testPluginsList);
		
		when(statusLine.getStatusCode()).thenReturn(404);		
		when(httpResponse.getStatusLine()).thenReturn(statusLine);		
		when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);
		
		assertTrue(pluginStatusBL.checkPluginsListAvailability().equals("[]"));
		
	}
	
	@Test
	public void TestOnlinePluginNotBeingReturnedWhenCallingCheckPluginsListAvailability() throws ClientProtocolException, IOException {
		testPluginsList.add(onlinePlugin);
		when(pluginsConfiguration.getPluginsList()).thenReturn(testPluginsList);
		
		when(statusLine.getStatusCode()).thenReturn(200);		
		when(httpResponse.getStatusLine()).thenReturn(statusLine);		
		when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);
		
		String result = gson.toJson(testPluginsList);
		
		assertTrue(pluginStatusBL.checkPluginsListAvailability().contains(result));
		
	}

}
