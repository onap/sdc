/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;


public class AuditTestUtils {
    public final static String USER_FIRST_NAME = "Carlos";
    public final static String USER_LAST_NAME = "Santana";
    public final static String USER_ID = "cs0008";
    public final static String DESCRIPTION = "OK";
    public final static String STATUS_OK = "200";
    public final static String STATUS_CREATED = "201";
    public final static String REQUEST_ID = "123456";
    public final static String USER_UID = "Carlos Santana(cs0008)";
    public final static String SERVICE_INSTANCE_ID = "d07fdc15-122d-4476-a349-8c9a2d80b485";
    public final static String CURRENT_STATE = "CERTIFIED";
    public final static String CURRENT_VERSION = "1.1";
    public final static String RESOURCE_TYPE = ResourceTypeEnum.VF.name();
    public final static String RESOURCE_TYPE_VFC = ResourceTypeEnum.VFC.name();
    public final static String RESOURCE_NAME = "ciServicea184822c06e6";
    public final static String DIST_ID = "e5765a82-e7cd-4c5c-91a0-eae58d6ae08f";
    public final static String TOPIC_NAME = "ASDC-DISTR-NOTIF-TOPIC-PROD_TEST";
    public final static String DESIGNER_USER_ROLE = "DESIGNER";
    public final static String TESTER_USER_ROLE = "TESTER";
    public final static String USER_EMAIL = "carlos@email.com";
    public final static String MODIFIER_FIRST_NAME = "Jimmy";
    public final static String MODIFIER_LAST_NAME = "Hendrix";
    public final static String MODIFIER_ID = "jh0003";
    public final static String MODIFIER_UID = "Jimmy Hendrix(jh0003)";
    public final static String USER_EXTENDED_NAME = "cs0008, Carlos Santana, carlos@email.com, DESIGNER";
    public final static String UPDATED_USER_EXTENDED_NAME = "cs0008, Carlos Santana, carlos@email.com, TESTER";
    public final static String VNF_WORKLOAD_CONTEXT = "WORKLOAD";
    public final static String DPREV_STATUS = "DPREV_STATUS";
    public final static String DCURR_STATUS = "DCURR_STATUS";

    final static String CONSUMER_NAME = "consumer";
    final static String CONSUMER_SALT = "2a1f887d607d4515d4066fe0f5452a50";
    final static String CONSUMER_PASSWORD = "0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b";

    public final static String PREV_RESOURCE_VERSION = "1.0";
    public final static String PREV_RESOURCE_STATE = "READY_FOR_CERTIFICATION";
    public final static String COMMENT = "Attempt to perform";
    public final static String ARTIFACT_DATA = "123456qwertasdfgljkPIPIPIOPI";
    public final static String TOSCA_NODE_TYPE = "tosca.node.Root";
    public final static String ARTIFACT_UUID = "1234-ASDFG_7894443";
    public final static String INVARIANT_UUID = "INV-123456";
    public final static String USER_DETAILS = "All";

    public final static String STATUS_500 = "500";
    public final static String DESC_ERROR = "Error";

    public final static String DIST_CONSUMER_ID = "ABC-123445678";
    public final static String DIST_RESOURCE_URL = "http://abc.com/res";
    public final static String DIST_STATUS_TOPIC = "STopic";
    public final static String DIST_NOTIFY_TOPIC = "NTopic";
    public final static String DIST_API_KEY = "Key111";
    public final static String DIST_ENV_NAME = "Env111";
    public final static String DIST_ROLE = "Governer";
    public final static String DIST_STATUS_TIME = "154567890123";

    public final static String AUTH_URL = "http://abc.com/auth";
    public final static String REALM = "12345ABSDF";
    public final static String AUTH_STATUS = "AUTHENTICATED";

    public final static String CATEGORY = "VFs";
    public final static String SUB_CATEGORY = "Network";
    public final static String GROUPING_NAME = "Group1";

    public final static String OP_ENV_ID = "12345678";
    public final static String OP_ENV_NAME = "Op1";
    public final static String OP_ENV_TYPE = "ECOMP";
    public final static String OP_ENV_ACTION = "Create";
    public final static String TENANT_CONTEXT = "TENANT";

    public final static String EXPECTED_USER_ACCESS_LOG_STR = "ACTION = \"" + AuditingActionEnum.USER_ACCESS.getName() + "\" USER = \"" + USER_UID +
            "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DISTRIB_NOTIFICATION_LOG_STR = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_NOTIFY.getName() +
            "\" RESOURCE_NAME = \"" + RESOURCE_NAME + "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" SERVICE_INSTANCE_ID = \"" +
            SERVICE_INSTANCE_ID + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + USER_UID + "\" CURR_STATE = \"" + CURRENT_STATE +
            "\" DID = \"" + DIST_ID + "\" TOPIC_NAME = \"" + TOPIC_NAME + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION +
            "\" TENANT = \"" + TENANT_CONTEXT + "\" VNF_WORKLOAD_CONTEXT = \"" + VNF_WORKLOAD_CONTEXT  + "\" ENV_ID = \"" + OP_ENV_ID + "\"";

