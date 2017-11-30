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

package org.openecomp.sdc.be.dao.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.InvalidElementException;
import com.thinkaurelius.titan.core.InvalidIDException;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.QueryException;
import com.thinkaurelius.titan.core.SchemaViolationException;
import com.thinkaurelius.titan.core.TitanConfigurationException;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TitanVertexProperty;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.diskstorage.ResourceUnavailableException;
import com.thinkaurelius.titan.diskstorage.locking.PermanentLockingException;
import com.thinkaurelius.titan.graphdb.database.idassigner.IDPoolExhaustedException;

import fj.data.Either;


@Component("titan-client")
public class TitanGraphClient {

	private static Logger logger = LoggerFactory.getLogger(TitanGraphClient.class.getName());
	private static Logger healthLogger = LoggerFactory.getLogger("titan.healthcheck");

	private static final String HEALTH_CHECK = GraphPropertiesDictionary.HEALTH_CHECK.getProperty();
	private static final String OK = "GOOD";

	public TitanGraphClient() {
	}

	private class HealthCheckTask implements Callable<Vertex> {
		@Override
		public Vertex call() {

			TitanVertex v = (TitanVertex) graph.query().has(HEALTH_CHECK, OK).vertices().iterator().next();
			TitanVertexProperty<String> property = v.property("healthcheck", OK + "_" + System.currentTimeMillis());
			healthLogger.trace("Health Check Node Found...{}", v.property(HEALTH_CHECK));
			graph.tx().commit();

			// Vertex v = graph.getVertices(HEALTH_CHECK, OK).iterator().next();
			// v.setProperty("healthcheck", OK + "_" +
			// System.currentTimeMillis());
			// graph.commit();
			// healthLogger.trace("Health Check Node
			// Found..."+v.getProperty(HEALTH_CHECK) );
			return v;
		}
	}

	private class HealthCheckScheduledTask implements Runnable {
		@Override
		public void run() {
			healthLogger.trace("Executing TITAN Health Check Task - Start");
			boolean healthStatus = isGraphOpen();
			healthLogger.trace("Executing TITAN Health Check Task - Status = {}", healthStatus);
			if (healthStatus != lastHealthState) {
				logger.trace("TITAN  Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
				lastHealthState = healthStatus;
				logAlarm();
			}
		}
	}

	private class ReconnectTask implements Runnable {
		@Override
		public void run() {
			logger.trace("Trying to reconnect to Titan...");
			if (graph == null) {
				createGraph(titanCfgFile);
			}
		}
	}

	private TitanGraph graph;

	// Health Check Variables

	/**
	 * This executor will execute the health check task on a callable task that can be executed with a timeout.
	 */
	ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Titan-Health-Check-Thread");
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

	private String titanCfgFile = null;
	TitanClientStrategy titanClientStrategy;

