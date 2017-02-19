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

public enum DistributionStatusEnum {
	DISTRIBUTION_NOT_APPROVED("Distribution not approved"), 
	DISTRIBUTION_APPROVED("Distribution approved"), 
	DISTRIBUTED("Distributed"), 
	DISTRIBUTION_REJECTED("Distribution rejected");

	private String value;

	private DistributionStatusEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static DistributionStatusEnum findState(String state) {

		for (DistributionStatusEnum distributionStatus : DistributionStatusEnum.values()) {
			if (distributionStatus.name().equalsIgnoreCase(state)
					|| distributionStatus.getValue().equalsIgnoreCase(state)) {
				return distributionStatus;
			}
		}
		return null;
	}

}
