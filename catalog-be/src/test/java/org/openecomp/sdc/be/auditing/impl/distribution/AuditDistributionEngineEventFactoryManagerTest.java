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
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.GET_UEB_CLUSTER;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL;

@RunWith(MockitoJUnitRunner.class)
public class AuditDistributionEngineEventFactoryManagerTest {

    @Mock
    private DistributionTopicData distributionTopicData;
    private static final String STATUS = "status";
    private static final String APIKEY = "apikey";
    private static final String ROLE = "role";
    private static final String ENVNAME = "envname";

    @Test
    public void shouldCreateDistributionEngineEventFactoryForCreateDistributionTopic() {
        AuditEventFactory distributionEngineEventFactory = AuditDistributionEngineEventFactoryManager
            .createDistributionEngineEventFactory(CREATE_DISTRIBUTION_TOPIC,
                ENVNAME, distributionTopicData, ROLE, APIKEY, STATUS);

        assertThat(distributionEngineEventFactory, instanceOf(AuditCreateTopicDistributionEngineEventFactory.class));
    }

    @Test
    public void shouldCreateDistributionEngineEventFactoryForAddKeyToTopicAcl() {
        AuditEventFactory distributionEngineEventFactory = AuditDistributionEngineEventFactoryManager
            .createDistributionEngineEventFactory(ADD_KEY_TO_TOPIC_ACL,
                ENVNAME, distributionTopicData, ROLE, APIKEY, STATUS);

        assertThat(distributionEngineEventFactory, instanceOf(AuditAddRemoveKeyDistributionEngineEventFactory.class));
    }


    @Test
    public void shouldCreateDistributionEngineEventFactoryForRemovekeyFromTopicAcl() {
        AuditEventFactory distributionEngineEventFactory = AuditDistributionEngineEventFactoryManager
            .createDistributionEngineEventFactory(REMOVE_KEY_FROM_TOPIC_ACL,
                ENVNAME, distributionTopicData, ROLE, APIKEY, STATUS);

        assertThat(distributionEngineEventFactory, instanceOf(AuditAddRemoveKeyDistributionEngineEventFactory.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionOnDifferentAuditingActionEnum() {
        AuditDistributionEngineEventFactoryManager
            .createDistributionEngineEventFactory(GET_UEB_CLUSTER,
                ENVNAME, distributionTopicData, ROLE, APIKEY, STATUS);
    }
}