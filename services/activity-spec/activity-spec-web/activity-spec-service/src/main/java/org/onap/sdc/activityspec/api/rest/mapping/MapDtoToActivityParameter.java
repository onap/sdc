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

import org.onap.sdc.activityspec.api.rest.types.ActivitySpecParameterDto;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapDtoToActivityParameter extends MappingBase<ActivitySpecParameterDto, ActivitySpecParameter> {

    @Override
    public void doMapping(ActivitySpecParameterDto source, ActivitySpecParameter target) {
        target.setName(source.getName());
        target.setType(source.getType());
        target.setValue(source.getValue());
    }
}
