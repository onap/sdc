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

package org.openecomp.sdc.be.components.path;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ForwardingPathRenameNodeTest implements ForwardingPathTestUtils{

    @Test
    public void renameNodeA(){
        ForwardingPathUtils forwardingPathUtils = new ForwardingPathUtils();
        ForwardingPathDataDefinition path = createPath("testPath", "http", "8080", UUID.randomUUID().toString());
        assertTrue(forwardingPathUtils.shouldRenameCI(path, "nodeA"));
        assertTrue(forwardingPathUtils.shouldRenameCI(path, "nodeB"));
        Set<ForwardingPathDataDefinition> updated = forwardingPathUtils.updateComponentInstanceName(Sets.newHashSet(path),"nodeA", "nodeAA");
        assertEquals(1, updated.size());
        ForwardingPathDataDefinition updatedPath = updated.iterator().next();
        assertFalse(forwardingPathUtils.shouldRenameCI(updatedPath, "nodeA"));
        assertTrue(forwardingPathUtils.shouldRenameCI(updatedPath, "nodeB"));
    }

    @Test
    public void cannotRename(){
        ForwardingPathUtils forwardingPathUtils = new ForwardingPathUtils();
        ForwardingPathDataDefinition path = createPath("testPath", "http", "8080", UUID.randomUUID().toString());
        assertTrue(forwardingPathUtils.shouldRenameCI(path, "nodeA"));
        assertTrue(forwardingPathUtils.shouldRenameCI(path, "nodeB"));
        Set<ForwardingPathDataDefinition> updated = forwardingPathUtils.updateComponentInstanceName(Sets.newHashSet(path),"nodeAA", "nodeAAA");
        assertEquals(0, updated.size());
    }
}
