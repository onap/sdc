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

package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.aUniqueId;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.alphaNum;
import static org.openecomp.sdc.be.tosca.ComponentCache.entry;

import io.vavr.collection.List;
import java.util.function.BinaryOperator;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.utils.TestDataUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.tosca.ComponentCache.CacheEntry;

public class ComponentCacheTest {
    private static final BinaryOperator<CacheEntry> RETURN_LATEST = (oldValue, newValue) -> newValue;
    private static final BinaryOperator<CacheEntry> NO_MERGE = (oldValue, newValue) ->  {
        fail("Should not merge");
        return oldValue;
    };

    @Test
    public void emptyCase() {
        ComponentCache cache = ComponentCache.overwritable(NO_MERGE);
        List<CacheEntry> actual = cache.all().toList();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void emptyCacheShouldNotMerge() {
        ComponentCache cache = ComponentCache.overwritable(NO_MERGE);

        String id = alphaNum(10);
        String fileName = alphaNum(10);
        Component component = aComponent();

        List<CacheEntry> actual = cache
            .put(id, fileName, component)
            .all().toList();

        List<CacheEntry> expected = List.of(entry(id, fileName, component));
        assertEquals(expected, actual);
    }

    @Test
    public void nonEmptyCacheShouldMerge() {
        ComponentCache cache = ComponentCache.overwritable(RETURN_LATEST);

        String id = alphaNum(10);
        String fileName = alphaNum(10);
        String invariantId = aUniqueId();

        Component oldComponent = aComponent(invariantId);
        Component newComponent = aComponent(invariantId);

        CacheEntry actual = cache
            .put(id, fileName, oldComponent)
            .put(id, fileName, newComponent)
            .all().toList().head();

        assertEquals(newComponent, actual.component);
        assertEquals(1, cache.all().size());
    }

    private static Component aComponent() {
        return aComponent(aUniqueId());
    }

    private static Component aComponent(String invariantId) {
        Component component = TestDataUtils.aComponent();
        component.setInvariantUUID(invariantId);
        return component;
    }
}

