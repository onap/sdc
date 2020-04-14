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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Report {
    private final Map<String, Set<String>> failedVerticesPerTask = new HashMap<>();
    private final Map<String, Map<String, VertexResult>> resultsPerVertex = new HashMap<>();

    private final String txtReportFilePath;
    private final String csvReportFilePath;

    public static Report make(String txtReportFilePath, String csvReportFilePath) {
        return new Report(txtReportFilePath, csvReportFilePath);
    }

    private Report(String txtReportFilePath, String csvReportFilePath) {
        this.txtReportFilePath = txtReportFilePath;
        this.csvReportFilePath = csvReportFilePath;
    }

    public String getTxtReportFilePath() {
        return txtReportFilePath;
    }

    public String getCsvReportFilePath() {
        return csvReportFilePath;
    }

    public void addFailedVertex(String taskName, String vertexId) {
        Set<String> failedVertices = failedVerticesPerTask.get(taskName);
        if (failedVertices == null) {
            failedVertices = new HashSet<>();
        }
        failedVertices.add(vertexId);
        failedVerticesPerTask.put(taskName, failedVertices);
    }

    public void reportTaskEnd(String vertexId, String taskName, VertexResult result) {
        Map<String, VertexResult> vertexTasksResults =
                Optional.ofNullable(resultsPerVertex.get(vertexId)).orElse(new HashMap<>());
        vertexTasksResults.put(taskName, result);
        resultsPerVertex.put(vertexId, vertexTasksResults);
    }

    public void forEachFailedVertices(FailureConsumer c) {
        failedVerticesPerTask.forEach(c::traverse);
    }

    @FunctionalInterface
    public interface FailureConsumer {
        void traverse(String taskName, Set<String> failedVertice);
    }

    public void forEachResult(ResultsConsumer p) {
        resultsPerVertex.forEach((vertex, tasksResults) ->
                tasksResults.forEach((task, result) ->
                        p.traverse(vertex, task, result)));
    }

    @FunctionalInterface
    public interface ResultsConsumer {
        void traverse(String vertex, String task, VertexResult result);
    }
}
