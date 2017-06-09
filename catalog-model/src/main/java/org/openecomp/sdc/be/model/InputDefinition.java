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

public class InputDefinition extends PropertyDefinition {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6920076037265309468L;

	List<ComponentInstanceInput> inputs;
	List<ComponentInstanceProperty> properties;

	public InputDefinition() {
		super();
	}

	public InputDefinition(PropertyDataDefinition p) {
		super(p);
	}

	public InputDefinition(PropertyDefinition pd) {
		super(pd);
	}

	
	public List<ComponentInstanceInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<ComponentInstanceInput> inputs) {
		this.inputs = inputs;
	}

	public List<ComponentInstanceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ComponentInstanceProperty> properties) {
		this.properties = properties;
	}

}
