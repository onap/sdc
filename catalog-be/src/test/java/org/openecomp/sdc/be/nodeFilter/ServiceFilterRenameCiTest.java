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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;

class ServiceFilterRenameCiTest extends BaseServiceFilterUtilsTest {

    protected static final String CI_NEW_NAME = "BBBBB";

    @Test
    void renameCI() {
        Map<String, CINodeFilterDataDefinition> renamedNodeFilters = getRenamedNodeFilters(CI_NAME, CI_NEW_NAME);
        assertNotNull(renamedNodeFilters);
        final List<PropertyFilterConstraintDataDefinition> constraints =
                renamedNodeFilters.get(CI_NAME).getProperties().getListToscaDataDefinition().iterator().next()
                                  .getConstraints();
        assertEquals(1, constraints.size());
    }

    private Map<String, CINodeFilterDataDefinition> getRenamedNodeFilters(String oldName, String newName) {
        return ServiceFilterUtils.getRenamedNodesFilter(service, oldName, newName);
    }
}
