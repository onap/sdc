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

package org.openecomp.sdc.be.model.operations.impl;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.openecomp.sdc.be.model.cache.jobs.CheckAndUpdateJob;
import org.openecomp.sdc.be.model.cache.jobs.DeleteJob;
import org.openecomp.sdc.be.model.cache.jobs.Job;
import org.openecomp.sdc.be.model.cache.jobs.OverrideJob;
import org.openecomp.sdc.be.model.cache.jobs.StoreJob;
import org.openecomp.sdc.be.model.cache.workers.CacheWorker;
import org.openecomp.sdc.be.model.cache.workers.IWorker;
import org.openecomp.sdc.be.model.cache.workers.SyncWorker;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.workers.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Created by mlando on 9/5/2016. the class is responsible for handling all cache update operations asynchronously including sync between the graph and cache and on demand update requests
 */
@Component("cacheManger-operation")
public class CacheMangerOperation implements ICacheMangerOperation {
	@Autowired
	private ToscaOperationFacade toscaOperationFacade;
	@Autowired
	private TitanGenericDao titanGenericDao;
	@Autowired
	private ComponentCache componentCache;

	private static Logger log = LoggerFactory.getLogger(Manager.class.getName());
	private LinkedBlockingQueue<Job> jobQueue = null;
	private int waitOnShutDownInMinutes;
	private ScheduledExecutorService syncExecutor;
	private ExecutorService workerExecutor;
	private LinkedList<IWorker> workerList = new LinkedList<>();
	private DaoInfo daoInfo;

	/**
	 * constructor
	 */
	public CacheMangerOperation() {
	}

	/**
	 * the method checks in the cache is enabled, if it is, it initializes all the workers according to the configuration values.
	 */
	@PostConstruct
	public void init() {

		daoInfo = new DaoInfo(toscaOperationFacade, componentCache);

		Configuration.ApplicationL2CacheConfig applicationL2CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL2Cache();
		if (applicationL2CacheConfig != null && applicationL2CacheConfig.isEnabled()) {
			Integer numberOfWorkers = applicationL2CacheConfig.getQueue().getNumberOfCacheWorkers();
			this.waitOnShutDownInMinutes = applicationL2CacheConfig.getQueue().getWaitOnShutDownInMinutes();
			jobQueue = new LinkedBlockingQueue<>();
			log.info("L2 Cache is enabled inishilsing queue");
			log.debug("initializing SyncWorker, creating {} workers");
			ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Sync-Cache-Worker-%d").build();
			this.syncExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
			log.debug("initializing workers, creating {} cacheWorkers", numberOfWorkers);
			threadFactory = new ThreadFactoryBuilder().setNameFormat("Cache-Worker-%d").build();
			String workerName = "Sync-Worker";
			Integer syncWorkerExacutionIntrval = applicationL2CacheConfig.getQueue().getSyncIntervalInSecondes();
			log.debug("starting Sync worker:{} with executions interval:{} ", workerName, syncWorkerExacutionIntrval);
			SyncWorker syncWorker = new SyncWorker(workerName, this);
			this.syncExecutor.scheduleAtFixedRate(syncWorker, 5 * 60, syncWorkerExacutionIntrval, TimeUnit.SECONDS);
			this.workerExecutor = Executors.newFixedThreadPool(numberOfWorkers, threadFactory);
			CacheWorker cacheWorker;
			for (int i = 0; i < numberOfWorkers; i++) {
				workerName = "Cache-Worker-" + i;
				log.debug("starting Cache worker:{}", workerName);
				cacheWorker = new CacheWorker(workerName, jobQueue);
				this.workerExecutor.submit(cacheWorker);
				this.workerList.add(cacheWorker);
			}
		} else {
			log.info("L2 Cache is disabled");
		}
		log.info("L2 Cache has been initialized and the workers are running");
	}

	/**
	 * the method creates a job to check it the given component is in the cach and if so is it valid if the value in the cache is not valid it will be updated.
	 * 
	 * @param componentId
	 *            the uid of the component we want to update
	 * @param timestamp
	 *            the time of the component update
	 * @param nodeTypeEnum
	 *            the type of the component resource/service/product
	 */
	@Override
	public void updateComponentInCache(String componentId, long timestamp, NodeTypeEnum nodeTypeEnum) {
		Configuration.ApplicationL2CacheConfig applicationL2CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL2Cache();
		if (applicationL2CacheConfig != null && applicationL2CacheConfig.isEnabled()) {
			this.jobQueue.add(new CheckAndUpdateJob(daoInfo, componentId, nodeTypeEnum, timestamp));
		}
	}

	public void overideComponentInCache(String componentId, long timestamp, NodeTypeEnum nodeTypeEnum) {
		Configuration.ApplicationL2CacheConfig applicationL2CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL2Cache();
		if (applicationL2CacheConfig != null && applicationL2CacheConfig.isEnabled()) {
			this.jobQueue.add(new OverrideJob(daoInfo, componentId, nodeTypeEnum, timestamp));
		}
	}

	public void deleteComponentInCache(String componentId, long timestamp, NodeTypeEnum nodeTypeEnum) {
		Configuration.ApplicationL2CacheConfig applicationL2CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL2Cache();
		if (applicationL2CacheConfig != null && applicationL2CacheConfig.isEnabled()) {
			this.jobQueue.add(new DeleteJob(daoInfo, componentId, nodeTypeEnum, timestamp));
		}
	}

	/**
	 * the method stores the given component in the cache
	 * 
	 * @param component
	 *            componet to store in cache
	 * @param nodeTypeEnum
	 *            the type of the component we want to store
	 */
	@Override
	public void storeComponentInCache(org.openecomp.sdc.be.model.Component component, NodeTypeEnum nodeTypeEnum) {
		Configuration.ApplicationL2CacheConfig applicationL2CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL2Cache();
		if (applicationL2CacheConfig != null && applicationL2CacheConfig.isEnabled()) {
			this.jobQueue.add(new StoreJob(daoInfo, component, nodeTypeEnum));
		}
	}

	/**
	 * the method shutdown's all the worker's. the method has a pre set of how long it will wait for the workers to shutdown. the pre defined value is taken from the configuration.
	 */
	@PreDestroy
	public void shutDown() {
		workerExecutor.shutdown();
		syncExecutor.shutdown();
		this.workerList.forEach(e -> e.shutDown());
		try {
			if (!workerExecutor.awaitTermination(this.waitOnShutDownInMinutes, TimeUnit.MINUTES)) {
				log.error("timer elapsed while waiting for Cache workers to finish, forcing a shutdown. ");
			}
			log.debug("all Cache workers finished");
		} catch (InterruptedException e) {
			log.error("failed while waiting for Cache worker", e);
		}
		try {
			if (!workerExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
				log.error("timer elapsed while waiting for the Sync worker's to finish, forcing a shutdown. ");
			}
			log.debug("sync worker finished");
		} catch (InterruptedException e) {
			log.error("failed while waiting for sync worker", e);
		}
	}

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	public ComponentCache getComponentCache() {
		return componentCache;
	}
}
