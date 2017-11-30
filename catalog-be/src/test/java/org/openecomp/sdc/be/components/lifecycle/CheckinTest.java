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
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CheckinTest extends LifecycleTestBase {

	private CheckinTransition checkinObj = null;
	private ComponentsUtils componentsUtils = new ComponentsUtils();

	@Before
	public void setup() {

		super.setup();

		// checkout transition object
		checkinObj = new CheckinTransition(componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,  titanDao);
		checkinObj.setLifeCycleOperation(toscaElementLifecycleOperation);
		checkinObj.setConfigurationManager(configurationManager);
		componentsUtils.Init();
	}

	@Test
	public void testSimpleCheckin() {
		Either<Boolean, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Either<User, ResponseFormat> ownerResponse = checkinObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		changeStateResult = checkinObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(changeStateResult.isLeft(), true);

	}

	@Test
	public void testSimpleServiceCheckin() {
		Either<Boolean, ResponseFormat> changeStateResult;
		Service service = createServiceObject(false);

		service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Either<User, ResponseFormat> ownerResponse = checkinObj.getComponentOwner(service, ComponentTypeEnum.SERVICE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		changeStateResult = checkinObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(changeStateResult.isLeft(), true);

	}

	@Test
	public void testCheckinTwiceValidation() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		Either<User, ResponseFormat> owner = checkinObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(owner.isLeft());
		// changeStateResult = checkinObj.changeStateOperation(resource, user,
		// owner.left().value());
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkinObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner.left().value(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());

		assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testServiceCheckinTwiceValidation() {
		Either<Service, ResponseFormat> changeStateResult;
		Service service = createServiceObject(false);

		service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		Either<User, ResponseFormat> owner = checkinObj.getComponentOwner(service, ComponentTypeEnum.SERVICE);
		assertTrue(owner.isLeft());

		Either<Boolean, ResponseFormat> validateBeforeTransition = checkinObj.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, user, owner.left().value(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());

		assertServiceResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CHECKED_IN, service.getName(), ComponentTypeEnum.SERVICE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testCheckoutByAnotherUserValidation() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject();

		User modifier = new User();
		modifier.setUserId("modifier");
		modifier.setFirstName("Albert");
		modifier.setLastName("Einstein");
		modifier.setRole(Role.DESIGNER.name());

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Either<User, ResponseFormat> ownerResponse = checkinObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		// changeStateResult = checkinObj.changeStateOperation(resource,
		// modifier, owner);
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkinObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertEquals(changeStateResult.isRight(), true);

		assertResponse(changeStateResult, ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testServiceCheckoutByAnotherUserValidation() {
		Either<Service, ResponseFormat> changeStateResult;
		Service service = createServiceObject(false);

		User modifier = new User();
		modifier.setUserId("modifier");
		modifier.setFirstName("Albert");
		modifier.setLastName("Einstein");
		modifier.setRole(Role.DESIGNER.name());

		service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		Either<User, ResponseFormat> ownerResponse = checkinObj.getComponentOwner(service, ComponentTypeEnum.SERVICE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		Either<Boolean, ResponseFormat> validateBeforeTransition = checkinObj.validateBeforeTransition(service, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertEquals(changeStateResult.isRight(), true);

		assertServiceResponse(changeStateResult, ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, service.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}
}
