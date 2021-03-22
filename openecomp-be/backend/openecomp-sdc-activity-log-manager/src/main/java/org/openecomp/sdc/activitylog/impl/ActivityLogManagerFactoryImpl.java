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
package org.openecomp.sdc.activitylog.impl;

import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.ActivityLogDaoFactory;

public class ActivityLogManagerFactoryImpl extends ActivityLogManagerFactory {

    private final ActivityLogManager INSTANCE;

    public ActivityLogManagerFactoryImpl() {
        this.INSTANCE = new ActivityLogManagerImpl(ActivityLogDaoFactory.getInstance().createInterface());
    }

    ActivityLogManagerFactoryImpl(ActivityLogManager activityLogManager) {
        this.INSTANCE = activityLogManager;
    }

    @Override
    public ActivityLogManager createInterface() {
        return INSTANCE;
    }
}
