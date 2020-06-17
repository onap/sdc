/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.togglz.rest.services;
import org.openecomp.sdcrests.togglz.rest.TogglzFeatures;
import org.openecomp.sdcrests.togglz.rest.mapping.MapToggleableFeatureToDto;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.openecomp.sdc.be.togglz.ToggleableFeature;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Named
@Service("TogglzFeature")
@Scope(value = "prototype")
public class TogglzFeaturesImpl implements TogglzFeatures {

    @Override
    public Response getFeatures() {
        FeatureSetDto featureSetDto = new FeatureSetDto();
        new MapToggleableFeatureToDto().doMapping(Arrays.asList(ToggleableFeature.values()), featureSetDto);
        return Response.ok(featureSetDto).build();
    }

    @Override
    public Response setAllFeatures(boolean active) {
        FeatureSetDto featureSetDto = new FeatureSetDto();
        new MapToggleableFeatureToDto().doMapping(Arrays.asList(ToggleableFeature.values()), featureSetDto);
        featureSetDto.getFeatures().forEach(featureDto -> {
            Feature feature = new NamedFeature(featureDto.getName());
            FeatureState featureState = new FeatureState(feature,active);
            FeatureContext.getFeatureManager().setFeatureState(featureState);
        });
        return Response.ok().build();
    }

    @Override
    public Response setFeatureState(String featureName, boolean active) {
        Feature feature = new NamedFeature(featureName);
        FeatureState featureState = new FeatureState(feature,active);
        FeatureContext.getFeatureManager().setFeatureState(featureState);
        return Response.ok().build();
    }

    @Override
    public Response getFeatureState(String featureName) {
        boolean active = ToggleableFeature.valueOf(featureName).isActive();
        FeatureDto featureDto = new FeatureDto(featureName,active);

        return Response.ok(featureDto).build();
    }

}

