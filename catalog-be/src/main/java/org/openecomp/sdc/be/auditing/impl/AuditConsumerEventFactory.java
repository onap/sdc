/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ConsumerEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditConsumerEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" ECOMP_USER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final ConsumerEvent event;

    public AuditConsumerEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, ConsumerDefinition ecompUser) {
        this(action, commonFields, buildConsumerName(ecompUser), buildUserName(modifier));
    }

    public AuditConsumerEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                     String ecompUser, String modifier, String timestamp) {
        this(action, commonFields, ecompUser, modifier);
        this.event.setTimestamp1(timestamp);
    }

    private AuditConsumerEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                      String ecompUser, String modifier) {
        super(action);
        event = new ConsumerEvent(getAction().getName(), commonFields, ecompUser, modifier);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getModifier(), event.getEcompUser(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    static String buildConsumerName(ConsumerDefinition consumer) {
        StringBuilder ecompUser = new StringBuilder();
        if (consumer != null) {
            appendIfNotEmpty(consumer.getConsumerName(), ecompUser, ",");
            appendIfNotEmpty(consumer.getConsumerSalt(), ecompUser, ",");
            appendIfNotEmpty(consumer.getConsumerPassword(), ecompUser, ",");
        }
        return ecompUser.toString();
    }
}
