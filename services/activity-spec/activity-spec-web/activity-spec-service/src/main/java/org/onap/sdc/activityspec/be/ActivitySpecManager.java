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

package org.onap.sdc.activityspec.be;

import org.onap.sdc.activityspec.api.rest.types.ActivitySpecAction;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.sdc.versioning.types.Item;

import java.util.Collection;

public interface ActivitySpecManager {

    ActivitySpecEntity createActivitySpec(ActivitySpecEntity activitySpecEntity);

    ActivitySpecEntity get(ActivitySpecEntity activitySpec);

    void update(ActivitySpecEntity activitySpec);

    void actOnAction(String activitySpecId, String versionId, ActivitySpecAction action);

    Collection<Item> list(String versionStatus);
}
