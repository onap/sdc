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

package org.openecomp.sdc.be.datatypes.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FilterKeyEnum {
	SUB_CATEGORY("subCategory"), CATEGORY("category"), DISTRIBUTION_STATUS("distributionStatus");

	private String name;

	FilterKeyEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static List<String> getAllFilters() {
		return Arrays.stream(FilterKeyEnum.values()).map(f -> f.getName()).collect(Collectors.toList());
	}

	public static List<String> getValidFiltersByAssetType(ComponentTypeEnum assetType) {
		switch (assetType) {
		case RESOURCE:
			return getAllFilters().subList(0, 2);
		case SERVICE:
			return getAllFilters().subList(1, 3);
		default:
			return null;
		}

	}
}
