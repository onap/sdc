/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.onap.sdc.frontend.ci.tests.dataProvider.OnbordingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.tosca.model.ToscaMetadataFieldsPresentationEnum;
import org.onap.sdc.frontend.ci.tests.utilities.DownloadManager;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.CsarParserUtils;
import org.onap.sdc.backend.ci.tests.utils.ToscaParserUtils;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtillViaApis;
import org.onap.sdc.backend.ci.tests.utils.rest.ArtifactRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.PropertyRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.frontend.ci.tests.verificator.ToscaValidation;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.datatypes.GroupHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.TypeHeatMetaDefinition;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaGroupPropertyDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaServiceGroupsMetadataDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.VfModuleDefinition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.testng.Assert.assertFalse;


public class ToscaValidationTest extends SetupCDTest {

    private static final String GENERIC_VF = "Generic_VF";
    private static final String GENERIC_PNF = "Generic_PNF";
    private static final String GENERIC_CR = "Generic_CR";

    protected SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
    User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
    public void toscaFileValidator(String filePath, String vnfFile) throws Exception {
//--------------------------GENERAL--------------------------------
/*//		for debugging only
        setLog("Test");
		File amdocsCsarFileName = (new File("C:\\Users\\al714h\\Downloads\\d218be69637647b0b693647d84a8c03f.csar"));
		toscaMainAmdocsDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(amdocsCsarFileName);
		toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File("C:\\Users\\al714h\\Downloads\\resource-Civfonboarded2016073VmxBv301072E2eE60f5c15-csar.csar"));
	*/
        //      vnfFile = "vRouter for DHV Test_Version_4.zip";
        setLog(vnfFile);
        List<Boolean> status = new ArrayList<>();
        ISdcCsarHelper fdntCsarHelper;
        File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//--------------------------AMDOCS--------------------------------
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, user);//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        resourceReqDetails = createCustomizedVsp(resourceReqDetails, filePath, vnfFile);
        ToscaDefinition toscaMainAmdocsDefinition = downloadAndGetToscaMainYamlObjectUI(resourceReqDetails, filesFolder);
//------adding generic inputs to expected object
        toscaMainAmdocsDefinition = addGenericPropertiesToToscaDefinitionObject(toscaMainAmdocsDefinition, GENERIC_VF);
//	copy object
        ToscaDefinition toscaExpectedMainServiceDefinition = new ToscaDefinition(toscaMainAmdocsDefinition);
//		create list of modules from HEAT.meta file
        File latestFileFromDir = FileHandling.getLastModifiedFileNameFromDir();
        List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition = CsarParserUtils.getListTypeHeatMetaDefinition(latestFileFromDir);
//TODO 	VfModuleVerificator.verifyGroupMetadata();
//TODO--------------------------AMDOCS DOWNLOAD VIA APIS--------------------------------
//--------------------------VF--------------------------------
//		create VF base on VNF imported from previous step - have, resourceReqDetails object include part of resource metadata
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        ToscaDefinition toscaMainVfDefinition = downloadAndGetToscaMainYamlObjectApi(resource, filesFolder);
//--------------------------SERVICE--------------------------------
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();//getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();
//--------------------------getProperties set values and declare--------------------
        Component componentObject = AtomicOperationUtils.getComponentObject(service, UserRoleEnum.DESIGNER);
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = componentObject.getComponentInstancesInputs();
        setValuesToPropertiesList(componentInstancesInputs, toscaExpectedMainServiceDefinition);
        PropertyRestUtils.declareProporties(componentObject, componentInstancesInputs, user);

        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        Map<String, VfModuleDefinition> expectedVfModulesDefinitionObject = createExpectedVfModuleDefinitionObject(resource, service, listTypeHeatMetaDefinition);

        File serviceCsarFileName = new File(File.separator + "ServiceCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
        OnboardingUtillViaApis.downloadToscaCsarToDirectory(service, new File(filesFolder.getPath() + serviceCsarFileName));
        ToscaDefinition toscaMainServiceDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + serviceCsarFileName));
//--------------------------initialization of Tosca Parser--------------------------------
        fdntCsarHelper = initSdcCsarHelper(serviceCsarFileName, filesFolder);
