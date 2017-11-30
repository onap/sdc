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

package org.openecomp.sdc.ci.tests.execute.devCI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.openecomp.sdc.ci.tests.utils.CsarParserUtils;

public class ArtifactFromCsar {
	

	public static void main(String[] args) throws Exception {
		String zipFile = "C:\\Users\\rp955r\\Documents\\InTesting\\resource-CivfonboardedFdnt2f792348-csar.csar";
		
//		Map<String, Object> combinedMap = combineHeatArtifacstWithFolderArtifacsToMap(zipFile);
		
		Map<String, Object> vfcArtifacts = ArtifactFromCsar.getVFCArtifacts(zipFile);
		
		System.out.println("1234o");
	}
	
	public static Map<String, Object> combineHeatArtifacstWithFolderArtifacsToMap(String pathToCsar) throws Exception {
		return combineHeatArtifacstWithFolderArtifacsToMap(pathToCsar, "output");
	}
	
	public static Map<String, Object> combineHeatArtifacstWithFolderArtifacsToMap(String pathToCsar, String outputCsar) throws Exception {
		File csarFile = new File(pathToCsar);
		
		
		File dir = new File(csarFile.getParent() + File.separator + outputCsar);
			dir.mkdir();
			if(!dir.exists()) {
		}

		String outputFolder = dir.getPath();
		unZip(pathToCsar, outputFolder);
		File directory = new File(outputFolder + File.separator + "Artifacts" + File.separator );
		
		Map<String, Object> artifactsMap = combineHeatArtifacstWithFolderArtifacsToMap(getMapArtifactFromFolderStructure(directory), getDeploymentArtifactListFromHeatMeta(csarFile, directory));
		FileUtils.cleanDirectory(new File(outputFolder));
		
		return artifactsMap;
	}
	
	public static Map<String, Object> getVFCArtifacts(String pathToCsar) throws Exception{
		String outputFolder = unzipCsarFile(pathToCsar);
		File directory = new File(outputFolder + File.separator + "Artifacts" + File.separator );
		Map<String, Object> artifactsMap = getMapArtifactFromFolderStructure(directory);
		cleanFolders(outputFolder);
		
		return artifactsMap;
	}
	
	private static Map<String, Object> combineHeatArtifacstWithFolderArtifacsToMap(Map<String, Object> map, List<HeatMetaFirstLevelDefinition> rlist) {
		if(map.get("Deployment") != null) {
			rlist.addAll((Collection<? extends HeatMetaFirstLevelDefinition>) map.get("Deployment"));
		}
		map.put("Deployment", rlist);
		return map;
	}
	
	private static List<HeatMetaFirstLevelDefinition> getDeploymentArtifactListFromHeatMeta(File pathToCsar, File directory) throws Exception {
		List<HeatMetaFirstLevelDefinition> artifactList = new LinkedList<HeatMetaFirstLevelDefinition>();
		
		List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(pathToCsar);

	    for(TypeHeatMetaDefinition typeHeatMetaDefinition : listTypeHeatMetaDefinition){
	    	for(GroupHeatMetaDefinition groupHeatMetaDefinition : typeHeatMetaDefinition.getGroupHeatMetaDefinition()){
	        	artifactList.addAll(groupHeatMetaDefinition.getArtifactList());
	        }
	    }
		
	    List<HeatMetaFirstLevelDefinition> listArtifactWithTypesByList = getListArtifactWithTypesByList(directory, artifactList);
		return listArtifactWithTypesByList;
//	    return artifactList;
		
	}
	
	private static Map<String, Object> getMapArtifactFromFolderStructure(File pathToArtifactFolder) throws IOException {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		
		final Path dir = Paths.get(pathToArtifactFolder.getPath());
		final DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir);
		
