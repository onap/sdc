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

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.config.EcompErrorConfiguration.EcompErrorSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import fj.data.Either;

public class EcompErrorLogUtil {

	private static String ECOMP_ERROR_TMPL = "ETYPE = \"%s\" ENAME = \"%s\" ECODE = \"%s\" ECONTEXT = \"%s\" EDESC = \"%s\"";
	private static Logger log = LoggerFactory.getLogger(EcompErrorLogUtil.class.getName());
	private static final String FATAL_ERROR_PREFIX = "FATAL ERROR!! ";

	public static void logEcompError(EcompErrorName ecompErrorName, EcompErrorInfo ecompErrorInfo,
			String ecompErrorContext, String... ecompDescriptionParams) {
		if (ecompErrorInfo != null) {
			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb, Locale.US);
			try {
				String description = ecompErrorInfo.getDescription();
				String severityStr = ecompErrorInfo.getSeverity();
				EcompErrorConfiguration.EcompErrorSeverity severity = EcompErrorSeverity.ERROR;
				// Since there is no FATAL log level, this is how we distinguish
				// the FATAL errors
				if (severityStr.equals(EcompErrorConfiguration.EcompErrorSeverity.FATAL.name())) {
					description = FATAL_ERROR_PREFIX + description;
					severity = EcompErrorSeverity.FATAL;
				} else if (severityStr.equals(EcompErrorConfiguration.EcompErrorSeverity.WARN.name())) {
					severity = EcompErrorSeverity.WARN;
				} else if (severityStr.equals(EcompErrorConfiguration.EcompErrorSeverity.INFO.name())) {
					severity = EcompErrorSeverity.INFO;
				}

				MDC.put("alarmSeverity", ecompErrorInfo.getAlarmSeverity());
				// else it stays ERROR
				formatter.format(ECOMP_ERROR_TMPL, ecompErrorInfo.getType(), ecompErrorName.name(),
						ecompErrorInfo.getCode(), ecompErrorContext, description);
				switch (severity) {
				case INFO:
					log.info(formatter.toString());
					break;
				case WARN:
					log.warn(formatter.toString());
					break;
				case ERROR:
					log.error(formatter.toString());
					break;
				case FATAL:
					// same as ERROR for now, might be additional logic later..
					log.error(formatter.toString());
					break;
				default:
					break;
				}
			} finally {
				formatter.close();
				MDC.remove("alarmSeverity");
			}
		}
	}

	public static void logEcompError(String ecompErrorContext, EcompErrorEnum ecompErrorEnum,
			String... ecompDescriptionParams) {
		logEcompError(ecompErrorContext, ecompErrorEnum, true, ecompDescriptionParams);
	}

	public static void logEcompError(String ecompErrorContext, EcompErrorEnum ecompErrorEnum, boolean logMissingParams,
			String... ecompDescriptionParams) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		try {
			String description = ecompErrorEnum.getEcompErrorCode().getDescription();

			Either<String, Boolean> setDescriptionParamsResult = setDescriptionParams(ecompErrorEnum,
					ecompDescriptionParams);
			if (setDescriptionParamsResult.isLeft()) {
				description = setDescriptionParamsResult.left().value();
			} else {
				EcompErrorEnum mismatchErrorEnum = EcompErrorEnum.EcompMismatchParam;
				if (logMissingParams == true) {
					logEcompError("logEcompError", mismatchErrorEnum, false, ecompErrorEnum.name().toString());
					return;
				} else {
					log.info("Failed to log the error code {}", mismatchErrorEnum);
					return;
				}
			}
			EcompClassification classification = ecompErrorEnum.getClassification();

			EcompErrorConfiguration.EcompErrorSeverity severity = EcompErrorSeverity.ERROR;
			// Since there is no FATAL log level, this is how we distinguish the
			// FATAL errors
			if (classification == EcompClassification.FATAL) {
				description = FATAL_ERROR_PREFIX + description;
				severity = EcompErrorSeverity.FATAL;
			} else if (classification == EcompClassification.WARNING) {
				severity = EcompErrorSeverity.WARN;
			} else if (classification == EcompClassification.INFORMATION) {
				severity = EcompErrorSeverity.INFO;
			}

			String eCode = createEcode(ecompErrorEnum);

			MDC.put("alarmSeverity", ecompErrorEnum.alarmSeverity.name());
			// else it stays ERROR
			formatter.format(ECOMP_ERROR_TMPL, ecompErrorEnum.geteType(), ecompErrorEnum.name(), eCode,
					ecompErrorContext, description);
			switch (severity) {
			case INFO:
				log.info(formatter.toString());
				break;
			case WARN:
				log.warn(formatter.toString());
				break;
			case ERROR:
				log.error(formatter.toString());
				break;
			case FATAL:
				// same as ERROR for now, might be additional logic later..
				log.error(formatter.toString());
				break;
			default:
				break;
			}
		} finally {
			formatter.close();
			MDC.remove("alarmSeverity");
		}
	}

	public static String createEcode(EcompErrorEnum ecompErrorEnum) {

		EcompClassification classification = ecompErrorEnum.getClassification();
		String ecompErrorCode = ecompErrorEnum.getEcompErrorCode().name();

		String ecodeNumber = ecompErrorCode.substring(ecompErrorCode.indexOf("_") + 1);
		String eCode = "ASDC" + ecodeNumber + classification.getClassification();

		return eCode;
	}

	private static Either<String, Boolean> setDescriptionParams(EcompErrorEnum ecompErrorEnum,
			String... descriptionParams) {
		String description = ecompErrorEnum.getEcompErrorCode().getDescription();

		// Counting number of params in description
		int countMatches = StringUtils.countMatches(description, AbsEcompErrorManager.PARAM_STR);
		// Catching cases when there are more params passed than there are in
		// the description (formatter will ignore extra params and won't throw
		// exception)
		if (countMatches != descriptionParams.length) {
			return Either.right(false);
		}
		// Setting params of the description if any
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		try {
			formatter.format(description, (Object[]) descriptionParams).toString();
			return Either.left(formatter.toString());
		} catch (IllegalFormatException e) {
			// Number of passed params doesn't match number of params in config
			// file
			return Either.right(false);
		} finally {
			formatter.close();
		}

	}

}
