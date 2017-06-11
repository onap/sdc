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

package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VFCArtifact;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.testng.Assert;

import com.aventstack.extentreports.Status;

public class VFCArtifactVerificator {
	
	private static final String ARTIFACTS = "artifacts";
	private static final String DEPLOYMENT_ARTIFACTS = "deploymentArtifacts";
	private static List<VFCArtifact> vfcArtifactList = new ArrayList<VFCArtifact>();
	private static JSONObject jsonResource;
	
	public static void verifyVFCArtifactsNotInVFArtifactList(ResourceReqDetails resource , User user, RestResponse optionalGetResponse, Map<String, LinkedList<HeatMetaFirstLevelDefinition>> expectedArtifactMap){
		ExtentTestActions.log(Status.INFO, "Verifying that VFC artifacts are not as part of VF artifacts.");
		LinkedList<HeatMetaFirstLevelDefinition> expectedDeploymentArtifacts = expectedArtifactMap.get(DEPLOYMENT_ARTIFACTS);
		LinkedList<HeatMetaFirstLevelDefinition> expectedInformationalArtifacts = expectedArtifactMap.get(ARTIFACTS);
		
		Map<String, Object> vfDepArtifacts = getVFDeploymentArtifacts(resource, user, optionalGetResponse);
		for (Object artifact : vfDepArtifacts.values()){
			JSONObject acArtifact = ((JSONObject) JSONValue.parse(artifact.toString()));
			String acArtifactName = acArtifact.get("artifactName").toString();
			
			for(HeatMetaFirstLevelDefinition exDepArtifact : expectedDeploymentArtifacts){
				assertTrue(!exDepArtifact.getType().equals(acArtifactName));
			}
		}
		
		Map<String, Object> vfInfoArtifacts = getVFInforamtionalArtifacts(resource, user, optionalGetResponse);
		for (Object artifact : vfInfoArtifacts.values()){
			JSONObject acArtifact = ((JSONObject) JSONValue.parse(artifact.toString()));
			if (acArtifact.containsKey("artifactName")){
				String acArtifactName  = acArtifact.get("artifactName").toString();
				
				for(HeatMetaFirstLevelDefinition exInfoArtifact : expectedInformationalArtifacts){
					assertTrue(!exInfoArtifact.getType().equals(acArtifactName));
				}
			}
			

		}


		
	}
	
	public static void verifyVfcArtifactUpdated(String instanceName, ResourceReqDetails resource, User user){
		ExtentTestActions.log(Status.INFO, "Verifying VFC artifacts are updated.");
		List<VFCArtifact> vfcArtifactsBeforeUpdate = getVfcArtifactList();
		
		setVfcArtifactList(new ArrayList<VFCArtifact>());
		setActualVfcArtifactList(instanceName, resource, user);
		
		for (VFCArtifact artifact : vfcArtifactsBeforeUpdate){
			String artifactnameBeforeUpdate = artifact.getArtifactname();
			for (VFCArtifact newArtifact : vfcArtifactList){
				String artifactnameAfterUpdate = newArtifact.getArtifactname();
				if (artifactnameBeforeUpdate.equals(artifactnameAfterUpdate)){
					String artifactUUIDAfterUpdate = newArtifact.getArtifactUUID();
					assertTrue(!artifactUUIDAfterUpdate.equals(artifact.getArtifactUUID()));
					
					int artifactVersionAfterUpdate = Integer.parseInt(newArtifact.getArtifactVersion());
					int artifactVersionBeforeUpdate = Integer.parseInt(artifact.getArtifactVersion());
					assertTrue(artifactVersionAfterUpdate == artifactVersionBeforeUpdate + 1);
					
					
					vfcArtifactList.remove(newArtifact);
					
					ExtentTestActions.log(Status.INFO, "VFC artifacts are updated and verified.");
					
					break;
				}
			}
		}
		
		
		assertTrue(vfcArtifactList.size() == 0);
		
	}
	
	public static void verifyVFCArtifactNotChanged(String instanceName, ResourceReqDetails resource, User user){
		ExtentTestActions.log(Status.INFO, "Verifying VFC artifacts are not chaned after update.");
		List<VFCArtifact> vfcArtifactsBeforeUpdate = getVfcArtifactList();
		
		setVfcArtifactList(new ArrayList<VFCArtifact>());
		setActualVfcArtifactList(instanceName, resource, user);
		
		for (VFCArtifact artifact : vfcArtifactsBeforeUpdate){
			String artifactnameBeforeUpdate = artifact.getArtifactname();
			for (VFCArtifact newArtifact : vfcArtifactList){
				String artifactnameAfterUpdate = newArtifact.getArtifactname();
				if (artifactnameBeforeUpdate.equals(artifactnameAfterUpdate)){
					String artifactUUIDAfterUpdate = newArtifact.getArtifactUUID();
					assertTrue(artifactUUIDAfterUpdate.equals(artifact.getArtifactUUID()));
					
					int artifactVersionAfterUpdate = Integer.parseInt(newArtifact.getArtifactVersion());
					int artifactVersionBeforeUpdate = Integer.parseInt(artifact.getArtifactVersion());
					assertTrue(artifactVersionAfterUpdate == artifactVersionBeforeUpdate);
					
					vfcArtifactList.remove(newArtifact);
					break;
				}
			}
		}
		
		
		assertTrue(vfcArtifactList.size() == 0);
		
	}
	
