package org.openecomp;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdcrests.togglz.rest.TogglzFeatures;
import org.openecomp.sdcrests.togglz.rest.mapping.MapToggleableFeatureToDto;
import org.openecomp.sdcrests.togglz.rest.services.TogglzFeaturesImpl;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.togglz.core.annotation.Label;
import org.openecomp.sdc.common.togglz.ToggleStatus;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TogglzFeatureRestTest {

    @Test
    public void shouldConvertDataProperly() {
        ToggleStatus tf = mock(ToggleStatus.class);
        final String TF_NAME = "tf";
        final boolean ACTIVE = true;
        when(tf.name()).thenReturn(TF_NAME);
        when(tf.isActive()).thenReturn(ACTIVE);
        MapToggleableFeatureToDto mapToggleableFeatureToDto = new MapToggleableFeatureToDto();
        FeatureSetDto target = new FeatureSetDto();
        Collection<ToggleStatus> source = Arrays.asList(tf);
        mapToggleableFeatureToDto.doMapping(source, target);
        assertEquals(source.size(), target.getFeatures().size());
        FeatureDto result = target.getFeatures().iterator().next();
        assertEquals(TF_NAME, result.getName());
        assertEquals(ACTIVE, result.isActive());

    }

    @Test
    public void shouldGetCurrentTogglzValues() {
        TogglzFeatures togglzFeature = new TogglzFeaturesImpl();
        Response response = togglzFeature.getFeatures();
        assertNotNull(response);
        Object entity = response.getEntity();
        assertEquals(entity.getClass(), FeatureSetDto.class);
        Set<FeatureDto> features = ((FeatureSetDto) entity).getFeatures();
        assertEquals(features.size(), ToggleableFeature.values().length);
        Set<String> names = Arrays.asList(ToggleableFeature.values()).stream().map(toggleableFeature -> toggleableFeature.name()).collect(Collectors.toSet());
        Set<String> dtoNames = features.stream().map(featureDto -> featureDto.getName()).collect(Collectors.toSet());
        assertTrue(Sets.symmetricDifference(names, dtoNames).isEmpty());
    }
}