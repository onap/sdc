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

package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class ReportFileWriterFactory {
    private ReportFileWriterFactory() {
    }

    public static <A extends FileType> ReportFileWriter<A> nioWriter(Path filePath, Consumer<IOException> onError) {
        return new ReportFileWriter<A>() {
            @Override
            public void write(String line) {
                try {
                    StandardOpenOption soo = Files.exists(filePath) ? APPEND : CREATE;
                    Files.write(filePath, line.getBytes(), soo);
                } catch (IOException ex) {
                    onError.accept(ex);
                }
            }
        };
    }
}
