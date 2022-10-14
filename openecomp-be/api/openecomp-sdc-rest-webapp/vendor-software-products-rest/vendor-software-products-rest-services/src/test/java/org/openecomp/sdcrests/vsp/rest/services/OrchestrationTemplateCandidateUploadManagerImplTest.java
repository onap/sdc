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

package org.openecomp.sdcrests.vsp.rest.services;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.alreadyInStatusBeingUpdated;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotFindStatus;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotUpdateStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier;

class OrchestrationTemplateCandidateUploadManagerImplTest {

    @Mock
    private VspUploadStatusRecordDao vspUploadStatusRecordDao;
    @Mock
    private VendorSoftwareProductManager vendorSoftwareProductManager;
    @InjectMocks
    private OrchestrationTemplateCandidateUploadManagerImpl packageUploadManagerImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startUploadSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        //when
        final VspUploadStatusDto vspUploadStatusDto = packageUploadManagerImpl.putUploadInProgress(vspId, vspVersionId, username);
        //then
        assertEquals(vspId, vspUploadStatusDto.getVspId());
        assertEquals(vspVersionId, vspUploadStatusDto.getVspVersionId());
        assertEquals(VspUploadStatus.UPLOADING, vspUploadStatusDto.getStatus());
        assertFalse(vspUploadStatusDto.isComplete());
        assertNotNull(vspUploadStatusDto.getLockId());
        assertNotNull(vspUploadStatusDto.getCreated());
    }

    @Test
    void startUpload_uploadAlreadyInProgressTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        when(vspUploadStatusRecordDao.findAllInProgress(vspId, vspVersionId)).thenReturn(List.of(new VspUploadStatusRecord()));
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        //when/then
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.putUploadInProgress(vspId, vspVersionId, username));
        final CoreException expectedCoreException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspUploadAlreadyInProgress(vspId,
            vspVersionId).get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.code().message(), actualCoreException.code().message());
    }

    @Test
    void startUpload_vspNotFoundTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        //when/then
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.putUploadInProgress(vspId, vspVersionId, username));
        final CoreException expectedCoreException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspNotFound(vspId, vspVersionId)
            .get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.code().message(), actualCoreException.code().message());
    }

    @Test
    void startUpload_createLockErrorTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        //when/then
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        doThrow(new RuntimeException()).when(vspUploadStatusRecordDao).create(any(VspUploadStatusRecord.class));
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.putUploadInProgress(vspId, vspVersionId, username));
        final CoreException expectedCoreException =
            OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotCreateLock(vspId, vspVersionId, new RuntimeException()).get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.code().message(), actualCoreException.code().message());
    }

    @Test
    void finishUploadSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        vspUploadStatusRecord.setCreated(new Date());
        when(vspUploadStatusRecordDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatusRecord));
        //when
        final VspUploadStatusDto actualVspUploadStatus = packageUploadManagerImpl
            .putUploadAsFinished(vspId, vspVersionId, lockId, VspUploadStatus.SUCCESS, username);
        //then
        assertEquals(vspId, actualVspUploadStatus.getVspId());
        assertEquals(vspVersionId, actualVspUploadStatus.getVspVersionId());
        assertEquals(VspUploadStatus.SUCCESS, actualVspUploadStatus.getStatus());
        assertEquals(vspUploadStatusRecord.getLockId(), actualVspUploadStatus.getLockId());
        assertEquals(vspUploadStatusRecord.getCreated(), actualVspUploadStatus.getCreated());
        assertNotNull(actualVspUploadStatus.getUpdated());
        assertTrue(actualVspUploadStatus.isComplete());
    }

    @Test
    void finishUploadIllegalStatusTest() {
        final UUID lockId = UUID.randomUUID();
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
            () -> packageUploadManagerImpl.putUploadAsFinished("vspId", "vspVersionId", lockId, VspUploadStatus.UPLOADING, "username"));
        IllegalArgumentException expectedException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.invalidCompleteStatus(
            VspUploadStatus.UPLOADING).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());

        actualException = assertThrows(IllegalArgumentException.class,
            () -> packageUploadManagerImpl
                .putUploadAsFinished("vspId", "vspVersionId", lockId, VspUploadStatus.PROCESSING, "username")
        );
        expectedException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.invalidCompleteStatus(
            VspUploadStatus.PROCESSING).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void finishUploadCouldNotFindLockTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        vspUploadStatusRecord.setCreated(new Date());
        when(vspUploadStatusRecordDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.empty());
        //when/then
        final CoreException actualException = assertThrows(CoreException.class,
            () -> packageUploadManagerImpl.putUploadAsFinished(vspId, vspVersionId, lockId, VspUploadStatus.SUCCESS, username));

        final CoreException expectedException =
            OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotFindLock(lockId, vspId, vspVersionId).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void finishUpload_uploadAlreadyFinishedTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setIsComplete(true);
        when(vspUploadStatusRecordDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatusRecord));
        //when/then
        final CoreException actualException = assertThrows(CoreException.class, () -> packageUploadManagerImpl
            .putUploadAsFinished(vspId, vspVersionId, lockId, VspUploadStatus.SUCCESS, username));

        final CoreException expectedException =
            OrchestrationTemplateCandidateUploadManagerExceptionSupplier.uploadAlreadyFinished(lockId, vspId, vspVersionId).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void finishUploadCouldNotUpdateLockTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        vspUploadStatusRecord.setCreated(new Date());
        when(vspUploadStatusRecordDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatusRecord));
        doThrow(new RuntimeException()).when(vspUploadStatusRecordDao).update(vspUploadStatusRecord);
        //when/then
        final CoreException actualException = assertThrows(CoreException.class, () -> packageUploadManagerImpl
            .putUploadAsFinished(vspId, vspVersionId, lockId, VspUploadStatus.SUCCESS, username));

        final CoreException expectedException =
            OrchestrationTemplateCandidateUploadManagerExceptionSupplier
                .couldNotUpdateLock(lockId, vspId, vspVersionId, new RuntimeException()).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void findLatestSuccessTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        final var expectedVspUploadStatus = new VspUploadStatusRecord();
        expectedVspUploadStatus.setStatus(VspUploadStatus.UPLOADING);
        expectedVspUploadStatus.setLockId(UUID.randomUUID());
        expectedVspUploadStatus.setVspId(vspId);
        expectedVspUploadStatus.setVspVersionId(vspVersionId);
        expectedVspUploadStatus.setCreated(new Date());
        expectedVspUploadStatus.setUpdated(new Date());
        expectedVspUploadStatus.setIsComplete(true);
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(expectedVspUploadStatus));
        //when
        final Optional<VspUploadStatusDto> actualVspUploadStatusDtoOpt = packageUploadManagerImpl.findLatestStatus(vspId, vspVersionId, username);
        //then
        assertTrue(actualVspUploadStatusDtoOpt.isPresent());
        final VspUploadStatusDto actualVspUploadStatusDto = actualVspUploadStatusDtoOpt.get();
        assertEquals(expectedVspUploadStatus.getVspId(), actualVspUploadStatusDto.getVspId());
        assertEquals(expectedVspUploadStatus.getVspVersionId(), actualVspUploadStatusDto.getVspVersionId());
        assertEquals(expectedVspUploadStatus.getLockId(), actualVspUploadStatusDto.getLockId());
        assertEquals(expectedVspUploadStatus.getIsComplete(), actualVspUploadStatusDto.isComplete());
        assertEquals(expectedVspUploadStatus.getCreated(), actualVspUploadStatusDto.getCreated());
        assertEquals(expectedVspUploadStatus.getUpdated(), actualVspUploadStatusDto.getUpdated());
        assertEquals(expectedVspUploadStatus.getStatus(), actualVspUploadStatusDto.getStatus());
    }

    @Test
    void findLatest_noStatusFoundTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.empty());
        //when
        final Optional<VspUploadStatusDto> actualVspUploadStatusDtoOpt = packageUploadManagerImpl.findLatestStatus(vspId, vspVersionId, username);
        //then
        assertTrue(actualVspUploadStatusDtoOpt.isEmpty());
    }

    @Test
    void findLatest_vspNotFoundTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final String username = "username";
        //when/then
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.findLatestStatus(vspId, vspVersionId, username));
        final CoreException expectedCoreException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspNotFound(vspId, vspVersionId)
            .get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.code().message(), actualCoreException.code().message());
    }

    @Test
    void startValidationSuccessTest() throws ParseException {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final Date created = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/1900");
        final Date updated = new SimpleDateFormat("dd/MM/yyyy").parse("02/01/1900");
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setCreated(created);
        vspUploadStatusRecord.setUpdated(updated);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(vspUploadStatusRecord));
        //when
        final VspUploadStatusDto vspUploadStatusDto = packageUploadManagerImpl.putUploadInValidation(vspId, vspVersionId, username);
        //then
        assertEquals(VspUploadStatus.VALIDATING, vspUploadStatusDto.getStatus());
        assertNotEquals(updated, vspUploadStatusDto.getUpdated());
        assertEquals(vspId, vspUploadStatusDto.getVspId());
        assertEquals(vspVersionId, vspUploadStatusDto.getVspVersionId());
        assertEquals(lockId, vspUploadStatusDto.getLockId());
        assertEquals(created, vspUploadStatusDto.getCreated());
        assertFalse(vspUploadStatusDto.isComplete());
    }

    @Test
    void startProcessingSuccessTest() throws ParseException {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final Date created = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/1900");
        final Date updated = new SimpleDateFormat("dd/MM/yyyy").parse("02/01/1900");
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setLockId(lockId);
        vspUploadStatusRecord.setCreated(created);
        vspUploadStatusRecord.setUpdated(updated);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(vspUploadStatusRecord));
        //when
        final VspUploadStatusDto vspUploadStatusDto = packageUploadManagerImpl.putUploadInProcessing(vspId, vspVersionId, username);
        //then
        assertEquals(VspUploadStatus.PROCESSING, vspUploadStatusDto.getStatus());
        assertNotEquals(updated, vspUploadStatusDto.getUpdated());
        assertEquals(vspId, vspUploadStatusDto.getVspId());
        assertEquals(vspVersionId, vspUploadStatusDto.getVspVersionId());
        assertEquals(lockId, vspUploadStatusDto.getLockId());
        assertEquals(created, vspUploadStatusDto.getCreated());
        assertFalse(vspUploadStatusDto.isComplete());
    }


    @Test
    void startProcessing_statusNotFoundTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.empty());
        //when/then
        final CoreException actualCoreException = assertThrows(CoreException.class,
            () -> packageUploadManagerImpl.putUploadInProcessing(vspId, vspVersionId, "username"));

        final CoreException expectedCoreException = couldNotFindStatus(vspId, vspVersionId).get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.getMessage(), actualCoreException.getMessage());
    }

    @Test
    void startProcessing_alreadyInGivenStatusTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final VspUploadStatus processingStatus = VspUploadStatus.PROCESSING;
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setStatus(processingStatus);
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(vspUploadStatusRecord));

        //when/then
        final CoreException actualCoreException = assertThrows(CoreException.class,
            () -> packageUploadManagerImpl.putUploadInProcessing(vspId, vspVersionId, "username"));

        final CoreException expectedCoreException = alreadyInStatusBeingUpdated(vspId, vspVersionId, processingStatus).get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.getMessage(), actualCoreException.getMessage());
    }

    @Test
    void updateStatus_couldNotUpdateTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final VspUploadStatusRecord vspUploadStatusRecord = new VspUploadStatusRecord();
        vspUploadStatusRecord.setVspId(vspId);
        vspUploadStatusRecord.setVspVersionId(vspVersionId);
        vspUploadStatusRecord.setStatus(VspUploadStatus.UPLOADING);
        when(vspUploadStatusRecordDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(vspUploadStatusRecord));
        final RuntimeException exception = new RuntimeException("test");
        doThrow(exception).when(vspUploadStatusRecordDao).update(vspUploadStatusRecord);

        //when/then
        final CoreException actualCoreException = assertThrows(CoreException.class,
            () -> packageUploadManagerImpl.putUploadInProcessing(vspId, vspVersionId, "username"));

        final CoreException expectedCoreException = couldNotUpdateStatus(vspId, vspVersionId, VspUploadStatus.PROCESSING, exception).get();
        assertEquals(expectedCoreException.code().id(), actualCoreException.code().id());
        assertEquals(expectedCoreException.getMessage(), actualCoreException.getMessage());
    }

}
