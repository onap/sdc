// /*
//  * -
//  *  ============LICENSE_START=======================================================
//  *  Copyright (C) 2022 Nordix Foundation.
//  *  ================================================================================
//  *  Licensed under the Apache License, Version 2.0 (the "License");
//  *  you may not use this file except in compliance with the License.
//  *  You may obtain a copy of the License at
//  *
//  *       http://www.apache.org/licenses/LICENSE-2.0
//  *
//  *  Unless required by applicable law or agreed to in writing, software
//  *  distributed under the License is distributed on an "AS IS" BASIS,
//  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  *  See the License for the specific language governing permissions and
//  *  limitations under the License.
//  *
//  *  SPDX-License-Identifier: Apache-2.0
//  *  ============LICENSE_END=========================================================
//  */

// package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.Mockito.when;

// import java.util.ArrayList;
// import java.util.Calendar;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;
// import java.util.stream.IntStream;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordDaoInternal;
// import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;

// class VspUploadStatusRecordDaoImlTest {

//     @Mock
//     private VspUploadStatusRecordDaoInternal dao;
//     private VspUploadStatusRecordDaoIml packageUploadManagerDaoIml;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         packageUploadManagerDaoIml = new VspUploadStatusRecordDaoIml(dao);
//     }

//     @Test
//     void findAllByVspIdAndVersionIdSuccessTest() {
//     // given
//     final String vspId = "vspId";
//     final String vspVersionId = "vspVersionId";
//     final List<VspUploadStatusRecord> expectedList = List.of(new VspUploadStatusRecord(), new VspUploadStatusRecord());

//     // Mock mapper method directly returning List
//     when(dao.findAllByVspIdAndVersionId(vspId, vspVersionId)).thenReturn(expectedList);

//     // when
//     final List<VspUploadStatusRecord> actualList =
//         packageUploadManagerDaoIml.findAllByVspIdAndVersionId(vspId, vspVersionId);

//     // then
//     assertEquals(expectedList, actualList);
// }

//     @Test
//     void findByVspIdAndVersionIdAndLockIdSuccessTest() {
//     // given
//     final String vspId = "vspId";
//     final String vspVersionId = "vspVersionId";
//     final UUID lockId = UUID.randomUUID();
//     final VspUploadStatusRecord expectedVspUploadStatus = new VspUploadStatusRecord();

//     // Mock the mapper to return Optional directly
//     when(dao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
//     .thenReturn(expectedVspUploadStatus);


//     // when
//     final Optional<VspUploadStatusRecord> vspUploadStatusOptional =
//         packageUploadManagerDaoIml.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId);

//     // then
//     assertTrue(vspUploadStatusOptional.isPresent());
//     assertEquals(expectedVspUploadStatus, vspUploadStatusOptional.get());
// }

//     @Test
//     void findAllNotCompleteSuccessTest() {
//     // given
//     final String vspId = "vspId";
//     final String vspVersionId = "vspVersionId";
//     final List<VspUploadStatusRecord> expectedVspUploadStatusRecordList = List.of(
//         new VspUploadStatusRecord(), new VspUploadStatusRecord()
//     );

//     // Mock the DAO mapper to return a List directly
//     when(dao.findAllIncomplete(vspId, vspVersionId)).thenReturn(expectedVspUploadStatusRecordList);

//     // when
//     final List<VspUploadStatusRecord> actualVspUploadStatusRecordList =
//         packageUploadManagerDaoIml.findAllInProgress(vspId, vspVersionId);

//     // then
//     assertEquals(expectedVspUploadStatusRecordList, actualVspUploadStatusRecordList);
// }


//     @Test
//     void findLatestSuccessTest() {
//     // given
//     final String vspId = "vspId";
//     final String vspVersionId = "vspVersionId";
//     final List<VspUploadStatusRecord> expectedVspUploadStatusRecordList = new ArrayList<>();
    
    // IntStream.rangeClosed(1, 31)
    //     .mapToObj(day -> {
    //         final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
    //         final Calendar calendar = Calendar.getInstance();
    //         calendar.set(2022, Calendar.JANUARY, day);
    //         vspUploadStatusRecord.setCreated(calendar.toInstant());
    //         return vspUploadStatusRecord;
    //     })
    //     .forEach(expectedVspUploadStatusRecordList::add);

//     final VspUploadStatusRecord mostRecentVspUploadStatus = expectedVspUploadStatusRecordList.get(expectedVspUploadStatusRecordList.size() - 1);

//     // Mock the DAO mapper to return the list directly
//     when(dao.findAllByVspIdAndVersionId(vspId, vspVersionId))
//         .thenReturn(expectedVspUploadStatusRecordList);

//     // when
//     final Optional<VspUploadStatusRecord> vspUploadStatusOptional =
//         packageUploadManagerDaoIml.findLatest(vspId, vspVersionId);

//     // then
//     assertTrue(vspUploadStatusOptional.isPresent());
//     assertEquals(mostRecentVspUploadStatus, vspUploadStatusOptional.get());
// }


//     @Test
//     void findLatest_noEntryFoundTest() {
//     // given
//     final String vspId = "vspId";
//     final String vspVersionId = "vspVersionId";

//     // Mock the DAO mapper to return an empty list directly
//     when(dao.findAllByVspIdAndVersionId(vspId, vspVersionId))
//         .thenReturn(new ArrayList<>());

//     // when
//     final Optional<VspUploadStatusRecord> vspUploadStatusOptional =
//         packageUploadManagerDaoIml.findLatest(vspId, vspVersionId);

//     // then
//     assertTrue(vspUploadStatusOptional.isEmpty());
// }

// }