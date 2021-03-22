/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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


import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListValidatorTest {

    @Test
    public void isValidTest() {
        Map<String, DataTypeDefinition> map = new HashMap();
        ListValidator validator = new ListValidator();
        assertTrue(validator.isValid("", "", map));
        assertFalse(validator.isValid("test", null, map));

        assertTrue(validator.isValid("[2,3]", "integer", map));
        assertTrue(validator.isValid("[0.2]", "float", map));
        assertTrue(validator.isValid("[true]", "boolean", map));
        assertTrue(validator.isValid("[test]", "string", map));
        assertTrue(validator.isValid("[{\"key\":1};{\"key2\":2}]", "json", map));

        assertFalse(validator.isValid("[[1,2],[3]]", "list", map));
        assertFalse(validator.isValid("[2,true]", "integer", map));
        assertFalse(validator.isValid("test", "wrong", map));
    }
}
