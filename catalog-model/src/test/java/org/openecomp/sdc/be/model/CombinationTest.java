/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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



package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.ui.model.UiCombination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CombinationTest {

    private Combination createTestSubject() {
        return new Combination();
    }

    @Test
    public void testCtor() throws Exception {
        UiCombination uiComb = Mockito.mock(UiCombination.class);
        new Combination(uiComb);
    }

    @Test
    public void testGetName() throws Exception {
        Combination testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getName();
    }

    @Test
    public void testSetName() throws Exception {
        Combination testSubject;
        String result = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setName(result);
    }

    @Test
    public void testGetDesc() throws Exception {
        Combination testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getDesc();
    }

    @Test
    public void testSetDesc() throws Exception {
        Combination testSubject;
        String result = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setDesc(result);
    }

    @Test
    public void testGetUniqueId() throws Exception {
        Combination testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUniqueId();
    }

    @Test
    public void testSetUniqueId() throws Exception {
        Combination testSubject;
        String result = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setUniqueId(result);
    }

    @Test
    public void testGetComponentInstances() throws Exception {
        Combination testSubject;
        List<ComponentInstance> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstances();
    }

    @Test
    public void testSetComponentInstances() throws Exception {
        Combination testSubject;
        List<ComponentInstance> result = new ArrayList<ComponentInstance>();

        // default test
        testSubject = createTestSubject();
        testSubject.setComponentInstances(result);
    }

    @Test
    public void testGetComponentInstancesRelations() throws Exception {
        Combination testSubject;
        List<RequirementCapabilityRelDef> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstancesRelations();
    }

    @Test
    public void testSetComponentInstancesRelations() throws Exception {
        Combination testSubject;
        List<RequirementCapabilityRelDef> result = new ArrayList<RequirementCapabilityRelDef>();

        // default test
        testSubject = createTestSubject();
        testSubject.setComponentInstancesRelations(result);
    }

    @Test
    public void testGetComponentInstancesInputs() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceInput>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstancesInputs();
    }

    @Test
    public void testSetComponentInstancesInputs() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceInput>> result = new HashMap<String, List<ComponentInstanceInput>>();

        // default test
        testSubject = createTestSubject();
        testSubject.setComponentInstancesInputs(result);
    }

    @Test
    public void testGetComponentInstancesProperties() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceProperty>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstancesProperties();
    }

    @Test
    public void testSetComponentInstancesProperties() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceProperty>> result = new HashMap<String, List<ComponentInstanceProperty>>();

        // default test
        testSubject = createTestSubject();
        testSubject.setComponentInstancesProperties(result);
    }

    @Test
    public void testGetComponentInstancesAttributes() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceProperty>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getComponentInstancesAttributes();
    }

    @Test
    public void testSetComponentInstancesAttributes() throws Exception {
        Combination testSubject;
        Map<String, List<ComponentInstanceProperty>> result = new HashMap<String, List<ComponentInstanceProperty>>();

        // default test
        testSubject = createTestSubject();
        testSubject.setComponentInstancesAttributes(result);
    }
}
