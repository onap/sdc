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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;

public class MapCompositionEntityValidationDataToDtoTest {
    private static final String TEST_ID =  "some_test_id";

    @Test
    public void testEntityType() {
        CompositionEntityValidationData source = new CompositionEntityValidationData(CompositionEntityType.compute,TEST_ID);
        CompositionEntityValidationDataDto target = new CompositionEntityValidationDataDto();
        MapCompositionEntityValidationDataToDto mapper = new MapCompositionEntityValidationDataToDto();
        mapper.doMapping(source, target);
        assertEquals(target.getEntityType(), CompositionEntityType.compute);
    }

    @Test
    public void testEntityId() {
        CompositionEntityValidationData source = new CompositionEntityValidationData(CompositionEntityType.compute,TEST_ID);
        CompositionEntityValidationDataDto target = new CompositionEntityValidationDataDto();
        MapCompositionEntityValidationDataToDto mapper = new MapCompositionEntityValidationDataToDto();
        mapper.doMapping(source, target);
        assertEquals(target.getEntityId(), TEST_ID);
    }

    @Test
    public void testEntityName() {
        CompositionEntityValidationData source = new CompositionEntityValidationData(CompositionEntityType.compute,TEST_ID);
        CompositionEntityValidationDataDto target = new CompositionEntityValidationDataDto();
        String name = "some_test_name";
        source.setEntityName(name);
        MapCompositionEntityValidationDataToDto mapper = new MapCompositionEntityValidationDataToDto();
        mapper.doMapping(source, target);
        assertEquals(target.getEntityName(), name);
    }

    @Test
    public void testErrors() {
        CompositionEntityValidationData source = new CompositionEntityValidationData(CompositionEntityType.compute,TEST_ID);
        CompositionEntityValidationDataDto target = new CompositionEntityValidationDataDto();
        Collection<String> errors = Arrays.asList("some_test_id_1","some_test_id_2","some_test_id_3");
        source.setErrors(errors);
        MapCompositionEntityValidationDataToDto mapper = new MapCompositionEntityValidationDataToDto();
        mapper.doMapping(source, target);
        assertEquals(target.getErrors(), errors);
    }
}
