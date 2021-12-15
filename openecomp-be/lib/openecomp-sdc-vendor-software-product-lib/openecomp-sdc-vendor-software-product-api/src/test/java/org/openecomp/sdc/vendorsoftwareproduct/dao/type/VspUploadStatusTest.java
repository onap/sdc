/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class VspUploadStatusTest {

    @Test
    void isCompleteStatus() {
        assertTrue(VspUploadStatus.SUCCESS.isCompleteStatus());
        assertTrue(VspUploadStatus.ERROR.isCompleteStatus());
        assertFalse(VspUploadStatus.UPLOADING.isCompleteStatus());
        assertFalse(VspUploadStatus.PROCESSING.isCompleteStatus());
    }

    @Test
    void getCompleteStatusTest() {
        final List<VspUploadStatus> completeStatus = VspUploadStatus.getCompleteStatus();
        assertEquals(2, completeStatus.size());
        assertTrue(completeStatus.contains(VspUploadStatus.SUCCESS));
        assertTrue(completeStatus.contains(VspUploadStatus.ERROR));
    }
}