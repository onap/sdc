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

package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;

public class ToscaInputsTopologyTemplateDefinition {
	
	private Map<String,Map<String,Object>> inputs;

	public ToscaInputsTopologyTemplateDefinition() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Map<String, Map<String,Object>> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Map<String,Object>> inputs) {
		this.inputs = inputs;
	}
	
	
	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaInputsTopologyTemplateDefinition.class);
    	return typeDescription;
	}

}
