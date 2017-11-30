package org.openecomp.sdc.ci.tests.execute.sanity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.model.ToscaMetadataFieldsPresentationEnum;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;
import org.openecomp.sdc.ci.tests.verificator.ToscaValidation;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

import fj.data.Either;


public class ToscaValidationTest extends SetupCDTest{

	private static final String GENERIC_VF = "Generic_VF";
	private static final String GENERIC_PNF = "Generic_PNF";
			
//	private ToscaDefinition toscaMainAmdocsDefinition, toscaMainVfDefinition, toscaMainServiceDefinition, toscaExpectedMainServiceDefinition;
	protected String vnfFile;
	protected String filepath;
//	protected File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
	protected SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
//	protected ISdcCsarHelper fdntCsarHelper;
//	protected ResourceReqDetails resourceReqDetails;
//	protected Resource resource;
//	protected ServiceReqDetails serviceReqDetails;
//	protected Service service;
//	protected ComponentInstance componentInstanceDefinition;
	User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
//	File importToscaFilesFolder = new File("C:/Git_work/sdc/catalog-be/src/main/resources/import/tosca/");

//	File dataTypesLocation = new File(importToscaFilesFolder.getPath() + "/data-types/dataTypes.yml");
//	List<Boolean> status = new ArrayList<>();

//	File genericVfFileLocation = new File(importToscaFilesFolder.getPath() + "/heat-types/Generic_VF/Generic_VF.yml");
//	File genericVfcFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_VFC/Generic_VFC.yml");
//	File genericPnfFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_PNF/Generic_PNF.yml");
//	File genericServiceFileLocation = new File (importToscaFilesFolder.getPath() + "/heat-types/Generic_Service/Generic_Service.yml");

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

	public ToscaValidationTest() {
	}



//	@BeforeClass
	@Test()
	public void toscaFileValidator() throws Exception{
//--------------------------GENERAL--------------------------------
/*//		for debugging only
		setLog("Test");
		File amdocsCsarFileName = (new File("C:\\Users\\al714h\\Downloads\\d218be69637647b0b693647d84a8c03f.csar"));
		toscaMainAmdocsDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(amdocsCsarFileName);
		toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File("C:\\Users\\al714h\\Downloads\\resource-Civfonboarded2016073VmxBv301072E2eE60f5c15-csar.csar"));
	*/
		setLog(vnfFile);
		List<Boolean> status = new ArrayList<>();
		ISdcCsarHelper fdntCsarHelper;
		File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//		filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//--------------------------AMDOCS--------------------------------	
//		vnfFile = "2016-197_vscp_vscp_30_1610_e2e.zip";
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, user);//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
		resourceReqDetails = createCustomizedVsp(resourceReqDetails, filepath, vnfFile);

		ToscaDefinition toscaMainAmdocsDefinition = downloadAndGetToscaMainYamlObjectUI(resourceReqDetails, filesFolder);
//------adding generic inputs to expected object
		toscaMainAmdocsDefinition = addGenericPropertiesToToscaDefinitionObject(toscaMainAmdocsDefinition, GENERIC_VF);
//	copy object
		ToscaDefinition toscaExpectedMainServiceDefinition = new ToscaDefinition(toscaMainAmdocsDefinition);
//TODO--------------------------AMDOCS DOWNLOAD VIA APIS--------------------------------

//--------------------------VF--------------------------------
//		create VF base on VNF imported from previous step - have, resourceReqDetails object include part of resource metadata
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		ToscaDefinition toscaMainVfDefinition = downloadAndGetToscaMainYamlObjectApi(resource, filesFolder);
		
//--------------------------SERVICE--------------------------------	
		ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
//		serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(serviceReqDetails, user);
		Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();

//--------------------------getProperties set values and declare--------------------

		Component componentObject = AtomicOperationUtils.getComponentObject(service, UserRoleEnum.DESIGNER);
		Map<String, List<ComponentInstanceInput>> componentInstancesInputs = componentObject.getComponentInstancesInputs();
		setValuesToPropertiesList(componentInstancesInputs, toscaExpectedMainServiceDefinition);
		PropertyRestUtils.declareProporties(componentObject, componentInstancesInputs, user);

		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		File ServiceCsarFileName = new File(File.separator + "ServiceCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(service, new File(filesFolder.getPath() + ServiceCsarFileName));
		ToscaDefinition toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + ServiceCsarFileName));




		
//--------------------------initialization of Tosca Parser--------------------------------

		fdntCsarHelper = initSdcCsarHelper(ServiceCsarFileName, filesFolder);


