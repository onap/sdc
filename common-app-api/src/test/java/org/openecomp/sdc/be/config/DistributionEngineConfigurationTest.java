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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistributionEngineConfigurationTest {

    @Test
    void validateSetGetEnvironments() {
        final String testEnvironment = "testEnvironment";
        DistributionEngineConfiguration distributionEngineConfiguration = new DistributionEngineConfiguration();
        distributionEngineConfiguration.setEnvironments(Collections.singletonList(testEnvironment));

        List<String> response = distributionEngineConfiguration.getEnvironments();

        assertEquals(1, response.size());
        assertEquals(testEnvironment, response.get(0));
    }
}
