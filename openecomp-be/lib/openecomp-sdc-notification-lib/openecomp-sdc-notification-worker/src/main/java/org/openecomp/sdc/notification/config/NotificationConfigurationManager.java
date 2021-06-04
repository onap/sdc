/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017, 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.notification.config;

import org.openecomp.sdc.common.CommonConfigurationManager;

/**
 * This class return a config manager dedicated to notification Yaml section.
 * @see org.openecomp.sdc.common.CommonConfigurationManager
 */
public class NotificationConfigurationManager extends CommonConfigurationManager {

    private static NotificationConfigurationManager singletonInstance;
    private NotificationConfigurationManager() {
        super("notifications");
    }
    public static synchronized NotificationConfigurationManager getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new NotificationConfigurationManager();
        }
        return singletonInstance;
    }
}
