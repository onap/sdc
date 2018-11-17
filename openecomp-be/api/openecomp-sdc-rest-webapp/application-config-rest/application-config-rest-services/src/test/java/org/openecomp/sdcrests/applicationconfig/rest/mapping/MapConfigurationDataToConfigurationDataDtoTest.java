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
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;

/**
 * This class was generated.
 */
public class MapConfigurationDataToConfigurationDataDtoTest {

    @Test
    public void testConversion() {

        final String value = "d7e2a112-5031-49ef-bdc9-98e64080d3bd";
        final long timeStamp = 389988670954481114L;
        final ConfigurationData source = new ConfigurationData(value, timeStamp);
        final ConfigurationDataDto target = new ConfigurationDataDto();
        final MapConfigurationDataToConfigurationDataDto mapper = new MapConfigurationDataToConfigurationDataDto();
        mapper.doMapping(source, target);

        assertEquals(value, target.getValue());
        assertEquals(timeStamp, target.getTimeStamp());
    }
}