		dirStream.forEach(currFile -> {
			File file = currFile.toFile();
				if (file.isDirectory()) {
					System.out.println(file.getName());
					if(file.getName().toLowerCase().equals("deployment") || file.getName().toLowerCase().equals("informational")) {
						map.put(file.getName(), getListArtifactWithTypes(file));
					} else {
						try {
							map.put(file.getName(), getMapArtifactFromFolderStructure(file));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
		});
		
		dirStream.close();
		

		
//		
//		File[] fileList = pathToArtifactFolder.listFiles();
//		for(File file: fileList) {
//			if (file.isDirectory()) {
//				
//				System.out.println(file.getName());
//				if(file.getName().equals("Deployment") || file.getName().equals("Informational")) {
//					map.put(file.getName(), getListArtifactWithTypes(file));
//				} else {
//					map.put(file.getName(), getMapArtifactFromFolderStructure(file));
//				}
//			}
//		}
		return map;
	}
	
	
	
	private static List<HeatMetaFirstLevelDefinition> getListArtifactWithTypes(File folderPath) {
		List<HeatMetaFirstLevelDefinition> artifactList = new LinkedList<HeatMetaFirstLevelDefinition>();
		
		File[] fileList = folderPath.listFiles();
		
		for(File file: fileList) {
			File[] artifacts = file.listFiles();
			
			for(File artifact: artifacts) {
//				HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition = new HeatMetaFirstLevelDefinition(file.getName(), artifact.getName());
				HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition = new HeatMetaFirstLevelDefinition(artifact.getName(), file.getName(), crunchifyGetMd5ForFile(artifact));
				artifactList.add(heatMetaFirstLevelDefinition);
			}
		}
		
		return artifactList;
	}
	
	private static List<HeatMetaFirstLevelDefinition> getListArtifactWithTypesByList(File folderPath, List<HeatMetaFirstLevelDefinition> artifactLogicList) {
				
		
		File[] fileList = folderPath.listFiles();
		
		
		
		for (HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition : artifactLogicList) {
			
			String fileName = heatMetaFirstLevelDefinition.getFileName();
			
			for (File fileFromFolder : fileList) {
				if ( fileFromFolder.getName().equals(fileName)){
					heatMetaFirstLevelDefinition.setCheckSum(crunchifyGetMd5ForFile(fileFromFolder));
				}
				
			}
		 }

		return artifactLogicList;
	}
	
	public static String crunchifyGetMd5ForFile(File crunchifyFile) {
		String crunchifyValue = null;
		FileInputStream crunchifyInputStream = null;
		try {
			crunchifyInputStream = new FileInputStream(crunchifyFile);
 
			// md5Hex converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
			// The returned array will be double the length of the passed array, as it takes two characters to represent any given byte.
			crunchifyValue = DigestUtils.md5Hex(IOUtils.toByteArray(crunchifyInputStream));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(crunchifyInputStream);
		}
		return crunchifyValue;
	}
	
	public static void unZip(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];

	     try{
	    	File folder = new File(outputFolder);
	    	
	    	if(!folder.exists()){
	    		folder.mkdir();
	    	}

	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
	    	ZipEntry ze = zis.getNextEntry();

	    	while(ze!=null){

	    	   String fileName = ze.getName();
	    	   File newFile = new File(outputFolder + File.separator + fileName);
	           
	           if(ze.isDirectory()) {
	        	   newFile.mkdir();
	        	   ze = zis.getNextEntry();
	        	   continue;
	           }

	            new File(newFile.getParent()).mkdirs();
	            FileOutputStream fos = new FileOutputStream(newFile);

	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	            	fos.write(buffer, 0, len);
	            }

	            fos.close();
	            ze = zis.getNextEntry();
	    	}

	        zis.closeEntry();
	    	zis.close();

	    } catch (IOException ex) {
	       ex.printStackTrace();
	    }

	}
	
    private static void cleanFolders(String outputFolder) throws IOException {
		System.gc();
		FileUtils.cleanDirectory(new File(outputFolder));
		FileUtils.deleteDirectory(new File(outputFolder));
	}

    private static String unzipCsarFile(String pathToCsar) {
		File csarFile = new File(pathToCsar);
		
		
		File dir = new File(csarFile.getParent() + File.separator + "output-" + UUID.randomUUID());
		if(!dir.exists()) {
			dir.mkdirs();
		}

		String outputFolder = dir.getPath();
		ArtifactFromCsar.unZip(pathToCsar, outputFolder);
		return outputFolder;
	}

	public static String[] getArtifactNamesFromCsar(String path, String csarFile) throws Exception {
		Map<String, Object> combinedMap = combineHeatArtifacstWithFolderArtifacsToMap(path + csarFile);
		LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
		List<String> artifactNamesList = deploymentArtifacts.stream().map(e -> e.getFileName()).collect(Collectors.toList());
		Object[] artifactNamesObjectArr = artifactNamesList.toArray();
		String[] artifactNamesFromFile = Arrays.copyOf(artifactNamesObjectArr, artifactNamesObjectArr.length, String[].class);
		return artifactNamesFromFile;
	}

}
