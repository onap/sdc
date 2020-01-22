/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DistributionStatusOfServiceInfoTest {

	private static final String STATUS = "status";
	private static final String USER_ID = "userID";
	private static final String TIMESTAMP = "timestamp";
	private static final String DISTRIBUTION_ID = "distributionID";

	private DistributionStatusOfServiceInfo createTestSubject() {
		return new DistributionStatusOfServiceInfo();
	}

	@Test
	public void testCtor() throws Exception {
		new DistributionStatusOfServiceInfo("","","","");
	}


	@Test
	public void testGetDistributionID() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionID();
	}


	@Test
	public void testSetDistributionID() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String distributionID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionID(distributionID);
	}


	@Test
	public void testGetTimestamp() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}


	@Test
	public void testSetTimestamp() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String timestamp = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}


	@Test
	public void testGetUserId() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId();
	}


	@Test
	public void testSetUserId() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String userId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserId(userId);
	}


	@Test
	public void testGetDeployementStatus() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeployementStatus();
	}


	@Test
	public void testSetDeployementStatus() throws Exception {
		DistributionStatusOfServiceInfo testSubject;
		String deployementStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDeployementStatus(deployementStatus);
	}


	@Test
	public void shouldHaveValidDefaultConstructor() {
		assertThat(DistributionStatusOfServiceInfo.class, hasValidBeanConstructor());
	}

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(DistributionStatusOfServiceInfo.class, hasValidGettersAndSetters());
	}

	@Test
	public void shouldTestWhetherTheDefaultConstructorCorrectlySetAllFields() {
		DistributionStatusOfServiceInfo distributionStatusOfServiceInfo = new DistributionStatusOfServiceInfo(
				DISTRIBUTION_ID, TIMESTAMP, USER_ID, STATUS);
		assertThat(distributionStatusOfServiceInfo.getDistributionID(), is(DISTRIBUTION_ID));
		assertThat(distributionStatusOfServiceInfo.getTimestamp(), is(TIMESTAMP));
		assertThat(distributionStatusOfServiceInfo.getUserId(), is(USER_ID));
		assertThat(distributionStatusOfServiceInfo.getDeployementStatus(), is(STATUS));
	}


}