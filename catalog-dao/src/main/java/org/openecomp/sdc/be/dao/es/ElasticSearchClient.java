/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.es;

import org.apache.commons.lang.SystemUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.shield.ShieldPlugin;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Prepare the node to work with elastic search.
 * 
 * @author luc boutier
 */
@Component("elasticsearch-client")
public class ElasticSearchClient {

	private static Logger log = Logger.getLogger(ElasticSearchClient.class.getName());

	private Node node;
	private boolean isLocal;
	private String clusterName;
	private Client client;

	String serverHost;
	String serverPort;

	ArrayList<String> nodes = new ArrayList<>();

	private boolean isTransportClient;

	@PostConstruct
	public void initialize() throws URISyntaxException {

		URL url = null;
		Settings settings = null;
		URL systemResourceElasticsearchPath = ClassLoader.getSystemResource("elasticsearch.yml");

		if (systemResourceElasticsearchPath != null) {
			log.debug("try to create URI for {}", systemResourceElasticsearchPath.toString());
			Path classpathConfig = Paths.get(systemResourceElasticsearchPath.toURI());
			settings = Settings.settingsBuilder().loadFromPath(classpathConfig).build();
		}
		String configHome = System.getProperty("config.home");
		if (configHome != null && !configHome.isEmpty()) {
			try {
				if (SystemUtils.IS_OS_WINDOWS) {
					url = new URL("file:///" + configHome + "/elasticsearch.yml");
				} else {
					url = new URL("file:" + configHome + "/elasticsearch.yml");
				}

				log.debug("URL {}", url);
				settings = Settings.settingsBuilder().loadFromPath(Paths.get(url.toURI())).build();
			} catch (MalformedURLException | URISyntaxException e1) {
				log.error("Failed to create URL in order to load elasticsearch yml");
				System.err.println("Failed to create URL in order to load elasticsearch yml from " + configHome);
			}
		}
		if (settings == null) {
			log.error("Failed to find settings of elasticsearch yml");
			System.err.println("Failed to create URL in order to load elasticsearch yml from " + configHome);
		}
		if (isTransportClient()) {
			log.info("******* ElasticSearchClient type is Transport Client *****");
			TransportClient transportClient = TransportClient.builder().addPlugin(ShieldPlugin.class).settings(settings)
					.build();

			String[] nodesArray = transportClient.settings().getAsArray("transport.client.initial_nodes");
			for (String host : nodesArray) {
				int port = 9300;

				// or parse it from the host string...
				String[] splitHost = host.split(":", 2);
				if (splitHost.length == 2) {
					host = splitHost[0];
					port = Integer.parseInt(splitHost[1]);
				}

				transportClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host, port)));

			}
			this.client = transportClient;
			serverHost = Arrays.toString(nodesArray);

		} else {
			log.info("******* ElasticSearchClient type is Node Client *****");
			this.node = NodeBuilder.nodeBuilder().settings(settings).client(!isLocal).clusterName(this.clusterName)
					.local(isLocal).node();
			this.client = node.client();

			serverHost = this.client.settings().get("discovery.zen.ping.unicast.hosts");
			if (serverHost == null) {
				serverHost = "['localhost:9200']";
			}

		}

		serverPort = this.client.settings().get("http.port");
		if (serverPort == null) {
			serverPort = "9200";
		}

		log.info("Initialized ElasticSearch client for cluster <{}> with nodes: {}", this.clusterName, serverHost);
	}

	@PreDestroy
	public void close() {
		if (client != null) {
			client.close();
		}
		if (node != null) {
			node.close();
		}
		log.info("Closed ElasticSearch client for cluster <{}>", this.clusterName);
	}

	/**
	 * Get the elastic search client.
	 * 
	 * @return The elastic search client.
	 */
	public Client getClient() {
		return this.client;
	}

	public String getServerHost() {
		return serverHost;
	}

	public String getServerPort() {
		return serverPort;
	}

	@Value("#{elasticsearchConfig['cluster.name']}")
	public void setClusterName(final String clusterName) {
		this.clusterName = clusterName;
	}

	@Value("#{elasticsearchConfig['elasticSearch.local']}")
	public void setLocal(final String strIsLocal) {
		if (strIsLocal != null && !strIsLocal.isEmpty())
			this.isLocal = Boolean.parseBoolean(strIsLocal);
	}

	public boolean isTransportClient() {
		return isTransportClient;
	}

	@Value("#{elasticsearchConfig['elasticSearch.transportclient']}")
	public void setTransportClient(final String strIsTransportclient) {
		if (strIsTransportclient != null && !strIsTransportclient.isEmpty())
			this.isTransportClient = Boolean.parseBoolean(strIsTransportclient);
	}

}
