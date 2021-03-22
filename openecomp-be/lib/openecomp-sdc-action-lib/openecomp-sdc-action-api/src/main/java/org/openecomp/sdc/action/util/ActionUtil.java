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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.LongSupplier;
import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.types.ActionLogResponseCode;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.slf4j.MDC;

public class ActionUtil {

    private static final String UTC_DATE_FORMAT = "dd MMM yyyy kk:mm:ss z";
    private static final String LOG_UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final ActionLogResponseCode defaultResponseCode = INTERNAL_SERVER_ERROR;
    private static final Map<String, ActionLogResponseCode> errorCodeMap = initErrorCodeMap();
    private static final EnumMap<CategoryLogLevel, String> errorTypeMap = initErrorTypeMap();

    private ActionUtil() {
    }

    private static Map<String, ActionLogResponseCode> initErrorCodeMap() {
        Map<String, ActionLogResponseCode> map = new HashMap<>();
        map.put(ACTION_REQUEST_INVALID_GENERIC_CODE, INVALID_REQUEST_PARAM);
        map.put(ACTION_AUTHENTICATION_ERR_CODE, INTERNAL_SERVER_ERROR);
        map.put(ACTION_AUTHORIZATION_ERR_CODE, MISSING_AUTHORIZATION);
        map.put(ACTION_INVALID_INSTANCE_ID_CODE, MISSING_INSTANCE_ID_HEADER);
        map.put(ACTION_INVALID_REQUEST_ID_CODE, MISSING_REQUEST_ID_HEADER);
        map.put(ACTION_INVALID_PARAM_CODE, INVALID_REQUEST_PARAM);
        map.put(ACTION_INVALID_REQUEST_BODY_CODE, MISSING_REQUEST_BODY);
        map.put(ACTION_UPDATE_NOT_ALLOWED_CODE_NAME, ACTION_NAME_UPDATE_NOT_ALLOWED);
        map.put(ACTION_CHECKOUT_ON_LOCKED_ENTITY, CHECKOUT_ON_LOCKED_ENTITY);
        map.put(ACTION_ENTITY_UNIQUE_VALUE_ERROR, ACTION_NAME_ALREADY_EXISTS);
        map.put(ACTION_INVALID_SEARCH_CRITERIA, INVALID_SEARCH_FILTER_CRITERIA);
        map.put(ACTION_MULT_SEARCH_CRITERIA, MULTIPLE_FILTER_CRITERIA_NOT_SUPPORTED);
        map.put(ACTION_UPDATE_ON_UNLOCKED_ENTITY, UPDATE_ON_UNLOCKED_ENTITY);
        map.put(ACTION_UPDATE_INVALID_VERSION, INVALID_REQUESTED_VERSION);
        map.put(ACTION_UPDATE_NOT_ALLOWED_CODE, UPDATE_NOT_ALLOWED);
        map.put(ACTION_CHECKIN_ON_UNLOCKED_ENTITY, CHECKIN_ON_UNLOCKED_ENTITY);
        map.put(ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED, SUBMIT_ON_FINAL_ENTITY);
        map.put(ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED, SUBMIT_ON_LOCKED_ENTITY_OTHER_USER);
        map.put(ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY, UNDO_CHECKOUT_ON_UNLOCKED_ENTITY);
        map.put(ACTION_NOT_LOCKED_CODE, ACTION_NOT_LOCKED);
        map.put(ACTION_ARTIFACT_CHECKSUM_ERROR_CODE, CHECKSUM_ERROR);
        map.put(ACTION_ARTIFACT_TOO_BIG_ERROR_CODE, ARTIFACT_TOO_BIG);
        map.put(ACTION_ARTIFACT_ALREADY_EXISTS_CODE, ARTIFACT_ALREADY_EXISTS);
        map.put(ACTION_ARTIFACT_UPDATE_READ_ONLY, ARTIFACT_UPDATE_READ_ONLY);
        map.put(ACTION_ARTIFACT_DELETE_READ_ONLY, ARTIFACT_DELETE_READ_ONLY);
        map.put(ACTION_ARTIFACT_INVALID_PROTECTION_CODE, ARTIFACT_PROTECTION_INVALID);
        map.put(ACTION_ARTIFACT_INVALID_NAME_CODE, ARTIFACT_NAME_INVALID);
        map.put(ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER, UPDATE_ON_LOCKED_ENTITY);
        map.put(ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER, CHECKIN_ON_LOCKED_ENTITY_OTHER_USER);
        map.put(ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER, CHECKOUT_ON_LOCKED_ENTITY);
        map.put(ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER, UNDO_CHECKOUT_ON_LOCKED_ENTITY);
        map.put(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_NOT_FOUND);
        map.put(ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE, ARTIFACT_NOT_FOUND);
        map.put(ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE, DELETE_ARTIFACT_ON_LOCKED_ENTITY);
        map.put(ACTION_DELETE_ON_LOCKED_ENTITY_CODE, DELETE_ON_LOCKED_ENTITY_OTHER_USER);
        map.put(ACTION_INTERNAL_SERVER_ERR_CODE, INTERNAL_SERVER_ERROR);
        map.put(ACTION_QUERY_FAILURE_CODE, QUERY_FAILURE);
        return map;
    }

