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

package org.openecomp.sdc.ci.tests.execute.sanity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.model.ToscaMetadataFieldsPresentationEnum;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.ToscaValidation;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

import fj.data.Either;


public class ToscaValidationTest extends SetupCDTest{
	
	ToscaDefinition toscaMainAmdocsDefinition, toscaMainVfDefinition, toscaMainServiceDefinition;
	protected String vnfFile;
	protected String filepath;
	protected File filesFolder;
	protected SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
	protected ISdcCsarHelper fdntCsarHelper;
	protected ResourceReqDetails resourceReqDetails;
	protected Resource resource;
	protected ServiceReqDetails serviceReqDetails;
	protected Service service;
	protected ComponentInstance componentInstanceDefinition;
	User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	File importToscaFilesFolder = new File("C:/Git_work/sdc/catalog-be/src/main/resources/import/tosca/");

	File dataTypesLocation = new File(importToscaFilesFolder.getPath() + "/data-types/dataTypes.yml");

	File genericVfFileLocation = new File(importToscaFilesFolder.getPath() + "/heat-types/Generic_VF/Generic_VF.yml");
	File genericVfcFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_VFC/Generic_VFC.yml");
	File genericPnfFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_PNF/Generic_PNF.yml");
	File genericServiceFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_Service/Generic_Service.yml");

//	Map<String, DataTypeDefinition> parseDataTypesYaml = FileHandling.parseDataTypesYaml(dataTypesLocation.getAbsoluteFile().toString());
	
//	toscaMainAmdocsDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + amdocsCsarFileName));
//	toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + VfCsarFileName));
//	toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + serviceCsarFileName));
//	ToscaTopologyTemplateDefinition topologyTemplate = toscaMainAmdocsDefinition.getTopology_template();
//	ToscaDefinition objectHelper = ToscaParserUtils.parseToscaYamlToJavaObject(genericVfFileLocation);
	

	public ToscaValidationTest(String filepath, String vnfFile) {
		this.filepath = filepath;
		this.vnfFile = vnfFile;
	}


	@BeforeClass
	
	public void precondition() throws Exception{
//--------------------------GENERAL--------------------------------
		setLog(vnfFile);
		filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//--------------------------AMDOCS--------------------------------		
		Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = OnboardingUtillViaApis.createVspViaApis(filepath, vnfFile, user);
		VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
		vendorSoftwareProductObject.setVspName(createVendorSoftwareProduct.left);
		DownloadManager.downloadCsarByNameFromVSPRepository(vendorSoftwareProductObject.getVspName(), vendorSoftwareProductObject.getVspId(), false);
		File amdocsCsarFileName = FileHandling.getLastModifiedFileNameFromDir(filesFolder.getAbsolutePath());
		toscaMainAmdocsDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(amdocsCsarFileName);
		
//TODO--------------------------AMDOCS DOWNLOAD VIA APIS--------------------------------

//--------------------------VF--------------------------------
//		create VF base on VNF imported from previous step - have, resourceReqDetails object include part of resource metadata
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(vendorSoftwareProductObject, vendorSoftwareProductObject.getVspName());
		resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails, vendorSoftwareProductObject.getVspName());
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		File VfCsarFileName = new File(File.separator + "VfCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(resource, new File(filesFolder.getPath() + VfCsarFileName));
		toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + VfCsarFileName));

//--------------------------SERVICE--------------------------------	
		serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(user);
		service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();
//		TODO declare all VFi inputs + add all generic
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		File ServiceCsarFileName = new File(File.separator + "ServiceCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(service, new File(filesFolder.getPath() + ServiceCsarFileName));
		toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + ServiceCsarFileName));
		
//--------------------------verification against Pavel Parser--------------------------------
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Tosca parser is going to convert service csar file to ISdcCsarHelper object...");
		fdntCsarHelper = factory.getSdcCsarHelper(filesFolder.getPath() + ServiceCsarFileName);

	}
	

	//--------------------------Metadata verification--------------------------------	
