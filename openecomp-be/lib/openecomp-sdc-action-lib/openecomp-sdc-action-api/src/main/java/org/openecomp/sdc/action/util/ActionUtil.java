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

package org.openecomp.sdc.action.util;

import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.logging.StatusCode;
import org.openecomp.sdc.action.types.ActionLogResponseCode;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static org.openecomp.sdc.action.ActionConstants.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.action.ActionConstants.ELAPSED_TIME;
import static org.openecomp.sdc.action.ActionConstants.END_TIMESTAMP;
import static org.openecomp.sdc.action.ActionConstants.ERROR_CATEGORY;
import static org.openecomp.sdc.action.ActionConstants.ERROR_CODE;
import static org.openecomp.sdc.action.ActionConstants.ERROR_DESCRIPTION;
import static org.openecomp.sdc.action.ActionConstants.RESPONSE_CODE;
import static org.openecomp.sdc.action.ActionConstants.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.action.ActionConstants.SERVICE_METRIC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.action.ActionConstants.STATUS_CODE;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY;
import static org.openecomp.sdc.action.ActionConstants.TARGET_SERVICE_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ALREADY_EXISTS_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_CHECKSUM_ERROR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_INVALID_NAME_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_INVALID_PROTECTION_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_TOO_BIG_ERROR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_AUTHENTICATION_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_AUTHORIZATION_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_DELETE_ON_LOCKED_ENTITY_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_ERROR;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_INSTANCE_ID_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_PARAM_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_REQUEST_BODY_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_REQUEST_ID_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_SEARCH_CRITERIA;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_MULT_SEARCH_CRITERIA;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_NOT_LOCKED_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_QUERY_FAILURE_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_REQUEST_INVALID_GENERIC_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_INVALID_VERSION;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ACTION_NAME_ALREADY_EXISTS;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ACTION_NAME_UPDATE_NOT_ALLOWED;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ACTION_NOT_FOUND;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ACTION_NOT_LOCKED;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_ALREADY_EXISTS;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_DELETE_READ_ONLY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_NAME_INVALID;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_NOT_FOUND;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_PROTECTION_INVALID;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_TOO_BIG;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.ARTIFACT_UPDATE_READ_ONLY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.CHECKIN_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.CHECKIN_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.CHECKOUT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.CHECKSUM_ERROR;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.DELETE_ARTIFACT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.DELETE_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.INTERNAL_SERVER_ERROR;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.INVALID_REQUESTED_VERSION;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.INVALID_REQUEST_PARAM;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.INVALID_SEARCH_FILTER_CRITERIA;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.MISSING_AUTHORIZATION;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.MISSING_INSTANCE_ID_HEADER;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.MISSING_REQUEST_BODY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.MISSING_REQUEST_ID_HEADER;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.MULTIPLE_FILTER_CRITERIA_NOT_SUPPORTED;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.QUERY_FAILURE;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.SUBMIT_ON_FINAL_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.SUBMIT_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.UNDO_CHECKOUT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.UNDO_CHECKOUT_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.UPDATE_NOT_ALLOWED;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.UPDATE_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.UPDATE_ON_UNLOCKED_ENTITY;

public class ActionUtil {

  private static final String UTC_DATE_FORMAT = "dd MMM yyyy kk:mm:ss z";
  private static final String LOG_UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  /**
   * Get Current Timestamp in UTC format.
   *
   * @return Current Timestamp in UTC format
   */
  public static Date getCurrentTimeStampUtc() {
    return Date.from(java.time.ZonedDateTime.now(ZoneOffset.UTC).toInstant());
  }

  /**
   * Convert timestamp to UTC format date string.
   *
   * @param timeStamp UTC timestamp to be converted to the UTC Date format
   * @return UTC formatted Date string from timestamp
   */
  public static String getUtcDateStringFromTimestamp(Date timeStamp) {
    DateFormat df = new SimpleDateFormat(UTC_DATE_FORMAT);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    return df.format(timeStamp);
  }

  /**
   * Convert timestamp to UTC format date string.
   *
   * @param timeStamp UTC timestamp to be converted to the UTC Date format
   * @return UTC formatted Date string from timestamp
   */
  public static String getLogUtcDateStringFromTimestamp(Date timeStamp) {
    DateFormat df = new SimpleDateFormat(LOG_UTC_DATE_FORMAT);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    return df.format(timeStamp);
  }

