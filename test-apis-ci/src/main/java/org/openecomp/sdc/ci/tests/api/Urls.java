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

package org.openecomp.sdc.ci.tests.api;

public interface Urls {

	final String UPLOAD_ZIP_URL = "http://%s:%s/sdc1/rest/v1/catalog/resources";
	final String GET_IMAGE_DATA_FROM_ES = "http://%s:%s/resources/imagedata/_search?q=resourceName:%s&pretty=true&size=1000";
	final String GET_SCRIPT_DATA_FROM_ES = "http://%s:%s/resources/artifactdata/_search?q=resourceName:%s&pretty=true&size=1000";
	final String GET_ID_LIST_BY_INDEX_FROM_ES = "http://%s:%s/%s/%s/_search?fields=_id&size=1000";

	final String ES_URL = "http://%s:%s";
	final String GET_SERVICE_CSAR_API1 = "http://%s:%s/sdc2/rest/services/%s/%s";
	final String GET_SERVICE_CSAR_API2 = "http://%s:%s/sdc2/rest/services/%s/%s/csar";

	final String GET_SERVICE_CSAR_FE_PROXY_API1 = "http://%s:%s/sdc1/portal/rest/services/%s/%s";
	final String GET_CSAR_USING_SIMULATOR = "http://%s:%s/onboardingci/onbrest/onboarding-api/v1.0/vendor-software-products/packages/%s";
	final String COPY_CSAR_USING_SIMULATOR = "http://%s:%s/onboardingci/onbrest/onboarding-api/v1.0/vendor-software-products/packages/%s/%s";

	final String GET_HEALTH_CHECK_VIA_PROXY = "http://%s:%s/sdc1/rest/healthCheck";

	// Get back-end config http://172.20.43.132:8080/sdc2/rest/configmgr/get
	final String GET_CONFIG_MANAGER = "http://%s:%s/sdc2/rest/configmgr/get";

	// Get latest version of all non-abstract resources
	final String GET_RESOURCE_lATEST_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/resources/latestversion/notabstract";

	final String GET_SERVICE_lATEST_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/services/latestversion/notabstract";

	// Get resource artifact list:
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/resources/alien.nodes.Apache/2.0.0-SNAPSHOT/artifacts
	final String GET_RESOURCE_ARTIFACTS_LIST = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/artifacts";

	// get resource artifact metadata (creation, MD5, etc):
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/resources/alien.nodes.Apache/2.0.0-SNAPSHOT/artifacts/install_apache.sh/metadata
	final String GET_RESOURCE_ARTIFACT_METADATA = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/artifacts/%s/metadata";

	// resource artifact payload:
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/resources/alien.nodes.Apache/2.0.0-SNAPSHOT/artifacts/install_apache.sh
	final String GET_RESOURCE_ARTIFACT_PAYLOAD = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/artifacts/%s";

	final String GET_RESOURCE_ARTIFACT_PAYLOAD_FE_PROXY = "http://%s:%s/sdc1/portal/rest/v1/catalog/resources/%s/%s/artifacts/%s";

	// Get service list: http://172.20.43.124:8080/sdc2/rest/v1/catalog/services
	final String GET_SERVICE_LIST = "http://%s:%s/sdc2/rest/v1/catalog/services";

	// Get service versions:
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/services/MyService
	final String GET_SERVICE_VERSIONS = "http://%s:%s/sdc2/rest/v1/catalog/services/%s";

	// Get service artifact list:
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/services/alien.nodes.Apache/0.0.1/artifacts
	final String GET_SERVICE_ARTIFACTS_LIST = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/%s/artifacts";

	// get service artifact metadata (creation, MD5, etc):
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/services/alien.nodes.Apache/0.0.1/artifacts/install_apache.sh/metadata
	final String GET_SERVICE_METADATA = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/%s/artifacts/%s/metadata";

	// service artifact payload:
	// http://172.20.43.124:8080/sdc2/rest/v1/catalog/services/alien.nodes.Apache/0.0.1/artifacts/install_apache.sh
	final String GET_SERVICE_ARTIFACT_PAYLOAD = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/artifacts/%s";

	final String GET_SEARCH_DATA_FROM_ES = "http://%s:%s/%s";

