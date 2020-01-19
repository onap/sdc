/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia.
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

package org.openecomp.sdc.be.config;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigurationTest {
	public void validateBean() {
		assertThat(Configuration.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding(
						"excludedGroupTypesMapping",
						"excludedPolicyTypesMapping",
						"skipUpgradeVSPs",
						"skipUpgradeVSPsFlag",
						"supportAllottedResourcesAndProxy",
						"supportAllottedResourcesAndProxyFlag")
		));
	}
	@Test
	public void validateBeanForCassandrConfig() {
		assertThat(Configuration.CassandrConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForSwitchoverDetectorConfig() {
		assertThat(Configuration.SwitchoverDetectorConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForBeMonitoringConfig() {
		assertThat(Configuration.BeMonitoringConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForArtifactTypeConfig() {
		assertThat(Configuration.ArtifactTypeConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForOnboardingConfig() {
		assertThat(Configuration.OnboardingConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForDcaeConfig() {
		assertThat(Configuration.DcaeConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForEcompPortalConfig() {
		assertThat(Configuration.EcompPortalConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForApplicationL1CacheConfig() {
		assertThat(Configuration.ApplicationL1CacheConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForApplicationL2CacheConfig() {
		assertThat(Configuration.ApplicationL2CacheConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForToscaValidatorsConfig() {
		assertThat(Configuration.ToscaValidatorsConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForApplicationL1CacheInfo() {
		assertThat(Configuration.ApplicationL1CacheInfo.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForApplicationL1CacheCatalogInfo() {
		assertThat(Configuration.ApplicationL1CacheCatalogInfo.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForQueueInfo() {
		assertThat(Configuration.QueueInfo.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForEnvironmentContext() {
		assertThat(Configuration.EnvironmentContext.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForPathsAndNamesDefinition() {
		assertThat(Configuration.PathsAndNamesDefinition.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}
	@Test
	public void validateBeanForGabConfig() {
		assertThat(Configuration.GabConfig.class, allOf(
				hasValidBeanConstructor(),
				hasValidGettersAndSettersExcluding()
		));
	}

}
