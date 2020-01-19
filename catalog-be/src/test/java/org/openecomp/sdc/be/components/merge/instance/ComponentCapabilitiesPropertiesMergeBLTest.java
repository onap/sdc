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
import org.openecomp.sdc.be.components.merge.capability.CapabilityResolver;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class ComponentCapabilitiesPropertiesMergeBLTest {

	@InjectMocks
	ComponentCapabilitiesPropertiesMergeBL testSubject;

	@Mock
	DataDefinitionsValuesMergingBusinessLogic dataDefinitionsValuesMergingBusinessLogicMock;

	@Mock
	private ToscaOperationFacade toscaOperationFacadeMock;

	@Mock
	ComponentsUtils componentsUtilsMock;

	@Mock
	CapabilityResolver capabilityResolverMock;

	private Resource buildBasicResource;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testDescription() throws Exception {
		// ComponentCapabilitiesPropertiesMergeBL testSubject;
		String result;

		// default test
		// testSubject = createTestSubject();
		result = testSubject.description();
	}

	@Test
	public void testMergeComponents() throws Exception {
		// ComponentCapabilitiesPropertiesMergeBL testSubject;
		Component prevComponent = ObjectGenerator.buildResourceWithComponentInstance("mock3");
		Component currentComponent = ObjectGenerator.buildResourceWithComponentInstance("mock1", "mock2");
		currentComponent.setUniqueId("mock");
		ActionStatus result;
		buildBasicResource = ObjectGenerator.buildBasicResource();
		when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.left(buildBasicResource));
		;
		result = testSubject.mergeComponents(prevComponent, currentComponent);
	}

	@Test
	public void testMergeComponentInstanceCapabilities() throws Exception {
		// ComponentCapabilitiesPropertiesMergeBL testSubject;
		Component currentComponent = null;
		Component origInstanceCmpt = null;
		String instanceId = "";

		List<CapabilityDefinition> prevInstanceCapabilities = null;
		ActionStatus result;

		// default test
		// testSubject = createTestSubject();
		result = testSubject.mergeComponentInstanceCapabilities(currentComponent, origInstanceCmpt, instanceId,
				prevInstanceCapabilities);

		prevInstanceCapabilities = initCapabilites();

		result = testSubject.mergeComponentInstanceCapabilities(currentComponent, origInstanceCmpt, instanceId,
				prevInstanceCapabilities);
	}

	private List<CapabilityDefinition> initCapabilites() {
		final String NODE_A_FORWARDER_CAPABILITY = "nodeA_FORWARDER_CAPABILITY";

		CapabilityDefinition forwarder = new CapabilityDefinition();
		forwarder.setType(ForwardingPathUtils.FORWARDER_CAPABILITY);
		forwarder.setUniqueId(NODE_A_FORWARDER_CAPABILITY);
		return Arrays.asList(forwarder);
	}
}
