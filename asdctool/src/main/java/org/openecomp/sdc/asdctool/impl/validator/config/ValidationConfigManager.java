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

import java.util.function.Supplier;

public final class ValidationConfigManager {

    public static final String DEFAULT_CSV_PATH = "summary.csv";
    private static final String REPORT_OUTPUT_FILE_NAME = "/reportOutput.txt";
    private static final String CSV_FILE_PREFIX = "/csvSummary_";
    private static final String CSV_EXT = ".csv";

    private ValidationConfigManager() {
    }

    public static String txtReportFilePath(String outputPath) {
        return outputPath + REPORT_OUTPUT_FILE_NAME;
    }

    public static String csvReportFilePath(String outputPath, Supplier<Long> getTime) {
        return outputPath + CSV_FILE_PREFIX + getTime.get() + CSV_EXT;
    }
}
