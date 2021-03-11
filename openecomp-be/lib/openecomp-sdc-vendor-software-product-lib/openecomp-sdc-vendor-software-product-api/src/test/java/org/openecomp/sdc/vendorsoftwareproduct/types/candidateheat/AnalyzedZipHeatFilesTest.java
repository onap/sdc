/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AnalyzedZipHeatFilesTest {

    private AnalyzedZipHeatFiles testSubject = new AnalyzedZipHeatFiles();

    @Test
    public void testGetFilesNotEligbleForModules() {
        testSubject.addNestedFile("testfile1");
        testSubject.addModuleFile("testfile2");
        HashSet<String> fileNames = new HashSet<>();
        fileNames.addAll(Arrays.asList("testfile2", "testfile3"));
        testSubject.addNestedFiles(fileNames);

        testSubject.addOtherNonModuleFile("testfile4");
        testSubject.addModuleFile("testfile5");
        testSubject.addModuleFile("testfile7");
        HashSet<String> fileNames2 = new HashSet<>();
        fileNames2.addAll(Arrays.asList("testfile5", "testfile6"));
        testSubject.addOtherNonModuleFiles(fileNames2);

        HashSet<String> moduelFiles = (HashSet<String>) testSubject.getModuleFiles();
        assertEquals(1, moduelFiles.size());
        assertEquals("testfile7", moduelFiles.iterator().next());

        List<String> res = (List<String>) testSubject.getFilesNotEligbleForModules();
        assertEquals(6, res.size());
    }
}
