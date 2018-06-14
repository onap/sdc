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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;

public class FilePortConsolidationDataTest {

    private static final String PORT_NODE_TEMPLATE_ID_1 = "portNodeTemplateId1";
    private static final String PORT_NODE_TEMPLATE_ID_2 = "portNodeTemplateId2";
    private static final String PORT_RESOURCE_ID_1 = "pcm_port_01";
    private static final String PORT_RESOURCE_ID_2 = "pcm_port_02";
    private static final String PORT_RESOURCE_TYPE = HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource();

    private final FilePortConsolidationData consolidationData = new FilePortConsolidationData();

    @Test
    public void testAddPortConsolidationData() {
        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_1,
                PORT_RESOURCE_TYPE);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(PORT_NODE_TEMPLATE_ID_1);
        checkComputeConsolidationData(consolidationData, PORT_NODE_TEMPLATE_ID_1, expectedComputeNodeTypes);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_1,
                PORT_RESOURCE_TYPE);
        PortTemplateConsolidationData firstPortTemplateConsolidationData =
                consolidationData.getPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1);

        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_1,
                PORT_RESOURCE_TYPE);
        PortTemplateConsolidationData secondPortTemplateConsolidationData =
                consolidationData.getPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1);
        Assert.assertEquals(firstPortTemplateConsolidationData, secondPortTemplateConsolidationData);
    }

    @Test
    public void testAddDiffConsolidationData_diffNodeTemplateIds() {
        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_1,
                PORT_RESOURCE_TYPE);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(PORT_NODE_TEMPLATE_ID_1);
        checkComputeConsolidationData(consolidationData, PORT_NODE_TEMPLATE_ID_1, expectedComputeNodeTypes);

        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_2, PORT_RESOURCE_ID_2,
                PORT_RESOURCE_TYPE);
        expectedComputeNodeTypes.add(PORT_NODE_TEMPLATE_ID_2);
        checkComputeConsolidationData(consolidationData, PORT_NODE_TEMPLATE_ID_2, expectedComputeNodeTypes);
    }

    @Test
    public void testAddDiffConsolidationData_diffResourceIds() {
        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_1,
                PORT_RESOURCE_TYPE);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(PORT_NODE_TEMPLATE_ID_1);
        checkComputeConsolidationData(consolidationData, PORT_NODE_TEMPLATE_ID_1, expectedComputeNodeTypes);

        consolidationData.addPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_1, PORT_RESOURCE_ID_2,
                PORT_RESOURCE_TYPE);
        checkComputeConsolidationData(consolidationData, PORT_NODE_TEMPLATE_ID_1, expectedComputeNodeTypes);
    }

    private void checkComputeConsolidationData(FilePortConsolidationData filePortConsolidationData,
            String computeNodeTemplateId, Set<String> expectedPortNodeTemplateIds) {
        PortTemplateConsolidationData consolidationData =
                filePortConsolidationData.getPortTemplateConsolidationData(computeNodeTemplateId);
        Assert.assertNotNull(consolidationData);
        checkGetTemplateIds(filePortConsolidationData, expectedPortNodeTemplateIds);
    }

    private void checkGetTemplateIds(FilePortConsolidationData filePortConsolidationData,
            Set<String> expectedPortNodeTemplateIds) {
        Collection<String> allPortNodeTemplateIds = filePortConsolidationData.getAllPortNodeTemplateIds();
        Assert.assertNotNull(allPortNodeTemplateIds);
        Assert.assertEquals(allPortNodeTemplateIds.size(), expectedPortNodeTemplateIds.size());
        Assert.assertTrue(allPortNodeTemplateIds.containsAll(expectedPortNodeTemplateIds));
    }
}
