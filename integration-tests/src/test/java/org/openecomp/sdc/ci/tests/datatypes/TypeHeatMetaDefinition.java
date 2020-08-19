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

import java.util.List;

public class TypeHeatMetaDefinition {

	String typeName;

	List<GroupHeatMetaDefinition> groupHeatMetaDefinition;

	public TypeHeatMetaDefinition() {
		super();
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<GroupHeatMetaDefinition> getGroupHeatMetaDefinition() {
		return groupHeatMetaDefinition;
	}

	public void setGroupHeatMetaDefinition(List<GroupHeatMetaDefinition> groupHeatMetaDefinition) {
		this.groupHeatMetaDefinition = groupHeatMetaDefinition;
	}

	@Override
	public String toString() {
		return "TypeHeatMetaDefinition [typeName=" + typeName + ", groupHeatMetaDefinition=" + groupHeatMetaDefinition
				+ "]";
	}

}
