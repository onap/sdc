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

package org.openecomp.sdcrests.vsp.rest.services;

import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotCreateLock;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotFindLock;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.couldNotUpdateLock;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.uploadAlreadyFinished;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspUploadAlreadyInProgress;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateUploadManagerDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier;
import org.openecomp.sdcrests.vsp.rest.mapping.VspUploadStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Manages the package upload process status.
 */
@Service
public class OrchestrationTemplateCandidateUploadManagerImpl implements OrchestrationTemplateCandidateUploadManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationTemplateCandidateUploadManagerImpl.class);

    private final OrchestrationTemplateCandidateUploadManagerDao uploadManagerDao;
    private final VspUploadStatusMapper vspUploadStatusMapper;
    private final VendorSoftwareProductManager vendorSoftwareProductManager;

    @Autowired
    public OrchestrationTemplateCandidateUploadManagerImpl(
        @Qualifier("package-upload-manager-dao-impl") final OrchestrationTemplateCandidateUploadManagerDao uploadManagerDao) {

        this.uploadManagerDao = uploadManagerDao;
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
        this.vspUploadStatusMapper = new VspUploadStatusMapper();
    }

    //for tests purpose
    OrchestrationTemplateCandidateUploadManagerImpl(final OrchestrationTemplateCandidateUploadManagerDao uploadManagerDao,
                                                    final VendorSoftwareProductManager vendorSoftwareProductManager) {
        this.uploadManagerDao = uploadManagerDao;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
        this.vspUploadStatusMapper = new VspUploadStatusMapper();
    }

    @Override
    public VspUploadStatusDto startUpload(final String vspId, final String vspVersionId, final String user) {
        checkVspExists(vspId, vspVersionId);
        LOGGER.debug("Start uploading for VSP id '{}', version '{}', triggered by user '{}'", vspId, vspVersionId, user);

        final List<VspUploadStatus> incompleteUploadList = uploadManagerDao.findAllNotComplete(vspId, vspVersionId);
        if (!incompleteUploadList.isEmpty()) {
            final CoreException coreException = vspUploadAlreadyInProgress(vspId, vspVersionId).get();
            LOGGER.error(coreException.getMessage());
            throw coreException;
        }

        final VspUploadStatus vspUploadStatus = new VspUploadStatus();
        vspUploadStatus.setStatus(VspUploadStatusType.UPLOADING);
        vspUploadStatus.setVspId(vspId);
        vspUploadStatus.setVspVersionId(vspVersionId);
        vspUploadStatus.setLockId(UUID.randomUUID());
        vspUploadStatus.setCreated(new Date());
        try {
            uploadManagerDao.create(vspUploadStatus);
            LOGGER.debug("Upload lock '{}' created for VSP id '{}', version '{}'", vspUploadStatus.getLockId(), vspId, vspVersionId);
        } catch (final Exception e) {
            final CoreException coreException = couldNotCreateLock(vspId, vspVersionId, e).get();
            LOGGER.error(coreException.getMessage());
            throw coreException;
        }

        return vspUploadStatusMapper.applyMapping(vspUploadStatus, VspUploadStatusDto.class);
    }

    @Override
    public VspUploadStatusDto finishUpload(final String vspId, final String vspVersionId, final UUID lockId, final VspUploadStatusType completeStatus,
                                           final String user) {

        if (!completeStatus.isCompleteStatus()) {
            throw OrchestrationTemplateCandidateUploadManagerExceptionSupplier.invalidCompleteStatus(completeStatus).get();
        }
        final Optional<VspUploadStatus> vspUploadStatusOptional =
            uploadManagerDao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId);
        if (vspUploadStatusOptional.isEmpty()) {
            final CoreException coreException = couldNotFindLock(lockId, vspId, vspVersionId).get();
            LOGGER.error(coreException.getMessage());
            throw coreException;
        }
        final VspUploadStatus vspUploadStatus = vspUploadStatusOptional.get();
        if (vspUploadStatus.getIsComplete()) {
            final CoreException coreException = uploadAlreadyFinished(lockId, vspId, vspVersionId).get();
            LOGGER.error(coreException.getMessage());
            throw coreException;
        }
        LOGGER.debug("Finishing the upload for VSP id '{}', version '{}', lock '{}', triggered by user '{}'",
            vspUploadStatus.getVspId(), vspUploadStatus.getVspVersionId(), vspUploadStatus.getLockId(), user);
        vspUploadStatus.setStatus(completeStatus);
        vspUploadStatus.setUpdated(new Date());
        vspUploadStatus.setIsComplete(true);

        try {
            uploadManagerDao.update(vspUploadStatus);
            LOGGER.debug("Upload complete for VSP '{}', version '{}', lock '{}'",
                vspUploadStatus.getLockId(), vspUploadStatus.getVspId(), vspUploadStatus.getVspVersionId());
        } catch (final Exception e) {
            final CoreException coreException =
                couldNotUpdateLock(vspUploadStatus.getLockId(), vspUploadStatus.getVspId(), vspUploadStatus.getVspVersionId(), e).get();
            LOGGER.error(coreException.getMessage());
            throw coreException;
        }

        return vspUploadStatusMapper.applyMapping(vspUploadStatus, VspUploadStatusDto.class);
    }


    private void checkVspExists(final String vspId, final String vspVersionId) {
        final VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, new Version(vspVersionId));
        if (vspDetails == null) {
            throw OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspNotFound(vspId, vspVersionId).get();
        }
    }

    @Override
    public Optional<VspUploadStatusDto> findLatestStatus(final String vspId, final String vspVersionId, final String user) {
        checkVspExists(vspId, vspVersionId);

        final Optional<VspUploadStatus> vspUploadStatus = uploadManagerDao.findLatest(vspId, vspVersionId);
        if (vspUploadStatus.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(vspUploadStatusMapper.applyMapping(vspUploadStatus.get(), VspUploadStatusDto.class));
    }
}
