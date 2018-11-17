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

package org.openecomp.sdcrests.applicationconfig.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;

/**
 * This class was generated.
 */
public class MapApplicationConfigEntityToApplicationConfigDtoTest {

    @Test
    public void testConversion() {

        final ApplicationConfigEntity source = new ApplicationConfigEntity();

        final String key = "81a540d5-1fd5-4fac-a4f0-90ce6d5b30e1";
        source.setKey(key);

        final String value = "8a77aa41-4c91-480c-b5b3-602f4aabfc39";
        source.setValue(value);

        final ApplicationConfigDto target = new ApplicationConfigDto();
        final MapApplicationConfigEntityToApplicationConfigDto mapper = new MapApplicationConfigEntityToApplicationConfigDto();
        mapper.doMapping(source, target);

        assertEquals(key, target.getKey());
        assertEquals(value, target.getValue());
    }
}
