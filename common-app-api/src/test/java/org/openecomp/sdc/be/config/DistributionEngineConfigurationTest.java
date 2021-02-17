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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class DistributionEngineConfigurationTest {

    @Test
    public void validateBean() {
        assertThat(DistributionEngineConfiguration.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSettersExcluding("environments")));
    }

    @Test
    public void validateDistribNotifServiceArtifactsBean() {
        assertThat(DistributionEngineConfiguration.DistribNotifServiceArtifacts.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateNotifArtifactTypesBean() {
        assertThat(DistributionEngineConfiguration.NotifArtifactTypes.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateNotifArtifactTypesResourceBean() {
        assertThat(DistributionEngineConfiguration.NotifArtifactTypesResource.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateCreateTopicConfigBean() {
        assertThat(DistributionEngineConfiguration.CreateTopicConfig.class,
            allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanToString()));
    }

    @Test
    public void validateEnvironmentConfigBean() {
        assertThat(DistributionEngineConfiguration.EnvironmentConfig.class,
            allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanToString()));
    }

    @Test
    public void validateDistributionStatusTopicConfigBean() {
        assertThat(DistributionEngineConfiguration.DistributionStatusTopicConfig.class,
            allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanToString()));
    }

    @Test
    public void validateDistributionNotificationTopicConfigBean() {
        assertThat(DistributionEngineConfiguration.DistributionNotificationTopicConfig.class,
            allOf(hasValidBeanConstructor(), hasValidGettersAndSetters(), hasValidBeanToString()));
    }

    @Test
    public void validateComponentArtifactTypesConfigBean() {
        assertThat(DistributionEngineConfiguration.ComponentArtifactTypesConfig.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSetters()));
    }

    @Test
    public void validateSetGetEnvironments() {
        final String testEnvironment = "testEnvironment";
        DistributionEngineConfiguration distributionEngineConfiguration = new DistributionEngineConfiguration();
        distributionEngineConfiguration.setEnvironments(Collections.singletonList(testEnvironment));
        List<String> response = distributionEngineConfiguration.getEnvironments();
        assertEquals(response.size(), 1);
        assertEquals(response.get(0), testEnvironment);
    }
}
