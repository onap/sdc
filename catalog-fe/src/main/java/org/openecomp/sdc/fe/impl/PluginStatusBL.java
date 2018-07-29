package org.openecomp.sdc.fe.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.PluginsConfiguration;
import org.openecomp.sdc.fe.config.PluginsConfiguration.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginStatusBL {

	private static final Logger log = LoggerFactory.getLogger(PluginStatusBL.class.getName());
	private final Gson gson;
	private final CloseableHttpClient client;
	private final PluginsConfiguration pluginsConfiguration;
	private Integer connectionTimeout;
	private RequestConfig requestConfig;

	public PluginStatusBL() {
		this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
		this.client = HttpClients.createDefault();
		this.gson = new GsonBuilder().setPrettyPrinting().create();
	}

	public PluginStatusBL(CloseableHttpClient client) {
		this.pluginsConfiguration = ConfigurationManager.getConfigurationManager().getPluginsConfiguration();
		this.client = client;

		this.gson = new GsonBuilder().setPrettyPrinting().create();
				
	}

	public String checkPluginsListAvailability() {
		String result = null;

		if (pluginsConfiguration == null || pluginsConfiguration.getPluginsList() == null) {
			log.warn("Configuration of type {} was not found", PluginsConfiguration.class);
		} else {
			log.debug("The value returned from getConfig is {}", pluginsConfiguration);
			connectionTimeout = pluginsConfiguration.getConnectionTimeout();
			this.requestConfig = RequestConfig.custom()
					.setSocketTimeout(connectionTimeout)
					.setConnectTimeout(connectionTimeout)
					.setConnectionRequestTimeout(connectionTimeout).build();

			List<Plugin> availablePluginsList = new ArrayList<>();

			pluginsConfiguration.getPluginsList().forEach(plugin -> {
				plugin.setOnline(checkPluginAvailability(plugin));

				availablePluginsList.add(plugin);
			});
			result = gson.toJson(availablePluginsList);
		}
		return result;
	}

	private boolean checkPluginAvailability(Plugin plugin) {
		boolean result = false;
		log.debug("sending head request to id:{} url:{}",plugin.getPluginId(),plugin.getPluginDiscoveryUrl());
		HttpHead head = new HttpHead(plugin.getPluginDiscoveryUrl());

		head.setConfig(this.requestConfig);

		try (CloseableHttpResponse response = this.client.execute(head)) {
			result = response != null && response.getStatusLine().getStatusCode() == 200;
			log.debug("The plugin {} is {} with result {}",plugin.getPluginId(),( result ? "online" : "offline" ), result);
		} catch (IOException e) {
			log.debug("The plugin {} is offline", plugin.getPluginId());
                        log.debug("Exception:",e);
		}

		return result;
	}

}
