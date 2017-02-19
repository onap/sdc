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

package org.openecomp.sdc.be.tosca;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.model.Component;

public class ToscaRepresentation {

	private String mainYaml;
	private List<Triple<String, String, Component>> dependencies;

	public String getMainYaml() {
		return mainYaml;
	}

	public void setMainYaml(String mainYaml) {
		this.mainYaml = mainYaml;
	}

	public List<Triple<String, String, Component>> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Triple<String, String, Component>> dependancies) {
		this.dependencies = dependancies;
	}

}
