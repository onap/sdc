/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.asdctool.migration.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.CassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;

@Service
public class MigrationTasksDao extends CassandraDao {

    private static Logger logger = Logger.getLogger(MigrationTasksDao.class.getName());
    private MigrationTasksAccessor migrationTasksAccessor;
    private Mapper<MigrationTaskEntry> migrationTaskMapper;

    @Autowired
    public MigrationTasksDao(CassandraClient cassandraClient){
        super(cassandraClient);
    }

    @PostConstruct
    public void init() {
        String keyspace = AuditingTypesConstants.REPO_KEYSPACE;
        if (client.isConnected()) {
            Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result = client.connect(keyspace);
            if (result.isLeft()) {
                session = result.left().value().left;
                manager = result.left().value().right;
                migrationTasksAccessor = manager.createAccessor(MigrationTasksAccessor.class);
                migrationTaskMapper =  manager.mapper(MigrationTaskEntry.class);
                logger.info("** migrationTasksAccessor created");
            } else {
                logger.info("** migrationTasksAccessor failed");
                throw new RuntimeException("Artifact keyspace [" + keyspace + "] failed to connect with error : "
                        + result.right().value());
            }
        } else {
            logger.info("** Cassandra client isn't connected");
            logger.info("** migrationTasksAccessor created, but not connected");
        }
    }

    public BigInteger getLatestMinorVersion(BigInteger majorVersion) {
        try {
            ResultSet latestMinorVersion = migrationTasksAccessor.getLatestMinorVersion(majorVersion.longValue());
            Row minorVersionRow = latestMinorVersion.one();
            return minorVersionRow == null ? BigInteger.valueOf(Long.MIN_VALUE) : BigInteger.valueOf(minorVersionRow.getLong(0));
        } catch (RuntimeException e) {
            logger.error("failed to get latest minor version for major version {}", majorVersion,  e);
            throw e;
        }
    }

    public void deleteAllTasksForVersion(BigInteger majorVersion) {
        try {
            migrationTasksAccessor.deleteTasksForMajorVersion(majorVersion.longValue());
        } catch (RuntimeException e) {
            logger.error("failed to delete tasks for major version {}", majorVersion,  e);
            throw e;
        }
    }

    public void createMigrationTask(MigrationTaskEntry migrationTask) {
        migrationTaskMapper.save(migrationTask);
    }


}
