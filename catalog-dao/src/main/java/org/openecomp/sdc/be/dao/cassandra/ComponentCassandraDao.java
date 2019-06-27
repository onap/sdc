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

package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component("component-cassandra-dao")
public class ComponentCassandraDao extends CassandraDao {

	private static Logger logger = Logger.getLogger(ComponentCassandraDao.class.getName());

	public final static Integer DEFAULT_FETCH_SIZE = 500;

	private ComponentCacheAccessor componentCacheAccessor;


	@Autowired
	public ComponentCassandraDao(CassandraClient cassandraClient) {
		super(cassandraClient);
	}

	@PostConstruct
	public void init() {
		String keyspace = AuditingTypesConstants.COMPONENT_KEYSPACE;
		if (client.isConnected()) {
			Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result = client.connect(keyspace);
			if (result.isLeft()) {
				session = result.left().value().left;
				manager = result.left().value().right;
				componentCacheAccessor = manager.createAccessor(ComponentCacheAccessor.class);
				logger.info("** ComponentCassandraDao created");
			} else {
				logger.info("** ComponentCassandraDao failed");
				throw new RuntimeException("Artifact keyspace [" + keyspace + "] failed to connect with error : "
						+ result.right().value());
			}
		} else {
			logger.info("** Cassandra client isn't connected");
			logger.info("** ComponentCassandraDao created, but not connected");
		}
	}

	/**
	 * 
	 * @param ids
	 *            - list of components unique id
	 * @return
	 */
	public Either<List<ComponentCacheData>, ActionStatus> getComponents(List<String> ids) {

		List<ComponentCacheData> components = new ArrayList<>();
		if (ids == null || ids.isEmpty()) {
			return Either.left(components);
		}

		try {

			Result<ComponentCacheData> events = componentCacheAccessor.getComponents(ids);
			if (events == null) {
				logger.trace("not found component for ids list of in size {}", ids.size());
				return Either.left(components);
			}
			events.all().forEach(event -> {
				components.add(event);
				logger.trace("Fetch component uid = {} isDirty = {}", event.getId(), event.getIsDirty());
			});

			logger.debug("Number of components to fetch was {}. Actually, {} components fetched", ids.size(),
					components.size());

			return Either.left(components);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("GetComponentsFromCache");

			logger.debug("failed to get components from cache", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public Either<List<ComponentCacheData>, ActionStatus> getAllComponentIdTimeAndType() {
		try {
			List<ComponentCacheData> components = new ArrayList<>();
			Result<ComponentCacheData> events = componentCacheAccessor.getAllComponentIdTimeAndType();
			if (events == null) {
				logger.trace("no component found ");
				return Either.left(components);
			}
			events.all().forEach(event -> {
				components.add(event);
					logger.trace("Fetch component uid = {} isDirty = {}", event.getId(), event.getIsDirty());
			});

			logger.debug("Number of components fetched was {}.", components.size());

			return Either.left(components);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("GetComponentsFromCache");

			logger.debug("failed to get components from cache", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	/**
	 * 
	 * @param id
	 *            - component unique id
	 * @return
	 */
	public Either<ComponentCacheData, ActionStatus> getComponent(String id) {

		if (id == null) {
			return Either.right(ActionStatus.INVALID_CONTENT);
		}

		try {

			Result<ComponentCacheData> events = componentCacheAccessor.getComponent(id);
			if (events == null) {
				logger.trace("not found component for id {}", id);
				return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
			}

			ComponentCacheData componentCacheData = events.one();
			if (componentCacheData != null) {
				logger.debug("Component with id {} was found. isDirty={}.", componentCacheData.getId(),
						componentCacheData.getIsDirty());
			} else {
				return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
			}
			return Either.left(componentCacheData);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("GetComponentFromCache");

			logger.trace("Failed to get component from cache", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public CassandraOperationStatus saveComponent(ComponentCacheData componentCacheData) {
		return client.save(componentCacheData, ComponentCacheData.class, manager);
	}

	/**
	 * the method checks if the given table is empty in the artifact keyspace
	 * 
	 * @param tableName
	 *            the name of the table we want to check
	 * @return true if the table is empty
	 */
	public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
		return super.isTableEmpty(tableName);
	}

	/**
	 * 
	 * @param idToTimestampMap
	 *            - list of components unique id
	 * @return
	 */
	public Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> getComponents(
			Map<String, Long> idToTimestampMap) {

		List<ComponentCacheData> components = new ArrayList<>();
		Set<String> notFetchedFromCache = new HashSet<>();
		ImmutablePair<List<ComponentCacheData>, Set<String>> result = new ImmutablePair<>(
                components, notFetchedFromCache);

		if (idToTimestampMap == null || idToTimestampMap.isEmpty()) {
			return Either.left(result);
		}

		try {

			Set<String> keySet = idToTimestampMap.keySet();
			List<String> ids = new ArrayList<>();
			ids.addAll(keySet);
			Result<ComponentCacheData> events = componentCacheAccessor.getComponents(ids);
			if (events == null) {
				logger.trace("not found component for ids list of in size {}", ids.size());
				notFetchedFromCache.addAll(idToTimestampMap.keySet());
				return Either.left(result);
			}
			events.all().forEach(event -> {
				long timeFromCache = event.getModificationTime().getTime();
				long timeRequested = idToTimestampMap.get(event.getId());
				if (timeFromCache == timeRequested) {
					logger.trace("Fetch component uid = {} from cache", event.getId());
					components.add(event);
				} else {
					logger.trace(
							"Fetch and ignore component uid = {} from cache. Time requested is {} while timestamp in cache is {}",
							event.getId(), timeRequested, timeFromCache);
				}
			});

			logger.debug("Number of components to fetch was {}. Actually, {} components fetched", ids.size(),
					components.size());
			List<String> foundComponents = components.stream().map(ComponentCacheData::getId).collect(Collectors.toList());
			// fetch all ids which was not found in cache/found in cache and not
			// updated.
			Set<String> notFoundComponents = idToTimestampMap.keySet().stream()
                                                             .filter(p -> !foundComponents.contains(p)).collect(Collectors.toSet());

			notFetchedFromCache.addAll(notFoundComponents);

			return Either.left(result);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("GetComponentsFromCache");

			logger.debug("failed to get components from cache", e);
			return Either.right(ActionStatus.GENERAL_ERROR);
		}
	}

	public CassandraOperationStatus deleteComponent(String id) {
		return client.delete(id, ComponentCacheData.class, manager);
	}

}
