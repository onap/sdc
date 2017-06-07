package org.openecomp.sdc.be.dao.cassandra;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.ESSdcSchemaFilesData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

import fj.data.Either;

@Component("sdc-schema-files-cassandra-dao")
public class SdcSchemaFilesCassandraDao extends CassandraDao {
	
	private static Logger logger = LoggerFactory.getLogger(SdcSchemaFilesCassandraDao.class.getName());
	private SdcSchemaFilesAccessor sdcSchemaFilesAccessor;
	
	public SdcSchemaFilesCassandraDao() {
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
				sdcSchemaFilesAccessor = manager.createAccessor(SdcSchemaFilesAccessor.class);
				logger.info("** SdcSchemaFilesCassandraDao created");
			} else {
				logger.info("** SdcSchemaFilesCassandraDao failed");
				throw new RuntimeException("Artifact keyspace [" + keyspace + "] failed to connect with error : "
						+ result.right().value());
			}
		} else {
			logger.info("** Cassandra client isn't connected");
			logger.info("** SdcSchemaFilesCassandraDao created, but not connected");
		}
	}
	
	public CassandraOperationStatus saveArtifact(ESSdcSchemaFilesData artifact) {
		return client.save(artifact, ESSdcSchemaFilesData.class, manager);
	}

	public Either<ESSdcSchemaFilesData, CassandraOperationStatus> getArtifact(String artifactId) {
		return client.getById(artifactId, ESSdcSchemaFilesData.class, manager);
	}

	public CassandraOperationStatus deleteArtifact(String artifactId) {
		return client.delete(artifactId, ESSdcSchemaFilesData.class, manager);
	}
	
	public Either<List<ESSdcSchemaFilesData>, ActionStatus> getSpecificSchemaFiles(String sdcreleasenum, String conformancelevel) {		
		Result<ESSdcSchemaFilesData> specificSdcSchemaFiles = sdcSchemaFilesAccessor.getSpecificSdcSchemaFiles(sdcreleasenum, conformancelevel);
		
		if(specificSdcSchemaFiles == null) {
			logger.debug("not found specific SdcSchemaFiles for sdcreleasenum {}, conformancelevel {}", sdcreleasenum, conformancelevel);
			return Either.left(new LinkedList<ESSdcSchemaFilesData>());
		}
		
		if(logger.isDebugEnabled()){
			for (ESSdcSchemaFilesData esSdcSchemaFilesData : specificSdcSchemaFiles) {
				logger.debug(esSdcSchemaFilesData.toString());
			}			
		}
		
		return Either.left(specificSdcSchemaFiles.all());
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
}
