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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

import fj.data.Either;

@Component("artifact-cassandra-dao")
public class ArtifactCassandraDao extends CassandraDao {

	private static Logger logger = LoggerFactory.getLogger(ArtifactCassandraDao.class.getName());
	private ArtifactAccessor artifactAccessor;

	public ArtifactCassandraDao() {
		super();

	}

	@PostConstruct
	public void init() {
		String keyspace = AuditingTypesConstants.ARTIFACT_KEYSPACE;
		if (client.isConnected()) {
			Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result = client.connect(keyspace);
			if (result.isLeft()) {
				session = result.left().value().left;
				manager = result.left().value().right;
				artifactAccessor = manager.createAccessor(ArtifactAccessor.class);
				logger.info("** ArtifactCassandraDao created");
			} else {
				logger.info("** ArtifactCassandraDao failed");
				throw new RuntimeException("Artifact keyspace [" + keyspace + "] failed to connect with error : "
						+ result.right().value());
			}
		} else {
			logger.info("** Cassandra client isn't connected");
			logger.info("** ArtifactCassandraDao created, but not connected");
		}
	}

	public CassandraOperationStatus saveArtifact(ESArtifactData artifact) {
		return client.save(artifact, ESArtifactData.class, manager);
	}

	public Either<ESArtifactData, CassandraOperationStatus> getArtifact(String artifactId) {
		return client.getById(artifactId, ESArtifactData.class, manager);
	}

	public CassandraOperationStatus deleteArtifact(String artifactId) {
		return client.delete(artifactId, ESArtifactData.class, manager);
	}

	/**
	 * ---------for use in JUnit only--------------- the method deletes all the
	 * tables in the audit keyspace
	 * 
	 * @return the status of the last failed operation or ok if all the deletes
	 *         were successful
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
		logger.info("cleaning all artifacts finished succsesfully.");
		return CassandraOperationStatus.OK;
	}

	/**
	 * the method checks if the given table is empty in the artifact keyspace
	 * 
	 * @param tableName
	 *            the name of the table we want to check
	 * @return true if the table is empty
	 */
	public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
		return super.isTableEmpty(tableName);
	}

	public Either<Long, CassandraOperationStatus> getCountOfArtifactById(String uniqeId) {
		ResultSet artifactCount = artifactAccessor.getNumOfArtifactsById(uniqeId);
		if (artifactCount == null) {
			return Either.right(CassandraOperationStatus.NOT_FOUND);
		}
		return Either.left(artifactCount.one().getLong(0));
	}

}