////---------------------------TESTS--------------------------------------------------
        validateVfModuleJsonFile(expectedVfModulesDefinitionObject, service, componentInstanceDefinition.getUniqueId(), vnfFile, status);
        validateVfMetadata(toscaMainAmdocsDefinition, toscaMainVfDefinition, resourceReqDetails, resource, vnfFile, status);
        validateResourceNodeTemplateMetadata(toscaMainVfDefinition, resource, vnfFile, status);
        validateServiceMetadata(toscaMainServiceDefinition, serviceReqDetails, service, vnfFile, status);
        validateServiceNodeTemplateMetadata(toscaMainServiceDefinition, componentInstanceDefinition, resourceReqDetails, resource, vnfFile, status);
        validateServiceMetadataUsingParser(fdntCsarHelper, serviceReqDetails, service, vnfFile, status);
        validateServiceNodeTemplateMetadataUsingParser(fdntCsarHelper, resourceReqDetails, resource, componentInstanceDefinition, vnfFile, status);
        validateResourceInputs(toscaMainAmdocsDefinition, toscaMainVfDefinition, vnfFile, status);
        validateServiceInputs(toscaExpectedMainServiceDefinition, toscaMainServiceDefinition, vnfFile, status);
        validateServiceInputsUsingParser(fdntCsarHelper, toscaExpectedMainServiceDefinition, vnfFile, status);

        Map<String, ToscaGroupsTopologyTemplateDefinition> expectedToscaServiceGroupsDefinitionObject = createExpectedToscaServiceGroupsDefinitionObject(resource, service, listTypeHeatMetaDefinition);
        validateServiceModuleMetadata(expectedToscaServiceGroupsDefinitionObject, toscaMainServiceDefinition, vnfFile, status);
        validateServiceModuleProperty(expectedToscaServiceGroupsDefinitionObject, toscaMainServiceDefinition, vnfFile, status);
        validateServiceModuleMetadataUsingParser(fdntCsarHelper, expectedToscaServiceGroupsDefinitionObject, vnfFile, status);
        validateServiceModulePropertyUsingParser(fdntCsarHelper, expectedToscaServiceGroupsDefinitionObject, vnfFile, status);

        if (status.contains(false)) {
            SetupCDTest.getExtendTest().log(Status.FAIL, "Summary: tosca validation test failed with zip file " + vnfFile);
            assertFalse(true);
        }
    }


    @DataProvider(name = "toscaValidationTest", parallel = true)
    public static Object[][] dataProviderForSpecificResourceType() {
        return new Object[][]{
                {"networkModel", ResourceTypeEnum.PNF, GENERIC_PNF},
                {"collectorResourceModel", ResourceTypeEnum.CR, GENERIC_CR},
        };
    }

    @Test(dataProvider = "toscaValidationTest")
    public void validateSpecificResourceType(String type, ResourceTypeEnum resourceType, String genericPropName) throws Exception {
//--------------------------GENERAL--------------------------------
        String vnfFile = type;
        setLog(vnfFile);
        List<Boolean> status = new ArrayList<>();
        ISdcCsarHelper fdntCsarHelper;
        ToscaDefinition expectedToscaMainDefinition = new ToscaDefinition();
        File filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());
//		filesFolder = new File(SetupCDTest.getWindowTest().getDownloadDirectory());

        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(resourceType, user);
        expectedToscaMainDefinition = addGenericPropertiesToToscaDefinitionObject(expectedToscaMainDefinition, genericPropName);
        ToscaDefinition toscaExpectedMainServiceDefinition = new ToscaDefinition(expectedToscaMainDefinition);
