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

package org.openecomp.sdc.exception;

import java.util.Arrays;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSdncException {

	private String messageId;

	private String text;

	private String[] variables;

	private static Logger log = LoggerFactory.getLogger(AbstractSdncException.class.getName());

	private final static Pattern ERROR_PARAM_PATTERN = Pattern.compile("%\\d");

	public AbstractSdncException() {
	}

	public AbstractSdncException(String messageId, String text, String[] variables) {
		super();
		this.messageId = messageId;
		this.text = text;
		this.variables = validateParameters(messageId, text, variables);
	}

	private String[] validateParameters(String messageId, String text, String[] variables) {
		String[] res = null;
		Matcher m = ERROR_PARAM_PATTERN.matcher(text);
		int expectedParamsNum = 0;
		while (m.find()) {
			expectedParamsNum += 1;
		}
		int actualParamsNum = (variables != null) ? variables.length : 0;
		if (actualParamsNum < expectedParamsNum) {
			log.warn(
					"Received less parameters than expected for error with messageId {}, expected: {}, actual: {}. Missing parameters are padded with null values.",
					messageId, expectedParamsNum, actualParamsNum);
		} else if (actualParamsNum > expectedParamsNum) {
			log.warn(
					"Received more parameters than expected for error with messageId {}, expected: {}, actual: {}. Extra parameters are ignored.",
					messageId, expectedParamsNum, actualParamsNum);
		}
		if (variables != null) {
			res = Arrays.copyOf(variables, expectedParamsNum);
		}

		return res;
	}

	public String getMessageId() {
		return this.messageId;
	}

	public String getText() {
		return text;
	}

	public String[] getVariables() {
		return variables;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public String getFormattedErrorMessage() {
		String res;
		if (variables != null && variables.length > 0) {
			Formatter formatter = new Formatter();
			try {
				res = formatter.format(this.text.replaceAll("%\\d", "%s"), (Object[]) this.variables).toString();
			} finally {
				formatter.close();
			}
		} else {
			res = this.text;
		}
		return res;
	}

}
