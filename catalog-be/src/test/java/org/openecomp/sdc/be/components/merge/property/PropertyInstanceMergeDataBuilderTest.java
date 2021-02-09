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

package org.openecomp.sdc.be.components.merge.property;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

public class PropertyInstanceMergeDataBuilderTest {


    @Test
    public void testBuildDataForMerging() throws Exception {
        PropertyInstanceMergeDataBuilder testSubject;
        List oldProps = null;
        List<InputDefinition> oldInputs = null;
        List newProps = null;
        List<InputDefinition> newInputs = null;
        List<MergePropertyData> result;

        // default test

        result = PropertyInstanceMergeDataBuilder.buildDataForMerging(oldProps, oldInputs, newProps, newInputs);
    }

    @Test
    public void testBuildMergeData() throws Exception {
        PropertyInstanceMergeDataBuilder testSubject;
        Map<String, T> oldPropsByName = null;
        Map<String, InputDefinition> oldInputsByName = null;
        Map<String, T> newPropsByName = null;
        Map<String, InputDefinition> newInputsByName = null;
        List<MergePropertyData> result;

        // default test

        result = PropertyInstanceMergeDataBuilder
            .buildMergeData(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    @Test
    public void testBuildMergePropertyData() throws Exception {
        PropertyInstanceMergeDataBuilder testSubject;
        PropertyDataDefinition oldProp = null;
        Map<String, InputDefinition> oldInputsByName = null;
        PropertyDataDefinition newProp = null;
        Map<String, InputDefinition> newInputsByName = null;
        MergePropertyData result;

        // default test
        result = PropertyInstanceMergeDataBuilder.buildMergePropertyData(new PropertyDataDefinition(), new HashMap<>(),
            new PropertyDataDefinition(), new HashMap<>());
    }

    @Test
    public void testGetOldGetInputNamesWhichExistInNewVersion() throws Exception {
        PropertyInstanceMergeDataBuilder testSubject;
        List<GetInputValueDataDefinition> getInputValues = null;
        Map<String, InputDefinition> newInputsByName = null;
        List<String> result;

        // default test
        result = PropertyInstanceMergeDataBuilder
            .getOldGetInputNamesWhichExistInNewVersion(new LinkedList<>(), new HashMap<>());
    }

    @Test
    public void testGetOldDeclaredInputsByUser() throws Exception {
        PropertyInstanceMergeDataBuilder testSubject;
        List<GetInputValueDataDefinition> getInputValues = null;
        Map<String, InputDefinition> oldInputsByName = null;
        List<String> result;

        // default test
        result = PropertyInstanceMergeDataBuilder.getOldDeclaredInputsByUser(new LinkedList<>(), new HashMap<>());
    }
}
