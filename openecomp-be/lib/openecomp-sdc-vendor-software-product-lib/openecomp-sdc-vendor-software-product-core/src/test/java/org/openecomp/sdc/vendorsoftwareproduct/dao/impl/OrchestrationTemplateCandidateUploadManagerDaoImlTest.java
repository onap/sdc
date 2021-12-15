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
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageUploadManagerAccessor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;

class OrchestrationTemplateCandidateUploadManagerDaoImlTest {

    @Mock
    private PackageUploadManagerAccessor accessor;

    private OrchestrationTemplateCandidateUploadManagerDaoIml packageUploadManagerDaoIml;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        packageUploadManagerDaoIml = new OrchestrationTemplateCandidateUploadManagerDaoIml(accessor);
    }

    @Test
    void findAllByVspIdAndVersionIdSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final List<VspUploadStatus> expectedVspUploadStatusList = List.of(new VspUploadStatus(), new VspUploadStatus());
        final Result<VspUploadStatus> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusList);
        when(accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final List<VspUploadStatus> actualVspUploadStatusList = packageUploadManagerDaoIml.findAllByVspIdAndVersionId(vspId, vspVersionId);
        //then
        assertEquals(expectedVspUploadStatusList, actualVspUploadStatusList);
    }

    @Test
    void findByVspIdAndVersionIdAndLockIdSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final var expectedVspUploadStatus = new VspUploadStatus();
        final Result<VspUploadStatus> resultMock = mock(Result.class);
        when(resultMock.one()).thenReturn(expectedVspUploadStatus);
        when(accessor.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId)).thenReturn(resultMock);
        //when
        final Optional<VspUploadStatus> vspUploadStatusOptional =
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
        final List<VspUploadStatus> expectedVspUploadStatusList = List.of(new VspUploadStatus(), new VspUploadStatus());
        final Result<VspUploadStatus> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusList);
        when(accessor.findAllIncomplete(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final List<VspUploadStatus> actualVspUploadStatusList = packageUploadManagerDaoIml.findAllNotComplete(vspId, vspVersionId);
        //then
        assertEquals(expectedVspUploadStatusList, actualVspUploadStatusList);
    }

    @Test
    void findLatestSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final List<VspUploadStatus> expectedVspUploadStatusList = new ArrayList<>();
        IntStream.rangeClosed(1, 31)
            .mapToObj(day -> {
                final VspUploadStatus vspUploadStatus = new VspUploadStatus();
                final Calendar calendar = Calendar.getInstance();
                calendar.set(2022, Calendar.JANUARY, day);
                vspUploadStatus.setCreated(calendar.getTime());
                return vspUploadStatus;
            })
            .forEach(expectedVspUploadStatusList::add);
        final Result<VspUploadStatus> resultMock = mock(Result.class);
        when(resultMock.all()).thenReturn(expectedVspUploadStatusList);

        final VspUploadStatus mostRecentVspUploadStatus = expectedVspUploadStatusList.get(expectedVspUploadStatusList.size() - 1);

        when(accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId)).thenReturn(resultMock);
        //when
        final Optional<VspUploadStatus> vspUploadStatusOptional = packageUploadManagerDaoIml.findLatest(vspId, vspVersionId);
        //then
        assertTrue(vspUploadStatusOptional.isPresent());
        assertEquals(mostRecentVspUploadStatus, vspUploadStatusOptional.get());
    }

}