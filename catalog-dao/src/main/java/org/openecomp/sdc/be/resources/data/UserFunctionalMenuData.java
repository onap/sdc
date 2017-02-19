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

package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.DaoUtils;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class UserFunctionalMenuData extends GraphNode {

	private String uniqueId;

	private String functionalMenu;

	public UserFunctionalMenuData(String functionalMenu, String uniqueId) {
		super(NodeTypeEnum.UserFunctionalMenu);
		this.functionalMenu = functionalMenu;
		this.uniqueId = uniqueId;
	}

	public UserFunctionalMenuData() {
		super(NodeTypeEnum.UserFunctionalMenu);
	}

	public UserFunctionalMenuData(Map<String, Object> properties) {
		super(NodeTypeEnum.UserFunctionalMenu);

		setFunctionalMenu((String) properties.get(GraphPropertiesDictionary.FUNCTIONAL_MENU.getProperty()));
		setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
	}

	public String getFunctionalMenu() {
		return functionalMenu;
	}

	public void setFunctionalMenu(String functionalMenu) {
		this.functionalMenu = functionalMenu;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		return "UserFunctionalMenuData [uniqueId=" + uniqueId + ", functionalMenu=" + functionalMenu + "]";
	}

	public String toJson() {
		return DaoUtils.convertToJson(toGraphMap());
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);
		addIfExists(map, GraphPropertiesDictionary.FUNCTIONAL_MENU, functionalMenu);

		return map;
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	@Override
	public Object getUniqueId() {
		return uniqueId;
	}

}
