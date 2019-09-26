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
package org.onap.sdc.tosca.datatypes.model;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class PropertyDefinitionTest {

    @Test
    public void cloneTest() {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setRequired(false);
        propertyDefinition.setStatus(Status.DEPRECATED.getName());
        Constraint constraint = new Constraint();
        constraint.setEqual("123");
        ArrayList<Constraint> constraints = new ArrayList<>();
        constraints.add(constraint);
        propertyDefinition.setConstraints(constraints);

        PropertyDefinition propertyDefinitionClone = propertyDefinition.clone();
        Assert.assertEquals(propertyDefinition.getRequired(), propertyDefinitionClone.getRequired());
        Assert.assertEquals(propertyDefinition.getStatus(),
                propertyDefinitionClone.getStatus());
        Assert.assertEquals(propertyDefinition.getConstraints().get(0).getEqual(),
                propertyDefinitionClone.getConstraints().get(0).getEqual());
    }


}
