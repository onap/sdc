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

package org.openecomp.sdc.ci.tests.US;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ToscaArtifactsPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.clearspring.analytics.util.Pair;


public class AddComponentInstancesArtifactsInCsar extends SetupCDTest {
	
	private String filePath;
	@BeforeClass
	public void beforeClass(){
		filePath = System.getProperty("filePath");
		if (filePath == null && System.getProperty("os.name").contains("Windows")) {
			filePath = FileHandling.getResourcesFilesPath() + "AddComponentInstancesArtifactsInCsar"+ File.separator;
		}
		else if(filePath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
			filePath = FileHandling.getBasePath() + File.separator + "Files" + File.separator + "AddComponentInstancesArtifactsInCsar"+ File.separator;
		}
	}
	
	// US847439 - Story [BE] - Add Component Instance's artifacts in CSAR
	// TC1521795 - VF CSAR - The Flow
	@Test
	public void vfAndServicerCsarTheFlow() throws Exception{
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		
		String vnfFile = "FDNT.zip";
		String snmpFile = "Fault-alarms-ASDC-vprobes-vLB.zip";
		
		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUtils.createVendorLicense(getUser());
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		Pair<String, Map<String, String>> createVSP = OnboardingUtils.createVSP(resourceReqDetails, vnfFile, filePath, getUser(), amdocsLicenseMembers);
		String vspName = createVSP.left;
		resourceMetaData.setName(vspName);
		Map<String, String> resourceMeta = createVSP.right;
		String vspid = resourceMeta.get("vspId");
		OnboardingUtils.addVFCArtifacts(filePath, snmpFile, null, vspid, getUser());
		OnboardingUtils.prepareVspForUse(getUser(), vspid, "0.1");

		HomePage.showVspRepository();
		OnboardingUiUtils.importVSP(createVSP);
		resourceMetaData.setVersion("0.1");
		Resource vfResource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), resourceMetaData.getVersion());

		
		Map<String, Object> artifacts = getArtifactsOfComponentAndComponentsInstance(vfResource);

		List<ImmutablePair<ComponentInstance, ArtifactDefinition>> artifactsUploadedToComponentInstance = new LinkedList<>();
		Random random = new Random();
		for(int i=0; i<random.nextInt(10) + 10; i++) {
			ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomVfc = uploadArtifactOnRandomRI(vfResource);

			if(uploadArtifactOnRandomVfc.getRight().getArtifactName() != null) {
				artifactsUploadedToComponentInstance.add(uploadArtifactOnRandomVfc);
			}
		}
		
		if(artifactsUploadedToComponentInstance.size() > 0) {
			Map<String, Object> artifactsOfResourceInstance = getArtifactsOfResourceInstance(artifactsUploadedToComponentInstance);
			artifacts.put("Resources", artifactsOfResourceInstance);
		}
		
		
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		ToscaArtifactsPage.downloadCsar();
		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
		Map<String, Object> combineHeatArtifacstWithFolderArtifacsToMap = ArtifactFromCsar.getVFCArtifacts(latestFilefromDir.getAbsolutePath());
		
		compareArtifactFromFileStructureToArtifactsFromJavaObject(artifacts, combineHeatArtifacstWithFolderArtifacsToMap);
		
		
