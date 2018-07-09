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

package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import static org.testng.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupDescriptorDto;


public class MapFeatureGroupDescriptorDtoToFeatureGroupEntityTest {

    @Test
    public void testName() {
        FeatureGroupDescriptorDto source = new FeatureGroupDescriptorDto();
        FeatureGroupEntity target = new FeatureGroupEntity();
        MapFeatureGroupDescriptorDtoToFeatureGroupEntity mapper =
                new MapFeatureGroupDescriptorDtoToFeatureGroupEntity();
        String param = "bf328e2e-8c77-440b-8485-d6a5a54e604a";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testDescription() {
        FeatureGroupDescriptorDto source = new FeatureGroupDescriptorDto();
        FeatureGroupEntity target = new FeatureGroupEntity();
        MapFeatureGroupDescriptorDtoToFeatureGroupEntity mapper =
                new MapFeatureGroupDescriptorDtoToFeatureGroupEntity();
        String param = "6361dbd3-07e5-40db-bffc-00aaf0c1e16e";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testPartNumber() {
        FeatureGroupDescriptorDto source = new FeatureGroupDescriptorDto();
        FeatureGroupEntity target = new FeatureGroupEntity();
        MapFeatureGroupDescriptorDtoToFeatureGroupEntity mapper =
                new MapFeatureGroupDescriptorDtoToFeatureGroupEntity();
        String param = "4a99afe6-8493-42d3-92df-62c03dd1d55e";
        source.setPartNumber(param);
        mapper.doMapping(source, target);
        assertEquals(target.getPartNumber(), param);
    }
}
