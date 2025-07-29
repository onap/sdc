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

package org.openecomp;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.openecomp.sdc.be.togglz.ToggleableFeature;
import org.openecomp.sdcrests.togglz.rest.TogglzFeatures;
import org.openecomp.sdcrests.togglz.rest.mapping.MapToggleableFeatureToDto;
import org.openecomp.sdcrests.togglz.rest.services.TogglzFeaturesImpl;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.springframework.http.ResponseEntity;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TogglzFeatureRestTest {

    @Test
    public void shouldConvertDataProperly() {
        ToggleableFeature tf = mock(ToggleableFeature.class);
        final String TF_NAME = "tf";
        final boolean ACTIVE = true;
        when(tf.name()).thenReturn(TF_NAME);
        when(tf.isActive()).thenReturn(ACTIVE);
        MapToggleableFeatureToDto mapToggleableFeatureToDto = new MapToggleableFeatureToDto();
        FeatureSetDto target = new FeatureSetDto();
        Collection<ToggleableFeature> source = Collections.singletonList(tf);
        mapToggleableFeatureToDto.doMapping(source, target);
        assertEquals(source.size(), target.getFeatures().size());
        FeatureDto result = target.getFeatures().iterator().next();
        assertEquals(TF_NAME, result.getName());
        assertEquals(ACTIVE, result.isActive());

    }

    @Test
    public void shouldGetCurrentTogglzValues() {
        TogglzFeatures togglzFeature = new TogglzFeaturesImpl();
        ResponseEntity response = togglzFeature.getFeatures();
        assertNotNull(response);
        Object entity = response.getBody();
        assertEquals(entity.getClass(), FeatureSetDto.class);
        Set<FeatureDto> features = ((FeatureSetDto) entity).getFeatures();
        assertEquals(features.size(), ToggleableFeature.values().length);
        Set<String> names = Arrays.stream(ToggleableFeature.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> dtoNames = features.stream().map(FeatureDto::getName).collect(Collectors.toSet());
        assertTrue(Sets.symmetricDifference(names, dtoNames).isEmpty());
    }
}
