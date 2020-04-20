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

package org.openecomp.sdc.asdctool.impl.validator.config;

public class ValidationConfigManager {

    private static String outputFullFilePath;
    private static String outputFilePath;

    private ValidationConfigManager() {
    }

    public static String getOutputFullFilePath() {
        return outputFullFilePath;
    }

    public static String getOutputFilePath() {
        return outputFilePath;
    }

    public static void setOutputFullFilePath(String outputPath) {
        ValidationConfigManager.outputFilePath = outputPath;
        ValidationConfigManager.outputFullFilePath = outputPath + "/reportOutput.txt";
    }

    public static String getCsvReportFilePath() {
        return csvReportFilePath;
    }

    public static void setCsvReportFilePath(String outputPath) {
        ValidationConfigManager.csvReportFilePath =
            outputPath + "/csvSummary_" + System.currentTimeMillis() + ".csv";
    }

    private static String csvReportFilePath = "summary.csv";
}