    public final static String EXPECTED_ADD_USER_LOG_STR = "ACTION = \"" + AuditingActionEnum.ADD_USER.getName() + "\" MODIFIER = \"" + MODIFIER_UID +
            "\" USER_BEFORE = \"\" USER_AFTER = \"" + USER_EXTENDED_NAME + "\" STATUS = \"" + STATUS_CREATED + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_UPDATE_USER_LOG_STR = "ACTION = \"" + AuditingActionEnum.UPDATE_USER.getName() + "\" MODIFIER = \"" + MODIFIER_UID +
            "\" USER_BEFORE = \"" + USER_EXTENDED_NAME + "\" USER_AFTER = \"" + UPDATED_USER_EXTENDED_NAME + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DELETE_USER_LOG_STR = "ACTION = \"" + AuditingActionEnum.DELETE_USER.getName() + "\" MODIFIER = \"" + MODIFIER_UID +
            "\" USER_BEFORE = \"" + USER_EXTENDED_NAME + "\" USER_AFTER = \"\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_CHECK_IN_RESOURCE_LOG_STR = "ACTION = \"" + AuditingActionEnum.CHECKIN_RESOURCE.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\"" + " INVARIANT_UUID = \"" + INVARIANT_UUID + "\"" +
            " PREV_VERSION = \"" + PREV_RESOURCE_VERSION + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_STATE = \"" +
            PREV_RESOURCE_STATE + "\" CURR_STATE = \"" + CURRENT_STATE + "\" COMMENT = \"" + COMMENT + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_CREATE_RESOURCE_LOG_STR = "ACTION = \"" + AuditingActionEnum.CREATE_RESOURCE.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\"" + " INVARIANT_UUID = \"" + INVARIANT_UUID + "\"" +
            " PREV_VERSION = \"" + PREV_RESOURCE_VERSION + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_STATE = \"" +
            PREV_RESOURCE_STATE + "\" CURR_STATE = \"" + CURRENT_STATE + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_IMPORT_RESOURCE_LOG_STR = "ACTION = \"" + AuditingActionEnum.IMPORT_RESOURCE.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\"" + " INVARIANT_UUID = \"" + INVARIANT_UUID + "\"" +
            " PREV_VERSION = \"" + PREV_RESOURCE_VERSION + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_STATE = \"" +
            PREV_RESOURCE_STATE + "\" CURR_STATE = \"" + CURRENT_STATE + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\" TOSCA_NODE_TYPE = \""
            + TOSCA_NODE_TYPE + "\"";

    public final static String EXPECTED_ARTIFACT_UPLOAD_LOG_STR = "ACTION = \"" + AuditingActionEnum.ARTIFACT_UPLOAD.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\"" + " INVARIANT_UUID = \"" + INVARIANT_UUID + "\"" +
            " PREV_VERSION = \"" + PREV_RESOURCE_VERSION + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_STATE = \"" +
            PREV_RESOURCE_STATE + "\" CURR_STATE = \"" + CURRENT_STATE + "\" PREV_ARTIFACT_UUID = \"" + ARTIFACT_UUID + "\" CURR_ARTIFACT_UUID = \"" +
            ARTIFACT_UUID + "\" ARTIFACT_DATA = \"" + ARTIFACT_DATA + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DIST_STATE_CHANGE_REQUEST = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" +
            MODIFIER_UID + "\" CURR_STATE = \"" + CURRENT_STATE + "\" DPREV_STATUS = \"" + DPREV_STATUS + "\" DCURR_STATUS = \"" +
            DCURR_STATUS + "\" DID = \"" + DIST_ID + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DIST_STATE_CHANGE_APPROV = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_APPROV.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE_VFC + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" +
            MODIFIER_UID + "\" CURR_STATE = \"" + CURRENT_STATE + "\" DPREV_STATUS = \"" + DPREV_STATUS + "\" DCURR_STATUS = \"" +
            DCURR_STATUS + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\" COMMENT = \"" + COMMENT + "\"";

