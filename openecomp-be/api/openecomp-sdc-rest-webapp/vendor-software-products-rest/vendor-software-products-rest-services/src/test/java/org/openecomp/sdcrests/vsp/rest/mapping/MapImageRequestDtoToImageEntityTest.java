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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;

/**
 * This class was generated.
 */
public class MapImageRequestDtoToImageEntityTest {

    @Test()
    public void testConversion() {

        final ImageRequestDto source = new ImageRequestDto();

        final String fileName = "e32b7688-972e-4c99-8343-9c21fbbb5261";
        source.setFileName(fileName);

        final String description = "5be79c85-3fec-4a5e-b757-846203f4ea09";
        source.setDescription(description);

        final ImageEntity target = new ImageEntity();
        final MapImageRequestDtoToImageEntity mapper = new MapImageRequestDtoToImageEntity();
        mapper.doMapping(source, target);

        Image image = target.getImageCompositionData();
        assertEquals(fileName, image.getFileName());
        assertEquals(description, image.getDescription());
    }
}