//---------------------------TESTS--------------------------------------------------
		status = validateVfMetadata(toscaMainAmdocsDefinition, toscaMainVfDefinition, resourceReqDetails, resource, vnfFile, status);
		status = validateResourceNodeTemplateMetadata(toscaMainVfDefinition, resource, vnfFile, status);
		status = validateServiceMetadata(toscaMainServiceDefinition, serviceReqDetails, service, vnfFile, status);
		status = validateServiceNodeTemplateMetadata(toscaMainServiceDefinition, componentInstanceDefinition, resourceReqDetails, resource, vnfFile, status);
		status = validateServiceMetadataUsingParser(fdntCsarHelper, serviceReqDetails, service, vnfFile, status);
		status = validateServiceNodeTemplateMetadataUsingParser(fdntCsarHelper, resourceReqDetails, resource, componentInstanceDefinition, vnfFile, status);
		status = validateResourceInputs(toscaMainAmdocsDefinition, toscaMainVfDefinition, vnfFile, status);
		status = validateServiceInputs(toscaExpectedMainServiceDefinition, toscaMainServiceDefinition, vnfFile, status);
		status = validateServiceInputsUsingParser(fdntCsarHelper, toscaExpectedMainServiceDefinition, vnfFile, status);
		
		if(status.contains(false)){
			SetupCDTest.getExtendTest().log(Status.FAIL, "Summary: tosca validation test failed with zip file " + vnfFile);
			Assert.assertFalse(true);
		}
	}







	/**The method set values to toscaDefinition object service level only, to resource level should put instead of setDefault --> setValue 
	 * inputs.get(componentInstanceInput.getName()).setValue(randomString);
	 * @param componentInstancesInputs
	 * @param toscaDefinition
	 */
	private void setValuesToPropertiesList(Map<String, List<ComponentInstanceInput>> componentInstancesInputs, ToscaDefinition toscaDefinition) {
		for(Map.Entry<String, List<ComponentInstanceInput>> entry : componentInstancesInputs.entrySet()) {
			List<ComponentInstanceInput> value = entry.getValue();
			String[] names = entry.getKey().split("\\.");
			String expectedServiceInputPrefix = null;
			Map<String, ToscaInputsTopologyTemplateDefinition> inputs = toscaDefinition.getTopology_template().getInputs();
			if(names.length>0) {
				expectedServiceInputPrefix = names[names.length - 1] + "_";
			}
			for (ComponentInstanceInput componentInstanceInput :value) {


				String type = componentInstanceInput.getType();
				List<String> myList = new ArrayList<String>();
				myList.add("cbf8049e-69e8-48c3-a06f-255634391403");
				if (type.equals("string"))  {
					String randomString = getRandomString();
					componentInstanceInput.setValue(randomString);
					inputs.get(componentInstanceInput.getName()).setDefault(randomString);

				}
				else if (type.equals("integer") ) {
					int randomInteger = getRandomInteger();
					componentInstanceInput.setValue(Integer.toString(randomInteger));
					inputs.get(componentInstanceInput.getName()).setDefault(randomInteger);
				}
				else if (type.equals("float") ){
					componentInstanceInput.setValue("5.5");
					inputs.get(componentInstanceInput.getName()).setDefault("5.5");

				}
				else if (type.equals("boolean")  ){
					componentInstanceInput.setValue("true");
					inputs.get(componentInstanceInput.getName()).setDefault("true");
				}
				else if (type.equals("list")  ){
					String myListofStrings = myList.toString();
					componentInstanceInput.setValue(myListofStrings);
					inputs.get(componentInstanceInput.getName()).setDefault(myListofStrings);
				}
				
				String expectedServiceInputName = expectedServiceInputPrefix + componentInstanceInput.getName();
				ToscaInputsTopologyTemplateDefinition oldInput = inputs.get(componentInstanceInput.getName());
				inputs.put(expectedServiceInputName, oldInput);
				inputs.remove(componentInstanceInput.getName());
				
			}
			
		}
	}

	protected String getRandomString() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 18) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String strValue = salt.toString();
		return strValue;

		}

	protected int getRandomInteger() {
		Random r = new Random();
		int Low = 10;
		int High = 100;
		int integerValue = r.nextInt(High - Low) + Low;
		return integerValue;
	}



	//--------------------------Metadata verification--------------------------------	
