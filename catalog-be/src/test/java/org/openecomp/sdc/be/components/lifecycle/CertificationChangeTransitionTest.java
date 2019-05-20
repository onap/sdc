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
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CertificationChangeTransitionTest extends LifecycleTestBase {

    private CertificationChangeTransition certifyTransitionObj = null;
    private CertificationChangeTransition certificationCancelObj = null;
    private CertificationChangeTransition certificationFailObj = null;

    private User owner = null;

    Resource resource;
    Service service; 

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {

        super.setup();
        // checkout transition object
        certifyTransitionObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,
            janusGraphDao);
        certifyTransitionObj.setConfigurationManager(configurationManager);
        certifyTransitionObj.setLifeCycleOperation(toscaElementLifecycleOperation);

        certificationCancelObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation,  toscaOperationFacade,
            janusGraphDao);
        certificationCancelObj.setConfigurationManager(configurationManager);
        certificationCancelObj.setLifeCycleOperation(toscaElementLifecycleOperation);

        certificationFailObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,
            janusGraphDao);
        certificationFailObj.setConfigurationManager(configurationManager);
        certificationFailObj.setLifeCycleOperation(toscaElementLifecycleOperation);

        owner = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);

        resource = createResourceObject();
        service = createServiceObject();
    }
    
    @Test
    public void testConstructor(){
        Resource resource = createResourceVFCMTObject();

        User user = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);

        for (LifeCycleTransitionEnum value : LifeCycleTransitionEnum.values()) {
        	new CertificationChangeTransition(value, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,
              janusGraphDao);
		}
        
    }
    
    @Test
    public void testVFCMTStateValidation(){
        Resource resource = createResourceVFCMTObject();

        User user = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);

        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isLeft());
    }

    @Test
    public void testStateValidationSuccess() {

        Either<Boolean, ResponseFormat> changeStateResult = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(changeStateResult.isLeft());

    }
    
    @Test
    public void testStateValidationFail() {

        // checkout
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        assertValidationStateErrorResponse(validateBeforeTransition);

        // checkin
        validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertValidationStateErrorResponse(validateBeforeTransition);

        // rfc
        validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
        assertValidationStateErrorResponse(validateBeforeTransition);

        // certified
        validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.CERTIFIED);
        assertValidationStateErrorResponse(validateBeforeTransition);

    }

    @Test
    public void testRolesFail() {
        Either<Resource, ResponseFormat> changeStateResult;

        resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

        User modifier = new User();
        modifier.setUserId("modifier");
        modifier.setFirstName("Albert");
        modifier.setLastName("Einstein");
        modifier.setRole(Role.DESIGNER.name());
        Either<User, ResponseFormat> ownerResponse = certifyTransitionObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
//the lifecycle was changed for resource!!
        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isLeft());

        modifier.setRole(Role.TESTER.name());
        validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isLeft());

    }

    @Test
    public void testRolesSuccess() {

        resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        Either<User, ResponseFormat> ownerResponse = certifyTransitionObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, owner, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isLeft());

        User modifier = new User();
        modifier.setUserId("modifier");
        modifier.setFirstName("Albert");
        modifier.setLastName("Einstein");
        modifier.setRole(Role.ADMIN.name());
        validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isLeft());

    }

    private void assertValidationStateErrorResponse(Either<Boolean, ResponseFormat> validateBeforeTransition) {
        assertTrue(validateBeforeTransition.isRight());
        ResponseFormat error = validateBeforeTransition.right().value();
        Either<Resource, ResponseFormat> changeStateResult = Either.right(error);
        assertTrue(changeStateResult.isRight());

        assertResponse(changeStateResult, ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
    }

}
