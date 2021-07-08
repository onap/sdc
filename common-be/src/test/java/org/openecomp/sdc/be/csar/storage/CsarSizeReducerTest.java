/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;

class CsarSizeReducerTest {

    @Mock
    private CsarPackageReducerConfiguration csarPackageReducerConfiguration;
    @InjectMocks
    private CsarSizeReducer csarSizeReducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void reduceByPathAndSizeTest() throws ZipException {
        final var pathToReduce1 = Path.of("Files/images");
        final var pathToReduce2 = Path.of("Files/Scripts/my_script.sh");
        final var sizeLimit = 150000L;
        when(csarPackageReducerConfiguration.getSizeLimit()).thenReturn(sizeLimit);
        when(csarPackageReducerConfiguration.getFoldersToStrip()).thenReturn(Set.of(pathToReduce1, pathToReduce2));

        final var csarPath = Path.of("src/test/resources/csarSizeReducer/dummy.csar");

        final Map<String, byte[]> originalCsar = ZipUtils.readZip(csarPath.toFile(), false);

        final byte[] reduce = csarSizeReducer.reduce(csarPath);

        final Map<String, byte[]> reducedCsar = ZipUtils.readZip(reduce, false);

        assertEquals(originalCsar.keySet().size(), reducedCsar.keySet().size(), "No file should be removed");
        for (final Entry<String, byte[]> originalEntry : originalCsar.entrySet()) {
            final var originalFilePath = originalEntry.getKey();
            final byte[] originalBytes = originalEntry.getValue();
            assertTrue(reducedCsar.containsKey(originalFilePath),
                String.format("No file should be removed, but it is missing original file '%s'", originalFilePath));

            if (originalFilePath.startsWith(pathToReduce1.toString()) || originalFilePath.startsWith(pathToReduce2.toString())
                || originalBytes.length > sizeLimit) {
                assertArrayEquals("".getBytes(StandardCharsets.UTF_8), reducedCsar.get(originalFilePath),
                    String.format("File '%s' expected to be reduced to empty string", originalFilePath));
            } else {
                assertArrayEquals(originalBytes, reducedCsar.get(originalFilePath),
                    String.format("File '%s' expected to be equal", originalFilePath));
            }
        }
    }
}