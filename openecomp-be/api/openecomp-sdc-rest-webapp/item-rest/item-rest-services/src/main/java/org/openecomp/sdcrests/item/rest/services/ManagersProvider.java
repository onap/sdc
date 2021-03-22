/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdcrests.item.rest.services;

import lombok.Getter;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.conflicts.ConflictsManager;
import org.openecomp.sdc.conflicts.ConflictsManagerFactory;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.AsdcItemManagerFactory;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;

@Getter
class ManagersProvider {

    private PermissionsManager permissionsManager = PermissionsManagerFactory.getInstance().createInterface();
    private AsdcItemManager asdcItemManager = AsdcItemManagerFactory.getInstance().createInterface();
    private VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
    private ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
    private ActivityLogManager activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
    private NotificationPropagationManager notificationPropagationManager = NotificationPropagationManagerFactory.getInstance().createInterface();
    private ItemManager itemManager = ItemManagerFactory.getInstance().createInterface();
}
