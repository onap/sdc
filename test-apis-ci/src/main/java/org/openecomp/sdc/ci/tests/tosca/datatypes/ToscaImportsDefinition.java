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

import org.yaml.snakeyaml.TypeDescription;

public class ToscaImportsDefinition {

	private String name;
	private String file;
	private String repository;
	private String namespace_uri;
	private String namespace_prefix;
	
	public ToscaImportsDefinition() {
		super();
	}

	public ToscaImportsDefinition(String name, String file, String repository, String namespace_uri, String namespace_prefix) {
		super();
		this.name = name;
		this.file = file;
		this.repository = repository;
		this.namespace_uri = namespace_uri;
		this.namespace_prefix = namespace_prefix;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getNamespace_uri() {
		return namespace_uri;
	}

	public void setNamespace_uri(String namespace_uri) {
		this.namespace_uri = namespace_uri;
	}

	public String getNamespace_prefix() {
		return namespace_prefix;
	}

	public void setNamespace_prefix(String namespace_prefix) {
		this.namespace_prefix = namespace_prefix;
	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaImportsDefinition.class);
    	return typeDescription;
	}

	
	
}
