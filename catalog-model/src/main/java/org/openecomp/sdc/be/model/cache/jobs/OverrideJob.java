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

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

/**
 * Created by mlando on 9/20/2016.
 */
public class OverrideJob extends Job {
	private static Logger log = LoggerFactory.getLogger(OverrideJob.class.getName());

	public OverrideJob(DaoInfo daoInfo, String componentId, NodeTypeEnum nodeTypeEnum, long timestamp) {
		super(daoInfo, componentId, nodeTypeEnum, timestamp);

	}

	@Override
	public Object doWork() {
		try {
			log.trace("starting work on job.");
			log.trace("override component in cache, componentId:{} of nodeTypeEnum:{} with timestamp:{}.", componentId,
					nodeTypeEnum, timestamp);
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
			log.debug("cache successfully overrided  componentId:{} nodeTypeEnum:{} timestemp:{}.", componentId,
					nodeTypeEnum, timestamp);
			return true;
		} catch (Exception e) {
			log.debug("an exception was encountered during OverrideJob", e);
		} finally {
			this.daoInfo.getToscaOperationFacade().commit();
		}
		return false;

	}
}
