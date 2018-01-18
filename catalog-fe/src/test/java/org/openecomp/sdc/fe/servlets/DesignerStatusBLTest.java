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
import org.openecomp.sdc.fe.config.DesignersConfiguration;
import org.openecomp.sdc.fe.config.DesignersConfiguration.Designer;
import org.openecomp.sdc.fe.impl.DesignerStatusBL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DesignerStatusBLTest {

	final static CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
	DesignerStatusBL designerStatusBL = new DesignerStatusBL(httpClient);
	private static  Gson gson = new GsonBuilder().setPrettyPrinting().create();

	final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
	final static DesignersConfiguration designersConfiguration = Mockito.mock(DesignersConfiguration.class);
	final static Designer offlineDesigner = new Designer();
	final static Designer onlineDesigner = new Designer();
	final static CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
	final static StatusLine statusLine = Mockito.mock(StatusLine.class); 
	final static List<Designer> testDesignersList = new ArrayList<>();

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

	@BeforeClass
	public static void beforeClass() {
		ConfigurationManager.setTestInstance(configurationManager);
		when(configurationManager.getDesignersConfiguration()).thenReturn(designersConfiguration);
		
		offlineDesigner.setDesignerId(offlineDesignerDisplayName);
		offlineDesigner.setDesignerHost(offlineDesignerHost);
		offlineDesigner.setDesignerPort(offlineDesignerPort);
		offlineDesigner.setDesignerPath(offlineDesignerPath);
		offlineDesigner.setDesignerProtocol(offlineDesignerProtocol);

		StringBuilder offlineRequestString = new StringBuilder();
		offlineRequestString.append(offlineDesignerProtocol).append("://").append(onlineDesignerHost).append(":")
				.append(offlineDesignerPort).append(offlineDesignerPath);

		onlineDesigner.setDesignerId(onlineDesignerDisplayName);
		onlineDesigner.setDesignerHost(onlineDesignerHost);
		onlineDesigner.setDesignerPort(onlineDesignerPort);
		onlineDesigner.setDesignerPath(onlineDesignerPath);
		onlineDesigner.setDesignerProtocol(onlineDesignerProtocol);

		StringBuilder onlineRequestString = new StringBuilder();
		onlineRequestString.append(onlineDesignerProtocol).append("://").append(onlineDesignerHost).append(":")
				.append(offlineDesignerPort).append(offlineDesignerPath);
		
	}

	@Test
	public void TestOfflineDesignerNotBeingReturnedWhenCallingCheckDesignerListAvailability() throws ClientProtocolException, IOException {
		testDesignersList.add(offlineDesigner);
		when(designersConfiguration.getDesignersList()).thenReturn(testDesignersList);
		
		when(statusLine.getStatusCode()).thenReturn(404);		
		when(httpResponse.getStatusLine()).thenReturn(statusLine);		
		when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);
		
		assertTrue(designerStatusBL.checkDesignerListAvailability().equals("[]"));
		
	}
	
	@Test
	public void TestOnlineDesignerNotBeingReturnedWhenCallingCheckDesignerListAvailability() throws ClientProtocolException, IOException {
		testDesignersList.add(onlineDesigner);
		when(designersConfiguration.getDesignersList()).thenReturn(testDesignersList);
		
		when(statusLine.getStatusCode()).thenReturn(200);		
		when(httpResponse.getStatusLine()).thenReturn(statusLine);		
		when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);
		
		String result = gson.toJson(testDesignersList);
		
		assertTrue(designerStatusBL.checkDesignerListAvailability().contains(result));
		
	}

}
