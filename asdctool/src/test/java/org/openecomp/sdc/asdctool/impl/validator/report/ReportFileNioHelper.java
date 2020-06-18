/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Bell. All rights reserved.
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

import static org.openecomp.sdc.asdctool.impl.validator.report.ReportFileWriterTestFactory.makeNioWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openecomp.sdc.asdctool.impl.validator.report.ReportFile.TXTFile;

/**
 * Provides facilities to for writing report files when testing
 */
public class ReportFileNioHelper {
    private ReportFileNioHelper() {
    }

    /**
     * Provides a transactional context for CSV report file writing
     *
     * @param csvReportFilePath The resulting file path
     * @param f The function to write in a CSV file
     * @param <A> A Phantom type only required for type-safety
     */
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

    /**
     * Provides a transactional context for TXT report file writing
     *
     * @param txtReportFilePath The resulting file path
     * @param f                 The function consuming the TXT file
     */
    public static void withTxtFile(String txtReportFilePath, Consumer<TXTFile> f) {
        withTxtFile(txtReportFilePath, file -> {
            f.accept(file);
            return null;
        });
    }

   /**
     * Provides a transactional context for TXT report file writing
     *
     * @param txtReportFilePath The resulting file path
     * @param f                 The function to write in a TXT file
     * @param <A>               The type returned by the function consuming the file
     */
    public static <A> A withTxtFile(String txtReportFilePath, Function<ReportFile.TXTFile, A> f) {
        // TODO: Switch to makeTxtFile once all the report file business logic has been moved to
        // ReportFile
        ReportFile.TXTFile file = ReportFile.makeAppendableTxtFile(makeNioWriter(txtReportFilePath));
        A result = f.apply(file);
        try {
            Files.delete(Paths.get(txtReportFilePath));
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads the content of a file and store each line in a list
     * @param filePath The path to the file
     */
    public static List<String> readFileAsList(String filePath) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
