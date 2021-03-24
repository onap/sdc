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
package org.openecomp.sdc.be.dao.impl.heal;

import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealConstants;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

public class HealVertexGraphDao implements HealGraphDao<GraphVertex, EdgeLabelEnum> {

    private HealingPipelineDao healingPipelineDao;

    public HealVertexGraphDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }

    @Override
    public GraphVertex performGraphReadHealing(GraphVertex childVertex, EdgeLabelEnum edgeLabelEnum) {
        final Integer healingVersionInt = (Integer) childVertex.getMetadataProperties()
            .getOrDefault(GraphPropertyEnum.HEALING_VERSION, HealConstants.DEFAULT_HEAL_VERSION);
        HealVersion<Integer> healingVersion = HealVersionBuilder.build(healingVersionInt);
        healingPipelineDao.getHealersForVertex(edgeLabelEnum.name(), healingVersion).forEach(heal -> healGraphVertex(childVertex, heal));
        childVertex.addMetadataProperty(GraphPropertyEnum.HEALING_VERSION, healingPipelineDao.getCurrentHealVersion().getVersion());
        return childVertex;
    }

    private GraphVertex healGraphVertex(GraphVertex childVertex, Heal<GraphVertex> heal) {
        heal.healData(childVertex);
        final HealVersion<Integer> healVersion = heal.fromVersion();
        HealVersion newerVersion = HealVersionBuilder.build(healVersion.getVersion() + 1);
        childVertex.addMetadataProperty(GraphPropertyEnum.HEALING_VERSION, newerVersion);
        heal.healData(childVertex);
        return childVertex;
    }
}
