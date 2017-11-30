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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CertificationChangeTransitionTest extends LifecycleTestBase {

	private CertificationChangeTransition certifyTransitionObj = null;
	private CertificationChangeTransition certificationCancelObj = null;
	private CertificationChangeTransition certificationFailObj = null;

	private ComponentsUtils componentsUtils = new ComponentsUtils();
	private User owner = null;

	Resource resource;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {

		super.setup();
		componentsUtils.Init();
		// checkout transition object
		certifyTransitionObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade, titanDao);
		certifyTransitionObj.setConfigurationManager(configurationManager);
		certifyTransitionObj.setLifeCycleOperation(toscaElementLifecycleOperation);
		
		certificationCancelObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation,  toscaOperationFacade, titanDao);
		certificationCancelObj.setConfigurationManager(configurationManager);
		certificationCancelObj.setLifeCycleOperation(toscaElementLifecycleOperation);
		
		certificationFailObj = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade, titanDao);
		certificationFailObj.setConfigurationManager(configurationManager);
		certificationFailObj.setLifeCycleOperation(toscaElementLifecycleOperation);
		
		owner = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);

		resource = createResourceObject();
	}
	
	@Test
	public void testVFCMTStateValidation(){
		Resource resource = createResourceVFCMTObject();
				
		User user = new User("cs0008", "Carlos", "Santana", "cs@sdc.com", "DESIGNER", null);
				
		Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(validateBeforeTransition.isLeft(), true);
	}

	@Test
	public void testStateValidationSuccess() {

		Either<Boolean, ResponseFormat> changeStateResult = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(changeStateResult.isLeft(), true);

	}

	@Test
	public void testStateValidationFail() {

		// checkout
		Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		assertValidationStateErrorResponse(validateBeforeTransition);

		// checkin
		validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertValidationStateErrorResponse(validateBeforeTransition);

		// rfc
		validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertValidationStateErrorResponse(validateBeforeTransition);

		// certified
		validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFIED);
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

		Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.RESTRICTED_OPERATION);

		modifier.setRole(Role.TESTER.name());
		validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testRolesSuccess() {

		resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<User, ResponseFormat> ownerResponse = certifyTransitionObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();

		Either<Boolean, ResponseFormat> validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, owner, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(true, validateBeforeTransition.isLeft());

		User modifier = new User();
		modifier.setUserId("modifier");
		modifier.setFirstName("Albert");
		modifier.setLastName("Einstein");
		modifier.setRole(Role.ADMIN.name());
		validateBeforeTransition = certifyTransitionObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(true, validateBeforeTransition.isLeft());

	}

	private void assertValidationStateErrorResponse(Either<Boolean, ResponseFormat> validateBeforeTransition) {
		assertEquals(validateBeforeTransition.isRight(), true);
		ResponseFormat error = validateBeforeTransition.right().value();
		Either<Resource, ResponseFormat> changeStateResult = Either.right(error);
		assertEquals(changeStateResult.isRight(), true);

		assertResponse(changeStateResult, ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
	}

}
