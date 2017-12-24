package org.openecomp.sdc.servlets;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.AssertFalse;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.DesignersConfiguration;
import org.openecomp.sdc.fe.config.DesignersConfiguration.Designer;
import org.openecomp.sdc.fe.impl.DesignerStatusBL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DesignerStatusBLTest {

	DesignerStatusBL designerStatusBL = new DesignerStatusBL();
	private static  Gson gson = new GsonBuilder().setPrettyPrinting().create();

	final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
	final static DesignersConfiguration designersConfiguraiton = Mockito.mock(DesignersConfiguration.class);
	final static Designer offlineDesigner = new Designer();
	final static Designer onlineDesinger = new Designer();
	final static CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
	final static CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
	final static StatusLine statusLine = Mockito.mock(StatusLine.class); 
	final static Map<String, Designer> testDesignersList = new HashMap<String, Designer>();

	final static String offlineDesignerDisplayName = "offlineDesigner";
	final static String offlineDesignerHost = "192.168.10.1";
	final static int offlineDesignerPort = 1000;
	final static String offlineDesignerPath = "/offline";
	final static String offlineDesignerProtocol = "http";

	final static String onlineDesignerDisplayName = "onlineDesigner";
	final static String onlineDesignerHost = "192.168.20.2";
	final static int onlineDesignerPort = 2000;
	final static String onlineDesignerPath = "/online";
	final static String onlineDesignerProtocol = "http";
	
	StringBuilder offlineRequestString = new StringBuilder();
	StringBuilder onlineRequestString = new StringBuilder();

	@BeforeClass
	public static void beforeClass() {
		when(ConfigurationManager.getConfigurationManager()).thenReturn(configurationManager);
		when(configurationManager.getDesignersConfiguration()).thenReturn(designersConfiguraiton);
		
		offlineDesigner.setDisplayName(offlineDesignerDisplayName);
		offlineDesigner.setDesignerHost(offlineDesignerHost);
		offlineDesigner.setDesignerPort(offlineDesignerPort);
		offlineDesigner.setDesignerPath(offlineDesignerPath);
		offlineDesigner.setDesignerProtocol(offlineDesignerProtocol);

		StringBuilder offlineRequestString = new StringBuilder();
		offlineRequestString.append(offlineDesignerProtocol).append("://").append(onlineDesignerHost).append(":")
				.append(offlineDesignerPort).append(offlineDesignerPath);

		onlineDesinger.setDisplayName(onlineDesignerDisplayName);
		onlineDesinger.setDesignerHost(onlineDesignerHost);
		onlineDesinger.setDesignerPort(onlineDesignerPort);
		onlineDesinger.setDesignerPath(onlineDesignerPath);
		onlineDesinger.setDesignerProtocol(onlineDesignerProtocol);

		StringBuilder onlineRequestString = new StringBuilder();
		onlineRequestString.append(onlineDesignerProtocol).append("://").append(onlineDesignerHost).append(":")
				.append(offlineDesignerPort).append(offlineDesignerPath);
		
	}

	@Test
	public void TestOfflineDesignerNotBeingReturnedWhenCallingCheckDesinerListAvailability() throws ClientProtocolException, IOException {
		testDesignersList.put("offlineDesigner", offlineDesigner);
		when(designersConfiguraiton.getDesignersList()).thenReturn(testDesignersList);
		
		when(statusLine.getStatusCode()).thenReturn(404);		
		when(httpResponse.getStatusLine()).thenReturn(statusLine);		
		when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);
		
		assertNull(designerStatusBL.checkDesinerListAvailability());
		
	}
	
	@Test
	public void TestOnlineDesignerNotBeingReturnedWhenCallingCheckDesinerListAvailability() throws ClientProtocolException, IOException {
		testDesignersList.put("onlineDesigner", onlineDesinger);
		when(designersConfiguraiton.getDesignersList()).thenReturn(testDesignersList);
		
		when(httpResponse.getStatusLine().getStatusCode()).thenReturn(200);		
		when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);
		
		String result = gson.toJson(testDesignersList);
		
		assert(designerStatusBL.checkDesinerListAvailability()).equals(result);
		
	}

}
