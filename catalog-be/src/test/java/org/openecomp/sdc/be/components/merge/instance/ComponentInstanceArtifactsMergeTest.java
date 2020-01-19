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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ComponentInstanceArtifactsMergeTest {

	@InjectMocks
	ComponentInstanceArtifactsMerge testInstance;

	@Mock
	ArtifactsBusinessLogic artifactsBusinessLogicMock;
	
	@Mock
	ToscaOperationFacade toscaOperationFacadeMock;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testDeploymentArtifactSaveData() throws Exception {

		Component containerComponent = new Resource();
		Component originComponent = buildOriginalComponentWithOneArtifact();
        ComponentInstance componentInstance = buildComponentInstanceWithTwoArtifactsAndVfModuleFile();

		DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
		testInstance.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, originComponent);
		Map<String, ArtifactDefinition> originalComponentDeploymentArtifactsCreatedOnTheInstance = dataForMergeHolder
				.getOrigComponentDeploymentArtifactsCreatedOnTheInstance();
		Map<String, Integer> componentInstanceDeploymentArtifactsTimeOut = dataForMergeHolder
				.getComponentInstanceDeploymentArtifactsTimeOut();
        assertThat(originalComponentDeploymentArtifactsCreatedOnTheInstance.size()).isEqualTo(1);
		assertThat(componentInstanceDeploymentArtifactsTimeOut.size()).isEqualTo(3);
		assert (originalComponentDeploymentArtifactsCreatedOnTheInstance.containsKey("artifactTwo"));
	}

	@Test
	public void testInformationalArtifactSaveData() throws Exception {

		Component containerComponent = new Resource();
		Component originComponent = buildOriginalComponentWithOneArtifact();
		ComponentInstance componentInstance = buildComponentInstanceWithTwoArtifacts();

		DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
		testInstance.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, originComponent);
		Map<String, ArtifactDefinition> originalComponentInformationalArtifactsCreatedOnTheInstance = dataForMergeHolder
				.getOrigComponentInformationalArtifactsCreatedOnTheInstance();

        assertThat(originalComponentInformationalArtifactsCreatedOnTheInstance.size()).isEqualTo(1);
		assert (originalComponentInformationalArtifactsCreatedOnTheInstance.containsKey("artifactTwo"));
	}

	@Test
	public void testMergeDataAfterCreate() throws Exception {

		Component originComponent = buildOriginalComponentWithOneArtifact();
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		ComponentInstance ci = new ComponentInstance();
		ci.setUniqueId("mock");
		Map<String, ArtifactDefinition> currentDeploymentArtifacts = buildDeploymentArtifacts();
		ci.setDeploymentArtifacts(currentDeploymentArtifacts);
		resourceInstances.add(ci);
		originComponent.setComponentInstances(resourceInstances);
		DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
		Map<String, ArtifactDefinition> origDeploymentArtifacts = new HashMap<>();
		ArtifactDefinition currentArtifactDefinition = new ArtifactDefinition();
		origDeploymentArtifacts.put("mock", currentArtifactDefinition);
		dataForMergeHolder.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(origDeploymentArtifacts);
		Map<String, ArtifactDefinition> updateDeploymentArtifacts = buildDeploymentArtifacts();
		updateDeploymentArtifacts.get("artifactOne").setTimeout(55);
		dataForMergeHolder.setComponentInstanceDeploymentArtifactsTimeOut(updateDeploymentArtifacts.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, artifact -> artifact.getValue().getTimeout())));
		when(artifactsBusinessLogicMock.buildJsonForUpdateArtifact(Mockito.anyString(), 
				Mockito.anyString(),
				Mockito.anyString(), 
				Mockito.any(ArtifactGroupTypeEnum.class), 
				Mockito.anyString(), 
				Mockito.anyString(),
				Mockito.anyString(), 
				Mockito.any(byte[].class), 
				Mockito.any(), 
				Mockito.any(List.class)))
						.thenReturn(new HashMap<>());
		
		Either<ArtifactDefinition, Operation> left = Either.left(new ArtifactDefinition());

		User user = new User();

		when(artifactsBusinessLogicMock.updateResourceInstanceArtifactNoContent("mock", originComponent, user,
				new HashMap<>(), artifactsBusinessLogicMock.new ArtifactOperationInfo(false, false,
						ArtifactsBusinessLogic.ArtifactOperationEnum.LINK), currentDeploymentArtifacts.get("artifactOne"))).thenReturn(left);

		when(artifactsBusinessLogicMock.updateResourceInstanceArtifactNoContent(Mockito.anyString(), Mockito.any(Component.class), Mockito.any(User.class),
				Mockito.any(Map.class), Mockito.any(ArtifactOperationInfo.class), Mockito.any(ArtifactDefinition.class))).thenReturn(left);

		testInstance.mergeDataAfterCreate(user, dataForMergeHolder, originComponent, "mock");

	}

	private Map<String, ArtifactDefinition> buildDeploymentArtifacts() {
		ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
		artifactFromTheOriginalResource.setArtifactLabel("artifactOne");
		artifactFromTheOriginalResource.setTimeout(30);
		ArtifactDefinition artifactCreatedOnTheInstance = new ArtifactDefinition();
		artifactCreatedOnTheInstance.setArtifactLabel("artifactTwo");
		artifactCreatedOnTheInstance.setTimeout(30);
		ArtifactDefinition artifactGeneratedBySubmitForTesting = new ArtifactDefinition();
		artifactGeneratedBySubmitForTesting.setArtifactLabel("artifactThree");
		artifactGeneratedBySubmitForTesting.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.name());
		artifactGeneratedBySubmitForTesting.setTimeout(30);
		Map<String, ArtifactDefinition> componentInstanceArtifacts = new HashMap<>();
		componentInstanceArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(), artifactFromTheOriginalResource);
		componentInstanceArtifacts.put(artifactCreatedOnTheInstance.getArtifactLabel(), artifactCreatedOnTheInstance);
		componentInstanceArtifacts.put(artifactGeneratedBySubmitForTesting.getArtifactLabel(), artifactGeneratedBySubmitForTesting);
		return componentInstanceArtifacts;
	}

	private ComponentInstance buildComponentInstanceWithTwoArtifactsAndVfModuleFile(){

		Map<String, ArtifactDefinition> componentInstanceArtifacts = buildDeploymentArtifacts();
		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setArtifacts(componentInstanceArtifacts);
		componentInstance.setDeploymentArtifacts(componentInstanceArtifacts);
		return componentInstance;
	}

	private ComponentInstance buildComponentInstanceWithTwoArtifacts() {
		ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
		artifactFromTheOriginalResource.setArtifactLabel("artifactOne");
		artifactFromTheOriginalResource.setTimeout(30);
		ArtifactDefinition artifactCreatedOnTheInstance = new ArtifactDefinition();
		artifactCreatedOnTheInstance.setArtifactLabel("artifactTwo");
		artifactCreatedOnTheInstance.setTimeout(30);

		Map<String, ArtifactDefinition> componentInstanceArtifacts = new HashMap<>();
		componentInstanceArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(),
				artifactFromTheOriginalResource);
		componentInstanceArtifacts.put(artifactCreatedOnTheInstance.getArtifactLabel(), artifactCreatedOnTheInstance);

		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setDeploymentArtifacts(componentInstanceArtifacts);
		componentInstance.setArtifacts(componentInstanceArtifacts);
		return componentInstance;
	}

	private Component buildOriginalComponentWithOneArtifact() {
		ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
		artifactFromTheOriginalResource.setArtifactLabel("artifactOne");
		artifactFromTheOriginalResource.setTimeout(30);

		Map<String, ArtifactDefinition> originComponentArtifacts = new HashMap<>();
		originComponentArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(),
				artifactFromTheOriginalResource);
		Component originComponent = new Resource();
		originComponent.setDeploymentArtifacts(originComponentArtifacts);
		originComponent.setArtifacts(originComponentArtifacts);
		return originComponent;
	}

}
