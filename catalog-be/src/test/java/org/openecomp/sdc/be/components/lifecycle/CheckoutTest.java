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
import org.mockito.InjectMocks;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CheckoutTest extends LifecycleTestBase {

	private CheckoutTransition checkoutObj = null;
	private ComponentsUtils componentsUtils = new ComponentsUtils();
	@InjectMocks
	ResourceBusinessLogic bl = new ResourceBusinessLogic();

	@Before
	public void setup() {

		super.setup();

		// checkout transition object
		checkoutObj = new CheckoutTransition(componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,  titanDao);
		checkoutObj.setLifeCycleOperation(toscaElementLifecycleOperation);
		checkoutObj.setConfigurationManager(configurationManager);
		componentsUtils.Init();
		bl.setToscaOperationFacade(toscaOperationFacade);
		bl.setComponentsUtils(componentsUtils);

	}

	@Test
	public void testCheckoutStateValidation() {
		Either<? extends Component, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		changeStateResult = checkoutObj.changeState(ComponentTypeEnum.RESOURCE, resource, bl, user, owner, false, false);
		assertEquals(changeStateResult.isLeft(), true);

		resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		changeStateResult = checkoutObj.changeState(ComponentTypeEnum.RESOURCE, resource, bl, user, owner, false, false);
		assertEquals(changeStateResult.isLeft(), true);

	}

	@Test
	public void testAlreadyCheckout() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());

		assertEquals(changeStateResult.isRight(), true);
		assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CHECKOUT_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testCertificationInProgress() {
		Either<? extends Component, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		changeStateResult = checkoutObj.changeState(ComponentTypeEnum.RESOURCE, resource, bl, user, owner, false, false);

		Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertEquals(changeStateResult.isRight(), true);

		assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testReadyForCertification() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.READY_FOR_CERTIFICATION);

		// if modifier = owner
		Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		// changeStateResult = checkoutObj.changeStateOperation(resource, user,
		// owner);
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(validateBeforeTransition.isLeft(), true);

		// else
		User modifier = new User();
		modifier.setUserId("modifier");
		modifier.setFirstName("Albert");
		modifier.setLastName("Einstein");

		// admin
		modifier.setRole(Role.ADMIN.name());
		// changeStateResult = checkoutObj.changeStateOperation(resource, user,
		// owner);
		validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(validateBeforeTransition.isLeft(), true);

		// designer
		modifier.setRole(Role.TESTER.name());
		validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());

		assertEquals(changeStateResult.isRight(), true);
		assertResponse(changeStateResult, ActionStatus.RESTRICTED_OPERATION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testRoles() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

		User modifier = new User();
		modifier.setUserId("modifier");
		modifier.setFirstName("Albert");
		modifier.setLastName("Einstein");
		modifier.setRole(Role.DESIGNER.name());
		Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		// changeStateResult = checkoutObj.changeStateOperation(resource,
		// modifier, owner);
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(validateBeforeTransition.isLeft(), true);

		modifier.setRole(Role.TESTER.name());
		// changeStateResult = checkoutObj.changeStateOperation(resource,
		// modifier, owner);
		validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.RESTRICTED_OPERATION);

	}
}
