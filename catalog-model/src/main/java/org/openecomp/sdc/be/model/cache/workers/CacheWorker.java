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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openecomp.sdc.be.model.cache.jobs.Job;
import org.openecomp.sdc.be.workers.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mlando on 9/6/2016. the class represents a worker the pull job
 * from a queue and evacuates them.
 *
 */
public class CacheWorker implements Runnable, IWorker {
	private String workerName;
	private static Logger log = LoggerFactory.getLogger(Worker.class.getName());
	private LinkedBlockingQueue<Job> jobQueue;
	private volatile boolean shutdown = false;

	/**
	 * constructor
	 * 
	 * @param workerName
	 *            the name of the given worker
	 * @param jobQueue
	 *            the queue the worker will block on.
	 */
	public CacheWorker(String workerName, LinkedBlockingQueue<Job> jobQueue) {
		this.workerName = workerName;
		this.jobQueue = jobQueue;
	}

	/**
	 * the method will try to get a job if one is avilable it will be retrived
	 * and handled. if no jobs are available the worker will block for 500
	 * milliseconds and then it wil check if it needs to shutdown. if not it
	 * will block again and so on until sutdown or a new job is available
	 */
	@Override
	public void run() {
		while (true) {
			log.trace("CacheWorker:{} doing work", workerName);
			try {
				Job job = jobQueue.poll(500, TimeUnit.MILLISECONDS);
				if (job != null) {
					job.doWork();
					log.trace("worker:{} done with work", workerName);
				}
			} catch (Throwable e) {
				log.debug("worker {} failed during job execution.", workerName);
				log.debug("exception", e);
			}
			if (shutdown) {
				log.debug("worker:{} nothing to do stoping", workerName);
				break;
			}
		}

	}

	/**
	 * the method sets the shutdown flag, when set the worker will stop it's
	 * execution as soon as possible with out completing its work
	 */
	@Override
	public void shutDown() {
		this.shutdown = true;
	}

}
