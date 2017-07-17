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

package org.openecomp.sdc.ci.tests.execute.imports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class CsarUtilsTest extends ComponentBaseTest {
	
	public static final String ASSET_TOSCA_TEMPLATE = "assettoscatemplate";
	
	@Rule
	public static TestName name = new TestName();
	
	public CsarUtilsTest() {
		super(name, CsarUtilsTest.class.getName());
	}
	
	@Test(enabled = true)
	public void createServiceCsarBasicTest() throws Exception {
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, service);
		
		csarBasicValidation(service, downloadCSAR);
	}
	
	@Test(enabled = true)
	public void createResourceCsarBasicTest() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
				
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, resourceVF);
		
		csarBasicValidation(resourceVF, downloadCSAR);
		
		validateVFCsar(resourceVF, downloadCSAR, 1, 0, 0, 0, 0, 0, 0);

		
	}
	
	@Test(enabled = true)
	public void createServiceCsarInclDeploymentArtTest() throws Exception {
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF1 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resourceVF2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		resourceVF1 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		resourceVF2 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF1, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER, true);
		
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG_XML, service, UserRoleEnum.DESIGNER, true, true);
		
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, service);
		
		csarBasicValidation(service, downloadCSAR);
		
		validateServiceCsar(resourceVF1, resourceVF2, service, downloadCSAR, 3, 3, 1, 0);
	}
	
	@Test(enabled = true)
	public void createResourceCsarInclDeploymentArtTest() throws Exception {

		Resource resourceVF1 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG_XML, resourceVF1, UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_ARTIFACT, resourceVF1, UserRoleEnum.DESIGNER, true, true);
		
		resourceVF1 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, resourceVF1);
		
		csarBasicValidation(resourceVF1, downloadCSAR);
		
		validateVFCsar(resourceVF1, downloadCSAR, 1, 0, 1, 1, 0, 0, 0);
	}
	
	@Test(enabled = true)
	public void createResourceCsarInclInformationalArtTest() throws Exception {

		Resource resourceVF1 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();

		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG_XML, resourceVF1, UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceVF1, UserRoleEnum.DESIGNER, false, true);
		
		resourceVF1 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, resourceVF1);
		
		csarBasicValidation(resourceVF1, downloadCSAR);
		
		validateVFCsar(resourceVF1, downloadCSAR, 1, 0, 0, 0, 1, 1, 0);
	}
	
	@Test(enabled = true)
	public void createServiceCsarNotMandatoryMetadataFieldsTest() throws Exception {
		
		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		service.setServiceType("serviceTypeTest");
		service.setServiceRole("serviceRoleTest");
		ServiceRestUtils.updateService(new ServiceReqDetails(service), sdncModifierDetails);

		Resource resourceVF1 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resourceVF2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		
		resourceVF1 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		resourceVF2 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF1, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER, true);
				
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
				
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, service);
		
		csarBasicValidation(service, downloadCSAR);
		
		validateServiceCsar(resourceVF1, resourceVF2, service, downloadCSAR, 3, 3, 0, 0);
	}

	@Test(enabled = true)
	public void createResourceCsarNotMandatoryMetadataFieldsTest() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		resourceVF.setResourceVendorModelNumber("modelNumberTest");
		ResourceRestUtils.updateResourceMetadata(new ResourceReqDetails(resourceVF), sdncModifierDetails, resourceVF.getUniqueId());
		
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, resourceVF);
		
		csarBasicValidation(resourceVF, downloadCSAR);
		
		validateVFCsar(resourceVF, downloadCSAR, 1, 0, 0, 0, 0, 0, 0);

		
	}

	
	private void csarBasicValidation(Component mainComponent, byte[] downloadCSAR) {
		try (ByteArrayInputStream ins = new ByteArrayInputStream(downloadCSAR);
				ZipInputStream zip = new ZipInputStream(ins);) {

			String resourceYaml = null;
			byte[] buffer = new byte[1024];
			ZipEntry nextEntry = zip.getNextEntry();
			StringBuffer sb = new StringBuffer();
			int len;

			while ((len = zip.read(buffer)) > 0) {
				sb.append(new String(buffer, 0, len));
			}
			assertTrue(nextEntry.getName().equals("csar.meta"));

			readNextEntry(sb, len, buffer, zip);

			nextEntry = zip.getNextEntry();
			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));
			
			readNextEntry(sb, len, buffer, zip);
			nextEntry = zip.getNextEntry();
			resourceYaml = sb.toString();

			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
			ArtifactDefinition artifactDefinition = mainComponent.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String fileName = artifactDefinition.getArtifactName();
			assertEquals("Tosca-Template file name: ", "Definitions/" + fileName, nextEntry.getName());
			assertTrue("Tosca template Yaml validation: ", yamlToObjectConverter.isValidYaml(resourceYaml.getBytes()));

			ins.close();
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void validateServiceCsar(Component certifiedVFC1, Component certifiedVFC2, Service fetchedService,
			byte[] resultByte, int toscaEntryIndexToPass, int generatorEntryIndexToPass,
			int deploymentArtifactIndexToPass, int informationalArtifactIndexToPass) {
		
		// TODO Test to validate everything is right (comment out after testing)
		/*try {
			FileUtils.writeByteArrayToFile(new File("c:/TestCSAR/" + fetchedService.getName() + ".zip"), resultByte);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}*/

		try (ByteArrayInputStream ins = new ByteArrayInputStream(resultByte);
				ZipInputStream zip = new ZipInputStream(ins);) {
			
			String resourceYaml = null;
			byte[] buffer = new byte[1024];
			ZipEntry nextEntry = zip.getNextEntry();
			StringBuffer sb = new StringBuffer();
			int len;

			while ((len = zip.read(buffer)) > 0) {
				sb.append(new String(buffer, 0, len));
			}
			assertTrue(nextEntry.getName().equals("csar.meta"));	
			readNextEntry(sb, len, buffer, zip);
			nextEntry = zip.getNextEntry();
			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));
			readNextEntry(sb, len, buffer, zip);


			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

			int toscaEntryIndex = 0;
			int generatorEntryIndex = 0;
			int deploymentArtifactIndex = 0;
			int informationalArtifactIndex = 0;
			String fileName = null;
			ArtifactDefinition artifactDefinition;
			Component componentToValidate = null;

			artifactDefinition = fetchedService.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String serviceFileName = artifactDefinition.getArtifactName();
			artifactDefinition = certifiedVFC1.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String vfc1FileName = artifactDefinition.getArtifactName();
			artifactDefinition = certifiedVFC2.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String vfc2FileName = artifactDefinition.getArtifactName();

			while ((nextEntry = zip.getNextEntry()) != null) {
				sb.setLength(0);

				while ((len = zip.read(buffer)) > 0) {
					sb.append(new String(buffer, 0, len));
				}

				String entryName = nextEntry.getName();

				resourceYaml = sb.toString();
				if (entryName.contains(serviceFileName)) {
					componentToValidate = fetchedService;
					fileName = "Definitions/" + serviceFileName;
					
					assertEquals("Validate entry Name", (fileName), nextEntry.getName());
					assertTrue(yamlToObjectConverter.isValidYaml(resourceYaml.getBytes()));
					validateContent(resourceYaml, componentToValidate);
					++toscaEntryIndex;
					continue;
				}
				
				if (entryName.contains(vfc1FileName)) {
					componentToValidate = certifiedVFC1;
					fileName = "Definitions/" + vfc1FileName;
					
					assertEquals("Validate entry Name", (fileName), nextEntry.getName());
					assertTrue(yamlToObjectConverter.isValidYaml(resourceYaml.getBytes()));
					validateContent(resourceYaml, componentToValidate);
					++toscaEntryIndex;
					continue;
				}
				if (entryName.contains(vfc2FileName)) {
					componentToValidate = certifiedVFC2;
					fileName = "Definitions/" + vfc2FileName;
					
					assertEquals("Validate entry Name", (fileName), nextEntry.getName());
					assertTrue(yamlToObjectConverter.isValidYaml(resourceYaml.getBytes()));
					validateContent(resourceYaml, componentToValidate);
					++toscaEntryIndex;
					continue;
				}

				if (entryName.startsWith("Artifacts/Deployment/MODEL_INVENTORY_PROFILE") && entryName.contains("AAI")) {
					++generatorEntryIndex;
					continue;
				}
				
				if (entryName.contains(".xml") && entryName.startsWith("Artifacts/Deployment/") && !entryName.contains("AAI")) {
					++deploymentArtifactIndex;
					continue;
				}
				
				if (entryName.contains(".xml") && entryName.startsWith("Artifacts/Informational/") && !entryName.contains("AAI")) {
					++informationalArtifactIndex;
					continue;
				}
				
				assertTrue("Unexpected entry: " + entryName, true);
			}
			assertEquals("Validate amount of entries", toscaEntryIndexToPass, toscaEntryIndex);
			assertEquals("Validate amount of generated AAI artifacts", generatorEntryIndexToPass, generatorEntryIndex);
			assertEquals("Validate amount of Deployment artifacts entries", deploymentArtifactIndexToPass,
					deploymentArtifactIndex);
			assertEquals("Validate amount of Informational artifacts entries", informationalArtifactIndexToPass,
					informationalArtifactIndex);
			
			ins.close();
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void validateVFCsar(Component certifiedVF, byte[] resultByte, int toscaEntryIndexToPass, 
			int ymlDeploymentArtifactIndexToPass, int xmlDeploymentArtifactIndexToPass, int heatDeploymentArtifactIndexToPass,
			int ymlInformationalArtifactIndexToPass, int xmlInformationalArtifactIndexToPass, int heatInformationalArtifactIndexToPass) {
		
		// TODO Test to validate everything is right (comment out after testing)
		/*try {
			FileUtils.writeByteArrayToFile(new File("c:/TestCSAR/" + fetchedService.getName() + ".zip"), resultByte);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}*/

		try (ByteArrayInputStream ins = new ByteArrayInputStream(resultByte);
				ZipInputStream zip = new ZipInputStream(ins);) {
			
			String resourceYaml = null;
			byte[] buffer = new byte[1024];
			ZipEntry nextEntry = zip.getNextEntry();
			StringBuffer sb = new StringBuffer();
			int len;

			while ((len = zip.read(buffer)) > 0) {
				sb.append(new String(buffer, 0, len));
			}

			assertTrue(nextEntry.getName().equals("csar.meta"));

			readNextEntry(sb, len, buffer, zip);
			nextEntry = zip.getNextEntry();
			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));
			
			readNextEntry(sb, len, buffer, zip);
		
			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

			int toscaEntryIndex = 0;
			int ymlDeploymentArtifactsIndex = 0;
			int xmlDeploymentArtifactsIndex = 0;
			int heatDeploymentArtifactIndex = 0;
			int ymlInformationalArtifactsIndex = 0;
			int xmlInformationalArtifactsIndex = 0;
			int heatInformationalArtifactIndex = 0;
			String fileName = null;
			ArtifactDefinition artifactDefinition;
			Component componentToValidate = null;

			artifactDefinition = certifiedVF.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String vfFileName = artifactDefinition.getArtifactName();

			while ((nextEntry = zip.getNextEntry()) != null) {
				
				readNextEntry(sb, len, buffer, zip);

				String entryName = nextEntry.getName();

				resourceYaml = sb.toString();
				if (entryName.contains(vfFileName)) {
					componentToValidate = certifiedVF;
					fileName = "Definitions/" + vfFileName;
					
					assertEquals("Validate entry Name", (fileName), nextEntry.getName());
					assertTrue(yamlToObjectConverter.isValidYaml(resourceYaml.getBytes()));
					validateContent(resourceYaml, componentToValidate);
					++toscaEntryIndex;
					continue;
				}

				if (entryName.contains(".xml") && entryName.contains("YANG_XML")) {
					if(entryName.startsWith("Artifacts/Deployment")){
						++xmlDeploymentArtifactsIndex;
						continue;						
					}else if(entryName.startsWith("Artifacts/Informational")){
						++xmlInformationalArtifactsIndex;
						continue;
					}
				}

				if (entryName.contains(".sh") && entryName.contains("HEAT_ARTIFACT")) {
					if(entryName.startsWith("Artifacts/Deployment")){
						++heatDeploymentArtifactIndex;
						continue;					
					}else if(entryName.startsWith("Artifacts/Informational")){
						++heatInformationalArtifactIndex;
						continue;
					}
				}
				
				if ((entryName.contains(".yml") || entryName.contains(".yaml")) && entryName.contains("HEAT")) {
					if(entryName.startsWith("Artifacts/Deployment")){
						++ymlDeploymentArtifactsIndex;
						continue;						
					}else if(entryName.startsWith("Artifacts/Informational")){
						++ymlInformationalArtifactsIndex;
						continue;
					}
				}
				
				if(entryName.contains("Definitions/") && entryName.contains("template-interface.yml")){
					validateInterfaceContent(resourceYaml, certifiedVF);
					continue;
				}
				if(entryName.contains("Definitions/")) {
					if(isImportsFileValidation(entryName))
					continue;
				}
				
				assertTrue("Unexpected entry: " + entryName, false);
			}
			
			//Definitions folder
			assertEquals("Validate amount of entries", toscaEntryIndexToPass, toscaEntryIndex);
			
			//Deployment folder
			assertEquals("Validate amount of YAML Deployment artifacts", ymlDeploymentArtifactIndexToPass, ymlDeploymentArtifactsIndex);
			assertEquals("Validate amount of XML Deployment artifacts", xmlDeploymentArtifactIndexToPass,
					xmlDeploymentArtifactsIndex);
			assertEquals("Validate amount of HEAT Deployment artifacts", heatDeploymentArtifactIndexToPass,
					heatDeploymentArtifactIndex);
			
			//Informational folder
			assertEquals("Validate amount of YAML Informational artifacts", ymlInformationalArtifactIndexToPass, ymlInformationalArtifactsIndex);
			assertEquals("Validate amount of XML Informational artifacts", xmlInformationalArtifactIndexToPass,
					xmlInformationalArtifactsIndex);
			assertEquals("Validate amount of HEAT Informational artifacts", heatInformationalArtifactIndexToPass,
					heatInformationalArtifactIndex);
			
			ins.close();
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void validateContent(String content, Component component) {
		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(content.getBytes());
		@SuppressWarnings("unchecked")
		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		@SuppressWarnings("unchecked")
		Map<String, Object> metadata = (Map<String, Object>) load.get("metadata");
		assertNotNull(metadata);

		String name = (String) metadata.get("name");
		assertNotNull(name);
		assertEquals("Validate component name", component.getName(), name);

		String invariantUUID = (String) metadata.get("invariantUUID");
		assertNotNull(invariantUUID);
		assertEquals("Validate component invariantUUID", component.getInvariantUUID(), invariantUUID);

		String UUID = (String) metadata.get("UUID");
		assertNotNull(UUID);
		assertEquals("Validate component UUID", component.getUUID(), UUID);

		String type = (String) metadata.get("type");
		assertNotNull(type);
		if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
			assertEquals("Validate component type", component.getComponentType().getValue(), type);
			String serviceType = (String) metadata.get("serviceType");
			assertNotNull(serviceType);
			assertEquals("Validate service type", ((Service )component).getServiceType(), serviceType);
			String serviceRole = (String) metadata.get("serviceRole");
			assertNotNull(serviceRole);
			assertEquals("Validate service role", ((Service )component).getServiceRole(), serviceRole);
		} else {
			assertEquals("Validate component type", ((Resource) component).getResourceType(),
					ResourceTypeEnum.valueOf(type));
			String resourceVendorModelNumber = (String) metadata.get("resourceVendorModelNumber");
			assertNotNull(resourceVendorModelNumber);
			assertEquals("Validate resource vendor model number", ((Resource )component).getResourceVendorModelNumber(), resourceVendorModelNumber);
		}
	}
	
	private byte[] downloadCSAR(User sdncModifierDetails, Component createdComponent) throws Exception {

		String artifactUniqeId = createdComponent.getToscaArtifacts().get("assettoscacsar").getUniqueId();
		RestResponse getCsarResponse = null;
		
		switch (createdComponent.getComponentType()) {
		case RESOURCE:
			getCsarResponse = ArtifactRestUtils.downloadResourceArtifactInternalApi(createdComponent.getUniqueId(),
					sdncModifierDetails, artifactUniqeId);
			break;			
		case SERVICE:
			getCsarResponse = ArtifactRestUtils.downloadServiceArtifactInternalApi(createdComponent.getUniqueId(),
					sdncModifierDetails, artifactUniqeId);
			break;
		default:
			break;
		}
		
		assertNotNull(getCsarResponse);
		BaseRestUtils.checkSuccess(getCsarResponse);

		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(getCsarResponse.getResponse(),
				ArtifactUiDownloadData.class);

		assertNotNull(artifactUiDownloadData);

		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		byte[] decodeBase64 = Base64.decodeBase64(fromUiDownload);

		return decodeBase64;
	}
	
	private void validateInterfaceContent(String content, Component component) {
		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(content.getBytes());
		@SuppressWarnings("unchecked")
		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		@SuppressWarnings("unchecked")
		Map<String, Object> node_types = (Map<String, Object>) load.get("node_types");
		assertNotNull(node_types);

		String toscaInterfaceName = node_types.keySet().stream().filter(p -> p.startsWith("org.openecomp.")).findAny().get();
		Map<String, Object> toscaInterface = (Map<String, Object>) node_types.get(toscaInterfaceName);
		assertNotNull(toscaInterface);
		String derived_from = (String) toscaInterface.get("derived_from");
		assertNotNull(derived_from);
		assertEquals("Validate derived from generic", component.getDerivedFromGenericType(), derived_from);

	}
	
	private void readNextEntry(StringBuffer sb, int len, byte[] buffer, ZipInputStream zip) throws IOException {
		sb.setLength(0);

		while ((len = zip.read(buffer)) > 0) {
			sb.append(new String(buffer, 0, len));
		}
	}
	
	private boolean isImportsFileValidation(String fileName) {
	
		switch(fileName){
			case "Definitions/artifacts.yml":
			case "Definitions/capabilities.yml":
			case "Definitions/data.yml":
			case "Definitions/groups.yml":
			case "Definitions/interfaces.yml":
			case "Definitions/nodes.yml":
			case "Definitions/policies.yml":
			case "Definitions/relationships.yml":
				return true;
				
		}
		return false;
	}
}
