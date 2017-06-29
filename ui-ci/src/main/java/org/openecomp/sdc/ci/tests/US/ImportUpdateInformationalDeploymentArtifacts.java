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

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ImportUpdateInformationalDeploymentArtifacts extends SetupCDTest {
	
	private String folder ="US747946";

	// US747946 - Import artifacts to component instances
	// TC1407822 - 	Import VFC Artifacts - Deployment Artifacts - Multiple Artifacts, Multiple Types
	@Test
	public void importVfvArtifactsDeploymentArtifactsMultipleArtifactsMultipleTypes() throws Exception {
		
		if(true){
			throw new SkipException("Due to the new design the test should be updated accordingly");			
		}
		
		String filePath = FileHandling.getFilePath(folder);
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		
		String fileName = "TC1407822.csar";
		
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
		
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
		
		List<String> snmpPollArtifactList = Stream
				.of("base_cgi_frwl.mib", "base_vIECCF_volume.yml", "node_userdata_script.sh", "vendor-license-model.xml")
				.collect(Collectors.toList());
		List<String> snmpTrapArtifactList = Stream
				.of("module_1_ixlt.mib", "module_1_ixlt.yaml")
				.collect(Collectors.toList());
		
	
		List<ArtifactDefinition> filteredArtifactNames = 
				//Stream of component Instances
				resource.getComponentInstances().stream()
					//Stream of all the artifacts on all the component instances
					.flatMap( e -> e.getDeploymentArtifacts().values().stream())
					//filter relevant artifact types
					.filter( e -> e.getArtifactType().equals(ArtifactTypeEnum.SNMP_TRAP.getType()) || e.getArtifactType().equals(ArtifactTypeEnum.SNMP_POLL.getType()))
					//collect to list
					.collect(Collectors.toList());

		
		assertTrue("Not contain all SNMP TRAP artifacts.", filteredArtifactNames.stream()
				.filter(e -> e.getArtifactType().equals(ArtifactTypeEnum.SNMP_TRAP.getType()))
				.map(e -> e.getArtifactName())
				.collect(Collectors.toList())
				.containsAll(snmpTrapArtifactList));
		
		assertTrue("Not contain all SNMP POLL artifacts.", filteredArtifactNames.stream()
				.filter(e -> e.getArtifactType().equals(ArtifactTypeEnum.SNMP_POLL.getType()))
				.map(e -> e.getArtifactName())
				.collect(Collectors.toList())
				.containsAll(snmpPollArtifactList));
		
		filteredArtifactNames.stream()
			.map(e->e.getArtifactDisplayName())
			.collect(Collectors.toList())
			.forEach(e -> {
				assertTrue("Wrong artifact appear on deployment artifact UI page.", 
						!GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + e));
			});
		
	}
	
	
	
	// US747946 - Import artifacts to component instances
	// TC1408044 - Import VFC Artifacts - Informational Artifacts on Single VFC
	@Test
	public void importVfcArtifactsInformationalArtifactsOnSingleVfc() throws Exception {
		
		if(true){
			throw new SkipException("Due to the new design the test should be updated accordingly");			
		}
		
		String filePath = FileHandling.getFilePath(folder);
		String fileName = "TC1408044.csar";
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
		
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
		
		resource.getComponentInstances().forEach(e -> {
			
			if(e.getToscaComponentName().endsWith("heat.ltm")) {
				Map<String, List<String>> artifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.GUIDE.getType(), Arrays.asList("module_1_ldsa.yaml", "vendor-license-model.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("module_2_ldsa.yaml", "vf-license-model.xml"));
					}
				};
					
				validateInformationalArtifactOnComponetInstance(e, artifactsMap, "heat.ltm");
			}
		});
	}
	
	// TODO: Note there is performance issue with this CSAR
	// US747946 - Import artifacts to component instances
	// TC1407998 - Import VFC Artifacts - Deployment & Informational Artifacts - Multiple VFCs
	@Test
	public void importVfcArtifactsDeploymentAndInformationalArtifactsMultipleVfcs() throws Exception {
		
		if(true){
			throw new SkipException("Due to the new design the test should be updated accordingly");			
		}
		
		String filePath = FileHandling.getFilePath(folder);
		String fileName = "TC1407998.csar";
			
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
			
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
		
//		resourceMetaData.setName("TC1407998");
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
			
		resource.getComponentInstances().forEach(e -> {
				
			if(e.getToscaComponentName().endsWith("heat.ps")) {
				Map<String, List<String>> deployArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.SNMP_POLL.getType(), Arrays.asList("PS_DEPL_Poll1.mib", "PS_DEPL_Poll2.xml", "PS_DEPL_Poll3.yaml"));
						put(ArtifactTypeEnum.SNMP_TRAP.getType(), Arrays.asList("PS_DEPL_Trap1.mib", "PS_DEPL_Trap2.xml", "PS_DEPL_Trap3.sh", "PS_DEPL_Trap4.yml"));
					}
				};
				validateDeploymentArtifactOnComponetInstance(e, deployArtifactsMap, "heat.ps");
				
				Map<String, List<String>> infoArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.GUIDE.getType(), Arrays.asList("PS_INFO_GUIDE1.yaml", "PS_INFO_GUIDE2.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("PS_INFO_OTHER1.yaml", "PS_INFO_OTHER2.xml"));
					}
				};
				validateInformationalArtifactOnComponetInstance(e, infoArtifactsMap, "heat.ps");
				
				
			} else if (e.getToscaComponentName().endsWith("heat.sm")) {
				Map<String, List<String>> deployArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.SNMP_POLL.getType(), Arrays.asList("SM_DEPL_Poll1.mib", "SM_DEPL_Poll2.mib", "SM_DEPL_Poll3.xml"));
						put(ArtifactTypeEnum.SNMP_TRAP.getType(), Arrays.asList("SM_DEPL_Trap1.mib", "SM_DEPL_Trap2.xml"));
					}
				};
				validateDeploymentArtifactOnComponetInstance(e, deployArtifactsMap, "heat.sm");
				
				Map<String, List<String>> infoArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.GUIDE.getType(), Arrays.asList("SM_INFO_GUIDE1.yaml", "SM_INFO_GUIDE2.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("SM_INFO_OTHER1.yaml", "SM_INFO_OTHER2.xml"));
					}
				};
				validateInformationalArtifactOnComponetInstance(e, infoArtifactsMap, "heat.sm");
			}
		});
			
	}
	
	// US747946 - Import artifacts to component instances
	// TC1410352 - Import VFC Artifacts - Deployment Artifacts - Extra folder Under VFC-Identification
	@Test
	public void importVfcArtifactsDeploymentArtifactsExtraFolderUnderVfcIdentification() throws Exception {
		
		if(true){
			throw new SkipException("Due to the new design the test should be updated accordingly");			
		}
		
		String filePath = FileHandling.getFilePath(folder);
		String fileName = "TC1410352.csar";
			
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
			
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
		
//		resourceMetaData.setName("TC1410352");
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
			
		resource.getComponentInstances().forEach(e -> {
				
			if(e.getToscaComponentName().endsWith("heat.ltm")) {
				Map<String, List<String>> deployArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.SNMP_POLL.getType(), Arrays.asList("Poll1.mib", "Poll2.xml", "Poll3.sh", "Poll4.yml"));
						put(ArtifactTypeEnum.SNMP_TRAP.getType(), Arrays.asList("Trap1.mib", "Trap2.yaml"));
					}
				};
				validateDeploymentArtifactOnComponetInstance(e, deployArtifactsMap, "heat.ltm");
				
				Map<String, List<String>> infoArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.GUIDE.getType(), Arrays.asList("GUIDE1.yaml", "GUIDE2.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("OTHER1.yaml", "OTHER2.xml"));
					}
				};
				validateInformationalArtifactOnComponetInstance(e, infoArtifactsMap, "heat.ltm");
			}
		});
	}
	
	
	// US747946 - Import artifacts to component instances
	// TC1410352 - Import VFC Artifacts - Deployment Artifacts - Invalid Artifact Type
	@Test
	public void importVfcArtifactsDeploymentArtifactsInvalidArtifactType() throws Exception {
		
		if(true){
			throw new SkipException("Due to the new design the test should be updated accordingly");			
		}
		
		String filePath = FileHandling.getFilePath(folder);
		String fileName = "TC1425032.csar";
				
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
				
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());
			
