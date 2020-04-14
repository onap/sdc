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

package org.openecomp.sdc.asdctool.impl.validator;

import org.openecomp.sdc.asdctool.impl.validator.utils.FileType;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportFileWriter;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportFileWriterFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class ReportFileWriterTestFactory {
    private ReportFileWriterTestFactory() {
    }

    public static <A extends FileType> ReportFileWriter<A> makeNioWriter(String path) {
        Path p = Paths.get(path);
        return makeNioWriter(p);
    }
    public static <A extends FileType> ReportFileWriter<A> makeNioWriter(Path path) {
        return ReportFileWriterFactory.nioWriter(path, ex -> {
            ex.printStackTrace();
            fail(ex.getMessage());
        });
    }

    public static <A extends FileType> ReportFileWriter<A> makeConsoleWriter() {
        return new ReportFileWriter<A>() {
            @Override
            public void write(String line) {
                System.out.print(line);
            }
        };
    }
}
