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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.activation.DataHandler;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@TestMethodOrder(OrderAnnotation.class)
class PersistentVolumeArtifactStorageManagerTest {

    private static final String SRC_TEST_RESOURCES = "src/test/resources/";

    private PersistentVolumeArtifactStorageManager testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new PersistentVolumeArtifactStorageManager(new PersistentVolumeArtifactStorageConfig(true, Path.of(SRC_TEST_RESOURCES)));
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.move(Path.of(SRC_TEST_RESOURCES + "vspId/versionId/versionId"),
            Path.of(SRC_TEST_RESOURCES + "persistentVolumeArtifactStorageManager/dummy.csar"));
        Files.list(Path.of("src/test/resources/vspId/versionId/")).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.deleteIfExists(Path.of(SRC_TEST_RESOURCES + "vspId/versionId/"));
        Files.deleteIfExists(Path.of(SRC_TEST_RESOURCES + "vspId/"));
    }

    @Test
    @Order(1)
    void testUpload() throws IOException {
        final Attachment attachment = mockAttachment("dummy.csar", this.getClass().getResource("/persistentVolumeArtifactStorageManager/dummy.csar"));
        final ArtifactInfo result = testSubject.upload("vspId", "versionId", attachment.getDataHandler().getInputStream());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getPath());
        Assertions.assertTrue(result.getPath().startsWith(Path.of(SRC_TEST_RESOURCES + "vspId/versionId/")));
    }

    @Test
    @Order(2)
    void testPersist() {
        final ArtifactInfo result = testSubject.persist("vspId", "versionId",
            new PersistentStorageArtifactInfo(Path.of(SRC_TEST_RESOURCES + "persistentVolumeArtifactStorageManager/dummy.csar")));
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getPath());
        Assertions.assertTrue(result.getPath().startsWith(Path.of(SRC_TEST_RESOURCES + "vspId/versionId/")));
    }

    @Test
    void testIsEnabled() {
        Assertions.assertTrue(testSubject.isEnabled());
    }

    private Attachment mockAttachment(final String fileName, final URL fileToUpload) throws IOException {
        final Attachment attachment = Mockito.mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        final DataHandler dataHandler = Mockito.mock(DataHandler.class);
        when(dataHandler.getName()).thenReturn(fileName);
        final InputStream inputStream = Mockito.mock(InputStream.class);
        when(dataHandler.getInputStream()).thenReturn(inputStream);
        when(attachment.getDataHandler()).thenReturn(dataHandler);
        byte[] bytes = "upload package Test".getBytes();
        if (Objects.nonNull(fileToUpload)) {
            try {
                bytes = IOUtils.toByteArray(fileToUpload);
            } catch (final IOException e) {
                fail("Not able to convert file to byte array");
            }
        }
        when(attachment.getObject(ArgumentMatchers.any())).thenReturn(bytes);
        return attachment;
    }

}
