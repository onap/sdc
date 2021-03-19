/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.tosca;

import io.vavr.control.Try;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipWriter abstracts the Zip file writing logic.
 */
public interface ZipWriter {

    /**
     * Builds a ZipWriter that outputs the data on a {@link java.util.zip.ZipOutputStream}
     *
     * @param zos the target {@link java.util.zip.ZipOutputStream}
     */
    static ZipWriter live(ZipOutputStream zos) {
        return (entryName, payload) -> Try.of(() -> {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(payload);
            // We can return null as a Void is expected;
            return null;
        });
    }

    /**
     * Writes an entry provided with its name and its payload
     *
     * @param entryName The entry's name to use in the zip file
     * @param payload   The payload to write for this entry
     */
    Try<Void> write(String entryName, byte[] payload);

    /**
     * Writes an entry provided with its name and its payload
     *
     * @param entryName The entry's name to use in the zip file
     * @param payload   The payload to write for this entry
     */
    default Try<Void> write(String entryName, String payload) {
        return write(entryName, payload.getBytes());
    }

    /**
     * Alias for {@link org.openecomp.sdc.be.tosca.ZipWriter}
     *
     * @param entryName The entry's name to use in the zip file
     */
    default Function<String, Try<Void>> writeString(String entryName) {
        return payload -> write(entryName, payload.getBytes());
    }

    default Function<byte[], Try<Void>> write(String entryName) {
        return payload -> write(entryName, payload);
    }
}
