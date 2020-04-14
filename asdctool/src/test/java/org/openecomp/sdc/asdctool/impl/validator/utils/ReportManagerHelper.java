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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openecomp.sdc.asdctool.impl.validator.ReportFileWriterTestFactory.makeNioWriter;
import static org.openecomp.sdc.asdctool.impl.validator.utils.ReportFile.makeCsvFile;

public class ReportManagerHelper {

    private ReportManagerHelper() {
    }

    public static void withTxtFile(String txtReportFilePath, Consumer<ReportFile.TXTFile> f) {
        withTxtFile(txtReportFilePath, file -> {
            f.accept(file);
            return null;
        });
    }

    public static <A> A withTxtFile(String txtReportFilePath, Function<ReportFile.TXTFile, A> f) {
        ReportFile.TXTFile file = ReportFile.makeTxtFile(makeNioWriter(txtReportFilePath));
        A result = f.apply(file);
        try {
            Files.delete(Paths.get(txtReportFilePath));
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void withCsvFile(String csvReportFilePath, Consumer<ReportFile.CSVFile> f) {
        withCsvFile(csvReportFilePath, file -> {
            f.accept(file);
            return null;
        });
    }

    public static <A> A withCsvFile(String csvReportFilePath, Function<ReportFile.CSVFile, A> f) {
        ReportFile.CSVFile file = makeCsvFile(makeNioWriter(csvReportFilePath));
        A result = f.apply(file);
        try {
            Files.delete(Paths.get(csvReportFilePath));
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> readFileAsList(String filePath) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
