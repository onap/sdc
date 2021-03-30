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
package org.openecomp.sdc.common.config.generation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.config.EcompErrorEnum.AlarmSeverity;
import org.openecomp.sdc.common.config.EcompErrorEnum.ErrorType;
import org.openecomp.sdc.common.config.EcompErrorLogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateEcompErrorsCsv {

    private static final Logger log = LoggerFactory.getLogger(GenerateEcompErrorsCsv.class);
    private static final String DATE_FORMAT = "dd-M-yyyy-hh-mm-ss";
    private static final String NEW_LINE = System.getProperty("line.separator");

    public boolean generateEcompErrorsCsvFile(String targetFolder, final boolean addTimeToFileName) {
        targetFolder += File.separator;
        boolean result = false;
        String dateFormatted = "";
        if (addTimeToFileName) {
            final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            final Date date = new Date();
            dateFormatted = "." + dateFormat.format(date);
        }
        final String outputFile = targetFolder + "ecompErrorCodes" + dateFormatted + ".csv";
        try (final FileWriter writer = new FileWriter(outputFile)) {
            writeHeaders(writer);
            for (final EcompErrorEnum ecompErrorEnum : EcompErrorEnum.values()) {
                final EcompErrorRow ecompErrorRow = new EcompErrorRow();
                final String errorCode = EcompErrorLogUtil.createEcode(ecompErrorEnum);
                final EcompErrorEnum clearCodeEnum = ecompErrorEnum.getClearCode();
                String cleanErrorCode = null;
                if (clearCodeEnum != null) {
                    cleanErrorCode = EcompErrorLogUtil.createEcode(clearCodeEnum);
                }
                ecompErrorRow.setAlarmSeverity(ecompErrorEnum.getAlarmSeverity());
                ecompErrorRow.setCleanErrorCode(cleanErrorCode);
                ecompErrorRow.setDescription(ecompErrorEnum.getEcompErrorCode().getDescription());
                ecompErrorRow.setErrorCode(errorCode);
                ecompErrorRow.setErrorName(ecompErrorEnum.name());
                ecompErrorRow.setErrorType(ecompErrorEnum.getEType());
                ecompErrorRow.setResolution(ecompErrorEnum.getEcompErrorCode().getResolution());
                writer.append(addInvertedCommas(ecompErrorRow.getErrorCode()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getErrorType().toString()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getDescription()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getResolution()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getAlarmSeverity().toString()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getErrorName()));
                writer.append(',');
                writer.append(addInvertedCommas(ecompErrorRow.getCleanErrorCode()));
                writer.append(NEW_LINE);
            }
            result = true;
        } catch (final Exception e) {
            log.warn("generate Ecomp Errors Csv File failed", e);
        }
        return result;
    }

    private void writeHeaders(final FileWriter writer) throws IOException {
        writer.append("\"ERROR CODE\"");
        writer.append(',');
        writer.append("\"ERROR TYPE\"");
        writer.append(',');
        writer.append("\"DESCRIPTION\"");
        writer.append(',');
        writer.append("\"RESOLUTION\"");
        writer.append(',');
        writer.append("\"ALARM SEVERITY\"");
        writer.append(',');
        writer.append("\"ERROR NAME\"");
        writer.append(',');
        writer.append("\"CLEAN CODE\"");
        writer.append(NEW_LINE);
    }

    private String addInvertedCommas(final String str) {
        if (str == null) {
            return "\"\"";
        }
        return "\"" + str + "\"";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private class EcompErrorRow {

        private String errorName;
        private String errorCode;
        private String description;
        private ErrorType errorType;
        private AlarmSeverity alarmSeverity;
        private String cleanErrorCode;
        private String resolution;

    }
}
