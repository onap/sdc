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

package org.openecomp.sdc.be.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openecomp.sdc.be.components.distribution.engine.CambriaHandler;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TestQueue {

	public static void main(String[] args) {
		ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
		threadFactoryBuilder.setNameFormat("distribution-notification-thread");
		ThreadFactory threadFactory = threadFactoryBuilder.build();
		// TODO: add the package of google to the pom

		ExecutorService executorService = new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
		// ExecutorService executorService = new ThreadPoolExecutor(0, 2, 60L,
		// TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

		// 2 threads are always up and they handle the tasks. in case core size
		// is 0, only one is handles the tasks.
		// ExecutorService executorService = new ThreadPoolExecutor(0, 2, 60L,
		// TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

		// TODO : check what happen when the number of threads are full. Throw
		// RejectedExecutionException
		// TODO : check what happen whether the pool is full and the size of
		// pool

		ExecutorService newCachedThreadPool = Executors.newCachedThreadPool(threadFactory);
		Runnable task = new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("iN SLEEP" + Thread.currentThread());
					Thread.sleep(10 * 1000);
					System.out.println("OUT SLEEP");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		for (int i = 0; i < 4; i++) {
			try {
				executorService.submit(task);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}

		newCachedThreadPool.submit(task);
		System.out.println("After submitting the task");

		MyWorker[] watchThreads = new MyWorker[1];
		BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
		for (int i = 0; i < watchThreads.length; i++) {
			MyWorker myWorker = new MyWorker(queue);
			myWorker.start();
		}

		for (int i = 0; i < 1; i++) {
			try {
				queue.put("message " + i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static class MyTimerTask extends TimerTask {

		AtomicBoolean state;
		Thread thread;

		public MyTimerTask(AtomicBoolean state, Thread thread) {
			super();
			this.state = state;
			this.thread = thread;

			System.out.println("After create timer");
		}

		@Override
		public void run() {
			System.out.println("In running of Timer task");
			if (state.get() == false) {
				System.out.println("In running of Timer task. Going to interrupt thread");
				// thread.interrupt();
			} else {
				System.out.println("In running of Timer task. Finished.");
			}
		}

	}

	public static class MyWorker extends Thread {

		boolean active = true;
		private final BlockingQueue<String> queue;

		public MyWorker(BlockingQueue<String> queue) {
			this.queue = queue;
		}

		Timer timer = new Timer();

		public void run() {
			try {
				while (active) {
					String s = queue.take();
					System.out.println("Thread " + Thread.currentThread() + " fecthed a message " + s);

					AtomicBoolean atomicBoolean = new AtomicBoolean(false);
					MyTimerTask myTimerTask = new MyTimerTask(atomicBoolean, this);
					timer.schedule(myTimerTask, 10 * 1000);
					doWork(s);
					atomicBoolean.set(true);

				}
			} catch (InterruptedException ie) {

				System.out.println("Interrupted our thread");
				ie.printStackTrace();
			}
		}

		private void doWork(String s) {
			// TODO Auto-generated method stub

			CambriaHandler cambriaHandler = new CambriaHandler();
			INotificationData data = new NotificationDataImpl();
			List<String> servers = new ArrayList<>();
			servers.add("aaaaaaa");
			cambriaHandler.sendNotification("topicName", "uebPublicKey", "uebSecretKey", servers, data);

			System.out.println("IN WORK " + s);
			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {

				for (int i = 0; i < 10; i++) {
					System.out.println("*************************************************");
				}
				e.printStackTrace();
			}
		}
	}

}
