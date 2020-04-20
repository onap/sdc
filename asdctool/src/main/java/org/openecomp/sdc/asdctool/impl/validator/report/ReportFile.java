/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Bell Intellectual Property. All rights reserved.
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

/**
 * Provides business logic in regards to file writing required by the validation tools
 */
public class ReportFile {

    static public CSVFile makeCsvFile(ReportFileWriter<FileType.CSV> writer) {
        writer.writeln("Vertex ID,Task Name,Success,Result Details,Result Description");
        return new CSVFile(writer);
    }

    /**
     * Provides csv writing business logic related to {@link org.openecomp.sdc.asdctool.main.ValidationTool}
     */
    public static final class CSVFile extends ReportFile {

        private final ReportFileWriter<FileType.CSV> writer;

        private CSVFile(ReportFileWriter<FileType.CSV> writer) {
            this.writer = writer;
        }

        public void printAllResults(Report report) {
            report.forEachSuccess((vertex, task, result) -> {
                String resultLine = vertex + "," + task + "," + result.getStatus() + "," + result.getResult();
                writer.writeln(resultLine);
            });
        }
    }
}
