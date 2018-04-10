/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.factories.SubscriptionServiceFactory;
import org.openecomp.sdc.versioning.AsdcItemManagerFactory;

/**
 * Created by ayalaben on 6/18/2017
 */
public class PermissionsManagerFactoryImpl extends PermissionsManagerFactory {

    private static final PermissionsManager INSTANCE =
        new PermissionsManagerImpl(PermissionsServicesFactory.getInstance().createInterface(),
            AsdcItemManagerFactory.getInstance().createInterface(),
            NotificationPropagationManagerFactory.getInstance().createInterface(),
            SubscriptionServiceFactory.getInstance().createInterface());

    @Override
    public PermissionsManager createInterface() {
        return INSTANCE;
    }
}
