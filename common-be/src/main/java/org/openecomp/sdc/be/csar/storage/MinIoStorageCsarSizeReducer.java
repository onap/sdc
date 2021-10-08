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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.sdc.be.csar.storage.exception.CsarSizeReducerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinIoStorageCsarSizeReducer implements PackageSizeReducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinIoStorageCsarSizeReducer.class);
    private static final Set<String> ALLOWED_SIGNATURE_EXTENSIONS = Set.of("cms");
    private static final Set<String> ALLOWED_CERTIFICATE_EXTENSIONS = Set.of("cert", "crt");
    private static final String CSAR_EXTENSION = "csar";
    private static final String UNEXPECTED_PROBLEM_HAPPENED_WHILE_READING_THE_CSAR = "An unexpected problem happened while reading the CSAR '%s'";
    @Getter
    private final AtomicBoolean reduced = new AtomicBoolean(false);

    private final CsarPackageReducerConfiguration configuration;

    public MinIoStorageCsarSizeReducer(final CsarPackageReducerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public byte[] reduce(final Path csarPackagePath) {
        if (hasSignedPackageStructure(csarPackagePath)) {
            return reduce(csarPackagePath, this::signedZipProcessingConsumer);
        } else {
            return reduce(csarPackagePath, this::unsignedZipProcessingConsumer);
        }
    }

    private byte[] reduce(final Path csarPackagePath, final ZipProcessFunction zipProcessingFunction) {
        final var reducedCsarPath = Path.of(csarPackagePath + "." + UUID.randomUUID());

        try (final var zf = new ZipFile(csarPackagePath.toString());
            final var zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(reducedCsarPath)))) {
            zf.entries().asIterator().forEachRemaining(zipProcessingFunction.getProcessZipConsumer(csarPackagePath, zf, zos));
        } catch (final IOException ex1) {
            rollback(reducedCsarPath);
            final var errorMsg = String.format(UNEXPECTED_PROBLEM_HAPPENED_WHILE_READING_THE_CSAR, csarPackagePath);
            throw new CsarSizeReducerException(errorMsg, ex1);
        }
        final byte[] reducedCsarBytes;
        try {
            if (reduced.get()) {
                reducedCsarBytes = Files.readAllBytes(reducedCsarPath);
            } else {
                reducedCsarBytes = Files.readAllBytes(csarPackagePath);
            }
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

    private Consumer<ZipEntry> signedZipProcessingConsumer(final Path csarPackagePath, final ZipFile zf, final ZipOutputStream zos) {
        final var thresholdEntries = configuration.getThresholdEntries();
        final var totalEntryArchive = new AtomicInteger(0);
        return zipEntry -> {
            final var entryName = zipEntry.getName();
            try {
                if (totalEntryArchive.getAndIncrement() > thresholdEntries) {
                    // too much entries in this archive, can lead to inodes exhaustion of the system
                    final var errorMsg = String.format("Failed to extract '%s' from zip '%s'", entryName, csarPackagePath);
                    throw new CsarSizeReducerException(errorMsg);
                }
                zos.putNextEntry(new ZipEntry(entryName));
                if (!zipEntry.isDirectory()) {
                    if (entryName.toLowerCase().endsWith(CSAR_EXTENSION)) {
                        final var internalCsarExtractPath = Path.of(csarPackagePath + "." + UUID.randomUUID());
                        Files.copy(zf.getInputStream(zipEntry), internalCsarExtractPath, REPLACE_EXISTING);
                        zos.write(reduce(internalCsarExtractPath, this::unsignedZipProcessingConsumer));
                        Files.delete(internalCsarExtractPath);
                    } else {
                        zos.write(zf.getInputStream(zipEntry).readAllBytes());
                    }
                }
                zos.closeEntry();
            } catch (final IOException ei) {
                final var errorMsg = String.format("Failed to extract '%s' from zip '%s'", entryName, csarPackagePath);
                throw new CsarSizeReducerException(errorMsg, ei);
            }
        };
    }

    private Consumer<ZipEntry> unsignedZipProcessingConsumer(final Path csarPackagePath, final ZipFile zf, final ZipOutputStream zos) {
        final var thresholdEntries = configuration.getThresholdEntries();
        final var totalEntryArchive = new AtomicInteger(0);
        return zipEntry -> {
            final var entryName = zipEntry.getName();
            if (totalEntryArchive.getAndIncrement() > thresholdEntries) {
                // too much entries in this archive, can lead to inodes exhaustion of the system
                final var errorMsg = String.format("Failed to extract '%s' from zip '%s'", entryName, csarPackagePath);
                throw new CsarSizeReducerException(errorMsg);
            }
            try {
                zos.putNextEntry(new ZipEntry(entryName));
                if (!zipEntry.isDirectory()) {
                    if (isCandidateToRemove(zipEntry)) {
                        // replace with EMPTY string to avoid package description inconsistency/validation errors
                        zos.write("".getBytes());
                        reduced.set(true);
                    } else {
                        zos.write(zf.getInputStream(zipEntry).readAllBytes());
                    }
                }
                zos.closeEntry();
            } catch (final IOException ei) {
                final var errorMsg = String.format("Failed to extract '%s' from zip '%s'", entryName, csarPackagePath);
                throw new CsarSizeReducerException(errorMsg, ei);
            }
        };
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

    private boolean hasSignedPackageStructure(final Path csarPackagePath) {
        final List<Path> packagePathList;
        try (final var zf = new ZipFile(csarPackagePath.toString())) {
            packagePathList = zf.stream()
                .filter(zipEntry -> !zipEntry.isDirectory())
                .map(ZipEntry::getName).map(Path::of)
                .collect(Collectors.toList());
        } catch (final IOException e) {
            final var errorMsg = String.format(UNEXPECTED_PROBLEM_HAPPENED_WHILE_READING_THE_CSAR, csarPackagePath);
            throw new CsarSizeReducerException(errorMsg, e);
        }

        if (CollectionUtils.isEmpty(packagePathList)) {
            return false;
        }
        final int numberOfFiles = packagePathList.size();
        if (numberOfFiles == 2) {
            return hasOneInternalPackageFile(packagePathList) && hasOneSignatureFile(packagePathList);
        }
        if (numberOfFiles == 3) {
            return hasOneInternalPackageFile(packagePathList) && hasOneSignatureFile(packagePathList) && hasOneCertificateFile(packagePathList);
        }
        return false;
    }

    private boolean hasOneInternalPackageFile(final List<Path> packagePathList) {
        return packagePathList.parallelStream()
            .map(Path::toString)
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(extension -> extension.endsWith(CSAR_EXTENSION)).count() == 1;
    }

    private boolean hasOneSignatureFile(final List<Path> packagePathList) {
        return packagePathList.parallelStream()
            .map(Path::toString)
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(ALLOWED_SIGNATURE_EXTENSIONS::contains).count() == 1;
    }

    private boolean hasOneCertificateFile(final List<Path> packagePathList) {
        return packagePathList.parallelStream()
            .map(Path::toString)
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(ALLOWED_CERTIFICATE_EXTENSIONS::contains).count() == 1;
    }

    @FunctionalInterface
    private interface ZipProcessFunction {

        Consumer<ZipEntry> getProcessZipConsumer(Path csarPackagePath, ZipFile zf, ZipOutputStream zos);
    }

}
