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

import java.util.List;

public class ToscaDefinition {

	String toscaDefinitionVersion;
	List<ToscaImportsDefinition> toscaImports;
	List<ToscaNodeTypesDefinition> toscaNodeTypes;
	ToscaTopologyTemplateDefinition toscaTopologyTemplate;

	public ToscaDefinition() {
		super();
	}

	public String getToscaDefinitionVersion() {
		return toscaDefinitionVersion;
	}

	public void setToscaDefinitionVersion(String toscaDefinitionVersion) {
		this.toscaDefinitionVersion = toscaDefinitionVersion;
	}

	public List<ToscaImportsDefinition> getToscaImports() {
		return toscaImports;
	}

	public void setToscaImports(List<ToscaImportsDefinition> toscaImports) {
		this.toscaImports = toscaImports;
	}

	public List<ToscaNodeTypesDefinition> getToscaNodeTypes() {
		return toscaNodeTypes;
	}

	public void setToscaNodeTypes(List<ToscaNodeTypesDefinition> toscaNodeTypes) {
		this.toscaNodeTypes = toscaNodeTypes;
	}

	public ToscaTopologyTemplateDefinition getToscaTopologyTemplate() {
		return toscaTopologyTemplate;
	}

	public void setToscaTopologyTemplate(ToscaTopologyTemplateDefinition toscaTopologyTemplate) {
		this.toscaTopologyTemplate = toscaTopologyTemplate;
	}

	@Override
	public String toString() {
		return "ToscaDefinition [toscaDefinitionVersion=" + toscaDefinitionVersion + ", toscaImports=" + toscaImports
				+ ", toscaNodeTypes=" + toscaNodeTypes + ", toscaTopologyTemplate=" + toscaTopologyTemplate + "]";
	}

}
