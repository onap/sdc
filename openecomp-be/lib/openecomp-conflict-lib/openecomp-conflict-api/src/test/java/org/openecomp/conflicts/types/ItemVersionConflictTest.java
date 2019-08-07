/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.conflicts.types;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ItemVersionConflictTest {

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ItemVersionConflict.class, hasValidGettersAndSetters());
    }

    @Test
    public void shouldAddItemToList() {
        ItemVersionConflict itemVersionConflict = new ItemVersionConflict();
        ConflictInfo conflictInfo = new ConflictInfo();
        itemVersionConflict.addElementConflictInfo(conflictInfo);
        assertEquals(itemVersionConflict.getElementConflicts().size(),1 );
        assertTrue(itemVersionConflict.getElementConflicts().contains(conflictInfo));
    }
}