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

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiResourceDataTransfer;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.Test;



public class FilteredDataByParamsComponentServletTest extends ComponentBaseTest{

	private static final String CSAR_NAME = "LDSA1_with_inputs.csar";
	private static final String COMPONENT_INSTANCES = "include=componentInstances";
	private static final String COMPONENT_INSTANCES_RELATIONS = "include=componentInstancesRelations";
	private static final String DEPLOYMENT_ARTIFACTS = "include=deploymentArtifacts";
	private static final String INFORMATIONAL_ARTIFACTS = "include=artifacts";
	private static final String METADATA = "include=metadata";
	public static TestName name = new TestName();


	
	protected User designerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	
	public FilteredDataByParamsComponentServletTest() {
		super(name, ImportCsarResourceTest.class.getName());
	}

	
	@Test
	public void getComponentInstancesAndComponentInstancesRelationsTest() throws Exception {
		Resource resource = ResourceRestUtils.importResourceFromCsar(CSAR_NAME);
		List<String> parameters = new ArrayList<>();
		parameters.add(COMPONENT_INSTANCES);
		parameters.add(COMPONENT_INSTANCES_RELATIONS);
		// create UiComponentDataTransfer and parse the ComponentInstancesRelations into it
		RestResponse resourceGetResponse = ResourceRestUtils.getResourceFilteredDataByParams(designerDetails, resource.getUniqueId() , parameters);
		UiComponentDataTransfer uiComponentWithComponentInstancesAndRelations = ResponseParser.parseToObjectUsingMapper(resourceGetResponse.getResponse() , UiComponentDataTransfer.class);
		
		uiComponentWithComponentInstancesAndRelations.getComponentInstances().stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		resource.getComponentInstances().stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		
		for (int i = 0 ; i <  resource.getComponentInstances().size() ; i++){
			assertEquals(uiComponentWithComponentInstancesAndRelations.getComponentInstances().get(i).getUniqueId() ,resource.getComponentInstances().get(i).getUniqueId());
		}
		assertEquals(uiComponentWithComponentInstancesAndRelations.getComponentInstancesRelations().size() , resource.getComponentInstancesRelations().size());
	}

	
	@Test
	public void getComponentDeploymentAndInformationalArtifacts() throws Exception {
		Resource resource = ResourceRestUtils.importResourceFromCsar(CSAR_NAME);
		List<String> parameters = new ArrayList<>();
		parameters.add(DEPLOYMENT_ARTIFACTS);
		parameters.add(INFORMATIONAL_ARTIFACTS);

		// create new UiComponentData transfer and parse the artifacts into it
		RestResponse resourceGetResponse = ResourceRestUtils.getResourceFilteredDataByParams(designerDetails, resource.getUniqueId() , parameters);
		UiComponentDataTransfer uiComponentWithArtifacts = ResponseParser.parseToObjectUsingMapper(resourceGetResponse.getResponse() , UiComponentDataTransfer.class);
		
		List<ArtifactDefinition> deploymentArtifactsFromResource = new ArrayList<ArtifactDefinition>(resource.getDeploymentArtifacts().values());
		List<ArtifactDefinition> deploymentArtifactsFromUiComponent = new ArrayList<ArtifactDefinition>(uiComponentWithArtifacts.getDeploymentArtifacts().values());
		List<ArtifactDefinition> informationalArtifactsFromResource = new ArrayList<ArtifactDefinition>(resource.getArtifacts().values());
		List<ArtifactDefinition> informationalArtifactsFromUiComponent = new ArrayList<ArtifactDefinition>(uiComponentWithArtifacts.getArtifacts().values());
		
		deploymentArtifactsFromResource.stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		deploymentArtifactsFromUiComponent.stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		informationalArtifactsFromResource.stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		informationalArtifactsFromUiComponent.stream().sorted((object1, object2) -> object1.getUniqueId().compareTo(object2.getUniqueId()));
		
		for (int i = 0 ; i <  deploymentArtifactsFromResource.size() ; i++){
			assertEquals(deploymentArtifactsFromResource.get(i).getUniqueId() , deploymentArtifactsFromUiComponent.get(i).getUniqueId());
		}
		
		for (int i = 0 ; i <  informationalArtifactsFromResource.size() ; i++){
			assertEquals(informationalArtifactsFromResource.get(i).getUniqueId() , informationalArtifactsFromUiComponent.get(i).getUniqueId());
		}
	}
	
	
	@Test
	public void getComponentMetadataTest() throws Exception {
		
		Resource resource = ResourceRestUtils.importResourceFromCsar(CSAR_NAME);
		List<String> parameters = new ArrayList<>();
		parameters.add(METADATA);

		// create new UiResourceDataTransfer and parse the metadata into it
		RestResponse resourceGetResponse = ResourceRestUtils.getResourceFilteredDataByParams(designerDetails, resource.getUniqueId() , parameters);
		UiResourceDataTransfer uiResourceWithMetadata = ResponseParser.parseToObjectUsingMapper(resourceGetResponse.getResponse(), UiResourceDataTransfer.class);
		
		// assert that the metadata is equal
		assertEquals(uiResourceWithMetadata.getMetadata().getName(), resource.getName());
		assertEquals(uiResourceWithMetadata.getMetadata().getVersion() , resource.getVersion());
		assertEquals(uiResourceWithMetadata.getMetadata().getUniqueId() , resource.getUniqueId());
		assertEquals(uiResourceWithMetadata.getMetadata().getUUID(), resource.getUUID());
	}
	



}
