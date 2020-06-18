/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.tosca.datatypes.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstraintTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(Constraint.class, hasValidGettersAndSettersExcluding("in_range"));
    }

    @Test
    public void setInRangeTest() {
        Constraint constraint = new Constraint();
        Object[] tmpInRange = new Object[] {"str1","str2","str3"};
        constraint.setIn_range(tmpInRange);
        assertEquals(constraint.getIn_range().length, 2);
        assertEquals(constraint.getIn_range()[0], "str1");
        assertEquals(constraint.getIn_range()[1], "str2");
    }
}
