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

package org.openecomp.sdc.asdctool.impl.validator.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReportFileNioHelper {
    private ReportFileNioHelper() {
    }

    public static void withCsvFile(String csvReportFilePath, Consumer<ReportFile.CSVFile> f) {
        withCsvFile(csvReportFilePath, file -> {
            f.accept(file);
            return null;
        });
    }

    public static <A> A withCsvFile(String csvReportFilePath, Function<ReportFile.CSVFile, A> f) {
        ReportFile.CSVFile file = ReportFile.makeCsvFile(ReportFileWriterTestFactory.makeNioWriter(csvReportFilePath));
        A result = f.apply(file);
        try {
            Files.delete(Paths.get(csvReportFilePath));
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
