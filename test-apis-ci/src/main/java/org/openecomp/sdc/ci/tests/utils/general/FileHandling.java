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

package org.openecomp.sdc.ci.tests.utils.general;

import static org.testng.AssertJUnit.assertTrue;

import com.aventstack.extentreports.Status;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.XnfTypeEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.yaml.snakeyaml.Yaml;

public class FileHandling {

//	------------------yaml parser methods----------------------------
	public static Map<?, ?> parseYamlFile(String filePath) throws Exception {
		Yaml yaml = new Yaml();
		File file = new File(filePath);
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
		return map;
	}
	
	/**
	 * The method return map fetched objects by pattern from yaml file 
	 * @param yamlFile
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> parseYamlFileToMapByPattern(File yamlFile, String pattern) throws Exception {
		Map<?, ?> yamlFileToMap = FileHandling.parseYamlFile(yamlFile.toString());
		Map<String, Object> objectMap = getObjectMapByPattern(yamlFileToMap, pattern);
		return objectMap;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getObjectMapByPattern(Map<?, ?> parseUpdetedEnvFile, String pattern) {
		Map<String, Object> objectMap = null;
		
		Object objectUpdetedEnvFile = parseUpdetedEnvFile.get(pattern);
		if(objectUpdetedEnvFile instanceof HashMap){
			objectMap = (Map<String, Object>) objectUpdetedEnvFile;
		}
		return objectMap;
	}
	
	
	public static Map<String, DataTypeDefinition> parseDataTypesYaml(String filePath) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, DataTypeDefinition> dataTypesMap = (Map<String, DataTypeDefinition>) parseYamlFile(filePath);
		return dataTypesMap;
	}
//	-------------------------------------------------------------------------------------------------

	/**
	 * @param folder, folder name under "Files" folder
	 * @return path to given folder from perspective of working directory or sdc-vnfs repository
	 */
	public static String getFilePath(String folder) {
		String filepath = System.getProperty("filePath");
		boolean isFilePathEmptyOrNull = (filepath == null || filepath.isEmpty());

		// return folder from perspective of sdc-vnfs repository
		if (isFilePathEmptyOrNull && ( System.getProperty("os.name").contains("Windows") || System.getProperty("os.name").contains("Mac"))) {
			return FileHandling.getResourcesFilesPath() + folder + File.separator;
		}

		// return folder from perspective of working directory ( in general for nightly run from Linux, should already contain "Files" directory )
		return FileHandling.getBasePath() + "Files" + File.separator + folder + File.separator;
	}

	public static String getBasePath() {
		return System.getProperty("user.dir") + File.separator;
	}
	
	public static String getSdcVnfsPath() {
		return  getBasePath() + Paths.get("..", "..", "sdc-vnfs").toString();
	}
	
	public static String getDriversPath() {
		return getBasePath() + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "ci" + File.separator + "drivers" + File.separator;
	}

	public static String getResourcesFilesPath() {
//		return getBasePath() + "src" + File.separator + "main" + File.separator + "resources"
//				+ File.separator + "Files" + File.separator;

		return getSdcVnfsPath()+ File.separator + "ui-tests" + File.separator + "Files" + File.separator;
	}
	
	public static String getResourcesEnvFilesPath() {
		return getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "Files" + File.separator + "ResourcesEnvFiles" +File.separator;
	}

	public static String getCiFilesPath() {
		return getBasePath() + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "ci";
	}

	public static String getConfFilesPath() {
		return getCiFilesPath() + File.separator + "conf" + File.separator;
	}

	public static String getTestSuitesFilesPath() {
		return getCiFilesPath() + File.separator + "testSuites" + File.separator;
	}
	
	public static String getVnfRepositoryPath() {
		return getFilePath("VNFs");
	}

	public static String getXnfRepositoryPath(XnfTypeEnum xnfTypeEnum) {
		return xnfTypeEnum.getValue().equals(XnfTypeEnum.PNF.name()) ? getFilePath("PNFs") : getFilePath("VNFs");
	}

	public static String getPortMirroringRepositoryPath() {
		return getFilePath("PortMirroring");
	}
	