	public static void verifyNoVfcArtifacts(ResourceReqDetails resource , User user, RestResponse optionalGetResponse){
		ExtentTestActions.log(Status.INFO, "Verifying that there are no VFC artifacts at all.");
		JSONArray jArr = getVFInstances(resource, user, optionalGetResponse);
		for (Object instanceObj : jArr){
			JSONObject instance = (JSONObject) JSONValue.parse(instanceObj.toString());
			List<String> actualDeploymentArtifacts = getActualVfcInstanceArtifactsFromJson(DEPLOYMENT_ARTIFACTS, instance);
			
			assertTrue(actualDeploymentArtifacts == null || actualDeploymentArtifacts.size() == 0);
		}
	}
	
	public static void verifyVfcArtifacts(ResourceReqDetails resource , User user, String instanceName, Map<String, LinkedList<HeatMetaFirstLevelDefinition>> expectedArtifactMap, 
			RestResponse optionalGetResponse){
		ExtentTestActions.log(Status.INFO, "Verifying VFC artifacts for instance named " + instanceName);
		
		String exCompName = instanceName.split(".vfc.")[1].toLowerCase();
		String exName = instanceName.split(".heat.")[1].toLowerCase();
		
		JSONArray jArr = getVFInstances(resource, user, optionalGetResponse);
		int jArrSize = jArr.size();
		
		for (Object instanceObj : jArr){
			JSONObject instance = (JSONObject) JSONValue.parse(instanceObj.toString());
			String componentName = instance.get("componentName").toString().toLowerCase();
			String name = instance.get("name").toString().toLowerCase();
			
			if (componentName.contains(exCompName) || name.toLowerCase().equals(exName)){
				
				List<String> actualDeploymentArtifacts = getActualVfcInstanceArtifactsFromJson(DEPLOYMENT_ARTIFACTS, instance);
				LinkedList<HeatMetaFirstLevelDefinition> expectedDeploymentArtifacts = expectedArtifactMap.get(DEPLOYMENT_ARTIFACTS);
				checkVFCArtifactsExist(expectedDeploymentArtifacts, actualDeploymentArtifacts);
				
				
				List<String> actualInformationalArtifacts = getActualVfcInstanceArtifactsFromJson(ARTIFACTS, instance);
				LinkedList<HeatMetaFirstLevelDefinition> expectedInformationalArtifacts = expectedArtifactMap.get(ARTIFACTS);
				checkVFCArtifactsExist(expectedInformationalArtifacts, actualInformationalArtifacts);
				
				jArr.remove(instanceObj);
				
				ExtentTestActions.log(Status.INFO, "VFC artifacts for instance named " + instanceName + "are verified.");
				
				break;
			}
		}
		
		assertTrue(jArr.size() == jArrSize - 1, "Instance " + instanceName + " was not found and tested");
		
	}



	private static JSONArray getVFInstances(ResourceReqDetails resource, User user, RestResponse response) {
		
		jsonResource = getVFAsJsonObject(resource, user, response);
		JSONArray jArr = (JSONArray) jsonResource.get("componentInstances");
		return jArr;
	}
	
	private static Map<String, Object> getVFDeploymentArtifacts(ResourceReqDetails resource, User user, RestResponse response) {
		
		jsonResource = getVFAsJsonObject(resource, user, response);
		Map<String, Object> jArr = (Map<String, Object>) jsonResource.get(DEPLOYMENT_ARTIFACTS);
		return jArr;
	}
	
	private static Map<String, Object> getVFInforamtionalArtifacts(ResourceReqDetails resource, User user, RestResponse response) {
		
		jsonResource = getVFAsJsonObject(resource, user, response);
		Map<String, Object> jArr = (Map<String, Object>) jsonResource.get(ARTIFACTS);
		return jArr;
	}
	
	private static JSONObject getVFAsJsonObject(ResourceReqDetails resource, User user, RestResponse response) {
		if (response == null){
			resource.setUniqueId(null);
			response = RestCDUtils.getResource(resource, user);
			assertTrue(response.getErrorCode().intValue() == 200);
			getVFAsJsonObject(resource, user, response);
		}
		
		String responseAfterDrag = response.getResponse();
		jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		return jsonResource;
	}
	
	
	
