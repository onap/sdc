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

package org.openecomp.sdc.action.errors;

public class ActionErrorConstants {

  //Error Codes
  public static final String ACTION_REQUEST_INVALID_GENERIC_CODE = "ACT0001";
  public static final String ACTION_AUTHENTICATION_ERR_CODE = "ACT1000";
  public static final String ACTION_AUTHORIZATION_ERR_CODE = "ACT1001";
  public static final String ACTION_INVALID_INSTANCE_ID_CODE = "ACT1002";
  public static final String ACTION_INVALID_REQUEST_ID_CODE = "ACT1003";
  public static final String ACTION_INVALID_PARAM_CODE = "ACT1004";
  //Operation <status> is not supported
  public static final String ACTION_INVALID_REQUEST_BODY_CODE = "ACT1005";
  //ACTION_REQUEST_BODY_EMPTY
  public static final String ACTION_UPDATE_NOT_ALLOWED_CODE_NAME = "ACT1007";
  public static final String ACTION_CHECKOUT_ON_LOCKED_ENTITY = "ACT1008";
  public static final String ACTION_ENTITY_UNIQUE_VALUE_ERROR = "ACT1009";

  public static final String ACTION_INVALID_SEARCH_CRITERIA = "ACT1011";
  public static final String ACTION_MULT_SEARCH_CRITERIA = "ACT1012";
  public static final String ACTION_UPDATE_ON_UNLOCKED_ENTITY = "ACT1013";
  public static final String ACTION_UPDATE_INVALID_VERSION = "ACT1014";
  public static final String ACTION_UPDATE_NOT_ALLOWED_CODE = "ACT1015";

  public static final String ACTION_CHECKIN_ON_UNLOCKED_ENTITY = "ACT1017";
  public static final String ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED = "ACT1018";
  public static final String ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED = "ACT1019";
  public static final String ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY = "ACT1020";
  public static final String ACTION_NOT_LOCKED_CODE = "ACT1021";
  public static final String ACTION_ARTIFACT_CHECKSUM_ERROR_CODE = "ACT1022";
  public static final String ACTION_ARTIFACT_TOO_BIG_ERROR_CODE = "ACT1023";
  public static final String ACTION_ARTIFACT_ALREADY_EXISTS_CODE = "ACT1025";
  public static final String ACTION_ARTIFACT_UPDATE_READ_ONLY = "ACT1026";
  public static final String ACTION_ARTIFACT_DELETE_READ_ONLY = "ACT1027";
  public static final String ACTION_ARTIFACT_INVALID_PROTECTION_CODE = "ACT1028";
  public static final String ACTION_ARTIFACT_INVALID_NAME_CODE = "ACT1029";

  public static final String ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER = "ACT1041";
  public static final String ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER = "ACT1042";
  public static final String ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER = "ACT1043";
  public static final String ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER = "ACT1044";
  public static final String ACTION_ENTITY_NOT_EXIST_CODE = "ACT1045";
  public static final String ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE = "ACT1046";
  public static final String ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE = "ACT1047";
  public static final String ACTION_DELETE_ON_LOCKED_ENTITY_CODE = "ACT1048";

  public static final String ACTION_INTERNAL_SERVER_ERR_CODE = "ACT1060";
  public static final String ACTION_QUERY_FAILURE_CODE = "QUERY_FAILURE";
  public static final String ACTION_QUERY_FAILURE_MSG = "Query Failure";


  //Error Messages
  public static final String ACTION_REQUEST_BODY_EMPTY = "Request Body is missing";
  //"The API failed due to missing body";
  public static final String ACTION_REQUEST_MISSING_MANDATORY_PARAM =
      "Missing mandatory parameter(s) : ";
  public static final String ACTION_REQUEST_ECOMP_INSTANCE_ID_INVALID =
      "X-ECOMP-InstanceID HTTP header missing or empty";
  public static final String ACTION_REQUEST_ECOMP_REQUEST_ID_INVALID =
      "X-ECOMP-RequestID HTTP header missing or empty";
  public static final String ACTION_REQUEST_CONTENT_TYPE_INVALID =
      "Content-Type HTTP header missing or empty";
  public static final String ACTION_REQUEST_AUTHORIZATION_HEADER_INVALID =
      "Authentication is required to use the API";
  public static final String ACTION_REQUEST_INVALID_NAME =
      "Invalid syntax for action name. No whitespaces allowed.";
  public static final String ACTION_REQUEST_FILTER_PARAM_INVALID =
      "Invalid Search filter criteria provided";
  public static final String ACTION_FILTER_MULTIPLE_QUERY_PARAM_NOT_SUPPORTED =
      "Multiple filter criteria are not supported";
  //"Operation supports filter by one property at a time";
  public static final String ACTION_ARTIFACT_ENTITY_NOT_EXIST = "Specified artifact is not found";
  public static final String ACTION_REQUEST_ARTIFACT_CHECKSUM_ERROR = "Checksum error";
  public static final String ACTION_REQUEST_ARTIFACT_INVALID_PROTECTION_VALUE =
      "Invalid artifact protection value";

  public static final String ACTION_ARTIFACT_INVALID_NAME =
      "Artifact name cannot contain any of the following characters : #<>$+%!`&*'|{}?=/:@ "
          + "including whitespaces, double quotes and back-slash";
  public static final String ACTION_ARTIFACT_READ_FILE_ERROR = "Error Occurred while reading file";
  public static final String ACTION_REQUEST_ARTIFACT_OPERATION_ALLOWED =
      "Artifacts cannot be created/updated using this operation";
  public static final String ACTION_ARTIFACT_TOO_BIG_ERROR =
      "Operation is not allowed. Artifact size exceeds the maximum file size limit (20MB).";

  //Business Validation Error messages
  public static final String ACTION_UPDATE_NOT_ALLOWED_FOR_NAME =
      "Action Name update is not allowed";
  public static final String ACTION_UPDATE_PARAM_INVALID =
      "Update not allowed for the parameter(s) : %s";
  public static final String ACTION_ENTITY_NOT_EXIST = "Specified Action is not found";
  public static final String ACTION_REQUESTED_VERSION_INVALID = "Invalid requested version : %s";
  public static final String ACTION_UNSUPPORTED_OPERATION = "Operation %s not supported";
  public static final String ACTION_ENTITY_UNIQUE_VALUE_MSG =
      "%s with the value '%s' already exists.";
  public static final String ACTION_ARTIFACT_ALREADY_EXISTS =
      "Artifact name already exists for Action with id %s. Please use another name.";
  public static final String ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG =
      "The request failed due to an internal ASDC problem. ECOMP Component should continue the "
          + "attempts, with corrected data if required, to create the resource.";
  public static final String ACTION_ARTIFACT_DELETE_READ_ONLY_MSG =
      "Cannot delete read only artifact.";
  public static final String ACTION_ARTIFACT_UPDATE_READ_ONLY_MSG =
      "Cannot update read only artifact.";
  public static final String ACTION_NOT_LOCKED_MSG =
      "Operation is not allowed. Action status should be Locked.";
  public static final String ACTION_ARTIFACT_UPDATE_NAME_INVALID =
      "Artifact name cannot be updated.";
  public static final String ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER =
      "Cannot delete artifact since it is locked by other user %s.";
  public static final String UNDO_CHECKOUT_ON_UNLOCKED_ENTITY_MSG =
      "Can not undo checkout on versionable entity %s with id %s since it is not checked out.";
  public static final String UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG =
      "Can not undo checkout on versionable entity %s with id"
          + " %s since it is checked out by other user: %s.";
}