//		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		
//		// Submit for testing + certify
//		DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
//
//		reloginWithNewRole(UserRoleEnum.TESTER);
//		GeneralUIUtils.findComponentAndClick(vspName);
//		TesterOperationPage.certifyComponent(vspName);
//
//		reloginWithNewRole(UserRoleEnum.DESIGNER);
//		// create service
//		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
//		ServiceUIUtils.createService(serviceMetadata, getUser());
//		serviceMetadata.setVersion("0.1");
//		
//		
//		// Upload informationl artifact to service
//		ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
//		
//		String HEAT_FILE_YAML_NAME = "Heat-File.yaml";
//		String DESCRIPTION = "kuku";
//		String ARTIFACT_LABEL = "artifact3";
//		
//		ArtifactInfo artifact = new ArtifactInfo(filePath, HEAT_FILE_YAML_NAME, DESCRIPTION, ARTIFACT_LABEL,"OTHER");
//		CompositionPage.showDeploymentArtifactTab();
//		CompositionPage.clickAddArtifactButton();
//		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifact, CompositionPage.artifactPopup());
//		
//		ArtifactInfo informationArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "GUIDE");
//		CompositionPage.showInformationArtifactTab();
//		CompositionPage.clickAddArtifactButton();
//		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationArtifact, CompositionPage.artifactPopup());
//		
//		
//		
//		// Add component instance to canvas of the service
//		CompositionPage.searchForElement(vspName);
//		CanvasManager serviceCanvasManager = CanvasManager.getCanvasManager();
//		CanvasElement vfElement = serviceCanvasManager.createElementOnCanvas(vspName);
//		
//		Service service = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceMetadata.getName(), serviceMetadata.getVersion());
//		
////		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", "OTHER", true, false);
////		RestResponse restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(service, getUser(), artifactReqDetails);
////		Integer responseCode = restResponse.getErrorCode();
////		Assert.assertEquals(responseCode, (Integer)200, "Response code is not correct.");
////		
////		service = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceMetadata.getName(), serviceMetadata.getVersion());
//		
//		Map<String, Object> artifactsService = getArtifactsOfComponentAndComponentsInstance(service);
//		
//		System.out.println("12354");
//		
//		artifactsService.put(vfResource.getToscaResourceName(), artifacts);
//		
//		System.out.println("1234");

	}
	
	public void compareArtifactFromFileStructureToArtifactsFromJavaObject(Map<String, Object> artifactFromJavaObject, Map<String, Object> artifactsFromFileStructure) {
		for(String key: artifactFromJavaObject.keySet()) {
			if((!key.equals("Deployment")) && (!key.equals("Informational"))) {
				Map<String, Object> newArtifactFromJavaObject = (Map<String, Object>) artifactFromJavaObject.get(key);
				Map<String, Object> newArtifactsFromFileStructure = (Map<String, Object>) artifactsFromFileStructure.get(key);
				compareArtifactFromFileStructureToArtifactsFromJavaObject(newArtifactFromJavaObject, newArtifactsFromFileStructure);
			} else {
				compareArtifacts(artifactFromJavaObject.get(key), artifactsFromFileStructure.get(key));
			}
		}
	}
	
	
	private void compareArtifacts(Object artifactFromJavaObject, Object artifactsFromFileStructure) {	
		Map<String, List<String>> artifactsMap = (Map<String, List<String>>) artifactFromJavaObject;
		List<HeatMetaFirstLevelDefinition> artifactsList = (List<HeatMetaFirstLevelDefinition>) artifactsFromFileStructure;

		for(HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition: artifactsList) {
			Assert.assertTrue(artifactsMap.get(heatMetaFirstLevelDefinition.getType()).contains(heatMetaFirstLevelDefinition.getFileName()), 
					"Expected that artifacts will be the same. Not exists: " + heatMetaFirstLevelDefinition.getFileName() + " of type: " + heatMetaFirstLevelDefinition.getType());
		}
		
		for(String key: artifactsMap.keySet()) {
			List<String> artifacts = artifactsMap.get(key);
			
			for(HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition: artifactsList) {
				if(heatMetaFirstLevelDefinition.getType().equals(key)) {
					if(artifacts.contains(heatMetaFirstLevelDefinition.getFileName())) {
						artifacts.remove(heatMetaFirstLevelDefinition.getFileName());
					}
				}
			}
			
			Assert.assertEquals(artifacts.size(), 0, "Expected that all artifacts equal. There is artifacts which not equal: " + artifacts.toString());
		}
	}
	

	public Map<String, Object> getArtifactsOfResourceInstance(List<ImmutablePair<ComponentInstance, ArtifactDefinition>> riList) {
		Map<String, Object> artifacts = new HashMap<>();
		
		for(ImmutablePair<ComponentInstance, ArtifactDefinition> ri: riList) {
			ArtifactDefinition artifactDefinition = ri.getRight();
			ComponentInstance componentInstance = ri.getLeft();
			if(artifacts.containsKey(componentInstance.getNormalizedName())) {
				if( ((Map<String, ArrayList<String>>)((Map<String, Object>)artifacts.get(componentInstance.getNormalizedName())).get("Deployment")).containsKey(artifactDefinition.getArtifactType()) ) {

					((Map<String, ArrayList<String>>)((Map<String, Object>) artifacts.get(componentInstance.getNormalizedName())).get("Deployment")).get(artifactDefinition.getArtifactType()).add(artifactDefinition.getArtifactName());

				} else {
					ArrayList<String> list = new ArrayList<String>();
					list.add(artifactDefinition.getArtifactName());				
					((Map<String, ArrayList<String>>)((Map<String, Object>) artifacts.get(componentInstance.getNormalizedName())).get("Deployment")).put(artifactDefinition.getArtifactType(), list);
				}	
		
			} else {
				try {
					
					
					ArrayList<String> list = new ArrayList<String>();
					list.add(artifactDefinition.getArtifactName());
					
					Map<String, ArrayList<String>> map = new HashMap<>();
					map.put(artifactDefinition.getArtifactType(), list);
					
					Map<String, Map<String, ArrayList<String>>> addMap = new HashMap<>();
					addMap.put("Deployment", map);
					
					artifacts.put(componentInstance.getNormalizedName(), addMap);
					
//					if(artifacts.size() == 0) {
//						artifacts.put("Deployment", addMap);
//					} else {
//						((Map<String, Map<String, ArrayList<String>>>) artifacts.get("Deployment")).putAll(addMap);
//					}
				} catch (Exception e) {
					Assert.fail("Artifact name is null for componentInstance: " + componentInstance.getNormalizedName());
				}
			}
		}
		return artifacts;
	}
	
	public Map<String, Object> getArtifactsOfComponentAndComponentsInstance(Component component) {
		Map<String, Object> artifacts = getArtifacstOfComponent(component);
		
		for(ComponentInstance componentInstance: component.getComponentInstances()) {
			Map<String, Object> artifacstOfComponentInstance = getArtifacstOfComponentInstance(componentInstance);
			if(artifacstOfComponentInstance.size() > 0) {
				artifacts.put(componentInstance.getToscaComponentName() + "." + componentInstance.getComponentVersion(), artifacstOfComponentInstance);
			}
		}
		
		return artifacts;
	}
	
	public Map<String, Object> getArtifacstOfComponentInstance(ComponentInstance componentInstance) {
		Map<String, Object> map = new HashMap<>();
		
		if(componentInstance.getArtifacts() != null) {
			Map<String, Object> informationalArtifacts = getArtifacts(componentInstance.getArtifacts());
			if(informationalArtifacts.size() > 0) {
				map.put("Informational", informationalArtifacts);
			}
		}
		
		if(componentInstance.getDeploymentArtifacts() != null) {
			Map<String, Object> deploymentArtifacts = getArtifacts(componentInstance.getDeploymentArtifacts());
			if(deploymentArtifacts.size() > 0) {
				map.put("Deployment", deploymentArtifacts);
			}
		}
		
		return map;
	}
	
	public Map<String, Object> getArtifacstOfComponent(Component component) {
		Map<String, Object> map = new HashMap<>();
		
		if(component.getArtifacts() != null) {
			Map<String, Object> informationalArtifacts = getArtifacts(component.getArtifacts());
			if(informationalArtifacts.size() > 0) {
				map.put("Informational", informationalArtifacts);
			}
		}
		
		if(component.getDeploymentArtifacts() != null) {
			Map<String, Object> deploymentArtifacts = getArtifacts(component.getDeploymentArtifacts());
			if(deploymentArtifacts.size() > 0) {
				map.put("Deployment", deploymentArtifacts);
			}
		}
		
		return map;
	}
	
	public Map<String, Object> getArtifacts(Map<String, ArtifactDefinition> artifacts) {
		Map<String, Object> map = new HashMap<>();
		
		for(String artifact: artifacts.keySet()) {
			ArtifactDefinition artifactDefinition = artifacts.get(artifact);
			if((artifactDefinition.getEsId() != null) && (!artifactDefinition.getEsId().equals("")) && (!artifactDefinition.getArtifactType().equals("HEAT_ENV"))) {
				if(map.containsKey(artifactDefinition.getArtifactType())) {
					((List<String>) map.get(artifactDefinition.getArtifactType())).add(artifactDefinition.getArtifactName());
				} else {
					ArrayList<String> list = new ArrayList<String>();
					list.add(artifactDefinition.getArtifactName());
					map.put(artifactDefinition.getArtifactType(), list);
				}
			}
		}
		
		return map;
	}
	
	public ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomRI(Component component) throws IOException, Exception {
		ArtifactReqDetails artifactReqDetails = getRandomArtifact();
		Random random = new Random();
		int randInt = random.nextInt(component.getComponentInstances().size());
		User defaultUser = ElementFactory.getDefaultUser(getRole());
		ComponentInstance componentInstance = component.getComponentInstances().get(randInt);
		
		RestResponse uploadArtifactRestResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(component, defaultUser, artifactReqDetails, componentInstance);
		
		// Check response of external API
		Integer responseCode = uploadArtifactRestResponse.getErrorCode();
		Assert.assertEquals(responseCode, (Integer)200, "Response code is not correct.");
		
		ImmutablePair<ComponentInstance, ArtifactDefinition> pair = ImmutablePair.of(componentInstance, ArtifactRestUtils.getArtifactDataFromJson(uploadArtifactRestResponse.getResponse()));
	
		return pair;
	}
	
	public ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomRI(Resource resource) throws IOException, Exception {
		ArtifactReqDetails artifactReqDetails = getRandomVfcArtifact();
		Random random = new Random();
		int randInt = random.nextInt(resource.getComponentInstances().size());
		User defaultUser = ElementFactory.getDefaultUser(getRole());
		ComponentInstance componentInstance = resource.getComponentInstances().get(randInt);
		
		RestResponse uploadArtifactRestResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(resource, defaultUser, artifactReqDetails, componentInstance);
		
		
		
		// Check response of external API
		Integer responseCode = uploadArtifactRestResponse.getErrorCode();
		
//		if(responseCode.equals(404)) {
//			getExtendTest().log(Status.SKIP, String.format("DE271521"));
//			throw new SkipException("DE271521");			
//		}
		
		Assert.assertEquals(responseCode, (Integer)200, "Response code is not correct.");
		
		ImmutablePair<ComponentInstance, ArtifactDefinition> pair = ImmutablePair.of(componentInstance, ArtifactRestUtils.getArtifactDataFromJson(uploadArtifactRestResponse.getResponse()));
	
		return pair;
	}
	
	public ArtifactReqDetails getRandomArtifact() throws IOException, Exception {
		List<String> artifactsTypeList = Arrays.asList("Other");
		return getRandomArtifact(artifactsTypeList);
	}
	
	public ArtifactReqDetails getRandomVfcArtifact() throws IOException, Exception {
		List<String> vfcArtifactsTypeList = Arrays.asList("DCAE_INVENTORY_TOSCA", "DCAE_INVENTORY_JSON", "DCAE_INVENTORY_POLICY", "DCAE_INVENTORY_DOC",
				"DCAE_INVENTORY_BLUEPRINT", "DCAE_INVENTORY_EVENT", "SNMP_POLL", "SNMP_TRAP");
		return getRandomArtifact(vfcArtifactsTypeList);
	}
	
	public ArtifactReqDetails getRandomArtifact(List<String> artifactType) throws IOException, Exception {
		Random random = new Random();
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType.get(random.nextInt(artifactType.size())), true, false);
		return artifactReqDetails;
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
