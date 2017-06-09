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

package org.openecomp.sdc.common.api;

import java.util.ArrayList;
import java.util.List;

public enum ArtifactGroupTypeEnum {

	INFORMATIONAL("INFORMATIONAL"), DEPLOYMENT("DEPLOYMENT"), LIFE_CYCLE("LIFE_CYCLE"), SERVICE_API("SERVICE_API"), TOSCA("TOSCA"), OTHER("OTHER");

	private String type;

	ArtifactGroupTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static ArtifactGroupTypeEnum findType(final String type) {
		for (ArtifactGroupTypeEnum ate : ArtifactGroupTypeEnum.values()) {
			if (ate.getType().equals(type)) {
				return ate;
			}
		}
		return null;
	}

	public static List<String> getAllTypes() {
		List<String> types = new ArrayList<>();
		for (ArtifactGroupTypeEnum ate : ArtifactGroupTypeEnum.values()) {
			types.add(ate.getType());
		}
		return types;
	}

}
