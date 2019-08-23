/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.be.distribution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

@RunWith(MockitoJUnitRunner.class)
public class AuditHandlerTest {

    private static final String INSTANCEID = "INSTANCEID";

    private AuditHandler auditHandler;
    @Mock
    private RegistrationRequest registrationRequest;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private DistributionTopicData distributionTopicData;
    @Mock
    private CambriaErrorResponse registerResponse;

    @Before
    public void setUp() throws Exception {
        auditHandler = new AuditHandler(componentsUtils, INSTANCEID, registrationRequest);
    }

    @Test
    public void verifyAuditRegisterACLCalls() {
        auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.CONSUMER, distributionTopicData);
        Mockito.verify(componentsUtils).auditDistributionEngine(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL, null, distributionTopicData, SubscriberTypeEnum.CONSUMER.name(), null, "0");
    }

    @Test
    public void verifyAuditUnRegisterACLCalls() {
        auditHandler.auditUnRegisterACL(registerResponse, SubscriberTypeEnum.PRODUCER, distributionTopicData);
        Mockito.verify(componentsUtils).auditDistributionEngine(AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL, null, distributionTopicData, SubscriberTypeEnum.PRODUCER.name(), null, "0");
    }

}