//		resourceMetaData.setName("TC1425032");
		Resource resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
				
		resource.getComponentInstances().forEach(e -> {
					
			if(e.getToscaComponentName().endsWith("heat.ltm")) {
				Map<String, List<String>> deployArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.SNMP_POLL.getType(), Arrays.asList("DeploySNMPPoll1.mib", "DeploySNMPPoll2.yml", "DeploySNMPPoll3.sh", "DeploySNMPPoll4.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("DeploySNMPTrapB1.mib", "DeploySNMPTrapB2.yaml"));
					}
				};
				validateDeploymentArtifactOnComponetInstance(e, deployArtifactsMap, "heat.ltm");
					
				Map<String, List<String>> infoArtifactsMap = new HashMap<String, List<String>>() {
					{
						put(ArtifactTypeEnum.GUIDE.getType(), Arrays.asList("InfoGuide1.yaml", "InfoGuide2.xml"));
						put(ArtifactTypeEnum.OTHER.getType(), Arrays.asList("InfoOther1.yaml", "InfoOther2.xml", "InfoInvalid1.yaml"));
					}
				};
				validateInformationalArtifactOnComponetInstance(e, infoArtifactsMap, "heat.ltm");
			}
		});
	}
	
	
	private void validateInformationalArtifactOnComponetInstance(ComponentInstance instacne, Map<String, List<String>> artifactsMap, String endswith){
		if(instacne.getToscaComponentName().endsWith(endswith) ){
			Set<String> types = artifactsMap.keySet();

			Map<String, List<ArtifactDefinition>> collect = instacne.getArtifacts().values().stream()
					.filter( a -> types.contains(a.getArtifactType()))
					.collect(Collectors.groupingBy( e -> e.getArtifactType()));
				
			types.forEach(m -> {
				if(collect.containsKey(m)){
					List<String> found = collect.get(m).stream().map(e -> e.getArtifactName()).collect(Collectors.toList());
					boolean isValid = found.containsAll(artifactsMap.get(m)) && artifactsMap.get(m).containsAll(found);
					assertTrue("Not contain all artifact of type: " + m, isValid);
				} else{
					assertTrue("Contains informational artifact which not in provided list", false);
				}
			});
		}
	}
	
	private void validateDeploymentArtifactOnComponetInstance(ComponentInstance instacne, Map<String, List<String>> artifactsMap, String endswith){
		if(instacne.getToscaComponentName().endsWith(endswith) ){
			Set<String> types = artifactsMap.keySet();

			Map<String, List<ArtifactDefinition>> collect = instacne.getDeploymentArtifacts().values().stream()
					.filter( a -> types.contains(a.getArtifactType()))
					.collect(Collectors.groupingBy( e -> e.getArtifactType()));
		
			types.forEach(m -> {
				if(collect.containsKey(m)){
					List<String> found = collect.get(m).stream().map(e -> e.getArtifactName()).collect(Collectors.toList());
					boolean isValid = found.containsAll(artifactsMap.get(m)) && artifactsMap.get(m).containsAll(found);
					assertTrue("Not contain all artifact of type: " + m, isValid);
				} else{
					assertTrue("Contains deployment artifact which not in provided list", false);
				}
			});
		}
	}
	
	

	
	
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
