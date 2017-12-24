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

public interface Constants {

	public static final String APPLICATION_NAME = "application-name";
	public static final String APPLICATION_VERSION = "application-version";
	public static final String CONFIG_HOME = "config.home";
	public static final String LOG_HOME = "log.home";
	public static final String YAML_SUFFIX = ".yaml";
	public static final String CONFIGURATION_SOURCE_ATTR = "configuration-source";
	public static final String MDC_APP_NAME = "APP_NAME";
	public static final String CONFIGURATION_MANAGER_ATTR = "configuration-manager";
	public static final String HEALTH_CHECK_SERVICE_ATTR = "healthCheckService";
	public static final String REST_CLIENT_ATTR = "rest-client";
	public static final String ARTIFACT_DAO_ATTR = "artifact-dao";
	public static final String UPLOAD_VALIDATORR_ATTR = "upload-validator";
	public static final String THREAD_EXECUTOR_ATTR = "thread-executor";
	public static final String ERROR_LOG_FORMAT = "EVENT = ARTIFACT_UPLOAD  USER_ID=%s USER_NAME=%s ACCESS_IP=%s ACCESS_TYPE=%s RURL=%s SC=%d";
	public static final String FIRST_NAME_HEADER = "HTTP_CSP_FIRSTNAME";
	public static final String LAST_NAME_HEADER = "HTTP_CSP_LASTNAME";
	public static final String USER_ID_HEADER = "USER_ID";
	public static final String MD5_HEADER = "Content-MD5";
	public static final String CONTENT_LENGTH_HEADER = "Content-Length";
	public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String ORIGIN_HEADER = "HTTP_IV_REMOTE_ADDRESS";
	public static final String ACCESS_HEADER = "HTTP_CSP_WSTYPE";
	public static final String X_ECOMP_REQUEST_ID_HEADER = "X-ECOMP-RequestID";
	public static final String X_ECOMP_INSTANCE_ID_HEADER = "X-ECOMP-InstanceID";
	public static final String X_ECOMP_SERVICE_ID_HEADER = "X-ECOMP-ServiceID";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	public static final String HTTP_IV_USER = "HTTP_IV_USER";
	public static final String A4C_CSAR_CONTEXT = "/rest/csars/";
	public static final String WEB_APPLICATION_CONTEXT_WRAPPER_ATTR = "web-application-context-wrapper";
	public static final String CATALOG_BE = "catalog-be";
	public static final String HTTP_CSP_FIRSTNAME = "HTTP_CSP_FIRSTNAME";
	public static final String HTTP_CSP_LASTNAME = "HTTP_CSP_LASTNAME";
	public static final String HTTP_IV_REMOTE_ADDRESS = "HTTP_IV_REMOTE_ADDRESS";
	public static final String HTTP_CSP_TYPE = "HTTP_CSP_WSTYPE";
	public static final String RESOURCE_SUPPORTED_VERSION = "0.0.1";
	public static final String ARTIFACT_ID_FORMAT = "%s:%s:%s"; // resourceName:resourceVersion:artifactName
	public static final String SERVICE_ARTIFACT_ID_FORMAT = "%s:%s:%s:%s"; // serviceName:serviceVersion:nodeTemplateName:artifactName
	public static final String CONTENT_DISPOSITION = "content-disposition";
	public static final String DOWNLOAD_ARTIFACT_LOGIC_ATTR = "downloadArtifactLogic";
	public static final String ASDC_RELEASE_VERSION_ATTR = "SDC-Version";
	// public static final String AUDITING_MANAGER = "auditingManager";
	// public static final String USER_ADMIN_MANAGER = "userAdminManager";
	public static final String YEAR = "year";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String HOUR = "hour";
	public static final String MINUTE = "minute";
	public static final String NONE = "none";
	public static final String RESOURCE_OPERATION_MANAGER = "resourceOperationManager";
	public static final String PROPERTY_OPERATION_MANAGER = "propertyOperationManager";
	public static final String SERVICE_OPERATION_MANAGER = "serviceOperationManager";
	public static final String EMPTY_STRING = "";
	public static final String NULL_STRING = "null";
	public static final String DOUBLE_NULL_STRING = "null null";
	public static final String ECOMP_ERROR_MNGR_ATTR = "ecompErrorMngrAttr";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String ACCEPT_HEADER = "Accept";
	public static final String STANDARD_INTERFACE_TYPE = "standard";
	public static final String MURANO_PKG_ARTIFACT_TYPE = "MURANO-PKG";
	public static final String ARTIFACT_GROUP_TYPE_FIELD = "artifactGroupType";

