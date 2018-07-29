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

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupDescriptorDto;

import static org.testng.Assert.assertEquals;


public class MapFeatureGroupEntityToFeatureGroupDescriptorDtoTest {

    @Test
    public void testDescription() {
        FeatureGroupEntity source = new FeatureGroupEntity();
        FeatureGroupDescriptorDto target = new FeatureGroupDescriptorDto();
        MapFeatureGroupEntityToFeatureGroupDescriptorDto mapper =
                new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
        String param = "0173a1e0-f713-413c-854b-152d35228616";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testName() {
        FeatureGroupEntity source = new FeatureGroupEntity();
        FeatureGroupDescriptorDto target = new FeatureGroupDescriptorDto();
        MapFeatureGroupEntityToFeatureGroupDescriptorDto mapper =
                new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
        String param = "d0a3212d-06c6-455e-ab36-b1fd63eefeca";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testPartNumber() {
        FeatureGroupEntity source = new FeatureGroupEntity();
        FeatureGroupDescriptorDto target = new FeatureGroupDescriptorDto();
        MapFeatureGroupEntityToFeatureGroupDescriptorDto mapper =
                new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
        String param = "562265f0-abe0-44e9-9ee4-3cf2f5436ea9";
        source.setPartNumber(param);
        mapper.doMapping(source, target);
        assertEquals(target.getPartNumber(), param);
    }
}
