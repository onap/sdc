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

package org.openecomp.sdc.logging;

/**
 * Constants that must be visible across the logging API.
 *
 * @author evitaliy
 * @since 18 Mar 2018
 */
public class LoggingConstants {

    /**
     * Default HTTP header for propagation of a request ID for distributed tracing.
     */
    public static final String DEFAULT_REQUEST_ID_HEADER = "X-ECOMP-RequestID";

    /**
     * Default HTTP header for exchanging a partner name between components.
     */
    public static final String DEFAULT_PARTNER_NAME_HEADER = "USER_ID";

    private LoggingConstants() {
        // prevent instantiation of the constants class
    }
}
