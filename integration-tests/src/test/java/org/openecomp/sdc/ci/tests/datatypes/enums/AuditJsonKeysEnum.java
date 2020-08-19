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

public enum AuditJsonKeysEnum {

	ACTION("ACTION"), RESOURCE_NAME("RESOURCE_NAME"), RESOURCE_TYPE("RESOURCE_TYPE"), PREV_VERSION("PREV_VERSION"), CURR_VERSION("CURR_VERSION"), PREV_STATE("PREV_STATE"), CURR_STATE("CURR_STATE"), 
	DPREV_STATUS("DPREV_STATUS"), DCURR_STATUS("DCURR_STATUS"), STATUS("STATUS"), DESCRIPTION("DESCRIPTION"), ARTIFACT_DATA("ARTIFACT_DATA"), CONSUMER_ID("CONSUMER_ID"), RESOURCE_URL("RESOURCE_URL"), 
	COMMENT("COMMENT"), DID("DID"), TOPIC_NAME("TOPIC_NAME"), TOSCA_NODE_TYPE("TOSCA_NODE_TYPE"), CURR_ARTIFACT_UUID("CURR_ARTIFACT_UUID"), PREV_ARTIFACT_UUID("PREV_ARTIFACT_UUID"), DETAILS("DETAILS"), 
	MODIFIER("MODIFIER"), SERVICE_INSTANCE_ID("SERVICE_INSTANCE_ID"), INVARIANT_UUID("INVARIANT_UUID");

	private String auditJsonKeyName;

	private AuditJsonKeysEnum(String auditJsonKeyName) {
		this.auditJsonKeyName = auditJsonKeyName;
	}

	public String getAuditJsonKeyName() {
		return auditJsonKeyName.toLowerCase();
	}

}
