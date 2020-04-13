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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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

    public static void reportTaskEnd(Map<String, Map<String, VertexResult>> resultsPerVertex, String vertexId, String taskName, VertexResult result) {
        Map<String, VertexResult> vertexTasksResults =
                Optional.ofNullable(resultsPerVertex.get(vertexId)).orElse(new HashMap<>());
        vertexTasksResults.put(taskName, result);
        resultsPerVertex.put(vertexId, vertexTasksResults);
    }

    public static void addFailedVertex (Map<String, Set<String>> failedVerticesPerTask, String taskName, String vertexId) {
        Set<String> failedVertices = failedVerticesPerTask.get(taskName);
        if (failedVertices == null) {
            failedVertices = new HashSet<>();
        }
        failedVertices.add(vertexId);
        failedVerticesPerTask.put(taskName, failedVertices);
    }

    public static void printValidationTaskStatus(GraphVertex vertexScanned, String taskName, boolean success, String txtReportFilePath) {
        String successStatus = success ? "success" : "failed";
        String line = "-----------------------Vertex: "+vertexScanned.getUniqueId()+", Task " + taskName + " " +successStatus+"-----------------------";
        StrBuilder sb = new StrBuilder();
        sb.appendln(line);
        writeReportLineToFile(line, txtReportFilePath);
    }

    public static void writeReportLineToFile(String message, String txtReportFilePath) {
        try {
            Files.write(Paths.get(txtReportFilePath), new StrBuilder().appendNewLine().toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(txtReportFilePath), message.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.info("write to file failed - {}", e.getClass().getSimpleName(), e);
        }
    }

    public static void reportValidatorTypeSummary(
            String validatorName,
            Set<String> failedTasksNames,
            Set<String> successTasksNames,
            String txtReportFilePath
    ){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: "+ failedTasksNames);
        sb.appendln("Success tasks: "+ successTasksNames);
        writeReportLineToFile(sb.toString(), txtReportFilePath);
    }

    public static void reportStartValidatorRun(String validatorName, int componenentsNum, String txtReportFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("------ValidatorExecuter " + validatorName + " Validation Started, on "+componenentsNum+" components---------");
        writeReportLineToFile(sb.toString(), txtReportFilePath);
    }

    public static void reportStartTaskRun(GraphVertex vertex, String taskName, String txtReportFilePath){
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------Vertex: "+vertex.getUniqueId()+", Task " + taskName + " Started-----------------------");
        writeReportLineToFile(sb.toString(), txtReportFilePath);
    }

    public static void reportEndOfToolRun(Map<String, Set<String>> failedVerticesPerTask, Map<String, Map<String, VertexResult>> resultsPerVertex, String txtReportFilePath, String csvReportFilePath) {
        StrBuilder sb = new StrBuilder();
        sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
        failedVerticesPerTask.forEach((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writeReportLineToFile(sb.toString(), txtReportFilePath);
        printAllResults(resultsPerVertex, csvReportFilePath);
    }

    public static void printAllResults(Map<String, Map<String, VertexResult>> resultsPerVertex, String csvReportFilePath) {
        resultsPerVertex.forEach((vertex, tasksResults) -> tasksResults.forEach((task, result) -> {
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
        }));
    }
}