//--------------------------VF--------------------------------
        Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
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
        validateVfMetadata(expectedToscaMainDefinition, toscaMainVfDefinition, resourceReqDetails, resource, vnfFile, status);
        validateResourceNodeTemplateMetadata(toscaMainVfDefinition, resource, vnfFile, status);
        validateServiceMetadata(toscaMainServiceDefinition, serviceReqDetails, service, vnfFile, status);
        validateServiceNodeTemplateMetadata(toscaMainServiceDefinition, componentInstanceDefinition, resourceReqDetails, resource, vnfFile, status);
        validateServiceMetadataUsingParser(fdntCsarHelper, serviceReqDetails, service, vnfFile, status);
        validateServiceNodeTemplateMetadataUsingParser(fdntCsarHelper, resourceReqDetails, resource, componentInstanceDefinition, vnfFile, status);
        validateResourceInputs(expectedToscaMainDefinition, toscaMainVfDefinition, vnfFile, status);
        validateServiceInputs(toscaExpectedMainServiceDefinition, toscaMainServiceDefinition, vnfFile, status);
        validateServiceInputsUsingParser(fdntCsarHelper, toscaExpectedMainServiceDefinition, vnfFile, status);

        if (status.contains(false)) {
            SetupCDTest.getExtendTest().log(Status.FAIL, "Summary: tosca validation test failed with zip file " + vnfFile);
            assertFalse(true);
        }
    }

    /**
     * The method set values to toscaDefinition object service level only, to resource level should put instead of setDefault --> setValue
     * inputs.get(componentInstanceInput.getName()).setValue(randomString);
     *
     * @param componentInstancesInputs list of componentInstancesInputs
     * @param toscaDefinition
     */
    private void setValuesToPropertiesList(Map<String, List<ComponentInstanceInput>> componentInstancesInputs, ToscaDefinition toscaDefinition) {
        for (Map.Entry<String, List<ComponentInstanceInput>> entry : componentInstancesInputs.entrySet()) {
            List<ComponentInstanceInput> value = entry.getValue();
            String[] names = entry.getKey().split("\\.");
            String expectedServiceInputPrefix = null;
            Map<String, ToscaInputsTopologyTemplateDefinition> inputs = toscaDefinition.getTopology_template().getInputs();
            if (names.length > 0) {
                expectedServiceInputPrefix = names[names.length - 1] + "_";
            }
            for (ComponentInstanceInput componentInstanceInput : value) {


                String type = componentInstanceInput.getType();
                List<String> myList = new ArrayList<>();
                myList.add("cbf8049e-69e8-48c3-a06f-255634391403");
                switch (type) {
                    case "string":
                        String randomString = getRandomString();
                        componentInstanceInput.setValue(randomString);
                        inputs.get(componentInstanceInput.getName()).setDefault(randomString);

                        break;
                    case "integer":
                        int randomInteger = getRandomInteger();
                        componentInstanceInput.setValue(Integer.toString(randomInteger));
                        inputs.get(componentInstanceInput.getName()).setDefault(randomInteger);
                        break;
                    case "float":
                        componentInstanceInput.setValue("5.5");
                        inputs.get(componentInstanceInput.getName()).setDefault("5.5");

                        break;
                    case "boolean":
                        componentInstanceInput.setValue("true");
                        inputs.get(componentInstanceInput.getName()).setDefault("true");
                        break;
                    case "list":
                        String myListofStrings = myList.toString();
                        componentInstanceInput.setValue(myListofStrings);
                        inputs.get(componentInstanceInput.getName()).setDefault(myListofStrings);
                        break;
                    case "json":
                        String myJson = "{\"firstParam\":\"my First Param Value\",\"secondParam\":\"my Second Param Value\",\"numberParam\":666}";
                        componentInstanceInput.setValue(myJson);
                        inputs.get(componentInstanceInput.getName()).setDefault(myJson);
                        break;
                    case "comma_delimited_list":
                        String commaDelimitedList = "[\"one\", \"two\"]";
                        componentInstanceInput.setValue(commaDelimitedList);
                        inputs.get(componentInstanceInput.getName()).setDefault(commaDelimitedList);
                        break;
                    default:
                        break;
                }

                String expectedServiceInputName = expectedServiceInputPrefix + componentInstanceInput.getName();
                ToscaInputsTopologyTemplateDefinition oldInput = inputs.get(componentInstanceInput.getName());
                inputs.put(expectedServiceInputName, oldInput);
                inputs.remove(componentInstanceInput.getName());

            }

        }
    }

    protected String getRandomString() {
        final int LengthOfRandomString = 18;
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < LengthOfRandomString) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String strValue = salt.toString();
        return strValue;

    }

    protected int getRandomInteger() {
        final int low = 10;
        final int high = 100;
        Random r = new Random();
        return r.nextInt(high - low) + low;
    }


    //--------------------------Metadata verification--------------------------------
