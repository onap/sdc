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

package org.openecomp.sdcrests.common.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdcrests.common.types.ErrorCodeDto;

/**
 * This class was generated.
 */
public class MapErrorCodeToDtoTest {

    @Test()
    public void testConversion() {

        final String id = "f42e101e-595a-4826-bd15-832e5436a527";
        final String message = "45255428-0e3a-4e93-8d40-ac970dce2d48";
        final ErrorCategory category = ErrorCategory.SECURITY;

        final ErrorCode source =
            new ErrorCode.ErrorCodeBuilder().withId(id).withMessage(message).withCategory(category).build();

        final ErrorCodeDto target = new ErrorCodeDto();
        final MapErrorCodeToDto mapper = new MapErrorCodeToDto();
        mapper.doMapping(source, target);

        assertEquals(id, target.getId());
        assertEquals(message, target.getMessage());
        assertEquals(category, target.getCategory());
    }
}
