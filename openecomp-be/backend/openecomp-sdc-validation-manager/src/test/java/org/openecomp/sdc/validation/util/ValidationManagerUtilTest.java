/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.validation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

public class ValidationManagerUtilTest {

    private FileContentHandler fileContentMap;
    private Map<String, List<ErrorMessage>> errors;

    @Before
    public void setUp() throws Exception {
        fileContentMap = new FileContentHandler();
        errors = new HashMap<>();
    }

    @Test
    public void shouldHandleNonMissingManifest() throws IOException {
        fileContentMap.addFile(SdcCommon.MANIFEST_NAME, this.getClass().getClassLoader().getResourceAsStream("MANIFEST.json"));
        ValidationManagerUtil.handleMissingManifest(fileContentMap, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldHandleMissingManifest() throws IOException {
        fileContentMap.addFile("ANY", this.getClass().getClassLoader().getResourceAsStream("vfw.zip"));
        ValidationManagerUtil.handleMissingManifest(fileContentMap, errors);
        assertEquals(errors.get(SdcCommon.MANIFEST_NAME).size(), 1);
        assertEquals(errors.get(SdcCommon.MANIFEST_NAME).get(0).getMessage(), "Manifest doesn't exist");
    }

}