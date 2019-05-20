/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("derived-resource-resolver")
public class ByToscaNameDerivedNodeTypeResolver implements DerivedNodeTypeResolver {

    @Autowired
    private JanusGraphDao janusGraphDao;

    @Override
    public Either<List<GraphVertex>, JanusGraphOperationStatus> findDerivedResources(String parentResource) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<>();
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, parentResource);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        return janusGraphDao
            .getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, JsonParseFlagEnum.NoParse);
    }
}
