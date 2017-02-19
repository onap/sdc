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

import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class Product extends Component {

	public Product() {
		super(new ProductMetadataDefinition());
		componentType = ComponentTypeEnum.PRODUCT;
	}

	public Product(ProductMetadataDefinition productMetadataDefinition) {
		super(productMetadataDefinition);
		componentType = ComponentTypeEnum.PRODUCT;
	}

	public String getFullName() {
		return getProductMetadataDefinition().getFullName();
	}

	public void setFullName(String fullName) {
		getProductMetadataDefinition().setFullName(fullName);
	}

	public String getInvariantUUID() {
		return getProductMetadataDefinition().getInvariantUUID();
	}

	public void setInvariantUUID(String invariantUUID) {
		getProductMetadataDefinition().setInvariantUUID(invariantUUID);
	}

	public List<String> getContacts() {
		return getProductMetadataDefinition().getContacts();
	}

	public void setContacts(List<String> contacts) {
		getProductMetadataDefinition().setContacts(contacts);
	}

	public void addContact(String contact) {
		getProductMetadataDefinition().addContact(contact);
	}

	public Boolean getIsActive() {
		return getProductMetadataDefinition().getIsActive();
	}

	public void setIsActive(Boolean isActive) {
		getProductMetadataDefinition().setIsActive(isActive);
	}

	private ProductMetadataDataDefinition getProductMetadataDefinition() {
		return (ProductMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition();
	}

}
