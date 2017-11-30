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

package org.openecomp.sdc.be.model.cache.workers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

/**
 * the class creates a worker that is used to update cache date, in case of
 * failures and inconsistencies
 */
public class SyncWorker implements Runnable, IWorker {

	private static Logger log = LoggerFactory.getLogger(SyncWorker.class.getName());
	private final CacheMangerOperation cacheMangerOperation;
	private final String workerName;
	private volatile boolean shutdown = false;
	private Map<String, ComponentCacheData> cacheIdAndTimeMap;
	private long updateDelayInMilliseconds = 60 * 60 * 1000;

	/**
	 * creates the sync worker
	 * 
	 * @param workerName
	 *            the name of the worker
	 * @param cacheMangerOperation
	 *            responsible for all persistence's operations to graph and the
	 *            cache
	 */
	public SyncWorker(String workerName, CacheMangerOperation cacheMangerOperation) {
		this.workerName = workerName;
		this.cacheMangerOperation = cacheMangerOperation;
	}

	/**
	 * the method collects all the resources/services/products from graph and
	 * checks if the component representing them in the cache is valid logic: if
	 * the record is present in the graph but not in cache -> create a job that
	 * will update the record oin cache if the timestamp of the record in cache
	 * is older than the timestamp on the graph -> create a job that will update
	 * the record oin cache otherwise no update is required
	 */
	@Override
	public void run() {
		try {
			collectAllCacheRecords();
			syncCacheByComponentType(NodeTypeEnum.Resource);
			syncCacheByComponentType(NodeTypeEnum.Service);
			syncCacheByComponentType(NodeTypeEnum.Product);
			clearCacheRecords();

		} catch (Exception e) {
			log.debug("sync worker:{} encounered an exception", workerName);
			log.debug("exception", e);
		} finally {
			this.cacheMangerOperation.getTitanGenericDao().commit();
		}
	}

	/**
	 * the method checks for each component in the cache except the ones that
	 * were update during the sync, if they exist on the graph if not a job to
	 * remove them is created
	 */
	private void clearCacheRecords() {
		cacheIdAndTimeMap.forEach((k, v) -> {
			try {
				Either<ComponentMetadataData, TitanOperationStatus> componentFromGraphRes = getComponentMetaData(k,
						NodeTypeEnum.getByName(v.getType()));
				if (componentFromGraphRes.isRight()) {
					TitanOperationStatus error = componentFromGraphRes.right().value();
					if (TitanOperationStatus.NOT_FOUND.equals(error)) {
						long delay = System.currentTimeMillis() - v.getModificationTime().getTime();
						if (delay > updateDelayInMilliseconds) {
							this.cacheMangerOperation.deleteComponentInCache(k, v.getModificationTime().getTime(),
									NodeTypeEnum.getByName(v.getType()));
						} else {
							log.trace(
									"no delete done because an hour did not pass since the delete was done  timeSinceUpdate {} < updateDelayInMilliseconds {} ",
									delay, updateDelayInMilliseconds);
						}
					} else {
						log.debug("failed to get metadata for id:{} from graph error:{}", k, error);
					}
				} else {
					log.trace("id {} is in graph nothing to do");
				}
			} catch (Exception e) {
				log.debug("during clean cache records an exception was thrown", e);
			}
		});
	}

	/**
	 * the method collects all the records from cache except the component
	 * itself
	 */
	public void collectAllCacheRecords() {
		Either<List<ComponentCacheData>, ActionStatus> getAllRes = this.cacheMangerOperation.getComponentCache()
				.getAllComponentIdTimeAndType();
		if (getAllRes.isRight()) {
			log.debug("error while trying to get all records from cache error:{}", getAllRes.right().value());
			cacheIdAndTimeMap = new HashMap<>();
		} else {
			cacheIdAndTimeMap = getAllRes.left().value().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		}
	}

	/**
	 * the method checks that the records ot the given type are sync between the
	 * cache and the graph
	 * 
	 * @param nodeTypeEnum
	 *            the type of components we want to sync
	 */
	private void syncCacheByComponentType(NodeTypeEnum nodeTypeEnum) {
		if (!this.shutdown) {
			log.trace("syncCache records of type:{} .", nodeTypeEnum);
			Either<List<ComponentMetadataData>, TitanOperationStatus> getAllResult = getAllComponentsMetaData(
					nodeTypeEnum);
			List<ComponentMetadataData> componentList = new ArrayList<>();
			if (getAllResult.isRight() && !TitanOperationStatus.NOT_FOUND.equals(getAllResult.right().value())) {
				log.debug("error while trying to get all components of type:{} TitanOperationStatus:{}.", nodeTypeEnum,
						getAllResult.right().value());
				return;
			}
			if (getAllResult.isLeft()) {
				componentList = getAllResult.left().value();
				log.trace("get all components of type:{} returned:{} components.", nodeTypeEnum, componentList.size());
			}
			componentList.forEach(this::checkAndUpdateCacheComponent);
			log.trace("syncCache records of type:{} was successful.", nodeTypeEnum);
		}
	}

