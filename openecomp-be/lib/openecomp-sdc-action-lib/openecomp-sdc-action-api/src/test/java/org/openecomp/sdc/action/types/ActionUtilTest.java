/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License.getValue(), ActionUtil.getLogResponseCode(Version 2.0 (the "License"));
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing.getValue(), ActionUtil.getLogResponseCode(software
 * distributed under the License is distributed on an "AS IS" BASIS.getValue(),
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.getValue(), ActionUtil.getLogResponseCode(either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.action.types;

import org.junit.jupiter.api.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.util.ActionUtil;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openecomp.sdc.action.ActionConstants.*;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.*;
import static org.openecomp.sdc.action.types.ActionLogResponseCode.*;

public class ActionUtilTest {

    @Test
    public void getLogResponseCodeTest() {
        assertEquals(INVALID_REQUEST_PARAM.getValue(), ActionUtil.getLogResponseCode(ACTION_REQUEST_INVALID_GENERIC_CODE));
        assertEquals(INTERNAL_SERVER_ERROR.getValue(), ActionUtil.getLogResponseCode(ACTION_AUTHENTICATION_ERR_CODE));
        assertEquals(MISSING_AUTHORIZATION.getValue(), ActionUtil.getLogResponseCode(ACTION_AUTHORIZATION_ERR_CODE));
        assertEquals(MISSING_INSTANCE_ID_HEADER.getValue(), ActionUtil.getLogResponseCode(ACTION_INVALID_INSTANCE_ID_CODE));
        assertEquals(MISSING_REQUEST_ID_HEADER.getValue(), ActionUtil.getLogResponseCode(ACTION_INVALID_REQUEST_ID_CODE));
        assertEquals(INVALID_REQUEST_PARAM.getValue(), ActionUtil.getLogResponseCode(ACTION_INVALID_PARAM_CODE));
        assertEquals(MISSING_REQUEST_BODY.getValue(), ActionUtil.getLogResponseCode(ACTION_INVALID_REQUEST_BODY_CODE));
        assertEquals(ACTION_NAME_UPDATE_NOT_ALLOWED.getValue(), ActionUtil.getLogResponseCode(ACTION_UPDATE_NOT_ALLOWED_CODE_NAME));
        assertEquals(CHECKOUT_ON_LOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_CHECKOUT_ON_LOCKED_ENTITY));
        assertEquals(ACTION_NAME_ALREADY_EXISTS.getValue(), ActionUtil.getLogResponseCode(ACTION_ENTITY_UNIQUE_VALUE_ERROR));
        assertEquals(INVALID_SEARCH_FILTER_CRITERIA.getValue(), ActionUtil.getLogResponseCode(ACTION_INVALID_SEARCH_CRITERIA));
        assertEquals(MULTIPLE_FILTER_CRITERIA_NOT_SUPPORTED.getValue(), ActionUtil.getLogResponseCode(ACTION_MULT_SEARCH_CRITERIA));
        assertEquals(UPDATE_ON_UNLOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_UPDATE_ON_UNLOCKED_ENTITY));
        assertEquals(INVALID_REQUESTED_VERSION.getValue(), ActionUtil.getLogResponseCode(ACTION_UPDATE_INVALID_VERSION));
        assertEquals(UPDATE_NOT_ALLOWED.getValue(), ActionUtil.getLogResponseCode(ACTION_UPDATE_NOT_ALLOWED_CODE));
        assertEquals(CHECKIN_ON_UNLOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_CHECKIN_ON_UNLOCKED_ENTITY));
        assertEquals(SUBMIT_ON_FINAL_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED));
        assertEquals(SUBMIT_ON_LOCKED_ENTITY_OTHER_USER.getValue(), ActionUtil.getLogResponseCode(ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED));
        assertEquals(UNDO_CHECKOUT_ON_UNLOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY));
        assertEquals(ACTION_NOT_LOCKED.getValue(), ActionUtil.getLogResponseCode(ACTION_NOT_LOCKED_CODE));
        assertEquals(CHECKSUM_ERROR.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_CHECKSUM_ERROR_CODE));
        assertEquals(ARTIFACT_TOO_BIG.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_TOO_BIG_ERROR_CODE));
        assertEquals(ARTIFACT_ALREADY_EXISTS.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_ALREADY_EXISTS_CODE));
        assertEquals(ARTIFACT_UPDATE_READ_ONLY.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_UPDATE_READ_ONLY));
        assertEquals(ARTIFACT_DELETE_READ_ONLY.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_DELETE_READ_ONLY));
        assertEquals(ARTIFACT_PROTECTION_INVALID.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_INVALID_PROTECTION_CODE));
        assertEquals(ARTIFACT_NAME_INVALID.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_INVALID_NAME_CODE));
        assertEquals(UPDATE_ON_LOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER));
        assertEquals(CHECKIN_ON_LOCKED_ENTITY_OTHER_USER.getValue(), ActionUtil.getLogResponseCode(ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER));
        assertEquals(CHECKOUT_ON_LOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER));
        assertEquals(UNDO_CHECKOUT_ON_LOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER));
        assertEquals(ACTION_NOT_FOUND.getValue(), ActionUtil.getLogResponseCode(ACTION_ENTITY_NOT_EXIST_CODE));
        assertEquals(ARTIFACT_NOT_FOUND.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE));
        assertEquals(DELETE_ARTIFACT_ON_LOCKED_ENTITY.getValue(), ActionUtil.getLogResponseCode(ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE));
        assertEquals(DELETE_ON_LOCKED_ENTITY_OTHER_USER.getValue(), ActionUtil.getLogResponseCode(ACTION_DELETE_ON_LOCKED_ENTITY_CODE));
        assertEquals(INTERNAL_SERVER_ERROR.getValue(), ActionUtil.getLogResponseCode(ACTION_INTERNAL_SERVER_ERR_CODE));
        assertEquals(QUERY_FAILURE.getValue(), ActionUtil.getLogResponseCode(ACTION_QUERY_FAILURE_CODE));
        assertEquals(INTERNAL_SERVER_ERROR.getValue(), ActionUtil.getLogResponseCode("plomplomoyo"));
    }

    @Test
    public void actionErrorLogProcessorTest() {
        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.DEBUG, "", "description");
        assertEquals("201", MDC.get(ERROR_CODE));
        assertEquals("DEBUG", MDC.get(ERROR_CATEGORY));
        assertEquals("description", MDC.get(ERROR_DESCRIPTION));

        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.WARN, "222", "description?");
        assertEquals("201W", MDC.get(ERROR_CODE));
        assertEquals("WARN", MDC.get(ERROR_CATEGORY));
        assertEquals("description?", MDC.get(ERROR_DESCRIPTION));

        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_ARTIFACT_INVALID_NAME_CODE, "noitpircsed?!");
        assertEquals("516E", MDC.get(ERROR_CODE));
        assertEquals("ERROR", MDC.get(ERROR_CATEGORY));
        assertEquals("noitpircsed?!", MDC.get(ERROR_DESCRIPTION));

        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.FATAL, "400", "sloubi1");
        assertEquals("201F", MDC.get(ERROR_CODE));
        assertEquals("FATAL", MDC.get(ERROR_CATEGORY));
        assertEquals("sloubi1", MDC.get(ERROR_DESCRIPTION));

        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.INFO, null, "sloubi2");
        assertEquals("201F", MDC.get(ERROR_CODE));
        assertEquals("INFO", MDC.get(ERROR_CATEGORY));
        assertEquals("sloubi2", MDC.get(ERROR_DESCRIPTION));
    }

    @Test
    public void actionLogPostProcessorTest() {
        MDC.put(SERVICE_METRIC_BEGIN_TIMESTAMP, "486");
        MDC.put(BEGIN_TIMESTAMP, "133");

        ActionUtil.actionLogPostProcessor(ONAPLogConstants.ResponseStatus.COMPLETE, "randomString", "anotherString", true);
        assertEquals("COMPLETE", MDC.get(STATUS_CODE));
        assertEquals("201", MDC.get(RESPONSE_CODE));
        assertEquals("anotherString", MDC.get(RESPONSE_DESCRIPTION));

        ActionUtil.actionLogPostProcessor(ONAPLogConstants.ResponseStatus.COMPLETE, "randomString", "anotherString", true, () -> 68000L);
        assertEquals("1970-01-01T00:01:08.000", MDC.get(END_TIMESTAMP));
        assertEquals("67514", MDC.get(ELAPSED_TIME));
        assertEquals("1970-01-01T00:00:00.486", MDC.get(BEGIN_TIMESTAMP));

        ActionUtil.actionLogPreProcessor(ActionSubOperation.CHECKOUT_ACTION, "targetEntity");
        assertEquals("targetEntity", MDC.get(TARGET_ENTITY));
        assertEquals("CHECKOUT_ACTION", MDC.get(TARGET_SERVICE_NAME));
    }

    @Test
    public void getLogUtcDateStringFromTimestampTest() {
        assertEquals("2020-01-23T12:34:56.000", ActionUtil.getLogUtcDateStringFromTimestamp(Date.from(Instant.now(Clock.fixed(Instant.parse("2020-01-23T12:34:56Z"), ZoneOffset.UTC)))));
    }
}
