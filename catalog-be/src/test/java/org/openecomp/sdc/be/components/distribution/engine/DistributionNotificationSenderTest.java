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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher;
import org.openecomp.sdc.be.components.kafka.DistributionNotificationPublisher.PublishResult;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class DistributionNotificationSenderTest {

    private final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private final DistributionNotificationPublisher publisher = mock(DistributionNotificationPublisher.class);

    @BeforeAll
    static void loadConfiguration() {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @ParameterizedTest
    @CsvSource({"OK,OK,200,OK", "FAILED,GENERAL_ERROR,500,Error: failed to publish to Kafka"})
    void publishResultDrivesStatusAndAudit(PublishResult result, ActionStatus expected, String auditStatus, String auditDesc) {
        DistributionNotificationSender sender = new DistributionNotificationSender(componentsUtils, publisher);
        when(publisher.publish(eq("topic-AUTO"), any(), eq(5L))).thenReturn(result);
        Service service = new Service();
        service.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        OperationalEnvironmentEntry environment = new OperationalEnvironmentEntry();
        environment.setEnvironmentId("AUTO");

        ActionStatus status = sender.sendNotification("topic-AUTO", "did", new EnvironmentMessageBusData(environment),
            new NotificationDataImpl(), service, new User());

        assertEquals(expected, status);
        verify(componentsUtils).auditDistributionNotification(any(), any(), eq("Service"), any(), any(), eq("AUTO"), eq("CERTIFIED"),
            eq("topic-AUTO"), eq("did"), eq(auditDesc), eq(auditStatus), any(), any());
    }
}
