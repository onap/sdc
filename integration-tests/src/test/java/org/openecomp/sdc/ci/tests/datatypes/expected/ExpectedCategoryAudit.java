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

package org.openecomp.sdc.ci.tests.datatypes.expected;

public class ExpectedCategoryAudit {
	String action;
	String modifier;
	String modifierUid;
	String modifierName;
	String categoryName;
	String subCategoryName;
	String groupingName;
	String resourceType;
	String status;
	String desc;
	String details;

	public ExpectedCategoryAudit(String action, String modifier, String categoryName, String subCategoryName,
			String groupingName, String resourceType, String status, String desc) {
		super();
		this.action = action;
		this.modifier = modifier;
		this.categoryName = categoryName;
		this.subCategoryName = subCategoryName;
		this.groupingName = groupingName;
		this.resourceType = resourceType;
		this.status = status;
		this.desc = desc;
	}

	public ExpectedCategoryAudit() {
		action = null;
		modifier = null;
		categoryName = null;
		subCategoryName = null;
		groupingName = null;
		resourceType = null;
		status = null;
		desc = null;
		details = null;
		modifierName = null;
		modifierUid = null;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public String getGroupingName() {
		return groupingName;
	}

	public void setGroupingName(String groupingName) {
		this.groupingName = groupingName;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getModifierUid() {
		return modifierUid;
	}

	public void setModifierUid(String modifierUid) {
		this.modifierUid = modifierUid;
	}

	public String getModifierName() {
		return modifierName;
	}

	public void setModifierName(String modifierName) {
		this.modifierName = modifierName;
	}

}
