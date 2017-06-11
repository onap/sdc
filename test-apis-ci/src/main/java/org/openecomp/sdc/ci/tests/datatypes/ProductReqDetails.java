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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class ProductReqDetails extends ComponentReqDetails {

	private String fullName;
	private List<String> contacts;
	private String isActive;

	public ProductReqDetails(String name, List<CategoryDefinition> category) {
		this.categories = category;
		this.name = name;
	}

	public ProductReqDetails(String name) {
		this.name = name;
	}

	public void addCategory(CategoryDefinition category) {
		if (categories == null) {
			categories = new ArrayList<>();
		}
		categories.add(category);
	}

	public void addContact(String contactUserId) {
		if (contacts == null) {
			contacts = new ArrayList<>();
		}
		contacts.add(contactUserId);
	}

	public List<String> getContacts() {
		return contacts;
	}

	public void setContacts(List<String> contacts) {
		this.contacts = contacts;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getActive() {
		return isActive;
	}

	public void setActive(String isActive) {
		this.isActive = isActive;
	}
}
