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

package org.openecomp.sdc.be.dao.janusgraph;

import org.janusgraph.core.*;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.util.JanusGraphCleanup;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.ResourceUnavailableException;
import org.janusgraph.diskstorage.locking.PermanentLockingException;
import org.janusgraph.graphdb.database.idassigner.IDPoolExhaustedException;
import fj.data.Either;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;


@Component("janusgraph-client")
public class JanusGraphClient {
	private static Logger logger = LoggerFactory.getLogger(JanusGraphClient.class.getName());
	private static Logger healthLogger = LoggerFactory.getLogger("janusgraph.healthcheck");

	private static final String HEALTH_CHECK = GraphPropertiesDictionary.HEALTH_CHECK.getProperty();
	private static final String OK = "GOOD";

	public JanusGraphClient() {
	}

	private class HealthCheckTask implements Callable<Vertex> {
		@Override
		public Vertex call() {

			JanusGraphVertex v = (JanusGraphVertex) graph.query().has(HEALTH_CHECK, OK).vertices().iterator().next();
			JanusGraphVertexProperty<String> property = v.property("healthcheck", OK + "_" + System.currentTimeMillis());
			healthLogger.trace("Health Check Node Found...{}", v.property(HEALTH_CHECK));
			graph.tx().commit();

			return v;
		}
	}

	private class HealthCheckScheduledTask implements Runnable {
		@Override
		public void run() {
			healthLogger.trace("Executing janusGraph Health Check Task - Start");
			boolean healthStatus = isGraphOpen();
			healthLogger.trace("Executing janusGraph Health Check Task - Status = {}", healthStatus);
			if (healthStatus != lastHealthState) {
				logger.trace("janusGraph  Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
				lastHealthState = healthStatus;
				logAlarm();
			}
		}
	}

	private class ReconnectTask implements Runnable {
		@Override
		public void run() {
			logger.trace("Trying to reconnect to JanusGraph...");
			if (graph == null) {
				createGraph(janusGraphCfgFile);
			}
		}
	}

	private JanusGraph graph;

	// Health Check Variables