//--------------------------Resource--------------------------------	
	
	@Test()
	public void validateVfMetadata() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateVfMetadata " + vnfFile);
		//add resource metadata to expected object
		toscaMainAmdocsDefinition = addAndGenerateResourceMetadataToExpectedObject(toscaMainAmdocsDefinition, resourceReqDetails, resource);
		Either<Boolean,Map<String,Object>> resourceToscaMetadataValidator = ToscaValidation.resourceToscaMetadataValidator(toscaMainAmdocsDefinition, toscaMainVfDefinition);
		Assert.assertFalse(!resourceToscaMetadataValidator.left().value().equals(true), "Found error/s on Vf metadata verification");
		
	}
	
	@Test()
	public void validateResourceNodeTemplateMetadata() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateResourceNodeTemplateMetadata " + vnfFile);
		Map<String, Map<String, String>> generateReosurceNodeTemplateMetadataToExpectedObject = generateResourceNodeTemplateMetadataToExpectedObject(resource);
		Boolean resourceToscaMetadataValidator = ToscaValidation.resourceToscaNodeTemplateMetadataValidator(generateReosurceNodeTemplateMetadataToExpectedObject, toscaMainVfDefinition);
		Assert.assertFalse(!resourceToscaMetadataValidator.equals(true), "Found error/s on Resource Node Template metadata verification");
	}

//--------------------------Service--------------------------------	
	@Test()
	public void validateServiceMetadata() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateServiceMetadata " + vnfFile);
		Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
		Either<Boolean,Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.serviceToscaMetadataValidator(generateServiceMetadataToExpectedObject, toscaMainServiceDefinition);
		Assert.assertFalse(!serviceToscaMetadataValidator.left().value().equals(true), "Found error/s on Service metadata verification");
	}


	@Test()
	public void validateServiceNodeTemplateMetadata() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateServiceNodeTemplateMetadata " + vnfFile);
		Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
		Either<Boolean,Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.componentToscaNodeTemplateMetadataValidator(generateServiceNodeTemplateMetadataToExpectedObject, toscaMainServiceDefinition, componentInstanceDefinition.getName(), ComponentTypeEnum.SERVICE);
		Assert.assertFalse(!serviceToscaMetadataValidator.left().value().equals(true), "Found error/s on Service Node Template metadata verification");
	}

