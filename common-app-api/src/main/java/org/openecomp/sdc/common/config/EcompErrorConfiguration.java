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

package org.openecomp.sdc.common.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class EcompErrorConfiguration extends BasicConfiguration {

	private Map<String, EcompErrorInfo> errors = new HashMap<>();
	static final String ECODE_PREFIX = "ASDC_";
	private static final Pattern ECODE_PATTERN = Pattern.compile("^" + ECODE_PREFIX + "\\d{4}$");

	public Map<String, EcompErrorInfo> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, EcompErrorInfo> errors) {
		// Validating ecomp-error-configuration.yaml
		for (Map.Entry<String, EcompErrorInfo> ecompErrorInfo : errors.entrySet()) {
			String ecompErrorName = ecompErrorInfo.getKey();
			EcompErrorInfo res = validateEcompErrorInfo(ecompErrorName, ecompErrorInfo.getValue());
			if (res != null) {
				// Validation failed!
				EcompErrorLogUtil.logEcompError(EcompErrorName.EcompConfigFileFormat, res, this.getClass().getName());
				return;
			}
		}
		// Validation passed
		this.errors = errors;
	}

	public EcompErrorInfo getEcompErrorInfo(String key) {
		EcompErrorInfo clone = null;
		EcompErrorInfo other = errors.get(key);
		if (other != null) {
			clone = new EcompErrorInfo();
			clone.cloneData(other);
		}
		return clone;
	}

	protected EcompErrorInfo validateEcompErrorInfo(String ecompErrorName, EcompErrorInfo ecompErrorInfoToValidate) {
		if (ecompErrorInfoToValidate == null) {
			return getErrorInfoForConfigFile("error " + ecompErrorName + " not found ");
		}
		String type = ecompErrorInfoToValidate.getType();
		if (type == null) {
			return getErrorInfoForConfigFile("empty error type for error " + ecompErrorName
					+ ", value should be one of the following: " + Arrays.asList(EcompErrorType.values()));
		}
		try {
			EcompErrorType.valueOf(type);
		} catch (IllegalArgumentException e) {
			return getErrorInfoForConfigFile("error type " + type + " is invalid for error " + ecompErrorName
					+ ", value should be one of the following: " + Arrays.asList(EcompErrorType.values()));
		}

		String severity = ecompErrorInfoToValidate.getSeverity();
		if (severity == null) {
			return getErrorInfoForConfigFile("empty error severity for error " + ecompErrorName
					+ ", value should be one of the following: " + Arrays.asList(EcompErrorSeverity.values()));
		}
		try {
			EcompErrorSeverity.valueOf(severity);
		} catch (IllegalArgumentException e) {
			return getErrorInfoForConfigFile("error severity " + severity + " is invalid for error " + ecompErrorName
					+ ", value should be one of the following: " + Arrays.asList(EcompErrorSeverity.values()));
		}
		String alarmSeverity = ecompErrorInfoToValidate.getAlarmSeverity();
		if (alarmSeverity == null) {
			return getErrorInfoForConfigFile("empty error alarm for error " + ecompErrorName
					+ ", , value should be one of the following: " + Arrays.asList(EcompAlarmSeverity.values()));
		}
		try {
			EcompAlarmSeverity.valueOf(alarmSeverity);
		} catch (IllegalArgumentException e) {
			return getErrorInfoForConfigFile("error alarm severity " + alarmSeverity + " is invalid for error "
					+ ecompErrorName + ", , value should be one of the following: "
					+ Arrays.asList(EcompAlarmSeverity.values()));
		}

		String code = ecompErrorInfoToValidate.getCode();
		if (code != null && ECODE_PATTERN.matcher(code).matches()) {
			String[] split = code.split("_");
			int parseInt = Integer.parseInt(split[1]);
			if (parseInt < 3010 || parseInt > 9999) {
				return getErrorInfoForInvalidCode(code, ecompErrorName);
			}
		} else {
			return getErrorInfoForInvalidCode(code, ecompErrorName);
		}
		return null;
	}

	private EcompErrorInfo getErrorInfoForInvalidCode(String code, String ecompErrorName) {
		return getErrorInfoForConfigFile("error code " + code + " is invalid for error " + ecompErrorName
				+ ", should be in format ASDC_[3010-9999]");
	}

	private EcompErrorInfo getErrorInfoForConfigFile(String errorMessage) {
		EcompErrorInfo ecompErrorInfo = new EcompErrorInfo();
		ecompErrorInfo.setCode(ECODE_PREFIX + "3000");
		ecompErrorInfo.setType(EcompErrorType.CONFIG_ERROR.name());
		ecompErrorInfo.setSeverity(EcompErrorSeverity.FATAL.name());
		ecompErrorInfo.setAlarmSeverity(EcompAlarmSeverity.CRITICAL.name());
		ecompErrorInfo.setDescription(errorMessage);
		return ecompErrorInfo;
	}

	@Override
	public String toString() {
		return "EcompErrorConfiguration [errors=" + errors + "]";
	}

	/*******************************
	 * Enums
	 */

	public enum EcompErrorType {
		RECOVERY, CONFIG_ERROR, SYSTEM_ERROR, DATA_ERROR, CONNECTION_PROBLEM, AUTHENTICATION_PROBLEM;
	}

	public enum EcompAlarmSeverity {
		CRITICAL, MAJOR, MINOR, INFORMATIONAL, NONE;
	}

	public enum EcompErrorSeverity {
		INFO, WARN, ERROR, FATAL;
	}

	public enum EcompErrorSeverityPrefix {
		I, W, E, F;
	}

	public static void main(String[] args) {
		System.out.println(Arrays.asList(EcompErrorType.values()));
	}
}