	// ****************************************************USER
	// URLs********************************************************
	final String GET_USER = "http://%s:%s/sdc2/rest/v1/user/%s";

	final String GET_USER_ROLE = "http://%s:%s/sdc2/rest/v1/user/%s/role";

	final String CREATE_USER = "http://%s:%s/sdc2/rest/v1/user";

	final String UPDATE_USER = "http://%s:%s/sdc2/rest/v1/user/%s";

	final String UPDATE_USER_ROLE = "http://%s:%s/sdc2/rest/v1/user/%s/role";

	final String DELETE_USER = "http://%s:%s/sdc2/rest/v1/user/%s";

	final String GET_ALL_ADMIN_USERS = "http://%s:%s/sdc2/rest/v1/user/admins";

	final String AUTHORIZE_USER = "http://%s:%s/sdc2/rest/v1/user/authorize";

	final String GET_ALL_TAGS = "http://%s:%s/sdc2/rest/v1/tags";

	final String AUTH_USER = "http://%s:%s/sdc2/rest/v1/user/authorize";

	final String GET_ALL_NOT_ABSTRACT_RESOURCES = "http://%s:%s/sdc2/rest/v1/catalog/resources/certified/notabstract";

	final String GET_ALL_ABSTRACT_RESOURCES = "http://%s:%s/sdc2/rest/v1/catalog/resources/certified/abstract";

	final String QUERY_NEO4J = "http://%s:%s/db/data/transaction";
	final String CHANGE_IN_NEO4J = "http://%s:%s/db/data/transaction/commit";

	final String GET_ALL_ADMINS = "http://%s:%s/sdc2/rest/v1/user/admins";

	final String GET_USERS_BY_ROLES = "http://%s:%s/sdc2/rest/v1/user/users?roles=%s";

	final String GET_ALL_USERS = "http://%s:%s/sdc2/rest/v1/user/users?roles/";

	// *****************************************ECOMP User
	// URL's*****************************************************
	final String ECOMP_PUSH_USER = "http://%s:%s/api/v2/user";

	final String ECOMP_EDIT_USER = "http://%s:%s/api/v2/user/%s";

	final String ECOMP_GET_USER = "http://%s:%s/api/v2/user/%s";

	final String ECOMP_GET_ALL_USERS = "http://%s:%s/api/v2/users";

	final String ECOMP_GET_ALL_AVAILABLE_ROLES = "http://%s:%s/api/v2/roles";

	final String ECOMP_PUSH_USER_ROLES = "http://%s:%s/api/v2/user/%s/roles";

	final String ECOMP_GET_USER_ROLES = "http://%s:%s/api/v2/user/%s/roles";

	// *****************************************Elements*************************************************************
	final String GET_TAGS_LIST = "http://%s:%s/sdc2/rest/v1/tags";

	final String GET_PROPERTY_SCOPES_LIST = "http://%s:%s/sdc2/rest/v1/propertyScopes";

	final String GET_CONFIGURATION = "http://%s:%s/sdc2/rest/v1/configuration/ui";

	final String GET_ALL_ARTIFACTS = "http://%s:%s/sdc2/rest/v1/artifactTypes";

	final String GET_FOLLWED_LIST = "http://%s:%s/sdc2/rest/v1/followed";

	final String GET_CATALOG_DATA = "http://%s:%s/sdc2/rest/v1/screen/?excludeTypes=VFCMT&excludeTypes=Configuration";

	// *****************************************Resources
	// **********************************************************************
	final String GET_LIST_CERTIFIED_RESOURCE_TEMPLATES = "http://%s:%s/sdc2/rest/v1/resoourceTemplates";

	final String CREATE_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources";
	final String UPDATE_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s";

	final String IMPORT_RESOURCE_NORMATIVE = "http://%s:%s/sdc2/rest/v1/catalog/upload/multipart";

	final String IMPORT_USER_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/upload/user-resource";

	final String IMPORT_CAPABILITY_TYPE = "http://%s:%s/sdc2/rest/v1/catalog/uploadType/capability";
	final String IMPORT_CATEGORIES = "http://%s:%s/sdc2/rest/v1/catalog/uploadType/categories";
	final String IMPORT_GROUP_TYPE = "http://%s:%s/sdc2/rest/v1/catalog/uploadType/grouptypes";

