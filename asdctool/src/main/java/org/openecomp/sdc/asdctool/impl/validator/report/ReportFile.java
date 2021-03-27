/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Bell Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.asdctool.impl.validator.report;

import java.util.Set;
import org.apache.commons.text.StrBuilder;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

/**
 * Provides business logic in regards to file writing required by the validation tools
 */
public class ReportFile {

    static public TXTFile makeTxtFile(ReportFileWriter<FileType.TXT> writer) {
        writer.writeln("-----------------------Validation Tool Results:-------------------------");
        return new TXTFile(writer);
    }

    static public CSVFile makeCsvFile(ReportFileWriter<FileType.CSV> writer) {
        writer.writeln("Vertex ID,Task Name,Success,Result Details,Result Description");
        return new CSVFile(writer);
    }

    /**
     * Provides csv writing business logic related to {@link org.openecomp.sdc.asdctool.main.ValidationTool}
     */
    public static final class TXTFile extends ReportFile {

        private final ReportFileWriter<FileType.TXT> writer;

        private TXTFile(ReportFileWriter<FileType.TXT> writer) {
            this.writer = writer;
        }

        public void reportStartTaskRun(GraphVertex vertex, String taskName) {
            writer.writeln("");
            writer.writeln("-----------------------Vertex: " + vertex.getUniqueId() + ", Task " + taskName + " Started-----------------------");
        }

        public void reportStartValidatorRun(String validatorName, int componentsNum) {
            writer.writeln("");
            writer.writeln("------ValidatorExecuter " + validatorName + " Validation Started, on " + componentsNum + " components---------");
        }

        public void printValidationTaskStatus(GraphVertex vertexScanned, String taskName, boolean success) {
            String successStatus = success ? "success" : "failed";
            writer.writeln("");
            writer.writeln("-----------------------Vertex: " + vertexScanned.getUniqueId() + ", Task " + taskName + " " + successStatus
                + "-----------------------");
        }

        public void reportValidatorTypeSummary(String validatorName, Set<String> failedTasksNames, Set<String> successTasksNames) {
            StrBuilder sb = new StrBuilder();
            sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
            sb.appendln("Failed tasks: " + failedTasksNames);
            sb.appendln("Success tasks: " + successTasksNames);
            writer.writeln("");
            writer.write(sb.toString());
        }

        public void reportEndOfToolRun(Report report) {
            StrBuilder sb = new StrBuilder();
            sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
            report.forEachFailure((taskName, failedVertices) -> sb.append("Task: ").append(taskName).appendNewLine().append("FailedVertices: ")
                .append(String.valueOf(failedVertices)).appendNewLine());
            writer.writeln("");
            writer.write(sb.toString());
        }

        public void writeReportLineToFile(String message) {
            writer.writeln("");
            writer.write(message);
        }
    }

    /**
     * Provides csv writing business logic related to {@link org.openecomp.sdc.asdctool.main.ValidationTool}
     */
    public static final class CSVFile extends ReportFile {

        private final ReportFileWriter<FileType.CSV> writer;

        private CSVFile(ReportFileWriter<FileType.CSV> writer) {
            this.writer = writer;
        }

        public void printAllResults(Report report) {
            report.forEachSuccess((vertex, task, result) -> {
                String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
                writer.writeln(resultLine);
            });
        }
    }
}
