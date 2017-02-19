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

package org.openecomp.sdc.be.dao;

//import com.datastax.driver.mapping.annotations.Column;
//import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.Objects;

@Table(keyspace = "dstest", name = "accounts")
public class Account {

	@PartitionKey
	private String email;
	private String name;
	// @Column (name = "addr")
	// @Frozen
	// private Address address;

	public Account() {
	}

	// public Account(String name, String email, Address address) {
	public Account(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	//
	// public Address getAddress() {
	// return address;
	// }
	//
	// public void setAddress(Address address) {
	// this.address = address;
	// }

	@Override
	public boolean equals(Object other) {
		if (other instanceof Account) {
			Account that = (Account) other;
			return Objects.equal(this.name, that.name) && Objects.equal(this.email, that.email);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, email);
	}

}
