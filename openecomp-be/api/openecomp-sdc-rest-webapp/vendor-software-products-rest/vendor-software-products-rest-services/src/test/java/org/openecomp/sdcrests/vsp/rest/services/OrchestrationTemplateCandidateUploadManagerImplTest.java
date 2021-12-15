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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateUploadManagerDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier;

class OrchestrationTemplateCandidateUploadManagerImplTest {

    @Mock
    private OrchestrationTemplateCandidateUploadManagerDao orchestrationTemplateCandidateUploadManagerDao;
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
        final VspUploadStatusDto vspUploadStatusDto = packageUploadManagerImpl.startUpload(vspId, vspVersionId, username);
        //then
        assertEquals(vspId, vspUploadStatusDto.getVspId());
        assertEquals(vspVersionId, vspUploadStatusDto.getVspVersionId());
        assertEquals(VspUploadStatusType.UPLOADING, vspUploadStatusDto.getStatus());
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
        when(orchestrationTemplateCandidateUploadManagerDao.findAllNotComplete(vspId, vspVersionId)).thenReturn(List.of(new VspUploadStatus()));
        when(vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId))).thenReturn(new VspDetails());
        //when/then
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.startUpload(vspId, vspVersionId, username));
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
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.startUpload(vspId, vspVersionId, username));
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
        doThrow(new RuntimeException()).when(orchestrationTemplateCandidateUploadManagerDao).create(any(VspUploadStatus.class));
        final CoreException actualCoreException =
            assertThrows(CoreException.class, () -> packageUploadManagerImpl.startUpload(vspId, vspVersionId, username));
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
        final VspUploadStatus vspUploadStatus = new VspUploadStatus();
        vspUploadStatus.setVspId(vspId);
        vspUploadStatus.setVspVersionId(vspVersionId);
        vspUploadStatus.setLockId(lockId);
        vspUploadStatus.setStatus(VspUploadStatusType.UPLOADING);
        vspUploadStatus.setCreated(new Date());
        when(orchestrationTemplateCandidateUploadManagerDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatus));
        //when
        final VspUploadStatusDto actualVspUploadStatus = packageUploadManagerImpl
            .finishUpload(vspId, vspVersionId, lockId, VspUploadStatusType.SUCCESS, username);
        //then
        assertEquals(vspId, actualVspUploadStatus.getVspId());
        assertEquals(vspVersionId, actualVspUploadStatus.getVspVersionId());
        assertEquals(VspUploadStatusType.SUCCESS, actualVspUploadStatus.getStatus());
        assertEquals(vspUploadStatus.getLockId(), actualVspUploadStatus.getLockId());
        assertEquals(vspUploadStatus.getCreated(), actualVspUploadStatus.getCreated());
        assertNotNull(actualVspUploadStatus.getUpdated());
        assertTrue(actualVspUploadStatus.isComplete());
    }

    @Test
    void finishUploadIllegalStatusTest() {
        final UUID lockId = UUID.randomUUID();
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
            () -> packageUploadManagerImpl.finishUpload("vspId", "vspVersionId", lockId, VspUploadStatusType.UPLOADING, "username"));
        IllegalArgumentException expectedException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.invalidCompleteStatus(
            VspUploadStatusType.UPLOADING).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());

        actualException = assertThrows(IllegalArgumentException.class,
            () -> packageUploadManagerImpl.finishUpload("vspId", "vspVersionId", lockId, VspUploadStatusType.PROCESSING, "username"));
        expectedException = OrchestrationTemplateCandidateUploadManagerExceptionSupplier.invalidCompleteStatus(
            VspUploadStatusType.PROCESSING).get();
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void finishUploadCouldNotFindLockTest() {
        //given
        final String vspId = "vspId";
        final String vspVersionId = "vspVersionId";
        final UUID lockId = UUID.randomUUID();
        final String username = "username";
        final VspUploadStatus vspUploadStatus = new VspUploadStatus();
        vspUploadStatus.setVspId(vspId);
        vspUploadStatus.setVspVersionId(vspVersionId);
        vspUploadStatus.setLockId(lockId);
        vspUploadStatus.setStatus(VspUploadStatusType.UPLOADING);
        vspUploadStatus.setCreated(new Date());
        when(orchestrationTemplateCandidateUploadManagerDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.empty());
        //when/then
        final CoreException actualException = assertThrows(CoreException.class,
            () -> packageUploadManagerImpl.finishUpload(vspId, vspVersionId, lockId, VspUploadStatusType.SUCCESS, username));

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
        final VspUploadStatus vspUploadStatus = new VspUploadStatus();
        vspUploadStatus.setVspId(vspId);
        vspUploadStatus.setVspVersionId(vspVersionId);
        vspUploadStatus.setLockId(lockId);
        vspUploadStatus.setIsComplete(true);
        when(orchestrationTemplateCandidateUploadManagerDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatus));
        //when/then
        final CoreException actualException = assertThrows(CoreException.class, () -> packageUploadManagerImpl
            .finishUpload(vspId, vspVersionId, lockId, VspUploadStatusType.SUCCESS, username));

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
        final VspUploadStatus vspUploadStatus = new VspUploadStatus();
        vspUploadStatus.setVspId(vspId);
        vspUploadStatus.setVspVersionId(vspVersionId);
        vspUploadStatus.setLockId(lockId);
        vspUploadStatus.setStatus(VspUploadStatusType.UPLOADING);
        vspUploadStatus.setCreated(new Date());
        when(orchestrationTemplateCandidateUploadManagerDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId))
            .thenReturn(Optional.of(vspUploadStatus));
        doThrow(new RuntimeException()).when(orchestrationTemplateCandidateUploadManagerDao).update(vspUploadStatus);
        //when/then
        final CoreException actualException = assertThrows(CoreException.class, () -> packageUploadManagerImpl
            .finishUpload(vspId, vspVersionId, lockId, VspUploadStatusType.SUCCESS, username));

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
        final var expectedVspUploadStatus = new VspUploadStatus();
        expectedVspUploadStatus.setStatus(VspUploadStatusType.UPLOADING);
        expectedVspUploadStatus.setLockId(UUID.randomUUID());
        expectedVspUploadStatus.setVspId(vspId);
        expectedVspUploadStatus.setVspVersionId(vspVersionId);
        expectedVspUploadStatus.setCreated(new Date());
        expectedVspUploadStatus.setUpdated(new Date());
        expectedVspUploadStatus.setIsComplete(true);
        when(orchestrationTemplateCandidateUploadManagerDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.of(expectedVspUploadStatus));
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
        when(orchestrationTemplateCandidateUploadManagerDao.findLatest(vspId, vspVersionId)).thenReturn(Optional.empty());
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

}