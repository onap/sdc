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

package org.openecomp.sdc.common.data_structure;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.openecomp.sdc.common.datastructure.CapList;

public class CapListTest {
	public enum LIST_ACTION {
		Add, Remove, Size, Get
	}

	@Test
	public void testCap() {
		List<Integer> testList = new CapList<>(10);
		for (int i = 0; i < 100; i++) {
			testList.add(i);
		}
		assertTrue(testList.size() == 10);
		for (int i = 0; i < testList.size(); i++) {
			assertTrue(testList.get(i) == (i + 90));
		}
	}

	@Test
	public void testThreadSafe() {
		List<Integer> testList = new CapList<>(1000);

		ExecutorService executor = Executors.newFixedThreadPool(4);
		for (int i = 0; i < 10; i++) {
			Runnable worker;
			// 0 - 4
			if (i < 5) {
				worker = new ThreadWorker(i, LIST_ACTION.Add, testList);
			}
			// 5, 8
			else if (i == 5 || i == 8) {
				worker = new ThreadWorker(i, LIST_ACTION.Remove, testList);
			}
			// 6
			else if (i == 6) {
				worker = new ThreadWorker(i, LIST_ACTION.Size, testList);
			}
			// 7, 9
			else {
				worker = new ThreadWorker(i, LIST_ACTION.Get, testList);
			}
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		assertTrue(testList.size() == 60);
	}

	public static class ThreadWorker implements Runnable {
		private LIST_ACTION action;
		private List<Integer> list;
		private Integer id;

		ThreadWorker(Integer id, LIST_ACTION action, List<Integer> list) {
			this.action = action;
			this.list = list;
			this.id = id;
		}

		@Override
		public void run() {
			for (int i = 0; i < 20; i++) {
				threadNap();
				switch (action) {
				case Add:
					list.add(id * 100 + i);
					break;
				case Remove: {
					int index = (int) (Math.random() * 10);
					list.remove(index);
					break;
				}
				case Get:
					int index = (int) (Math.random() * 10);
					Integer integer = list.get(index);

					break;
				case Size:
					int size = list.size();
					break;
				}
			}

		}

		private void threadNap() {
			long napTime = (long) (Math.random() * 100);
			try {
				Thread.sleep(napTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
