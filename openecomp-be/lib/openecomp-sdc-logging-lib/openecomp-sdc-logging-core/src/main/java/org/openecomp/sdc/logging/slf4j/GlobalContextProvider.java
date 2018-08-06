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

import java.net.InetAddress;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.logging.context.HostAddressCache;
import org.openecomp.sdc.logging.context.InstanceId;

/**
 * Maps global logging context to corresponding MDC fields.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
class GlobalContextProvider implements ContextProvider {

    private static final HostAddressCache HOST_ADDRESS_CACHE = new HostAddressCache();

    @Override
    public Map<ContextField, String> values() {

        Map<ContextField, String> values = new EnumMap<>(ContextField.class);
        values.put(ContextField.INSTANCE_ID, InstanceId.get());

        Optional<InetAddress> hostAddress = HOST_ADDRESS_CACHE.get();
        hostAddress.ifPresent(address -> {
            values.put(ContextField.SERVER, address.getHostName());
            values.put(ContextField.SERVER_IP_ADDRESS, address.getHostAddress());
        });

        return values;
    }
}
