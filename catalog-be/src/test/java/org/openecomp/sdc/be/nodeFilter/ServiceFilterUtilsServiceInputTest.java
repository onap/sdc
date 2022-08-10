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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.Test;
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
        requirementNodeFilterPropertyDataDefinition
                .setConstraints(List.of("mem_size:\n  equal: {get_input: " + CONSTRAINT_NAME + "}\n"));
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(constraintName);
        return ServiceFilterUtils.getNodesFiltersToBeDeleted(service, inputDefinition);
    }

    @Test
    public void checkInputStreamIsNOtFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CONSTRAINT_NAME + " aaa bbb");
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.isEmpty());
        assertFalse(nodesFiltersToBeDeleted.contains(CI_NAME));
    }
}
