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

import org.openecomp.sdc.ci.tests.config.UserCredentialsFromFile;
import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;

public enum UserRoleEnum {

//	ADMIN("jh0003", "Jimmy", "Hendrix"), DESIGNER("cs0008", "Carlos", "Santana"), DESIGNER2("me0009", "Melissa","Etheridge"), TESTER("jm0007", "Joni", "Mitchell"), ADMIN4("km2000", "Kot", "May"),
//	GOVERNOR("gv0001","David", "Shadmi"), OPS("op0001", "Steve", "Regev"), PRODUCT_STRATEGIST1("ps0001", "Eden","Rozin"), PRODUCT_STRATEGIST2("ps0002", "Ella", "Kvetny"), PRODUCT_STRATEGIST3("ps0003", "Geva", "Alon"), 
//	PRODUCT_MANAGER1("pm0001", "Teddy", "Isashar"), PRODUCT_MANAGER2("pm0002", "Sarah", "Bettens");
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

	private String password;
	private String userId;
	private String firstName;
    private String lastName;


    private UserRoleEnum(String userRole) {
        String name = name();
        final UserCredentialsFromFile instance = UserCredentialsFromFile.getInstance();
        this.credentials = instance.getUserCredentialsByRole(userRole);
        this.userId = this.credentials.getUserId();
        this.firstName = this.credentials.getFirstName();
		this.lastName = this.credentials.getLastName();
		this.password = this.credentials.getPassword();
		this.userName = this.firstName + " " + this.lastName;
    }


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public UserCredentials getCredentials() {
		return credentials;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUserName() {
		return userName;
	}

	private String userName;
	private UserCredentials credentials;


	public String getPassword() {
		return password;
	}
}