	// last %s is resourceId, resourceId = resourceName.resourceVersion
	final String GET_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s";
	final String GET_RESOURCE_BY_NAME_AND_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/resources/resourceName/%s/resourceVersion/%s";
	final String GET_RESOURCE_BY_CSAR_UUID = "http://%s:%s/sdc2/rest/v1/catalog/resources/csar/%s";
	final String GET_COMPONENT_REQUIRMENTS_CAPABILITIES = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/requirmentsCapabilities";

	final String DELETE_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s";
	final String DELETE_RESOURCE_BY_NAME_AND_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s";
	final String DELETE_SERVICE_BY_NAME_AND_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/%s";

	final String DELETE_MARKED_RESOURCES = "http://%s:%s/sdc2/rest/v1/inactiveComponents/resource";
	final String DELETE_MARKED_SERVICES = "http://%s:%s/sdc2/rest/v1/inactiveComponents/service";

	final String GET_FOLLOWED_RESOURCES = "http://%s:%s/sdc2/rest/v1/followed/resources/%s";
	final String CHANGE_RESOURCE_LIFECYCLE_STATE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/lifecycleState/%s";
	final String CHANGE_SERVICE_LIFECYCLE_STATE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/lifecycleState/%s";
	final String CHANGE_PRODUCT_LIFECYCLE_STATE = "http://%s:%s/sdc2/rest/v1/catalog/products/%s/lifecycleState/%s";
	final String CHANGE_COMPONENT_LIFECYCLE_STATE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/lifecycleState/%s";

	final String CREATE_PROPERTY = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/properties";
	final String DECLARE_PROPERTIES  = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/create/inputs";

	final String UPDATE_RESOURCE_METADATA = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/metadata";

	// ***********************************External API's
	// (AssetData)****************************************
	
	final String DELETE_EXTRNAL_API_DELETE_ARTIFACT_OF_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/artifacts/%s";
	final String DELETE_EXTRNAL_API_DELETE_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts/%s";

	final String POST_EXTERNAL_API_UPDATE_ARTIFACT_OF_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/artifacts/%s";
	final String POST_EXTERNAL_API_UPDATE_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts/%s";
	
	final String POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/artifacts";
	final String POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET  = "http://%s:%s/sdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts";

	final String GET_DOWNLOAD_RESOURCE_ARTIFACT_OF_ASSET = "http://%s:%s/sdc/v1/catalog/resources/%s/artifacts/%s";
	final String GET_DOWNLOAD_SERVICE_ARTIFACT_OF_ASSET = "http://%s:%s/sdc/v1/catalog/services/%s/artifacts/%s";

	final String GET_DOWNLOAD_RESOURCE_ARTIFACT_OF_COMPONENT_INSTANCE = "http://%s:%s/sdc/v1/catalog/resources/%s/resourceInstances/%s/artifacts/%s";
	final String GET_DOWNLOAD_SERVICE_ARTIFACT_OF_COMPONENT_INSTANCE = "http://%s:%s/sdc/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s";

	final String GET_ASSET_LIST = "http://%s:%s/sdc/v1/catalog/%s";
	final String GET_FILTERED_ASSET_LIST = "http://%s:%s/sdc/v1/catalog/%s?%s";
	final String GET_TOSCA_MODEL = "http://%s:%s/sdc/v1/catalog/%s/%s/toscaModel";
	// https://{serverRoot}/sdc/v1/catalog/{assetType}/{uuid}/metadata, where
	// assetType in {resources, services}
	final String GET_ASSET_METADATA = "http://%s:%s/sdc/v1/catalog/%s/%s/metadata";
	final String POST_AUTHORIZATION = "http://%s:%s/sdc2/rest/v1/consumers";
	final String GET_DOWNLOAD_SERVICE_RI_ARTIFACT = "http://%s:%s/sdc/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s";
	final String GET_DOWNLOAD_SERVICE_ARTIFACT = "http://%s:%s/sdc/v1/catalog/services/%s/artifacts/%s";
	
	final String POST_EXTERNAL_API_CREATE_RESOURCE = "http://%s:%s/sdc/v1/catalog/resources";
	
