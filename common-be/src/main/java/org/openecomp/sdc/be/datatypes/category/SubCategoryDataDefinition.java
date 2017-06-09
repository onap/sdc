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

package org.openecomp.sdc.be.datatypes.category;

import java.io.Serializable;
import java.util.List;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class SubCategoryDataDefinition extends ToscaDataDefinition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8279397988497086676L;

	private String name;
	private String normalizedName;
	private String uniqueId;
	private List<String> icons;

	public SubCategoryDataDefinition() {

	}

	public SubCategoryDataDefinition(SubCategoryDataDefinition c) {
		this.name = c.name;
		this.normalizedName = c.normalizedName;
		this.uniqueId = c.uniqueId;
		this.icons = c.icons;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public void setNormalizedName(String normalizedName) {
		this.normalizedName = normalizedName;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public List<String> getIcons() {
		return icons;
	}

	public void setIcons(List<String> icons) {
		this.icons = icons;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((normalizedName == null) ? 0 : normalizedName.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((icons == null) ? 0 : icons.hashCode());
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
		SubCategoryDataDefinition other = (SubCategoryDataDefinition) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (normalizedName == null) {
			if (other.normalizedName != null)
				return false;
		} else if (!normalizedName.equals(other.normalizedName))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (icons == null) {
			if (other.icons != null)
				return false;
		} else if (!icons.equals(other.icons))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubCategoryDataDefinition [name=" + name + ", normalizedName=" + normalizedName + ", uniqueId="
				+ uniqueId + ", icons=" + icons + "]";
	}
}
