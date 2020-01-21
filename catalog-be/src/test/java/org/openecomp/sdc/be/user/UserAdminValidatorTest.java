/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.user;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UserAdminValidatorTest {

	@Test
	public void testGetInstance() {
		UserAdminValidator result = createTestSubject();
		assertThat(result, is(IsNull.notNullValue()));
	}

	@Test
	public void testShouldValidateCorrectEmail() {
		UserAdminValidator validator = createTestSubject();
		String email = "test@test.com";
		boolean result = validator.validateEmail(email);
		assertThat(result, is(true));
	}

	@Test
	public void testShouldNotValidateEmailWithoutAt() {
		UserAdminValidator validator = createTestSubject();
		String email = "test#test.com";
		boolean result = validator.validateEmail(email);
		assertThat(result, is(false));
	}

	@Test
	public void testShouldNotValidateEmailWithoutDomainSuffix() {
		UserAdminValidator validator = createTestSubject();
		String email = "test@test";
		boolean result = validator.validateEmail(email);
		assertThat(result, is(false));
	}

	@Test
	public void testShouldNotValidateEmailWithoutPrefix() {
		UserAdminValidator validator = createTestSubject();
		String email = "@test.com";
		boolean result = validator.validateEmail(email);
		assertThat(result, is(false));
	}

	@Test
	public void testShouldValidateUserId() {
		UserAdminValidator testSubject = createTestSubject();
		String userId = "User";
		boolean result = testSubject.validateUserId(userId);
		assertThat(result, is(true));
	}

	@Test
	public void testShouldNotValidateUserIdLongerThan25Characters() {
		UserAdminValidator testSubject = createTestSubject();
		String userId = "User1user2user3user4user5toLong";
		boolean result = testSubject.validateUserId(userId);
		assertThat(result, is(false));
	}

	@Test
	public void testShouldNotValidateUserIdWithMulipleWords() {
		UserAdminValidator testSubject = createTestSubject();
		String userId = "User 1";
		boolean result = testSubject.validateUserId(userId);
		assertThat(result, is(false));
	}

	@Test
	public void testShouldNotValidateEmptyUserId() {
		UserAdminValidator testSubject = createTestSubject();
		String userId = "";
		boolean result = testSubject.validateUserId(userId);
		assertThat(result, is(false));
	}

	@Test
	public void testValidateCorrectRole() {
		UserAdminValidator testSubject = createTestSubject();
		String role = "ADMIN";
		boolean result = testSubject.validateRole(role);
		assertThat(result, is(true));
	}

	@Test
	public void testValidateIncorrectRole() {
		UserAdminValidator testSubject = createTestSubject();
		String role = "DEVELOPER";
		boolean result = testSubject.validateRole(role);
		assertThat(result, is(false));
	}

	private UserAdminValidator createTestSubject() {
		return UserAdminValidator.getInstance();
	}

}