    public final static String EXPECTED_GET_USER_LIST_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_USERS_LIST.getName() + "\" MODIFIER = \"" + USER_UID +
            "\" DETAILS = \"" + USER_DETAILS + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_ACTIVATE_SERVICE_API_LOG_STR = "ACTION = \"" + AuditingActionEnum.ACTIVATE_SERVICE_BY_API.getName() + "\" RESOURCE_TYPE = \"" +
            RESOURCE_TYPE + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" MODIFIER = \"" +
            MODIFIER_UID + "\" STATUS = \"" + STATUS_OK + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" INVARIANT_UUID = \"" + INVARIANT_UUID +
            "\" DESC = \"" + DESCRIPTION + "\"";

    //TODO: remove with the old API and tests
    public final static String EXPECTED_DOWNLOAD_ARTIFACT_EXTERNAL_API_LOG_STR = "ACTION = \"" + AuditingActionEnum.DOWNLOAD_ARTIFACT.getName() + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" +
            DIST_RESOURCE_URL + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_CHANGE_LIFECYCLE_EXTERNAL_API_LOG_STR = "ACTION = \"" + AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL +
            "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_VERSION = \"" + PREV_RESOURCE_VERSION + "\" CURR_VERSION = \"" + CURRENT_VERSION +
            "\" PREV_STATE = \"" + PREV_RESOURCE_STATE + "\" CURR_STATE = \"" + CURRENT_STATE + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID +
            "\" INVARIANT_UUID = \"" + INVARIANT_UUID + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DELETE_ARTIFACT_EXTERNAL_API_LOG_STR = "ACTION = \"" + AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName() + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL +
            "\" MODIFIER = \"" + MODIFIER_UID + "\" PREV_ARTIFACT_UUID = \"" + ARTIFACT_UUID + "\" CURR_ARTIFACT_UUID = \"" + ARTIFACT_UUID +
            "\" ARTIFACT_DATA = \"" + ARTIFACT_DATA + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_GET_ASSET_LIST_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_ASSET_LIST.getName() + "\" CONSUMER_ID = \"" +
            DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_GET_TOSCA_MODEL_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_TOSCA_MODEL.getName() + "\" CONSUMER_ID = \"" +
            DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" RESOURCE_NAME = \"" + RESOURCE_NAME + "\" RESOURCE_TYPE = \"" +
            RESOURCE_TYPE + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_AUTH_REQUEST_LOG_STR = "ACTION = \"" + AuditingActionEnum.AUTH_REQUEST.getName() + "\" URL = \"" +
            AUTH_URL + "\" USER = \"" + USER_ID + "\" AUTH_STATUS = \"" + AUTH_STATUS + "\" REALM = \"" + REALM + "\"";

    final static String EXPECTED_ADD_ECOMP_USER_CRED_LOG_STR = "ACTION = \"" + AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS.getName() +
            "\" MODIFIER = \"" + MODIFIER_UID + "\" ECOMP_USER = \"" + USER_ID + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    final static String EXPECTED_GET_ECOMP_USER_CRED_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS.getName() +
            "\" MODIFIER = \"" + MODIFIER_UID + "\" ECOMP_USER = \"" + USER_ID + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_ADD_CATEGORY_LOG_STR = "ACTION = \"" + AuditingActionEnum.ADD_CATEGORY.getName() +
            "\" MODIFIER = \"" + MODIFIER_UID + "\" CATEGORY_NAME = \"" + CATEGORY + "\" SUB_CATEGORY_NAME = \"" + SUB_CATEGORY +
            "\" GROUPING_NAME = \"" + GROUPING_NAME + "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    private final static String OP_ENV_LOG = "\" OPERATIONAL_ENVIRONMENT_ACTION = \"" + OP_ENV_ACTION + "\" OPERATIONAL_ENVIRONMENT_ID = \"" + OP_ENV_ID +
                                        "\" OPERATIONAL_ENVIRONMENT_NAME = \"" + OP_ENV_NAME + "\" OPERATIONAL_ENVIRONMENT_TYPE = \"" + OP_ENV_TYPE +
                                         "\" TENANT_CONTEXT = \"" + TENANT_CONTEXT + "\"";

    public final static String EXPECTED_CREATE_OP_ENV_LOG_STR = "ACTION = \"" + AuditingActionEnum.CREATE_ENVIRONMENT.getName() + OP_ENV_LOG;

    public final static String EXPECTED_UNSUPPORTED_TYPE_OP_ENV_LOG_STR = "ACTION = \"" + AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE.getName() + OP_ENV_LOG;

    public final static String EXPECTED_UNKNOWN_NOTIFICATION_OP_ENV_LOG_STR = "ACTION = \"" + AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION.getName() + OP_ENV_LOG;

    public final static String EXPECTED_DIST_ADD_KEY_ENGINE_LOG_STR = "ACTION = \"" + AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL.getName() +
            "\" D_ENV = \"" + DIST_ENV_NAME + "\" TOPIC_NAME = \"" + DIST_NOTIFY_TOPIC + "\" ROLE = \"" + DIST_ROLE + "\" API_KEY = \"" +
            DIST_API_KEY + "\" STATUS = \"" + STATUS_OK + "\"";

