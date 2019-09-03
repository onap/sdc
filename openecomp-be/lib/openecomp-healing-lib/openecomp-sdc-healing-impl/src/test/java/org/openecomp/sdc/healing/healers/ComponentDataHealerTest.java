/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.healing.healers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

@RunWith(MockitoJUnitRunner.class)
public class ComponentDataHealerTest {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String VERSION_STRING = "1.1";

    @Mock
    private ComponentDao componentDao;

    @Test
    public void shouldCheckIsHealingNeededIfNullCompositions() {
        ComponentDataHealer componentDataHealer = new ComponentDataHealer(componentDao);
        Mockito.when(componentDao.listCompositionAndQuestionnaire(Mockito.any(),Mockito.any())).thenReturn(null);
        boolean healingNeeded = componentDataHealer.isHealingNeeded(ITEM_ID, Version.valueOf(VERSION_STRING));
        assertFalse(healingNeeded);
    }

    @Test
    public void shouldCheckIsHealingNeededIfEmptyCompositions() {
        ComponentDataHealer componentDataHealer = new ComponentDataHealer(componentDao);
        Mockito.when(componentDao.listCompositionAndQuestionnaire(Mockito.any(),Mockito.any())).thenReturn(Collections.emptyList());
        boolean healingNeeded = componentDataHealer.isHealingNeeded(ITEM_ID, Version.valueOf(VERSION_STRING));
        assertFalse(healingNeeded);
    }

    @Test
    public void shouldCheckIsHealingNeeded() {
        ComponentDataHealer componentDataHealer = new ComponentDataHealer(componentDao);
        List<ComponentEntity> compositions = new ArrayList<>();
        ComponentEntity componentEntity = new ComponentEntity();
        componentEntity.setCompositionData("{ \"vfcCode\": 1, \"nfcFunction\": 2 }");
        compositions.add(componentEntity);
        Mockito.when(componentDao.listCompositionAndQuestionnaire(Mockito.any(),Mockito.any())).thenReturn(compositions);
        boolean healingNeeded = componentDataHealer.isHealingNeeded(ITEM_ID, Version.valueOf(VERSION_STRING));
        assertTrue(healingNeeded);
    }

    @Test
    public void shouldHeal() throws Exception {
        ComponentDataHealer componentDataHealer = new ComponentDataHealer(componentDao);
        List<ComponentEntity> compositions = new ArrayList<>();
        ComponentEntity componentEntity = new ComponentEntity();
        componentEntity.setCompositionData("{ \"vfcCode\": 1, \"nfcFunction\": 2 }");
        componentEntity.setQuestionnaireData("{}");
        compositions.add(componentEntity);
        Mockito.when(componentDao.listCompositionAndQuestionnaire(Mockito.any(),Mockito.any())).thenReturn(compositions);
        componentDataHealer.heal(ITEM_ID, Version.valueOf(VERSION_STRING));
        Mockito.verify(componentDao).update(Mockito.any());
        Mockito.verify(componentDao).updateQuestionnaireData(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}