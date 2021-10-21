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

package org.openecomp.sdc.be.impl;

import org.javatuples.Pair;
import org.junit.Test;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ForwardingPathUtilsTest {

	private ForwardingPathUtils createTestSubject() {
		return new ForwardingPathUtils();
	}

	@Test
	public void testConvertServiceToServiceRelations() {
		ForwardingPathUtils testSubject;
		Service service = new Service();
		ServiceRelations result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertServiceToServiceRelations(service);
		assertThat(result).isInstanceOf(ServiceRelations.class);

		List<ComponentInstance> resourceInstances = new LinkedList<>();
		ComponentInstance e = new ComponentInstance();
		e.setCapabilities(new HashMap<>());
		resourceInstances.add(e);
		service.setComponentInstances(resourceInstances);

		result = testSubject.convertServiceToServiceRelations(service);
		assertThat(result).isInstanceOf(ServiceRelations.class);
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
		assertThat(result).isInstanceOf(Set.class);
	}

	@Test
	public void testUpdateForwardingPathOnVersionChange() {
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
		assertThat((Iterable<?>) result).isInstanceOf(Pair.class);
	}

	@Test
	public void testGetForwardingPathsToBeDeletedOnVersionChange() {
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
		assertThat(result).isInstanceOf(Set.class);
	}
}
