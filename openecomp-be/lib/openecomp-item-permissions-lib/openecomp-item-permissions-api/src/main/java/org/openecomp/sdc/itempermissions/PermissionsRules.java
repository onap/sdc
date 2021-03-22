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
package org.openecomp.sdc.itempermissions;

import java.util.Set;

/**
 * Created by ayalaben on 6/22/2017.
 */
public interface PermissionsRules {

    boolean isAllowed(String userId, String action);

    void executeAction(String itemId, String userId, String action);

    void updatePermission(String itemId, String currentUserId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds);
}
