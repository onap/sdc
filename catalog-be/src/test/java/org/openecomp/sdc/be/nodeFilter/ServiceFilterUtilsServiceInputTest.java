/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.nodeFilter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;
import org.openecomp.sdc.be.model.InputDefinition;

public class ServiceFilterUtilsServiceInputTest extends BaseServiceFilterUtilsTest {

    private static final String CONSTRAINT_NAME = "InputName";


    @Test
    public void checkInputStreamIsFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CONSTRAINT_NAME);
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.contains(CI_NAME));
    }

    private Set<String> getNodeFiltersToBeDeleted(String constraintName) {
        final var propertyFilterConstraint = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint.setPropertyName("mem_size");
        propertyFilterConstraint.setOperator(ConstraintType.EQUAL);

        propertyFilterConstraint.setValue(
            createToscaGetFunction(PropertySource.SELF.getName(), ToscaGetFunctionType.GET_INPUT, List.of(CONSTRAINT_NAME))
        );
        propertyFilterConstraint.setValueType(FilterValueType.GET_INPUT);
        propertyFilterConstraint.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition.setConstraints(List.of(propertyFilterConstraint));
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(constraintName);
        return ServiceFilterUtils.getNodesFiltersToBeDeleted(service, inputDefinition);
    }

    @Test
    public void checkInputStreamIsNotFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CONSTRAINT_NAME + " aaa bbb");
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.isEmpty());
    }
}