	// Change LifeCycle of Resource
	// https://{serverRoot}:{port}/sdc/v1/catalog/{resources|services}/{uuid}/lifecycleState/{lifecycle state}
	final String POST_EXTERNAL_API_CHANGE_LIFE_CYCLE_OF_ASSET = "http://%s:%s/sdc/v1/catalog/%s/%s/lifecycleState/%s";
	

	// *****************************************************************************************************

	final String ADD_ARTIFACT_TO_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/artifacts";
	final String UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/artifacts/%s";
	final String ADD_ARTIFACT_TO_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/artifacts";
	final String UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/artifacts/%s";

	final String UPLOAD_DELETE_ARTIFACT_OF_COMPONENT = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/artifacts";
	final String UPDATE_ARTIFACT_OF_COMPONENT = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/artifacts/%s";
	final String UPLOAD_HEAT_ENV_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s/artifacts/%s";
	// *****************************************************************************************************
	final String UPLOAD_ARTIFACT_BY_INTERFACE_TO_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/%s/artifacts/";
	final String UPDATE_OR_DELETE_ARTIFACT_BY_INTERFACE_TO_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/%s/artifacts/%s";

	final String UPLOAD_ARTIFACT_BY_INTERFACE_TO_COMPONENT = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/%s/artifacts/";
	final String UPDATE_OR_DELETE_ARTIFACT_BY_INTERFACE_TO_COMPONENT = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/%s/%s/artifacts/%s";

	// *****************************************************************************************************
	// "/sdc2/v1/services/<serviceName>/<0.1>/artifacts/aaa.hh"
	final String DISTRIB_DOWNLOAD_SERVICE_ARTIFACT = "/sdc2/rest/v1/catalog/services/%s/%s/artifacts/%s";
	// "/sdc2/v1/services/<serviceName>/<0.1>/resources/{resourceName}/{resourceVersion}/artifacts/<opeartion_name>_aaa.hh"
	final String DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT = "/sdc2/rest/v1/catalog/services/%s/%s/resources/%s/%s/artifacts/%s";
	final String DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL = "/sdc/v1/catalog/services/%s/%s/artifacts/%s";
	final String DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL = "/sdc/v1/catalog/services/%s/%s/resources/%s/%s/artifacts/%s";
	final String DOWNLOAD_SERVICE_ARTIFACT_FULL_URL = "http://%s:%s%s";
	final String DOWNLOAD_RESOURCE_ARTIFACT_FULL_URL = "http://%s:%s%s";
	// **********************************************************************************
	final String UI_DOWNLOAD_RESOURCE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/artifacts/%s";
	final String UI_DOWNLOAD_SERVICE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/artifacts/%s";

	// **********************************************************************************************************
	final String UPDATE_PROPERTY = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/properties/%s";

	final String DELETE_PROPERTY = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/properties/%s";

	final String GET_PROPERTY = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/properties/%s";

	// *****************************************************************************************************

	final String VALIDATE_RESOURCE_NAME = "http://%s:%s/sdc2/rest/v1/catalog/resources/validate-name/%s";

	final String CREATE_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services";
	final String DELETE_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s";
	final String GET_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s";
	final String GET_SERVICE_BY_NAME_AND_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/services/serviceName/%s/serviceVersion/%s";

	final String GET_SERVICES_REQUIRMENTS_CAPABILITIES = "http://%s:%s/sdc2/rest/v1/catalog/requirmentsCapabilities/services/%s";

	final String CREATE_COMPONENT_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance";
	final String DELETE_COMPONENT_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s";
	final String UPDATE_COMPONENT_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s";
	final String GET_COMPONENT_INSTANCES = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/componentInstances";
	// Tal New API
	final String UPDATE_MULTIPLE_COMPONENT_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/multipleComponentInstance";

	final String CHANGE__RESOURCE_INSTANCE_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s/changeVersion";

	final String CREATE_AND_ASSOCIATE_RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/createAndAssociate";
	final String ASSOCIATE__RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/associate";
	final String DISSOCIATE__RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/dissociate";

