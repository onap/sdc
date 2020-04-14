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

import org.apache.commons.lang.text.StrBuilder;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

import java.util.Set;

public final class ReportWriters {

    private ReportWriters() {
    }

    private static final String SUMMARY = "-----------------------------------Validator Tool Summary-----------------------------------";

    public static void writeValidationSummary(Report report, ReportFileWriter<?> writer) {
        StrBuilder sb = new StrBuilder();
        sb.appendln(SUMMARY);
        report.forEachFailedVertices((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writer.writeln("");
        writer.write(sb.toString());
    }

    public static void writeResults(Report report, ReportFileWriter<?> writer) {
        report.forEachResult((vertex, task, result) -> {
            String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
            writer.writeln(resultLine);
        });
    }

    public static void writeReportStartTaskRun(GraphVertex vertex, String taskName, ReportFileWriter<?> writer) {
        writer.writeln("");
        writer.writeln("-----------------------Vertex: " + vertex.getUniqueId() + ", Task " + taskName + " Started-----------------------");
    }

    public static void writeReportStartValidatorRun(String validatorName, int componenentsNum, ReportFileWriter<?> writer) {
        writer.writeln("");
        writer.writeln("------ValidatorExecuter " + validatorName + " Validation Started, on " + componenentsNum + " components---------");
    }

    public static void writeReportValidatorTypeSummary(String validatorName,
                                                       Set<String> failedTasksNames,
                                                       Set<String> successTasksNames,
                                                       ReportFileWriter<?> writer) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: "+ failedTasksNames);
        sb.appendln("Success tasks: "+ successTasksNames);
        writer.writeln("");
        writer.write(sb.toString());
    }

    public static void writePrintValidationTaskStatus(GraphVertex vertexScanned,
                                                      String taskName,
                                                      boolean success,
                                                      ReportFileWriter<?> writer) {
        String successStatus = success ? "success" : "failed";
        String line = "-----------------------Vertex: " + vertexScanned.getUniqueId() + ", Task " + taskName + " " + successStatus + "-----------------------";
        writer.writeln("");
        writer.writeln(line);
    }

    public static void writeReportLineToFile(String message, ReportFileWriter<?> writer) {
        writer.writeln("");
        writer.write(message);
    }
}
