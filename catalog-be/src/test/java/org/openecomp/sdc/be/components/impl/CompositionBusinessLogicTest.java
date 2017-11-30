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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils;

public class CompositionBusinessLogicTest {
	CompositionBusinessLogic compBl = new CompositionBusinessLogic();

	@Test
	public void testBuildSpiralPatternPositioningForComponentInstances() {
		int instancesNum = 10;
		Resource createVF = FactoryUtils.createVF();
		for (int i = 0; i < instancesNum; i++) {
			FactoryUtils.addComponentInstanceToVF(createVF, FactoryUtils.createResourceInstance());
		}
		Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstances = compBl.buildSpiralPatternPositioningForComponentInstances(createVF);
		assertTrue(componentInstances.size() == instancesNum);
		// Verify Spiral Pattern
		ImmutablePair<Double, Double> key;
		key = new ImmutablePair<>(0D, 0D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(-1D, 0D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(-1D, 1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(0D, 1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(1D, 1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(1D, 0D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(1D, -1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(0D, -1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(-1D, -1D);
		assertTrue(componentInstances.containsKey(key));
		key = new ImmutablePair<>(-2D, -1D);
		assertTrue(componentInstances.containsKey(key));
	}

	@Test
	public void testGetCpsConnectedToVFC() {
		List<ComponentInstance> allComponentInstances = new ArrayList<>();
		Resource createVF = FactoryUtils.createVF();
		ComponentInstance vfc = populateVfWithVfcAndCps(allComponentInstances, createVF);

		Map<ComponentInstance, List<ComponentInstance>> cpsConnectedToVFC = compBl.getCpsConnectedToVFC(allComponentInstances, createVF);
		assertTrue(cpsConnectedToVFC.size() == 1);
		assertTrue(cpsConnectedToVFC.containsKey(vfc));
		Set<ComponentInstance> cps = cpsConnectedToVFC.get(vfc).stream().collect(Collectors.toSet());
		assertTrue(cps.size() == 3);
		cps.stream().forEach(e -> assertTrue(e.getOriginType() == OriginTypeEnum.CP));

	}

	@Test
	public void testBuildCirclePatternForCps() {
		List<ComponentInstance> allComponentInstances = new ArrayList<>();
		Resource createVF = FactoryUtils.createVF();
		ComponentInstance vfcInstance = populateVfWithVfcAndCps(allComponentInstances, createVF);
		Map<ComponentInstance, List<ComponentInstance>> cpsConnectedToVFC = compBl.getCpsConnectedToVFC(allComponentInstances, createVF);

		Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstLocations = new HashMap<>();
		componentInstLocations.put(new ImmutablePair<Double, Double>(0D, 0D), vfcInstance);
		compBl.buildCirclePatternForCps(componentInstLocations, cpsConnectedToVFC);
		assertTrue(componentInstLocations.size() == 4);

		Set<ImmutablePair<Double, Double>> cpsLocations = componentInstLocations.entrySet().stream().filter(entry -> entry.getValue().getOriginType() == OriginTypeEnum.CP).map(e -> e.getKey()).collect(Collectors.toSet());
		// Verify that all cps are located at different positions
		assertTrue(cpsLocations.size() == 3);
		Set<Double> distances = cpsLocations.stream().map(cpLocation -> Math.sqrt(Math.pow(cpLocation.left, 2) + Math.pow(cpLocation.right, 2))).collect(Collectors.toSet());
		// Verify that all cps are at the same distance from center
		assertTrue(distances.size() == 1);

	}

	/**
	 * Adds 4 instances to the vf.<br>
	 * vfc instance and 3 cps instances.<br>
	 * the cp instances are connected to the vfc instance.<br>
	 * 
	 * @param allComponentInstances
	 * @param createVF
	 * @return vfc instance
	 */
	private ComponentInstance populateVfWithVfcAndCps(List<ComponentInstance> allComponentInstances, Resource createVF) {
		ComponentInstance vfc = FactoryUtils.createResourceInstance();
		vfc.setOriginType(OriginTypeEnum.VFC);
		FactoryUtils.addComponentInstanceToVF(createVF, vfc);
		allComponentInstances.add(vfc);

		connectCpToVfc(allComponentInstances, createVF, vfc);
		connectCpToVfc(allComponentInstances, createVF, vfc);
		connectCpToVfc(allComponentInstances, createVF, vfc);
		return vfc;
	}

	private void connectCpToVfc(List<ComponentInstance> allComponentInstances, Resource createVF, ComponentInstance vfc) {
		List<RequirementCapabilityRelDef> allRelations;
		if (createVF.getComponentInstancesRelations() != null) {
			allRelations = createVF.getComponentInstancesRelations();
		} else {
			allRelations = new ArrayList<>();
			createVF.setComponentInstancesRelations(allRelations);
		}
		ComponentInstance cp1 = FactoryUtils.createResourceInstance();
		cp1.setOriginType(OriginTypeEnum.CP);
		addVfcCpRelation(vfc, cp1, allRelations);
		FactoryUtils.addComponentInstanceToVF(createVF, cp1);
		allComponentInstances.add(cp1);
	}

	private void addVfcCpRelation(ComponentInstance vfc, ComponentInstance cp, List<RequirementCapabilityRelDef> allRelations) {
		RequirementCapabilityRelDef rel = new RequirementCapabilityRelDef();
		rel.setToNode(vfc.getComponentUid());
		rel.setFromNode(cp.getComponentUid());
		allRelations.add(rel);
	}
}
