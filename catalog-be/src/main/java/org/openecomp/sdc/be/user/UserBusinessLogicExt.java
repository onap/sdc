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

package org.openecomp.sdc.be.user;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class UserBusinessLogicExt {

    private static final Logger log = Logger.getLogger(UserBusinessLogicExt.class);

    private final UserBusinessLogic userBusinessLogic;
    private final UserAdminOperation userAdminOperation;
    private final LifecycleBusinessLogic lifecycleBusinessLogic;
    private final ComponentsUtils componentsUtils;

    public UserBusinessLogicExt(UserBusinessLogic userBusinessLogic, UserAdminOperation userAdminOperation,
                                LifecycleBusinessLogic lifecycleBusinessLogic, ComponentsUtils componentsUtils) {
        this.userBusinessLogic = userBusinessLogic;
        this.userAdminOperation = userAdminOperation;
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.componentsUtils = componentsUtils;
    }


    public User deActivateUser(String modifierUserId, String userIdToDeactivate) {

        User modifier = userBusinessLogic.getValidModifier(modifierUserId, userIdToDeactivate, AuditingActionEnum.DELETE_USER);

        User userToDeactivate = userBusinessLogic.getUser(userIdToDeactivate, false);
        if (userToDeactivate.getStatus() == UserStatusEnum.INACTIVE) {
            log.debug("deActivateUser method - User already inactive", userIdToDeactivate);
            componentsUtils.auditAdminUserActionAndThrowException(AuditingActionEnum.DELETE_USER, modifier, userToDeactivate, null, ActionStatus.USER_NOT_FOUND, userIdToDeactivate);
        }

        handleTasksInProgress(userToDeactivate);

        userAdminOperation.deActivateUser(userToDeactivate);
        componentsUtils.auditUserAccess(userToDeactivate, ActionStatus.OK);
        handleAuditing(modifier, userToDeactivate, null, componentsUtils.getResponseFormat(ActionStatus.OK), AuditingActionEnum.DELETE_USER);
        userBusinessLogic.getFacadeUserOperation().updateUserCache(UserOperationEnum.DEACTIVATE, userToDeactivate.getUserId(), userToDeactivate.getRole());
        return userToDeactivate;
    }

    private void handleTasksInProgress(User userToDeactivate) {
        String userIdToDeactivate = userToDeactivate.getUserId();
        List<Component> userPendingTasks = userAdminOperation
                .getUserActiveComponents(userToDeactivate, getDeactivateUserStateLimitations());
        if (userPendingTasks.isEmpty()) {
            return;
        }
        LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("User became inactive");
        List<String> failedComponents = new ArrayList<>();
        for (Component component : userPendingTasks) {
            String componentId = component.getUniqueId();
            LifecycleStateEnum currentState = component.getLifecycleState();
            LifeCycleTransitionEnum transition = getLifeCycleTransition(currentState);
            if (transition == null) {
                log.debug("Erroneous component state when deactivating user for component {} state is {}", componentId, currentState);
                continue;
            }
            Either<? extends Component, ResponseFormat> result = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), componentId, userToDeactivate,
                    transition, changeInfo, false, true);
            if (result.isRight()) {
                failedComponents.add(component.getName());
            }
        }
        if (CollectionUtils.isNotEmpty(failedComponents)) {
            String componentList = failedComponents.toString();
            log.error(EcompLoggerErrorCode.DATA_ERROR, "", "", "User cannot be deleted, {} has the following pending projects that cannot be committed: {}", userIdToDeactivate, componentList);
            String userInfo = userToDeactivate.getFirstName() + " " + userToDeactivate.getLastName() + '(' + userToDeactivate.getUserId() + ')';
            componentsUtils.auditAdminUserActionAndThrowException(AuditingActionEnum.DELETE_USER, null, userToDeactivate, null, ActionStatus.CANNOT_DELETE_USER_WITH_ACTIVE_ELEMENTS, userInfo, componentList);
        }
    }

    private LifeCycleTransitionEnum getLifeCycleTransition(LifecycleStateEnum currentState) {
        LifeCycleTransitionEnum transition = null;
        if (LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT == currentState) {
            transition = LifeCycleTransitionEnum.CHECKIN;
        }
        return transition;
    }

    private List<Object> getDeactivateUserStateLimitations() {
        List<Object> properties = new ArrayList<>();
        properties.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
        return properties;
    }

    private void handleAuditing(User modifier, User userBefor, User userAfter, ResponseFormat responseFormat, AuditingActionEnum actionName) {
        componentsUtils.auditAdminUserAction(actionName, modifier, userBefor, userAfter, responseFormat);
    }

}
