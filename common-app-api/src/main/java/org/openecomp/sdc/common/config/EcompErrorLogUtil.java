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
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.enums.EcompErrorSeverity;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.slf4j.MDC;

public class EcompErrorLogUtil {

    private static final String FATAL_ERROR_PREFIX = "FATAL ERROR!! ";
    private static final String ECOMP_ERROR_TMPL = "ETYPE = \"%s\" ENAME = \"%s\" ECODE = \"%s\" ECONTEXT = \"%s\" EDESC = \"%s\"";
    private static final Logger log = Logger.getLogger(EcompErrorLogUtil.class.getName());

    private EcompErrorLogUtil() {

    }

    public static void logEcompError(EcompErrorName ecompErrorName, EcompErrorInfo ecompErrorInfo, String ecompErrorContext) {
        if (ecompErrorInfo != null) {
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            try {
                String description = ecompErrorInfo.getDescription();
                String severityStr = ecompErrorInfo.getSeverity();
                EcompErrorSeverity severity = EcompErrorSeverity.ERROR;
                // Since there is no FATAL log level, this is how we distinguish

                // the FATAL errors
                if (severityStr.equals(EcompErrorSeverity.FATAL.name())) {
                    description = FATAL_ERROR_PREFIX + description;
                    severity = EcompErrorSeverity.FATAL;
                } else if (severityStr.equals(EcompErrorSeverity.WARN.name())) {
                    severity = EcompErrorSeverity.WARN;
                } else if (severityStr.equals(EcompErrorSeverity.INFO.name())) {
                    severity = EcompErrorSeverity.INFO;
                }
                MDC.put("alarmSeverity", ecompErrorInfo.getAlarmSeverity());
                // else it stays ERROR
                formatter.format(ECOMP_ERROR_TMPL, ecompErrorInfo.getType(), ecompErrorName.name(), ecompErrorInfo.getCode(), ecompErrorContext,
                    description);
                log.error(severity, EcompLoggerErrorCode.getByValue(ecompErrorInfo.getCode()), ecompErrorContext, ecompErrorContext, description);
            } finally {
                formatter.close();
                MDC.remove("alarmSeverity");
            }
        }
    }

    public static void logEcompError(String ecompErrorContext, EcompErrorEnum ecompErrorEnum, String... ecompDescriptionParams) {
        logEcompError(ecompErrorContext, ecompErrorEnum, true, ecompDescriptionParams);
    }

    public static void logEcompError(String ecompErrorContext, EcompErrorEnum ecompErrorEnum, boolean logMissingParams,
                                     String... ecompDescriptionParams) {
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, Locale.US)) {
            Optional<String> descriptionParamsOptional = getDescriptionParams(ecompErrorEnum, ecompDescriptionParams);
            if (descriptionParamsOptional.isEmpty()) {
                EcompErrorEnum mismatchErrorEnum = EcompErrorEnum.EcompMismatchParam;
                if (logMissingParams) {
                    logEcompError("logEcompError", mismatchErrorEnum, false, ecompErrorEnum.name());
                } else {
                    log.info("Failed to log the error code {}", mismatchErrorEnum);
                }
                return;
            }
            String description = descriptionParamsOptional.get();
            EcompClassification classification = ecompErrorEnum.getClassification();
            EcompErrorSeverity severity = EcompErrorSeverity.ERROR;
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
            MDC.put("alarmSeverity", ecompErrorEnum.getAlarmSeverity().name());
            // else it stays ERROR
            formatter.format(ECOMP_ERROR_TMPL, ecompErrorEnum.getEType(), ecompErrorEnum.name(), eCode, ecompErrorContext, description);
            log.error(severity, EcompLoggerErrorCode.getByValue(ecompErrorEnum.getEcompErrorCode().name()), ecompErrorContext, ecompErrorContext,
                description);
        } finally {
            MDC.remove("alarmSeverity");
        }
    }

    public static String createEcode(EcompErrorEnum ecompErrorEnum) {
        EcompClassification classification = ecompErrorEnum.getClassification();
        String ecompErrorCode = ecompErrorEnum.getEcompErrorCode().name();
        String ecodeNumber = ecompErrorCode.substring(ecompErrorCode.indexOf("_") + 1);
        return "ASDC" + ecodeNumber + classification.getClassification();
    }

    private static Optional<String> getDescriptionParams(final EcompErrorEnum ecompErrorEnum, final String... descriptionParams) {
        final String description = ecompErrorEnum.getEcompErrorCode().getDescription();
        // Counting number of params in description
        final int countMatches = StringUtils.countMatches(description, AbsEcompErrorManager.PARAM_STR);
        // Catching cases when there are more params passed than there are in the description (formatter will ignore extra params and won't throw
        // exception)
        if (countMatches != descriptionParams.length) {
            return Optional.empty();
        }
        // Setting params of the description if any
        final StringBuilder sb = new StringBuilder();
        try (final Formatter formatter = new Formatter(sb, Locale.US)) {
            formatter.format(description, (Object[]) descriptionParams).toString();
            return Optional.of(formatter.toString());
        } catch (final IllegalFormatException ignored) {
            // Number of passed params doesn't match number of params in config file
            return Optional.empty();
        }
    }
}
