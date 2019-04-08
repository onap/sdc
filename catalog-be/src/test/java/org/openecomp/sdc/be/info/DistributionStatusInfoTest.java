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

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;

public class DistributionStatusInfoTest {

	private static final String AUDIT_DISTRIBUTION_STATUS_TIME = "AUDIT_DISTRIBUTION_STATUS_TIME";
	private static final String AUDIT_DISTRIBUTION_CONSUMER_ID = "AUDIT_DISTRIBUTION_CONSUMER_ID";
	private static final String AUDIT_DISTRIBUTION_RESOURCE_URL = "AUDIT_DISTRIBUTION_RESOURCE_URL";
	private static final String AUDIT_STATUS = "AUDIT_STATUS";
	private static final String AUDIT_DESC = "AUDIT_DESC";

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(DistributionStatusInfo.class, hasValidGettersAndSetters());
	}

	@Test
	public void testCtorWithESTimeBasedEvent() {
		ESTimeBasedEvent distributionStatusEvent = createESTimeBasedEvent();
		DistributionStatusInfo distributionStatusInfo = new DistributionStatusInfo(distributionStatusEvent);
		Assert.assertThat(distributionStatusInfo.getTimestamp(), is(AUDIT_DISTRIBUTION_STATUS_TIME));
		Assert.assertThat(distributionStatusInfo.getOmfComponentID(), is(AUDIT_DISTRIBUTION_CONSUMER_ID));
		Assert.assertThat(distributionStatusInfo.getErrorReason(), is(AUDIT_DESC));
		Assert.assertThat(distributionStatusInfo.getStatus(), is(AUDIT_STATUS));
		Assert.assertThat(distributionStatusInfo.getUrl(), is(AUDIT_DISTRIBUTION_RESOURCE_URL));
	}

	@Test
	public void shouldTestWhetherTheDefaultConstructorCorrectlySetAllFields() {
		DistributionStatusInfo distributionStatusInfo = new DistributionStatusInfo(AUDIT_DISTRIBUTION_CONSUMER_ID, AUDIT_DISTRIBUTION_STATUS_TIME, AUDIT_DISTRIBUTION_RESOURCE_URL, AUDIT_STATUS);
		Assert.assertThat(distributionStatusInfo.getUrl(), is(AUDIT_DISTRIBUTION_RESOURCE_URL));
		Assert.assertThat(distributionStatusInfo.getStatus(), is(AUDIT_STATUS));
		Assert.assertThat(distributionStatusInfo.getOmfComponentID(), is(AUDIT_DISTRIBUTION_CONSUMER_ID));
		Assert.assertThat(distributionStatusInfo.getTimestamp(), is(AUDIT_DISTRIBUTION_STATUS_TIME));
	}

	private ESTimeBasedEvent createESTimeBasedEvent() {
		ESTimeBasedEvent distributionStatusEvent = new ESTimeBasedEvent();
		Map<String, Object> fields = new HashMap<>();
		fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), AUDIT_DISTRIBUTION_CONSUMER_ID);
		fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_STATUS_TIME.getDisplayName(), AUDIT_DISTRIBUTION_STATUS_TIME);
		fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_RESOURCE_URL.getDisplayName(), AUDIT_DISTRIBUTION_RESOURCE_URL);
		fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), AUDIT_STATUS);
		fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), AUDIT_DESC);
		distributionStatusEvent.setFields(fields);
		return distributionStatusEvent;
	}

}