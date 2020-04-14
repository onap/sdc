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
import java.util.Map;
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

    public Map<String, Set<String>> getFailedVerticesPerTask() {
        return failedVerticesPerTask;
    }

    public Map<String, Map<String, VertexResult>> getResultsPerVertex() {
        return resultsPerVertex;
    }

    public String getTxtReportFilePath() {
        return txtReportFilePath;
    }

    public String getCsvReportFilePath() {
        return csvReportFilePath;
    }
}