    private static EnumMap<CategoryLogLevel, String> initErrorTypeMap() {
        EnumMap<CategoryLogLevel, String> map = new EnumMap<>(CategoryLogLevel.class);
        map.put(CategoryLogLevel.WARN, "W");
        map.put(CategoryLogLevel.ERROR, "E");
        map.put(CategoryLogLevel.FATAL, "F");
        return map;
    }

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
    public static void actionLogPostProcessor(ResponseStatus statusCode) {
        actionLogPostProcessor(statusCode, false);
    }

    public static void actionLogPostProcessor(ResponseStatus statusCode, boolean isServiceMetricLog) {
        actionLogPostProcessor(statusCode, null, isServiceMetricLog);
    }

    public static void actionLogPostProcessor(ResponseStatus statusCode, String responseCode, boolean isServiceMetricLog) {
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
    public static void actionLogPostProcessor(ResponseStatus statusCode, String responseCode, String responseDescription,
                                              boolean isServiceMetricLog) {
        actionLogPostProcessor(statusCode, responseCode, responseDescription, isServiceMetricLog, System::currentTimeMillis);
    }

    /**
     * Action log post processor.
     *
     * @param statusCode          the status code
     * @param responseCode        the response code
     * @param responseDescription the response description
     * @param isServiceMetricLog  the is service metric log
     */
    public static void actionLogPostProcessor(ResponseStatus statusCode, String responseCode, String responseDescription, boolean isServiceMetricLog,
                                              LongSupplier getCurrentTime) {
        MDC.put(STATUS_CODE, statusCode.name());
        if (responseCode != null) {
            int logResponseCode = getLogResponseCode(responseCode);
            MDC.put(RESPONSE_CODE, Integer.toString(logResponseCode));
        }
        MDC.put(RESPONSE_DESCRIPTION, responseDescription);
        long beginTimestamp;
        if (isServiceMetricLog) {
            beginTimestamp = Long.parseLong(MDC.get(SERVICE_METRIC_BEGIN_TIMESTAMP));
        } else {
            beginTimestamp = Long.parseLong(MDC.get(BEGIN_TIMESTAMP));
        }
        long endTimestamp = getCurrentTime.getAsLong();
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
    public static void actionErrorLogProcessor(CategoryLogLevel errorCategory, String errorCode, String errorDescription) {
        MDC.put(ERROR_CATEGORY, errorCategory.name());
        if (errorCode != null) {
            MDC.put(ERROR_CODE, getLogResponseCode(errorCode) + (errorTypeMap.getOrDefault(errorCategory, "")));
        }
        MDC.put(ERROR_DESCRIPTION, errorDescription);
    }

    /**
     * Method to convert Action Library exception codes to OPENECOMP Audit codes in {@link ActionLogResponseCode} e.g: ACT1060 --> 201
     *
     * @param errorCode Action library exception code
     * @return Audit log code corresponding to the Action Library exception
     */
    public static int getLogResponseCode(String errorCode) {
        return errorCodeMap.getOrDefault(errorCode, defaultResponseCode).getValue();
    }
}
