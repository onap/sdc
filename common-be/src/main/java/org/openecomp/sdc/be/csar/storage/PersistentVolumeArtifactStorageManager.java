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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.be.csar.storage.exception.PersistentVolumeArtifactStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentVolumeArtifactStorageManager implements ArtifactStorageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentVolumeArtifactStorageManager.class);

    private final PersistentVolumeArtifactStorageConfig storageConfiguration;

    public PersistentVolumeArtifactStorageManager(final ArtifactStorageConfig storageConfiguration) {
        this.storageConfiguration = (PersistentVolumeArtifactStorageConfig) storageConfiguration;
    }

    @Override
    public ArtifactInfo persist(final String vspId, final String versionId, final ArtifactInfo uploadedArtifactInfo) {
        final var temporaryPath = uploadedArtifactInfo.getPath();
        if (!Files.exists(temporaryPath)) {
            throw new PersistentVolumeArtifactStorageException(String.format("Given artifact does not exist '%s'", uploadedArtifactInfo.getPath()));
        }

        final var filePath = buildFilePath(vspId, versionId);
        final var backupPath = backupPreviousVersion(filePath).orElse(null);
        try {
            moveFile(temporaryPath, filePath);
        } catch (final Exception e) {
            rollback(backupPath, filePath);
            final var errorMsg = String.format("Could not persist artifact for VSP '%s', version '%s'", vspId, versionId);
            throw new PersistentVolumeArtifactStorageException(errorMsg, e);
        }

        removePreviousVersion(backupPath);

        return new PersistentStorageArtifactInfo(filePath);
    }

    @Override
    public ArtifactInfo upload(final String vspId, final String versionId, final InputStream artifactInputStream) {
        final var destinationFolder = buildDestinationFolder(vspId, versionId);
        try {
            Files.createDirectories(destinationFolder);
        } catch (final IOException e) {
            throw new PersistentVolumeArtifactStorageException(String.format("Could not create directory '%s'", destinationFolder), e);
        }

        final var filePath = createTempFilePath(destinationFolder);
        try {
            persist(artifactInputStream, filePath);
        } catch (final IOException e) {
            throw new PersistentVolumeArtifactStorageException(String.format("Could not persist artifact '%s'", filePath), e);
        }

        return new PersistentStorageArtifactInfo(filePath);
    }

    private Path buildFilePath(final String vspId, final String versionId) {
        return buildDestinationFolder(vspId, versionId).resolve(versionId);
    }

    @Override
    public boolean isEnabled() {
        return storageConfiguration != null && storageConfiguration.isEnabled();
    }

    private Optional<Path> backupPreviousVersion(final Path filePath) {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        final var backupPath = Path.of(filePath + UUID.randomUUID().toString());
        moveFile(filePath, backupPath);
        return Optional.ofNullable(backupPath);
    }

    private void rollback(final Path backupPath, final Path filePath) {
        try {
            moveFile(backupPath, filePath);
        } catch (final Exception ex) {
            LOGGER.warn("Could not rollback the backup file '{}' to the original '{}'", backupPath, filePath, ex);
        }
    }

    private void removePreviousVersion(final Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return;
        }

        try {
            Files.delete(filePath);
        } catch (final IOException e) {
            throw new PersistentVolumeArtifactStorageException(String.format("Could not delete previous version '%s'", filePath), e);
        }
    }

    private Path createTempFilePath(final Path destinationFolder) {
        final var retries = 10;
        return createTempFilePath(destinationFolder, retries).orElseThrow(() -> {
            throw new PersistentVolumeArtifactStorageException(String.format("Could not generate upload file path after '%s' retries", retries));
        });
    }

    private Optional<Path> createTempFilePath(final Path destinationFolder, int retries) {
        for (var i = 0; i < retries; i++) {
            final var filePath = destinationFolder.resolve(UUID.randomUUID().toString());
            if (Files.notExists(filePath)) {
                return Optional.of(filePath);
            }
        }
        return Optional.empty();
    }

    private Path buildDestinationFolder(final String vspId, final String versionId) {
        return storageConfiguration.getStoragePath().resolve(vspId).resolve(versionId);
    }

    private void persist(final InputStream artifactInputStream, final Path filePath) throws IOException {
        try (final var inputStream = artifactInputStream;
            final var fileOutputStream = new FileOutputStream(filePath.toFile());) {
            inputStream.transferTo(fileOutputStream);
        }
    }

    private void moveFile(final Path from, final Path to) {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new PersistentVolumeArtifactStorageException(String.format("Could not move file '%s' to '%s'", from, to), e);
        }
    }

}
