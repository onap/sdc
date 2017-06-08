/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.activityLog.impl;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public class ActivityLogManagerImpl implements ActivityLogManager {
    private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
    private ActivityLogDao activityLogDao;

    public ActivityLogManagerImpl(ActivityLogDao activityLogDao) {
        this.activityLogDao = activityLogDao;
    }

    @Override
    public void addActionLog(ActivityLogEntity activityLogEntity, String user) {
        mdcDataDebugMessage.debugEntryMessage("ITEM id", activityLogEntity.getItemId());
        activityLogEntity.setId(CommonMethods.nextUuId());
        activityLogDao.create(activityLogEntity);
    }

    @Override
    public Collection<ActivityLogEntity> listActivityLogs(String itemId, Version version, String user) {
        mdcDataDebugMessage.debugEntryMessage("ITEM id", itemId);
        String versionId = version.getMinor() == 0 ? String.valueOf(version.getMajor()) : String.valueOf(version.getMajor()+1);
        return activityLogDao.getActivityLogListForItem(itemId, versionId);
    }
}
