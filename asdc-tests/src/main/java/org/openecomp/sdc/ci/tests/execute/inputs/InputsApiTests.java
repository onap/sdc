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

package org.openecomp.sdc.ci.tests.execute.inputs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.InputsRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

/**
 * CI-Tests for inputs 
 * @author il0695
 *
 */
public class InputsApiTests extends ComponentBaseTest {

	private static String inputCsar1 = "FCGI_with_inputs.csar";
	private static String inputCsar2 = "LDSA1_with_inputs.csar";
	private static User  sdncDesignerDetails = null;
	
	@Rule
	public static TestName name = new TestName();
	
	/**
	 * Constructor
	 */
	public InputsApiTests() {
		super(name, InputsApiTests.class.getName());
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	}
	
	/**
	 * Create VF with inputs from CSAR file
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testCreateResourceInstanceWithInputsFromCsar() throws Exception {
		Resource vf = AtomicOperationUtils.importResourceFromCSAR(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, inputCsar1);
		assertTrue("Success creating VF from CSAR", !vf.getInputs().isEmpty());
	}
	
	/**
	 * Create service and add to it VF instance with inputs
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testAddVfInstanceWithInputsToService() throws Exception {
		createServiceWithVFInstanceWithInputs();
	}
	
	/**
	 * General test to check most functionality of inputs
	 * <ul>
	 * 	<li>Create service with VF instance that has inputs)</li>
	 * 	<li>Get all inputs of VF instance</li>
	 * 	<li>Add inputs to service</li>
	 * 	<li>Get service inputs</li>
	 * 	<li>Delete service inputs</li>
	 * </ul>
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testInputsMainFunctionality() throws Exception {
		Service service = createServiceWithVFInstanceWithInputs();
		int totalInputsBeforeAdd = service.getInputs().size();
		
		// Get component instances
		RestResponse getInstancesResponse = ComponentInstanceRestUtils.getComponentInstances(ComponentTypeEnum.SERVICE, service.getUniqueId(), sdncDesignerDetails);
		BaseValidationUtils.checkSuccess(getInstancesResponse);
		List<ComponentInstance> serviceInstances = new Gson().fromJson(getInstancesResponse.getResponse(), new TypeToken<ArrayList<ComponentInstance>>(){}.getType());
		
		// Get all inputs of first instance
		ComponentInstance vfInstance = serviceInstances.get(0);
		RestResponse getComponentInstanceInputsResponse = InputsRestUtils.getComponentInstanceInputs(service, vfInstance);
		BaseValidationUtils.checkSuccess(getComponentInstanceInputsResponse);
		List<ComponentInstancePropInput> instanceInputs = new Gson().fromJson(getComponentInstanceInputsResponse.getResponse(), new TypeToken<ArrayList<ComponentInstancePropInput>>(){}.getType());
		
		// Take only the 2 first inputs
		List<ComponentInstancePropInput> inputsToAdd = instanceInputs.stream().limit(2).collect(Collectors.toList());
		
		// Build component instances input map to add to server
		ComponentInstInputsMap buildComponentInstInputsMap = buildComponentInstInputsMap(vfInstance.getUniqueId(), inputsToAdd);
		RestResponse addInputResponse = InputsRestUtils.addInput(service, buildComponentInstInputsMap, UserRoleEnum.DESIGNER);
		BaseValidationUtils.checkSuccess(addInputResponse);
		
		// Get service inputs count
		RestResponse getComponentInputsResponse = InputsRestUtils.getComponentInputs(service);
		BaseValidationUtils.checkSuccess(getComponentInputsResponse);
		List<InputDefinition> serviceInputsAfterAdd = new Gson().fromJson(getComponentInputsResponse.getResponse(), new TypeToken<ArrayList<InputDefinition>>(){}.getType());
		if (serviceInputsAfterAdd.size()-totalInputsBeforeAdd!=2) {
			assertTrue("Error adding inputs to service (service should have 2 inputs)", false);
		}
		
		// Delete 1 input from service
		RestResponse deleteInputFromComponentResponse = InputsRestUtils.deleteInputFromComponent(service, serviceInputsAfterAdd.get(0).getUniqueId());
		BaseValidationUtils.checkSuccess(deleteInputFromComponentResponse);
		
		// Get service inputs count after delete
		RestResponse getComponentInputsResponseAfterDelete = InputsRestUtils.getComponentInputs(service);
		BaseValidationUtils.checkSuccess(getComponentInputsResponseAfterDelete);
		List<InputDefinition> serviceInputsAfterDelete = new Gson().fromJson(getComponentInputsResponseAfterDelete.getResponse(), new TypeToken<ArrayList<InputDefinition>>(){}.getType());
		if (serviceInputsAfterDelete.size()-totalInputsBeforeAdd!=1) {
			assertTrue("Error deleting inputs from service (service should have 1 input)", false);
		}
		
		assertTrue("Success testing inputs main functionality", true);
	}

	/**
	 * Test to check deletion of inputs related to CP/VL property values 
	 * @throws Exception 
	 */
	@Test
	public void testDeleteInputsRelatedToPropertyValues() throws Exception {
		Service service = createServiceWithVLinstance();
		User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String simplePropName = "network_technology";
		String complexPropName = "network_assignments";
		String propertiesName = "network_assignments#ipv6_subnet_default_assignment#ip_network_address_plan";
		String vlInstanceId = service.getComponentInstances().get(0).getUniqueId();
		
		// Take a simple property type and a complex property type 
		List<ComponentInstanceProperty> inputsToAdd = service.getComponentInstancesProperties().get(vlInstanceId)
				.stream()
				.filter(p -> simplePropName.equals(p.getName()) || complexPropName.equals(p.getName()))
				.collect(Collectors.toList());
		
		List<ComponentInstancePropInput> propInputList = new ArrayList<>();
		for (ComponentInstanceProperty prop : inputsToAdd) {
			if (simplePropName.equals(prop.getName())) {
				propInputList.add(new ComponentInstancePropInput(prop));
			} else {
				propInputList.add(buildComponentInstNetworkAssignmentIpv6AssignmentComplexPropertyInput(prop, propertiesName));
			}
		}
		// Set component instance property input map and add to inputs service
		ComponentInstInputsMap componentInstInputsMap = buildComponentInstPropertyInputsMap(vlInstanceId, propInputList);
		RestResponse addInputResponse = InputsRestUtils.addInput(service, componentInstInputsMap, UserRoleEnum.DESIGNER);
		BaseValidationUtils.checkSuccess(addInputResponse);
		
		// Get the updated service
		ServiceReqDetails serviceDetails = new ServiceReqDetails(service);
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, user);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		
		// validate instance get_input property values in service and tosca template
		ComponentInstance vlInstance = service.getComponentInstances().get(0);	
		String simplePropValue = "{\"get_input\":\"" + vlInstance.getNormalizedName() + "_" + simplePropName + "\"}";
		String complexPropValue = "{\"ipv6_subnet_default_assignment\":{\"ip_network_address_plan\":{\"get_input\":\"" + vlInstance.getNormalizedName() + "_" + propertiesName.replaceAll("#", "_") + "\"}}}";
		// download and compare
		Map<String, Object> load = downloadAndParseServiceToscaTemplate(user, service);
		validateGetInputInstancePropertyValues(load, service, simplePropName, simplePropValue, complexPropName, complexPropValue, vlInstance);
		
