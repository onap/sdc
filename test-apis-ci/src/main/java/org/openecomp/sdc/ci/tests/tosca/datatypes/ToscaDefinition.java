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
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;

public class ToscaDefinition {

	String tosca_definitions_version;
	Map<String, String> metadata;
//	Map<String, ToscaImportsDefinition> imports;
//	TODO waiting for answer about imports representation
	Object imports;
	Map<String, ToscaNodeTypesDefinition> node_types;
	ToscaTopologyTemplateDefinition topology_template;

	public ToscaDefinition() {
		super();
	}

	@Override
	public String toString() {
		return "ToscaDefinition [tosca_definitions_version=" + tosca_definitions_version + ", metadata=" + metadata + ", imports=" + imports + ", node_types=" + node_types + ", topology_template=" + topology_template + "]";
	}

	public String getTosca_definitions_version() {
		return tosca_definitions_version;
	}

	public void setTosca_definitions_version(String tosca_definitions_version) {
		this.tosca_definitions_version = tosca_definitions_version;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	

	
//	public Map<String,ToscaImportsDefinition> getImports() {
//		return (Map<String, ToscaImportsDefinition>) imports;
//	}
//
//	public void setImports(Map<String,ToscaImportsDefinition> imports) {
//		this.imports = imports;
//	}
//	
//	public List<ToscaImportsDefinition> getImports() {
//		return (List<ToscaImportsDefinition>) imports;
//	}
//
//	public void setImports(List<ToscaImportsDefinition> imports) {
//		this.imports = imports;
//	}

	public Object getImports() {
		return imports;
	}

	public void setImports(Object imports) {
		this.imports = imports;
	}

	public Map<String, ToscaNodeTypesDefinition> getNode_types() {
		return node_types;
	}

	public void setNode_types(Map<String, ToscaNodeTypesDefinition> node_types) {
		this.node_types = node_types;
	}

	public ToscaTopologyTemplateDefinition getTopology_template() {
		return topology_template;
	}

	public void setTopology_template(ToscaTopologyTemplateDefinition topology_template) {
		this.topology_template = topology_template;
	}



	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaDefinition.class);
        typeDescription.putMapPropertyType("metadata", String.class, String.class);
//        TODO Andrey imports format
        typeDescription.putMapPropertyType("imports", String.class, ToscaImportsDefinition.class);
        typeDescription.putListPropertyType("imports", List.class);
        typeDescription.putMapPropertyType("node_types", String.class, ToscaNodeTypesDefinition.class);
    	return typeDescription;
	}
	
	
}
