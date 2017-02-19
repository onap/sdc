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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;

public class CambriaErrorResponse {

	public static final int HTTP_OK = 200;

	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	CambriaOperationStatus operationStatus;
	Integer httpCode;
	List<String> variables = new ArrayList<String>();

	public CambriaErrorResponse() {
		super();
	}

	public CambriaErrorResponse(CambriaOperationStatus operationStatus) {
		super();
		this.operationStatus = operationStatus;
	}

	public CambriaErrorResponse(CambriaOperationStatus operationStatus, Integer httpCode) {
		super();
		this.operationStatus = operationStatus;
		this.httpCode = httpCode;
	}

	public CambriaOperationStatus getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(CambriaOperationStatus operationStatus) {
		this.operationStatus = operationStatus;
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(Integer httpCode) {
		this.httpCode = httpCode;
	}

	public void addVariable(String variable) {
		variables.add(variable);
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		return "CambriaErrorResponse [operationStatus=" + operationStatus + ", httpCode=" + httpCode + ", variables=" + variables + "]";
	}

}
