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

package org.openecomp.sdc.be.dao.neo4j;

public enum Neo4jOperationStatus {

	OK, NOT_CONNECTED, NOT_AUTHORIZED, HTTP_PROTOCOL_ERROR, DB_NOT_AVAILABLE, DB_READ_ONLY, BAD_REQUEST, LEGACY_INDEX_ERROR, SCHEMA_ERROR, TRANSACTION_ERROR, EXECUTION_FAILED, ENTITY_ALREADY_EXIST,

	WRONG_INPUT, GENERAL_ERROR, NOT_SUPPORTED, NOT_FOUND;

	private String originError;
	private String message;
	private String helpErrorMsg;

	private static final String NA = "NA";

	Neo4jOperationStatus() {
		originError = NA;
		message = NA;
		helpErrorMsg = NA;
	}

	public Neo4jOperationStatus setOriginError(String originError) {
		this.originError = originError;
		return this;
	}

	public Neo4jOperationStatus setMessage(String message) {
		if (message != null && !message.isEmpty()) {
			this.message = message;
		}
		return this;
	}

	public Neo4jOperationStatus setHelpErrorMsg(String helpErrorMsg) {
		this.helpErrorMsg = helpErrorMsg;
		return this;
	}

	public String getOriginError() {
		return originError;
	}

	public String getMessage() {
		return message;
	}

	public String getHelpErrorMsg() {
		return helpErrorMsg;
	}

	public String printError() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(toString()).append("-").append(originError).append("-").append(helpErrorMsg).append("-")
				.append(message).append("]");
		return sb.toString();
	}

}
