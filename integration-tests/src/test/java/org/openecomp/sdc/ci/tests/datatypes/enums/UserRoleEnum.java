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

package org.openecomp.sdc.ci.tests.datatypes.enums;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.ci.tests.config.UserCredentialsFromFile;
import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;

@Getter
public enum UserRoleEnum {

	ADMIN("admin"),
	ADMIN2("admin"),
    DESIGNER("designer"),
	DESIGNER2("designer"),
    TESTER("tester"),
    GOVERNOR("governor"),
    OPS("ops"),
	PRODUCT_STRATEGIST1("ops"),
	PRODUCT_STRATEGIST2("ops"),
	PRODUCT_STRATEGIST3("ops"),
	PRODUCT_MANAGER1("ops"),
	PRODUCT_MANAGER2("ops");

	private String userName;
	private UserCredentials credentials;
	private String password;
	@Setter
	private String userId;
	private String firstName;
    private String lastName;
    private String userRole;

    UserRoleEnum(final String userRole) {
        final UserCredentialsFromFile instance = UserCredentialsFromFile.getInstance();
		this.credentials = instance.getUserCredentialsByRole(userRole);
		this.userId = this.credentials.getUserId();
		this.firstName = this.credentials.getFirstName();
		this.lastName = this.credentials.getLastName();
		this.password = this.credentials.getPassword();
		this.userName = this.firstName + " " + this.lastName;
		this.userRole = userRole;
	}

}
