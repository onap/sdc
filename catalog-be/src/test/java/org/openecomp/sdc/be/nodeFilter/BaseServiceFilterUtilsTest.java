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

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.openecomp.sdc.be.components.impl.utils.DirectivesUtils;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;

public class BaseServiceFilterUtilsTest {

    protected Service service;
    protected RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition;
    protected RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition2;
    protected static final String CI_NAME = "AAAAAA";
    protected static final String A_PROP_NAME = "A_PROP";
    protected static final String SIZE_PROP = "size";
    protected static final String ORIG_COMP_INSTANCE_ID = "54355645457457";

    @Before
    public void initService() {
        try {
            service = new Service();
            ComponentInstance componentInstance = new ComponentInstance();
            componentInstance.setUniqueId(CI_NAME);
            componentInstance.setName(CI_NAME);
            service.setComponentInstances(Arrays.asList(componentInstance));
            componentInstance.setDirectives(Arrays.asList(DirectivesUtils.SELECTABLE));
            CINodeFilterDataDefinition serviceFilter = new CINodeFilterDataDefinition();
            componentInstance.setNodeFilter(serviceFilter);
            requirementNodeFilterPropertyDataDefinition = new RequirementNodeFilterPropertyDataDefinition();
            requirementNodeFilterPropertyDataDefinition.setName("Name1");
            requirementNodeFilterPropertyDataDefinition
                    .setConstraints(Arrays.asList("mem_size:\n" + "  equal: { get_property : [" + CI_NAME + ", size]}\n"));
            requirementNodeFilterPropertyDataDefinition2 = new RequirementNodeFilterPropertyDataDefinition();
            requirementNodeFilterPropertyDataDefinition2.setName("Name2");
            requirementNodeFilterPropertyDataDefinition2
                    .setConstraints(Arrays.asList("mem_size:\n {equal:  { get_property : [SELF, "+A_PROP_NAME+"]}}\n"));

            ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> listDataDefinition =
                    new ListDataDefinition<>(Arrays.asList(
                            requirementNodeFilterPropertyDataDefinition,
                            requirementNodeFilterPropertyDataDefinition2));
            serviceFilter.setProperties(listDataDefinition);
            PropertyDefinition property = new PropertyDefinition();
            property.setName(A_PROP_NAME);
            service.setProperties(Arrays.asList(property));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
