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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import org.apache.commons.lang.text.StrBuilder;
import org.openecomp.sdc.asdctool.impl.validator.report.Report;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportManager {

    private static final Logger log = LoggerFactory.getLogger(ReportManager.class);

    public static ReportManager make(String csvReportFilePath, String txtReportFilePath) {
        return new ReportManager(csvReportFilePath, txtReportFilePath);
    }

    private ReportManager(String csvReportFilePath, String txtReportFilePath) {
        try {
            initCsvFile(csvReportFilePath);
            initReportFile(txtReportFilePath);
        } catch (IOException e) {
            log.info("Init file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    private void initReportFile(String txtReportFilePath) throws IOException {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Validation Tool Results:-------------------------");
        Files.write(Paths.get(txtReportFilePath), sb.toString().getBytes());
    }

    private void initCsvFile(String csvReportFilePath) throws IOException {
        StrBuilder sb = new StrBuilder();
        sb.append("Vertex ID,Task Name,Success,Result Details,Result Description");
        sb.appendNewLine();
        Files.write(Paths.get(csvReportFilePath), sb.toString().getBytes());
    }

    public static void printValidationTaskStatus(GraphVertex vertexScanned, String taskName, boolean success,
        String outputFilePath) {
        String successStatus = success ? "success" : "failed";
        String line =
            "-----------------------Vertex: " + vertexScanned.getUniqueId() + ", Task " + taskName + " " + successStatus
                + "-----------------------";
        StrBuilder sb = new StrBuilder();
        sb.appendln(line);
        writeReportLineToFile(line, outputFilePath);
    }

    public static void writeReportLineToFile(String message, String outputFilePath) {
        try {
            Files.write(Paths.get(outputFilePath), new StrBuilder().appendNewLine().toString().getBytes(),
                StandardOpenOption.APPEND);
            Files.write(Paths.get(outputFilePath), message.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    public static void reportValidatorTypeSummary(String validatorName, Set<String> failedTasksNames,
        Set<String> successTasksNames, String outputFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln(
            "-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: " + failedTasksNames);
        sb.appendln("Success tasks: " + successTasksNames);
        writeReportLineToFile(sb.toString(), outputFilePath);
    }

    public static void reportStartValidatorRun(String validatorName, int componenentsNum, String outputFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("------ValidatorExecuter " + validatorName + " Validation Started, on " + componenentsNum
            + " components---------");
        writeReportLineToFile(sb.toString(), outputFilePath);
    }

    public static void reportStartTaskRun(GraphVertex vertex, String taskName, String outputFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Vertex: " + vertex.getUniqueId() + ", Task " + taskName
            + " Started-----------------------");
        writeReportLineToFile(sb.toString(), outputFilePath);
    }

    public static void reportEndOfToolRun(Report report, String csvReportFilePath, String outputFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
        report.forEachFailure((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writeReportLineToFile(sb.toString(), outputFilePath);
        printAllResults(report, csvReportFilePath);
    }

    public static void printAllResults(Report report, String csvReportFilePath) {
        report.forEachSuccess((vertex, task, result) -> {
            try {
                String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
                Files.write(Paths.get(csvReportFilePath), resultLine.getBytes(),
                    StandardOpenOption.APPEND);
                Files.write(Paths.get(csvReportFilePath),
                    new StrBuilder().appendNewLine().toString().getBytes(),
                    StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
            }
        });
    }
}
