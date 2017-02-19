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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class NotificationExecutorService {

	private static Logger logger = LoggerFactory.getLogger(NotificationExecutorService.class.getName());

	public ExecutorService createExcecutorService(DistributionNotificationTopicConfig distributionNotificationTopic) {

		Integer minThreadPoolSize = distributionNotificationTopic.getMinThreadPoolSize();
		if (minThreadPoolSize == null) {
			minThreadPoolSize = 0;
		}

		Integer maxThreadPoolSize = distributionNotificationTopic.getMaxThreadPoolSize();
		if (maxThreadPoolSize == null) {
			maxThreadPoolSize = 10;
		}

		ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
		threadFactoryBuilder.setNameFormat("distribution-notification-thread-%d");
		ThreadFactory threadFactory = threadFactoryBuilder.build();

		ExecutorService executorService = new ThreadPoolExecutor(minThreadPoolSize, maxThreadPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);

		return executorService;
	}

	public void shutdownAndAwaitTermination(ExecutorService pool, long maxTimeToWait) {

		logger.debug("shutdown NotificationExecutorService");
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(maxTimeToWait, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(maxTimeToWait, TimeUnit.SECONDS)) {
					logger.debug("Failed to close executor service");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
