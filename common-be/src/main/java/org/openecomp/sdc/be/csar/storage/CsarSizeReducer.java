/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.csar.storage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.openecomp.sdc.be.csar.storage.exception.CsarSizeReducerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsarSizeReducer implements PackageSizeReducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsarSizeReducer.class);

    private final CsarPackageReducerConfiguration configuration;

    public CsarSizeReducer(final CsarPackageReducerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public byte[] reduce(final Path csarPackagePath) {
        final var reducedCsarPath = Path.of(csarPackagePath + "." + UUID.randomUUID());

        try (final var zf = new ZipFile(csarPackagePath.toString());
            final var zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(reducedCsarPath)))) {

            zf.entries().asIterator().forEachRemaining(entry -> {
                final var entryName = entry.getName();
                try {
                    if (!entry.isDirectory()) {
                        zos.putNextEntry(new ZipEntry(entryName));
                        if (isCandidateToRemove(entry)) {
                            // replace with EMPTY string to avoid package description inconsistency/validation errors
                            zos.write("".getBytes());
                        } else {
                            zos.write(zf.getInputStream(entry).readAllBytes());
                        }
                    }
                    zos.closeEntry();
                } catch (final IOException ei) {
                    final var errorMsg = String.format("Failed to extract '%s' from zip '%s'", entryName, csarPackagePath);
                    throw new CsarSizeReducerException(errorMsg, ei);
                }
            });

        } catch (final IOException ex1) {
            rollback(reducedCsarPath);
            final var errorMsg = String.format("An unexpected problem happened while reading the CSAR '%s'", csarPackagePath);
            throw new CsarSizeReducerException(errorMsg, ex1);
        }
        final byte[] reducedCsarBytes;
        try {
            reducedCsarBytes = Files.readAllBytes(reducedCsarPath);
        } catch (final IOException e) {
            final var errorMsg = String.format("Could not read bytes of file '%s'", csarPackagePath);
            throw new CsarSizeReducerException(errorMsg, e);
        }
        try {
            Files.delete(reducedCsarPath);
        } catch (final IOException e) {
            final var errorMsg = String.format("Could not delete temporary file '%s'", reducedCsarPath);
            throw new CsarSizeReducerException(errorMsg, e);
        }

        return reducedCsarBytes;
    }

    private void rollback(final Path reducedCsarPath) {
        if (Files.exists(reducedCsarPath)) {
            try {
                Files.delete(reducedCsarPath);
            } catch (final Exception ex2) {
                LOGGER.warn("Could not delete temporary file '{}'", reducedCsarPath, ex2);
            }
        }
    }

    private boolean isCandidateToRemove(final ZipEntry zipEntry) {
        final String zipEntryName = zipEntry.getName();
        return configuration.getFoldersToStrip().stream().anyMatch(Path.of(zipEntryName)::startsWith)
            || zipEntry.getSize() > configuration.getSizeLimit();
    }

}
