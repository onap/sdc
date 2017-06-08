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

package org.openecomp.sdcrests.activitylog.rest.services;

import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activityLog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.activitylog.rest.ActivityLog;
import org.openecomp.sdcrests.activitylog.rest.mapping.MapActivityLogEntityToActivityLogDto;
import org.openecomp.sdcrests.activitylog.types.ActivityLogDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("activityLog")
@Scope(value = "prototype")
public class ActivityLogImpl implements ActivityLog {

    private ActivityLogManager activityLogManager =
            ActivityLogManagerFactory.getInstance().createInterface();


    @Override
    public Response getActivityLog(String vspId, String versionId, String user) {
        MdcUtil.initMdc(LoggerServiceName.Get_List_Activity_Log.toString());

        Collection<ActivityLogEntity> activityLogs =
                activityLogManager.listActivityLogs(vspId, Version.valueOf(versionId), user);

        MapActivityLogEntityToActivityLogDto mapper = new MapActivityLogEntityToActivityLogDto();
        GenericCollectionWrapper<ActivityLogDto> results = new GenericCollectionWrapper<>();
        for (ActivityLogEntity activityLog : activityLogs) {
            results.add(mapper.applyMapping(activityLog, ActivityLogDto.class));
        }

        return Response.ok(results).build();
    }
}

