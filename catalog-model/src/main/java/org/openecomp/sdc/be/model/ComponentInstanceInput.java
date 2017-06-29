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

import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

public class ComponentInstanceInput extends InputDefinition implements IComponentInstanceConnectedElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3937554584759816724L;


	/**
	 * The unique id of the property value on graph
	 */
	private String valueUniqueUid;

	private List<String> path = null;

	private List<PropertyRule> rules = null;
	private String componentInstanceName;
	private String componentInstanceId;

	public ComponentInstanceInput() {
		super();
	}

	public ComponentInstanceInput(PropertyDataDefinition curPropertyDef, String inputId, String value,
			String valueUniqueUid) {
		super(curPropertyDef);
		setInputId(inputId);
		setValue(value);
		this.valueUniqueUid = valueUniqueUid;
	}

	public ComponentInstanceInput(InputDefinition pd, String value, String valueUniqueUid) {
		super(pd);

		setValue(value);
		this.valueUniqueUid = valueUniqueUid;
	}

	public ComponentInstanceInput(PropertyDataDefinition propertyDefinition) {
		super(propertyDefinition);
		if(propertyDefinition.getGetInputValues() != null && !propertyDefinition.getGetInputValues().isEmpty()){
			setInputId(propertyDefinition.getGetInputValues().get(0).getInputId());
		}
	}

	public String getComponentInstanceName() {
		return componentInstanceName;
	}

	public void setComponentInstanceName(String componentInstanceName) {
		this.componentInstanceName = componentInstanceName;
	}

	public String getComponentInstanceId() {
		return componentInstanceId;
	}

	public void setComponentInstanceId(String componentInstanceId) {
		this.componentInstanceId = componentInstanceId;
	}

	public String getValueUniqueUid() {
		return valueUniqueUid;
	}

	public void setValueUniqueUid(String valueUniqueUid) {
		this.valueUniqueUid = valueUniqueUid;
	}

	/*public boolean isDefinition() {
		return definition;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}*/

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

	@Override
	public String toString() {
		return "ComponentInstanceInput [ " + super.toString() + " , value=" + getValue() + ", valueUniqueUid = "
				+ valueUniqueUid + " , rules=" + rules + " , path=" + path + " ]";
	}

}
