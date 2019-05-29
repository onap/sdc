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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum;
import org.openecomp.sdc.asdctool.impl.EsToCassandraDataMigrationConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


public class SdcSchemaFileImport {
	
	private static final String SEPARATOR = FileSystems.getDefault().getSeparator();
	
	private static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
		
	private static String importToscaPath;
	
	private static final byte[] buffer = new byte[1024];
	
	private static final String YAML_EXTENSION = ".yml";

	private static final String DEPLOYMENT_TYPE_ONAP = "onap";
	
	private static String LICENSE_TXT;
	
	private static ZipOutputStream zos; 
	
	public static void main(String[] args) throws Exception {
		
		//Generation flow start - generating SDC from normatives
		System.out.println("Starting SdcSchemaFileImport procedure...");
		final String FILE_NAME = "SDC.zip";
		
		if (args == null || !(args.length ==4 || args.length == 5 )) {
			usageAndExit();
		}
		
		importToscaPath = args[0];
		String sdcReleaseNum = args[1];
		String conformanceLevel = args[2];
		String appConfigDir = args[3];
		String deploymentType=null;
		if(args.length==5){
			deploymentType=args[4];
		}

				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		zos = new ZipOutputStream(baos);
	
		//Initialize the license text
		try {
			LICENSE_TXT = new String(Files.readAllBytes(Paths.get(appConfigDir + SEPARATOR+"license.txt")));
		}
		catch(Exception e)  {
			System.err.println("Couldn't read license.txt in location :" + appConfigDir+", error: "+e);
			System.exit(1);
		}
		
		//Loop over schema file list and create each yaml file from /import/tosca folder 
		SchemaZipFileEnum[] schemaFileList = SchemaZipFileEnum.values();
		for (SchemaZipFileEnum schemaZipFileEnum : schemaFileList) {
			String pathname = importToscaPath + SEPARATOR + schemaZipFileEnum.getSourceFolderName() + SEPARATOR +  schemaZipFileEnum.getSourceFileName() + YAML_EXTENSION;
			try(InputStream input = new FileInputStream(new File(pathname));) {
				//get the source yaml file
				System.out.println("Processing file "+pathname+"....");
				//Convert the content of file to yaml 
				Yaml yamlFileSource = new Yaml();
			    Object content = yamlFileSource.load(input);
			    
			    createAndSaveSchemaFileYaml(schemaZipFileEnum, content);
			}
			catch(Exception e)  {
				System.err.println("Error in file creation : " + schemaZipFileEnum.getFileName() + ", " + e.getMessage());
				System.exit(1);
			}
		}
		
		createAndSaveNodeSchemaFile(deploymentType);
		
    	try  {
    		//close the ZipOutputStream
    		zos.close();
    		System.out.println("File SDC.zip creation successful");
    		
    	}	catch(Exception ex)  {
    		System.err.println("Failed to pack SDC.zip file, error: "+ex);
    		System.exit(1);
    	}
		
    	//Generation flow end - generating SDC from narratives
				
		AnnotationConfigApplicationContext context = initContext(appConfigDir);
        SdcSchemaFilesCassandraDao schemaFilesCassandraDao = (SdcSchemaFilesCassandraDao) context.getBean("sdc-schema-files-cassandra-dao");
		
		byte[] fileBytes = baos.toByteArray();

		Date date = new Date();
		String md5Hex = DigestUtils.md5Hex(fileBytes);
		
		SdcSchemaFilesData schemeFileData = new SdcSchemaFilesData(sdcReleaseNum, date, conformanceLevel, FILE_NAME, fileBytes, md5Hex);
		CassandraOperationStatus saveSchemaFile = schemaFilesCassandraDao.saveSchemaFile(schemeFileData);
		
		if(!saveSchemaFile.equals(CassandraOperationStatus.OK))  {
			System.err.println("SdcSchemaFileImport failed cassandra error" + saveSchemaFile);
			System.exit(1);
		}
		
		System.out.println("SdcSchemaFileImport successfully completed");
		
		System.exit(0);
	}
	
	public static void createAndSaveSchemaFileYaml(SchemaZipFileEnum schemaZipFileEnum, Object content) {	
		createAndSaveSchemaFileYaml(schemaZipFileEnum.getFileName(), schemaZipFileEnum.getImportFileList(), schemaZipFileEnum.getCollectionTitle(), content);
	}
	
