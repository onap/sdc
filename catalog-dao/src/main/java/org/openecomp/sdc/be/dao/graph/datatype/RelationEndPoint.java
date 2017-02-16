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

package org.openecomp.sdc.be.dao.graph.datatype;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RelationEndPoint {
	NodeTypeEnum label;
	String idName;
	Object idValue;

	public RelationEndPoint(NodeTypeEnum label, String idName, Object idValue) {
		super();
		this.label = label;
		this.idName = idName;
		this.idValue = idValue;
	}

	public NodeTypeEnum getLabel() {
		return label;
	}

	public void setLabel(NodeTypeEnum label) {
		this.label = label;
	}

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}

	public Object getIdValue() {
		return idValue;
	}

	public void setIdValue(Object idValue) {
		this.idValue = idValue;
	}

	@Override
	public String toString() {
		return "RelationEndPoint [label=" + label + ", idName=" + idName + ", idValue=" + idValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idName == null) ? 0 : idName.hashCode());
		result = prime * result + ((idValue == null) ? 0 : idValue.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationEndPoint other = (RelationEndPoint) obj;
		if (idName == null) {
			if (other.idName != null)
				return false;
		} else if (!idName.equals(other.idName))
			return false;
		if (idValue == null) {
			if (other.idValue != null)
				return false;
		} else if (!idValue.equals(other.idValue))
			return false;
		if (label != other.label)
			return false;
		return true;
	}

}
