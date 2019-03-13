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

/**
 * Enum That Represents possible Artifacts Types.
 *
 */
public enum ArtifactTypeEnum {
	CHEF("CHEF"), PUPPET("PUPPET"), YANG("YANG"), SHELL_SCRIPT("SHELL_SCRIPT"), SHELL("SHELL"), ICON("ICON"), UNKNOWN("UNKNOWN"), HEAT("HEAT"), DG_XML("DG_XML"), MURANO_PKG("MURANO_PKG"), HEAT_ENV("HEAT_ENV"), YANG_XML("YANG_XML"), HEAT_VOL("HEAT_VOL"), 
	HEAT_NET("HEAT_NET"), OTHER("OTHER"), WORKFLOW("WORKFLOW"), NETWORK_CALL_FLOW("NETWORK_CALL_FLOW"), TOSCA_TEMPLATE("TOSCA_TEMPLATE"), TOSCA_CSAR("TOSCA_CSAR"), VNF_CATALOG("VNF_CATALOG"), VF_LICENSE("VF_LICENSE"), BPEL("BPEL"),
	VENDOR_LICENSE("VENDOR_LICENSE"), MODEL_INVENTORY_PROFILE("MODEL_INVENTORY_PROFILE"), MODEL_QUERY_SPEC("MODEL_QUERY_SPEC"), APPC_CONFIG("APPC_CONFIG"), HEAT_NESTED("HEAT_NESTED"), HEAT_ARTIFACT("HEAT_ARTIFACT"), CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT("CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT"),
	VF_MODULES_METADATA("VF_MODULES_METADATA"), LIFECYCLE_OPERATIONS("LIFECYCLE_OPERATIONS"), VES_EVENTS("VES_EVENTS"), PERFORMANCE_COUNTER("PERFORMANCE_COUNTER"),UCPE_LAYER_2_CONFIGURATION("UCPE_LAYER_2_CONFIGURATION"),
	// DCAE Artifacts
	DCAE_TOSCA("DCAE_TOSCA"), DCAE_JSON("DCAE_JSON"), DCAE_POLICY("DCAE_POLICY"), DCAE_DOC("DCAE_DOC"), DCAE_EVENT("DCAE_EVENT"), DCAE_INVENTORY_TOSCA("DCAE_INVENTORY_TOSCA"), DCAE_INVENTORY_JSON("DCAE_INVENTORY_JSON"), 
	DCAE_INVENTORY_POLICY("DCAE_INVENTORY_POLICY"), DCAE_INVENTORY_DOC("DCAE_INVENTORY_DOC"), DCAE_INVENTORY_BLUEPRINT("DCAE_INVENTORY_BLUEPRINT"), DCAE_INVENTORY_EVENT("DCAE_INVENTORY_EVENT"),
	// AAI Artifacts
	AAI_SERVICE_MODEL("AAI_SERVICE_MODEL"), AAI_VF_MODEL("AAI_VF_MODEL"), AAI_VF_MODULE_MODEL("AAI_VF_MODULE_MODEL"), AAI_VF_INSTANCE_MODEL("AAI_VF_INSTANCE_MODEL"),
	// MIB artifacts
	SNMP_POLL ("SNMP_POLL"), SNMP_TRAP("SNMP_TRAP"), GUIDE("GUIDE"),
	PLAN("PLAN"),
	PM_DICTIONARY("PM_DICTIONARY")
	;
	
	ArtifactTypeEnum(String type) {
		this.type = type;
	}

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static ArtifactTypeEnum findType(final String type) {
		for (ArtifactTypeEnum ate : ArtifactTypeEnum.values()) {
			// According to Pavel/Ella
			if (ate.getType().equalsIgnoreCase(type)) {
				return ate;
			}
		}
		return null;
	}

	public static List<String> getAllTypes() {
		List<String> types = new ArrayList<>();
		for (ArtifactTypeEnum ate : ArtifactTypeEnum.values()) {
			types.add(ate.getType());
		}
		return types;
	}
}
