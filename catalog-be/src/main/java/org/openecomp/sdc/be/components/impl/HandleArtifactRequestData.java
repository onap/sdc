/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

public class HandleArtifactRequestData {

	private String componentId; 
	private String userId; 
	private ComponentTypeEnum componentType;
	private ArtifactOperationInfo operation;
	private String artifactId;
	private ArtifactDefinition artifactInfo;
	private String origMd5;
	private String originData;
	private String interfaceUuid;
	private String operationUuid;
	private String parentId;
	private String containerComponentType;

	public String getComponentId() {
		return componentId;
	}

	public HandleArtifactRequestData setComponentId(String componentId) {
		this.componentId = componentId;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public HandleArtifactRequestData setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public ComponentTypeEnum getComponentType() {
		return componentType;
	}

	public HandleArtifactRequestData setComponentType(ComponentTypeEnum componentType) {
		this.componentType = componentType;
		return this;
	}

	public ArtifactOperationInfo getOperation() {
		return operation;
	}

	public HandleArtifactRequestData setOperation(ArtifactOperationInfo operation) {
		this.operation = operation;
		return this;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public HandleArtifactRequestData setArtifactId(String artifactId) {
		this.artifactId = artifactId;
		return this;
	}

	public ArtifactDefinition getArtifactInfo() {
		return artifactInfo;
	}

	public HandleArtifactRequestData setArtifactInfo(ArtifactDefinition artifactInfo) {
		this.artifactInfo = artifactInfo;
		return this;
	}

	public String getOrigMd5() {
		return origMd5;
	}

	public HandleArtifactRequestData setOrigMd5(String origMd5) {
		this.origMd5 = origMd5;
		return this;
	}

	public String getOriginData() {
		return originData;
	}

	public HandleArtifactRequestData setOriginData(String originData) {
		this.originData = originData;
		return this;
	}

	public String getInterfaceUuid() {
		return interfaceUuid;
	}

	public HandleArtifactRequestData setInterfaceUuid(String interfaceUuid) {
		this.interfaceUuid = interfaceUuid;
		return this;
	}

	public String getOperationUuid() {
		return operationUuid;
	}

	public HandleArtifactRequestData setOperationUuid(String operationUuid) {
		this.operationUuid = operationUuid;
		return this;
	}

	public String getParentId() {
		return parentId;
	}

	public HandleArtifactRequestData setParentId(String parentId) {
		this.parentId = parentId;
		return this;
	}

	public String getContainerComponentType() {
		return containerComponentType;
	}

	public HandleArtifactRequestData setContainerComponentType(String containerComponentType) {
		this.containerComponentType = containerComponentType;
		return this;
	}


}
