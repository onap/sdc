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

import static org.openecomp.sdc.common.errors.Messages.EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.MinioClient.Builder;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.Credentials;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.EndPoint;
import org.openecomp.sdc.be.csar.storage.exception.ArtifactStorageException;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinIoStorageArtifactStorageManager implements ArtifactStorageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinIoStorageArtifactStorageManager.class);
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";

    @Getter
    private final MinIoStorageArtifactStorageConfig storageConfiguration;
    private final MinioClient minioClient;

    public MinIoStorageArtifactStorageManager() {
        this.storageConfiguration = readMinIoStorageArtifactStorageConfig();
        minioClient = initMinioClient();
    }

    //for testing only
    MinIoStorageArtifactStorageManager(final ArtifactStorageConfig storageConfiguration) {
        this.storageConfiguration = (MinIoStorageArtifactStorageConfig) storageConfiguration;
        minioClient = initMinioClient();
    }

    @Override
    public ArtifactInfo persist(final String vspId, final String versionId, final ArtifactInfo uploadedArtifactInfo) {
        final MinIoArtifactInfo minioObjectTemp = (MinIoArtifactInfo) uploadedArtifactInfo;
        try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioObjectTemp.getBucket())
                    .object(minioObjectTemp.getObjectName())
                    .build()
            );
        } catch (final Exception e) {
            LOGGER.error("Failed to retrieve uploaded artifact with bucket '{}' and name '{}' while persisting", minioObjectTemp.getBucket(),
                minioObjectTemp.getObjectName(), e);
            throw new ArtifactStorageException(
                String.format("Failed to retrieve uploaded artifact with bucket '%s' and name '%s' while persisting",
                    minioObjectTemp.getBucket(), minioObjectTemp.getObjectName()), e);
        }

        final var backupPath = backupPreviousVersion(vspId, versionId).orElse(null);
        try {
            moveFile(minioObjectTemp, vspId, versionId);
        } catch (final Exception e) {
            rollback(minioObjectTemp, vspId, versionId);
            LOGGER.error("Could not persist artifact for bucket '{}', object '{}'", vspId, versionId, e);
            final var errorMsg = String.format("Could not persist artifact for VSP '%s', version '%s'", vspId, versionId);
            throw new ArtifactStorageException(errorMsg, e);
        }

        removePreviousVersion(backupPath);

        return new MinIoArtifactInfo(vspId, versionId);
    }

    @Override
    public ArtifactInfo upload(final String vspId, final String versionId, final InputStream fileToUpload) {

        final String name = versionId + "--" + UUID.randomUUID();
        try {
            // Make bucket if not exist.
            final boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(vspId).build());

            if (!found) {
                // Make a new bucket ${vspId} .
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(vspId).build());
            } else {
                LOGGER.info("Bucket '{}' already exists.", vspId);
            }

            put(vspId, name, fileToUpload);

        } catch (final Exception e) {
            LOGGER.error("Failed to upload artifact - bucket: '{}', object: '{}'", vspId, name, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }

        return new MinIoArtifactInfo(vspId, name);
    }

    @Override
    public void put(final String vspId, final String name, final InputStream fileToUpload) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(vspId)
                    .object(name)
                    .stream(fileToUpload, fileToUpload.available(), -1)
                    .build()
            );
        } catch (final Exception e) {
            LOGGER.error("Failed to put - bucket: '{}', object: '{}'", vspId, name, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return storageConfiguration != null && storageConfiguration.isEnabled();
    }

    @Override
    public InputStream get(final ArtifactInfo artifactInfo) {
        final MinIoArtifactInfo minioObject = (MinIoArtifactInfo) artifactInfo;
        try {
            return get(minioObject.getBucket(), minioObject.getObjectName());
        } catch (final Exception e) {
            LOGGER.error("Failed to get - bucket: '{}', object: '{}'", minioObject.getBucket(), minioObject.getObjectName(), e);
            throw new ArtifactStorageException("Failed to get Object", e);
        }
    }

    @Override
    public InputStream get(final String bucketID, final String objectID) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketID)
                .object(objectID)
                .build());
        } catch (final Exception e) {
            LOGGER.error("Failed to get - bucket: '{}', object: '{}'", bucketID, objectID, e);
            throw new ArtifactStorageException("Failed to get Object", e);
        }
    }

    @Override
    public void delete(final ArtifactInfo artifactInfo) {
        final MinIoArtifactInfo minioObject = (MinIoArtifactInfo) artifactInfo;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioObject.getBucket())
                .object(minioObject.getObjectName())
                .bypassGovernanceMode(true)
                .build());
        } catch (final Exception e) {
            LOGGER.error("Failed to delete - bucket: '{}', object: '{}'", minioObject.getBucket(), minioObject.getObjectName(), e);
            throw new ArtifactStorageException(String.format("Failed to delete '%s'", minioObject.getObjectName()), e);
        }

    }

    private Optional<MinIoArtifactInfo> backupPreviousVersion(final String vspId, final String versionId) {

        final String tempName = versionId + "--" + UUID.randomUUID().toString();
        try {
            copy(vspId, tempName, versionId);
        } catch (final Exception e) {
            LOGGER.error("Failed to copy - bucket: '{}', object: '{}'", vspId, versionId, e);
            return Optional.empty();
        }

        return Optional.of(new MinIoArtifactInfo(vspId, tempName));
    }

    private void rollback(final MinIoArtifactInfo minioObject, final String vspId, final String versionId) {
        try {
            moveFile(minioObject, vspId, versionId);
        } catch (final Exception ex) {
            LOGGER.warn("Could not rollback the backup '{}' to the original '{}'", versionId, minioObject.getObjectName(), ex);
        }
    }

    private void removePreviousVersion(final MinIoArtifactInfo minioObject) {
        if (minioObject == null) {
            return;
        }
        delete(minioObject);
    }

    private void moveFile(final MinIoArtifactInfo minioObject, final String vspId, final String versionId) {
        try {
            copy(vspId, versionId, minioObject.getObjectName());
        } catch (final Exception e) {
            LOGGER.error("Failed to copy - bucket: '{}', object: '{}'", vspId, versionId, e);
            throw new ArtifactStorageException("Failed to move", e);
        }
        delete(minioObject);
    }

    private void copy(final String vspId, final String versionId, final String objectName) throws Exception {
        minioClient.copyObject(
            CopyObjectArgs.builder()
                .bucket(vspId)
                .object(versionId)
                .source(CopySource.builder()
                    .bucket(vspId)
                    .object(objectName)
                    .build())
                .build());
    }

    private MinIoStorageArtifactStorageConfig readMinIoStorageArtifactStorageConfig() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();

        final Map<String, Object> endpoint = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "endpoint", null);
        final Map<String, Object> credentials = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "credentials", null);
        final String tempPath = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "tempPath", null);

        if (endpoint == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("endpoint"));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("endpoint"));
        }
        if (credentials == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("credentials"));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("credentials"));
        }
        if (tempPath == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("tempPath"));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage("tempPath"));
        }
        LOGGER.info("ArtifactConfig.endpoint: '{}'", endpoint);
        LOGGER.info("ArtifactConfig.credentials: '{}'", credentials);
        LOGGER.info("ArtifactConfig.tempPath: '{}'", tempPath);

        final String host = (String) endpoint.getOrDefault("host", null);
        final int port = (int) endpoint.getOrDefault("port", 0);
        final boolean secure = (boolean) endpoint.getOrDefault("secure", false);

        final String accessKey = (String) credentials.getOrDefault("accessKey", null);
        final String secretKey = (String) credentials.getOrDefault("secretKey", null);

        return new MinIoStorageArtifactStorageConfig(true, new EndPoint(host, port, secure), new Credentials(accessKey, secretKey), tempPath);
    }

    private MinioClient initMinioClient() {
        final EndPoint endPoint = storageConfiguration.getEndPoint();
        final Credentials credentials = storageConfiguration.getCredentials();

        final Builder builder = MinioClient.builder();
        return builder
            .endpoint(endPoint.getHost(), endPoint.getPort(), endPoint.isSecure())
            .credentials(credentials.getAccessKey(), credentials.getSecretKey())
            .build();
    }

}
