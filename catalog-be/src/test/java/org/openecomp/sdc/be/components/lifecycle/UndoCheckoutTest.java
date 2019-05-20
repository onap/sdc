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

package org.openecomp.sdc.be.components.lifecycle;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UndoCheckoutTest extends LifecycleTestBase {

    private UndoCheckoutTransition undoCheckoutObj = null;

    @Before
    public void setup() {

        super.setup();
        // checkout transition object
        undoCheckoutObj = new UndoCheckoutTransition(componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,
            janusGraphDao);
        undoCheckoutObj.setLifeCycleOperation(toscaElementLifecycleOperation);
        undoCheckoutObj.setConfigurationManager(configurationManager);

    }

    @Test
    public void testResourceNotCheckedOutValidation() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = undoCheckoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        Either<Boolean, ResponseFormat> validateBeforeTransition = undoCheckoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());

        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

        resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        validateBeforeTransition = undoCheckoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        validateBeforeTransition = undoCheckoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFIED);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

        resource.setLifecycleState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        validateBeforeTransition = undoCheckoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

    }

    @Test
    public void testDifferentResourceOwnerValidation() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        User modifier = new User();
        modifier.setUserId("modifier");
        modifier.setFirstName("Albert");
        modifier.setLastName("Einstein");
        modifier.setRole(Role.DESIGNER.name());

        Either<User, ResponseFormat> ownerResponse = undoCheckoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        Either<Boolean, ResponseFormat> validateBeforeTransition = undoCheckoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
    }

}
