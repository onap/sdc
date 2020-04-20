/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.impl.validator.report;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides facilities to for creating report file writers when testing
 */
public class ReportFileWriterTestFactory {
    private ReportFileWriterTestFactory() {
    }

    /**
     * Alias for {@link org.openecomp.sdc.asdctool.impl.validator.report.ReportFileWriterTestFactory#makeNioWriter(Path)}
     *
     * @param path The resulting file path
     * @param <A> a Phantom type used only for type-safety
     */
    public static <A extends FileType> ReportFileWriter<A> makeNioWriter(String path) {
        Path p = Paths.get(path);
        return makeNioWriter(p);
    }

    /**
     * Creates a NIO writer storing the data written into a file on disk
     * @param path The resulting file path
     * @param <A> a Phantom type used only for type-safety
     */
    public static <A extends FileType> ReportFileWriter<A> makeNioWriter(Path path) {
        return ReportFileWriter.makeNioWriter(path, ex -> fail(ex.getMessage()));
    }
}
