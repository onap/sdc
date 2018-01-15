package org.openecomp.sdc.fe.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.DesignersConfiguration;
import org.openecomp.sdc.fe.config.DesignersConfiguration.Designer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DesignerStatusBL {

	private static Logger log = LoggerFactory.getLogger(DesignerStatusBL.class.getName());
	private static  Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private CloseableHttpClient client = null;
	
	public DesignerStatusBL() {
		this.client = HttpClients.createDefault();
	}
	
	public DesignerStatusBL(CloseableHttpClient client) {
		this.client = client;
				
	}

	public String checkDesignerListAvailability() {
		String result = null;

		DesignersConfiguration designersConfiguration = ConfigurationManager.getConfigurationManager()
				.getDesignersConfiguration();

		if (designersConfiguration == null || designersConfiguration.getDesignersList() == null) {
			log.warn("Configuration of type {} was not found", DesignersConfiguration.class);
		} else {
			log.debug("The value returned from getConfig is {}", designersConfiguration);

			List<Designer> availableDesignersList = new ArrayList<>();

			designersConfiguration.getDesignersList().forEach(value -> {
				if (checkDesignerAvailability(value)) {
					availableDesignersList.add(value);
				}

			});
			result = gson.toJson(availableDesignersList);
		}
		return result;
	}

	private boolean checkDesignerAvailability(Designer designer) {

		StringBuilder requestString = new StringBuilder();
		boolean result = false;

		requestString.append(designer.getDesignerProtocol()).append("://").append(designer.getDesignerHost()).append(":")
				.append(designer.getDesignerPort()).append(designer.getDesignerPath());

		HttpHead head = new HttpHead(requestString.toString());

		try (CloseableHttpResponse response = this.client.execute(head)) {
			result = response != null && response.getStatusLine().getStatusCode() == 200;
		} catch (IOException e) {
			log.debug("The designer {} is offline", designer);
		}

		return result;
	}

}
