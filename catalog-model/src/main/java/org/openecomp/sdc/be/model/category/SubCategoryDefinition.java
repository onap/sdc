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

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;

public class SubCategoryDefinition extends SubCategoryDataDefinition {

	private List<GroupingDefinition> groupings;

	public SubCategoryDefinition() {
		super();
	}

	public SubCategoryDefinition(SubCategoryDataDefinition subCategory) {
		super(subCategory);
	}

	public List<GroupingDefinition> getGroupings() {
		return groupings;
	}

	public void setGroupings(List<GroupingDefinition> groupingDefinitions) {
		this.groupings = groupingDefinitions;
	}

	public void addGrouping(GroupingDefinition groupingDefinition) {
		if (groupings == null) {
			groupings = new ArrayList<GroupingDefinition>();
		}
		groupings.add(groupingDefinition);
	}

	@Override
	public String toString() {
		return super.toString() + " SubCategoryDefinition [groupings=" + groupings + "]";
	}

}
