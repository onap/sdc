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
import java.util.*;

import org.apache.commons.lang.text.StrBuilder;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chaya on 7/5/2017.
 */
public class ReportManager {

    private static Logger log = LoggerFactory.getLogger(ReportManager.class);
    private static String reportOutputFilePath;
    private static String csvReportFilePath;
    private static Map<String, Set<String>> failedVerticesPerTask = new HashMap<>();
    private static Map<String, Map<String, VertexResult>> resultsPerVertex = new HashMap<>();

    public ReportManager() {
        try {
            initCsvFile();
            initReportFile();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("Init file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    private void initReportFile() throws IOException {
        reportOutputFilePath = ValidationConfigManager.getOutputFullFilePath();
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Validation Tool Results:-------------------------");
        Files.write(Paths.get(reportOutputFilePath), sb.toString().getBytes());
    }

    private void initCsvFile() throws IOException {
        csvReportFilePath = ValidationConfigManager.getCsvReportFilePath();
        StrBuilder sb = new StrBuilder();
        sb.append("Vertex ID,Task Name,Success,Result Details,Result Description");
        sb.appendNewLine();
        Files.write(Paths.get(csvReportFilePath), sb.toString().getBytes());
    }

    public static void reportTaskEnd(String vertexId, String taskName, VertexResult result) {
        Map<String, VertexResult> vertexTasksResults =
                Optional.ofNullable(resultsPerVertex.get(vertexId)).orElse(new HashMap<>());
        vertexTasksResults.put(taskName, result);
        resultsPerVertex.put(vertexId, vertexTasksResults);
    }

    public static void addFailedVertex (String taskName, String vertexId) {
        Set<String> failedVertices = failedVerticesPerTask.get(taskName);
        if (failedVertices == null) {
            failedVertices = new HashSet<>();
        }
        failedVertices.add(vertexId);
        failedVerticesPerTask.put(taskName, failedVertices);
    }

    public static void printValidationTaskStatus(GraphVertex vertexScanned, String taskName, boolean success) {
        String successStatus = success ? "success" : "failed";
        String line = "-----------------------Vertex: "+vertexScanned.getUniqueId()+", Task " + taskName + " " +successStatus+"-----------------------";
        StrBuilder sb = new StrBuilder();
        sb.appendln(line);
        writeReportLineToFile(line);
    }

    public static void writeReportLineToFile(String message) {
        try {
            Files.write(Paths.get(reportOutputFilePath), new StrBuilder().appendNewLine().toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(reportOutputFilePath), message.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    public static void reportValidatorTypeSummary(String validatorName, Set<String> failedTasksNames, Set<String> successTasksNames){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: "+ failedTasksNames);
        sb.appendln("Success tasks: "+ successTasksNames);
        writeReportLineToFile(sb.toString());
    }

    public static void reportStartValidatorRun(String validatorName, int componenentsNum) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("------ValidatorExecuter " + validatorName + " Validation Started, on "+componenentsNum+" components---------");
        writeReportLineToFile(sb.toString());
    }

    public static void reportStartTaskRun(GraphVertex vertex, String taskName){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Vertex: "+vertex.getUniqueId()+", Task " + taskName + " Started-----------------------");
        writeReportLineToFile(sb.toString());
    }

    public static void reportEndOfToolRun() {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
        failedVerticesPerTask.forEach((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writeReportLineToFile(sb.toString());
        printAllResults();
    }

    public static void printAllResults() {
        resultsPerVertex.forEach((vertex, tasksResults) -> tasksResults.forEach((task, result) -> {
            try {
                String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
                Files.write(Paths.get(csvReportFilePath), resultLine.getBytes(),
                    StandardOpenOption.APPEND);
                Files.write(Paths.get(csvReportFilePath),
                    new StrBuilder().appendNewLine().toString().getBytes(),
                    StandardOpenOption.APPEND);
            } catch (IOException e) {
                    e.printStackTrace();
                log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
            }
        }));
    }
}
