/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.tools.util;

import org.openecomp.sdc.logging.api.Logger;

/**
 * Copyright © 2016-2017 European Support Limited.
 *
 * @author Avrahamg
 * @since April 24, 2017
 * Since it is a command line tools writing to console will be helpful to users.
 */
public class Utils {

    public static void printMessage(Logger logger, String message) {
        /**
         * Since it is a command line tools writing to console will be helpful to users.
         */
        System.out.println(message);
        logger.debug(message);
    }

    public static void logError(Logger logger, Throwable ex) {
        /**
         * Since it is a command line tools writing to console will be helpful to users.
         */
        ex.printStackTrace();
        logger.error(ex.getMessage(), ex);
    }

    public static void logError(Logger logger, String message, Throwable ex) {
        /**
         * Since it is a command line tools writing to console will be helpful to users.
         */
        System.out.println(message);
        ex.printStackTrace();
        logger.error(message, ex);
    }
}
