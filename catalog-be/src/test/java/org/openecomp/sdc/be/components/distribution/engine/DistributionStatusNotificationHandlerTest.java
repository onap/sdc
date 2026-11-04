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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class DistributionStatusNotificationHandlerTest {

    private static final String TOPIC = "SDC-DISTR-STATUS-TOPIC-AUTO";
    private final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private final DistributionCompleteReporter reporter = mock(DistributionCompleteReporter.class);
    private final DistributionStatusNotificationHandler handler = new DistributionStatusNotificationHandler(componentsUtils, reporter);

    @BeforeAll
    static void loadConfiguration() {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void validStatusIsAudited() {
        handler.handle(TOPIC, "{\"distributionID\":\"d1\",\"consumerID\":\"so\",\"timestamp\":1,"
            + "\"artifactURL\":\"/a\",\"status\":\"DOWNLOAD_OK\"}");
        verify(componentsUtils).auditDistributionStatusNotification(eq("d1"), eq("so"), eq(TOPIC), eq("/a"), eq("1"),
            eq("DOWNLOAD_OK"), isNull(), any());
        verifyNoInteractions(reporter);
    }

    @Test
    void completeNotificationIsReported() {
        handler.handle(TOPIC, "{\"distributionID\":\"d1\",\"consumerID\":\"so\",\"timestamp\":1,"
            + "\"status\":\"DISTRIBUTION_COMPLETE_OK\"}");
        verify(reporter).reportDistributionComplete(argThat(n -> "d1".equals(n.getDistributionID())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not json", "{}", "{\"distributionID\":\"\"}", "{\"distributionID\":\"d1\",\"status\":\"NO_SUCH\"}", "null"})
    void invalidMessagesAreSkippedWithoutThrowing(String message) {
        assertDoesNotThrow(() -> handler.handle(TOPIC, message));
        verifyNoInteractions(componentsUtils, reporter);
    }

    @Test
    void nullRecordValueIsSkipped() {
        assertDoesNotThrow(() -> handler.handle(TOPIC, null));
        verifyNoInteractions(componentsUtils, reporter);
    }

    @Test
    void auditFailureDoesNotEscape() {
        doThrow(new IllegalStateException("cassandra down")).when(componentsUtils)
            .auditDistributionStatusNotification(anyString(), any(), any(), any(), any(), any(), any(), any());
        assertDoesNotThrow(() -> handler.handle(TOPIC, "{\"distributionID\":\"d1\",\"status\":\"DOWNLOAD_OK\"}"));
    }
}