    public final static String EXPECTED_DIST_CREATE_TOPIC_ENGINE_LOG_STR = "ACTION = \"" + AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC.getName() +
            "\" D_ENV = \"" + DIST_ENV_NAME + "\" TOPIC_NAME = \"" + DIST_NOTIFY_TOPIC + "\" STATUS = \"" + STATUS_OK + "\"";

    public final static String EXPECTED_DIST_REG_ENGINE_LOG_STR = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_REGISTER.getName() +
            "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" API_KEY = \"" + DIST_API_KEY + "\" D_ENV = \"" + DIST_ENV_NAME + "\" STATUS = \"" +
            STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\" DNOTIF_TOPIC = \"" + DIST_NOTIFY_TOPIC + "\" DSTATUS_TOPIC = \"" + DIST_STATUS_TOPIC + "\"";

    public final static String EXPECTED_DIST_STATUS_LOG_STR = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_STATUS.getName() +
            "\" DID = \"" + DIST_ID + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" TOPIC_NAME = \"" + TOPIC_NAME +
            "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" STATUS_TIME = \"" + DIST_STATUS_TIME + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DIST_DOWNLOAD_LOG_STR = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD.getName() +
            "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_DISTRIB_DEPLOY_LOG_STR = "ACTION = \"" + AuditingActionEnum.DISTRIBUTION_DEPLOY.getName() +
            "\" RESOURCE_NAME = \"" + RESOURCE_NAME + "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" SERVICE_INSTANCE_ID = \"" +
            SERVICE_INSTANCE_ID + "\" CURR_VERSION = \"" + CURRENT_VERSION + "\" MODIFIER = \"" + USER_UID + "\" DID = \"" + DIST_ID +
            "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_GET_UEB_CLUSTER_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_UEB_CLUSTER.getName() +
            "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" STATUS_TIME = "; //STATUS_TIME value is calculated at run time

    public final static String EXPECTED_GET_CATEGORY_HIERARCHY_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_CATEGORY_HIERARCHY.getName() + "\" MODIFIER = \"" + USER_UID +
            "\" DETAILS = \"" + USER_DETAILS + "\" STATUS = \"" + STATUS_OK + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_EXTERNAL_ASSET_LOG_STR = "ACTION = \"" + AuditingActionEnum.GET_ASSET_METADATA.getName() +
            "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID + "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" RESOURCE_NAME = \"" + RESOURCE_NAME +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE +"\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" STATUS = \"" + STATUS_OK +
            "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_EXTERNAL_CREATE_RESOURCE_LOG_STR = "ACTION = \"" + AuditingActionEnum.CREATE_RESOURCE_BY_API.getName() +
            "\" RESOURCE_NAME = \"" + RESOURCE_NAME + "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID +
            "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" MODIFIER = \"" + MODIFIER_UID + "\" CURR_VERSION = \"" + CURRENT_VERSION +
            "\" CURR_STATE = \"" + CURRENT_STATE + "\" CURR_ARTIFACT_UUID = \"" + ARTIFACT_UUID + "\" STATUS = \"" + STATUS_OK +
            "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID + "\" INVARIANT_UUID = \"" + INVARIANT_UUID + "\" DESC = \"" + DESCRIPTION + "\"";

    public final static String EXPECTED_EXTERNAL_CREATE_SERVICE_LOG_STR = "ACTION = \"" + AuditingActionEnum.CREATE_SERVICE_BY_API.getName() +
            "\" RESOURCE_TYPE = \"" + RESOURCE_TYPE + "\" CONSUMER_ID = \"" + DIST_CONSUMER_ID +
            "\" RESOURCE_URL = \"" + DIST_RESOURCE_URL + "\" MODIFIER = \"" + MODIFIER_UID +
            "\" STATUS = \"" + STATUS_OK + "\" SERVICE_INSTANCE_ID = \"" + SERVICE_INSTANCE_ID +
            "\" INVARIANT_UUID = \"" + INVARIANT_UUID + "\" DESC = \"" + DESCRIPTION + "\"";


    public static User user;
    public static User modifier;

    public static void init(Configuration.ElasticSearchConfig esConfig) {
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        configurationManager.getConfiguration().setDisableAudit(false);
        configurationManager.getConfiguration().setElasticSearch(esConfig);

        user = new User();
        modifier = new User();
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setUserId(USER_ID);

        modifier.setFirstName(MODIFIER_FIRST_NAME);
        modifier.setLastName(MODIFIER_LAST_NAME);
        modifier.setUserId(MODIFIER_ID);
    }


}
