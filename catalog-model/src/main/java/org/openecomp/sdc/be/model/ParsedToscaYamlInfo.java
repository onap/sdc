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

import java.util.Map;

public class ParsedToscaYamlInfo {
	Map<String, InputDefinition> inputs;

	Map<String, UploadComponentInstanceInfo> instances;

	Map<String, GroupDefinition> groups;

	public Map<String, UploadComponentInstanceInfo> getInstances() {
		return instances;
	}

	public void setInstances(Map<String, UploadComponentInstanceInfo> instances) {
		this.instances = instances;
	}

	public Map<String, GroupDefinition> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, GroupDefinition> groups) {
		this.groups = groups;
	}

	public Map<String, InputDefinition> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, InputDefinition> inputs) {
		this.inputs = inputs;
	}

	@Override
	public String toString() {
		return "ParsedToscaYamlInfo [inputs=" + inputs + ", instances=" + instances + ", groups=" + groups + "]";
	}

}
