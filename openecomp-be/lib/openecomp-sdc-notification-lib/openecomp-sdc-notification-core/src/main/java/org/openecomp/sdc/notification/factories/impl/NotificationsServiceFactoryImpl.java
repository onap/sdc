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
package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.factories.LastNotificationDaoFactory;
import org.openecomp.sdc.notification.factories.NotificationsDaoFactory;
import org.openecomp.sdc.notification.factories.NotificationsServiceFactory;
import org.openecomp.sdc.notification.services.NotificationsService;
import org.openecomp.sdc.notification.services.impl.NotificationsServiceImpl;

/**
 * @author Avrahamg
 * @since June 20, 2017
 */
public class NotificationsServiceFactoryImpl extends NotificationsServiceFactory {

    private static final NotificationsService INSTANCE = new NotificationsServiceImpl(LastNotificationDaoFactory.getInstance().createInterface(),
        NotificationsDaoFactory.getInstance().createInterface());

    @Override
    public NotificationsService createInterface() {
        return INSTANCE;
    }
}
