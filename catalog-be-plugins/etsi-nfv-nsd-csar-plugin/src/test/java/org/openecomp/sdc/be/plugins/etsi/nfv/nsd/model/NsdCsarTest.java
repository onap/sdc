/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder;

class NsdCsarTest {

    @Test
    void testAddFile() {
        final NsdCsar nsdCsar = new NsdCsar("");
        assertTrue(nsdCsar.getFileMap().isEmpty());
        final String aFilePath = "aFile";
        final byte[] fileContent = aFilePath.getBytes(StandardCharsets.UTF_8);
        nsdCsar.addFile(aFilePath, fileContent);
        assertEquals(1, nsdCsar.getFileMap().size());
        assertEquals(fileContent, nsdCsar.getFile(aFilePath));
    }

    @Test
    void testGetFile() {
        final NsdCsar nsdCsar = new NsdCsar("");
        final String aFilePath = "aFile";
        assertNull(nsdCsar.getFile(aFilePath));
        final byte[] fileContent = aFilePath.getBytes(StandardCharsets.UTF_8);
        nsdCsar.addFile(aFilePath, fileContent);
        assertEquals(1, nsdCsar.getFileMap().size());
        assertEquals(fileContent, nsdCsar.getFile(aFilePath));
    }

    @Test
    void testIsManifest() {
        final NsdCsar nsdCsar = new NsdCsar("");
        assertTrue(nsdCsar.isManifest(nsdCsar.getManifestPath()));
        assertFalse(nsdCsar.isManifest(""));
    }

    @Test
    void testGetManifest() {
        final NsdCsar nsdCsar = new NsdCsar("");
        assertNull(nsdCsar.getManifest());
        final byte[] expectedManifest = "".getBytes(StandardCharsets.UTF_8);
        nsdCsar.addFile(nsdCsar.getManifestPath(), expectedManifest);
        final byte[] actualManifest = nsdCsar.getManifest();
        assertNotNull(actualManifest);
        assertEquals(actualManifest, expectedManifest);
    }

    @Test
    void testGetMainDefinition() {
        final String csarFileName = "csarFileName";
        final NsdCsar nsdCsar = new NsdCsar(csarFileName);
        assertNull(nsdCsar.getMainDefinition());
        assertTrue(nsdCsar.getMainDefinitionPath().contains(csarFileName));
        final byte[] expectedMainDefinition = "".getBytes(StandardCharsets.UTF_8);
        nsdCsar.addFile(nsdCsar.getMainDefinitionPath(), expectedMainDefinition);
        final byte[] actualMainDefinition = nsdCsar.getMainDefinition();
        assertNotNull(actualMainDefinition);
        assertEquals(actualMainDefinition, expectedMainDefinition);
    }

    @Test
    void testFileMapEncapsulation() {
        final NsdCsar nsdCsar = new NsdCsar("");
        final Map<String, byte[]> fileMap = nsdCsar.getFileMap();
        fileMap.put("", new byte[]{});
        assertTrue(nsdCsar.getFileMap().isEmpty());
    }

    @Test
    void addAllFiles() {
        final NsdCsar nsdCsar = new NsdCsar("");
        assertTrue(nsdCsar.isEmpty());
        Map<String, byte[]> fileMap = new HashMap();
        fileMap.put("1", new byte[]{});
        fileMap.put("2", new byte[]{});
        fileMap.put("3", new byte[]{});
        fileMap.put("4", new byte[]{});
        nsdCsar.addAllFiles(fileMap);
        assertFalse(nsdCsar.isEmpty());
        assertEquals(nsdCsar.getFileMap().size(), fileMap.size());
    }

    @Test
    void testIfStartsEmpty() {
        final NsdCsar nsdCsar = new NsdCsar("test");
        assertTrue(nsdCsar.getFileMap().isEmpty(), "Csar should starts empty");
        assertTrue(nsdCsar.isEmpty(), "Csar should starts empty");
    }

    @Test
    void testIsEmpty() {
        final NsdCsar nsdCsar = new NsdCsar("test");
        assertTrue(nsdCsar.isEmpty());
        nsdCsar.addFile("", new byte[]{});
        assertFalse(nsdCsar.isEmpty());
    }

    @Test
    void testAddManifest() {
        final NsdCsar nsdCsar = new NsdCsar("test");
        nsdCsar.addManifest(null);
        assertTrue(nsdCsar.isEmpty());
        final NsdCsarManifestBuilder manifestBuilder = new NsdCsarManifestBuilder();
        nsdCsar.addManifest(manifestBuilder);
        assertFalse(nsdCsar.isEmpty());
        assertEquals(1, nsdCsar.getFileMap().size());
        final byte[] expectedManifestContent = manifestBuilder.build().getBytes(StandardCharsets.UTF_8);
        assertThat("Manifest content should be the same", nsdCsar.getManifest(), is(expectedManifestContent));
    }
}