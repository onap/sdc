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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
<<<<<<< HEAD
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
=======
>>>>>>> c39a149bb... Enforce state encapsulation in Report
import java.util.Set;

/**
 * Created by chaya on 7/5/2017.
 */
public class ReportManager {

    private final static Logger log = LoggerFactory.getLogger(ReportManager.class);

    public static ReportManager make(String csvReportFilePath, String outputFullFilePath) {
        return new ReportManager(csvReportFilePath, outputFullFilePath);
    }
    private ReportManager(String csvReportFilePath, String reportOutputFilePath) {
        try {
            initReportFile(reportOutputFilePath);
            initCsvFile(csvReportFilePath);
        } catch (IOException e) {
            log.info("Init file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    private void initReportFile(String reportOutputFilePath) throws IOException {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Validation Tool Results:-------------------------");
        Files.write(Paths.get(reportOutputFilePath), sb.toString().getBytes());
    }

    private void initCsvFile(String csvReportFilePath) throws IOException {
        StrBuilder sb = new StrBuilder();
        sb.append("Vertex ID,Task Name,Success,Result Details,Result Description");
        sb.appendNewLine();
        Files.write(Paths.get(csvReportFilePath), sb.toString().getBytes());
    }

    public static void printValidationTaskStatus(Report report, GraphVertex vertexScanned, String taskName, boolean success) {
        String successStatus = success ? "success" : "failed";
        String line = "-----------------------Vertex: "+vertexScanned.getUniqueId()+", Task " + taskName + " " +successStatus+"-----------------------";
        StrBuilder sb = new StrBuilder();
        sb.appendln(line);
        writeReportLineToFile(report, line);
    }

    public static void writeReportLineToFile(Report report, String message) {
        try {
            Files.write(Paths.get(report.getTxtReportFilePath()), new StrBuilder().appendNewLine().toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(report.getTxtReportFilePath()), message.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    public static void reportValidatorTypeSummary(
            Report report,
            String validatorName,
            Set<String> failedTasksNames,
            Set<String> successTasksNames
    ){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: "+ failedTasksNames);
        sb.appendln("Success tasks: "+ successTasksNames);
        writeReportLineToFile(report, sb.toString());
    }

    public static void reportStartValidatorRun(Report report, String validatorName, int componenentsNum) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("------ValidatorExecuter " + validatorName + " Validation Started, on "+componenentsNum+" components---------");
        writeReportLineToFile(report, sb.toString());
    }

    public static void reportStartTaskRun(Report report, GraphVertex vertex, String taskName){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Vertex: "+vertex.getUniqueId()+", Task " + taskName + " Started-----------------------");
        writeReportLineToFile(report, sb.toString());
    }

    public static void reportEndOfToolRun(Report report) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
        report.forEachFailedVertices((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writeReportLineToFile(report, sb.toString());
        printAllResults(report);
    }

    public static void printAllResults(Report report) {
        report.forEachResult((vertex, task, result) -> {
            try {
                String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
                Files.write(Paths.get(report.getCsvReportFilePath()), resultLine.getBytes(),
                        StandardOpenOption.APPEND);
                Files.write(Paths.get(report.getCsvReportFilePath()),
                        new StrBuilder().appendNewLine().toString().getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
<<<<<<< HEAD
=======
                e.printStackTrace();
>>>>>>> c39a149bb... Enforce state encapsulation in Report
                log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
            }
        });
    }
}