//--------------------------Resource--------------------------------	
	
	public List<Boolean> validateVfMetadata(ToscaDefinition toscaMainAmdocsDefinition, ToscaDefinition toscaMainVfDefinition, ResourceReqDetails resourceReqDetails,Resource resource, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateVfMetadata", vnfFile);
		//add resource metadata to expected object
		toscaMainAmdocsDefinition = addAndGenerateResourceMetadataToExpectedObject(toscaMainAmdocsDefinition, resourceReqDetails, resource);
		Either<Boolean,Map<String,Object>> resourceToscaMetadataValidator = ToscaValidation.resourceToscaMetadataValidator(toscaMainAmdocsDefinition, toscaMainVfDefinition);
		if(resourceToscaMetadataValidator.isRight())
			status.add(false);
		return status;
	}
	
	public List<Boolean> validateResourceNodeTemplateMetadata(ToscaDefinition toscaMainVfDefinition, Resource resource, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateResourceNodeTemplateMetadata", vnfFile);
		Map<String, Map<String, String>> generateReosurceNodeTemplateMetadataToExpectedObject = generateResourceNodeTemplateMetadataToExpectedObject(resource);
		Boolean resourceToscaMetadataValidator = ToscaValidation.resourceToscaNodeTemplateMetadataValidator(generateReosurceNodeTemplateMetadataToExpectedObject, toscaMainVfDefinition);
		if(! resourceToscaMetadataValidator)
			status.add(false);
		return status;
	}

//--------------------------Service--------------------------------	
	public List<Boolean> validateServiceMetadata(ToscaDefinition toscaMainServiceDefinition, ServiceReqDetails serviceReqDetails, Service service, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateServiceMetadata", vnfFile);
		Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
		Either<Boolean,Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.serviceToscaMetadataValidator(generateServiceMetadataToExpectedObject, toscaMainServiceDefinition);
		if(serviceToscaMetadataValidator.isRight())
			status.add(false);
		return status;
	}


	public List<Boolean> validateServiceNodeTemplateMetadata(ToscaDefinition toscaMainServiceDefinition, ComponentInstance componentInstanceDefinition, ResourceReqDetails resourceReqDetails, Resource resource, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateServiceNodeTemplateMetadata", vnfFile);
		Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
		Either<Boolean,Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.componentToscaNodeTemplateMetadataValidator(generateServiceNodeTemplateMetadataToExpectedObject, toscaMainServiceDefinition, componentInstanceDefinition.getName(), ComponentTypeEnum.SERVICE, componentInstanceDefinition.getName());
		if(serviceToscaMetadataValidator.isRight())
			status.add(false);
		return status;
	}

