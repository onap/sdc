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

public class UploadCapInfo extends UploadInfo {
	/**
	 * specify the node type(Optional by tosca)
	 */
	private List<String> validSourceTypes;

	private List<UploadPropInfo> properties;

	private String node;

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public List<String> getValidSourceTypes() {
		return validSourceTypes;
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		this.validSourceTypes = validSourceTypes;
	}

	public List<UploadPropInfo> getProperties() {
		return properties;
	}

	public void setProperties(List<UploadPropInfo> properties) {
		this.properties = properties;
	}
}
