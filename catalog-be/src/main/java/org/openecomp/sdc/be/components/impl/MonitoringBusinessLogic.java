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

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.impl.MonitoringDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.monitoring.MonitoringEvent;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("monitoringBusinessLogic")
public class MonitoringBusinessLogic {

	private static Logger log = LoggerFactory.getLogger(MonitoringBusinessLogic.class.getName());

	@javax.annotation.Resource
	private MonitoringDao monitoringDao;

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

	public Either<Boolean, ResponseFormat> logMonitoringEvent(MonitoringEvent monitoringEvent) {
		if (monitoringDao == null) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		ActionStatus status = monitoringDao.addRecord(monitoringEvent);
		if (!status.equals(ActionStatus.OK)) {
			log.warn("Failed to persist monitoring event: {}", status.name());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		return Either.left(true);
	}

	public String getEsHost() {

		String res = monitoringDao.getEsHost();
		res = res.replaceAll("[\\[\\]]", "");
		res = res.split(",")[0];
		res = res.replaceAll("[']", "");
		res = res.split(":")[0];
		return res;
	}

	public String getEsPort() {
		return monitoringDao.getEsPort();
	}

}
