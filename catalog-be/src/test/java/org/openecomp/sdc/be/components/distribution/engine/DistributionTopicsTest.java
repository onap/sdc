/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.components.distribution.engine;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;

class DistributionTopicsTest {

    @Test
    void oneStatusTopicPerEnvironmentUpperCased() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setEnvironments(List.of("auto", "PROD_TEST"));
        de.setDistributionStatusTopicName("SDC-DISTR-STATUS-TOPIC");
        assertArrayEquals(new String[]{"SDC-DISTR-STATUS-TOPIC-AUTO", "SDC-DISTR-STATUS-TOPIC-PROD_TEST"},
            new DistributionTopics(de).statusTopics());
    }

    @Test
    void notificationAndDeleteTopicsPerEnvironment() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setDistributionNotifTopicName("SDC-DISTR-NOTIF-TOPIC");
        de.setDistributionDeleteTopicName("SDC-DISTR-DELETE-TOPIC");
        DistributionTopics topics = new DistributionTopics(de);
        assertEquals("SDC-DISTR-NOTIF-TOPIC-AUTO", topics.notificationTopic("auto"));
        assertEquals("SDC-DISTR-DELETE-TOPIC-AUTO", topics.deleteTopic("auto"));
    }

    @Test
    void noEnvironmentsGiveNoStatusTopics() {
        DistributionEngineConfiguration de = new DistributionEngineConfiguration();
        de.setDistributionStatusTopicName("SDC-DISTR-STATUS-TOPIC");
        assertArrayEquals(new String[0], new DistributionTopics(de).statusTopics());
    }
}
