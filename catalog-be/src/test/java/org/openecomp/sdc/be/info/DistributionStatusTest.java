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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DistributionStatusTest {

	@Test
	public void deployedStatusShouldProvideCorrectStrings() {
		assertThat(DistributionStatus.DEPLOYED.getName(), is("Deployed"));
		assertThat(DistributionStatus.DEPLOYED.getAuditingStatus(), is("DEPLOYED"));
	}

	@Test
	public void shouldGetStatusByAuditingStatusName() {
		DistributionStatus deployed = DistributionStatus.getStatusByAuditingStatusName("DEPLOYED");
		assertThat(deployed, is(DistributionStatus.DEPLOYED));
	}

	@Test
	public void shouldNotGetStatusByAuditingStatusName() {
		DistributionStatus deployed = DistributionStatus.getStatusByAuditingStatusName("DIFFERENT_THAN_DEPLOYED");
		assertThat(deployed, is(nullValue()));
	}
}