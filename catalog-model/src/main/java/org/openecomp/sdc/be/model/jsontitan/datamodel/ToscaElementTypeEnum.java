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

package org.openecomp.sdc.be.model.jsontitan.datamodel;

import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;

public enum ToscaElementTypeEnum {
	NodeType("node_type"),
	TopologyTemplate("topology_template");
	
	String value;
	private ToscaElementTypeEnum(String value){
		this.value = value;
	}
	
	public static  VertexTypeEnum getVertexTypeByToscaType(ToscaElementTypeEnum toscaType ){
		switch ( toscaType ){
		case NodeType :
			return VertexTypeEnum.NODE_TYPE;
		case TopologyTemplate :
			return VertexTypeEnum.TOPOLOGY_TEMPLATE;
		default :
			return  null;
		}
	}

	public String getValue() {
		return value;
	}
	
}
