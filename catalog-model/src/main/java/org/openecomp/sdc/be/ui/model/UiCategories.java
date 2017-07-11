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

package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiCategories {
	
	private List<CategoryDefinition> resourceCategories;
	private List<CategoryDefinition> serviceCategories;
	private List<CategoryDefinition> productCategories;
	
	public List<CategoryDefinition> getResourceCategories() {
		return resourceCategories;
	}
	public void setResourceCategories(List<CategoryDefinition> resourceCategories) {
		this.resourceCategories = resourceCategories;
	}
	public List<CategoryDefinition> getServiceCategories() {
		return serviceCategories;
	}
	public void setServiceCategories(List<CategoryDefinition> serviceCategories) {
		this.serviceCategories = serviceCategories;
	}
	public List<CategoryDefinition> getProductCategories() {
		return productCategories;
	}
	public void setProductCategories(List<CategoryDefinition> productCategories) {
		this.productCategories = productCategories;
	}
}
