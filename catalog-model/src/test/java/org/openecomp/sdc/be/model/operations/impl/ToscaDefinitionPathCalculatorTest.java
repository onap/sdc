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

package org.openecomp.sdc.be.model.operations.impl;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.operations.api.ToscaDefinitionPathCalculator;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ToscaDefinitionPathCalculatorTest {

    private static final String INSTANCE_ID = "123";
    private static final String OWNER_ID = "321";

    private ToscaDefinitionPathCalculator toscaDefinitionPathCalculator;


    @Before
    public void setUp() throws Exception {
        toscaDefinitionPathCalculator = new ToscaDefinitionPathCalculatorImpl();
    }

    @Test
    public void calculatePath_ownerAndComponentInstanceEqual() throws Exception {
        ComponentInstance instance = getComponentInstance(INSTANCE_ID);
        GraphEdge edge = createEdgeWithOwner(INSTANCE_ID);
        List<String> definitionPath = toscaDefinitionPathCalculator.calculateToscaDefinitionPath(instance, edge);
        assertEquals(1, definitionPath.size());
        assertEquals(INSTANCE_ID, definitionPath.get(0));
    }

    @Test
    public void calculatePath() throws Exception {
        ComponentInstance instance = getComponentInstance(INSTANCE_ID);
        GraphEdge edge = createEdgeWithOwner(OWNER_ID);
        List<String> definitionPath = toscaDefinitionPathCalculator.calculateToscaDefinitionPath(instance, edge);
        assertEquals(2, definitionPath.size());
        assertEquals(INSTANCE_ID, definitionPath.get(0));
        assertEquals(OWNER_ID, definitionPath.get(1));

    }


    private ComponentInstance getComponentInstance(String instanceId) {
        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId(instanceId);
        return instance;
    }

    private GraphEdge createEdgeWithOwner(String owner) {
        GraphEdge edge = new GraphEdge();
        edge.setProperties(Collections.singletonMap(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), owner));
        return edge;
    }

}
