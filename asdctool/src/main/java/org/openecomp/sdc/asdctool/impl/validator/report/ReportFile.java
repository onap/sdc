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

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

/**
 * Provides business logic in regards to file writing required by the validation tools
 */
public class ReportFile {

    // TODO: Delete this function once all the report file business logic has been moved to ReportFile
    static public TXTFile makeAppendableTxtFile(ReportFileWriter<FileType.TXT> writer) {
        return new TXTFile(writer);
    }

    static public TXTFile makeTxtFile(ReportFileWriter<FileType.TXT> writer) {
        writer.writeln("-----------------------Validation Tool Results:-------------------------");
        return new TXTFile(writer);
    }

    static public CSVFile makeCsvFile(ReportFileWriter<FileType.CSV> writer) {
        writer.writeln("Vertex ID,Task Name,Success,Result Details,Result Description");
        return new CSVFile(writer);
    }

    /**
     * Provides csv writing business logic related to {@link org.openecomp.sdc.asdctool.main.ValidationTool}
     */
    public static final class TXTFile extends ReportFile {

        private final ReportFileWriter<FileType.TXT> writer;

        private TXTFile(ReportFileWriter<FileType.TXT> writer) {
            this.writer = writer;
        }

        public void reportStartTaskRun(GraphVertex vertex, String taskName) {
            writer.writeln("");
            writer.writeln("-----------------------Vertex: " + vertex.getUniqueId() +
                ", Task " + taskName + " Started-----------------------");
        }
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
