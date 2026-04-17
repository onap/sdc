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

import com.datastax.oss.driver.api.core.CqlSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordDaoInternal;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordMapper;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspUploadStatusRecordMapperBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;
import org.springframework.stereotype.Component;
import com.datastax.oss.driver.api.core.PagingIterable;

/**
 * Data access object for the package upload process status.
 */
@Component("vsp-upload-status-record-dao-impl")
public class VspUploadStatusRecordDaoIml implements VspUploadStatusRecordDao {

    private final VspUploadStatusRecordDaoInternal dao;

     public VspUploadStatusRecordDaoIml() {
        CqlSession session = CassandraSessionFactory.getSession();
        VspUploadStatusRecordMapper mapper =
                new VspUploadStatusRecordMapperBuilder(session).build();
        this.dao = mapper.vspUploadStatusRecordDaoInternal();
    }

    // For testing
    VspUploadStatusRecordDaoIml(VspUploadStatusRecordDaoInternal dao) {
    this.dao = dao;
    }

    @Override
    public void create(final VspUploadStatusRecord vspUploadStatusRecord) {
        dao.save(vspUploadStatusRecord);
    }

    @Override
    public void update(final VspUploadStatusRecord vspUploadStatusRecord) {
        dao.save(vspUploadStatusRecord);
    }

    @Override
    public PagingIterable<VspUploadStatusRecord> findAllByVspIdAndVersionId(final String vspId, final String vspVersionId) {
         return dao.findAllByVspIdAndVersionId(vspId, vspVersionId);
    }

    @Override
    public Optional<VspUploadStatusRecord> findByVspIdAndVersionIdAndLockId(final String vspId, final String vspVersionId, final UUID lockId) {
        return Optional.ofNullable(dao.findByVspIdAndVersionIdAndLockId(vspId, vspVersionId, lockId));
    }

    @Override
    public PagingIterable<VspUploadStatusRecord> findAllInProgress(final String vspId, final String vspVersionId) {
       return dao.findAllIncomplete(vspId, vspVersionId);
    }

    @Override
    public Optional<VspUploadStatusRecord> findLatest(final String vspId, final String vspVersionId) {
        PagingIterable<VspUploadStatusRecord> iterable = dao.findAllByVspIdAndVersionId(vspId, vspVersionId);
        List<VspUploadStatusRecord> list = new ArrayList<>();
        iterable.forEach(list::add);

        list.sort(Comparator.comparing(VspUploadStatusRecord::getCreated,
            Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        list.forEach(r -> System.out.println("[DEBUG] Record: status=" + r.getStatusEnum()
            + ", created=" + r.getCreated() + ", updated=" + r.getUpdated()));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }



}
