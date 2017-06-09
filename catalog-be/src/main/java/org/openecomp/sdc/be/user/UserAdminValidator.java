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

package org.openecomp.sdc.be.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAdminValidator {

	private Pattern emailPat;
	private Pattern userIdPat;
	private Matcher matcher;

	private static UserAdminValidator userAdminValidator = null;

	public static synchronized UserAdminValidator getInstance() {
		if (userAdminValidator == null) {
			userAdminValidator = new UserAdminValidator();
		}
		return userAdminValidator;
	}

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static final String USER_ID_PATTERN = "\\w{1,25}";

	private UserAdminValidator() {
		emailPat = Pattern.compile(EMAIL_PATTERN);
		userIdPat = Pattern.compile(USER_ID_PATTERN);
	}

	public boolean validateEmail(final String hex) {
		matcher = emailPat.matcher(hex);
		return matcher.matches();
	}

	public boolean validateUserId(String userId) {
		matcher = userIdPat.matcher(userId);
		return matcher.matches();
	}

	public boolean validateRole(String role) {
		for (Role r : Role.values()) {
			if (r.name().equals(role)) {
				return true;
			}
		}
		return false;
	}
}
