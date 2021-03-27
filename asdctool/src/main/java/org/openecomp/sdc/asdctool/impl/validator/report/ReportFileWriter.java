/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) Bell Intellectual Property. All rights reserved.
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

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * Describes a writer for report's data
 *
 * @param <A> phantom type which is only used for type-safety to prevent mixing writers for TXT Report files and CSV Report files
 */
@SuppressWarnings("unused")
public abstract class ReportFileWriter<A extends FileType> {

    /**
     * @param filePath The resulting file path
     * @param onError  error handling callback
     * @param <A>      phantom type which is only used for type-safety
     */
    public static <A extends FileType> ReportFileWriter<A> makeNioWriter(Path filePath, Consumer<IOException> onError) {
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

    abstract public void write(String s);

    public void writeln(String s) {
        write(s + "\n");
    }
}
