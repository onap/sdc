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

package org.openecomp.sdc.activitylog.impl;

import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityLogDaoStub implements ActivityLogDao {
    @Override
    public Collection<ActivityLogEntity> list(ActivityLogEntity entity) {
        List<ActivityLogEntity> list = new ArrayList<>();
        list.add(entity);
        return list;
    }

    @Override
    public void create(ActivityLogEntity entity) {
        //stub method
    }

    @Override
    public void update(ActivityLogEntity entity) {
        //stub method
    }

    @Override
    public ActivityLogEntity get(ActivityLogEntity entity) {
        return null;
    }

    @Override
    public void delete(ActivityLogEntity entity) {
        //stub method
    }
}
