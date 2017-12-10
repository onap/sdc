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

package org.openecomp.sdc.be.model.cache.jobs;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

/**
 * Created by mlando on 9/7/2016.
 */
public class CheckAndUpdateJob extends Job {
	private static Logger log = LoggerFactory.getLogger(CheckAndUpdateJob.class.getName());

	public CheckAndUpdateJob(DaoInfo daoInfo, String componentId, NodeTypeEnum nodeTypeEnum, long timestamp) {
		super(daoInfo, componentId, nodeTypeEnum, timestamp);
	}

	@Override
	public Object doWork() {
		log.trace("starting work on job.");
		log.trace("update cache for componentId:{} of nodeTypeEnum:{} with timestamp:{}.", componentId, nodeTypeEnum,
				timestamp);

		try {

			// get from cache
			Either<ImmutablePair<Component, Long>, ActionStatus> cacheResult = daoInfo.getComponentCache()
					.getComponentAndTime(componentId, Function.identity());
			// if error while getting from cache abort and update
			if (cacheResult.isRight()) {
				// genral error
				if (!ActionStatus.RESOURCE_NOT_FOUND.equals(cacheResult.right().value())
						&& !ActionStatus.INVALID_CONTENT.equals(cacheResult.right().value())) {
					log.debug("failed to get component:{} from cache error:{}", componentId,
							cacheResult.right().value());
					return false;
				}
				// component not in cache put there
				else {
					return updateCache(componentId, nodeTypeEnum, timestamp);
				}
			}
			ImmutablePair<Component, Long> recored = cacheResult.left().value();
			// the cache has allready been updated exit
			if (this.timestamp < recored.getRight()) {
				log.debug("job timestemp:{} is smaller then the cache timestamp:{} no update is needed.",
						this.timestamp, recored.getRight());
				return false;
			}
			return updateCache(componentId, nodeTypeEnum, timestamp);

		} catch (Exception e) {
			log.debug("an exception was encountered during CheckAndUpdateJob", e);
		} finally {
			daoInfo.getToscaOperationFacade().commit();
		}
		return false;
	}

	/**
	 * @param componentId
	 * @param nodeTypeEnum
	 * @return
	 */
	private boolean updateCache(String componentId, NodeTypeEnum nodeTypeEnum, Long timestamp) {
		// get component from cache
		Either<ComponentMetadataData, StorageOperationStatus> metaDataRes = getComponentMetaData(componentId,
				nodeTypeEnum);
		if (metaDataRes.isRight()) {
			return false;
		}
		ComponentMetadataData metaData = metaDataRes.left().value();
		// the job time is older then the one on graph nothing to do there is a
		// job that will handle this.
		Long graphTimestamp = metaData.getMetadataDataDefinition().getLastUpdateDate();
		if (timestamp < graphTimestamp) {
			log.debug(
					"the job timestamp:{} is smaller then the graph timestamp:{}. exiting because another job will update the cache.",
					timestamp, graphTimestamp);
			return false;
		} else {
			// update cache
			// get component from grath
			Either<Component, StorageOperationStatus> componentRes = daoInfo.getToscaOperationFacade().getToscaElement(componentId);
			if (componentRes.isRight()) {
				log.debug("failed to get full component:{} from graph status:{}", componentId,
						componentRes.right().value());
				return false;
			}
			Component component = componentRes.left().value();
			// store in cache
			if (!this.daoInfo.getComponentCache().setComponent(component, nodeTypeEnum)) {
				log.debug("failed to store componentId:{} nodeTypeEnum:", componentId, nodeTypeEnum);
				return false;
			}
		}
		log.debug("cache successfully updated for componentId:{} nodeTypeEnum:{} timestemp:{}.", componentId,
				nodeTypeEnum, timestamp);
		return true;
	}

}
