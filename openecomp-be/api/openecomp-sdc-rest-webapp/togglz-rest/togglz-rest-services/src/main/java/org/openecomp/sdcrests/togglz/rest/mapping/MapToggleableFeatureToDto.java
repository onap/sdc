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
package org.openecomp.sdcrests.togglz.rest.mapping;

import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class MapToggleableFeatureToDto extends MappingBase<Collection<ToggleableFeature>, FeatureSetDto> {

    @Override
    public void doMapping(Collection<ToggleableFeature> source, FeatureSetDto target) {
        if (source != null && !source.isEmpty()){
            Set<FeatureDto> fDtos = source.stream().map(f -> new FeatureDto(f.name(), f.isActive()))
                .collect(Collectors.toSet());
            target.setFeatures(fDtos);
        } else {
            target.setFeatures(Collections.emptySet());
        }
    }
}
