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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.driver.mapping.Result;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordAccessor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;

class VspUploadStatusRecordDaoImlTest {

    @Mock
    private VspUploadStatusRecordAccessor accessor;

    private VspUploadStatusRecordDaoIml packageUploadManagerDaoIml;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packageUploadManagerDaoIml = new VspUploadStatusRecordDaoIml(accessor);
    }

    @Test
    void findAllByVspIdAndVersionIdSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final List<VspUploadStatusRecord> expectedVspUploadStatusRecordList = List.of(new VspUploadStatusRecord(), new VspUploadStatusRecord());
        final Result<VspUploadStatusRecord> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusRecordList);
        when(accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final List<VspUploadStatusRecord> actualVspUploadStatusRecordList =
            packageUploadManagerDaoIml.findAllByVspIdAndVersionId(vspId, vspVersionId);
        //then
        assertEquals(expectedVspUploadStatusRecordList, actualVspUploadStatusRecordList);
    }

    @Test
    void findByVspIdAndVersionIdAndLockIdSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final var expectedVspUploadStatus = new VspUploadStatusRecord();
        final Result<VspUploadStatusRecord> resultMock = mock(Result.class);
        when(resultMock.one()).thenReturn(expectedVspUploadStatus);
        when(accessor.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId)).thenReturn(resultMock);
        //when
        final Optional<VspUploadStatusRecord> vspUploadStatusOptional =
            packageUploadManagerDaoIml.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId);
        //then
        assertTrue(vspUploadStatusOptional.isPresent());
        assertEquals(expectedVspUploadStatus, vspUploadStatusOptional.get());
    }

    @Test
    void findAllNotCompleteSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final List<VspUploadStatusRecord> expectedVspUploadStatusRecordList = List.of(new VspUploadStatusRecord(), new VspUploadStatusRecord());
        final Result<VspUploadStatusRecord> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusRecordList);
        when(accessor.findAllIncomplete(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final List<VspUploadStatusRecord> actualVspUploadStatusRecordList = packageUploadManagerDaoIml.findAllInProgress(vspId, vspVersionId);
        //then
        assertEquals(expectedVspUploadStatusRecordList, actualVspUploadStatusRecordList);
    }

    @Test
    void findLatestSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final List<VspUploadStatusRecord> expectedVspUploadStatusRecordList = new ArrayList<>();
        IntStream.rangeClosed(1, 31)
            .mapToObj(day -> {
                final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
                final Calendar calendar = Calendar.getInstance();
                calendar.set(2022, Calendar.JANUARY, day);
                vspUploadStatusRecord.setCreated(calendar.getTime());
                return vspUploadStatusRecord;
            })
            .forEach(expectedVspUploadStatusRecordList::add);
        final Result<VspUploadStatusRecord> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusRecordList);

        final VspUploadStatusRecord mostRecentVspUploadStatus = expectedVspUploadStatusRecordList.get(expectedVspUploadStatusRecordList.size() - 1);

        when(accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final Optional<VspUploadStatusRecord> vspUploadStatusOptional = packageUploadManagerDaoIml.findLatest(vspId, vspVersionId);
        //then
        assertTrue(vspUploadStatusOptional.isPresent());
        assertEquals(mostRecentVspUploadStatus, vspUploadStatusOptional.get());
    }

}