//--------------------------Service verification against Pavel Parser--------------------------------
	public List<Boolean> validateServiceMetadataUsingParser(ISdcCsarHelper fdntCsarHelper, ServiceReqDetails serviceReqDetails, Service service, String vnfFile, List<Boolean> status) throws Exception{
		if(fdntCsarHelper == null){
			reportSkipTestPrint("validateServiceMetadataUsingParser", status);
		}else{
			reportStartTestPrint("validateServiceMetadataUsingParser", vnfFile);
			Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
			Metadata serviceMetadata = fdntCsarHelper.getServiceMetadata();
			Either<Boolean,Map<String, Object>> serviceToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceMetadataToExpectedObject, serviceMetadata);
			if(serviceToscaMetadataValidatorAgainstParser.isRight())
				status.add(false);
		}
		return status;
	}

	public List<Boolean> validateServiceNodeTemplateMetadataUsingParser(ISdcCsarHelper fdntCsarHelper, ResourceReqDetails resourceReqDetails,Resource resource, ComponentInstance componentInstanceDefinition, String vnfFile, List<Boolean> status) throws Exception{
		if(fdntCsarHelper == null){
			reportSkipTestPrint("validateServiceNodeTemplateMetadataUsingParser", status);			
		}else{
			reportStartTestPrint("validateServiceNodeTemplateMetadataUsingParser", vnfFile);
			Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
			List<NodeTemplate> serviceNodeTemplates = fdntCsarHelper.getServiceNodeTemplates();
			Metadata serviceNodeTemplateMetadata = serviceNodeTemplates.get(0).getMetaData();
			Either<Boolean,Map<String, Object>> serviceNodeTemplateToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceNodeTemplateMetadataToExpectedObject, serviceNodeTemplateMetadata);
			if(serviceNodeTemplateToscaMetadataValidatorAgainstParser.isRight())
				status.add(false);
		}
		return status;
	}

	
	
	//--------------------------Input verification--------------------------------
	
	//--------------------------Resource--------------------------------
	public List<Boolean> validateResourceInputs(ToscaDefinition toscaMainAmdocsDefinition, ToscaDefinition toscaMainVfDefinition, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateResourceInputs", vnfFile);
		Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaMainAmdocsDefinition.getTopology_template().getInputs();
		Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = toscaMainVfDefinition.getTopology_template().getInputs();
		Either<Boolean,Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidator(expectedInputsMap, actualInputsMap);
		if(toscaInputsValidator.isRight())
			status.add(false);
		return status;
	}
	
	//--------------------------Service--------------------------------
	
	public List<Boolean> validateServiceInputs(ToscaDefinition toscaExpectedMainServiceDefinition, ToscaDefinition toscaMainServiceDefinition, String vnfFile, List<Boolean> status) throws Exception{
		reportStartTestPrint("validateServiceInputs", vnfFile);
		Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaExpectedMainServiceDefinition.getTopology_template().getInputs();
		Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = toscaMainServiceDefinition.getTopology_template().getInputs();
		Either<Boolean,Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidator(expectedInputsMap, actualInputsMap);
		if(toscaInputsValidator.isRight())
			status.add(false);
		return status;
	}
	
	//--------------------------Service verification against Pavel Parser--------------------------------
	public List<Boolean> validateServiceInputsUsingParser(ISdcCsarHelper fdntCsarHelper, ToscaDefinition toscaExpectedMainServiceDefinition, String vnfFile, List<Boolean> status) throws Exception{
		if(fdntCsarHelper == null){
			reportSkipTestPrint("validateServiceInputsUsingParser", status);			
		}else{
			reportStartTestPrint("validateServiceInputsUsingParser", vnfFile);
			Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaExpectedMainServiceDefinition.getTopology_template().getInputs();
			Either<Boolean,Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidatorAgainstParser(expectedInputsMap, fdntCsarHelper);
			if(toscaInputsValidator.isRight())
				status.add(false);
		}
		return status;
	}
	
	//--------------------------XXX verification--------------------------------
	
		//--------------------------Resource--------------------------------
		
		
		//--------------------------Service--------------------------------
		
		
		//--------------------------Service verification against Pavel Parser--------------------------------
	
	
	
	
	
	
	
	
	@Override
    protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
    }


	public static ToscaDefinition addGenericInputsToToscaObject(ToscaDefinition toscaDefinition, String genericName) throws Exception {
		Resource genericResource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, genericName, "1.0");
		ToscaTopologyTemplateDefinition topologyTemplate = toscaDefinition.getTopology_template();
		Map<String, ToscaInputsTopologyTemplateDefinition> newInput = new HashMap<String, ToscaInputsTopologyTemplateDefinition>();
			for (PropertyDefinition property : genericResource.getProperties()) {
				ToscaInputsTopologyTemplateDefinition input = new ToscaInputsTopologyTemplateDefinition();
//				input.setConstraints(property.getConstraints());
				input.setDefault(property.getDefaultValue());
				input.setDescription(property.getDescription());
//				input.setEntry_schema(property.getSchema());
				input.setName(property.getName());
//				input.setRequired(property.get);
				input.setStatus(property.getStatus());
				input.setType(property.getType());
				input.setValue(property.getValue());
				newInput.put(property.getName(),input);
			}
				
		topologyTemplate.addInputs(newInput);
		toscaDefinition.setTopology_template(topologyTemplate);
		return toscaDefinition;
	}

	public static ToscaDefinition setNameToToscaInput(ToscaDefinition toscaDefinition) {
		Map<String, ToscaInputsTopologyTemplateDefinition> inputs = toscaDefinition.getTopology_template().getInputs();
		for (String name : inputs.keySet()) {
			inputs.get(name).setName(name);
		}
		toscaDefinition.getTopology_template().setInputs(inputs);
		return toscaDefinition;
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
		metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.TYPE.value, resourceReqDetails.getResourceType());
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
	
	public static void reportStartTestPrint (String testName, String vnfFile){
//		reportMessageInColor("info", "blue", "Running test \" + testName + \" with zip file - \" + vnfFile");
		SetupCDTest.getExtendTest().log(Status.INFO, "<html><font color=\"blue\"> Running test " + testName + " with zip file - " + vnfFile + "</font></html>");
	}
	public static void reportSkipTestPrint (String testName, List<Boolean> status){
//		reportMessageInColor("error", "orange", "Skip test \" + testName + \" due to previous tosca parser error");
		SetupCDTest.getExtendTest().log(Status.ERROR, "<html><font color=\"orange\"> Skip test " + testName + " due to previous tosca parser error" + "</font></html>");
		status.add(false);
	}
	
	public static void reportMessageInColor(String status, String color, String message){
		String printLine = getReportMessageInColor(color, message);
		SetupCDTest.getExtendTest().log(Status.valueOf(status), printLine);
//		SetupCDTest.getExtendTest().log(Status.valueOf(status), getReportMessageInColor(color, message));
	}
	/**
	 * @param color = red, green, orange, blue ... 
	 * @param message - message string
	 * @return string in desired color
	 */
	public static String getReportMessageInColor(String color, String message){
		String returnValue = ("<html><font color=\\\"+color+\"\">" + message + "</font></html>").toString();
		return returnValue;
	}

