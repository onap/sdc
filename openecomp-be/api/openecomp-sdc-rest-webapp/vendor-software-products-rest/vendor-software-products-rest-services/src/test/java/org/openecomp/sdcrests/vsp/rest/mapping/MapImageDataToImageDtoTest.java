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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ImageData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;

/**
 * This class was generated.
 */
public class MapImageDataToImageDtoTest {

    @Test()
    public void testConversion() {

        final ImageData source = new ImageData();

        final String fileName = "4a71a5bf-bfee-4060-8d43-4f90215b5ce7";
        source.setFileName(fileName);

        final String description = "759ff9e4-cc83-4b66-b507-556d98c081e6";
        source.setDescription(description);

        final ImageDto target = new ImageDto();
        final MapImageDataToImageDto mapper = new MapImageDataToImageDto();
        mapper.doMapping(source, target);

        assertEquals(fileName, target.getFileName());
        assertEquals(description, target.getDescription());
    }
}
