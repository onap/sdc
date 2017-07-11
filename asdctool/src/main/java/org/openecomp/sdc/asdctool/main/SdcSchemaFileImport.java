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

package org.openecomp.sdc.asdctool.main;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.openecomp.sdc.asdctool.impl.EsToCassandraDataMigrationConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class SdcSchemaFileImport {
	
	private static SdcSchemaFilesCassandraDao schemaFilesCassandraDao;
	
	public static void main(String[] args) throws Exception {
		
		final String FILE_NAME = "SDC.zip";
		
		if (args == null || args.length < 4) {
			usageAndExit();
		}
		
		String pathAndFile = args[0];
		String sdcReleaseNum = args[1];
		String conformanceLevel = args[2];
		String appConfigDir = args[3];
		
		File file = new File(pathAndFile);
		if(!file.exists()){
			System.out.println("The file or path does not exist");
			System.exit(1);
		} else if(!file.isFile()){
			System.out.println("Specify the file name");
			System.exit(1);
		}

		AnnotationConfigApplicationContext context = initContext(appConfigDir);
		schemaFilesCassandraDao = (SdcSchemaFilesCassandraDao) context.getBean("sdc-schema-files-cassandra-dao");
		
		Path path = Paths.get(pathAndFile);
		byte[] fileBytes = Files.readAllBytes(path);
		
		Date date = new Date();
		String md5Hex = DigestUtils.md5Hex(fileBytes);
		
		SdcSchemaFilesData schemeFileData = new SdcSchemaFilesData(sdcReleaseNum, date, conformanceLevel, FILE_NAME, fileBytes, md5Hex);
		CassandraOperationStatus saveSchemaFile = schemaFilesCassandraDao.saveSchemaFile(schemeFileData);
		
		if(!saveSchemaFile.equals(CassandraOperationStatus.OK)){
			System.out.println("SdcSchemaFileImport failed cassandra error" + saveSchemaFile);
			System.exit(1);
		}
		
		System.out.println("SdcSchemaFileImport successfully completed");
		
		System.exit(0);
	}
	
	private static void usageAndExit(){
		SdcSchemaFileImportUsage();
		System.exit(1);
	}
	
	private static void SdcSchemaFileImportUsage(){
		System.out.println("Usage: <file dir/filename> <SDC release number> <Schema conformance level> <configuration dir>");
	}
	
	private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(EsToCassandraDataMigrationConfig.class);
		return context;
	}
}
