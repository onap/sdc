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

package org.openecomp.sdc.be.dao.janusgraph;

public enum JanusGraphOperationStatus {
	OK, NOT_CONNECTED, NOT_CREATED, INDEX_CANNOT_BE_CHANGED, NOT_FOUND, MISSING_UNIQUE_ID, MISSING_NODE_LABEL, MULTIPLE_EDGES_WITH_SAME_LABEL, CANNOT_DELETE_NON_LEAF_NODE, MULTIPLE_NODES_WITH_SAME_ID, GRAPH_IS_NOT_AVAILABLE, JANUSGRAPH_CONFIGURATION, JANUSGRAPH_SCHEMA_VIOLATION, INVALID_ELEMENT, INVALID_QUERY, INVALID_ID, RESOURCE_UNAVAILABLE, ILLEGAL_ARGUMENT, ALREADY_LOCKED, ALREADY_EXIST, MULTIPLE_CHILDS_WITH_SAME_EDGE, GENERAL_ERROR, MATCH_NOT_FOUND, INVALID_TYPE, PROPERTY_NAME_ALREADY_EXISTS, INVALID_PROPERTY,

}
