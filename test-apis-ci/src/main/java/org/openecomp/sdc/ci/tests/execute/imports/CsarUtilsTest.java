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
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
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
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		
		byte[] downloadCSAR = downloadCSAR(sdncModifierDetails, resourceVF);
		
		csarBasicValidation(resourceVF, downloadCSAR);
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
		
		validateServiceCsar(resourceVF1, resourceVF2, service, downloadCSAR, 3, 5, 1);
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
		
		validateVFCsar(resourceVF1, downloadCSAR, 1, 0, 1, 1);
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

			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));

			sb.setLength(0);
			nextEntry = zip.getNextEntry();

			while ((len = zip.read(buffer)) > 0) {
				sb.append(new String(buffer, 0, len));
			}

			resourceYaml = sb.toString();

			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
			ArtifactDefinition artifactDefinition = mainComponent.getToscaArtifacts()
					.get(ASSET_TOSCA_TEMPLATE);
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
			int deploymentArtifactIndexToPass) {
		
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

			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));

			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

			int toscaEntryIndex = 0;
			int generatorEntryIndex = 0;
			int deploymentArtifactIndex = 0;
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

				if (entryName.contains(".xml") && !entryName.startsWith("Artifacts/AAI")) {
					++deploymentArtifactIndex;
					continue;
				}

				if (entryName.startsWith("Artifacts/AAI")) {
					++generatorEntryIndex;
					continue;
				}
				
				assertTrue("Unexpected entry: " + entryName, true);
			}
			assertEquals("Validate amount of entries", toscaEntryIndexToPass, toscaEntryIndex);
			assertEquals("Validate amount of generated AAI artifacts", generatorEntryIndexToPass, generatorEntryIndex);
			assertEquals("Validate amount of generated Deployment artifacts", deploymentArtifactIndexToPass,
					deploymentArtifactIndex);

			ins.close();
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void validateVFCsar(Component certifiedVF, byte[] resultByte, int toscaEntryIndexToPass, int ymlDeploymentArtifactIndexToPass,
			int xmlDeploymentArtifactIndexToPass, int heatEnvDeploymentArtifactIndexToPass) {
		
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

			assertTrue(nextEntry.getName().equals("TOSCA-Metadata/TOSCA.meta"));

			YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

			int toscaEntryIndex = 0;
			int ymlEntryIndex = 0;
			int xmlArtifactsIndex = 0;
			int heatEnvDeploymentArtifactIndex = 0;
			String fileName = null;
			ArtifactDefinition artifactDefinition;
			Component componentToValidate = null;

			artifactDefinition = certifiedVF.getToscaArtifacts().get(ASSET_TOSCA_TEMPLATE);
			String vfFileName = artifactDefinition.getArtifactName();

			while ((nextEntry = zip.getNextEntry()) != null) {
				sb.setLength(0);

				while ((len = zip.read(buffer)) > 0) {
					sb.append(new String(buffer, 0, len));
				}

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

				if (entryName.contains(".xml") && entryName.startsWith("Artifacts/")) {
					++xmlArtifactsIndex;
					continue;
				}

				if (entryName.contains(".sh") && entryName.startsWith("Artifacts/")) {
					++heatEnvDeploymentArtifactIndex;
					continue;
				}
				
				if (entryName.contains(".yml") && entryName.startsWith("Artifacts/")) {
					++ymlEntryIndex;
					continue;
				}
				
				assertTrue("Unexpected entry: " + entryName, false);
			}
			assertEquals("Validate amount of entries", toscaEntryIndexToPass, toscaEntryIndex);
			assertEquals("Validate amount of YAML artifacts", ymlDeploymentArtifactIndexToPass, ymlEntryIndex);
			assertEquals("Validate amount of generated XML artifacts", xmlDeploymentArtifactIndexToPass,
					xmlArtifactsIndex);
			assertEquals("Validate amount of generated HEAT ENV artifacts", heatEnvDeploymentArtifactIndexToPass,
					heatEnvDeploymentArtifactIndex);

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
		assertEquals("Validate component invariantUUID", component.getUUID(), UUID);

		String type = (String) metadata.get("type");
		assertNotNull(type);
		if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
			assertEquals("Validate component type", component.getComponentType().getValue(), type);
		} else {
			assertEquals("Validate component type", ((Resource) component).getResourceType(),
					ResourceTypeEnum.valueOf(type));
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
}
