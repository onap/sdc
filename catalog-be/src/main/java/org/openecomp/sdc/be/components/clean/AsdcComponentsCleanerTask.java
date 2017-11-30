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

package org.openecomp.sdc.be.components.clean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.CleanComponentsConfiguration;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("asdcComponentsCleaner")
public class AsdcComponentsCleanerTask implements Runnable {

	private static Logger log = LoggerFactory.getLogger(AsdcComponentsCleanerTask.class.getName());

	@javax.annotation.Resource
	private ComponentsCleanBusinessLogic componentsCleanBusinessLogic = null;

	private List<NodeTypeEnum> componentsToClean;
	private long cleaningIntervalInMinutes;

	private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("ComponentsCleanThread-%d").build());
	ScheduledFuture<?> scheduledFuture = null;

	@PostConstruct
	public void init() {
		log.trace("Enter init method of AsdcComponentsCleaner");
		Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
		CleanComponentsConfiguration cleanComponentsConfiguration = configuration.getCleanComponentsConfiguration();

		if (cleanComponentsConfiguration == null) {
			log.info("ERROR - configuration is not valid!!! missing cleanComponentsConfiguration");
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeComponentCleanerSystemError, "AsdcComponentsCleanerTask.init()", "AsdcComponentsCleanerTask.init()");
			BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-init", "fecth configuration");
			return;

		}
		componentsToClean = new ArrayList<NodeTypeEnum>();
		List<String> components = cleanComponentsConfiguration.getComponentsToClean();
		if (components == null) {
			log.info("no component were configured for cleaning");
		}
		for (String component : components) {
			NodeTypeEnum typeEnum = NodeTypeEnum.getByNameIgnoreCase(component);
			if (typeEnum != null)
				componentsToClean.add(typeEnum);
		}

		long intervalInMinutes = cleanComponentsConfiguration.getCleanIntervalInMinutes();

		if (intervalInMinutes < 1) {
			log.warn("cleaningIntervalInMinutes value should be greater than or equal to 1 minute. use default");
			intervalInMinutes = 60;
		}
		cleaningIntervalInMinutes = intervalInMinutes;

		startTask();

		log.trace("End init method of AsdcComponentsCleaner");
	}

	@PreDestroy
	public void destroy() {
		this.stopTask();
		shutdownExecutor();
	}

	public void startTask() {

		log.debug("start task for cleaning components");

		try {

			if (scheduledService != null) {
				log.debug("Start Cleaning components task. interval {} minutes", cleaningIntervalInMinutes);
				scheduledFuture = scheduledService.scheduleAtFixedRate(this, 5, cleaningIntervalInMinutes, TimeUnit.MINUTES);

			}
		} catch (Exception e) {
			log.debug("unexpected error occured", e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeComponentCleanerSystemError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-startTask", e.getMessage());

		}
	}

	public void stopTask() {
		if (scheduledFuture != null) {
			boolean result = scheduledFuture.cancel(true);
			log.debug("Stop cleaning task. result = {}", result);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			if (false == result) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeComponentCleanerSystemError, methodName, "try to stop the polling task");
				BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-stopTask", "try to stop the polling task");
			}
			scheduledFuture = null;
		}

	}

	private void shutdownExecutor() {
		if (scheduledService == null)
			return;

		scheduledService.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!scheduledService.awaitTermination(60, TimeUnit.SECONDS)) {
				scheduledService.shutdownNow(); // Cancel currently executing
												// tasks
				// Wait a while for tasks to respond to being cancelled
				if (!scheduledService.awaitTermination(60, TimeUnit.SECONDS))
					log.debug("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			scheduledService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		try {
			componentsCleanBusinessLogic.cleanComponents(componentsToClean);
		} catch (Exception e) {
			log.error("unexpected error occured", e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeComponentCleanerSystemError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-run", e.getMessage());
		}

	}
}
