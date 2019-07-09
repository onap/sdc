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

package org.openecomp.sdcrests.conflict.rest.mapping;

import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.Resolution;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;

/**
 * This class was generated.
 */
public class MapDtoToConflictResolutionTest {

    @Test
    public void testConversion() {

        final ConflictResolutionDto source = new ConflictResolutionDto();
        final Resolution resolution = Resolution.OTHER;
        source.setResolution(resolution);
        final Map<String, Object> otherResolution = Collections.emptyMap();
        source.setOtherResolution(otherResolution);

        final ConflictResolution target = new ConflictResolution();
        final MapDtoToConflictResolution mapper = new MapDtoToConflictResolution();
        mapper.doMapping(source, target);

        assertSame(resolution, target.getResolution());
        assertSame(otherResolution, target.getOtherResolution());
    }
}