	final String DISTRIBUTION_INIT = "http://%s:%s/init";
	final String DISTRIBUTION_INIT_RESET = "http://%s:%s/initReset";
	final String APPROVE_DISTRIBUTION = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/distribution-state/approve";
	final String REJECT_DISTRIBUTION = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/distribution-state/reject";
	final String DISTRIBUTION_DOWNLOAD_ARTIFACT = "http://%s:%s/download";
	final String ACTIVATE_DISTRIBUTION = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/distribution/%s/activate";
	final String DISTRIBUTION_SERVICE_LIST = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/distribution";
	final String DISTRIBUTION_SERVICE_MONITOR = "http://%s:%s/sdc2/rest/v1/catalog/services/distribution/%s";

	final String DEPLOY_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/distribution/%s/markDeployed";
	final String UPDATE_SERVICE_METADATA = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/metadata";

	// Andrey changed name from ADD_PROPERTY_TO_RESOURCE_INSTANCE to
	// UPDATE_PROPERTY_TO_RESOURCE_INSTANCE
	final String UPDATE_PROPERTY_TO_RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s/property";
	final String DELETE_PROPERTY_FROM_RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/%s/property/%s";
	final String UPDATE_RESOURCE_INSTANCE_HEAT_ENV_PARAMS = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/%s/artifacts/%s/heatParams";

	// Actions on artifact in resource instance
	final String ADD_RESOURCE_INSTANCE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/%s/artifacts";
	final String UPDATE_RESOURCE_INSTANCE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/%s/artifacts/%s";
	final String DELETE_RESOURCE_INSTANCE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstance/%s/artifacts/%s";

	// Attributes On Resource instance
	public static final String UPDATE_ATTRIBUTE_ON_RESOURCE_INSTANCE = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/resourceInstance/%s/attribute";

	// ("/services/{serviceId}/resourceInstances/{resourceInstanceId}/artifacts/{artifactId}")
	final String DOWNLOAD_COMPONENT_INSTANCE_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s";

	// -------------------------------service api
	// artifact-----------------------------------------------------
	final String UPDATE_DELETE_SERVICE_API_ARTIFACT = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/artifacts/api/%s";

	final String CREATE_ADDITIONAL_INFORMATION_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/additionalinfo";
	final String UPDATE_ADDITIONAL_INFORMATION_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/additionalinfo/%s";
	final String DELETE_ADDITIONAL_INFORMATION_RESOURCE = UPDATE_ADDITIONAL_INFORMATION_RESOURCE;
	final String GET_ADDITIONAL_INFORMATION_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/additionalinfo/%s";
	final String GET_ALL_ADDITIONAL_INFORMATION_RESOURCE = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/additionalinfo";

	final String CREATE_ADDITIONAL_INFORMATION_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/additionalinfo";
	final String UPDATE_ADDITIONAL_INFORMATION_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/additionalinfo/%s";
	final String DELETE_ADDITIONAL_INFORMATION_SERVICE = UPDATE_ADDITIONAL_INFORMATION_SERVICE;
	final String GET_ADDITIONAL_INFORMATION_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/additionalinfo/%s";
	final String GET_ALL_ADDITIONAL_INFORMATION_SERVICE = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/additionalinfo";

	final String GET_COMPONENT_AUDIT_RECORDS = "http://%s:%s/sdc2/rest/v1/catalog/audit-records/%s/%s";

	// CONSUMER
	final String CREATE_CONSUMER = "http://%s:%s/sdc2/rest/v1/consumers";
	final String GET_CONSUMER = "http://%s:%s/sdc2/rest/v1/consumers/%s";
	final String DELETE_CONSUMER = "http://%s:%s/sdc2/rest/v1/consumers/%s";

	// Categories
	final String CREATE_CATEGORY = "http://%s:%s/sdc2/rest/v1/category/%s";
	final String GET_ALL_CATEGORIES = "http://%s:%s/sdc2/rest/v1/categories/%s";
	final String GET_ALL_CATEGORIES_FE = "http://%s:%s/sdc1/feProxy/rest/v1/categories/%s";
	final String DELETE_CATEGORY = "http://%s:%s/sdc2/rest/v1/category/%s/%s";
	final String CREATE_SUB_CATEGORY = "http://%s:%s/sdc2/rest/v1/category/%s/%s/subCategory";
	final String DELETE_SUB_CATEGORY = "http://%s:%s/sdc2/rest/v1/category/%s/%s/subCategory/%s";
	final String CREATE_GROUPING = "http://%s:%s/sdc2/rest/v1/category/%s/%s/subCategory/%s/grouping";
	final String DELETE_GROUPING = "http://%s:%s/sdc2/rest/v1/category/%s/%s/subCategory/%s/grouping/%s";

