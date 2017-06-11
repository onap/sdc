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

package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum NormativeTypesEnum {
	ROOT("tosca.nodes.Root", "root"), COMPUTE("tosca.nodes.Compute", "compute"), BLOCK_STORAGE("tosca.nodes.BlockStorage", "blockStorage"), CONTAINER_APPLICATION("tosca.nodes.Container.Application", "containerApplication"), 
	CONTAINER_RUNTIME("tosca.nodes.Container.Runtime","containerRuntime"), DATABASE("tosca.nodes.Database", "database"), DBMS("tosca.nodes.DBMS", "DBMS"), LOAD_BALANCER("tosca.nodes.LoadBalancer", "loadBalancer"), 
	OBJECT_STORAGE("tosca.nodes.ObjectStorage", "objectStorage"), NETWORK("tosca.nodes.network.Network", "network"), PORT("tosca.nodes.network.Port", "port"), SOFTWARE_COMPONENT("tosca.nodes.SoftwareComponent", "softwareComponent"), 
	WEB_APPLICATION("tosca.nodes.webapplication","webApplication"), WEB_SERVER("tosca.nodes.WebServer", "webServer");
	
	public String normativeName;
	private String folderName;

	private NormativeTypesEnum(String resourceName, String folderName) {
		this.normativeName = resourceName;
		this.folderName = folderName;
	}

	public String getNormativeName() {
		return normativeName;
	}

	public String getFolderName() {
		return folderName;
	}

}
