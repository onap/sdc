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

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mlando on 9/20/2016.
 */
public class DeleteJob extends Job {
	private static Logger log = LoggerFactory.getLogger(DeleteJob.class.getName());

	public DeleteJob(DaoInfo daoInfo, String componentId, NodeTypeEnum nodeTypeEnum, long timestamp) {
		super(daoInfo, componentId, nodeTypeEnum, timestamp);

	}

	@Override
	public Object doWork() {
		try {
			log.trace("starting work on job.");
			log.trace("delete component in cache, componentId:{} of nodeTypeEnum:{} with timestamp:{}.", componentId,
					nodeTypeEnum, timestamp);
			ActionStatus status = this.daoInfo.getComponentCache().deleteComponentFromCache(componentId);
			if (!ActionStatus.OK.equals(status)) {
				log.debug("failed to delete componentId:{} nodeTypeEnum:", componentId, nodeTypeEnum);
				return false;
			}
			log.trace("cache successfully deleted  componentId:{} nodeTypeEnum:{} timestamp:{}.", componentId,
					nodeTypeEnum, timestamp);
			return true;
		} catch (Exception e) {
			log.debug("an exception was encountered durring deletejob", e);
		}
		return false;

	}
}
