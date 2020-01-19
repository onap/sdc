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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CompositionBusinessLogicTest {

    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);

    CompositionBusinessLogic compBl = new CompositionBusinessLogic(componentInstanceBusinessLogic);

    @Test
    public void testBuildSpiralPatternPositioningForComponentInstances() {
        int instancesNum = 10;
        Resource createVF = FactoryUtils.createVF();
        for (int i = 0; i < instancesNum; i++) {
            FactoryUtils.addComponentInstanceToVF(createVF, FactoryUtils.createResourceInstance());
        }
        Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstances = compBl.buildSpiralPatternPositioningForComponentInstances(createVF);
        assertEquals(componentInstances.size(), instancesNum);
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
        assertEquals(1, cpsConnectedToVFC.size());
        assertTrue(cpsConnectedToVFC.containsKey(vfc));
        Set<ComponentInstance> cps = cpsConnectedToVFC.get(vfc).stream().collect(Collectors.toSet());
        assertEquals(3, cps.size());
        cps.stream().forEach(e -> assertSame(e.getOriginType(), OriginTypeEnum.CP));

    }

    @Test
    public void testBuildCirclePatternForCps() {
        List<ComponentInstance> allComponentInstances = new ArrayList<>();
        Resource createVF = FactoryUtils.createVF();
        ComponentInstance vfcInstance = populateVfWithVfcAndCps(allComponentInstances, createVF);
        Map<ComponentInstance, List<ComponentInstance>> cpsConnectedToVFC = compBl.getCpsConnectedToVFC(allComponentInstances, createVF);

        Map<ImmutablePair<Double, Double>, ComponentInstance> componentInstLocations = new HashMap<>();
        componentInstLocations.put(new ImmutablePair<>(0D, 0D), vfcInstance);
        compBl.buildCirclePatternForCps(componentInstLocations, cpsConnectedToVFC);
        assertEquals(4, componentInstLocations.size());

        Set<ImmutablePair<Double, Double>> cpsLocations = componentInstLocations.entrySet().stream().filter(entry -> entry.getValue().getOriginType() == OriginTypeEnum.CP).map(Map.Entry::getKey).collect(Collectors.toSet());
        // Verify that all cps are located at different positions
        assertEquals(3, cpsLocations.size());
        Set<Double> distances = cpsLocations.stream().map(cpLocation -> Math.sqrt(Math.pow(cpLocation.left, 2) + Math.pow(cpLocation.right, 2))).collect(Collectors.toSet());
        // Verify that all cps are at the same distance from center
        assertEquals(1, distances.size());

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
