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

package org.openecomp.sdc.be.model.tosca.validators;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;

public class MapValidatorTest {

    @Test
    public void isValid_nonMapString() {
        assertFalse(MapValidator.getInstance().isValid("abc", "string", Collections.emptyMap()));
        assertFalse(MapValidator.getInstance().isValid("1", "string", Collections.emptyMap()));
    }
}
