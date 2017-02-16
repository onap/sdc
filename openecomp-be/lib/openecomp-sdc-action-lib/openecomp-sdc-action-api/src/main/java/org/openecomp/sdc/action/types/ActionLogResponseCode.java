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

package org.openecomp.sdc.action.types;

import java.util.HashMap;
import java.util.Map;

public enum ActionLogResponseCode {

  MISSING_AUTHORIZATION(100),
  FORBIDDEN(101),
  UPDATE_ON_LOCKED_ENTITY(102),
  CHECKIN_ON_LOCKED_ENTITY_OTHER_USER(103),
  CHECKOUT_ON_LOCKED_ENTITY(104),
  UNDO_CHECKOUT_ON_LOCKED_ENTITY(105),
  DELETE_ARTIFACT_ON_LOCKED_ENTITY(106),
  DELETE_ON_LOCKED_ENTITY_OTHER_USER(107),
  INTERNAL_SERVER_ERROR(201),
  MISSING_MANDATORY_PARAMS(300),
  MISSING_INSTANCE_ID_HEADER(301),
  MISSING_REQUEST_ID_HEADER(302),
  MISSING_REQUEST_BODY(303),
  INVALID_SEARCH_FILTER_CRITERIA(304),
  INVALID_REQUESTED_VERSION(305),
  CHECKSUM_ERROR(306),
  ARTIFACT_TOO_BIG(307),
  ACTION_NOT_FOUND(308),
  ARTIFACT_NOT_FOUND(309),
  METHOD_NOT_ALLOWED(310),
  INVALID_REQUEST_PARAM(311),
  ARTIFACT_PROTECTION_INVALID(312),
  ACTION_NAME_UPDATE_NOT_ALLOWED(501),
  //METHOD_NOT_ALLOWED(502),
  ACTION_NAME_ALREADY_EXISTS(503),
  MULTIPLE_FILTER_CRITERIA_NOT_SUPPORTED(504),
  UPDATE_ON_UNLOCKED_ENTITY(505),
  UPDATE_NOT_ALLOWED(506),
  //METHOD_NOT_ALLOWED(507),
  CHECKIN_ON_UNLOCKED_ENTITY(508),
  SUBMIT_ON_FINAL_ENTITY(509),
  SUBMIT_ON_LOCKED_ENTITY_OTHER_USER(510),
  UNDO_CHECKOUT_ON_UNLOCKED_ENTITY(511),
  ACTION_NOT_LOCKED(512),
  ARTIFACT_ALREADY_EXISTS(513),
  ARTIFACT_UPDATE_READ_ONLY(514),
  ARTIFACT_DELETE_READ_ONLY(515),
  ARTIFACT_NAME_INVALID(516),
  ARTIFACT_CREATE_UPDATE_NOT_ALLOWED(518),
  QUERY_FAILURE(519);

  private static Map<Integer, ActionLogResponseCode> mapValueToEnum = new HashMap<>();

  static {
    for (ActionLogResponseCode responseCode : ActionLogResponseCode.values()) {
      mapValueToEnum.put(responseCode.value, responseCode);
    }
  }

  private int value;

  ActionLogResponseCode(int value) {
    this.value = value;
  }

  public static ActionLogResponseCode parseValue(int value) {
    return mapValueToEnum.get(value);
  }

  public int getValue() {
    return value;
  }
}
