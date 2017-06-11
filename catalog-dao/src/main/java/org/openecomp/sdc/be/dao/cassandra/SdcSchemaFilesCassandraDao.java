package org.openecomp.sdc.be.dao.cassandra;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
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
	
	public CassandraOperationStatus saveSchemaFile(SdcSchemaFilesData schemaFileData) {
		return client.save(schemaFileData, SdcSchemaFilesData.class, manager);
	}

	public Either<SdcSchemaFilesData, CassandraOperationStatus> getSchemaFile(String schemaFileId) {
		return client.getById(schemaFileId, SdcSchemaFilesData.class, manager);
	}

	public CassandraOperationStatus deleteSchemaFile(String schemaFileId) {
		return client.delete(schemaFileId, SdcSchemaFilesData.class, manager);
	}
	
	public Either<List<SdcSchemaFilesData>, CassandraOperationStatus> getSpecificSchemaFiles(String sdcreleasenum, String conformancelevel) {		
		
		Result<SdcSchemaFilesData> specificSdcSchemaFiles = null;
		try {
			specificSdcSchemaFiles = sdcSchemaFilesAccessor.getSpecificSdcSchemaFiles(sdcreleasenum, conformancelevel);
		} catch (Exception e) {
			logger.debug("getSpecificSchemaFiles failed with exception {}", e);
			return Either.right(CassandraOperationStatus.GENERAL_ERROR);
		}
		
		if(specificSdcSchemaFiles == null) {
			logger.debug("not found specific SdcSchemaFiles for sdcreleasenum {}, conformancelevel {}", sdcreleasenum, conformancelevel);
			return Either.right(CassandraOperationStatus.NOT_FOUND);
		}
		
		List<SdcSchemaFilesData> list = specificSdcSchemaFiles.all();
		if(logger.isDebugEnabled()){
			for (SdcSchemaFilesData esSdcSchemaFilesData : list) {
				logger.trace(esSdcSchemaFilesData.toString());
			}			
		}
		
		return Either.left(list);
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
