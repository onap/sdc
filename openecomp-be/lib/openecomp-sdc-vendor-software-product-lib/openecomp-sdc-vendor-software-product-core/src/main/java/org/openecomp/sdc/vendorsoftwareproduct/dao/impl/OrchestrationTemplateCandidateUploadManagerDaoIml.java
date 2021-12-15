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

import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageUploadManagerAccessor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateUploadManagerDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;
import org.springframework.stereotype.Component;

/**
 * Data access object for the package upload process status.
 */
@Component("package-upload-manager-dao-impl")
public class OrchestrationTemplateCandidateUploadManagerDaoIml implements OrchestrationTemplateCandidateUploadManagerDao {

    private final Mapper<VspUploadStatus> mapper;
    private final PackageUploadManagerAccessor accessor;

    public OrchestrationTemplateCandidateUploadManagerDaoIml() {
        final MappingManager mappingManager = NoSqlDbFactory.getInstance().createInterface().getMappingManager();
        mapper = mappingManager.mapper(VspUploadStatus.class);
        accessor = mappingManager.createAccessor(PackageUploadManagerAccessor.class);
        mappingManager.getSession().getCluster().getConfiguration().getCodecRegistry().register(new EnumNameCodec<>(VspUploadStatusType.class));
    }

    //for tests purpose
    OrchestrationTemplateCandidateUploadManagerDaoIml(final PackageUploadManagerAccessor accessor) {
        this.accessor = accessor;
        mapper = null;
    }

    @Override
    public void create(final VspUploadStatus vspUploadStatus) {
        mapper.save(vspUploadStatus);
    }

    @Override
    public void update(final VspUploadStatus vspUploadStatus) {
        mapper.save(vspUploadStatus);
    }

    @Override
    public List<VspUploadStatus> findAllByVspIdAndVersionId(final String vspId, final String vspVersionId) {
        final Result<VspUploadStatus> allByVspIdAndVspVersionId = accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId);
        return allByVspIdAndVspVersionId.all();
    }

    @Override
    public Optional<VspUploadStatus> findByVspIdAndVersionIdAndLockId(final String vspId, final String vspVersionId, final UUID lockId) {
        final Result<VspUploadStatus> byVspIdAndVersionIdAndLockIdResult = accessor.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId);
        final VspUploadStatus vspUploadStatus = byVspIdAndVersionIdAndLockIdResult.one();
        return Optional.ofNullable(vspUploadStatus);
    }

    @Override
    public List<VspUploadStatus> findAllNotComplete(final String vspId, final String vspVersionId) {
        final Result<VspUploadStatus> incompleteUploadList = accessor.findAllIncomplete(vspId, vspVersionId);
        return incompleteUploadList.all();
    }

    @Override
    public Optional<VspUploadStatus> findLatest(final String vspId, final String vspVersionId) {
        final List<VspUploadStatus> vspUploadStatusList = accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId).all();
        vspUploadStatusList.sort(Comparator.comparing(VspUploadStatus::getCreated).reversed());
        return Optional.ofNullable(vspUploadStatusList.get(0));
    }

}
