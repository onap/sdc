/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.openecomp.sdc.versioning.types;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ItemTest {

    @Test
    public void testAddVersionStatus() {
        Item item = new Item();
        item.addVersionStatus(VersionStatus.Draft);
        Assert.assertEquals(1, item.getVersionStatusCounters().size());
    }

    @Test
    public void testAddProperty() {
        Item item = new Item();
        item.setProperties(new HashMap<>());
        item.addProperty("item1", new Object());
        Assert.assertEquals(1, item.getProperties().size());
    }
    @Test
    public void testRemoveVersionStatus() {
        Item item = new Item();
        Map<VersionStatus, Integer> versionStatusCounters = new EnumMap<>(VersionStatus.class);
        versionStatusCounters.put(VersionStatus.Draft, 1);
        item.setVersionStatusCounters(versionStatusCounters);
        item.removeVersionStatus(VersionStatus.Draft);
        Assert.assertEquals(0, item.getVersionStatusCounters().size());
    }

}
