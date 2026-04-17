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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;

class VspUploadStatusRecordMapperTest {

    @Test
    void fullMappingTest() {
        //given
        final VspUploadStatusRecordMapper vspUploadStatusRecordMapper = new VspUploadStatusRecordMapper();
        final var vspUploadStatus = new VspUploadStatusRecord();
        vspUploadStatus.setVspId("vspId");
        vspUploadStatus.setVspVersionId("vspVersionId");
        vspUploadStatus.setStatus(VspUploadStatus.UPLOADING.name());
        vspUploadStatus.setLockId(UUID.randomUUID());
        vspUploadStatus.setIsComplete(true);
        vspUploadStatus.setCreated(Instant.now());
        vspUploadStatus.setUpdated(Instant.now());
        final var vspUploadStatusDto = new VspUploadStatusDto();
        //when
        vspUploadStatusRecordMapper.doMapping(vspUploadStatus, vspUploadStatusDto);
        //then
        assertEquals(vspUploadStatus.getVspId(), vspUploadStatusDto.getVspId());
        assertEquals(vspUploadStatus.getVspVersionId(), vspUploadStatusDto.getVspVersionId());
        assertEquals(vspUploadStatus.getStatus(), vspUploadStatusDto.getStatus().toString());
        assertEquals(vspUploadStatus.getLockId(), vspUploadStatusDto.getLockId());
        assertEquals(vspUploadStatus.getIsComplete(), vspUploadStatusDto.isComplete());
        assertEquals(vspUploadStatus.getCreated().truncatedTo(ChronoUnit.MILLIS), vspUploadStatusDto.getCreated().toInstant().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(vspUploadStatus.getUpdated().truncatedTo(ChronoUnit.MILLIS), vspUploadStatusDto.getUpdated().toInstant().truncatedTo(ChronoUnit.MILLIS));
    }
}
