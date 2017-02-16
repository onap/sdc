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

package org.openecomp.sdc.be.model.category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;

public class CategoryDefinition extends CategoryDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6552733796860992476L;

	List<SubCategoryDefinition> subcategories;

	public CategoryDefinition() {
		super();
	}

	public CategoryDefinition(CategoryDataDefinition c) {
		super(c);
	}

	public List<SubCategoryDefinition> getSubcategories() {
		return subcategories;
	}

	public void setSubcategories(List<SubCategoryDefinition> subcategories) {
		this.subcategories = subcategories;
	}

	public void addSubCategory(SubCategoryDefinition subcategory) {
		if (subcategories == null) {
			subcategories = new ArrayList<SubCategoryDefinition>();
		}
		subcategories.add(subcategory);
	}

	@Override
	public String toString() {
		return super.toString() + " CategoryDefinition [subcategories=" + subcategories + "]";
	}

}
