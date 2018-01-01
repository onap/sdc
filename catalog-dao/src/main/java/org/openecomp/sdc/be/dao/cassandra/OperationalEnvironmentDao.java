package org.openecomp.sdc.be.dao.cassandra;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

import fj.data.Either;

@Component("operational-environment-dao")
public class OperationalEnvironmentDao extends CassandraDao {

    private static Logger logger = LoggerFactory.getLogger(OperationalEnvironmentDao.class.getName());
    private OperationalEnvironmentsAccessor operationalEnvironmentsAccessor;

    public OperationalEnvironmentDao() {
        super();
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
                logger.info("** OperationalEnvironmentDao created");
            } else {
                logger.info("** OperationalEnvironmentDao failed");
                throw new RuntimeException("OperationalEnvironment keyspace [" + keyspace + "] failed to connect with error : "
                        + result.right().value());
            }
        } else {
            logger.info("** Cassandra client isn't connected");
            logger.info("** OperationalEnvironmentDao created, but not connected");
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
