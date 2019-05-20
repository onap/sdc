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
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
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
		ComponentInstance componentInstance = buildComponentInstanceWithTwoArtifacts();

		DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
		testInstance.saveDataBeforeMerge(dataForMergeHolder, containerComponent, componentInstance, originComponent);
		Map<String, ArtifactDefinition> originalComponentDeploymentArtifactsCreatedOnTheInstance = dataForMergeHolder
				.getOrigComponentDeploymentArtifactsCreatedOnTheInstance();

		assertEquals(originalComponentDeploymentArtifactsCreatedOnTheInstance.size(), 1);
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

		assertEquals(originalComponentInformationalArtifactsCreatedOnTheInstance.size(), 1);
		assert (originalComponentInformationalArtifactsCreatedOnTheInstance.containsKey("artifactTwo"));
	}

	@Test
	public void testMergeDataAfterCreate() throws Exception {

		Component originComponent = buildOriginalComponentWithOneArtifact();
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		ComponentInstance ci = new ComponentInstance();
		ci.setUniqueId("mock");
		resourceInstances.add(ci);
		originComponent.setComponentInstances(resourceInstances);
		DataForMergeHolder dataForMergeHolder = new DataForMergeHolder();
		Map<String, ArtifactDefinition> origDeploymentArtifacts = new HashMap<>();
		ArtifactDefinition currentArtifactDefinition = new ArtifactDefinition();
		origDeploymentArtifacts.put("mock", currentArtifactDefinition);
		dataForMergeHolder.setOrigComponentDeploymentArtifactsCreatedOnTheInstance(origDeploymentArtifacts);

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
		
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> left = Either.left(Either.left(new ArtifactDefinition()));
		
		when(artifactsBusinessLogicMock.updateResourceInstanceArtifactNoContent(Mockito.anyString(), Mockito.any(Component.class), Mockito.any(User.class), 
				Mockito.any(Map.class), Mockito.any(ArtifactOperationInfo.class), Mockito.any(ArtifactDefinition.class))).thenReturn(left);
		
		testInstance.mergeDataAfterCreate(new User(), dataForMergeHolder, originComponent, "mock");

	}

	private ComponentInstance buildComponentInstanceWithTwoArtifacts() {
		ArtifactDefinition artifactFromTheOriginalResource = new ArtifactDefinition();
		artifactFromTheOriginalResource.setArtifactLabel("artifactOne");
		ArtifactDefinition artifactCreatedOnTheInstance = new ArtifactDefinition();
		artifactCreatedOnTheInstance.setArtifactLabel("artifactTwo");

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

		Map<String, ArtifactDefinition> originComponentArtifacts = new HashMap<>();
		originComponentArtifacts.put(artifactFromTheOriginalResource.getArtifactLabel(),
				artifactFromTheOriginalResource);
		Component originComponent = new Resource();
		originComponent.setDeploymentArtifacts(originComponentArtifacts);
		originComponent.setArtifacts(originComponentArtifacts);
		return originComponent;
	}

}