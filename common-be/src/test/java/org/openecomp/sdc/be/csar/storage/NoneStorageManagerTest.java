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

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoneStorageManagerTest {

    public static final String VSP_ID = "vsp-id";
    public static final String VERSION_ID = "version-id";
    public static final MinIoArtifactInfo UPLOADED_ARTIFACT_INFO = new MinIoArtifactInfo(VSP_ID, VERSION_ID);
    private NoneStorageManager testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new NoneStorageManager();
    }

    @Test
    void testCtor() {
        Assertions.assertTrue(testSubject instanceof NoneStorageManager);
    }

    @Test
    void testPersist() {
        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> testSubject.persist(VSP_ID, VERSION_ID, UPLOADED_ARTIFACT_INFO));
    }

    @Test
    void testUpload() {
        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> testSubject.upload(VSP_ID, VERSION_ID, new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void testPut() {
        Assertions.assertThrows(UnsupportedOperationException.class,
            () -> testSubject.put(VSP_ID, VERSION_ID, new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void testGetStorageConfiguration() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.getStorageConfiguration());
    }

    @Test
    void testGetArtifactInfo() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.get(UPLOADED_ARTIFACT_INFO));
    }

    @Test
    void testGetString() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.get(VSP_ID, VERSION_ID));
    }

    @Test
    void testDeleteArtifactInfo() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.delete(UPLOADED_ARTIFACT_INFO));
    }

    @Test
    void testDeleteString() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.delete(VSP_ID));
    }

    @Test
    void testIsExists() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.isExists(VSP_ID));
    }

    @Test
    void testIsEnabled() {
        Assertions.assertFalse(testSubject.isEnabled());
    }

}
