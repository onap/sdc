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

package org.openecomp.sdc.common.transaction.api;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class TransactionUtils {

    private TransactionUtils() {

    }

    public enum DBTypeEnum {
        ELASTIC_SEARCH, JANUSGRAPH, MYSTERY, SWIFT
    }

    public enum TransactionCodeEnum {
        ROLLBACK_SUCCESS, ROLLBACK_FAILED, SUCCESS, COMMIT_FAILED, MULTIPLE_LAST_ACTION, INTERNAL_ERROR, UNALLOWED_METHOD_USAGE, PREPARE_ROLLBACK_FAILED, TRANSACTION_CLOSED
    }

    public enum TransactionStatusEnum {
        OPEN, CLOSED, FAILED_ROLLBACK
    }

    public enum DBActionCodeEnum {
        SUCCESS, FAIL_GENERAL, FAIL_MULTIPLE_LAST_ACTION
    }

    public enum ESActionTypeEnum {
        ADD_ARTIFACT, UPDATE_ARTIFACT, REMOVE_ARTIFACT
    }

    public enum ActionTypeEnum {
        ADD_ARTIFACT, REMOVE_ARTIFACT, UPDATE_ARTIFACT, CREATE_RESOURCE, UPDATE_RESOURCE, DELETE_RESOURCE
    }

    public static final int MAX_SIZE_TRANSACTION_LIST = 1000;

    public static final int TRANSACTION_ID_RESET_LIMIT = MAX_SIZE_TRANSACTION_LIST * 10;

    public static final String TRANSACTION_MARKER_STR = "TRANSACTION_MARKER";

    public static final String DUMMY_USER = "udummy";

    public static final Marker TRANSACTION_MARKER = MarkerFactory.getMarker(TRANSACTION_MARKER_STR);

    public static class LogMessages {
        public static final String PRE_INVOKE_ACTION = "About to add Action to SdncTransaction ID:{} for DB:{}, user:{}, action:{}";
        public static final String INVOKE_ACTION = "invoking Action in SdncTransactionID:{}, on DB:{}, user:{}, action:{}";
        public static final String COMMIT_ACTION_ALL_DB = "Starting Commit for SdncTransactionID:{} for all DBs, user:{}, action:{}";
        public static final String COMMIT_ACTION_SPECIFIC_DB = "Starting Commit for SdncTransactionID:{} on DB:{}, user:{}, action:{}";
        public static final String COMMIT_ON_CLOSED_TRANSACTION = "Commit failed for SdncTransactionID:{} Transaction is in status:{}, user:{}, action:{}";
        public static final String ACTION_ON_CLOSED_TRANSACTION = "Action failed for SdncTransactionID:{} Transaction is already closed, user:{}, action:{}";
        public static final String DOUBLE_FINISH_FLAG_ACTION = "Transaction with SdncTransactionID:{} on DB:{} was called multiple times with last action flag, user:{}, action:{}";
        public static final String DB_ACTION_FAILED_WITH_EXCEPTION = "Action on DB:{} has failed for SdncTransactionID:{}, message:{}, user:{}, action:{}";
        public static final String ROLLBACK_SUCCEEDED_GENERAL = "Transaction rollback succeeded for SdncTransactionID:{}, user:{}, action:{}";
        public static final String ROLLBACK_FAILED_GENERAL = "Transaction rollback failed for SdncTransactionID:{}, user:{}, action:{}";
        public static final String ROLLBACK_FAILED_ON_DB = "Transaction rollback failed for SdncTransactionID:{} on DB:{}, user:{}, action:{}";
        public static final String ROLLBACK_FAILED_ON_DB_WITH_EXCEPTION = "Transaction rollback failed for SdncTransactionID:{} on DB:{}, exception message:{}, user:{}, action:{}";
        public static final String ROLLBACK_PERSISTENT_ACTION = "About To Rollback Action on DB{} for TransactionID:{} ... user:{}, action:{}";
        public static final String ROLLBACK_NON_PERSISTENT_ACTION = "About To Rollback Actions on DB{} for TransactionID:{} ...  user:{}, action:{}";
        public static final String CREATE_ROLLBACK_HANDLER = "creating new Rollback Handler For DB:{} in SdncTransaction ID:{}, user:{}, action:{}";
        public static final String FAILED_CREATE_ROLLBACK = "failed to create new Rollback action For DB:{} with SdncTransactionID:{}, user:{}, action:{}";
    }

}
