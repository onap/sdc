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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.activation.DataHandler;
import okhttp3.Headers;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.Credentials;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.EndPoint;
import org.openecomp.sdc.be.csar.storage.exception.ArtifactStorageException;

@ExtendWith(MockitoExtension.class)
class MinIoStorageArtifactStorageManagerTest {

    public static final String VSP_ID = "vsp-id";
    public static final String VERSION_ID = "version-id";
    private MinIoStorageArtifactStorageManager testSubject;
    @Mock
    private MinioClient minioClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MinioClient.Builder builderMinio;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BucketExistsArgs.Builder builderBucketExistsArgs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<MinioClient> utilities = Mockito.mockStatic(MinioClient.class)) {
            utilities.when(MinioClient::builder).thenReturn(builderMinio);
            when(builderMinio
                .endpoint(anyString(), anyInt(), anyBoolean())
                .credentials(anyString(), anyString())
                .build()
            ).thenReturn(minioClient);

            testSubject = new MinIoStorageArtifactStorageManager(new MinIoStorageArtifactStorageConfig
                (true, new EndPoint("host", 9000, false), new Credentials("accessKey", "secretKey"), "", 10_000_000));
        }
    }

    @Test
    void testUploadOK() throws Exception {

        when(builderBucketExistsArgs
            .bucket(anyString())
            .build()
        ).thenReturn(new BucketExistsArgs());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        final Attachment attachment = mockAttachment();
        final ArtifactInfo result = testSubject.upload(VSP_ID, VERSION_ID, attachment.getDataHandler().getInputStream());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof MinIoArtifactInfo);
        Assertions.assertEquals(VSP_ID, ((MinIoArtifactInfo) result).getBucket());
        Assertions.assertTrue(((MinIoArtifactInfo) result).getObjectName().startsWith(VERSION_ID));
    }

    @Test
    void testUploadFail() throws Exception {

        when(builderBucketExistsArgs
            .bucket(anyString())
            .build()
        ).thenReturn(new BucketExistsArgs());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        final Attachment attachment = mockAttachment();
        doThrow(new RuntimeException()).when(minioClient).makeBucket(any(MakeBucketArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            testSubject.upload(VSP_ID, VERSION_ID, attachment.getDataHandler().getInputStream());
        });
    }

    @Test
    void testPersistOK() {
        final ArtifactInfo result = testSubject.persist(VSP_ID, VERSION_ID, new MinIoArtifactInfo(VSP_ID, VERSION_ID));
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof MinIoArtifactInfo);
        Assertions.assertEquals(VSP_ID, ((MinIoArtifactInfo) result).getBucket());
        Assertions.assertEquals(VERSION_ID, ((MinIoArtifactInfo) result).getObjectName());
    }

    @Test
    void testPersistFail() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).statObject(any(StatObjectArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            testSubject.persist(VSP_ID, VERSION_ID, new MinIoArtifactInfo(VSP_ID, VERSION_ID));
        });
    }

    @Test
    void testPutFail() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).putObject(any(PutObjectArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            testSubject.put(VSP_ID, VERSION_ID, new ByteArrayInputStream(new byte[0]));
        });
    }

    @Test
    void testIsEnabled() {
        Assertions.assertTrue(testSubject.isEnabled());
    }

    @Test
    void testDeleteVersionFail() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            testSubject.delete(new MinIoArtifactInfo(VSP_ID, VERSION_ID));
        });
    }

    @Test
    void testDeleteVspFail() throws Exception {
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(VSP_ID).build())).thenReturn(true);
        doThrow(new RuntimeException()).when(minioClient).removeBucket(any(RemoveBucketArgs.class));
        assertThrows(ArtifactStorageException.class, () -> testSubject.delete(VSP_ID));
    }

    @Test
    void testDeleteVspBucketNotFound() throws Exception {
        final BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(VSP_ID).build();
        //when
        when(minioClient.bucketExists(bucketExistsArgs)).thenReturn(false);
        testSubject.delete(VSP_ID);
        //then
        verify(minioClient).bucketExists(bucketExistsArgs);
    }

    @Test
    void testGetOK() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(
            new GetObjectResponse(Headers.of(), "", "", "",
                new FileInputStream(Path.of("src/test/resources/s3StoreArtifactStorageManager/dummy.csar").toFile())));
        final InputStream inputStream = testSubject.get(new MinIoArtifactInfo(VSP_ID, VERSION_ID));
        assertNotNull(inputStream);
    }

    @Test
    void testGetFail() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).getObject(any(GetObjectArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            final InputStream inputStream = testSubject.get(new MinIoArtifactInfo(VSP_ID, VERSION_ID));
        });
    }

    @Test
    void testIsExistsOK() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        Assertions.assertTrue(testSubject.exists(VSP_ID));
    }

    @Test
    void testIsExistsFail() throws Exception {
        doThrow(new RuntimeException()).when(minioClient).bucketExists(any(BucketExistsArgs.class));
        assertThrows(ArtifactStorageException.class, () -> {
            Assertions.assertTrue(testSubject.exists(VSP_ID));
        });
    }

    private Attachment mockAttachment() throws IOException {
        final Attachment attachment = Mockito.mock(Attachment.class);
        final DataHandler dataHandler = Mockito.mock(DataHandler.class);
        final InputStream inputStream = Mockito.mock(InputStream.class);
        when(dataHandler.getInputStream()).thenReturn(inputStream);
        when(attachment.getDataHandler()).thenReturn(dataHandler);
        return attachment;
    }

}