	public static void createAndSaveSchemaFileYaml(String fileName, String[] importFileList, String collectionTitle, Object content) {
	    
		//Initialize the snake yaml dumper option
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		
	    //Create the new yaml
		Yaml yaml = new Yaml(options);
		yaml.setName(fileName);
		 
		//Initialize the yaml contents
		Map<String, Object> data = new LinkedHashMap<>();
					
		data.put("tosca_definitions_version", TOSCA_VERSION);
		
		if (importFileList.length > 0)  {
			data.put("imports", importFileList);
		}
		
		data.put(collectionTitle, content);
		
		//Save the new yaml to file
		try {
		
			FileWriter writer;
			File file = File.createTempFile(fileName, YAML_EXTENSION);
			writer = new FileWriter(file);
			
			//Add the license as comment in top of file
			writer.write(LICENSE_TXT);
			
			yaml.dump(data, writer);
			
			writer.close();
			
			// begin writing a new ZIP entry, positions the stream to the start of the entry data
			ZipEntry entry = new ZipEntry(yaml.getName() + YAML_EXTENSION);
			zos.putNextEntry(entry);
			FileInputStream stream = new FileInputStream(file.getAbsolutePath());
    		int len;
    		while ((len = stream.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
    		//close the InputStream
            file.delete();
            stream.close();
    		zos.closeEntry();

    		
		} catch (IOException e) {
			System.out.println("Error in file creation : " + fileName + ", " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 *the method is responsible for creating and storing the sdc normatives in the DB
	 * @param deploymentType if the deployments type is onap the onap narratives will be add to the zip
	 * @throws IOException thrown in case of issues in reding files.
	 */
	public static void createAndSaveNodeSchemaFile(String deploymentType) throws IOException  {
		
		//Initialize the snake yaml dumper option
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		
		Map<String, Object> nodeTypeList = new LinkedHashMap<>();
		
		String[] importFileList = new String[]{"data.yml", "artifacts.yml", "capabilities.yml", "interfaces.yml", "relationships.yml"}; 
		String collectionTitle = "node_types";
		
		//Create node.yaml - collect all types from normative-types and heat-types directories
		String[] nodeTypesMainFolders = new String[]{"normative-types", "heat-types"};

		if(DEPLOYMENT_TYPE_ONAP.equals(deploymentType)){
            String[] onapNodeTypesMainFolders = new String[]{"nfv-types"};
            nodeTypesMainFolders=ArrayUtils.addAll(nodeTypesMainFolders,onapNodeTypesMainFolders);
		}
		
		for (String nodeTypesMainFolder : nodeTypesMainFolders) {
        try (Stream<Path> paths = Files.walk(Paths.get(importToscaPath + SEPARATOR + nodeTypesMainFolder))) {
            paths.filter(path -> path.getFileName().toString().toLowerCase().endsWith(YAML_EXTENSION))
                .forEach(yamlFile -> {
                    try {
                        String path = yamlFile.toAbsolutePath().toString();
                        System.out.println("Processing node type file " + path + "...");
                        FileInputStream inputStream = new FileInputStream(path);
                        Yaml yaml = new Yaml();
                        Map<String, Object> load = yaml.loadAs(inputStream, Map.class);
                        Map<String, Object> nodeType = (Map<String, Object>) load.get(collectionTitle);
                        nodeTypeList.putAll(nodeType);

                    } catch (Exception e) {
                        System.err.println("Error in opening file " + yamlFile.toAbsolutePath().toString());
                        System.exit(1);
                    }
                });
        }
    }
		createAndSaveSchemaFileYaml("nodes", importFileList, collectionTitle, nodeTypeList);
	}

	private static void usageAndExit()  {
		SdcSchemaFileImportUsage();
		System.exit(1);
	}
	
	private static void SdcSchemaFileImportUsage()  {
		System.err.println("Usage: <file dir/filename> <SDC release number> <Schema conformance level> <configuration dir> <deployment type optional>");
	}
	
	private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		new ConfigurationManager(configurationSource);
		return  new AnnotationConfigApplicationContext(EsToCassandraDataMigrationConfig.class);
	}
}
