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

package org.openecomp.sdc.ci.tests.execute.artifacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class HeatEnvArtifact extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public HeatEnvArtifact() {
		super(name, HeatEnvArtifact.class.getName());
	}

	@Test(enabled = true)
	public void heatEnvOnResourceFormatTest() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "csarHeatEnv.csar");
		assertNotNull(createdResource);

		RestResponse certifyState = LifecycleRestUtils.changeComponentState(createdResource, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(certifyState);

		Resource certifiedResource = ResponseParser.parseToObjectUsingMapper(certifyState.getResponse(), Resource.class);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());

		// 2 create service
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// 3 create vf instance in service
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(certifiedResource);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());

		ComponentInstance vfi = componentInstances.get(0);
		Map<String, ArtifactDefinition> deploymentArtifacts = vfi.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		assertEquals(4, deploymentArtifacts.size());
		ArtifactDefinition heatEnv = deploymentArtifacts.get("heat0env");
		assertNotNull(heatEnv);

		Map<String, Object> yaml = downloadComponentInstanceYamlFile(service.getUniqueId(), vfi.getUniqueId(), sdncModifierDetails, heatEnv.getUniqueId());
		assertNotNull(yaml);
		Map<String, Object> paramters = (Map<String, Object>)yaml.get("parameters");
		assertNotNull(paramters);
		assertEquals(8, paramters.size());
		assertEquals(null, paramters.get("param8"));
		List<HeatParameterDataDefinition> heatParameters = heatEnv.getHeatParameters();
		heatParameters.forEach(p -> {
			assertEquals(p.getCurrentValue(), paramters.get(p.getName()));
		});
	}
	@Test(enabled = true)
	public void noHeatEnvOnResourceFormatTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Resource createdResource = createVfFromCSAR(sdncModifierDetails, "csarHeatNoEnv.csar");
		assertNotNull(createdResource);

		RestResponse certifyState = LifecycleRestUtils.changeComponentState(createdResource, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(certifyState);

		Resource certifiedResource = ResponseParser.parseToObjectUsingMapper(certifyState.getResponse(), Resource.class);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());

		// 2 create service
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// 3 create vf instance in service
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(certifiedResource);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());
		
		ComponentInstance vfi = componentInstances.get(0);
		Map<String, ArtifactDefinition> deploymentArtifacts = vfi.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		assertEquals(4, deploymentArtifacts.size());
		ArtifactDefinition heatEnv = deploymentArtifacts.get("heat0env");
		assertNotNull(heatEnv);

		Map<String, Object> yaml = downloadComponentInstanceYamlFile(service.getUniqueId(), vfi.getUniqueId(), sdncModifierDetails, heatEnv.getUniqueId());
		assertNotNull(yaml);
		Map<String, Object> paramters = (Map<String, Object>)yaml.get("parameters");
		assertNotNull(paramters);
		assertEquals(8, paramters.size());
		assertEquals(null, paramters.get("param1"));
		assertEquals(null, paramters.get("param2"));
		assertEquals(null, paramters.get("param4"));
		assertEquals(null, paramters.get("param5"));
		assertEquals(null, paramters.get("param7"));
		assertEquals(null, paramters.get("param8"));
		List<HeatParameterDataDefinition> heatParameters = heatEnv.getHeatParameters();
		heatParameters.forEach(p -> {
			assertEquals(p.getCurrentValue(), paramters.get(p.getName()));
		});

	}
	//****************************************
	private Map<String, Object> downloadComponentInstanceYamlFile(String serviceUniqueId, String resourceInstanceId, User user, String artifactUniqeId) throws Exception {
		RestResponse heatEnvDownloadResponse = ArtifactRestUtils.downloadResourceInstanceArtifact(serviceUniqueId, resourceInstanceId, user, artifactUniqeId);
		BaseRestUtils.checkSuccess(heatEnvDownloadResponse);

		ArtifactUiDownloadData artifactUiDownloadData = ResponseParser.parseToObject(heatEnvDownloadResponse.getResponse(), ArtifactUiDownloadData.class);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		byte[] decodeBase64 = Base64.decodeBase64(fromUiDownload);
		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(decodeBase64);

		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);

		return load;
	}
}