	private static List<String> getActualVfcInstanceArtifactsFromJson(String artifactKind, JSONObject instanceFromJson){
		Object actualtObject = instanceFromJson.get(artifactKind);
		if (actualtObject != null){
			JSONObject actualJsonObject = (JSONObject) JSONValue.parse(actualtObject.toString());
			List<String> actualArtifacts = (List<String>) actualJsonObject.keySet().stream().map(e -> actualJsonObject.get(e).toString()).collect(Collectors.toList());
			return actualArtifacts;
		}
		return null;
	}

	private static void checkVFCArtifactsExist(LinkedList<HeatMetaFirstLevelDefinition> expectedArtifacts, List<String> actualArtifacts) {
		if (expectedArtifacts == null){
			return;
		}
		
		if (expectedArtifacts.size() != actualArtifacts.size()){
			ExtentTestActions.log(Status.FAIL, "Expected and actual VFC artifacts lists size are not the same. Expected size: " + expectedArtifacts.size() + " , actual size: " + actualArtifacts.size());
			Assert.fail("Expected and actual VFC artifacts lists size are not the same. Expected size: " + expectedArtifacts.size() + " , actual size: " + actualArtifacts.size());
		}
		
		List<String> types = new ArrayList<String>();
		List<String> fileNames = new ArrayList<String>();
		for (HeatMetaFirstLevelDefinition exArtifact : expectedArtifacts){
			
				fileNames.add(exArtifact.getFileName());
				types.add(exArtifact.getType());
			
		}
		
		for (int i = 0 ; i < actualArtifacts.size() ; i++){
			String actualArtifactsString = actualArtifacts.get(i);
			JSONObject acArtifact = ((JSONObject) JSONValue.parse(actualArtifactsString));
			
			String acArtifactFileName = acArtifact.get("artifactName").toString();
			String acArtifactType = acArtifact.get("artifactType").toString();
			
			assertTrue(types.contains(acArtifactType), "List does not contain " + acArtifactType);
			assertTrue(fileNames.contains(acArtifactFileName), "List does not contain " + acArtifactFileName);
			
			types.remove(acArtifactType);
			fileNames.remove(acArtifactFileName);
			
		}
		
		assertTrue(types.size() == 0);
		assertTrue(fileNames.size() == 0);
		
	}
	
	public static List<VFCArtifact> getVfcArtifactList(){
		return vfcArtifactList;
	}
	

	public static void setVfcArtifactList(List<VFCArtifact> vfcArtifactList) {
		VFCArtifactVerificator.vfcArtifactList = vfcArtifactList;
	}

	public static void setActualVfcArtifactList(String instanceName, ResourceReqDetails resource , User user) {
		String exCompName = instanceName.split(".vfc.")[1].toLowerCase();
		String exName = instanceName.split(".heat.")[1].toLowerCase();
		
		JSONArray jArr = getVFInstances(resource, user, null);
		
		for (Object instanceObj : jArr){
			JSONObject instance = (JSONObject) JSONValue.parse(instanceObj.toString());
			String componentName = instance.get("componentName").toString().toLowerCase();
			String name = instance.get("name").toString().toLowerCase();
			
			if (componentName.contains(exCompName) || name.toLowerCase().equals(exName)){
				List<String> actualDeploymentArtifacts = getActualVfcInstanceArtifactsFromJson(DEPLOYMENT_ARTIFACTS, instance);
				List<String> actualInformationalArtifacts = getActualVfcInstanceArtifactsFromJson(ARTIFACTS, instance);
		
				if (actualDeploymentArtifacts != null){
					for (int i = 0 ; i < actualDeploymentArtifacts.size() ; i++){
						String actualArtifactsString = actualDeploymentArtifacts.get(i);
						JSONObject acArtifact = ((JSONObject) JSONValue.parse(actualArtifactsString));
						
						if (acArtifact.containsKey("artifactName")){
							String acArtifactType = acArtifact.get("artifactName").toString();
							String acArtifactFileName = acArtifact.get("artifactType").toString();
							String acArtifactUUID = acArtifact.get("artifactUUID").toString();
							String acArtifactVersion = acArtifact.get("artifactVersion").toString();
							
							vfcArtifactList.add(new VFCArtifact(acArtifactType, acArtifactFileName, acArtifactUUID, acArtifactVersion));
						}
					}
				}
				if (actualInformationalArtifacts != null){
					for (int i = 0 ; i < actualInformationalArtifacts.size() ; i++){
						String actualArtifactsString = actualInformationalArtifacts.get(i);
						JSONObject acArtifact = ((JSONObject) JSONValue.parse(actualArtifactsString));
						
						if (acArtifact.containsKey("artifactName")){
							String acArtifactType = acArtifact.get("artifactName").toString();
							String acArtifactFileName = acArtifact.get("artifactType").toString();
							String acArtifactUUID = acArtifact.get("artifactUUID").toString();
							String acArtifactVersion = acArtifact.get("artifactVersion").toString();
							vfcArtifactList.add(new VFCArtifact(acArtifactType, acArtifactFileName, acArtifactUUID, acArtifactVersion));
						}
						
						
					}
				}
			}
		}
	}
	
}