	// product
	final String CREATE_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products";
	final String DELETE_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products/%s";
	// last %s is resourceId, productId
	final String GET_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products/%s";
	final String UPDATE_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products/%s/metadata";
	final String GET_PRODUCT_BY_NAME_AND_VERSION = "http://%s:%s/sdc2/rest/v1/catalog/products/productName/%s/productVersion/%s";

	// groups
	final String GET_GROUP_BY_ID = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/groups/%s";

	// modules
	final String GET_MODULE_BY_ID = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/groups/%s";
	
	// inputs
	final String ADD_INPUTS = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/create/inputs"; //{componentType}/{componentId}/create/inputs
	final String DELETE_INPUT_BY_ID = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/delete/%s/input"; //{componentType}/{componentId}/delete/{inputId}/input
	final String GET_COMPONENT_INPUTS = "http://%s:%s/sdc2/rest/v1/catalog/services/%s/inputs"; //services/{componentId}/inputs
	final String GET_COMPONENT_INSTANCE_INPUTS = "http://%s:%s/sdc2/rest/v1/catalog/%s/%s/componentInstances/%s/%s/inputs"; //{componentType}/{componentId}/componentInstances/{instanceId}/{originComonentUid}/inputs
	final String GET_INPUTS_FOR_COMPONENT_INPUT = "http://%s:%s/sdc2/rest/v1/catalog/resources/%s/groups/%s"; //{componentType}/{componentId}/inputs/{inputId}/inputs

	// check version
	final String ONBOARD_VERSION = "http://%s:%s/onboarding-api/docs/build-info.json";
	final String OS_VERSION = "http://%s:%s/sdc2/rest/version";
	
//	amdocs APIs
	final String GET_VENDOR_SOFTWARE_PRODUCT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/packages/%s";
	final String UPLOAD_SNMP_POLL_ARTIFACT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/uploads/types/SNMP_POLL";
	final String UPLOAD_SNMP_TRAP_ARTIFACT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/uploads/types/SNMP_TRAP";
	final String UPLOAD_VES_EVENTS_ARTIFACT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/uploads/types/VES_EVENTS";
	final String UPLOAD_AMDOCS_ARTIFACT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/uploads/types/%s";
	final String DELETE_AMDOCS_ARTIFACT_BY_TYPE = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components/%s/monitors/%s";
	final String GET_VSP_COMPONENTS = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/components";
	final String CREATE_VENDOR_LISENCE_MODELS = "http://%s:%s/onboarding-api/v1.0/vendor-license-models";
	final String CREATE_VENDOR_LISENCE_AGREEMENT = "http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/license-agreements";
	final String CREATE_VENDOR_LISENCE_FEATURE_GROUPS = "http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/feature-groups";
	final String CREATE_VENDOR_LISENCE_ENTITLEMENT_POOL = "http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/entitlement-pools";
	final String CREATE_VENDOR_LISENCE_KEY_GROUPS = "http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/0.1/license-key-groups";
	final String CREATE_VENDOR_SOFTWARE_PRODUCT = "http://%s:%s/onboarding-api/v1.0/vendor-software-products";
	final String VALIDATE_UPLOAD = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/0.1/orchestration-template-candidate/process";
	final String UPLOAD_HEAT_PACKAGE = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/%s/orchestration-template-candidate";
	final String ACTION_ON_COMPONENT = "http://%s:%s/onboarding-api/v1.0/%s/%s/versions/%s/actions";
	final String UPDATE_VSP = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/%s";
	final String GET_VSP_COMPONENT_BY_VERSION = "http://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/%s";
	final String GET_VLM_COMPONENT_BY_VERSION = "http://%s:%s/onboarding-api/v1.0/vendor-license-models/%s/versions/%s";
}