	/**
	 * the method compares the given component to the record in the cache if the
	 * record is not in the cache a job to update the cache for this record will
	 * be created. if the record is present in the graph but not in cache ->
	 * create a job that will update the record oin cache if the timestamp of
	 * the record in cache is older than the timestamp on the graph -> create a
	 * job that will update the record oin cache if the retried component from
	 * cache fails to be deserialized -> create job to override it otherwise no
	 * update is required
	 * 
	 * @param metadataData
	 *            the date of the node we want to compare to the value in the
	 *            cache
	 */
	private void checkAndUpdateCacheComponent(ComponentMetadataData metadataData) {
		long timeSinceUpdate = System.currentTimeMillis()
				- metadataData.getMetadataDataDefinition().getLastUpdateDate();
		if (timeSinceUpdate >= updateDelayInMilliseconds) {
			String uid = metadataData.getMetadataDataDefinition().getUniqueId();
			log.trace("checking cache if record for uid:{} needs to be updated.", uid);
			Either<Component, ActionStatus> cacheResult = this.cacheMangerOperation.getComponentCache()
					.getComponent(uid);
			if (cacheResult.isRight()) {
				ActionStatus actionStatus = cacheResult.right().value();
				if (ActionStatus.RESOURCE_NOT_FOUND.equals(actionStatus)) {
					log.trace("record for uid:{} not found in cache. creating an update job.", uid);
					this.cacheMangerOperation.updateComponentInCache(uid,
							metadataData.getMetadataDataDefinition().getLastUpdateDate(),
							NodeTypeEnum.getByName(metadataData.getLabel()));
				} else if (ActionStatus.CONVERT_COMPONENT_ERROR.equals(actionStatus)) {
					log.trace("uid:{} found in cache but we failed deserializing it. creating an override job  .", uid);
					this.cacheMangerOperation.overideComponentInCache(uid,
							metadataData.getMetadataDataDefinition().getLastUpdateDate(),
							NodeTypeEnum.getByName(metadataData.getLabel()));
				} else {
					log.debug("during lookup for uid:{} an error accords status:{} .", uid, actionStatus);
				}
			} else {
				log.trace("uid:{} found in cache.", uid);
				this.cacheIdAndTimeMap.remove(uid);
				Component cacheComponent = cacheResult.left().value();
				Long cacheTimestamp = cacheComponent.getLastUpdateDate();
				Long graphTimestamp = metadataData.getMetadataDataDefinition().getLastUpdateDate();
				if (cacheTimestamp < graphTimestamp) {
					log.trace("uid:{} found in cache. cache Timestamp {} < graph timestamp , creating an update job  .",
							uid, cacheTimestamp, graphTimestamp);
					this.cacheMangerOperation.updateComponentInCache(uid, graphTimestamp,
							NodeTypeEnum.getByName(metadataData.getLabel()));
				} else {
					log.trace("uid:{} found in cache. cache Timestamp {} => graph timestamp , no update is needed .",
							uid, cacheTimestamp, graphTimestamp);
				}
			}
		} else {
			log.trace(
					"no update done because an hour did not pass since the update was done  timeSinceUpdate {} < updateDelayInMilliseconds {} ",
					timeSinceUpdate, updateDelayInMilliseconds);
		}
	}

	/**
	 * the method sets the shutdown flag, when set the worker will stop it's
	 * execution as soon as possible with out completing its work
	 */
	@Override
	public void shutDown() {
		log.debug("syncWorker {} shuting down.", workerName);
		this.shutdown = true;
	}

	/**
	 * the method retrives all nodes matching the given node type from the graph
	 * 
	 * @param nodeTypeEnum
	 *            node type we want to lookup on the graph
	 * @return a list of retrieved nodes matching the given type or not found in
	 *         case no nodes were found or error in case of failure
	 */
	private Either<List<ComponentMetadataData>, TitanOperationStatus> getAllComponentsMetaData(
			NodeTypeEnum nodeTypeEnum) {
		return this.cacheMangerOperation.getTitanGenericDao().getByCriteria(nodeTypeEnum, null,
				ComponentMetadataData.class);
	}

	/**
	 * the method retrieves the metadata from graph for the given id
	 * 
	 * @param uid
	 *            the unique id of the component we want to retrieve
	 * @param nodeTypeEnum
	 *            the type of the recored we want to retrieve
	 * @return the meta dat of the component or the error encountered during the
	 *         get
	 */
	private Either<ComponentMetadataData, TitanOperationStatus> getComponentMetaData(String uid,
			NodeTypeEnum nodeTypeEnum) {
		return this.cacheMangerOperation.getTitanGenericDao().getNode(UniqueIdBuilder.getKeyByNodeType(nodeTypeEnum),
				uid, ComponentMetadataData.class);
	}
}
