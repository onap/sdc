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
package org.openecomp.sdc.be.auditing.impl.distribution;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AuditDistributionEngineEventMigrationFactoryTest {

    @Mock
    private CommonAuditData commonFields;
    @Mock
    private DistributionTopicData distributionTopicData;

    @Test
    public void shouldBuildUserNameExtended() {
        User user = new User("firstName", "lastName", "userId", "emailAddress", "role",
            1L);
        String userName = AuditDistributionEngineEventMigrationFactory.buildUserNameExtended(user);
        assertEquals(userName, "userId, firstName lastName, emailAddress, role");
    }

    @Test
    public void shouldReturnDefaultValues() {
        AuditDistributionEngineEventMigrationFactory auditDistributionEngineEventMigrationFactory = new AuditDistributionEngineEventMigrationFactory(
            AuditingActionEnum.ACTIVATE_SERVICE_BY_API, commonFields, distributionTopicData,
            "consumerId", "apiKey", "envName", "role", "1");
        assertEquals(auditDistributionEngineEventMigrationFactory.getLogMessageParams().length, 0);
        assertEquals(auditDistributionEngineEventMigrationFactory.getLogPattern(), "");
    }
}