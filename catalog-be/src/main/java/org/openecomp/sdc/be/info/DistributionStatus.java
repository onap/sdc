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

package org.openecomp.sdc.be.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DistributionStatus {
	DEPLOYED("Deployed", "DEPLOYED");

	private String name;
	private String auditingStatus;

	private static Logger log = LoggerFactory.getLogger(DistributionStatus.class.getName());

	DistributionStatus(String name, String auditingStatus) {
		this.name = name;
		this.auditingStatus = auditingStatus;
	}

	public String getName() {
		return name;
	}

	public String getAuditingStatus() {
		return auditingStatus;
	}

	public static DistributionStatus getStatusByAuditingStatusName(String auditingStatus) {
		DistributionStatus res = null;
		DistributionStatus[] values = values();
		for (DistributionStatus value : values) {
			if (value.getAuditingStatus().equals(auditingStatus)) {
				res = value;
				break;
			}
		}
		if (res == null) {
			log.debug("No DistributionStatus  is mapped to name {}", auditingStatus);
		}
		return res;
	}

}