	/**
	 * This executor will execute the health check task on a callable task that can be executed with a timeout.
	 */
	ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "JanusGraph-Health-Check-Thread");
		}
	});
	private long healthCheckReadTimeout = 2;
	HealthCheckTask healthCallableTask = new HealthCheckTask();
	HealthCheckScheduledTask healthCheckScheduledTask = new HealthCheckScheduledTask();
	boolean lastHealthState = false;

	// Reconnection variables
	private ScheduledExecutorService reconnectScheduler = null;
	private ScheduledExecutorService healthCheckScheduler = null;
	private Runnable reconnectTask = null;
	private long reconnectInterval = 3;
	@SuppressWarnings("rawtypes")
	private Future reconnectFuture;

	private String janusGraphCfgFile = null;
	JanusGraphClientStrategy janusGraphClientStrategy;

	public JanusGraphClient(JanusGraphClientStrategy janusGraphClientStrategy) {
		super();
		this.janusGraphClientStrategy = janusGraphClientStrategy;

		// Initialize a single threaded scheduler for health-check
		this.healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "JanusGraph-Health-Check-Task");
			}
		});

		healthCheckReadTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanHealthCheckReadTimeout(2);
		reconnectInterval = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanReconnectIntervalInSeconds(3);

		logger.info("** JanusGraphClient created");
	}

	@PostConstruct
	public JanusGraphOperationStatus createGraph() {

		logger.info("** createGraph started **");

		if (ConfigurationManager.getConfigurationManager().getConfiguration().getTitanInMemoryGraph()) {
			BaseConfiguration conf = new BaseConfiguration();
			conf.setProperty("storage.backend", "inmemory");
			graph = JanusGraphFactory.open(conf);
            createJanusGraphSchema();
			logger.info("** in memory graph created");
			return JanusGraphOperationStatus.OK;
		} else {
			this.janusGraphCfgFile = janusGraphClientStrategy.getConfigFile();
			if (janusGraphCfgFile == null || janusGraphCfgFile.isEmpty()) {
				janusGraphCfgFile = "config/janusgraph.properties";
			}

			// yavivi
			// In case connection failed on init time, schedule a reconnect task
			// in the BG
			JanusGraphOperationStatus status = createGraph(janusGraphCfgFile);
			logger.debug("Create JanusGraph graph status {}", status);
			if (status != JanusGraphOperationStatus.OK) {
				this.startReconnectTask();
			}

			return status;
		}
	}

	private void startHealthCheckTask() {
		this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, reconnectInterval, TimeUnit.SECONDS);
	}

	/**
	 * This method will be invoked ONLY on init time in case JanusGraph storage is down.
	 */
	private void startReconnectTask() {
		this.reconnectTask = new ReconnectTask();
		// Initialize a single threaded scheduler
		this.reconnectScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "JanusGraph-Reconnect-Task");
			}
		});

		logger.info("Scheduling reconnect task {} with interval of {} seconds", reconnectTask, reconnectInterval);
		reconnectFuture = this.reconnectScheduler.scheduleAtFixedRate(this.reconnectTask, 0, this.reconnectInterval, TimeUnit.SECONDS);
	}

	public void cleanupGraph() {
		if (graph != null) {
			// graph.shutdown();
			graph.close();
			try {
				JanusGraphFactory.drop(graph);
			} catch (BackendException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean graphInitialized(){
		JanusGraphManagement graphMgmt = graph.openManagement();
		return graphMgmt.containsPropertyKey(HEALTH_CHECK) && graphMgmt.containsGraphIndex(HEALTH_CHECK);
	}
	

	public JanusGraphOperationStatus createGraph(String janusGraphCfgFile) {
		logger.info("** open graph with {} started", janusGraphCfgFile);
		try {
			logger.info("openGraph : try to load file {}", janusGraphCfgFile);
			graph = JanusGraphFactory.open(janusGraphCfgFile);
			if (graph.isClosed() || !graphInitialized()) {
				logger.error("janusgraph graph was not initialized");
				return JanusGraphOperationStatus.NOT_CREATED;
			}

		} catch (Exception e) {
			this.graph = null;
			logger.info("createGraph : failed to open JanusGraph graph with configuration file: {}", janusGraphCfgFile);
			logger.debug("createGraph : failed with exception.", e);
			return JanusGraphOperationStatus.NOT_CONNECTED;
		}

		logger.info("** JanusGraph graph created ");

		// Do some post creation actions
		this.onGraphOpened();

		return JanusGraphOperationStatus.OK;
	}

	private void onGraphOpened() {
		// if a reconnect task is running, cancel it.
		if (this.reconnectFuture != null) {
			logger.info("** Cancelling JanusGraph reconnect task");
			reconnectFuture.cancel(true);
		}

		// create health-check node
		if (!graph.query().has(HEALTH_CHECK, OK).vertices().iterator().hasNext()) {
			logger.trace("Healthcheck Singleton node does not exist, Creating healthcheck node...");
			Vertex healthCheckNode = graph.addVertex();
			healthCheckNode.property(HEALTH_CHECK, OK);
			logger.trace("Healthcheck node created successfully. ID={}", healthCheckNode.property(T.id.getAccessor()));
			graph.tx().commit();
		} else {
			logger.trace("Skipping Healthcheck Singleton node creation. Already exist...");
		}
		this.startHealthCheckTask();
	}


	public Either<JanusGraph, JanusGraphOperationStatus> getGraph() {
		if (graph != null) {
			return Either.left(graph);
		} else {
			return Either.right(JanusGraphOperationStatus.NOT_CREATED);
		}
	}

	public JanusGraphOperationStatus commit() {
		if (graph != null) {
			try {
				graph.tx().commit();
				return JanusGraphOperationStatus.OK;
			} catch (Exception e) {
				return handleJanusGraphException(e);
			}
		} else {
			return JanusGraphOperationStatus.NOT_CREATED;
		}
	}

	public JanusGraphOperationStatus rollback() {
		if (graph != null) {
			try {
				// graph.rollback();
				graph.tx().rollback();
				return JanusGraphOperationStatus.OK;
			} catch (Exception e) {
				return handleJanusGraphException(e);
			}
		} else {
			return JanusGraphOperationStatus.NOT_CREATED;
		}
	}

	public static JanusGraphOperationStatus handleJanusGraphException(Exception e) {
		if (e instanceof JanusGraphConfigurationException) {
			return JanusGraphOperationStatus.JANUSGRAPH_CONFIGURATION;
		}
		if (e instanceof SchemaViolationException) {
			return JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION;
		}
		if (e instanceof PermanentLockingException) {
			return JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION;
		}
		if (e instanceof IDPoolExhaustedException) {
			return JanusGraphOperationStatus.GENERAL_ERROR;
		}
		if (e instanceof InvalidElementException) {
			return JanusGraphOperationStatus.INVALID_ELEMENT;
		}
		if (e instanceof InvalidIDException) {
			return JanusGraphOperationStatus.INVALID_ID;
		}
		if (e instanceof QueryException) {
			return JanusGraphOperationStatus.INVALID_QUERY;
		}
		if (e instanceof ResourceUnavailableException) {
			return JanusGraphOperationStatus.RESOURCE_UNAVAILABLE;
		}
		if (e instanceof IllegalArgumentException) {
			// TODO check the error message??
			return JanusGraphOperationStatus.ILLEGAL_ARGUMENT;
		}

		return JanusGraphOperationStatus.GENERAL_ERROR;
	}

	public boolean getHealth() {
		return this.lastHealthState;
	}

	private boolean isGraphOpen() {
		healthLogger.trace("Invoking JanusGraph health check ...");
		Vertex v = null;
		if (graph != null) {
			try {
				Future<Vertex> future = healthCheckExecutor.submit(healthCallableTask);
				v = future.get(this.healthCheckReadTimeout, TimeUnit.SECONDS);
				healthLogger.trace("Health Check Node Found... {}", v.property(HEALTH_CHECK));
				graph.tx().commit();
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null) {
					message = e.getClass().getName();
				}
				logger.error("JanusGraph Health Check Failed. {}", message);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}


	public static void main(String[] args) throws InterruptedException {
		JanusGraphClient client = new JanusGraphClient(new DAOJanusGraphStrategy());
		client.createGraph();

		while (true) {
			boolean health = client.isGraphOpen();
			System.err.println("health=" + health);
			Thread.sleep(2000);
		}

	}


	private static final String JANUSGRAPH_HEALTH_CHECK = "janusgraphHealthCheck";

	private void logAlarm() {
		if (lastHealthState) {
			BeEcompErrorManager.getInstance().logBeHealthCheckJanusGraphRecovery(JANUSGRAPH_HEALTH_CHECK);
		} else {
			BeEcompErrorManager.getInstance().logBeHealthCheckJanusGraphError(JANUSGRAPH_HEALTH_CHECK);
		}
	}
	
	private void createJanusGraphSchema() {
		
		JanusGraphManagement graphMgt = graph.openManagement();
		JanusGraphIndex index = null;
		for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
			PropertyKey propKey = null;
			if (!graphMgt.containsPropertyKey(prop.getProperty())) {
				Class<?> clazz = prop.getClazz();
				if (!clazz.isAssignableFrom(ArrayList.class) && !clazz.isAssignableFrom(HashMap.class)) {
					propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
				}
			} else {
				propKey = graphMgt.getPropertyKey(prop.getProperty());
			}
			if (prop.isIndexed()) {
				if (!graphMgt.containsGraphIndex(prop.getProperty())) {
					if (prop.isUnique()) {
						index = graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).unique().buildCompositeIndex();
						// Ensures only one name per vertex
						graphMgt.setConsistency(propKey, ConsistencyModifier.LOCK);
						// Ensures name uniqueness in the graph
						graphMgt.setConsistency(index, ConsistencyModifier.LOCK);

					} else {
						graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).buildCompositeIndex();
					}
				}
			}
		}
		graphMgt.commit();
	}

}
