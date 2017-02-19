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

package org.openecomp.sdc.be.model;

import java.io.Serializable;
import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

public class ComponentInstanceProperty extends PropertyDefinition
		implements IComponentInstanceConnectedElement, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6559573536869242691L;

	/**
	 * Value of property
	 */
	private String value;

	/**
	 * The unique id of the property value on graph
	 */
	private String valueUniqueUid;

	private List<String> path = null;

	private List<PropertyRule> rules = null;

	private List<GetInputValueInfo> getInputValues;

	public ComponentInstanceProperty() {
		super();
	}

	public ComponentInstanceProperty(PropertyDefinition pd, String value, String valueUniqueUid) {
		super(pd);

		this.value = value;
		this.valueUniqueUid = valueUniqueUid;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValueUniqueUid() {
		return valueUniqueUid;
	}

	public void setValueUniqueUid(String valueUniqueUid) {
		this.valueUniqueUid = valueUniqueUid;
	}

	public boolean isDefinition() {
		return definition;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public List<PropertyRule> getRules() {
		return rules;
	}

	public void setRules(List<PropertyRule> rules) {
		this.rules = rules;
	}

	public List<GetInputValueInfo> getGetInputValues() {
		return getInputValues;
	}

	public void setGetInputValues(List<GetInputValueInfo> getInputValues) {
		this.getInputValues = getInputValues;
	}

	@Override
	public String toString() {
		return "ComponentInstanceProperty [ " + super.toString() + " , value=" + value + ", valueUniqueUid = "
				+ valueUniqueUid + " , rules=" + rules + " , path=" + path + " ]";
	}

}
