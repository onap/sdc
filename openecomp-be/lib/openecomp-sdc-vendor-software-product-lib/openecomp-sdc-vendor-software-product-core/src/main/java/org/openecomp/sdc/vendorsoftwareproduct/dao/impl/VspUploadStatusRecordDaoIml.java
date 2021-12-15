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
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusAccessor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.springframework.stereotype.Component;

/**
 * Data access object for the package upload process status.
 */
@Component("vsp-upload-status-record-dao-impl")
public class VspUploadStatusRecordDaoIml implements VspUploadStatusRecordDao {

    private final Mapper<VspUploadStatusRecord> mapper;
    private final VspUploadStatusAccessor accessor;

    public VspUploadStatusRecordDaoIml() {
        final MappingManager mappingManager = NoSqlDbFactory.getInstance().createInterface().getMappingManager();
        mapper = mappingManager.mapper(VspUploadStatusRecord.class);
        accessor = mappingManager.createAccessor(VspUploadStatusAccessor.class);
        mappingManager.getSession().getCluster().getConfiguration().getCodecRegistry().register(new EnumNameCodec<>(VspUploadStatus.class));
    }

    //for tests purpose
    VspUploadStatusRecordDaoIml(final VspUploadStatusAccessor accessor) {
        this.accessor = accessor;
        mapper = null;
    }

    @Override
    public void create(final VspUploadStatusRecord vspUploadStatusRecord) {
        mapper.save(vspUploadStatusRecord);
    }

    @Override
    public void update(final VspUploadStatusRecord vspUploadStatusRecord) {
        mapper.save(vspUploadStatusRecord);
    }

    @Override
    public List<VspUploadStatusRecord> findAllByVspIdAndVersionId(final String vspId, final String vspVersionId) {
        final Result<VspUploadStatusRecord> allByVspIdAndVspVersionId = accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId);
        return allByVspIdAndVspVersionId.all();
    }

    @Override
    public Optional<VspUploadStatusRecord> findByVspIdAndVersionIdAndLockId(final String vspId, final String vspVersionId, final UUID lockId) {
        final Result<VspUploadStatusRecord> vspUploadStatusRecordResult = accessor.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId);
        final VspUploadStatusRecord vspUploadStatusRecord = vspUploadStatusRecordResult.one();
        return Optional.ofNullable(vspUploadStatusRecord);
    }

    @Override
    public List<VspUploadStatusRecord> findAllInProgress(final String vspId, final String vspVersionId) {
        final Result<VspUploadStatusRecord> incompleteUploadList = accessor.findAllIncomplete(vspId, vspVersionId);
        return incompleteUploadList.all();
    }

    @Override
    public Optional<VspUploadStatusRecord> findLatest(final String vspId, final String vspVersionId) {
        final List<VspUploadStatusRecord> vspUploadStatusRecordList = accessor.findAllByVspIdAndVspVersionId(vspId, vspVersionId).all();
        vspUploadStatusRecordList.sort(Comparator.comparing(VspUploadStatusRecord::getCreated).reversed());
        return Optional.ofNullable(vspUploadStatusRecordList.get(0));
    }

}
