package org.openecomp.sdc.be.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.javatuples.Pair;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.datamodel.NameIdPair;
import org.openecomp.sdc.be.datamodel.NameIdPairWrapper;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import mockit.Deencapsulation;

public class ForwardingPathUtilsTest {

	private ForwardingPathUtils createTestSubject() {
		return new ForwardingPathUtils();
	}

	@Test
	public void testConvertServiceToServiceRelations() throws Exception {
		ForwardingPathUtils testSubject;
		Service service = new Service();
		ServiceRelations result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertServiceToServiceRelations(service);
		List<ComponentInstance> resourceInstances = new LinkedList<>();
		ComponentInstance e = new ComponentInstance();
		e.setCapabilities(new HashMap<>());
		resourceInstances.add(e);
		service.setComponentInstances(resourceInstances);

		result = testSubject.convertServiceToServiceRelations(service);
	}

	@Test
	public void testAddNodes() throws Exception {
		ForwardingPathUtils testSubject;
		List<NameIdPairWrapper> cpOptions = new LinkedList<>();
		Map<NameIdPair, Set<NameIdPair>> options = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "addNodes", cpOptions, options);
	}

	@Test
	public void testCreateWrapper() throws Exception {
		ForwardingPathUtils testSubject;
		NameIdPair cpOption = new NameIdPair("mock", "mock");
		NameIdPairWrapper result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createWrapper", cpOption);
	}

	@Test
	public void testGetResponseFormatManager() throws Exception {
		ForwardingPathUtils testSubject;
		ResponseFormatManager result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getResponseFormatManager");
	}

	@Test
	public void testFindForwardingPathNamesToDeleteOnComponentInstanceDeletion() throws Exception {
		ForwardingPathUtils testSubject;
		Service containerService = new Service();
		containerService.setForwardingPaths(new HashMap<>());
		String componentInstanceId = "";
		Set<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findForwardingPathNamesToDeleteOnComponentInstanceDeletion(containerService,
				componentInstanceId);
	}

	@Test
	public void testFindForwardingPathToDeleteOnCIDeletion() throws Exception {
		ForwardingPathUtils testSubject;
		Service containerService = new Service();
		containerService.setForwardingPaths(new HashMap<>());
		String componentInstanceId = "";
		Map<String, ForwardingPathDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "findForwardingPathToDeleteOnCIDeletion", containerService,
				componentInstanceId);
	}

	@Test
	public void testElementContainsCI_1() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
		elementDataDefinitions.setFromNode("mock");
		String componentInstanceId = "mock";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "elementContainsCI", elementDataDefinitions, componentInstanceId);
	}

	@Test
	public void testUpdateForwardingPathOnVersionChange() throws Exception {
		ForwardingPathUtils testSubject;
		Service containerService = new Service();
		containerService.setForwardingPaths(new HashMap<>());
		DataForMergeHolder dataHolder = new DataForMergeHolder();
		Component updatedContainerComponent = new Service();
		String newInstanceId = "";
		Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateForwardingPathOnVersionChange(containerService, dataHolder,
				updatedContainerComponent, newInstanceId);
	}

	@Test
	public void testGetForwardingPathsToBeDeletedOnVersionChange() throws Exception {
		ForwardingPathUtils testSubject;
		Service containerService = new Service();
		containerService.setForwardingPaths(new HashMap<>());
		DataForMergeHolder dataHolder = new DataForMergeHolder();
		Component updatedContainerComponent = new Service();
		Set<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getForwardingPathsToBeDeletedOnVersionChange(containerService, dataHolder,
				updatedContainerComponent);
	}

	@Test
	public void testUpdateCI() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathDataDefinition inFP = new ForwardingPathDataDefinition();
		inFP.setPathElements(new ListDataDefinition<>());
		String oldCI = "";
		String newCI = "";
		ForwardingPathDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "updateCI", inFP, oldCI, newCI);
	}

	@Test
	public void testUpdateElement() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathElementDataDefinition element = new ForwardingPathElementDataDefinition();
		element.setFromNode("mock");
		element.setToNode("mock");
		element.setToCP("mock");
		String oldCI = "";
		String newCI = "";
		ForwardingPathElementDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "updateElement", element, oldCI, newCI);
	}

	@Test
	public void testElementContainsCIAndForwarder_1() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
		elementDataDefinitions.setFromNode("mock");
		elementDataDefinitions.setToNode("mock");
		elementDataDefinitions.setToCP("mock");
		String oldCIId = "mock";
		Component newCI = new Resource();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "elementContainsCIAndForwarder", elementDataDefinitions, oldCIId,
				newCI);
	}

	@Test
	public void testCiContainsForwarder() throws Exception {
		ForwardingPathUtils testSubject;
		Component newCI = new Resource();
		String capabilityID = "mock";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "ciContainsForwarder", newCI, capabilityID);
		newCI.setCapabilities(new HashMap<>());
		result = Deencapsulation.invoke(testSubject, "ciContainsForwarder", newCI, capabilityID);
	}

	@Test
	public void testElementContainsCIAndDoesNotContainForwarder() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
		ListDataDefinition<ForwardingPathElementDataDefinition> pathElements = new ListDataDefinition<>();
		forwardingPathDataDefinition.setPathElements(pathElements);
		String oldCIId = "";
		Component newCI = new Resource();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "elementContainsCIAndDoesNotContainForwarder",
				forwardingPathDataDefinition, oldCIId, newCI);
	}

	@Test
	public void testElementContainsCIAndDoesNotContainForwarder_1() throws Exception {
		ForwardingPathUtils testSubject;
		ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
		elementDataDefinitions.setFromNode("mock");
		elementDataDefinitions.setToNode("mock");
		elementDataDefinitions.setToCP("mock");
		String oldCIId = "";
		Component newCI = new Resource();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "elementContainsCIAndDoesNotContainForwarder",
				elementDataDefinitions, oldCIId, newCI);
	}
}