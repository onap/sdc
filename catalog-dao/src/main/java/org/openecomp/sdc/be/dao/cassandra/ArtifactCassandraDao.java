/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import fj.data.Either;
import javax.annotation.PostConstruct;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("artifact-cassandra-dao")
public class ArtifactCassandraDao extends CassandraDao {

    private static Logger logger = Logger.getLogger(ArtifactCassandraDao.class.getName());
    private CqlSession session;
    private ArtifactDao artifactDao; 

    @Autowired
    public ArtifactCassandraDao(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    public void init() {
        String keyspace = AuditingTypesConstants.ARTIFACT_KEYSPACE;
        if (client.isConnected()) {
            Either<CqlSession, CassandraOperationStatus> result = client.connect(keyspace);
            if (result.isLeft()) {
                session = result.left().value();
                ArtifactDaoMapper artifactMapper = new ArtifactDaoMapperBuilder(session).build();
                artifactDao = artifactMapper.artifactDao(keyspace);
                logger.info("** ArtifactCassandraDao created");
            } else {
                logger.info("** ArtifactCassandraDao failed");
                throw new RuntimeException("Artifact keyspace [" + keyspace + "] failed to connect with error : " + result.right().value());
            }
        } else {
            logger.info("** Cassandra client isn't connected");
            logger.info("** ArtifactCassandraDao created, but not connected");
        }
    }

    public CassandraOperationStatus saveArtifact(DAOArtifactData artifact) {
    try {
        artifactDao.save(artifact);
        return CassandraOperationStatus.OK;
    } catch (Exception e) {
        logger.error("Failed to save artifact", e);
        return CassandraOperationStatus.GENERAL_ERROR;
    }
}

   public Either<DAOArtifactData, CassandraOperationStatus> getArtifact(String artifactId) {
    try {
        DAOArtifactData artifact = artifactDao.findById(artifactId);
        if (artifact == null) {
            return Either.right(CassandraOperationStatus.NOT_FOUND);
        }
        return Either.left(artifact);
    } catch (Exception e) {
        logger.error("Failed to get artifact by id {}", artifactId, e);
        return Either.right(CassandraOperationStatus.GENERAL_ERROR);
    }
}

    
public CassandraOperationStatus deleteArtifact(String artifactId) {
    try {
        DAOArtifactData artifact = artifactDao.findById(artifactId);
        if (artifact == null) {
            return CassandraOperationStatus.NOT_FOUND;
        }
        artifactDao.delete(artifact);
        return CassandraOperationStatus.OK;
    } catch (Exception e) {
        logger.error("Failed to delete artifact by id {}", artifactId, e);
        return CassandraOperationStatus.GENERAL_ERROR;
    }
}

    /**
     * ---------for use in JUnit only--------------- the method deletes all the tables in the audit keyspace
     *
     * @return the status of the last failed operation or ok if all the deletes were successful
     */
    public CassandraOperationStatus deleteAllArtifacts() {
        logger.info("cleaning all artifacts.");
        String query = "truncate sdcartifact.resources;";
        try {
            session.execute(query);
        } catch (Exception e) {
            logger.debug("Failed to clean artifacts", e);
            return CassandraOperationStatus.GENERAL_ERROR;
        }
        logger.info("cleaning all artifacts finished successfully.");
        return CassandraOperationStatus.OK;
    }

    /**
     * the method checks if the given table is empty in the artifact keyspace
     *
     * @param tableName the name of the table we want to check
     * @return true if the table is empty
     */
    public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
        return super.isTableEmpty(tableName);
    }

    public Either<Long, CassandraOperationStatus> getCountOfArtifactById(String uniqeId) {
        ResultSet artifactCount = artifactDao.getNumOfArtifactsById(uniqeId);
        if (artifactCount == null) {
            return Either.right(CassandraOperationStatus.NOT_FOUND);
        }
        return Either.left(artifactCount.one().getLong(0));
    }
}
