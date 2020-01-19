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

package org.openecomp.sdc.be.ecomp.converters;

import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;

public final class EcompRoleConverter {
    private static final String FAILED_TO_CONVERT_USER = "Failed to convert user";
    private static final String EDIT_USER = "EditUser";
    private static final Logger log = Logger.getLogger(EcompRoleConverter.class);

    private EcompRoleConverter() {
    }

    public static String convertEcompRoleToRole(EcompRole ecompRole) throws PortalAPIException {

        log.debug("converting role{}", ecompRole);
        if (ecompRole == null) {
            log.debug("recieved null for roles");
            return null;
        }

        for (Role role : Role.values()) {
            if (role.name().toLowerCase().equals(ecompRole.getName().toLowerCase())){
                return  role.name();
            }
        }
        BeEcompErrorManager.getInstance().logInvalidInputError(EDIT_USER, FAILED_TO_CONVERT_USER, BeEcompErrorManager.ErrorSeverity.INFO);
        log.debug("Unsupported role for SDC user - role: {}", ecompRole);
        throw new PortalAPIException("Unsupported role for SDC user");
    }
}
