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

package org.onap.sdc.backend.ci.tests.execute.resource;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimultaneousApiTest extends ComponentBaseTest {

	protected static ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();

	@Rule
	public static TestName name = new TestName();

	static String httpCspUserId = "km2000";
	static String userFirstName = "Kot";
	static String userLastName = "Matroskin";
	static String email = "km2000@intl.sdc.com";
	static String role = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN).getRole();

	public static class WorkerThread implements Runnable {
		CountDownLatch countDownLatch;
		int threadIndex;

		public WorkerThread(int threadIndex, CountDownLatch countDownLatch) {
			this.threadIndex = threadIndex;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			System.out.println("**** Thread started " + threadIndex);
			try {
				RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
						ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
				String id = ResponseParser.getUniqueIdFromResponse(createResource);
				// System.out.println("**** Thread " + threadIndex + " create
				// resource status " + createResource.getErrorCode() + " id = "
				// + id + " error " + createResource.getResponse());
				// assertEquals("**** create resource: " +
				// createResource.getErrorCode() + " thread " + threadIndex,
				// 201, status);
			} catch (Exception e) {
				// System.out.println("**** Thread " + threadIndex + " exception
				// " + e);
			}
			countDownLatch.countDown();
			// System.out.println("**** Thread finished " + threadIndex);

		}

		// public void run_() {
		// System.out.println("**** Thread started " + threadIndex);
		// try {
		// UserUtils userUtils = new UserUtils();
		// User userDetails = new User(userFirstName, userLastName,
		// httpCspUserId, email, role , 0L);
		// RestResponse response =
		// userUtils.createUserTowardsCatalogBe(userDetails,
		// userUtils.getUserDetailesAdmin());
		// System.out.println("**** Thread " + threadIndex + " create resource
		// status " + response.getErrorCode() + " response " +
		// response.getResponse());
		//// assertEquals("**** create resource: " +
		// createResource.getErrorCode() + " thread " + threadIndex, 201,
		// status);
		// } catch (Exception e) {
		// System.out.println("**** Thread " + threadIndex + " exception " + e);
		// }
		// countDownLatch.countDown();
		// System.out.println("**** Thread finished " + threadIndex);
		//
		// }
	}

	@Test
	public void create2Resources() throws InterruptedException {
		int threadCount = 5;
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Runnable worker = new WorkerThread(i + 1, countDownLatch);
			executor.execute(worker);
		}
		countDownLatch.await();
		// System.out.println(" finished ");

	}
}
