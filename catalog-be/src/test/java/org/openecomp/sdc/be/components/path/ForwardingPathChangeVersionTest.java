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

package org.openecomp.sdc.be.components.path;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
public class ForwardingPathChangeVersionTest extends BaseForwardingPathVersionChangeTest {


    @Test
    public void updateForwardingPath() {
        Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> pair = new ForwardingPathUtils()
            .updateForwardingPathOnVersionChange(service,dataHolder,newNodeAC, newNodeA);
        Map<String, ForwardingPathDataDefinition> updated = pair.getValue0();
        assertNotNull(updated);
        assertEquals(1, updated.size());
        assertEquals(newNodeA, updated.values().iterator().next().getPathElements().getListToscaDataDefinition().get(0).getFromNode());
        Map<String, ForwardingPathDataDefinition> deleted = pair.getValue1();
        assertNotNull(deleted);
        assertEquals(0, deleted.size());
    }

    @Test
    public void deleteForwardingPath(){
        newNodeAC.setCapabilities(new HashMap<>());
        Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> pair = new ForwardingPathUtils()
            .updateForwardingPathOnVersionChange(service,dataHolder,newNodeAC, newNodeA);
        Map<String, ForwardingPathDataDefinition> updated = pair.getValue0();
        assertNotNull(updated);
        assertEquals(0, updated.size());

        Map<String, ForwardingPathDataDefinition> deleted = pair.getValue1();
        assertNotNull(deleted);
        assertEquals(1, deleted.size());
        assertEquals(FPId, deleted.keySet().stream().findAny().get());
    }

    @Test
    public void fetchPathsToBeDeletedZeroPaths(){
        Set<String> data=new ForwardingPathUtils().getForwardingPathsToBeDeletedOnVersionChange(service,
            dataHolder,newNodeAC);
        assertEquals(0,data.size());
    }

    @Test
    public void fetchPathsToBeDeleted(){
        Set<String> data=new ForwardingPathUtils().getForwardingPathsToBeDeletedOnVersionChange(service,
            dataHolder,newNodeWithoutCapability);
        assertEquals(1,data.size());
    }


}
