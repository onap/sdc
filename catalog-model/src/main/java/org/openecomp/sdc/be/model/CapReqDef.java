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
import java.util.Map;

public class CapReqDef {
	Map<String, List<CapabilityDefinition>> capabilities;
	Map<String, List<RequirementDefinition>> requirements;

	public CapReqDef() {
		super();
	}

	public CapReqDef(Map<String, List<RequirementDefinition>> requirements,
			Map<String, List<CapabilityDefinition>> capabilities) {
		super();
		this.capabilities = capabilities;
		this.requirements = requirements;
	}

	public Map<String, List<CapabilityDefinition>> getCapabilities() {
		return capabilities;
	}

	public Map<String, List<RequirementDefinition>> getRequirements() {
		return requirements;
	}

	public void setCapabilities(Map<String, List<CapabilityDefinition>> capabilities) {
		this.capabilities = capabilities;
	}

	public void setRequirements(Map<String, List<RequirementDefinition>> requirements) {
		this.requirements = requirements;
	}
}
