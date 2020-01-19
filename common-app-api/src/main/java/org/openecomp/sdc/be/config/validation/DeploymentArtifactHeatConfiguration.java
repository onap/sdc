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

package org.openecomp.sdc.be.config.validation;

import java.util.Map;

/**
 * Currently for deployment artifacts HEAT validation only.
 * 
 * Other artifacts might require different fields validation Be sure to check it
 * before you re-use this class
 * 
 * @author paharoni
 *
 */
public class DeploymentArtifactHeatConfiguration {

	// All the rest of heat file is not needed for now...
	String heat_template_version;

	Map<String, Object> resources;

	public String getHeat_template_version() {
		return heat_template_version;
	}

	public void setHeat_template_version(String heat_template_version) {
		this.heat_template_version = heat_template_version;
	}

	public Map<String, Object> getResources() {
		return resources;
	}

	public void setResources(Map<String, Object> resources) {
		this.resources = resources;
	}

}
