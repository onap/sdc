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

package org.onap.sdc.backend.ci.tests.tosca.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yaml.snakeyaml.TypeDescription;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToscaMetadataDefinition {

	private String invariantUUID;
	private String UUID;
	private String name;
	private String description;
	private String type;
	private String category;
	private String subcategory;
	private String resourceVendor;
	private String resourceVendorRelease;
	private String resourceVendorModelNumber;
	private String serviceType;
	private String serviceRole;
	private String serviceEcompNaming;
	private String ecompGeneratedNaming;
	private String namingPolicy;

	@Override
	public String toString() {
		return "ToscaMetadataDefinition [invariantUUID=" + invariantUUID + ", UUID=" + UUID + ", name=" + name + ", description=" + description + ", type=" + type + ", category=" + category + ", subcategory=" + subcategory
				+ ", resourceVendor=" + resourceVendor + ", resourceVendorRelease=" + resourceVendorRelease + ", resourceVendorModelNumber=" + resourceVendorModelNumber + ", serviceType=" + serviceType + ", serviceRole=" + serviceRole
				+ ", serviceEcompNaming=" + serviceEcompNaming + ", ecompGeneratedNaming=" + ecompGeneratedNaming + ", namingPolicy=" + namingPolicy + "]";
	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaMetadataDefinition.class);
    	return typeDescription;
	}

}