//--------------------------Service verification against Pavel Parser--------------------------------
	@Test()
	public void validateServiceMetadataUsingParser() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateServiceMetadataUsingParser " + vnfFile);
		Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
		Metadata serviceMetadata = fdntCsarHelper.getServiceMetadata();
		Either<Boolean,Map<String, Object>> serviceToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceMetadataToExpectedObject, serviceMetadata);
		Assert.assertFalse(!serviceToscaMetadataValidatorAgainstParser.left().value().equals(true), "Found error/s on Service metadata verification");
	}

	@Test()
	public void validateServiceNodeTemplateMetadataUsingParser() throws Exception{
		setLog(vnfFile);
		SetupCDTest.getExtendTest().log(Status.INFO, "validateServiceMetadataUsingParser " + vnfFile);
		Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
		List<NodeTemplate> serviceNodeTemplates = fdntCsarHelper.getServiceNodeTemplates();
		Metadata serviceNodeTemplateMetadata = serviceNodeTemplates.get(0).getMetaData();
		Either<Boolean,Map<String, Object>> serviceNodeTemplateToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceNodeTemplateMetadataToExpectedObject, serviceNodeTemplateMetadata);
		Assert.assertFalse(!serviceNodeTemplateToscaMetadataValidatorAgainstParser.left().value().equals(true), "Found error/s on Service metadata verification");
	}

	
	
	//--------------------------Input verification--------------------------------
	
	//--------------------------Resource--------------------------------
	
	
	//--------------------------Service--------------------------------
	
	
	//--------------------------Service verification against Pavel Parser--------------------------------
	
	
	
	
	
	
	
	
	
	@Override
    protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
    }




	public static ToscaDefinition addAndGenerateResourceMetadataToExpectedObject(ToscaDefinition toscaDefinition, ResourceReqDetails resourceReqDetails, Component component) {
		
		Map<String, String> metadata = convertResourceMetadataToMap(resourceReqDetails, component);
		toscaDefinition.setMetadata(metadata);
		return toscaDefinition;
	}


	public static Map<String, String> convertResourceMetadataToMap(ResourceReqDetails resourceReqDetails, Component component) {
		Map<String, String> metadata = new HashMap<>();
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CATEGORY.value, resourceReqDetails.getCategories().get(0).getName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.DESCRIPTION.value, resourceReqDetails.getDescription());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.INVARIANT_UUID.value, component.getInvariantUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.TYPE.value, "VF");
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.UUID.value, component.getUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAME.value, component.getName());
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_NAME.value, resourceReqDetails.getVendorName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_MODEL_NUMBER.value, resourceReqDetails.getResourceVendorModelNumber());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_RELEASE.value, resourceReqDetails.getVendorRelease());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SUBCATEGORY.value, resourceReqDetails.getCategories().get(0).getSubcategories().get(0).getName());
		return metadata;
	}
	
	public static Map<String, String> convertResourceNodeTemplateMetadataToMap(ComponentInstance componentInstance) throws Exception{
		
		Resource resource = AtomicOperationUtils.getResourceObject(componentInstance.getComponentUid());
		Map<String, String> metadata = new HashMap<>();
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CATEGORY.value, resource.getCategories().get(0).getName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.DESCRIPTION.value, resource.getDescription());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.INVARIANT_UUID.value, resource.getInvariantUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.TYPE.value, resource.getResourceType().toString());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.UUID.value, resource.getUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAME.value, resource.getName());
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_NAME.value, resource.getVendorName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_MODEL_NUMBER.value, resource.getResourceVendorModelNumber());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.RESOURCE_VENDOR_RELEASE.value, resource.getVendorRelease());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SUBCATEGORY.value, resource.getCategories().get(0).getSubcategories().get(0).getName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CUSTOMIZATION_UUID.value, componentInstance.getCustomizationUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.VERSION.value, componentInstance.getComponentVersion());
		
		return metadata;
		
	}
	public static Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject(ResourceReqDetails resourceReqDetails, Component component, ComponentInstance componentInstanceDefinition) {
		
		Map<String, String> metadata = convertResourceMetadataToMap(resourceReqDetails, component);
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CUSTOMIZATION_UUID.value, componentInstanceDefinition.getCustomizationUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.VERSION.value, componentInstanceDefinition.getComponentVersion());
		
		return metadata;
	}
	
	public static Map<String, Map<String, String>> generateResourceNodeTemplateMetadataToExpectedObject(Component component) throws Exception {
		
		Map<String, Map<String, String>> resourcesNodeTemplateMetadataMap = new HashMap<>();
		if(component.getComponentInstances() != null && component.getComponentInstances().size() != 0){
			for (ComponentInstance componentInstance:component.getComponentInstances()){
				Map<String, String> metadata = convertResourceNodeTemplateMetadataToMap(componentInstance);
				resourcesNodeTemplateMetadataMap.put(componentInstance.getName(), metadata);
			}
		}
		return resourcesNodeTemplateMetadataMap;
	}
	
	public static Map<String, String> generateServiceMetadataToExpectedObject(ServiceReqDetails serviceReqDetails, Component component) {
		
		Map<String, String> metadata = new HashMap<>();
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CATEGORY.value, serviceReqDetails.getCategories().get(0).getName());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.DESCRIPTION.value, serviceReqDetails.getDescription());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.INVARIANT_UUID.value, component.getInvariantUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.TYPE.value, "Service");
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.UUID.value, component.getUUID());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAME.value, component.getName());
		
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_TYPE.value, serviceReqDetails.getServiceType());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_ROLE.value, serviceReqDetails.getServiceRole());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAMING_POLICY.value, serviceReqDetails.getNamingPolicy());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.ECOMP_GENERATED_NAMING.value, serviceReqDetails.getEcompGeneratedNaming().toString());
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_ECOMP_NAMING.value, serviceReqDetails.getEcompGeneratedNaming().toString());//equals to ECOMP_GENERATED_NAMING
		
		return metadata;
	}
	

	
}
