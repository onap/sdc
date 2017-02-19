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

import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.be.dao.api.ActionStatus;

public enum ImportTestTypesEnum {

	MISSING_CONTACT("tosca.nodes.missing_contact", "missing_contact", ActionStatus.COMPONENT_MISSING_CONTACT, Arrays.asList("Resource"), true), 
	MISSING_RESOURCE_NAME("tosca.nodes.missing_resource_name", "missing_resource_name", ActionStatus.MISSING_COMPONENT_NAME, Arrays.asList("Resource"), true), 
	MISSING_DESC("tosca.nodes.missing_desc", "missing_desc", ActionStatus.COMPONENT_MISSING_DESCRIPTION, Arrays.asList("Resource"), true), 
	MISSING_ICON("tosca.nodes.missing_icon", "missing_icon", ActionStatus.COMPONENT_MISSING_ICON, Arrays.asList("Resource"), true), 
	MISSING_TAGS("tosca.nodes.missing_tags", "missing_tags", ActionStatus.COMPONENT_MISSING_TAGS, null, true), 
	MISSING_CATEGORY("tosca.nodes.missing_category", "missing_category", ActionStatus.COMPONENT_MISSING_CATEGORY, Arrays.asList("Resource"), true),
	// MISSING_PAYLOADNAME("tosca.nodes.missing_payloadName",
	// "missing_payloadName", ActionStatus.INVALID_TOSCA_FILE_EXTENSION, null,
	// true),

	EMPTY_RESOURCE_NAME("tosca.nodes.empty_resource_name", "missing_resource_name"/* "empty_resource_name" */, ActionStatus.MISSING_COMPONENT_NAME, Arrays.asList("Resource"), false), 
	EMPTY_CONTACT("tosca.nodes.empty_contact", "missing_contact"/* "empty_contact" */, ActionStatus.COMPONENT_MISSING_CONTACT, Arrays.asList("Resource"), false), 
	EMPTY_CATEGORY("tosca.nodes.empty_category", "missing_category"/* "empty_category" */, ActionStatus.COMPONENT_MISSING_CATEGORY, Arrays.asList("Resource"), false), 
	EMPTY_DESC("tosca.nodes.empty_desc", "missing_desc"/* "empty_desc" */, ActionStatus.COMPONENT_MISSING_DESCRIPTION, Arrays.asList("Resource"), false), 
	EMPTY_ICON("tosca.nodes.empty_icon", "missing_icon"/* "empty_icon" */, ActionStatus.COMPONENT_MISSING_ICON, Arrays.asList("Resource"), false), 
	EMPTY_PAYLOADNAME("tosca.nodes.empty_payloadName", "missing_payloadName"/* "empty_payloadName" */, ActionStatus.INVALID_TOSCA_FILE_EXTENSION, null, false),
	EMPTY_TAG("tosca.nodes.empty_tag", "empty_tag", ActionStatus.INVALID_FIELD_FORMAT, Arrays.asList("Resource", "tag"), false), 
	VALIDATE_PROPORTIES_1("tosca.nodes.validateProporties_typeBoolean_valueInit", "validateProporties_typeBoolean_valueInit", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "boolean", "123456"), false), 
	VALIDATE_PROPORTIES_2("tosca.nodes.validateProporties_typeBoolean_valueString", "validateProporties_typeBoolean_valueString", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "boolean", "abcd"), false), 
	VALIDATE_PROPORTIES_3("tosca.nodes.validateProporties_typeFloat_valueBoolean", "validateProporties_typeFloat_valueBoolean", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "float", "true"), false), 
	VALIDATE_PROPORTIES_4("tosca.nodes.validateProporties_typeFloat_valueString", "validateProporties_typeFloat_valueString", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "float", "abcd"), false), 
	VALIDATE_PROPORTIES_5("tosca.nodes.validateProporties_typeInit_valueBoolean", "validateProporties_typeInit_valueBoolean", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "integer", "true"), false), 
	VALIDATE_PROPORTIES_6("tosca.nodes.validateProporties_typeInit_valueFloat", "validateProporties_typeInit_valueFloat", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "integer", "0.123"), false), 
	VALIDATE_PROPORTIES_7("tosca.nodes.validateProporties_typeInit_valueString", "validateProporties_typeInit_valueString", ActionStatus.INVALID_DEFAULT_VALUE, Arrays.asList("validation_test", "integer", "abcd"), false);
	// VALIDATE_PROPORTIES_8("tosca.nodes.validateProporties_happyScenarios","validateProporties_happyScenarios", ActionStatus.OK, null, false);

	private String normativeName;
	private String folderName;
	private ActionStatus actionStatus;
	private Boolean validateAudit;
	private List<String> errorParams;
	private Boolean validateYaml;

	// private enum ActionStatus;

	private ImportTestTypesEnum(String resourceName, String folderName, ActionStatus actionStatus,
			List<String> errorParams, Boolean validateAudit) {
		this.normativeName = resourceName;
		this.folderName = folderName;
		this.actionStatus = actionStatus;
		this.errorParams = errorParams;
		this.validateAudit = validateAudit;

	}

	public String getNormativeName() {
		return normativeName;
	}

	public String getFolderName() {
		return folderName;
	}

	public ActionStatus getActionStatus() {
		return actionStatus;
	}

	public Boolean getvalidateAudit() {
		return validateAudit;
	}

	public List<String> getErrorParams() {
		return errorParams;
	}

}