  /**
   * Method to set up specific attributes MDC for the current logging operation.
   *
   * @param subOperation Request Name
   */
  public static void actionLogPreProcessor(ActionSubOperation subOperation, String targetEntity) {
    MDC.put(BEGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
    if (subOperation != null) {
      MDC.put(TARGET_SERVICE_NAME, subOperation.name());
    }

    MDC.put(TARGET_ENTITY, targetEntity);
  }

  /**
   * Method to enhance the MDC after the logging operation for Metrics and Audit logs.
   *
   * @param statusCode Response code for the current operation
   */
  public static void actionLogPostProcessor(StatusCode statusCode) {
    actionLogPostProcessor(statusCode, false);
  }

  public static void actionLogPostProcessor(StatusCode statusCode, boolean isServiceMetricLog) {
    actionLogPostProcessor(statusCode, null, isServiceMetricLog);
  }

  public static void actionLogPostProcessor(StatusCode statusCode, String responseCode,
                                            boolean isServiceMetricLog) {
    actionLogPostProcessor(statusCode, responseCode, null, isServiceMetricLog);
  }

  /**
   * Action log post processor.
   *
   * @param statusCode          the status code
   * @param responseCode        the response code
   * @param responseDescription the response description
   * @param isServiceMetricLog  the is service metric log
   */
  public static void actionLogPostProcessor(StatusCode statusCode, String responseCode,
                                            String responseDescription,
                                            boolean isServiceMetricLog) {
    MDC.put(STATUS_CODE, statusCode.name());
    if (responseCode != null) {
      int logResponseCode = getLogResponseCode(responseCode);
      MDC.put(RESPONSE_CODE, Integer.toString(logResponseCode));
    }
    MDC.put(RESPONSE_DESCRIPTION, responseDescription);
    long beginTimestamp;
    if (isServiceMetricLog) {
      beginTimestamp = Long.valueOf(MDC.get(SERVICE_METRIC_BEGIN_TIMESTAMP));
    } else {
      beginTimestamp = Long.valueOf(MDC.get(BEGIN_TIMESTAMP));
    }
    long endTimestamp = System.currentTimeMillis();
    MDC.put(BEGIN_TIMESTAMP, getLogUtcDateStringFromTimestamp(new Date(beginTimestamp)));
    MDC.put(END_TIMESTAMP, getLogUtcDateStringFromTimestamp(new Date(endTimestamp)));
    MDC.put(ELAPSED_TIME, String.valueOf(endTimestamp - beginTimestamp));
  }

  /**
   * Action Library Error logging Helper.
   *
   * @param errorCategory    WARN or ERROR
   * @param errorCode        Action Library exception code
   * @param errorDescription Description of the error
   */
  public static void actionErrorLogProcessor(CategoryLogLevel errorCategory, String errorCode,
                                             String errorDescription) {
    MDC.put(ERROR_CATEGORY, errorCategory.name());
    if (errorCode != null) {
      String errorType = "";
      switch (errorCategory) {
        case WARN:
          errorType = "W";
          break;
        case ERROR:
          errorType = "E";
          break;
        case FATAL:
          errorType = "F";
          break;
        default:
      }
      MDC.put(ERROR_CODE, getLogResponseCode(errorCode) + errorType);
    }
    MDC.put(ERROR_DESCRIPTION, errorDescription);
  }

  /**
   * Method to convert Action Library exception codes to OPENECOMP Audit codes in {@link
   * ActionLogResponseCode} e.g: ACT1060 --> 201
   *
   * @param errorCode Action library exception code
   * @return Audit log code corresponding to the Action Library exception
   */
  public static int getLogResponseCode(String errorCode) {
    ActionLogResponseCode responseCode = INTERNAL_SERVER_ERROR;
    switch (errorCode) {
      case ACTION_REQUEST_INVALID_GENERIC_CODE:
        responseCode = INVALID_REQUEST_PARAM;
        break;
      case ACTION_AUTHENTICATION_ERR_CODE:
        break;
      case ACTION_AUTHORIZATION_ERR_CODE:
        responseCode = MISSING_AUTHORIZATION;
        break;
      case ACTION_INVALID_INSTANCE_ID_CODE:
        responseCode = MISSING_INSTANCE_ID_HEADER;
        break;
      case ACTION_INVALID_REQUEST_ID_CODE:
        responseCode = MISSING_REQUEST_ID_HEADER;
        break;
      case ACTION_INVALID_PARAM_CODE:
        responseCode = INVALID_REQUEST_PARAM;
        break;
      case ACTION_INVALID_REQUEST_BODY_CODE:
        responseCode = MISSING_REQUEST_BODY;
        break;
      case ACTION_UPDATE_NOT_ALLOWED_CODE_NAME:
        responseCode = ACTION_NAME_UPDATE_NOT_ALLOWED;
        break;
      case ACTION_CHECKOUT_ON_LOCKED_ENTITY:
        responseCode = CHECKOUT_ON_LOCKED_ENTITY;
        break;
      case ACTION_ENTITY_UNIQUE_VALUE_ERROR:
        responseCode = ACTION_NAME_ALREADY_EXISTS;
        break;
      case ACTION_INVALID_SEARCH_CRITERIA:
        responseCode = INVALID_SEARCH_FILTER_CRITERIA;
        break;
      case ACTION_MULT_SEARCH_CRITERIA:
        responseCode = MULTIPLE_FILTER_CRITERIA_NOT_SUPPORTED;
        break;
      case ACTION_UPDATE_ON_UNLOCKED_ENTITY:
        responseCode = UPDATE_ON_UNLOCKED_ENTITY;
        break;
      case ACTION_UPDATE_INVALID_VERSION:
        responseCode = INVALID_REQUESTED_VERSION;
        break;
      case ACTION_UPDATE_NOT_ALLOWED_CODE:
        responseCode = UPDATE_NOT_ALLOWED;
        break;
      case ACTION_CHECKIN_ON_UNLOCKED_ENTITY:
        responseCode = CHECKIN_ON_UNLOCKED_ENTITY;
        break;
      case ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED:
        responseCode = SUBMIT_ON_FINAL_ENTITY;
        break;
      case ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED:
        responseCode = SUBMIT_ON_LOCKED_ENTITY_OTHER_USER;
        break;
      case ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY:
        responseCode = UNDO_CHECKOUT_ON_UNLOCKED_ENTITY;
        break;
      case ACTION_NOT_LOCKED_CODE:
        responseCode = ACTION_NOT_LOCKED;
        break;
      case ACTION_ARTIFACT_CHECKSUM_ERROR_CODE:
        responseCode = CHECKSUM_ERROR;
        break;
      case ACTION_ARTIFACT_TOO_BIG_ERROR_CODE:
        responseCode = ARTIFACT_TOO_BIG;
        break;
      case ACTION_ARTIFACT_ALREADY_EXISTS_CODE:
        responseCode = ARTIFACT_ALREADY_EXISTS;
        break;
      case ACTION_ARTIFACT_UPDATE_READ_ONLY:
        responseCode = ARTIFACT_UPDATE_READ_ONLY;
        break;
      case ACTION_ARTIFACT_DELETE_READ_ONLY:
        responseCode = ARTIFACT_DELETE_READ_ONLY;
        break;
      case ACTION_ARTIFACT_INVALID_PROTECTION_CODE:
        responseCode = ARTIFACT_PROTECTION_INVALID;
        break;
      case ACTION_ARTIFACT_INVALID_NAME_CODE:
        responseCode = ARTIFACT_NAME_INVALID;
        break;
      case ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER:
        responseCode = UPDATE_ON_LOCKED_ENTITY;
        break;
      case ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER:
        responseCode = CHECKIN_ON_LOCKED_ENTITY_OTHER_USER;
        break;
      case ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER:
        responseCode = CHECKOUT_ON_LOCKED_ENTITY;
        break;
      case ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER:
        responseCode = UNDO_CHECKOUT_ON_LOCKED_ENTITY;
        break;
      case ACTION_ENTITY_NOT_EXIST_CODE:
        responseCode = ACTION_NOT_FOUND;
        break;
      case ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE:
        responseCode = ARTIFACT_NOT_FOUND;
        break;
      case ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE:
        responseCode = DELETE_ARTIFACT_ON_LOCKED_ENTITY;
        break;
      case ACTION_DELETE_ON_LOCKED_ENTITY_CODE:
        responseCode = DELETE_ON_LOCKED_ENTITY_OTHER_USER;
        break;
      case ACTION_INTERNAL_SERVER_ERR_CODE:
        responseCode = INTERNAL_SERVER_ERROR;
        break;
      case ACTION_QUERY_FAILURE_CODE:
        responseCode = QUERY_FAILURE;
        break;
      default:
    }
    return responseCode.getValue();
  }
}
