/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.conflicts.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflictInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.versioning.dao.types.Version;

@RunWith(MockitoJUnitRunner.class)
public class ItemVersionConflictConvertorFromZusammenTest {

    private static final String ELEMENT_TYPE = "elementType";
    private static final String ITEM_VERSION = "itemVersion";
    private static final String ITEMID = "ITEMID";

    @Mock
    private Version version;
    @Mock
    private ItemVersionConflict itemVersionConflict;
    @Mock
    private ItemVersionDataConflict itemVersionDataConflict;
    private Collection<ElementConflictInfo> infos = new ArrayList<>();

    @Test
    public void shouldConvertFromZusammenTest() {
        prepareMocks();
        org.openecomp.conflicts.types.ItemVersionConflict converted = new ItemVersionConflictConvertorFromZusammen()
            .convert(ITEMID, version, itemVersionConflict);
        Assert.assertEquals(converted.getElementConflicts().size(), 1);
        Assert.assertNotNull(converted.getVersionConflict());
    }

    private void prepareMocks() {
        ElementConflictInfo elementConflictInfo = new ElementConflictInfo();
        ElementInfo remoteElementInfo = new ElementInfo();
        remoteElementInfo.setId(new Id());
        Info info = new Info();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(ELEMENT_TYPE, ITEM_VERSION);
        info.setProperties(properties);
        remoteElementInfo.setInfo(info);
        elementConflictInfo.setRemoteElementInfo(remoteElementInfo);
        infos.add(elementConflictInfo);
        Mockito.when(itemVersionConflict.getElementConflictInfos()).thenReturn(infos);
        Mockito.when(itemVersionConflict.getVersionDataConflict()).thenReturn(itemVersionDataConflict);
    }
}