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

package org.openecomp.sdc.ci.tests.businesslogic;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.qos.logback.classic.util.ContextInitializer;
import com.google.gson.Gson;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.ci.tests.datatypes.HeatAndHeatEnvNamesPair;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.HeatWithParametersDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;

import com.clearspring.analytics.util.Pair;

public class ArtifactBusinessLogic {

	private final static String[] okFileExtensions = new String[] {"yaml", "yml", "env"};
	private static final String PARAMETERS = "parameters";
	private static final String DEPLOYMENT = "Deployment";
	
	public static synchronized Map<String, File> createEnvFilesListFromCsar(String vspName, String filePath) throws Exception{
		Map<String, File> generatedEnvFiles = new HashMap<>();
		File csarFile= HomePage.downloadVspCsarToDefaultDirectory(vspName); 
		FileHandling.unzip(csarFile.toString(), filePath);
		List<File> yamlList = getHeatFilesCreatedFromCsar(csarFile, filePath);
		Map<String, HeatAndHeatEnvNamesPair> filesPairMap = getFilesPairMap(yamlList);
		generatedEnvFiles = generateDefaultEnvFiles(filesPairMap, filePath);
		return generatedEnvFiles;
	}
	
	public static synchronized List<File> getHeatFilesCreatedFromCsar(File pathToDirectory, String filePath) throws Exception {
		List<File> fileList = new ArrayList<>();
		String artifactsFilePath = filePath + "Artifacts" + File.separator;
		List<File> fileListFromArtifactsDirectory = FileHandling.getHeatAndHeatEnvArtifactsFromZip(new File(artifactsFilePath), okFileExtensions);
		Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(pathToDirectory.toString());
		LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get(DEPLOYMENT));
		for(HeatMetaFirstLevelDefinition deploymentArtifact : deploymentArtifacts){
			String type = deploymentArtifact.getType();
			if(type.equals(ArtifactTypeEnum.HEAT.getType()) || 
			   type.equals(ArtifactTypeEnum.HEAT_ENV.getType()) || 
			   type.equals(ArtifactTypeEnum.HEAT_VOL.getType()) || 
			   type.equals(ArtifactTypeEnum.HEAT_NET.getType())){
				File file = (new File(artifactsFilePath + deploymentArtifact.getFileName()));
				if(fileListFromArtifactsDirectory.contains(file)){
					fileList.add(file);
				}else{
					assertTrue("File " + file + " does not exist", false);
				}
			}
		}
		return fileList;
	}
	public static synchronized Map<String, HeatAndHeatEnvNamesPair> getFilesPairMap(List<File> generatedEnvFiles) {
		
		Map<String, HeatAndHeatEnvNamesPair> heatAndHeatEnvPairs= new HashMap<>();
		for(File file : generatedEnvFiles){
			String[] fileName = file.getName().split("\\.");
			String currentKey = fileName[0];
			String currentExtension = fileName[1];
			HeatAndHeatEnvNamesPair pair;
			if(!heatAndHeatEnvPairs.containsKey(currentKey)){
				pair = new HeatAndHeatEnvNamesPair();
				heatAndHeatEnvPairs.put(currentKey, pair);
			}else{
				pair = heatAndHeatEnvPairs.get(currentKey);
			}
			setFileToPair(file, currentExtension, pair);
		}
		return heatAndHeatEnvPairs;
	}
	
	/**
	 * The method fill list of HeatWithParametersDefinition parameters
	 * @param deploymentArtifacts
	 * @return
	 */
	public static synchronized List<HeatWithParametersDefinition> extractHeatWithParametersDefinition(Map<String, ArtifactDefinition> deploymentArtifacts) {

		List<HeatWithParametersDefinition> heatAndEnvLabelList = new ArrayList<>();
		
		for (Entry<String, ArtifactDefinition> artifactDefinitionChild : deploymentArtifacts.entrySet()){
			if(artifactDefinitionChild.getValue().getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())){
				for(Entry<String, ArtifactDefinition> artifactDefinitionParent : deploymentArtifacts.entrySet()){
					if(artifactDefinitionChild.getValue().getGeneratedFromId().equals(artifactDefinitionParent.getValue().getUniqueId())){
						String heatLabel = artifactDefinitionParent.getValue().getArtifactLabel();
						String heatArtifactType = artifactDefinitionParent.getValue().getArtifactType();
						String heatArtifactDisplayName = artifactDefinitionParent.getValue().getArtifactDisplayName();
						List<HeatParameterDataDefinition> heatParameterDefinition = artifactDefinitionParent.getValue().getHeatParameters();
						String heatEnvLabel = artifactDefinitionChild.getValue().getArtifactLabel();
						String heatEnvArtifactType = artifactDefinitionChild.getValue().getArtifactType();
						heatAndEnvLabelList.add(new HeatWithParametersDefinition(heatLabel, heatEnvLabel, heatArtifactType, heatEnvArtifactType, heatArtifactDisplayName, heatParameterDefinition));
						break;
					}
				}
			}
		}
		return heatAndEnvLabelList;
	}
	

	public static synchronized void setFileToPair(File file, String currentExtension, HeatAndHeatEnvNamesPair pair) {
		if(!currentExtension.equals("env")){
			pair.setHeatFileName(file);
		}else{
			pair.setHeatEnvFileName(file);
		}
	}

	public static synchronized Map<String, File> generateDefaultEnvFiles(Map<String, HeatAndHeatEnvNamesPair> filesPairMap, String filePath) throws Exception {
		
		Map<String, File> generatedEnvFilesMap = new HashMap<>();
		for(Entry<String, HeatAndHeatEnvNamesPair> pair : filesPairMap.entrySet()){
			Map<String, Pair<String, Object>> envParametersMap = getEnvParametersMap(pair);
			File generatedEnvFile = createEnvFile(envParametersMap, new File(filePath + pair.getKey() + ".env"));
			generatedEnvFilesMap.put(pair.getKey(), generatedEnvFile);
		}
		return generatedEnvFilesMap;
	}

	public static synchronized File createEnvFile(Map<String, Pair<String, Object>> envParametersMap, File fileToWrite) throws IOException {

		FileHandling.writeToFile(fileToWrite, PARAMETERS+":", 0);
		FileHandling.writeToFile(fileToWrite, envParametersMap, 2);
		return fileToWrite;
	}

	public static synchronized Map<String, Pair<String, Object>> getEnvParametersMap(Entry<String, HeatAndHeatEnvNamesPair> pair) throws Exception {
		File heatFileName = pair.getValue().getHeatFileName();
		File heatEnvFileName = pair.getValue().getHeatEnvFileName();
		Map<String, Pair<String, Object>> envParametersMap = new HashMap<>();
		fillParametersMapFromHeatFile(heatFileName, envParametersMap);
		fillParametersMapFromHeatEnvFile(heatEnvFileName, envParametersMap);
		return envParametersMap;
	}

	public static synchronized void fillParametersMapFromHeatEnvFile(File heatEnvFileName, Map<String, Pair<String, Object>> envParametersMap) throws Exception {
		if(heatEnvFileName != null){
			Map<String, Object> mapHeatEnvFileParameters = FileHandling.parseYamlFileToMapByPattern(heatEnvFileName, PARAMETERS);
			for (Map.Entry<String, Object> parameter : mapHeatEnvFileParameters.entrySet()){
				String key = parameter.getKey();
				Pair<String, Object> pair;
				if(envParametersMap.containsKey(key)){
					if(envParametersMap.get(key).left.equals("string") && parameter.getValue() != null){
						pair = Pair.create(envParametersMap.get(key).left, "\"" + parameter.getValue() + "\"");
					}else if(envParametersMap.get(key).left.equals("string") && parameter.getValue() == null){
						pair = Pair.create(envParametersMap.get(key).left, "");
					}else if(parameter.getValue() == null) {
						pair = Pair.create(envParametersMap.get(key).left, "");
					} else if(envParametersMap.get(key).left.equals("json") && parameter.getValue() != null){
						String pairValue = "";
						Gson gson = new Gson();
						if(parameter.getValue() instanceof java.util.LinkedHashMap){
							pairValue = gson.toJson(parameter.getValue());
						}
//						pair = Pair.create(envParametersMap.get(key).left, "\"" + pairValue + "\"");
						pair = Pair.create(envParametersMap.get(key).left, pairValue );

					}else if(envParametersMap.get(key).left.equals("json") && parameter.getValue() == null){
						pair = Pair.create(envParametersMap.get(key).left, "");
					}else{
						pair = Pair.create(envParametersMap.get(key).left, parameter.getValue());
					}
					envParametersMap.put(key, pair);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized void fillParametersMapFromHeatFile(File heatFileName, Map<String, Pair<String, Object>> envParametersMap) throws Exception {
		Map<String, Object> mapHeatFileParameters = FileHandling.parseYamlFileToMapByPattern(heatFileName, PARAMETERS);
		for (Map.Entry<String, Object> parameter : mapHeatFileParameters.entrySet()){
			Map<String, Object> value = (Map<String, Object>) parameter.getValue();
			Pair<String, Object> pair;
			if(value.get("type").toString().equals("string") && value.get("default") != null ){
				pair = Pair.create(value.get("type").toString(), "\"" + value.get("default") + "\"");
			}else if(value.get("type").toString().equals("string") && value.get("default") == null){
				pair = Pair.create(value.get("type").toString(), "");
			}else if(value.get("default") == null){
				pair = Pair.create(value.get("type").toString(), "");
			}else{
				pair = Pair.create(value.get("type").toString(), value.get("default"));
			}
			envParametersMap.put(parameter.getKey(), pair);
		}
	}
	
}
