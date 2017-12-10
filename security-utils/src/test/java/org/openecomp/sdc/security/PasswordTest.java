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

package org.openecomp.sdc.security;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordTest {

	@Test
	public void hashtest() {
		String password = "123456";
		String hash = Passwords.hashPassword(password);
		assertTrue(Passwords.isExpectedPassword(password, hash));
		password = "1sdfgsgd23456";
		hash = Passwords.hashPassword(password);
		assertTrue(Passwords.isExpectedPassword(password, hash));
		password = "1sdfgsgd2345((*&%$%6";
		hash = Passwords.hashPassword(password);
		assertTrue(Passwords.isExpectedPassword(password, hash));
		password = "";
		hash = Passwords.hashPassword(password);
		assertTrue(Passwords.isExpectedPassword(password, hash));
		password = " ";
		hash = Passwords.hashPassword(password);
		assertTrue(Passwords.isExpectedPassword(password, hash));
	}

}
