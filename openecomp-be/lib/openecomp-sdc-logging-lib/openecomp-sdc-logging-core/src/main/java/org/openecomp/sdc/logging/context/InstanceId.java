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

package org.openecomp.sdc.logging.context;

import java.util.UUID;

/**
 * Holds a unique ID of the logging entity. Is useful to distinguish between different nodes of the same application. If
 * it can be assumed, that the node can be re-started, then the unique ID must be retained on the disk.
 *
 * @author evitaliy
 * @since 04 Mar 2018
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "squid:S106", "squid:S1148"})
public class InstanceId {

    private static final String INSTANCE_ID;

    static {
        // for some reason Java Preferences API
        // https://docs.oracle.com/javase/8/docs/technotes/guides/preferences/overview.html
        // didn't work in a Docker container, so for now just generate an ID every time
        INSTANCE_ID = UUID.randomUUID().toString();
    }

    private InstanceId() {
        // prevent instantiation
    }

    /**
     * A unique ID of the logging entity.
     *
     * @return unique logging entity ID
     */
    public static String get() {
        return INSTANCE_ID;
    }
}
