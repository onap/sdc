/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

import java.util.Set;

public abstract class ReportFile {

    static public TXTFile makeTxtFile(ReportFileWriter<FileType.TXT> writer) {
        writer.writeln("-----------------------Validation Tool Results:-------------------------");
        return new TXTFile(writer);
    }

    static public CSVFile makeCsvFile(ReportFileWriter<FileType.CSV> writer) {
        writer.writeln("Vertex ID,Task Name,Success,Result Details,Result Description");
        return new CSVFile(writer);
    }

    public static final class TXTFile extends ReportFile {
        private final ReportFileWriter<FileType.TXT> writer;

        private TXTFile(ReportFileWriter<FileType.TXT> writer) {
            this.writer = writer;
        }

        public void printValidationTaskStatus(GraphVertex vertexScanned,
                                              String taskName,
                                              boolean success) {
            ReportWriters.writePrintValidationTaskStatus(vertexScanned, taskName, success, writer);
        }

        public void writeReportLineToFile(String message) {
            ReportWriters.writeReportLineToFile(message, writer);
        }

        public void reportValidatorTypeSummary(
                String validatorName,
                Set<String> failedTasksNames,
                Set<String> successTasksNames
        ) {
            ReportWriters.writeReportValidatorTypeSummary(validatorName, failedTasksNames, successTasksNames, writer);
        }

        public void reportStartValidatorRun(String validatorName, int componentsNum) {
            ReportWriters.writeReportStartValidatorRun(validatorName, componentsNum, writer);
        }

        public void reportStartTaskRun(GraphVertex vertex, String taskName) {
            ReportWriters.writeReportStartTaskRun(vertex, taskName, writer);
        }

        public void reportEndOfToolRun(Report report) {
            ReportWriters.writeValidationSummary(report, writer);
        }

    }

    public static final class CSVFile extends ReportFile {
        private final ReportFileWriter<FileType.CSV> writer;

        private CSVFile(ReportFileWriter<FileType.CSV> writer) {
            this.writer = writer;
        }

        public void printAllResults(Report report) {
            ReportWriters.writeResults(report, writer);
        }
    }
}
