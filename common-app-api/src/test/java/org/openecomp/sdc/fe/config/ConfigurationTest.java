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
package org.openecomp.sdc.fe.config;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void validateBean() {
        assertThat(Configuration.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateFeMonitoringConfigBean() {
        assertThat(Configuration.FeMonitoringConfig.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateOnboardingConfigBean() {
        assertThat(Configuration.OnboardingConfig.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateDcaeConfigBean() {
        assertThat(Configuration.DcaeConfig.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateGetHealthCheckSocketTimeoutInMsReturnsProperTime() {
        final int defaultTestTimeout = 100;
        final int setTestTimeout = 1000;
        Configuration configuration = new Configuration();
        assertEquals(configuration.getHealthCheckSocketTimeoutInMs(defaultTestTimeout).intValue(), defaultTestTimeout);
        configuration.setHealthCheckSocketTimeoutInMs(setTestTimeout);
        assertEquals(configuration.getHealthCheckSocketTimeoutInMs(defaultTestTimeout).intValue(), setTestTimeout);
    }

    @Test
    public void validateGetHealthCheckIntervalInSecondsReturnsProperTime() {
        final int defaultTestTimeout = 1;
        final int setTestTimeout = 2;
        Configuration configuration = new Configuration();
        assertEquals(configuration.getHealthCheckIntervalInSeconds(defaultTestTimeout).intValue(), defaultTestTimeout);
        configuration.setHealthCheckIntervalInSeconds(setTestTimeout);
        assertEquals(configuration.getHealthCheckIntervalInSeconds(defaultTestTimeout).intValue(), setTestTimeout);
    }
}
