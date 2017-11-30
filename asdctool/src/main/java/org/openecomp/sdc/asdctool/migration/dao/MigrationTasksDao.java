package org.openecomp.sdc.asdctool.migration.dao;

import java.math.BigInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.CassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import fj.data.Either;

public class MigrationTasksDao extends CassandraDao {

    private static Logger logger = LoggerFactory.getLogger(MigrationTasksDao.class.getName());
    private MigrationTasksAccessor migrationTasksAccessor;
    private Mapper<MigrationTaskEntry> migrationTaskMapper;

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
