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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class PropertyInstanceMergeDataBuilderTest {

    @Test
    public void testBuildDataForMerging() throws Exception {
        final InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName("mock");
        final List<GetInputValueDataDefinition> inputValues = new ArrayList<>();
        inputValues.add(new GetInputValueDataDefinition());
        inputDefinition.setGetInputValues(inputValues);

        final List<PropertyDataDefinition> oldProps = new ArrayList<>();
        final List<InputDefinition> oldInputs = new ArrayList<>();
        oldInputs.add(inputDefinition);
        oldProps.addAll(oldInputs);

        final List<PropertyDataDefinition> newProps = new ArrayList<>();
        final List<InputDefinition> newInputs = new ArrayList<>();
        newInputs.add(inputDefinition);
        newProps.addAll(oldInputs);

        final List<MergePropertyData> result = PropertyInstanceMergeDataBuilder.buildDataForMerging(oldProps, oldInputs, newProps, newInputs);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertNotNull(result.get(0).getOldProp());
        Assertions.assertTrue(result.get(0).getOldProp() instanceof PropertyDefinition);
        Assertions.assertNotNull(result.get(0).getNewProp());
        Assertions.assertTrue(result.get(0).getNewProp() instanceof PropertyDefinition);
    }

}
