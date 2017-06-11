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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupHeatMetaDefinition {

	private int group = 0;
//	private List<String> artifactList = new ArrayList<String>();
	private List<HeatMetaFirstLevelDefinition> artifactList = new ArrayList<HeatMetaFirstLevelDefinition>();
	@Override
	public String toString() {
		return "GroupHeatMetaDefinition [group=" + group + ", artifactList=" + artifactList + ", artifactMap=" + artifactMap + ", propertyHeatMetaDefinition=" + propertyHeatMetaDefinition + "]";
	}

	public List<HeatMetaFirstLevelDefinition> getArtifactList() {
		return artifactList;
	}

	public void setArtifactList(List<HeatMetaFirstLevelDefinition> artifactList) {
		this.artifactList = artifactList;
	}

	private Map<String, String> artifactMap = new HashMap<>();
	PropertyHeatMetaDefinition propertyHeatMetaDefinition;
	
	public Map<String, String> getArtifactMap() {
		return artifactMap;
	}

	public void setArtifactMap(Map<String, String> artifactMap) {
		this.artifactMap = artifactMap;
	}


	public PropertyHeatMetaDefinition getPropertyHeatMetaDefinition() {
		return propertyHeatMetaDefinition;
	}

	public void setPropertyHeatMetaDefinition(PropertyHeatMetaDefinition propertyHeatMetaDefinition) {
		this.propertyHeatMetaDefinition = propertyHeatMetaDefinition;
	}

	public GroupHeatMetaDefinition() {
		super();
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

//	public List<String> getArtifactList() {
//		return artifactList;
//	}
//
//	public void setArtifactList(List<String> artifactList) {
//		this.artifactList = artifactList;
//	}

}
