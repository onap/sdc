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

package org.openecomp.sdc.action;

public class ActionConstants {

  //
  public static final long MAX_ACTION_ARTIFACT_SIZE = 20 * 1024 * 1024; //20 MB
  //REST layer constants
  public static final String X_ECOMP_INSTANCE_ID_HEADER_PARAM = "X-ECOMP-InstanceID";
  public static final String X_ECOMP_REQUEST_ID_HEADER_PARAM = "X-ECOMP-RequestID";
  public static final String WWW_AUTHENTICATE_HEADER_PARAM = "WWW-Authenticate";

  public static final String ACTION_REQUEST_PARAM_NAME = "name";
  public static final String ACTION_REQUEST_PARAM_END_POINT_URI = "endpointUri";
  public static final String SUPPORTED_MODELS_VERSION_ID = "versionId";
  public static final String SUPPORTED_COMPONENTS_ID = "Id";
  public static final String ACTION_REQUEST_PARAM_SUPPORTED_MODELS = SUPPORTED_MODELS_VERSION_ID
       + " in supportedModels";
  public static final String ACTION_REQUEST_PARAM_SUPPORTED_COMPONENTS =
      SUPPORTED_COMPONENTS_ID + " in supportedComponents";

  public static final String REQUEST_EMPTY_BODY = "{}";
  public static final String REQUEST_TYPE_CREATE_ACTION = "REQUEST_CREATE_ACTION";
  public static final String REQUEST_TYPE_UPDATE_ACTION = "REQUEST_UPDATE_ACTION";
  public static final String REQUEST_TYPE_VERSION_ACTION = "REQUEST_VERSION_ACTION";

  //DAO layer constants
  public static final String ACTION_VERSIONABLE_TYPE = "Action";

  //Manager constants
  public static final String UNIQUE_ID = "actionUuId";
  public static final String VERSION = "version";
  public static final String INVARIANTUUID = "actionInvariantUuId";
  public static final String STATUS = "status";
  public static final String ARTIFACTS = "artifacts";
  public static final String TIMESTAMP = "timeStamp";
  public static final String UPDATED_BY = "updatedBy";
  public static final String ARTIFACT_NAME = "artifactName";
  public static final String ARTIFACT_FILE = "Artifact to be uploaded";

  // Status
  public static final String UNDO_CHECKOUT_RESPONSE_TEXT =
      "Changes to the Action object successfully reverted back.";

  //GET Request Filter Types
  public static final String FILTER_TYPE_VENDOR = "VENDOR";
  public static final String FILTER_TYPE_CATEGORY = "CATEGORY";
  public static final String FILTER_TYPE_NAME = "NAME";
  public static final String FILTER_TYPE_MODEL = "MODEL";
  public static final String FILTER_TYPE_ECOMP_COMPONENT = "ECOMP_COMPONENT";
  public static final String FILTER_TYPE_NONE = "NONE";

  public static final String ARTIFACT_METADATA_ATTR_UUID = "ARTIFACT_UUID";
  public static final String ARTIFACT_METADATA_ATTR_NAME = "ARTIFACT_NAME";

  public static final String REQUEST_ID = "uuid";
  public static final String SERVICE_INSTANCE_ID = "serviceInstanceID";
  public static final String PARTNER_NAME = "userId";
  public static final String SERVICE_NAME = "ServiceName";
  public static final String INSTANCE_UUID = "InstanceUUID";
  public static final String REMOTE_HOST = "RemoteHost";
  public static final String CLIENT_IP = "ClientIP";
  public static final String CATEGORY_LOG_LEVEL = "level";
  public static final String STATUS_CODE = "StatusCode";
  public static final String RESPONSE_CODE = "ResponseCode";
  public static final String RESPONSE_DESCRIPTION = "ResponseDescription";
  public static final String ELAPSED_TIME = "ElapsedTime";
  public static final String BEGIN_TIMESTAMP = "BeginTimestamp";
  public static final String TARGET_SERVICE_NAME = "TargetServiceName";
  public static final String TARGET_ENTITY = "TargetEntity";
  public static final String TARGET_ENTITY_API = "API";
  public static final String TARGET_ENTITY_DB = "DB";
  public static final String END_TIMESTAMP = "EndTimestamp";
  public static final String ERROR_CATEGORY = "ErrorCategory";
  public static final String ERROR_CODE = "ErrorCode";
  public static final String ERROR_DESCRIPTION = "ErrorDescription";
  public static final String MDC_ASDC_INSTANCE_UUID = "ASDC";
  public static final String SERVICE_METRIC_BEGIN_TIMESTAMP = "SERVICE-METRIC-BEGIN-TIMESTAMP";
  public static final String LOCAL_ADDR = "localAddr"; //map ServerIPAddress from loggingfilter
  public static final String BE_FQDN = "beFqdn"; //map ServerFQDN from logging filter

  public final class UniqueValues {
    public static final String ACTION_NAME = "Action name";
  }
}
