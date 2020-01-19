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

package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("operational-environment-dao")
public class OperationalEnvironmentDao extends CassandraDao {

    private static Logger logger = Logger.getLogger(OperationalEnvironmentDao.class.getName());
    private OperationalEnvironmentsAccessor operationalEnvironmentsAccessor;

    @Autowired
    public OperationalEnvironmentDao(CassandraClient cassandraClient) {
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
                operationalEnvironmentsAccessor = manager.createAccessor(OperationalEnvironmentsAccessor.class);
                logger.debug("** OperationalEnvironmentDao created");
            } else {
                logger.error(EcompLoggerErrorCode.DATA_ERROR, "OperationalEnvironmentDao", "OperationalEnvironmentDao", "** OperationalEnvironmentDao failed");
                throw new RuntimeException("OperationalEnvironment keyspace [" + keyspace + "] failed to connect with error : "
                        + result.right().value());
            }
        } else {
            logger.error(EcompLoggerErrorCode.DATA_ERROR, "OperationalEnvironmentDao", "OperationalEnvironmentDao", "** Cassandra client isn't connected");
            logger.error(EcompLoggerErrorCode.DATA_ERROR, "OperationalEnvironmentDao", "OperationalEnvironmentDao", "** OperationalEnvironmentDao created, but not connected");
        }
    }
    public CassandraOperationStatus save(OperationalEnvironmentEntry operationalEnvironmentEntry) {
        return client.save(operationalEnvironmentEntry, OperationalEnvironmentEntry.class, manager);
    }

    public Either<OperationalEnvironmentEntry, CassandraOperationStatus> get(String envId) {
        return client.getById(envId, OperationalEnvironmentEntry.class, manager);
    }

    public CassandraOperationStatus delete(String envId) {
        return client.delete(envId, OperationalEnvironmentEntry.class, manager);
    }

    public CassandraOperationStatus deleteAll() {
        logger.info("cleaning all operational environments.");
        String query = "truncate sdcrepository.operationalenvironment;";
        try {
            session.execute(query);
        } catch (Exception e) {
            logger.debug("Failed to clean operational environment", e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        logger.info("cleaning all operational environment finished succsesfully.");
        return CassandraOperationStatus.OK;
    }

    public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
        return super.isTableEmpty(tableName);
    }

    //accessors
    public Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> getByEnvironmentsStatus(EnvironmentStatusEnum status) {
        Result<OperationalEnvironmentEntry> operationalEnvironments = operationalEnvironmentsAccessor.getByEnvironmentsStatus(status.getName());
        if (operationalEnvironments == null) {
            return Either.right(CassandraOperationStatus.NOT_FOUND);
        }
        return Either.left(operationalEnvironments.all());
    }

}
