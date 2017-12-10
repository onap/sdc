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

package org.openecomp.sdc.ci.tests.datatypes;

import org.openecomp.sdc.be.model.ComponentInstance;

public class ComponentInstanceReqDetails {

	String componentUid;
	String description;
	String posX;
	String posY;
	String name;
	String uniqueId;

	public ComponentInstanceReqDetails() {
		super();
	}

	public ComponentInstanceReqDetails(ComponentInstance componentInstance) {
		super();
		this.setUniqueId(componentInstance.getUniqueId());
		this.description = componentInstance.getDescription();
		this.posX = componentInstance.getPosX();
		this.posY = componentInstance.getPosY();
		// this.name = "myResourceInstance";
		this.name = componentInstance.getName();
	}

	public ComponentInstanceReqDetails(String resourceUid, String description, String posX, String posY, String name) {
		super();
		this.componentUid = resourceUid;
		this.description = description;
		this.posX = posX;
		this.posY = posY;
		// this.name = "myResourceInstance";
		this.name = name;
	}

	public ComponentInstanceReqDetails(String resourceUid, String description, String posX, String posY) {
		super();
		this.componentUid = resourceUid;
		this.description = description;
		this.posX = posX;
		this.posY = posY;
	}

	public String getComponentUid() {
		return componentUid;
	}

	public void setComponentUid(String resourceUid) {
		this.componentUid = resourceUid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPosX() {
		return posX;
	}

	public void setPosX(String posX) {
		this.posX = posX;
	}

	public String getPosY() {
		return posY;
	}

	public void setPosY(String posY) {
		this.posY = posY;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		return "ResourceInstanceReqDetails [resourceUid=" + componentUid + ", description=" + description + ", posX="
				+ posX + ", posY=" + posY + ", name=" + name + ", uniqueId=" + uniqueId + "]";
	}

}