	public static File getConfigFile(String configFileName) throws Exception {
		File configFile = new File(FileHandling.getBasePath() + File.separator + "conf" + File.separator + configFileName);
		if (!configFile.exists()) {
			configFile = new File(FileHandling.getConfFilesPath() + configFileName);
		}
		return configFile;
	}

	public static Object[] filterFileNamesFromFolder(String filepath, String extension) {
		try {
			File dir = new File(filepath);
			List<String> filenames = new ArrayList<String>();
			
			FilenameFilter extensionFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(extension);
				}
			};
			
			if (dir.isDirectory()) {
				for (File file : dir.listFiles(extensionFilter)) {
					filenames.add(file.getName());
				}
				return filenames.toArray();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> filterFileNamesListFromFolder(String filepath, String extension) {
		List<String> filenames = new ArrayList<String>();
		try {
			File dir = new File(filepath);
			
			FilenameFilter extensionFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(extension);
				}
			};
			
			if (dir.isDirectory()) {
				for (File file : dir.listFiles(extensionFilter)) {
					filenames.add(file.getName());
				}
				return filenames;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return filenames;
	}
	
	public static String[] getArtifactsFromZip(String filepath, String zipFilename){
		try {
			ZipFile zipFile = new ZipFile(filepath + File.separator + zipFilename);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			String[] artifactNames = new String[zipFile.size() - 1];

			int i = 0;
			while(entries.hasMoreElements()){
				ZipEntry nextElement = entries.nextElement();
				if (!nextElement.isDirectory()){ 
					if (!nextElement.getName().equals("MANIFEST.json")){
						String name = nextElement.getName();
						artifactNames[i++] = name;
					}
				}
			}
			zipFile.close();
			return artifactNames;
		} catch(ZipException zipEx) {
			System.err.println("Error in zip file named : " +  zipFilename);	
			zipEx.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unhandled exception : ");
			e.printStackTrace();
		}
		
		return null;
		
	}

	public static List<String> getFileNamesFromZip(String zipFileLocation){
		try{
			ZipFile zipFile = new ZipFile(zipFileLocation);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			List<String> artifactNames = new ArrayList<>();

			int i = 0;
			while(entries.hasMoreElements()){
				ZipEntry nextElement = entries.nextElement();
				if (!nextElement.isDirectory()){ 
					String name = nextElement.getName();
					artifactNames.add(name);
				}
			}
			zipFile.close();
			return artifactNames;
		}
		catch(ZipException zipEx){
			System.err.println("Error in zip file named : " +  zipFileLocation);	
			zipEx.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unhandled exception : ");
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<String> getZipFileNamesFromFolder(String filepath) {
		List<String> fileNamesListFromFolder = filterFileNamesListFromFolder(filepath, ".zip");
		fileNamesListFromFolder.addAll(filterFileNamesListFromFolder(filepath, ".csar"));
		return fileNamesListFromFolder;
	}

	public static int countFilesInZipFile(String[] artifactsArr, String reqExtension){
		int fileCounter = 0;
		for (String artifact : artifactsArr){
			String extensionFile = artifact.substring(artifact.lastIndexOf(".") + 1 , artifact.length());
			if (extensionFile.equals(reqExtension)){
				fileCounter++;
			}
		}
		return fileCounter;
	}
	
	/**
	 * @param dirPath
	 * @return last modified file name from dirPath directory
	 */
	public static synchronized File getLastModifiedFileNameFromDir(String dirPath){
	    File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null) {
	    	assertTrue("File not found under directory " + dirPath, false);
	        return null;
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	    	if(files[i].isDirectory()) {
	    		continue;
	    	}
	    	if (lastModifiedFile.lastModified()  < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	    	}
	    }
	    return lastModifiedFile;
	}

	public static void deleteDirectory(String directoryPath) {
		File dir = new File(directoryPath);
		try {
			FileUtils.cleanDirectory(dir);
		} catch (IllegalArgumentException e) {
			System.out.println("Failed to clean " + dir);
		} catch (IOException e) {
			System.out.println("Failed to clean " + dir);
		}
	}
	
	public static void createDirectory(String directoryPath) {
		File directory = new File(String.valueOf(directoryPath));
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	}


	/**
	 * The method append data to existing file, if file not exists - create it
	 * @param pathToFile
	 * @param text
	 * @param leftSpaceCount
	 * @throws IOException
	 */
	public static synchronized void writeToFile(File pathToFile, Object text, Integer leftSpaceCount) throws IOException{

		BufferedWriter bw = null;
		FileWriter fw = null;
		if(!pathToFile.exists()){
			createEmptyFile(pathToFile);
		}
		try {
			fw = new FileWriter(pathToFile, true);
			bw = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();
			if(leftSpaceCount > 0 ){
				for(int i = 0; i < leftSpaceCount; i++){
					sb.append(" ");
				}
			}
			bw.write(sb.toString() + text);
			bw.newLine();
			bw.close();
			fw.close();
		} catch (Exception e) {
			ComponentBaseTest.getExtendTest().log(Status.INFO, "Unable to write to flie " + pathToFile);
		}
	}

	public static String getCreateDirByName(String dirName) {
		File dir = new File(dirName);
		dir.mkdir();
		if(!dir.exists()) {
		}

		return dir.getPath();
	}
	
	public static boolean isFileDownloaded(String downloadPath, String fileName) {
		boolean flag = false;
		File dir = new File(downloadPath);
		File[] dir_contents = dir.listFiles();
		for (int i = 0; i < dir_contents.length; i++) {
			if (dir_contents[i].getName().equals(fileName))
				return flag = true;
		}
		return flag;
	}
	
	public static String getMD5OfFile(File file) throws IOException {
		String content = FileUtils.readFileToString(file);
		String md5 = GeneralUtility.calculateMD5Base64EncodedByString(content);
		return md5;
	}
	
	public static File createEmptyFile(String fileToCreate) {
		File file= new File(fileToCreate);
		try {
			if(file.exists()){
				deleteFile(file);
			}
			file.createNewFile();
			ComponentBaseTest.getExtendTest().log(Status.INFO, "Create file " + fileToCreate);
		} catch (IOException e) {
			ComponentBaseTest.getExtendTest().log(Status.INFO, "Failed to create file " + fileToCreate);
			e.printStackTrace();
		}
		return file;
	}
	
	public static File createEmptyFile(File fileToCreate) {
		try {
			if(fileToCreate.exists()){
				deleteFile(fileToCreate);
			}
			fileToCreate.createNewFile();
			ComponentBaseTest.getExtendTest().log(Status.INFO, "Create file " + fileToCreate);
		} catch (IOException e) {
			ComponentBaseTest.getExtendTest().log(Status.INFO, "Failed to create file " + fileToCreate);
			e.printStackTrace();
		}
		return fileToCreate;
	}
	
	public static void deleteFile(File file){
		
		try{
    		if(file.exists()){
    			file.deleteOnExit();
    			ComponentBaseTest.getExtendTest().log(Status.INFO, "File " + file.getName() + "has been deleted");
    		}else{
    			ComponentBaseTest.getExtendTest().log(Status.INFO, "Failed to delete file " + file.getName());
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}

	}
	
	
	/**
	 * get file list from directory by extension array
	 * @param directory
	 * @param okFileExtensions
	 * @return
	 */
	public static List<File> getHeatAndHeatEnvArtifactsFromZip(File directory, String[] okFileExtensions){
		
			List<File> fileList = new ArrayList<>();
			File[] files = directory.listFiles();
			
			for (String extension : okFileExtensions){
				for(File file : files){
					if (file.getName().toLowerCase().endsWith(extension)){
						fileList.add(file);
					}
				}
			}
			return fileList;
	}

    public static String getKeyByValueFromPropertyFormatFile(String fullPath, String key) {
		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		try {
			input = new FileInputStream(fullPath);
			prop.load(input);
			value = (prop.getProperty(key));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return value.replaceAll("\"","");
	}

    public static void overWriteExistindDir(String outputCsar) throws IOException {
		String basePath = getBasePath();
		String csarDir = FileHandling.getCreateDirByName("outputCsar");
		FileUtils.cleanDirectory(new File(csarDir));
    }
}
