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
 * MDC fields that represent context data.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
enum ContextField implements MDCField {

    REQUEST_ID(ONAPLogConstants.MDCs.REQUEST_ID),
    SERVICE_NAME(ONAPLogConstants.MDCs.SERVICE_NAME),
    PARTNER_NAME(ONAPLogConstants.MDCs.PARTNER_NAME),
    INSTANCE_ID(ONAPLogConstants.MDCs.INSTANCE_UUID),
    SERVER(ONAPLogConstants.MDCs.SERVER_FQDN),
    SERVER_IP_ADDRESS("ServerIpAddress");

    private final String key;

    ContextField(String key) {
        this.key = key;
    }

    public String asKey() {
        return key;
    }
}
