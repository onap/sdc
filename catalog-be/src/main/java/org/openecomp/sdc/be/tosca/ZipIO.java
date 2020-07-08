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

import io.vavr.collection.Stream;
import io.vavr.control.Try;

import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipIO abstracts a writing operation in a Zip file
 */
public interface ZipIO {

    /**
     * Run the operation and perform any IO required
     *
     * @param zw The writer used to write in the zip file
     */
    Try<Void> run(ZipWriter zw);

    /** No-Op */
    // It's ok to return a null as an operation results in a Try<Void>
    ZipIO None = zw -> Try.success(null);

    /**
     * Combines two {@link org.openecomp.sdc.be.tosca.ZipIO} into one. The resulting operation performs each
     * underlying operation sequentially. If the first operation fails, the second one won't be executed.
     *
     * @param left The first operation to run
     * @param right The second operation to run
     */
    static ZipIO both(ZipIO left, ZipIO right) {
        return zw -> left.run(zw).flatMap(v -> right.run(zw));
    }

    /**
     * Builds an operation resulting in a failure
     *
     * @param th The resulting failure
     */
    static ZipIO error(Throwable th) {
        return zw -> Try.failure(th);
    }

    /**
     * Builds an operation adding an entry in a zip file
     *
     * @param name The entry name
     * @param payload The entry's payload
     */
    static ZipIO write(String name, Supplier<byte[]> payload) {
        return zw -> zw.write(name, payload.get());
    }

    /**
     * Alias for {@link org.openecomp.sdc.be.tosca.ZipIO}.writeTry
     */
    static ZipIO writeTry(String name, Try<byte[]> payload) {
        return writeTry(name, () -> payload);
    }

    /**
     * Builds an operation resulting from a computation potentially resulting in a payload.
     * If the payload cannot be retrieved, the operation results in an error.
     *
     * @param name The entry's name
     * @param payload The entry's payload if it can be successfully retrieved
     */
    static ZipIO writeTry(String name, Supplier<Try<byte[]>> payload) {
        return zw -> payload.get()
                .map(bs -> write(name, () -> bs))
                .getOrElseGet(ZipIO::error)
                .run(zw);
    }

    /**
     * Combine all operations resulting from a {@link io.vavr.collection.Stream}
     *
     * @param zs The {@link io.vavr.collection.Stream} outputting the operations
     */
    static ZipIO writeAll(Stream<ZipIO> zs) {
        return zw -> zs.foldLeft(
                Try.success(null),
                (acc, zio) -> acc.flatMap(void0 -> zio.run(zw))
        );
    }

    /**
     * Alias for {@link org.openecomp.sdc.be.tosca.ZipIO}.both
     */
    default ZipIO and(ZipIO zw) {
        return both(this, zw);
    }

    /**
     * Builds an operation only if the given predicate is true, otherwise returns a no-op.
     *
     * @param p The predicate
     */
    default ZipIO writeIf(boolean p) {
        return p ? this : None;
    }

    /**
     * ZipWriter abstracts the Zip file writing logic.
     */
    interface ZipWriter {

        /**
         * Writes an entry provided with its name and its payload
         *
         * @param entryName The entry's name to use in the zip file
         * @param payload   The payload to write for this entry
         */
        Try<Void> write(String entryName, byte[] payload);

        /**
         * Builds a {@link org.openecomp.sdc.be.tosca.ZipIO.ZipWriter} that outputs the data
         * on a {@link java.util.zip.ZipOutputStream}
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
    }
}

