/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRelationType;

public class MapComponentDependencyModelRequestToEntity extends MappingBase<ComponentDependencyModel, ComponentDependencyModelEntity> {

    @Override
    public void doMapping(ComponentDependencyModel source, ComponentDependencyModelEntity target) {
        target.setSourceComponentId(source.getSourceId());
        target.setTargetComponentId(source.getTargetId());
        try {
            ComponentRelationType.valueOf(source.getRelationType());
            target.setRelation(source.getRelationType());
        } catch (IllegalArgumentException exception) {
            ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getInvalidRelationTypeErrorBuilder();
            throw new CoreException(errorCode, exception);
        }
    }
}
