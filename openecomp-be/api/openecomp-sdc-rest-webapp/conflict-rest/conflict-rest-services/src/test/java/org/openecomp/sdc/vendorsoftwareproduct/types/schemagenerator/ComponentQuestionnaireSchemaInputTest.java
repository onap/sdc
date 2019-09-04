/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ComponentQuestionnaireSchemaInputTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        List<String> nicNames = new ArrayList<>();
        Map<String, String> componentQuestionnaireData = new HashMap<>();
        String displayName = "componentDisplayName";
        boolean manual = false;

        ComponentQuestionnaireSchemaInput input =
                new ComponentQuestionnaireSchemaInput(nicNames, componentQuestionnaireData, displayName, manual);
        assertEquals(nicNames, input.getNicNames());
        assertEquals(componentQuestionnaireData, input.getComponentQuestionnaireData());
        assertEquals(displayName, input.getComponentDisplayName());
        assertEquals(manual, input.isManual());

    }
}