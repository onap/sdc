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

package org.openecomp.sdc.ci.tests.datatypes.enums;

public class ErrorInfo {

	private Integer code;
	private String message;
	private String messageId;

	public ErrorInfo() {
		super();
	}

	public ErrorInfo(Integer code, String message, String messageId) {
		super();
		this.code = code;
		this.message = message;
		this.messageId = messageId;
	}

	public ErrorInfo(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public String getMessageAndReplaceVariables(Object... variables) {
		String formatReadyString = message.replaceAll("%[\\d]+", "%s");
		formatReadyString = String.format(formatReadyString, variables);
		return formatReadyString;
	}

	public String getAuditDesc(Object... variables) {
		String messageAndReplaceVariables = getMessageAndReplaceVariables(variables);
		String res;
		if (messageId != null) {
			res = messageId + ": " + messageAndReplaceVariables;
		} else {
			res = messageAndReplaceVariables;
		}
		return res;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public String toString() {
		return "ErrorInfo [code=" + code + ", message=" + message + ", messageId=" + messageId + "]";
	}

}
