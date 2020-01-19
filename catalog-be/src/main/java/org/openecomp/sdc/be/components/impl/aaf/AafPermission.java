/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl.aaf;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;

public enum AafPermission {

    READ(PermNames.READ_VALUE),
    WRITE(PermNames.WRITE_VALUE),
    DELETE(PermNames.DELETE_VALUE),
    INTERNAL_ALL(PermNames.INTERNAL_ALL_VALUE);

    private String permission;
    private String permissionSuffix;

    AafPermission(String permissionSuffix) {
        this.permissionSuffix = permissionSuffix;
        this.permission = String.format("%s.%s",
                ConfigurationManager.getConfigurationManager().getConfiguration().getAafNamespace(),
                permissionSuffix);
    }

    public String getFullPermission() {
        return permission;
    }

    public static AafPermission getEnumByString(String perm) {
        for (AafPermission e : AafPermission.values()) {
            if (perm.equals(e.getPermissionSuffix()))
                return e;
        }
        throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY, perm);
    }

    public String getPermissionSuffix() {
        return this.permissionSuffix;
    }

    public static class PermNames {
        public static final String READ_VALUE = "endpoint.api.access|*|read";
        public static final String WRITE_VALUE = "endpoint.api.access|*|write";
        public static final String DELETE_VALUE = "endpoint.api.access|*|delete";
        public static final String INTERNAL_ALL_VALUE = "endpoint.api.internal.access|*|all";
    }
}
