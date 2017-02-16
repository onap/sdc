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

package org.openecomp.sdc.be.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.resources.data.UserData;

public class User {
	public static final String FORCE_DELETE_HEADER_FLAG = "FORCE_DELETE";

	private String firstName;

	private String lastName;

	private String userId;

	private String email;

	private String role;

	private Long lastLoginTime;

	private UserStatusEnum status = UserStatusEnum.ACTIVE;

	public User() {
	}

	public User(UserData userDate) {
		this(userDate.getFirstName(), userDate.getLastName(), userDate.getUserId(), userDate.getEmail(),
				userDate.getRole(), userDate.getLastLoginTime());
	}

	public User(String firstName, String lastName, String userId, String emailAddress, String role,
			Long lastLoginTime) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userId = userId;
		this.email = emailAddress;
		this.role = role;
		this.lastLoginTime = lastLoginTime;

	}

	public void copyData(User other) {
		this.firstName = other.getFirstName();
		this.lastName = other.getLastName();
		this.userId = other.getUserId();
		this.email = other.getEmail();
		this.role = other.getRole();
		this.lastLoginTime = other.getLastLoginTime();

	}

	public User(User aUser) {
		this(aUser.getFirstName(), aUser.getLastName(), aUser.getUserId(), aUser.getEmail(), aUser.getRole(),
				aUser.getLastLoginTime());
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFullName() {
		return this.getFirstName() + " " + this.getLastName();
	}

	public void setLastLoginTime() {
		DateTime now = new DateTime(DateTimeZone.UTC);
		this.lastLoginTime = now.getMillis();
	}

	public void setLastLoginTime(Long time) {
		this.lastLoginTime = time;
	}

	public Long getLastLoginTime() {
		return this.lastLoginTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((lastLoginTime == null) ? 0 : lastLoginTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (lastLoginTime == null) {
			if (other.lastLoginTime != null)
				return false;
		} else if (!lastLoginTime.equals(other.lastLoginTime))
			return false;
		return true;
	}

	public UserStatusEnum getStatus() {
		return status;
	}

	public void setStatus(UserStatusEnum status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName + ", userId=" + userId + ", email=" + email
				+ ", role=" + role + ", last login time=" + lastLoginTime + "]";
	}

}
