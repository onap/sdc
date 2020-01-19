/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ReportManagerHelper {

    private ReportManagerHelper() {
    }

    public static List<String> getReportOutputFileAsList() {
        return readFileAsList(ValidationConfigManager.getOutputFullFilePath());
    }

    public static List<String> getReportCsvFileAsList() {
        return readFileAsList(ValidationConfigManager.getCsvReportFilePath());
    }

    public static void cleanReports() {
        cleanFile(ValidationConfigManager.getCsvReportFilePath());
        cleanFile(ValidationConfigManager.getOutputFullFilePath());
    }

    private static List<String> readFileAsList(String filePath) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    private static void cleanFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException ignored) {

        }
    }
}