	// TOSCA
	public static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";
	public static final String TOSCA_META_ENTRY_DEFINITIONS = "Entry-Definitions";
	public static final String USER_DEFINED_RESOURCE_NAMESPACE_PREFIX = "org.openecomp.resource.";

	public static final String IS_BASE = "isBase";
	public static final String HEAT_FILE_PROPS = "heat_file";
	public static final String MODULE_NAME_FORMAT = "%s..%s..module-%s";
	public static final String MODULE_DESC_PATTERN = "[\\_\\-\\.a-zA-Z0-9]+";
	public static final String MODULE_OLD_NAME_PATTERN = "([\\w\\_\\-\\.\\s]+)(::module-)(\\d+)";
	public static final String MODULE_NEW_NAME_PATTERN = "([\\w\\_\\-\\.\\s]+\\.\\.)([\\_\\-\\.a-zA-Z0-9]+)(..module-)(\\d+)";
	public static final String MODULE_NAME_DELIMITER = "module-";
	public static final String IMPORT_STRUCTURE = "importStructure";
	public static final String DEFAULT_GROUP_VF_MODULE = "org.openecomp.groups.VfModule";

	public static final String ARTIFACT_GROUP_TYPE = "artifactGroupType";
	public static final String ARTIFACT_LABEL = "artifactLabel";
	public static final String ARTIFACT_PAYLOAD_DATA = "payloadData";
	public static final String ARTIFACT_DISPLAY_NAME = "artifactDisplayName";
	public static final String ARTIFACT_DESCRIPTION = "description";
	public static final String ARTIFACT_TYPE = "artifactType";
	public static final String ARTIFACT_NAME = "artifactName";
	public static final String ARTIFACT_ID = "uniqueId";
	public static final String REQUIRED_ARTIFACTS = "requiredArtifacts";
	public static final String ARTIFACT_HEAT_PARAMS = "heatParameters";
	public static final String ARTIFACT_ES_ID = "esId";

	public static final String ABSTRACT = "abstract";
	public static final String GLOBAL_SUBSTITUTION_TYPES_SERVICE_TEMPLATE = "Definitions/GlobalSubstitutionTypesServiceTemplate.yaml";
	public static final String ABSTRACT_SUBSTITUTE_GLOBAL_TYPES_SERVICE_TEMPLATE = "Definitions/AbstractSubstituteGlobalTypesServiceTemplate.yaml";

	public static final String VENDOR_LICENSE_MODEL = "vendor-license-model.xml";
	public static final String VENDOR_LICENSE_LABEL = "vendorlicense";
	public static final String VENDOR_LICENSE_DISPLAY_NAME = "Vendor License";
	public static final String VENDOR_LICENSE_DESCRIPTION = " Vendor license file";

	public static final String VF_LICENSE_MODEL = "vf-license-model.xml";
	public static final String VF_LICENSE_LABEL = "vflicense";
	public static final String VF_LICENSE_DISPLAY_NAME = "VF License";
	public static final String VF_LICENSE_DESCRIPTION = "VF license file";
	public static final String GET_INPUT = "get_input";
	public static final String SERVICE_TEMPLATE_FILE_POSTFIX = "ServiceTemplate.yaml";
	public static final String SERVICE_TEMPLATES_CONTAINING_FOLDER = "Definitions/";
	public static final String UNBOUNDED = "unbounded";
	
	//SDC HealthCheck components
	public static final String HC_COMPONENT_FE = "FE";
	public static final String HC_COMPONENT_BE = "BE";
	public static final String HC_COMPONENT_TITAN = "TITAN";
	public static final String HC_COMPONENT_CASSANDRA = "CASSANDRA";
	public static final String HC_COMPONENT_DISTRIBUTION_ENGINE = "DE";

	//external HealthCheck components
	public static final String HC_COMPONENT_ON_BOARDING = "ON_BOARDING";
	public static final String HC_COMPONENT_DCAE = "DCAE";
	
	//Designer BL
	public static final String DESIGNER_BL_COMPONENT = "designerStatusBL";
}
