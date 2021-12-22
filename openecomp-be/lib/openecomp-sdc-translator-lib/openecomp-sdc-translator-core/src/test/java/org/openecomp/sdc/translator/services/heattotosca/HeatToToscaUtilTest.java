/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdc.translator.services.heattotosca;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;
public class HeatToToscaUtilTest {
    @Mock
    private static HeatOrchestrationTemplate heatOrchestrationTemplate;

    @Mock
    private static TranslationContext translationContext;

    @Test
    public void testExtractAttachedResourceIdReturnsTranslatedId() {

        Map<String, Object> propMap = new HashedMap<>();
        propMap.put("get_resource", "test");
        MockedStatic<ResourceTranslationBase> mockedStatic = Mockito.mockStatic(ResourceTranslationBase.class);
        Mockito.when(ResourceTranslationBase.getResourceTranslatedId(
            "heatFileName",
            heatOrchestrationTemplate,
            "test",
            translationContext)).thenReturn(Optional.of("translatedResourceId"));

        assertTrue(HeatToToscaUtil.extractAttachedResourceId("heatFileName",
            heatOrchestrationTemplate,
            translationContext,
            propMap).isPresent());

        assertEquals("translatedResourceId", HeatToToscaUtil.extractAttachedResourceId("heatFileName",
            heatOrchestrationTemplate,
            translationContext,
            propMap).get().getTranslatedId());

        mockedStatic.clearInvocations();
        mockedStatic.close();
    }

    @Test
    public void testExtractAttachedResourceIdReturnsNullTranslatedId() {

        Map<String, Object> propMap = new HashedMap<>();
        propMap.put("get_resource", "test");
        MockedStatic<ResourceTranslationBase> mockedStaticResourceTranslationBase = Mockito.mockStatic(ResourceTranslationBase.class);
        MockedStatic<FunctionTranslationFactory> mockedStaticFunctionTranslationFactory = Mockito.mockStatic(FunctionTranslationFactory.class);
        Mockito.when(ResourceTranslationBase.getResourceTranslatedId(
            "heatFileName",
            heatOrchestrationTemplate,
            "test",
            translationContext)).thenReturn(Optional.of("translatedResourceId"));
        Mockito.when(FunctionTranslationFactory.getInstance(anyString())).thenReturn(Optional.empty());

        assertTrue(HeatToToscaUtil.extractAttachedResourceId("heatFileName",
            heatOrchestrationTemplate,
            translationContext,
            propMap).isPresent());

        assertEquals(null, HeatToToscaUtil.extractAttachedResourceId("heatFileName",
            heatOrchestrationTemplate,
            translationContext,
            propMap).get().getTranslatedId());

        mockedStaticResourceTranslationBase.close();
        mockedStaticFunctionTranslationFactory.close();
    }

    @Test
    void testisNestedVfcResourceReturnsNestedHeatFileName() {
        final var resource = new Resource();
        resource.setType(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.name());
        assertFalse(HeatToToscaUtil.isNestedVfcResource(resource, translationContext));
    }
}
