package org.openecomp;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdcrests.togglz.rest.ITogglzFeature;
import org.openecomp.sdcrests.togglz.rest.services.TogglzFeatureImpl;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TogglzFeatureRestTest {

    @Test
    public void shouldgetCurrentTogglzValues() {
        ITogglzFeature togglzFeature = new TogglzFeatureImpl();
        Response response = togglzFeature.getFeatures();
        assertNotNull(response);
        Object entity = response.getEntity();
        if (ToggleableFeature.values() == null || ToggleableFeature.values().length == 0) {
            // enumeration is empty. does not expect any result
            return;
        }
        assertEquals(entity.getClass(), FeatureSetDto.class);
        Set<FeatureDto> features = ((FeatureSetDto) entity).getFeatures();
        assertEquals(features.size(), ToggleableFeature.values().length);
        Set<String> names = Arrays.asList(ToggleableFeature.values()).stream().map(toggleableFeature -> toggleableFeature.name()).collect(Collectors.toSet());
        Set<String> dtoNames = features.stream().map(featureDto -> featureDto.getName()).collect(Collectors.toSet());
        assertTrue(Sets.symmetricDifference(names, dtoNames).isEmpty());
    }
}