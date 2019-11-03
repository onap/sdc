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

import java.util.EnumMap;
import java.util.Map;
import org.openecomp.sdc.logging.api.ContextData;

/**
 * Maps request data sent to the context service to corresponding MDC fields.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
class RequestContextProvider implements ContextProvider {

    private final ContextData data;

    private RequestContextProvider(ContextData contextData) {
        this.data = contextData;
    }

    static RequestContextProvider from(ContextData contextData) {
        return new RequestContextProvider(contextData);
    }

    static ContextData to(Map<ContextField, String> values) {
        return ContextData.builder()
                .requestId(values.get(ContextField.REQUEST_ID))
                .serviceName(values.get(ContextField.SERVICE_NAME))
                .partnerName(values.get(ContextField.PARTNER_NAME)).build();
    }

    @Override
    public Map<ContextField, String> values() {

        Map<ContextField, String> values = new EnumMap<>(ContextField.class);

        putIfNotNull(values, ContextField.REQUEST_ID, data.getRequestId());
        putIfNotNull(values, ContextField.SERVICE_NAME, data.getServiceName());
        putIfNotNull(values, ContextField.PARTNER_NAME, data.getPartnerName());

        return values;
    }

    private void putIfNotNull(Map<ContextField, String> values, ContextField field, String value) {
        if (value != null) {
            values.put(field, value);
        }
    }
}