//--------------------------Resource--------------------------------	

    public List<Boolean> validateVfMetadata(ToscaDefinition toscaMainAmdocsDefinition, ToscaDefinition toscaMainVfDefinition, ResourceReqDetails resourceReqDetails, Resource resource, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateVfMetadata", vnfFile);
        //add resource metadata to expected object
        toscaMainAmdocsDefinition = addAndGenerateResourceMetadataToExpectedObject(toscaMainAmdocsDefinition, resourceReqDetails, resource);
        Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator = ToscaValidation.resourceToscaMetadataValidator(toscaMainAmdocsDefinition, toscaMainVfDefinition);
        if (resourceToscaMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    public List<Boolean> validateResourceNodeTemplateMetadata(ToscaDefinition toscaMainVfDefinition, Resource resource, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateResourceNodeTemplateMetadata", vnfFile);
        Map<String, Map<String, String>> generateReosurceNodeTemplateMetadataToExpectedObject = generateResourceNodeTemplateMetadataToExpectedObject(resource);
        Boolean resourceToscaMetadataValidator = ToscaValidation.resourceToscaNodeTemplateMetadataValidator(generateReosurceNodeTemplateMetadataToExpectedObject, toscaMainVfDefinition);
        if (!resourceToscaMetadataValidator) {
            status.add(false);
        }
        return status;
    }

    //--------------------------Service--------------------------------
    public List<Boolean> validateServiceMetadata(ToscaDefinition toscaMainServiceDefinition, ServiceReqDetails serviceReqDetails, Service service, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateServiceMetadata", vnfFile);
        Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
        Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.serviceToscaMetadataValidator(generateServiceMetadataToExpectedObject, toscaMainServiceDefinition);
        if (serviceToscaMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }


    public List<Boolean> validateServiceNodeTemplateMetadata(ToscaDefinition toscaMainServiceDefinition, ComponentInstance componentInstanceDefinition, ResourceReqDetails resourceReqDetails, Resource resource, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateServiceNodeTemplateMetadata", vnfFile);
        Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
        Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator = ToscaValidation.componentToscaNodeTemplateMetadataValidator(generateServiceNodeTemplateMetadataToExpectedObject, toscaMainServiceDefinition, componentInstanceDefinition.getName(), ComponentTypeEnum.SERVICE, componentInstanceDefinition.getName());
        if (serviceToscaMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    private List<Boolean> validateVfModuleJsonFile(Map<String, VfModuleDefinition> expectedVfModulesDefinitionObject, Service service, String resInstUniqueId, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateVfModuleJsonFile", vnfFile);
        String artifactUniqueId = null;
        for (ComponentInstance inst : service.getComponentInstances()) {
            if (inst.getUniqueId().equals(resInstUniqueId)) {
                artifactUniqueId = inst.getDeploymentArtifacts().get("vfModulesMetadata").getUniqueId();
                break;
            }
        }
        if (artifactUniqueId == null) {
            SetupCDTest.getExtendTest().log(Status.ERROR, "validateVfModuleJsonFile verification failed, artifact vfModulesMetadata not found");
            status.add(false);
            return status;
        }
        RestResponse restResponse = ArtifactRestUtils.downloadResourceInstanceArtifact(service.getUniqueId(), resInstUniqueId, user, artifactUniqueId);
        String artifactPayload = ArtifactRestUtils.getDecodedArtifactPayloadFromResponse(restResponse);
        Map<String, VfModuleDefinition> actualVfModulesDefinitionObject = ResponseParser.convertVfModuleJsonResponseToJavaObject(artifactPayload);
        Either<Boolean, Map<String, Object>> vfModuleJsonFileValidator = ToscaValidation.vfModuleJsonFileValidator(expectedVfModulesDefinitionObject, actualVfModulesDefinitionObject);
        if (vfModuleJsonFileValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    //--------------------------Service verification against Pavel Parser--------------------------------
    public List<Boolean> validateServiceMetadataUsingParser(ISdcCsarHelper fdntCsarHelper, ServiceReqDetails serviceReqDetails, Service service, String vnfFile, List<Boolean> status) throws Exception {
        if (fdntCsarHelper == null) {
            reportSkipTestPrint("validateServiceMetadataUsingParser", status);
        } else {
            reportStartTestPrint("validateServiceMetadataUsingParser", vnfFile);
            Map<String, String> generateServiceMetadataToExpectedObject = generateServiceMetadataToExpectedObject(serviceReqDetails, service);
            Metadata serviceMetadata = fdntCsarHelper.getServiceMetadata();
            Either<Boolean, Map<String, Object>> serviceToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceMetadataToExpectedObject, serviceMetadata);
            if (serviceToscaMetadataValidatorAgainstParser.isRight()) {
                status.add(false);
            }
        }
        return status;
    }

    public List<Boolean> validateServiceNodeTemplateMetadataUsingParser(ISdcCsarHelper fdntCsarHelper, ResourceReqDetails resourceReqDetails, Resource resource, ComponentInstance componentInstanceDefinition, String vnfFile, List<Boolean> status) throws Exception {
        if (fdntCsarHelper == null) {
            reportSkipTestPrint("validateServiceNodeTemplateMetadataUsingParser", status);
        } else {
            reportStartTestPrint("validateServiceNodeTemplateMetadataUsingParser", vnfFile);
            Map<String, String> generateServiceNodeTemplateMetadataToExpectedObject = generateServiceNodeTemplateMetadataToExpectedObject(resourceReqDetails, resource, componentInstanceDefinition);
            List<NodeTemplate> serviceNodeTemplates = fdntCsarHelper.getServiceNodeTemplates();
            Metadata serviceNodeTemplateMetadata = serviceNodeTemplates.get(0).getMetadata();
            Either<Boolean, Map<String, Object>> serviceNodeTemplateToscaMetadataValidatorAgainstParser = ToscaValidation.serviceToscaMetadataValidatorAgainstParser(generateServiceNodeTemplateMetadataToExpectedObject, serviceNodeTemplateMetadata);
            if (serviceNodeTemplateToscaMetadataValidatorAgainstParser.isRight()) {
                status.add(false);
            }
        }
        return status;
    }

    //--------------------------Input verification--------------------------------

    //--------------------------Resource--------------------------------
    public List<Boolean> validateResourceInputs(ToscaDefinition toscaMainAmdocsDefinition, ToscaDefinition toscaMainVfDefinition, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateResourceInputs", vnfFile);
        Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaMainAmdocsDefinition.getTopology_template().getInputs();
        Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = toscaMainVfDefinition.getTopology_template().getInputs();
        Either<Boolean, Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidator(expectedInputsMap, actualInputsMap);
        if (toscaInputsValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    //--------------------------Service--------------------------------

    public List<Boolean> validateServiceInputs(ToscaDefinition toscaExpectedMainServiceDefinition, ToscaDefinition toscaMainServiceDefinition, String vnfFile, List<Boolean> status) throws Exception {
        reportStartTestPrint("validateServiceInputs", vnfFile);
        Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaExpectedMainServiceDefinition.getTopology_template().getInputs();
        Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = toscaMainServiceDefinition.getTopology_template().getInputs();
        Either<Boolean, Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidator(expectedInputsMap, actualInputsMap);
        if (toscaInputsValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    public List<Boolean> validateServiceModuleMetadata(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedToscaServiceGroupsDefinitionObject, ToscaDefinition toscaMainServiceDefinition, String vnfFile, List<Boolean> status) {
        reportStartTestPrint("validateServiceModuleMetadata", vnfFile);

        Either<Boolean, Map<String, Object>> toscaServiceModuleMetadataValidator = ToscaValidation.serviceToscaGroupMetadataValidator(expectedToscaServiceGroupsDefinitionObject, toscaMainServiceDefinition);
        if (toscaServiceModuleMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    public List<Boolean> validateServiceModuleProperty(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedToscaServiceGroupsDefinitionObject, ToscaDefinition toscaMainServiceDefinition, String vnfFile, List<Boolean> status) {
        reportStartTestPrint("validateServiceModuleProperty", vnfFile);

        Either<Boolean, Map<String, Object>> toscaServiceModulePropertyValidator = ToscaValidation.serviceToscaGroupPropertyValidator(expectedToscaServiceGroupsDefinitionObject, toscaMainServiceDefinition);
        if (toscaServiceModulePropertyValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    //--------------------------Service verification against Pavel Parser--------------------------------
    public List<Boolean> validateServiceInputsUsingParser(ISdcCsarHelper fdntCsarHelper, ToscaDefinition toscaExpectedMainServiceDefinition, String vnfFile, List<Boolean> status) throws Exception {
        if (fdntCsarHelper == null) {
            reportSkipTestPrint("validateServiceInputsUsingParser", status);
        } else {
            reportStartTestPrint("validateServiceInputsUsingParser", vnfFile);
            Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap = toscaExpectedMainServiceDefinition.getTopology_template().getInputs();
            Either<Boolean, Map<String, Object>> toscaInputsValidator = ToscaValidation.toscaInputsValidatorAgainstParser(expectedInputsMap, fdntCsarHelper);
            if (toscaInputsValidator.isRight()) {
                status.add(false);
            }
        }
        return status;
    }

    public List<Boolean> validateServiceModuleMetadataUsingParser(ISdcCsarHelper fdntCsarHelper, Map<String, ToscaGroupsTopologyTemplateDefinition> expectedToscaServiceGroupsDefinitionObject, String vnfFile, List<Boolean> status) {
        reportStartTestPrint("validateServiceModuleMetadataUsingParser", vnfFile);
        String customizationUUID = fdntCsarHelper.getServiceNodeTemplates().get(0).getMetadata().getValue("customizationUUID");
        List<Group> actualGroups = fdntCsarHelper.getVfModulesByVf(customizationUUID);
        Either<Boolean, Map<String, Object>> toscaServiceModuleMetadataValidator = ToscaValidation.serviceToscaGroupMetadataValidatorUsingParser(expectedToscaServiceGroupsDefinitionObject, actualGroups);
        if (toscaServiceModuleMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    public List<Boolean> validateServiceModulePropertyUsingParser(ISdcCsarHelper fdntCsarHelper, Map<String, ToscaGroupsTopologyTemplateDefinition> expectedToscaServiceGroupsDefinitionObject, String vnfFile, List<Boolean> status) {
        reportStartTestPrint("validateServiceModuleMetadataUsingParser", vnfFile);
        String customizationUUID = fdntCsarHelper.getServiceNodeTemplates().get(0).getMetadata().getValue("customizationUUID");
        List<Group> actualGroups = fdntCsarHelper.getVfModulesByVf(customizationUUID);
        Either<Boolean, Map<String, Object>> toscaServiceModuleMetadataValidator = ToscaValidation.serviceToscaGroupPropertyValidatorUsingParser(expectedToscaServiceGroupsDefinitionObject, actualGroups);
        if (toscaServiceModuleMetadataValidator.isRight()) {
            status.add(false);
        }
        return status;
    }

    private Map<String, ToscaGroupsTopologyTemplateDefinition> createExpectedToscaServiceGroupsDefinitionObject(Resource resource, Service service, List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition) {
        Map<String, ToscaGroupsTopologyTemplateDefinition> toscaGroupsTopologyTemplateDefinitionMap = new HashMap<>();

        for (TypeHeatMetaDefinition moduleType : listTypeHeatMetaDefinition) {
            if (!moduleType.getTypeName().equals("artifacts")) {
                for (GroupHeatMetaDefinition module : moduleType.getGroupHeatMetaDefinition()) {
                    ToscaGroupsTopologyTemplateDefinition toscaGroupsTopologyTemplateDefinition = new ToscaGroupsTopologyTemplateDefinition();
                    String resourceModuleName = buildResourceModuleName(resource, module.getGroupName());
                    ToscaServiceGroupsMetadataDefinition toscaServiceGroupsMetadataDefinition = setGroupMetadataFromResourceObject(resourceModuleName, resource);
                    if (!toscaServiceGroupsMetadataDefinition.equals("")) {
                        String serviceModuleName = buildServiceModuleName(service.getComponentInstances().get(0).getNormalizedName(), toscaServiceGroupsMetadataDefinition.getVfModuleModelName());
                        toscaServiceGroupsMetadataDefinition = setGroupMetadataFromServiceObject(toscaServiceGroupsMetadataDefinition, serviceModuleName, service);
                        toscaGroupsTopologyTemplateDefinition.setMetadata(toscaServiceGroupsMetadataDefinition);
                        ToscaGroupPropertyDefinition toscaGroupPropertyDefinition = setGroupProperty(module);
                        toscaGroupsTopologyTemplateDefinition.setProperties(toscaGroupPropertyDefinition);
                        toscaGroupsTopologyTemplateDefinitionMap.put(serviceModuleName, toscaGroupsTopologyTemplateDefinition);
                    } else {
                        getExtendTest().log(Status.FAIL, "module name [" + module.getGroupName() + "] didn't represent in resource");
                    }
                }
            }
        }
        return toscaGroupsTopologyTemplateDefinitionMap;

    }


    private Map<String, VfModuleDefinition> createExpectedVfModuleDefinitionObject(Resource resource, Service service, List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition) {
        Map<String, VfModuleDefinition> toscaGroupsTopologyTemplateDefinitionMap = new HashMap<>();

        for (TypeHeatMetaDefinition moduleType : listTypeHeatMetaDefinition) {
            if (!moduleType.getTypeName().equals("artifacts")) {
                for (GroupHeatMetaDefinition module : moduleType.getGroupHeatMetaDefinition()) {
                    VfModuleDefinition toscaGroupsTopologyTemplateDefinition = new VfModuleDefinition();
                    String resourceModuleName = buildResourceModuleName(resource, module.getGroupName());
                    ToscaServiceGroupsMetadataDefinition toscaServiceGroupsMetadataDefinition = setGroupMetadataFromResourceObject(resourceModuleName, resource);
                    if (!toscaServiceGroupsMetadataDefinition.equals("")) {
                        toscaServiceGroupsMetadataDefinition = setGroupMetadataFromServiceObject(toscaServiceGroupsMetadataDefinition, resourceModuleName, service);
                        toscaGroupsTopologyTemplateDefinition.setMetadata(toscaServiceGroupsMetadataDefinition);
                        ToscaGroupPropertyDefinition toscaGroupPropertyDefinition = setGroupProperty(module);
                        toscaGroupsTopologyTemplateDefinition.setProperties(toscaGroupPropertyDefinition);
                        toscaGroupsTopologyTemplateDefinition.setArtifacts(getArtifactsUuidListForRI(module, resource, service));
                        toscaGroupsTopologyTemplateDefinitionMap.put(toscaServiceGroupsMetadataDefinition.vfModuleModelName, toscaGroupsTopologyTemplateDefinition);

                    } else {
                        getExtendTest().log(Status.FAIL, "module name [" + module.getGroupName() + "] didn't represent in resource");
                    }
                }
            }
        }
        return toscaGroupsTopologyTemplateDefinitionMap;

    }

    private List<String> getArtifactsUuidListForRI(GroupHeatMetaDefinition module, Resource resource, Service service) {
//TODO check if each heat include its env file
        List<String> artifactsUuidList = new ArrayList<>();
        ComponentInstance resourceInstance = null;
        for (ComponentInstance componentInstance : service.getComponentInstances()) {
            if (componentInstance.getUniqueId().contains(resource.getUniqueId())) {
                resourceInstance = componentInstance;
                break;
            }
        }

        Collection<ArtifactDefinition> artifactsDefinitionValues = resourceInstance.getDeploymentArtifacts().values();

        for (HeatMetaFirstLevelDefinition moduleArtifactDefinition : module.getArtifactList()) {
            for (ArtifactDefinition riArtifactDefinition : artifactsDefinitionValues) {
                String heatYamlName = moduleArtifactDefinition.getFileName();
                String heatEnvName = heatYamlName.replaceAll("yaml|yml", "env");
                String riArtifactName = riArtifactDefinition.getArtifactName();
                if (riArtifactName.equals(heatYamlName) || riArtifactName.equals(heatEnvName)) {
                    artifactsUuidList.add(riArtifactDefinition.getArtifactUUID());
                }
            }
        }

        return artifactsUuidList;
    }

    private ToscaGroupPropertyDefinition setGroupProperty(GroupHeatMetaDefinition module) {
        ToscaGroupPropertyDefinition toscaGroupPropertyDefinition = new ToscaGroupPropertyDefinition();
        toscaGroupPropertyDefinition.setVf_module_label(module.getGroupName());
        Boolean isBase = module.getPropertyHeatMetaDefinition().getValue();
        if (isBase) {
            toscaGroupPropertyDefinition.setInitial_count("1");
            toscaGroupPropertyDefinition.setMin_vf_module_instances("1");
            toscaGroupPropertyDefinition.setMax_vf_module_instances("1");
            toscaGroupPropertyDefinition.setVf_module_type("Base");
        } else {
            toscaGroupPropertyDefinition.setInitial_count("0");
            toscaGroupPropertyDefinition.setMin_vf_module_instances("0");
            toscaGroupPropertyDefinition.setMax_vf_module_instances("");
            toscaGroupPropertyDefinition.setVf_module_type("Expansion");
        }
        toscaGroupPropertyDefinition.setAvailability_zone_count("");
        toscaGroupPropertyDefinition.setVfc_list("");
        toscaGroupPropertyDefinition.setVf_module_description("");
        toscaGroupPropertyDefinition.setVolume_group(isVolumeGroup(module));

        return toscaGroupPropertyDefinition;
    }

    private String isVolumeGroup(GroupHeatMetaDefinition module) {
        String isVolumeGroup = "false";
        for (HeatMetaFirstLevelDefinition artifactList : module.getArtifactList()) {
            if (artifactList.getType().equals(ArtifactTypeEnum.HEAT_VOL.getType())) {
                isVolumeGroup = "true";
                return isVolumeGroup;
            }
        }
        return isVolumeGroup;
    }

    private Map<String, ToscaServiceGroupsMetadataDefinition> createExpectedToscaServiceGroupsPropertyDefinitionObject(Resource resource, Service service, List<TypeHeatMetaDefinition> listTypeHeatMetaDefinition) {

        Map<String, ToscaServiceGroupsMetadataDefinition> toscaServiceGroupsMetadataDefinitionMap = new HashMap<>();
        for (TypeHeatMetaDefinition moduleType : listTypeHeatMetaDefinition) {
            Map<String, String> groupProperty = new HashMap<>();

            ToscaServiceGroupsMetadataDefinition toscaServiceGroupsMetadataDefinition = new ToscaServiceGroupsMetadataDefinition();
            for (GroupHeatMetaDefinition module : moduleType.getGroupHeatMetaDefinition()) {
                String resourceModuleName = buildResourceModuleName(resource, module.getGroupName());
                toscaServiceGroupsMetadataDefinition = setGroupMetadataFromResourceObject(resourceModuleName, resource);
                if (!toscaServiceGroupsMetadataDefinition.equals("")) {
                    String serviceModuleName = buildServiceModuleName(service.getComponentInstances().get(0).getNormalizedName(), toscaServiceGroupsMetadataDefinition.getVfModuleModelName());
                    toscaServiceGroupsMetadataDefinition = setGroupMetadataFromServiceObject(toscaServiceGroupsMetadataDefinition, serviceModuleName, service);
                    toscaServiceGroupsMetadataDefinitionMap.put(serviceModuleName, toscaServiceGroupsMetadataDefinition);
                } else {
                    getExtendTest().log(Status.FAIL, "module name [" + module.getGroupName() + "] didn't represent in resource");
                }
            }
        }
        return toscaServiceGroupsMetadataDefinitionMap;

    }

    private ToscaServiceGroupsMetadataDefinition setGroupMetadataFromServiceObject(ToscaServiceGroupsMetadataDefinition toscaServiceGroupsMetadataDefinition, String serviceModuleName, Service service) {
        for (GroupInstance groupInstance : service.getComponentInstances().get(0).getGroupInstances()) {
            if (groupInstance.getName().contains(serviceModuleName)) {
                toscaServiceGroupsMetadataDefinition.setVfModuleModelCustomizationUUID(groupInstance.getCustomizationUUID());
                return toscaServiceGroupsMetadataDefinition;
            }
        }
        return toscaServiceGroupsMetadataDefinition;
    }

    private ToscaServiceGroupsMetadataDefinition setGroupMetadataFromResourceObject(String resourceModuleName, Resource resource) {
        ToscaServiceGroupsMetadataDefinition toscaServiceGroupsMetadataDefinition = new ToscaServiceGroupsMetadataDefinition();
        for (GroupDefinition group : resource.getGroups()) {
            if (group.getName().contains(resourceModuleName)) {
                toscaServiceGroupsMetadataDefinition.setVfModuleModelName(group.getName());
                toscaServiceGroupsMetadataDefinition.setVfModuleModelInvariantUUID(group.getInvariantUUID());
                toscaServiceGroupsMetadataDefinition.setVfModuleModelUUID(group.getGroupUUID());
                toscaServiceGroupsMetadataDefinition.setVfModuleModelVersion(group.getVersion());
                return toscaServiceGroupsMetadataDefinition;
            }
        }
        return toscaServiceGroupsMetadataDefinition;
    }

    public static String buildResourceModuleName(Resource resource, String groupName) {
        return resource.getSystemName() + ".." + groupName + ".." + "module-";
    }

    public static String buildServiceModuleName(String resourceInstanceNormalizedName, String resourceGroupName) {
        return resourceInstanceNormalizedName + ".." + resourceGroupName;
    }

    public static ToscaDefinition addGenericInputsToToscaObject(ToscaDefinition toscaDefinition, String genericName) throws Exception {
        Resource genericResource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, genericName, "1.0");
        ToscaTopologyTemplateDefinition topologyTemplate = toscaDefinition.getTopology_template();
        Map<String, ToscaInputsTopologyTemplateDefinition> newInput = new HashMap<>();
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
            newInput.put(property.getName(), input);
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

    public static Map<String, String> convertResourceNodeTemplateMetadataToMap(ComponentInstance componentInstance) throws Exception {

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
        if (component.getComponentInstances() != null && !component.getComponentInstances().isEmpty()) {
            for (ComponentInstance componentInstance : component.getComponentInstances()) {
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

    public static void reportStartTestPrint(String testName, String vnfFile) {
//		reportMessageInColor("info", "blue", "Running test \" + testName + \" with zip file - \" + vnfFile");
        SetupCDTest.getExtendTest().log(Status.INFO, "<html><font color=\"blue\"> Running test " + testName + " with zip file - " + vnfFile + "</font></html>");
    }

    public static void reportSkipTestPrint(String testName, List<Boolean> status) {
//		reportMessageInColor("error", "orange", "Skip test \" + testName + \" due to previous tosca parser error");
        SetupCDTest.getExtendTest().log(Status.ERROR, "<html><font color=\"orange\"> Skip test " + testName + " due to previous tosca parser error" + "</font></html>");
        status.add(false);
    }

    public static void reportMessageInColor(String status, String color, String message) {
        String printLine = getReportMessageInColor(color, message);
        SetupCDTest.getExtendTest().log(Status.valueOf(status), printLine);
//		SetupCDTest.getExtendTest().log(Status.valueOf(status), getReportMessageInColor(color, message));
    }

    /**
     * @param color   = red, green, orange, blue ...
     * @param message - message string
     * @return string in desired color
     */
    public static String getReportMessageInColor(String color, String message) {
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
	}*/


    //	help method to toscaValidation tests
    private ISdcCsarHelper initSdcCsarHelper(File serviceCsarFileName, File filesFolder) {

        ISdcCsarHelper fdntCsarHelper;
        try {
            SetupCDTest.getExtendTest().log(Status.INFO, "Tosca parser is going to convert service csar file to ISdcCsarHelper object...");
            fdntCsarHelper = factory.getSdcCsarHelper(filesFolder.getPath() + serviceCsarFileName);
        } catch (Exception e) {
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
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, filePath, vnfFile, user);
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
     * @param genericName               resource name
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
        File vfCsarFileName = new File(File.separator + "VfCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
        OnboardingUtillViaApis.downloadToscaCsarToDirectory(resource, new File(filesFolder.getPath() + vfCsarFileName));
        return ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + vfCsarFileName));
    }

}