		// Delete inputs from service
		RestResponse deleteInputResponse = InputsRestUtils.deleteInputFromComponent(service, service.getInputs().get(0).getUniqueId());
		BaseValidationUtils.checkSuccess(deleteInputResponse);
		deleteInputResponse = InputsRestUtils.deleteInputFromComponent(service, service.getInputs().get(1).getUniqueId());
		BaseValidationUtils.checkSuccess(deleteInputResponse);
		
		// Get the updated service
		getServiceResponse = ServiceRestUtils.getService(serviceDetails, user);
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		
		// download and compare (get_input property values removed)
		load = downloadAndParseServiceToscaTemplate(user, service);
		simplePropValue = "{}";
		complexPropValue = "{\"ipv6_subnet_default_assignment\":{\"ip_network_address_plan\":{}}}"; 
		validateGetInputInstancePropertyValues(load, service, simplePropName, simplePropValue, complexPropName, complexPropValue, vlInstance);
	
	}
	
	/**
	 * Private method to create service with VF instance that has inputs
	 * This is private method to be used by multiple tests
	 * 
	 * @return {@link org.openecomp.sdc.be.model}
	 * @throws Exception
	 * @throws IOException
	 */
	private Service createServiceWithVFInstanceWithInputs() throws Exception, IOException {
		// Create default service
		Either<Service, RestResponse> createDefaultServiceEither = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		if (createDefaultServiceEither.isRight()){
			assertTrue("Error creating default service", false);
		}
		Service service = createDefaultServiceEither.left().value();
		
		// Create VF from CSAR file
		Resource vfWithInputs = AtomicOperationUtils.importResourceFromCSAR(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, inputCsar2);

		// Certify VF
		Pair<Component, RestResponse> changeComponentState = AtomicOperationUtils.changeComponentState(vfWithInputs, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		assertTrue("response code is BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + changeComponentState.getRight().getErrorCode(), changeComponentState.getRight().getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		
		// Add VF instance to service
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainerEither = AtomicOperationUtils.addComponentInstanceToComponentContainer(vfWithInputs, service, UserRoleEnum.DESIGNER, true);
		if (addComponentInstanceToComponentContainerEither.isRight()){
			assertTrue("Error adding VF to service", false);
		}
				
		// Get service response
		ServiceReqDetails serviceDetails = new ServiceReqDetails(service);
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		
		// Get VF instance from service
		ComponentInstance vfInstance = service.getComponentInstances().get(0);
		if (vfInstance!=null){
			assertTrue("Success creating service with VF instance", true);
		} else {
			assertTrue("Error creating service with VF instance", false);
		}
		return service;
	}
	
	
	private Service createServiceWithVLinstance() throws Exception, IOException {
		// Create default service
		Either<Service, RestResponse> createDefaultServiceEither = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		if (createDefaultServiceEither.isRight()){
			assertTrue("Error creating default service", false);
		}
		Service service = createDefaultServiceEither.left().value();
		
		Resource vl = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, "ExtVL", "1.0");
		
		// add VL instance to service  
		Either<ComponentInstance, RestResponse> vlInstDetails = AtomicOperationUtils.addComponentInstanceToComponentContainer(vl, service, UserRoleEnum.DESIGNER, true);
		if (vlInstDetails.isRight()){
			assertTrue("Error adding VF to service", false);
		}
				
		// Get service response
		ServiceReqDetails serviceDetails = new ServiceReqDetails(service);
		RestResponse getServiceResponse = ServiceRestUtils.getService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		service = ResponseParser.parseToObjectUsingMapper(getServiceResponse.getResponse(), Service.class);
		
		// Get instances from service
		ComponentInstance instance = service.getComponentInstances().get(0);
		if (instance != null){
			assertTrue("Success creating service with VF instance", true);
		} else {
			assertTrue("Error creating service with VF instance", false);
		}
		
		return service;
	}
	
	/**	
	 * Return default ComponentInstInputsMap
	 * 
	 * @param addToInput
	 * @param inputs
	 * @return {@link org.openecomp.sdc.be.model.ComponentInstInputsMap}
	 */
	private ComponentInstInputsMap buildComponentInstInputsMap (String addToInput, List<ComponentInstancePropInput> inputs) {
		Map<String, List<ComponentInstancePropInput>> map = new HashMap<>();
		map.put(addToInput, inputs);
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setComponentInstanceInputsMap(map);		
		return componentInstInputsMap;
	}
	
	private ComponentInstInputsMap buildComponentInstPropertyInputsMap (String instanceId, List<ComponentInstancePropInput> props) {
		Map<String, List<ComponentInstancePropInput>> map = new HashMap<>();
		map.put(instanceId, props);
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setComponentInstancePropInput(map);		
		return componentInstInputsMap;
	}
	 	
	
	private ComponentInstancePropInput buildComponentInstNetworkAssignmentIpv6AssignmentComplexPropertyInput (ComponentInstanceProperty prop, String propertiesName) {
		ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput(prop);
		componentInstancePropInput.setPropertiesName(propertiesName);
		PropertyDefinition input = new PropertyDefinition();
		input.setUniqueId("org.openecomp.datatypes.network.SubnetAssignments.datatype.ip_network_address_plan");
		input.setName("ip_network_address_plan");
		input.setParentUniqueId("org.openecomp.datatypes.network.SubnetAssignments.datatype");
		input.setType("string");
		componentInstancePropInput.setInput(input);
		return componentInstancePropInput;
	
	}
	
	private Map<String, Object> downloadAndParseServiceToscaTemplate(User user, Service service) throws Exception {
		String artifactUniqeId = service.getToscaArtifacts().get("assettoscatemplate").getUniqueId();
		RestResponse toscaTemplate = ArtifactRestUtils.downloadServiceArtifactInternalApi(service.getUniqueId(), user, artifactUniqeId);	
		BaseRestUtils.checkSuccess(toscaTemplate);
		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(toscaTemplate.getResponse(), ArtifactUiDownloadData.class);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		byte[] decodeBase64 = Base64.decodeBase64(fromUiDownload);
		Yaml yaml = new Yaml();
		InputStream inputStream = new ByteArrayInputStream(decodeBase64);
		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		return load;
	}
	
	private void validateGetInputInstancePropertyValues (Map<String, Object> load, Service service, String simplePropName, String simplePropValue, String complexPropName, String complexPropValue, ComponentInstance instance) {
	
		String instanceName = instance.getName();
		String instanceId = instance.getUniqueId();
		ComponentInstanceProperty simpleProp = service.getComponentInstancesProperties().get(instanceId).stream().filter(p -> p.getName().equals(simplePropName)).findAny().get();
		ComponentInstanceProperty complexProp = service.getComponentInstancesProperties().get(instanceId).stream().filter(p -> p.getName().equals(complexPropName)).findAny().get();

		assertEquals(simpleProp.getValue(), simplePropValue);
		assertEquals(complexProp.getValue(), complexPropValue);
		// compare with downloaded tosca template  
		assertNotNull(load);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		assertNotNull(topology_template);
		Map<String, Object> nodes = (Map<String, Object>) topology_template.get("node_templates");
		assertNotNull(nodes);
		Map<String, Object> vlInstanceObj = (Map<String, Object>) nodes.get(instanceName);
		assertNotNull(vlInstanceObj);
		Map<String, Object> props = (Map<String, Object>) vlInstanceObj.get("properties");
		assertNotNull(props);
		Map<String, Object> complexPropObj = (Map<String, Object>) props.get(complexPropName);
		assertNotNull(complexPropObj);
		Gson gson = new Gson();
		assertEquals(gson.toJson(complexPropObj), complexProp.getValue());
		
		// if simpleProp has an empty value it will not be generated in the tosca map
		if (!simpleProp.getValue().equals("{}")){
			Map<String, Object> simplePropObj = (Map<String, Object>) props.get(simplePropName);
			assertNotNull(simplePropObj);
			assertEquals(gson.toJson(simplePropObj), simpleProp.getValue());
		}
		
		
		
		
		
		
	}



}
