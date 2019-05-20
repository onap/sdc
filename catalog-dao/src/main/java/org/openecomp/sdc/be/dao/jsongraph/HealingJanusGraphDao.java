/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.be.dao.jsongraph;

import fj.data.Either;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("janusgraph-dao")
public class HealingJanusGraphDao extends JanusGraphDao {


    @Autowired
    private HealingPipelineDao healingPipelineDao;


    public HealingJanusGraphDao(JanusGraphClient janusGraphClient) {
        super(janusGraphClient);
    }

    @Override
    public Either<List<GraphVertex>, JanusGraphOperationStatus> getChildrenVertecies(GraphVertex parentVertex,
                                                                                     EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        Either<List<GraphVertex>, JanusGraphOperationStatus> childrenVertecies =
                super.getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
        return Either.iif(childrenVertecies.isRight(), () -> childrenVertecies.right().value(),
                () -> childrenVertecies.left().value().stream()
                              .map(graphVertex -> transformVertex(graphVertex, edgeLabel))
                              .collect(Collectors.toList()));
    }

    private GraphVertex transformVertex(GraphVertex graphVertex, EdgeLabelEnum edgeLabelEnum) {
        Optional<GraphVertex> optional = healingPipelineDao.performGraphReadHealing(graphVertex, edgeLabelEnum);
        return optional.orElse(graphVertex);
    }


    public void setHealingPipelineDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }

}
