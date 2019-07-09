/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.webseal.simulator;

public class User {

	private String firstName;
	private String lastName;
	private String email;
	private String userId;
	private String role;
	
	private String password;
	
	public User(){		
	}
	
	public User(String userId){
		setUserId(userId);
	}

	public User(String firstName,String lastName,String email,String userId, String role, String password){
		setUserId(userId);
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
		setPassword(password);
		setRole(role);
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUserRef() {
		return "<a href='?userId="+getUserId()+"&password="+getPassword()+"'>"+getFirstName()+" "+getLastName()+"</a>";
	}
	
	public String getUserCreateRef() {
		return "<a href='create?userId="+getUserId()+"&firstName="+getFirstName()+"&lastName="+getLastName()+"&role=" + getRole() + "&email=" + getEmail() + "' target='resultFrame'>create</a>";
	}

	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", userId=" + userId
				+ ", role=" + role + ", password=" + password + "]";
	}

}