/*	@Test()
	public void printTest(){
		System.out.println("print");
		reportMessageInColor("ERROR", "green", "green");
		reportMessageInColor("INFO", "orange", "orange");
		reportMessageInColor("INFO", "red", "red");
	}*/

/*	@Test
	public void allottedResourceModelTest() throws Exception{
		List<Boolean> status = new ArrayList<>();

		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = OnbordingDataProviders.getRandomElements(1, fileNamesFromFolder);
		String vnfFile = newRandomFileNamesFromFolder.get(0);
		setLog(vnfFile);
		String filePath = FileHandling.getVnfRepositoryPath();
		File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());

		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(ResourceCategoryEnum.ALLOTTED_RESOURCE_SERVICE_ADMIN);
		resourceReqDetails = createCustomizedVsp(resourceReqDetails, filePath, vnfFile);

		ToscaDefinition toscaMainAmdocsDefinition = downloadAndGetToscaMainYamlObjectUI(resourceReqDetails, filesFolder);
		toscaMainAmdocsDefinition = addGenericPropertiesToToscaDefinitionObject(toscaMainAmdocsDefinition, GENERIC_VF);

		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		ToscaDefinition toscaMainVfDefinition = downloadAndGetToscaMainYamlObjectApi(resource, filesFolder);

		status = validateVfMetadata(toscaMainAmdocsDefinition, toscaMainVfDefinition, resourceReqDetails, resource, vnfFile, status);
		status = validateResourceNodeTemplateMetadata(toscaMainVfDefinition, resource, vnfFile, status);
		status = validateResourceInputs(toscaMainAmdocsDefinition, toscaMainVfDefinition, vnfFile, status);

		if(status.contains(false)){
			SetupCDTest.getExtendTest().log(Status.FAIL, "Summary: allottedResourceModelTest tosca validation test failed with zip file " + vnfFile);
			Assert.assertFalse(true);
		}
	}



	@Test()
	public void NetworkModel() throws Exception{
//--------------------------GENERAL--------------------------------
		String vnfFile = "networkModel";
		setLog(vnfFile);
		List<Boolean> status = new ArrayList<>();
		ISdcCsarHelper fdntCsarHelper;
		ToscaDefinition toscaMainAmdocsDefinition = new ToscaDefinition();
		File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//		filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());

		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, user);
		toscaMainAmdocsDefinition = addGenericPropertiesToToscaDefinitionObject(toscaMainAmdocsDefinition, GENERIC_PNF);
		ToscaDefinition toscaExpectedMainServiceDefinition = new ToscaDefinition(toscaMainAmdocsDefinition);
//--------------------------VF--------------------------------
		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceReqDetails,UserRoleEnum.DESIGNER,true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		ToscaDefinition toscaMainVfDefinition = downloadAndGetToscaMainYamlObjectApi(resource, filesFolder);

//--------------------------SERVICE--------------------------------
		ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
		Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
		ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();

//--------------------------getProperties set values and declare--------------------

		Component componentObject = AtomicOperationUtils.getComponentObject(service, UserRoleEnum.DESIGNER);
		Map<String, List<ComponentInstanceInput>> componentInstancesInputs = componentObject.getComponentInstancesInputs();
		setValuesToPropertiesList(componentInstancesInputs, toscaExpectedMainServiceDefinition);
		PropertyRestUtils.declareProporties(componentObject, componentInstancesInputs, user);

		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		File ServiceCsarFileName = new File(File.separator + "ServiceCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(service, new File(filesFolder.getPath() + ServiceCsarFileName));
		ToscaDefinition toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + ServiceCsarFileName));

//--------------------------initialization of Tosca Parser--------------------------------

		fdntCsarHelper = initSdcCsarHelper(ServiceCsarFileName, filesFolder);


//---------------------------TESTS--------------------------------------------------
		status = validateVfMetadata(toscaMainAmdocsDefinition, toscaMainVfDefinition, resourceReqDetails, resource, vnfFile, status);
		status = validateResourceNodeTemplateMetadata(toscaMainVfDefinition, resource, vnfFile, status);
		status = validateServiceMetadata(toscaMainServiceDefinition, serviceReqDetails, service, vnfFile, status);
		status = validateServiceNodeTemplateMetadata(toscaMainServiceDefinition, componentInstanceDefinition, resourceReqDetails, resource, vnfFile, status);
		status = validateServiceMetadataUsingParser(fdntCsarHelper, serviceReqDetails, service, vnfFile, status);
		status = validateServiceNodeTemplateMetadataUsingParser(fdntCsarHelper, resourceReqDetails, resource, componentInstanceDefinition, vnfFile, status);
		status = validateResourceInputs(toscaMainAmdocsDefinition, toscaMainVfDefinition, vnfFile, status);
		status = validateServiceInputs(toscaExpectedMainServiceDefinition, toscaMainServiceDefinition, vnfFile, status);
		status = validateServiceInputsUsingParser(fdntCsarHelper, toscaExpectedMainServiceDefinition, vnfFile, status);

		if(status.contains(false)){
			SetupCDTest.getExtendTest().log(Status.FAIL, "Summary: tosca validation test failed with zip file " + vnfFile);
			Assert.assertFalse(true);
		}
	}*/

	//	help method to toscaValidation tests
	private ISdcCsarHelper initSdcCsarHelper(File serviceCsarFileName, File filesFolder) {

		ISdcCsarHelper fdntCsarHelper;
		try{
			SetupCDTest.getExtendTest().log(Status.INFO, "Tosca parser is going to convert service csar file to ISdcCsarHelper object...");
			fdntCsarHelper = factory.getSdcCsarHelper(filesFolder.getPath() + serviceCsarFileName);
		}catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.ERROR, "Tosca parser FAILED to convert service csar file to ISdcCsarHelper object...");
			SetupCDTest.getExtendTest().log(Status.FAIL, e);
			fdntCsarHelper = null;
		}
		return fdntCsarHelper;
	}


	/**
	 * @param resourceReqDetails to create Vsp
	 * @return updated resourceReqDetails after Vsp was created
	 */
	private ResourceReqDetails createCustomizedVsp(ResourceReqDetails resourceReqDetails, String filePath, String vnfFile) throws Exception {
		Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, filePath, vnfFile, user);
		VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
		vendorSoftwareProductObject.setName(createVendorSoftwareProduct.left);
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		return resourceReqDetails;
	}

	/**
	 * @param resourceReqDetails to download csar file via UI
	 * @return Tosca definition object from main yaml file
	 */
	private ToscaDefinition downloadAndGetToscaMainYamlObjectUI(ResourceReqDetails resourceReqDetails, File filesFolder) throws Exception {
		DownloadManager.downloadCsarByNameFromVSPRepository(resourceReqDetails.getName(), false);
		File amdocsCsarFileName = FileHandling.getLastModifiedFileNameFromDir(filesFolder.getAbsolutePath());
		return ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(amdocsCsarFileName);
	}

	/**
	 * @param toscaMainAmdocsDefinition object to add generic properties
	 * @param genericName resource name
	 * @return updated toscaMainAmdocsDefinition object
	 */
	private ToscaDefinition addGenericPropertiesToToscaDefinitionObject(ToscaDefinition toscaMainAmdocsDefinition, String genericName) throws Exception {
		toscaMainAmdocsDefinition = setNameToToscaInput(toscaMainAmdocsDefinition);
		toscaMainAmdocsDefinition = addGenericInputsToToscaObject(toscaMainAmdocsDefinition, genericName);
		return toscaMainAmdocsDefinition;
	}

	/**
	 * @param resource to download csar file via API
	 * @return Tosca definition object from main yaml file
	 */
	private ToscaDefinition downloadAndGetToscaMainYamlObjectApi(Resource resource, File filesFolder) throws Exception {
		File VfCsarFileName = new File(File.separator + "VfCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
		OnboardingUtillViaApis.downloadToscaCsarToDirectory(resource, new File(filesFolder.getPath() + VfCsarFileName));
		return ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + VfCsarFileName));
	}

	/*public static void main(String[] args) {
		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
		List<CategoryDefinition> categories = resourceReqDetails.getCategories();
		System.out.println(categories);
	}*/

}


