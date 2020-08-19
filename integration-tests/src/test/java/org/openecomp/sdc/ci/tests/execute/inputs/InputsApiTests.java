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

import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fj.data.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.InputsRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

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
		Resource vf = AtomicOperationUtils.importResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, inputCsar1);
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
		Resource vfWithInputs = AtomicOperationUtils.importResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, inputCsar2);

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


	@Test
	public void validateValidValueConstraintOnInputFailTest() throws Exception {
		Either<Service, RestResponse> createDefaultServiceEither = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		if (createDefaultServiceEither.isRight()){
			assertTrue("Error creating default service", false);
		}
		Service service = createDefaultServiceEither.left().value();

		String body = "{\"SubnetProp\": {\"schema\": {\"property\": {\"type\": \"\"}},\"type\": \"org.openecomp"
				+ ".datatypes.heat.network.neutron.Subnet\",\"name\": \"SubnetProp\"}}";
		RestResponse createPropertyResponse = PropertyRestUtils.createProperty(service.getUniqueId(), body,
				sdncDesignerDetails);
		AssertJUnit.assertEquals("Expected result code - 200, received - " + createPropertyResponse.getErrorCode(), 200,
				(int) createPropertyResponse.getErrorCode());

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		PropertyReqDetails propertyResponseObject = mapper.readValue(createPropertyResponse.getResponse(),
				PropertyReqDetails.class);

		PropertyDefinition input = new PropertyDefinition();
		input.setName("ipv6_address_mode");
		input.setType("string");
		input.setUniqueId("org.openecomp.datatypes.heat.network.neutron.Subnet.datatype.ipv6_address_mode");

		ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput();
		componentInstancePropInput.setPropertiesName("SubnetProp#ipv6_address_mode");
		componentInstancePropInput.setName("SubnetProp");
		componentInstancePropInput.setParentUniqueId(service.getUniqueId());
		componentInstancePropInput.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		componentInstancePropInput.setUniqueId(propertyResponseObject.getUniqueId());
		componentInstancePropInput.setInput(input);

		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setServiceProperties(Collections.singletonMap(service.getUniqueId(),
				Collections.singletonList(componentInstancePropInput)));
		RestResponse addInputResponse = InputsRestUtils.addInput(service, componentInstInputsMap,
				UserRoleEnum.DESIGNER);

		AssertJUnit.assertEquals("Expected result code - 200, received - " + addInputResponse.getErrorCode(),
				200, (int) addInputResponse.getErrorCode());

		Type constraintType = new TypeToken<PropertyConstraint>() {}.getType();
		Type inDefType = new TypeToken<List<InputDefinition>>() { }.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyOperation.PropertyConstraintDeserialiser()).create();
		try {
			List<InputDefinition> inputDefinitions = gson.fromJson(addInputResponse.getResponse(),
					inDefType);

			inputDefinitions.get(0).setDefaultValue("Fail");

			RestResponse updateInputResponse = InputsRestUtils.updateInput(service, mapper.writeValueAsString(inputDefinitions),
					UserRoleEnum.DESIGNER);

			AssertJUnit.assertEquals("Expected result code - 400, received - " + addInputResponse.getErrorCode(),
					400, (int) updateInputResponse.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void validateValidValueConstraintOnInputSuccessTest() throws Exception {
		Either<Service, RestResponse> createDefaultServiceEither = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		if (createDefaultServiceEither.isRight()){
			assertTrue("Error creating default service", false);
		}
		Service service = createDefaultServiceEither.left().value();

		String body = "{\"SubnetProp\": {\"schema\": {\"property\": {\"type\": \"\"}},\"type\": \"org.openecomp"
				+ ".datatypes.heat.network.neutron.Subnet\",\"name\": \"SubnetProp\"}}";
		RestResponse createPropertyResponse = PropertyRestUtils.createProperty(service.getUniqueId(), body,
				sdncDesignerDetails);
		AssertJUnit.assertEquals("Expected result code - 200, received - " + createPropertyResponse.getErrorCode(), 200,
				(int) createPropertyResponse.getErrorCode());

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		PropertyReqDetails propertyResponseObject = mapper.readValue(createPropertyResponse.getResponse(),
				PropertyReqDetails.class);

		PropertyDefinition input = new PropertyDefinition();
		input.setName("ipv6_address_mode");
		input.setType("string");
		input.setUniqueId("org.openecomp.datatypes.heat.network.neutron.Subnet.datatype.ipv6_address_mode");

		ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput();
		componentInstancePropInput.setPropertiesName("SubnetProp#ipv6_address_mode");
		componentInstancePropInput.setName("SubnetProp");
		componentInstancePropInput.setParentUniqueId(service.getUniqueId());
		componentInstancePropInput.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		componentInstancePropInput.setUniqueId(propertyResponseObject.getUniqueId());
		componentInstancePropInput.setInput(input);

		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		componentInstInputsMap.setServiceProperties(Collections.singletonMap(service.getUniqueId(),
				Collections.singletonList(componentInstancePropInput)));
		RestResponse addInputResponse = InputsRestUtils.addInput(service, componentInstInputsMap,
				UserRoleEnum.DESIGNER);

		AssertJUnit.assertEquals("Expected result code - 200, received - " + addInputResponse.getErrorCode(),
				200, (int) addInputResponse.getErrorCode());

		Type constraintType = new TypeToken<PropertyConstraint>() {}.getType();
		Type inDefType = new TypeToken<List<InputDefinition>>() { }.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyOperation.PropertyConstraintDeserialiser()).create();
		try {
			List<InputDefinition> inputDefinitions = gson.fromJson(addInputResponse.getResponse(),
					inDefType);

			inputDefinitions.get(0).setDefaultValue("slaac");

			RestResponse updateInputResponse = InputsRestUtils.updateInput(service, mapper.writeValueAsString(inputDefinitions),
					UserRoleEnum.DESIGNER);

			AssertJUnit.assertEquals("Expected result code - 200, received - " + addInputResponse.getErrorCode(),
					200, (int) updateInputResponse.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
