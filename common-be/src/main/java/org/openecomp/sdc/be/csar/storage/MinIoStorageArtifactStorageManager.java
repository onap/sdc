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
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import java.io.InputStream;
import java.util.Map;
import lombok.Getter;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.Credentials;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.EndPoint;
import org.openecomp.sdc.be.csar.storage.exception.ArtifactStorageException;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinIoStorageArtifactStorageManager implements ArtifactStorageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinIoStorageArtifactStorageManager.class);
    private static final String ENDPOINT = "endpoint";
    private static final String CREDENTIALS = "credentials";
    private static final String TEMP_PATH = "tempPath";
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";
    @Getter
    private final MinIoStorageArtifactStorageConfig storageConfiguration;
    private final MinioClient minioClient;

    public MinIoStorageArtifactStorageManager() {
        storageConfiguration = readMinIoStorageArtifactStorageConfig();
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
        LOGGER.debug("PERSIST - bucket: '{}', object: '{}'", minioObjectTemp.getBucket(), minioObjectTemp.getObjectName());
        try {
            // Get information of an object.
            minioClient.statObject(
                StatObjectArgs.builder()
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
        return new MinIoArtifactInfo(vspId, versionId);
    }

    @Override
    public ArtifactInfo upload(final String vspId, final String versionId, final InputStream fileToUpload) {

        try {
            // Make bucket if not exist.
            final boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(vspId).build());

            if (!found) {
                // Make a new bucket ${vspId} .
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(vspId).build());
            } else {
                LOGGER.info("Bucket '{}' already exists.", vspId);
            }

            put(vspId, versionId, fileToUpload);

        } catch (final Exception e) {
            LOGGER.error("Failed to upload artifact - bucket: '{}', object: '{}'", vspId, versionId, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }

        return new MinIoArtifactInfo(vspId, versionId);
    }

    @Override
    public void put(final String vspId, final String name, final InputStream fileToUpload) {
        LOGGER.debug("BEGIN -> PUT - bucket: '{}', object: '{}'", vspId, name);
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(vspId)
                    .object(name)
                    .stream(fileToUpload, -1, storageConfiguration.getUploadPartSize())
                    .build()
            );
        } catch (final Exception e) {
            LOGGER.error("Failed to put - bucket: '{}', object: '{}'", vspId, name, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }
        LOGGER.debug("SUCCESS -> PUT - bucket: '{}', object: '{}'", vspId, name);
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
        LOGGER.debug("GET - bucket: '{}', object: '{}'", bucketID, objectID);
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
        LOGGER.debug("DELETE - bucket: '{}', object: '{}'", minioObject.getBucket(), minioObject.getObjectName());
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

    private MinIoStorageArtifactStorageConfig readMinIoStorageArtifactStorageConfig() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        commonConfigurationManager.reload();

        final Map<String, Object> endpoint = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, ENDPOINT, null);
        final Map<String, Object> creds = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, CREDENTIALS, null);
        final String tempPath = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, TEMP_PATH, null);
        final int uploadPartSize = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "uploadPartSize", 50_000_000);

        if (endpoint == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(ENDPOINT));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(ENDPOINT));
        }
        if (creds == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(CREDENTIALS));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(CREDENTIALS));
        }
        if (tempPath == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(TEMP_PATH));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(TEMP_PATH));
        }
        LOGGER.info("ArtifactConfig.endpoint: '{}'", endpoint);
        LOGGER.info("ArtifactConfig.credentials: '{}'", creds);
        LOGGER.info("ArtifactConfig.tempPath: '{}'", tempPath);

        final String host = (String) endpoint.getOrDefault("host", null);
        final int port = (int) endpoint.getOrDefault("port", 0);
        final boolean secure = (boolean) endpoint.getOrDefault("secure", false);

        final String accessKey = (String) creds.getOrDefault("accessKey", null);
        final String secretKey = (String) creds.getOrDefault("secretKey", null);

        return new MinIoStorageArtifactStorageConfig
            (true, new EndPoint(host, port, secure), new Credentials(accessKey, secretKey), tempPath, uploadPartSize);
    }

    private MinioClient initMinioClient() {
        final EndPoint storageConfigurationEndPoint = storageConfiguration.getEndPoint();
        final Credentials storageConfigurationCredentials = storageConfiguration.getCredentials();

        return MinioClient.builder()
            .endpoint(storageConfigurationEndPoint.getHost(), storageConfigurationEndPoint.getPort(), storageConfigurationEndPoint.isSecure())
            .credentials(storageConfigurationCredentials.getAccessKey(), storageConfigurationCredentials.getSecretKey())
            .build();
    }

}
