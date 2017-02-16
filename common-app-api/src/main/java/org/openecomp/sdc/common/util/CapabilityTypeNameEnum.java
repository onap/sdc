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

package org.openecomp.sdc.common.util;

public enum CapabilityTypeNameEnum {
	ROOT("tosca.capabilities.Root"), 
	NODE("tosca.capabilities.Node"), 
	CONTAINER("tosca.capabilities.Container"), 
	ENDPOINT("tosca.capabilities.Endpoint"), 
	ENDPOINT_PUBLIC("tosca.capabilities.Endpoint.Public"), 
	ENDPOINT_ADMIN("tosca.capabilities.Endpoint.Admin"), 
	ENDPOINT_DATABASE("tosca.capabilities.Endpoint.Database"), 
	OPERATING_SYSTEM("tosca.capabilities.OperatingSystem"), 
	SCALABLE("tosca.capabilities.Scalable"), 
	BINDABLE("tosca.capabilities.network.Bindable"),
	DOCKER("tosca.capabilities.Container.Docker"), 
	ATTACHMENT("tosca.capabilities.Attachment");

	private String capabilityName;

	private CapabilityTypeNameEnum(String capName) {
		this.capabilityName = capName;
	}

	public String getCapabilityName() {
		return capabilityName;
	}
}
