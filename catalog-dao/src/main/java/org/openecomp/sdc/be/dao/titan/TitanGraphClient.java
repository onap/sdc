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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.CategoryData;
import org.openecomp.sdc.be.resources.data.ResourceCategoryData;
import org.openecomp.sdc.be.resources.data.ServiceCategoryData;
import org.openecomp.sdc.be.resources.data.UserData;
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
import com.thinkaurelius.titan.core.TitanGraphQuery;
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
				createGraph(titanCfgFile, true);
			}
		}
	}

	private TitanGraph graph;

	// Health Check Variables

	/**
	 * This executor will execute the health check task on a callable task that
	 * can be executed with a timeout.
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

	public TitanGraphClient() {
		super();

		// Initialize a single threaded scheduler for health-check
		this.healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Titan-Health-Check-Task");
			}
		});

		healthCheckReadTimeout = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getTitanHealthCheckReadTimeout(2);
		reconnectInterval = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getTitanReconnectIntervalInSeconds(3);

		logger.info("** TitanGraphClient created");
	}

	@PostConstruct
	public TitanOperationStatus createGraph() {

		logger.info("** createGraph started **");

		if (ConfigurationManager.getConfigurationManager().getConfiguration().getTitanInMemoryGraph()) {
			BaseConfiguration conf = new BaseConfiguration();
			conf.setProperty("storage.backend", "inmemory");
			graph = TitanFactory.open(conf);

			createIndexesAndDefaults();

			logger.info("** in memory graph created");
			return TitanOperationStatus.OK;
		} else {
			this.titanCfgFile = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanCfgFile();
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
	 * This method will be invoked ONLY on init time in case Titan storage is
	 * down.
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
		reconnectFuture = this.reconnectScheduler.scheduleAtFixedRate(this.reconnectTask, 0, this.reconnectInterval,
				TimeUnit.SECONDS);
	}

	public void cleanupGraph() {
		if (graph != null) {
			// graph.shutdown();
			graph.close();
			TitanCleanup.clear(graph);
		}
	}

	public TitanOperationStatus createGraph(String titanCfgFile) {
		logger.info("** createGraph with " + titanCfgFile + " started");
		return createGraph(titanCfgFile, true);
	}

	public TitanOperationStatus createGraph(String titanCfgFile, boolean initializeGraph) {
		logger.info("** createGraph with " + titanCfgFile + " and initializeGraph=" + initializeGraph + " started");
		try {
			logger.info("createGraph : try to load file " + titanCfgFile);
			graph = TitanFactory.open(titanCfgFile);
			if (graph == null) {
				return TitanOperationStatus.NOT_CREATED;
			}

		} catch (Exception e) {
			this.graph = null;
			logger.info("createGraph : failed to open Titan graph with configuration file: " + titanCfgFile);
			logger.trace("createGraph : failed to open Titan graph. ", e);
			return TitanOperationStatus.NOT_CONNECTED;
		}
		if (true == initializeGraph) {
			createIndexesAndDefaults();
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

	private void createIndexesAndDefaults() {
		createVertexIndixes();
		createEdgeIndixes();
		createDefaultUsers();
		// createCategories();
	}

	public Either<TitanGraph, TitanOperationStatus> getGraph() {
		if (graph != null) {
			return Either.left(graph);
		} else {
			return Either.right(TitanOperationStatus.NOT_CREATED);
		}
	}

	private TitanOperationStatus createVertexIndixes() {
		logger.info("** createVertexIndixes started");
		if (graph != null) {
			// TitanManagement graphMgt = graph.getManagementSystem();
			TitanManagement graphMgt = graph.openManagement();
			TitanGraphIndex index = null;
			for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
				PropertyKey propKey = null;
				if (!graphMgt.containsPropertyKey(prop.getProperty())) {
					Class<?> clazz = prop.getClazz();
					if (!ArrayList.class.getName().equals(clazz.getName())
							&& !HashMap.class.getName().equals(clazz.getName())) {
						propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
					}
				} else {
					propKey = graphMgt.getPropertyKey(prop.getProperty());
				}
				if (prop.isIndexed()) {
					if (!graphMgt.containsGraphIndex(prop.getProperty())) {
						if (prop.isUnique()) {
							index = graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).unique()
									.buildCompositeIndex();

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
			logger.info("** createVertexIndixes ended");
			return TitanOperationStatus.OK;
		}
		logger.info("** createVertexIndixes ended, no Indixes were created.");
		return TitanOperationStatus.NOT_CREATED;
	}

	private TitanOperationStatus createEdgeIndixes() {
		logger.info("** createEdgeIndixes started");
		if (graph != null) {
			// TitanManagement graphMgt = graph.getManagementSystem();
			TitanManagement graphMgt = graph.openManagement();
			for (GraphEdgePropertiesDictionary prop : GraphEdgePropertiesDictionary.values()) {
				if (!graphMgt.containsGraphIndex(prop.getProperty())) {
					PropertyKey propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
					graphMgt.buildIndex(prop.getProperty(), Edge.class).addKey(propKey).buildCompositeIndex();

				}
			}
			graphMgt.commit();
			logger.info("** createEdgeIndixes ended");
			return TitanOperationStatus.OK;
		}
		logger.info("** createEdgeIndixes ended, no Indixes were created.");
		return TitanOperationStatus.NOT_CREATED;
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
				healthLogger.trace("Health Check Node Found... " + v.property(HEALTH_CHECK));
				graph.tx().commit();
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null) {
					message = e.getClass().getName();
				}
				logger.error("Titan Health Check Failed. " + message);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	private TitanOperationStatus createCategories() {
		logger.info("** createCategories started");
		if (graph != null) {
			try {
				List<CategoryData> categoryDataList = getDefaultResourceCategoryList();
				generateGraphCategories(categoryDataList, NodeTypeEnum.ResourceCategory.getName());
			} catch (Exception e) {
				logger.error("createCategories : failed to load categories ", e);
				rollback();
				return TitanOperationStatus.GENERAL_ERROR;
			} finally {
				logger.info("** createCategories ended");
				commit();
			}
		}
		return TitanOperationStatus.OK;
	}

	private void generateGraphCategories(List<CategoryData> categoryDataList, String label) {
		for (CategoryData categoryData : categoryDataList) {
			Map<String, Object> properties = categoryData.toGraphMap();
			properties.put(GraphPropertiesDictionary.LABEL.getProperty(), label);

			// This is temporary for old mechnism of categories - they don;t
			// have the norm name
			properties.remove(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty());

			if (isVertexExist(properties)) {
				return;
			}

			Vertex vertex = graph.addVertex();
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				vertex.property(entry.getKey(), entry.getValue());
			}
		}
	}

	private List<CategoryData> getDefaultResourceCategoryList() {
		Map<String, List<String>> categories = new HashMap<String, List<String>>();
		categories.put("Network Layer 2-3", Arrays.asList("Router", "Gateway", "WAN Connectors", "LAN Connectors"));
		categories.put("Network Layer 4+", Arrays.asList("Common Network Resources"));
		categories.put("Application Layer 4+", Arrays.asList("Border Elements", "Application Servers", "Web Server",
				"Call Control", "Media Servers", "Load Balancer", "Database"));
		categories.put("Generic", Arrays.asList("Infrastructure", "Abstract", "Network Elements", "Database"));

		List<CategoryData> categoryDataList = new ArrayList<CategoryData>();
		CategoryData categoryData = null;
		for (Map.Entry<String, List<String>> entryList : categories.entrySet()) {
			for (String val : entryList.getValue()) {
				categoryData = new ResourceCategoryData(entryList.getKey(), val);
				categoryData.setAction(ActionEnum.Create);
				categoryData.setElementType(GraphElementTypeEnum.Node);
				// categoryData.setCategoryName();
				// categoryData.setName();
				categoryDataList.add(categoryData);
			}
		}
		return categoryDataList;
	}

	private List<CategoryData> getDefaultServiceCategoryList() {
		List<String> categories = new ArrayList<>();
		categories.add("Mobility");
		categories.add("Network L1-3");
		categories.add("Network L4");
		categories.add("VoIP Call Control");

		List<CategoryData> categoryDataList = new ArrayList<CategoryData>();
		CategoryData categoryData = null;
		for (String category : categories) {
			categoryData = new ServiceCategoryData(category);
			categoryData.setAction(ActionEnum.Create);
			categoryData.setElementType(GraphElementTypeEnum.Node);
			// categoryData.setCategoryName(entryList.getKey());
			// categoryData.setName(val);
			categoryDataList.add(categoryData);
		}
		return categoryDataList;
	}

	private void createDefaultUsers() {
		if (graph != null) {
			List<UserData> users = createUserList();
			for (UserData user : users) {
				String propertyName = GraphPropertiesDictionary.USER_ID.getProperty();
				String propertyValue = user.getUserId();
				Vertex vertex = null;
				Map<String, Object> properties = null;
				if (!isVertexExist(propertyName, propertyValue)) {
					vertex = graph.addVertex();
					vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());
					properties = user.toGraphMap();
					for (Map.Entry<String, Object> entry : properties.entrySet()) {
						vertex.property(entry.getKey(), entry.getValue());
					}
				}
			}
			graph.tx().commit();
		}
	}

	private List<UserData> createUserList() {
		LinkedList<UserData> users = new LinkedList<UserData>();
		users.add(getDefaultUserAdmin1());
		users.add(getDefaultUserAdmin2());
		users.add(getDefaultUserAdmin3());
		users.add(getDefaultUserDesigner1());
		users.add(getDefaultUserDesigner2());
		users.add(getDefaultUserDesigner3());
		users.add(getDefaultUserTester1());
		users.add(getDefaultUserTester2());
		users.add(getDefaultUserTester3());
		users.add(getDefaultUserGovernor1());
		users.add(getDefaultUserOps1());
		users.add(getDefaultUserProductManager1());
		users.add(getDefaultUserProductManager2());
		users.add(getDefaultUserProductStrategist1());
		users.add(getDefaultUserProductStrategist2());
		users.add(getDefaultUserProductStrategist3());
		return users;
	}

	private boolean isVertexExist(String propertyName, String propertyValue) {
		// Iterable<Vertex> vertecies = graph.getVertices(propertyName,
		// propertyValue);
		// java.util.Iterator<Vertex> iterator = vertecies.iterator();

		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertecies = graph.query().has(HEALTH_CHECK, OK).vertices();

		java.util.Iterator<TitanVertex> iterator = vertecies.iterator();
		if (iterator.hasNext()) {
			return true;
		}
		return false;
	}

	private boolean isVertexExist(Map<String, Object> properties) {
		TitanGraphQuery query = graph.query();

		if (properties != null && !properties.isEmpty()) {
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Iterable<Vertex> vertecies = query.vertices();
		java.util.Iterator<Vertex> iterator = vertecies.iterator();
		if (iterator.hasNext()) {
			return true;
		}
		return false;
	}

	private UserData getDefaultUserAdmin1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jh0003");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Jimmy");
		userData.setLastName("Hendrix");
		userData.setRole("ADMIN");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserAdmin2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("tr0001");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Todd");
		userData.setLastName("Rundgren");
		userData.setRole("ADMIN");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserAdmin3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ah0002");
		userData.setEmail("admin@sdc.com");
		userData.setFirstName("Alex");
		userData.setLastName("Harvey");
		userData.setRole("ADMIN");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserDesigner1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("cs0008");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Carlos");
		userData.setLastName("Santana");
		userData.setRole("DESIGNER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserDesigner2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("me0009");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Melissa");
		userData.setLastName("Etheridge");
		userData.setRole("DESIGNER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserDesigner3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("af0006");
		userData.setEmail("designer@sdc.com");
		userData.setFirstName("Aretha");
		userData.setLastName("Franklin");
		userData.setRole("DESIGNER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserTester1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jm0007");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("Joni");
		userData.setLastName("Mitchell");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserTester2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("kb0004");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("Kate");
		userData.setLastName("Bush");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserTester3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("jt0005");
		userData.setEmail("tester@sdc.com");
		userData.setFirstName("James");
		userData.setLastName("Taylor");
		userData.setRole("TESTER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserOps1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("op0001");
		userData.setEmail("ops@sdc.com");
		userData.setFirstName("Steve");
		userData.setLastName("Regev");
		userData.setRole("OPS");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserGovernor1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("gv0001");
		userData.setEmail("governor@sdc.com");
		userData.setFirstName("David");
		userData.setLastName("Shadmi");
		userData.setRole("GOVERNOR");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserProductManager1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("pm0001");
		userData.setEmail("pm1@sdc.com");
		userData.setFirstName("Teddy");
		userData.setLastName("Isashar");
		userData.setRole("PRODUCT_MANAGER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserProductManager2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("pm0002");
		userData.setEmail("pm2@sdc.com");
		userData.setFirstName("Sarah");
		userData.setLastName("Bettens");
		userData.setRole("PRODUCT_MANAGER");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserProductStrategist1() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0001");
		userData.setEmail("ps1@sdc.com");
		userData.setFirstName("Eden");
		userData.setLastName("Rozin");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserProductStrategist2() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0002");
		userData.setEmail("ps2@sdc.com");
		userData.setFirstName("Ella");
		userData.setLastName("Kvetny");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	private UserData getDefaultUserProductStrategist3() {
		UserData userData = new UserData();
		userData.setAction(ActionEnum.Create);
		userData.setElementType(GraphElementTypeEnum.Node);
		userData.setUserId("ps0003");
		userData.setEmail("ps3@sdc.com");
		userData.setFirstName("Geva");
		userData.setLastName("Alon");
		userData.setRole("PRODUCT_STRATEGIST");
		userData.setStatus(UserStatusEnum.ACTIVE.name());
		userData.setLastLoginTime(0L);
		return userData;
	}

	public static void main(String[] args) throws InterruptedException {
		TitanGraphClient client = new TitanGraphClient();
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
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckRecovery,
					TITAN_HEALTH_CHECK_STR);
			BeEcompErrorManager.getInstance().logBeHealthCheckTitanRecovery(TITAN_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckError,
					TITAN_HEALTH_CHECK_STR);
			BeEcompErrorManager.getInstance().logBeHealthCheckTitanError(TITAN_HEALTH_CHECK_STR);
		}
	}
}
