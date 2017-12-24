package org.openecomp.sdc.fe.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	public String checkDesinerListAvailability() {
		String result = null;

		DesignersConfiguration designersConfiguarion = ConfigurationManager.getConfigurationManager()
				.getDesignersConfiguration();

		if (designersConfiguarion == null || designersConfiguarion.getDesignersList() == null) {
			log.warn("Configuration of type {} was not found", DesignersConfiguration.class);
		} else {
			log.debug("The value returned from getConfig is {}", designersConfiguarion);

			Map<String, Designer> avaiableDesignersMap = new HashMap<String, Designer>();

			designersConfiguarion.getDesignersList().forEach((key, value) -> {
				if (CheckDesignerAvailabilty(value)) {
					avaiableDesignersMap.put(key, value);
				}

			});
			result = gson.toJson(avaiableDesignersMap);
		}
		return result;
	}

	private boolean CheckDesignerAvailabilty(Designer designer) {

		StringBuilder requestString = new StringBuilder();
		boolean result = false;

		requestString.append(designer.getDesignerProtocol()).append("://").append(designer.getDesignerHost()).append(":")
				.append(designer.getDesignerPort()).append(designer.getDesignerPath());

		CloseableHttpClient client = HttpClients.createDefault();
		HttpHead head = new HttpHead(requestString.toString());

		try (CloseableHttpResponse response = client.execute(head)) {
			result = response != null && response.getStatusLine().getStatusCode() == 200;
		} catch (IOException e) {
			log.debug("The designer {} is offline", designer);
		}

		return result;
	}

}
