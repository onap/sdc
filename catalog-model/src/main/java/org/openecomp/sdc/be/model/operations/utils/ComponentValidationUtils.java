/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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
package org.openecomp.sdc.be.model.operations.utils;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ComponentValidationUtils {

    private static final Logger log = Logger.getLogger(ComponentValidationUtils.class.getName());

    private ComponentValidationUtils() {
    }

    public static boolean canWorkOnResource(Resource resource, String userId) {
        // verify resource is checked-out
        if (resource.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
            log.debug("resource is not checked-out");
            return false;
        }
        // verify resource is not deleted
        if ((resource.getIsDeleted() != null) && (resource.getIsDeleted())) {
            log.debug("resource is marked as delete");
            return false;
        }
        // verify resource last update user is the current user
        if (!userId.equals(resource.getLastUpdaterUserId())) {
            log.debug("resource last update is not {}", userId);
            return false;
        }
        return true;
    }

    public static boolean canWorkOnComponent(String componentId, ToscaOperationFacade toscaOperationFacade, String userId) {
        Either<Component, StorageOperationStatus> getResourceResult = toscaOperationFacade
            .getToscaElement(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getResourceResult.isRight()) {
            log.debug("Failed to retrieve component, component id {}", componentId);
            return false;
        }
        Component component = getResourceResult.left().value();
        return canWorkOnComponent(component, userId);
    }

    public static boolean canWorkOnComponent(Component component, String userId) {
        return canWorkOnComponent(component.getLifecycleState(), component.getLastUpdaterUserId(), userId);
    }

    private static boolean canWorkOnComponent(LifecycleStateEnum lifecycleState, String lastUpdaterUserId, String userId) {
        // verify resource is checked-out
        if (lifecycleState != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
            log.debug("resource is not checked-out");
            return false;
        }
        // verify userId is not null
        if (userId == null) {
            log.debug("current user userId is null");
            return false;
        }
        // verify resource last update user is the current user
        if (!userId.equals(lastUpdaterUserId)) {
            log.debug("resource last updater userId is not {}", userId);
            return false;
        }
        return true;
    }
}
