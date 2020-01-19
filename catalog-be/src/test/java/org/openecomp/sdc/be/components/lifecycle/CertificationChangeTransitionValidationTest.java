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
import org.mockito.Mock;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertTrue;

public class CertificationChangeTransitionValidationTest extends LifecycleTestBase {

    private CertificationChangeTransition certifyTransitionObj = null;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    private User owner = null;

    Resource resource;
    Service service; 

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {

        super.setup();
        // checkout transition object
        certifyTransitionObj = new CertificationChangeTransition(serviceBusinessLogic, LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade, janusGraphDao);
        certifyTransitionObj.setConfigurationManager(configurationManager);
        certifyTransitionObj.setLifeCycleOperation(toscaElementLifecycleOperation);

        owner = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);
        user.setRole(UserRoleEnum.DESIGNER.getName());

        resource = createResourceObject();
        service = createServiceObject();
    }

    @Test
    public void testVFCMTStateValidation(){
        Resource resource = createResourceVFCMTObject();
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isLeft());
    }

    @Test
    public void testStateCheckInValidationSuccess() {
        Either<Boolean, ResponseFormat> changeStateResult = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(changeStateResult.isLeft());

        changeStateResult = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testStateCheckOutValidationSuccess() {
        Either<Boolean, ResponseFormat> changeStateResult = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        assertTrue(changeStateResult.isLeft());

        changeStateResult = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        assertTrue(changeStateResult.isLeft());
    }
    
    @Test
    public void testStateCertifyValidationFail() {
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.CERTIFIED);
        assertValidationStateErrorResponse(validateBeforeTransition);

        certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFIED);
        assertValidationStateErrorResponse(validateBeforeTransition);
    }

    @Test
    public void testRolesSuccess() {

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertSuccessWithResourceAndService();

        user.setRole(UserRoleEnum.ADMIN.getName());
        assertSuccessWithResourceAndService();

    }

    @Test
    public void testRolesFail() {
        user.setRole(UserRoleEnum.TESTER.getName());
        assertBeforeTransitionRoleFalis();
        assertBeforeTransitionRoleFalis();
        assertBeforeTransitionRoleFalis();
        user.setRole(UserRoleEnum.PRODUCT_MANAGER.getName());
        assertBeforeTransitionRoleFalis();
        user.setRole(UserRoleEnum.PRODUCT_STRATEGIST.getName());
        assertBeforeTransitionRoleFalis();
    }

    private void assertSuccessWithResourceAndService() {
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isLeft());
        certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isLeft());
    }

    private void assertBeforeTransitionRoleFalis() {
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertResponse(Either.right(validateBeforeTransition.right().value()), ActionStatus.RESTRICTED_OPERATION);
        certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertResponse(Either.right(validateBeforeTransition.right().value()), ActionStatus.RESTRICTED_OPERATION);
    }

    private void assertValidationStateErrorResponse(Either<Boolean, ResponseFormat> validateBeforeTransition) {
        assertTrue(validateBeforeTransition.isRight());
        ResponseFormat error = validateBeforeTransition.right().value();
        Either<Resource, ResponseFormat> changeStateResult = Either.right(error);
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.ILLEGAL_COMPONENT_STATE);
    }

}
