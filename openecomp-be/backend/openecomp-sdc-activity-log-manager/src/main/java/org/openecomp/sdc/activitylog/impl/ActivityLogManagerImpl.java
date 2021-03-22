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

import java.util.Collection;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ActivityLogManagerImpl implements ActivityLogManager {

    private ActivityLogDao activityLogDao;

    public ActivityLogManagerImpl(ActivityLogDao activityLogDao) {
        this.activityLogDao = activityLogDao;
    }

    @Override
    public void logActivity(ActivityLogEntity activityLogEntity) {
        activityLogEntity.setId(CommonMethods.nextUuId());
        activityLogDao.create(activityLogEntity);
    }

    @Override
    public Collection<ActivityLogEntity> listLoggedActivities(String itemId, Version version) {
        return activityLogDao.list(new ActivityLogEntity(itemId, version));
    }
}
