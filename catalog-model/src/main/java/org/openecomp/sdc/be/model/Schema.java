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

/**
 * Schema allows to create new types that can be used along TOSCA definitions.
 */
public class Schema {
	private String derivedFrom;
	private List<PropertyConstraint> constraints;
	private Map<String, PropertyDefinition> properties;
	private PropertyDefinition property;

	public PropertyDefinition getProperty() {
		return property;
	}

	public void setProperty(PropertyDefinition property) {
		this.property = property;
	}
}
