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

package org.openecomp.sdc.be.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import fj.data.Either;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ESGenericSearchDAO;
import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.exception.ResourceDAOException;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Component("resource-dao")
public class ESCatalogDAO extends ESGenericSearchDAO implements ICatalogDAO {

	private static Logger log = Logger.getLogger(ESCatalogDAO.class.getName());

	// Index Checking Variables
    private boolean initCompleted = false;
	
	//TODO use LoggerMetric instead
	private static Logger healthCheckLogger = Logger.getLogger("elasticsearch.healthcheck");

	///// HealthCheck/////////
	private static final String ES_HEALTH_CHECK_STR = "elasticsearchHealthCheck";

	private ScheduledExecutorService healthCheckScheduler = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "ES-Health-Check-Thread");
				}
			});

	private class HealthCheckScheduledTask implements Runnable {
		@Override
		public void run() {
			log.trace("Executing ELASTICSEARCH Health Check Task - Start");

			HealthCheckStatus healthStatus = null;
			try {
				healthStatus = isInitCompleted() ? checkHealth() : HealthCheckStatus.DOWN;
			} catch (Exception e) {
				log.error("Error while trying to connect to elasticsearch. host: {} | port: {} | error: {}", 
						getEsClient().getServerHost(), getEsClient().getServerPort(), e.getMessage(), e);
				healthStatus = HealthCheckStatus.DOWN;
			}
			log.trace("Executed ELASTICSEARCH Health Check Task - Status = {}", healthStatus);
			if (healthStatus != lastHealthState) {
				log.trace("ELASTICSEARCH Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
				lastHealthState = healthStatus;
				logAlarm();
			}
		}
	}

	private HealthCheckScheduledTask healthCheckScheduledTask = new HealthCheckScheduledTask();
	private volatile HealthCheckStatus lastHealthState = HealthCheckStatus.DOWN;

	/**
	 * Get ES cluster status string rep
	 * 
	 * @return "GREEN", "YELLOW" or "RED"
	 */
	private HealthCheckStatus checkHealth() {
		if (!isInitCompleted()) {
			return HealthCheckStatus.DOWN;
		}
		ClusterHealthRequest healthRequest = new ClusterHealthRequest("_all");
		healthRequest.masterNodeTimeout(TimeValue.timeValueSeconds(2));
		ClusterHealthStatus status = getClient().admin().cluster().health(healthRequest).actionGet().getStatus();
		healthCheckLogger.debug("ES cluster health status is {}", status);
		if (status == null || status.equals(ClusterHealthStatus.RED)) {
			return HealthCheckStatus.DOWN;
		}
		return HealthCheckStatus.UP;
	}

	private void logAlarm() {
		if (lastHealthState == HealthCheckStatus.UP) {
			BeEcompErrorManager.getInstance().logBeHealthCheckElasticSearchRecovery(ES_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().logBeHealthCheckElasticSearchError(ES_HEALTH_CHECK_STR);
		}
	}

	@PostConstruct
	public void initCompleted() {
		long interval = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getEsReconnectIntervalInSeconds(5);
		this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, interval, TimeUnit.SECONDS);
		initCompleted = true;
	}

	@Override
	public void writeArtifact(ESArtifactData artifactData) {
		try {
			saveResourceData(artifactData);
		} catch (Exception e) {
			throw new ResourceDAOException("Error to save ArtifactData with " + artifactData.getId());
		}
	}

	@Override
	public Either<ESArtifactData, ResourceUploadStatus> getArtifact(String id) {
		ESArtifactData resData = null;

		try {
			resData = findById(getTypeFromClass(ESArtifactData.class), id, ESArtifactData.class);
		} catch (Exception e) {
			resData = null;
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Get Artifact from database");
			log.debug("ESCatalogDAO:getArtifact failed with exception ", e);
			return Either.right(ResourceUploadStatus.ERROR);
		}

		if (resData != null) {
			return Either.left(resData);
		} else {
			return Either.right(ResourceUploadStatus.NOT_EXIST);
		}
	}

	private <T> String getTypeFromClass(Class<T> clazz) {

		return clazz.getSimpleName().toLowerCase();
	}

	@Override
	public void deleteArtifact(String id) {
		delete(getTypeFromClass(ESArtifactData.class), id);
	}

	@Override
	public Either<List<ESArtifactData>, ResourceUploadStatus> getArtifacts(String[] ids) {
		List<ESArtifactData> resData = null;
		try {
			resData = findByIds(getTypeFromClass(ESArtifactData.class), ESArtifactData.class, ids);
		} catch (Exception e) {
			resData = null;
			return Either.right(ResourceUploadStatus.ERROR);
		}

		if (resData != null && !resData.isEmpty()) {
			return Either.left(resData);
		} else {
			return Either.right(ResourceUploadStatus.NOT_EXIST);
		}
	}

	private void saveResourceData(ESArtifactData data) throws JsonProcessingException {
		String typeName = getTypeFromClass(data.getClass());
		saveResourceData(typeName, data, data.getId());
	}

	@Override
	public void deleteAllArtifacts() {
		String typeName = getTypeFromClass(ESArtifactData.class);
		String indexName = getIndexForType(typeName);
		deleteIndex(indexName);

	}

	public boolean isInitCompleted() {
		return initCompleted;
	}

	public HealthCheckStatus getHealth() {
		return lastHealthState;
	}

}
