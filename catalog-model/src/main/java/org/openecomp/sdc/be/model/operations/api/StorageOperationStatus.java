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
package org.openecomp.sdc.be.model.operations.api;

public enum StorageOperationStatus {

    // @formatter:off
    OK,
    CONNECTION_FAILURE,
    BAD_REQUEST,
    ENTITY_ALREADY_EXISTS,
    GRAPH_IS_LOCK,
    GENERAL_ERROR,
    USER_NOT_FOUND,
    PERMISSION_ERROR,
    HTTP_PROTOCOL_ERROR,
    STORAGE_NOT_AVAILABLE,
    READ_ONLY_STORAGE,
    STORAGE_LEGACY_INDEX_ERROR,
    SCHEMA_ERROR,
    TRANSACTION_ERROR,
    EXEUCTION_FAILED,
    NOT_FOUND,
    OPERATION_NOT_SUPPORTED,
    CATEGORY_NOT_FOUND,
    PARENT_RESOURCE_NOT_FOUND,
    MODEL_NOT_FOUND,
    MULTIPLE_PARENT_RESOURCE_FOUND,
    INCONSISTENCY,
    GRAPH_IS_NOT_AVAILABLE,
    SCHEMA_VIOLATION,
    FAILED_TO_LOCK_ELEMENT,
    INVALID_ID,
    MATCH_NOT_FOUND,
    ARTIFACT_NOT_FOUND,
    DISTR_ENVIRONMENT_NOT_AVAILABLE,
    DISTR_ENVIRONMENT_NOT_FOUND,
    DISTR_ENVIRONMENT_SENT_IS_INVALID,
    DISTR_ARTIFACT_NOT_FOUND,
    OVERLOAD,
    INVALID_TYPE,
    INVALID_VALUE,
    INVALID_INNER_TYPE,
    CSAR_NOT_FOUND,
    GROUP_INVALID_CONTENT,
    CANNOT_UPDATE_EXISTING_ENTITY,
    PROPERTY_NAME_ALREADY_EXISTS,
    INVALID_PROPERTY,
    COMPONENT_IS_ARCHIVED,
    COMPONENT_NOT_ARCHIVED,
    COMPONENT_IS_IN_USE,
    DECLARED_INPUT_USED_BY_OPERATION;
    // @formatter:on

}
