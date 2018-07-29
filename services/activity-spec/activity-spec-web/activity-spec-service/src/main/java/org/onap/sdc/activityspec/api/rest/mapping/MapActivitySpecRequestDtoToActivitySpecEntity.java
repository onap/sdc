/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.api.rest.mapping;

import org.onap.sdc.activityspec.api.rest.types.ActivitySpecRequestDto;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapActivitySpecRequestDtoToActivitySpecEntity
        extends MappingBase<ActivitySpecRequestDto, ActivitySpecEntity> {

    @Override
    public void doMapping(ActivitySpecRequestDto source, ActivitySpecEntity target) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCategoryList(source.getCategoryList() == null ? new ArrayList<>() : source.getCategoryList());
        if (Objects.nonNull(source.getInputs())) {
            target.setInputs(source.getInputs().stream().map(activitySpecParameterDto -> new MapDtoToActivityParameter()
                                                                                       .applyMapping(
                                                                                          activitySpecParameterDto,
                                                                                          ActivitySpecParameter.class))
                                   .collect(Collectors.toList()));
        }
        if (Objects.nonNull(source.getOutputs())) {
            target.setOutputs(source.getOutputs().stream()
                                    .map(activitySpecParameterDto -> new MapDtoToActivityParameter()
                                                                             .applyMapping(activitySpecParameterDto,
                                                                                     ActivitySpecParameter.class))
                                    .collect(Collectors.toList()));
        }
        target.setType(source.getType());
        target.setContent(source.getContent());
    }
}