	public TitanGraphClient(TitanClientStrategy titanClientStrategy) {
		super();
		this.titanClientStrategy = titanClientStrategy;

		// Initialize a single threaded scheduler for health-check
		this.healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Titan-Health-Check-Task");
			}
		});

		healthCheckReadTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanHealthCheckReadTimeout(2);
		reconnectInterval = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanReconnectIntervalInSeconds(3);

		logger.info("** TitanGraphClient created");
	}

	@PostConstruct
	public TitanOperationStatus createGraph() {

		logger.info("** createGraph started **");

		if (ConfigurationManager.getConfigurationManager().getConfiguration().getTitanInMemoryGraph()) {
			BaseConfiguration conf = new BaseConfiguration();
			conf.setProperty("storage.backend", "inmemory");
			graph = TitanFactory.open(conf);
            createTitanSchema(); 
			logger.info("** in memory graph created");
			return TitanOperationStatus.OK;
		} else {
			this.titanCfgFile = titanClientStrategy.getConfigFile();
			if (titanCfgFile == null || titanCfgFile.isEmpty()) {
				titanCfgFile = "config/titan.properties";
			}

			// yavivi
			// In case connection failed on init time, schedule a reconnect task
			// in the BG
			TitanOperationStatus status = createGraph(titanCfgFile);
			logger.debug("Create Titan graph status {}", status);
			if (status != TitanOperationStatus.OK) {
				this.startReconnectTask();
			}

			return status;
		}
	}

	private void startHealthCheckTask() {
		this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, reconnectInterval, TimeUnit.SECONDS);
	}

	/**
	 * This method will be invoked ONLY on init time in case Titan storage is down.
	 */
	private void startReconnectTask() {
		this.reconnectTask = new ReconnectTask();
		// Initialize a single threaded scheduler
		this.reconnectScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Titan-Reconnect-Task");
			}
		});

		logger.info("Scheduling reconnect task {} with interval of {} seconds", reconnectTask, reconnectInterval);
		reconnectFuture = this.reconnectScheduler.scheduleAtFixedRate(this.reconnectTask, 0, this.reconnectInterval, TimeUnit.SECONDS);
	}

	public void cleanupGraph() {
		if (graph != null) {
			// graph.shutdown();
			graph.close();
			TitanCleanup.clear(graph);
		}
	}

	private boolean graphInitialized(){
		TitanManagement graphMgmt = graph.openManagement();
		return graphMgmt.containsPropertyKey(HEALTH_CHECK) && graphMgmt.containsGraphIndex(HEALTH_CHECK);
	}
	

	public TitanOperationStatus createGraph(String titanCfgFile) {
		logger.info("** open graph with {} started", titanCfgFile);
		try {
			logger.info("openGraph : try to load file {}", titanCfgFile);
			graph = TitanFactory.open(titanCfgFile);
			if (graph.isClosed() || !graphInitialized()) {
				logger.error("titan graph was not initialized");
				return TitanOperationStatus.NOT_CREATED;
			}

		} catch (Exception e) {
			this.graph = null;
			logger.info("createGraph : failed to open Titan graph with configuration file: {}", titanCfgFile, e);
			return TitanOperationStatus.NOT_CONNECTED;
		}

		logger.info("** Titan graph created ");

		// Do some post creation actions
		this.onGraphOpened();

		return TitanOperationStatus.OK;
	}

	private void onGraphOpened() {
		// if a reconnect task is running, cancel it.
		if (this.reconnectFuture != null) {
			logger.info("** Cancelling Titan reconnect task");
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


	public Either<TitanGraph, TitanOperationStatus> getGraph() {
		if (graph != null) {
			return Either.left(graph);
		} else {
			return Either.right(TitanOperationStatus.NOT_CREATED);
		}
	}

	public TitanOperationStatus commit() {
		if (graph != null) {
			try {
				graph.tx().commit();
				return TitanOperationStatus.OK;
			} catch (Exception e) {
				return handleTitanException(e);
			}
		} else {
			return TitanOperationStatus.NOT_CREATED;
		}
	}

	public TitanOperationStatus rollback() {
		if (graph != null) {
			try {
				// graph.rollback();
				graph.tx().rollback();
				return TitanOperationStatus.OK;
			} catch (Exception e) {
				return handleTitanException(e);
			}
		} else {
			return TitanOperationStatus.NOT_CREATED;
		}
	}

	public static TitanOperationStatus handleTitanException(Exception e) {
		if (e instanceof TitanConfigurationException) {
			return TitanOperationStatus.TITAN_CONFIGURATION;
		}
		if (e instanceof SchemaViolationException) {
			return TitanOperationStatus.TITAN_SCHEMA_VIOLATION;
		}
		if (e instanceof PermanentLockingException) {
			return TitanOperationStatus.TITAN_SCHEMA_VIOLATION;
		}
		if (e instanceof IDPoolExhaustedException) {
			return TitanOperationStatus.GENERAL_ERROR;
		}
		if (e instanceof InvalidElementException) {
			return TitanOperationStatus.INVALID_ELEMENT;
		}
		if (e instanceof InvalidIDException) {
			return TitanOperationStatus.INVALID_ID;
		}
		if (e instanceof QueryException) {
			return TitanOperationStatus.INVALID_QUERY;
		}
		if (e instanceof ResourceUnavailableException) {
			return TitanOperationStatus.RESOURCE_UNAVAILABLE;
		}
		if (e instanceof IllegalArgumentException) {
			// TODO check the error message??
			return TitanOperationStatus.ILLEGAL_ARGUMENT;
		}

		return TitanOperationStatus.GENERAL_ERROR;
	}

	public boolean getHealth() {
		return this.lastHealthState;
	}

	private boolean isGraphOpen() {
		healthLogger.trace("Invoking Titan health check ...");
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
				logger.error("Titan Health Check Failed. {}", message);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}


	public static void main(String[] args) throws InterruptedException {
		TitanGraphClient client = new TitanGraphClient(new DAOTitanStrategy());
		client.createGraph();

		while (true) {
			boolean health = client.isGraphOpen();
			System.err.println("health=" + health);
			Thread.sleep(2000);
		}

	}


	private static final String TITAN_HEALTH_CHECK_STR = "titanHealthCheck";

	private void logAlarm() {
		if (lastHealthState == true) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckRecovery, TITAN_HEALTH_CHECK_STR);
			BeEcompErrorManager.getInstance().logBeHealthCheckTitanRecovery(TITAN_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckError, TITAN_HEALTH_CHECK_STR);
			BeEcompErrorManager.getInstance().logBeHealthCheckTitanError(TITAN_HEALTH_CHECK_STR);
		}
	}
	
	private void createTitanSchema() {
		
		TitanManagement graphMgt = graph.openManagement();
		TitanGraphIndex index = null;
		for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
			PropertyKey propKey = null;
			if (!graphMgt.containsPropertyKey(prop.getProperty())) {
				Class<?> clazz = prop.getClazz();
				if (!ArrayList.class.getName().equals(clazz.getName()) && !HashMap.class.getName().equals(clazz.getName())) {
					propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
				}
			} else {
				propKey = graphMgt.getPropertyKey(prop.getProperty());
			}
			if (prop.isIndexed()) {
				if (!graphMgt.containsGraphIndex(prop.getProperty())) {
					if (prop.isUnique()) {
						index = graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).unique().buildCompositeIndex();

						graphMgt.setConsistency(propKey, ConsistencyModifier.LOCK); // Ensures
																					// only
																					// one
																					// name
																					// per
																					// vertex
						graphMgt.setConsistency(index, ConsistencyModifier.LOCK); // Ensures
																					// name
																					// uniqueness
																					// in
																					// the
																					// graph

					} else {
						graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).buildCompositeIndex();
					}
				}
			}
		}
		graphMgt.commit();
	}

}
