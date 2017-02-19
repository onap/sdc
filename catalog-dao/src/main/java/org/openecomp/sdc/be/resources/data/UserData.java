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

package org.openecomp.sdc.be.resources.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.DaoUtils;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class UserData extends GraphNode {

	private String firstName;

	private String lastName;

	private String userId;

	private String email;

	private String role;

	private String status;

	private Long lastLoginTime;

	public UserData(String firstName, String lastName, String userId, String email, String role, String status,
			Long lastLoginTime) {
		super(NodeTypeEnum.User);
		this.firstName = firstName;
		this.lastName = lastName;
		this.userId = userId;
		this.email = email;
		this.role = role;
		this.status = status;
		this.lastLoginTime = lastLoginTime;
	}

	public UserData() {
		super(NodeTypeEnum.User);
	}

	public UserData(Map<String, Object> properties) {
		super(NodeTypeEnum.User);

		setFirstName((String) properties.get("firstName"));
		setLastName((String) properties.get("lastName"));
		setUserId((String) properties.get("userId"));
		setEmail((String) properties.get("email"));
		setRole((String) properties.get("role"));
		setStatus((String) properties.get("status"));
		setLastLoginTime((Long) properties.get("lastLoginTime"));
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

	// right name?
	public void setLastLoginTime() {
		Date d = new Date();
		this.lastLoginTime = new Long(d.getTime()); // this is in milli-seconds
													// divide by 1000 to get
													// secs?
	}

	public void setLastLoginTime(Long time) {
		this.lastLoginTime = time;
	}

	public Long getLastLoginTime() {
		return this.lastLoginTime;
	}

	@Override
	public String toString() {
		return "UserData [firstName=" + firstName + ", lastName=" + lastName + ", userId=" + userId + ", email=" + email
				+ ", role=" + role + ", last Login time=" + lastLoginTime + ", parent: " + super.toString() + "]";
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
		UserData other = (UserData) obj;
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

	public String toJson() {
		return DaoUtils.convertToJson(toGraphMap());
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		addIfExists(map, GraphPropertiesDictionary.USER_ID, userId);
		addIfExists(map, GraphPropertiesDictionary.EMAIL, email);
		addIfExists(map, GraphPropertiesDictionary.FIRST_NAME, firstName);
		addIfExists(map, GraphPropertiesDictionary.LAST_NAME, lastName);
		addIfExists(map, GraphPropertiesDictionary.ROLE, role);
		addIfExists(map, GraphPropertiesDictionary.USER_STATUS, status);
		addIfExists(map, GraphPropertiesDictionary.LAST_LOGIN_TIME, lastLoginTime);
		return map;
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.USER_ID.getProperty();
	}

	@Override
	public Object getUniqueId() {
		return userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
