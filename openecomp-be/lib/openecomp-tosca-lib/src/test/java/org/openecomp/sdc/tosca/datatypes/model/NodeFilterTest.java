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

package org.openecomp.sdc.tosca.datatypes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.CapabilityFilter;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.NodeFilter;
import org.onap.sdc.tosca.services.YamlUtil;

/**
 * @author KATYR
 * @since May 10, 2018
 */

public class NodeFilterTest {

    private static final String GET_INPUT = "get_input";

    @Test
    public void nodeFilterToYamlAndBack() {
        NodeFilter nodeFilter = new NodeFilter();
        nodeFilter.setProperties(createTestProperties());
        nodeFilter.setCapabilities(createTestCapabilities());

        String yamlString = new YamlUtil().objectToYaml(nodeFilter);

        NodeFilter nodeFilterFromYaml =
                new YamlUtil().yamlToObject(yamlString, NodeFilter.class);
        Assert.assertNotNull(nodeFilterFromYaml);
    }


    private List<Map<String, List<Constraint>>> createTestProperties() {
        List<Map<String, List<Constraint>>> propertyFilterDefinitions = new ArrayList<>();
        Map<String, List<Constraint>> pfd1 = new HashMap<>();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(createConstraint("siteName", true));
        constraints.add(createConstraint("siteName1", false));
        pfd1.put("name1", constraints);


        Map<String, List<Constraint>> pfd2 = new HashMap<>();
        List<Constraint> constraints2 = new ArrayList<>();
        constraints2.add(createConstraint("input", true));
        constraints2.add(createConstraint("output", false));
        pfd2.put("name", constraints2);


        propertyFilterDefinitions.add(pfd1);
        propertyFilterDefinitions.add(pfd2);

        return propertyFilterDefinitions;

    }

    private Constraint createConstraint(String constraintParam, boolean createEqualityConstraint) {
        Constraint constraint = new Constraint();

        if (createEqualityConstraint) {
            constraint.setEqual(createValue(constraintParam));
        } else {
            constraint.setGreater_than(createValue(constraintParam));

        }
        return constraint;
    }

    private static Map<String, String> createValue(String propertyName) {
        Map<String, String> getPropertyMap = new HashMap<>();
        getPropertyMap.put(GET_INPUT, propertyName);

        return getPropertyMap;
    }



    private List<Map<String, CapabilityFilter>> createTestCapabilities() {
        List<Map<String, CapabilityFilter>> capabilities = new ArrayList<>();
        Map<String, CapabilityFilter> outerMap1 = new HashMap<>();
        Map<String, List<Constraint>> innerMap1 = new HashMap<>();

        CapabilityFilter filterProperties1 = new CapabilityFilter();
        innerMap1.put("queue_model", createConstraints(3));
        List<Map<String, List<Constraint>>> innerList1 = new ArrayList<>();
        innerList1.add(innerMap1);
        filterProperties1.setProperties(innerList1);
        outerMap1.put("diffserv1", filterProperties1);

        Map<String, CapabilityFilter> outerMap2 = new HashMap<>();
        CapabilityFilter filterProperties2 = new CapabilityFilter();
        Map<String, List<Constraint>> innerMap2 = new HashMap<>();
        Map<String, List<Constraint>> innerMap3 = new HashMap<>();
        innerMap2.put("queue_model", createConstraints(2));
        innerMap3.put("queue_model2", createConstraints(2));
        List<Map<String, List<Constraint>>> innerList2 = new ArrayList<>();
        innerList2.add(innerMap2);
        innerList2.add(innerMap3);
        filterProperties2.setProperties(innerList2);
        outerMap2.put("diffserv2", filterProperties2);

        capabilities.add(outerMap1);
        capabilities.add(outerMap2);


        return capabilities;
    }

    private List<Constraint> createConstraints(int length) {
        List<Constraint> constraints = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (i / 2 == 0) {
                constraints.add(createConstraint("siteName" + UUID.randomUUID().getMostSignificantBits(), true));
            } else {
                constraints.add(createConstraint("siteName" + UUID.randomUUID().getLeastSignificantBits(), false));
            }

        }

        return constraints;

    }

}
