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

package org.openecomp.sdc.be.workers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Created by michael on 6/24/2016.
 */
public class Manager<T extends Job, E> {
	private static Logger log = LoggerFactory.getLogger(Manager.class.getName());
	private ExecutorService executor;
	private LinkedBlockingQueue<T> inputQueue;
	private LinkedBlockingQueue<E> outputQueue;
	private int numberOfWorkers;

	public void init(int numberOfWorkers) {
		log.debug("initializing workers, creating {} workers", numberOfWorkers);
		this.numberOfWorkers = numberOfWorkers;
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Worker-%d").build();
		this.executor = Executors.newFixedThreadPool(numberOfWorkers, threadFactory);
		this.inputQueue = new LinkedBlockingQueue<T>();
		this.outputQueue = new LinkedBlockingQueue<E>();
	}

	public void addJob(T job) {
		log.trace("job add to input queue");
		this.inputQueue.add(job);
	}

	public LinkedBlockingQueue<E> start() {
		for (int i = 0; i < numberOfWorkers; i++) {
			String workerName = "worker-" + i;
			log.debug("starting worker:{}", workerName);
			this.executor.submit(new Worker(workerName, this.inputQueue, this.outputQueue));
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
				log.error("timer elapsed while waiting for the worker's to finish. ");
			}
			log.debug("all workers finished");
		} catch (InterruptedException e) {
			log.error("failed while waiting for", e);
		}
		return outputQueue;
	}

	//
	// public static void main(String[] args) {
	// ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
	// for (int i = 0; i < 500; i++) {
	// Runnable worker = new MyRunnable(10000000L + i);
	// executor.execute(worker);
	// }
	// // This will make the executor accept no new threads
	// // and finish all existing threads in the queue
	// executor.shutdown();
	// // Wait until all threads are finish
	// executor.awaitTermination();
	// System.out.println("Finished all threads");
	// }
	// }
}
