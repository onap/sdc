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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

public class ComputeTemplateConsolidationDataTest {

    private static final String PORT_NEUTRON = "port_neutron";
    private ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
    private Map<String, List<String>> mapToBeModified = new HashMap<String, List<String>>() {
        {
            put(PORT_NEUTRON, new ArrayList<>(Collections.singletonList("port_neutron_3")));
        }
    };

    @Test
    public void collectAllPortsOfEachTypeFromComputeKeyPresent() {
        Map<String, List<String>> portMap = new HashMap<>();
        portMap.put(PORT_NEUTRON, new ArrayList<>(Arrays.asList("port_neutron_1", "port_neuton_2")));

        computeTemplateConsolidationData.setPorts(portMap);

        computeTemplateConsolidationData.collectAllPortsOfEachTypeFromCompute(mapToBeModified);

        Assert.assertTrue(mapToBeModified.size() == 1
                                  && mapToBeModified.get(PORT_NEUTRON).size() == 3);

    }

    @Test
    public void collectAllPortsOfEachTypeFromComputeKeyAbsent() {
        Map<String, List<String>> portMap = new HashMap<>();
        portMap.put(PORT_NEUTRON, new ArrayList<>(Arrays.asList("port_neutron_1", "port_neuton_2")));

        computeTemplateConsolidationData.setPorts(portMap);

        Map<String, List<String>> mapToBeModified = new HashMap<>();

        computeTemplateConsolidationData.collectAllPortsOfEachTypeFromCompute(mapToBeModified);

        Assert.assertTrue(mapToBeModified.size() == 1
                                  && Objects.nonNull(mapToBeModified.get(PORT_NEUTRON))
                                  && mapToBeModified.get(PORT_NEUTRON).size() == 2);

    }

    @Test
    public void isNumberOfPortFromEachTypeLegalEmpty() {
        Assert.assertTrue(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void isNumberOfPortFromEachTypeLegalLessThanOne() {
        computeTemplateConsolidationData.setPorts(mapToBeModified);

        Assert.assertTrue(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void isNumberOfPortFromEachTypeLegalGreaterThanOne() {
        mapToBeModified.get(PORT_NEUTRON).addAll(Collections.singletonList("port_neutron_4"));
        computeTemplateConsolidationData.setPorts(mapToBeModified);

        Assert.assertFalse(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void getPortsIdsAndSizeEmpty() {
        Assert.assertNotNull(computeTemplateConsolidationData.getPortsIds());
        Assert.assertTrue(computeTemplateConsolidationData.getNumberOfPorts() == 0);
    }

    @Test
    public void getPortsIdsWithData() {
        computeTemplateConsolidationData.setPorts(mapToBeModified);
        Assert.assertNotNull(computeTemplateConsolidationData.getPortsIds());
        Assert.assertTrue(computeTemplateConsolidationData.getNumberOfPorts() == 1);
    }
}
