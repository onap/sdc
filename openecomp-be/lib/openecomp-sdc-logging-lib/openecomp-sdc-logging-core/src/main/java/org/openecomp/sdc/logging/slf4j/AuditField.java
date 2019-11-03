/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.slf4j;

import org.onap.logging.ref.slf4j.ONAPLogConstants;

/**
 * MDC fields that represent audit data.
 *
 * @author evitaliy
 * @since 25 Mar 2018
 */
enum AuditField implements MDCField {

    BEGIN_TIMESTAMP(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP),
    END_TIMESTAMP("EndTimestamp"),
    ELAPSED_TIME("ElapsedTime"),
    STATUS_CODE(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE),
    RESPONSE_CODE(ONAPLogConstants.MDCs.RESPONSE_CODE),
    RESPONSE_DESCRIPTION(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION),
    CLIENT_IP_ADDRESS(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS);

    private final String key;

    AuditField(String key) {
        this.key = key;
    }

    public String asKey() {
        return key;
    }
}
