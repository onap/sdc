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

package org.openecomp.sdc.be.model.operations.impl;

import org.springframework.stereotype.Component;

@Component("all-operations")
public class AllOperationsUtil {

	@javax.annotation.Resource
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource
	private RequirementOperation requirementOperation;

	@javax.annotation.Resource
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource
	private ResourceOperation resourceOperation;

	public PropertyOperation getPropertyOperation() {
		return propertyOperation;
	}

	public RequirementOperation getRequirementOperation() {
		return requirementOperation;
	}

	public CapabilityOperation getCapabilityOperation() {
		return capabilityOperation;
	}

	public ResourceOperation getResourceOperation() {
		return resourceOperation;
	}

}
