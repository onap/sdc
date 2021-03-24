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

import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealConstants;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.togglz.ToggleableFeature;

public class HealJanusGraphDao implements HealGraphDao<JanusGraphVertex, GraphEdgeLabels> {

    private HealingPipelineDao healingPipelineDao;

    public HealJanusGraphDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }

    @Override
    public JanusGraphVertex performGraphReadHealing(JanusGraphVertex childVertex, GraphEdgeLabels graphEdgeLabels) {
        final Integer healingVersionInt = (Integer) childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty())
            .orElse(HealConstants.DEFAULT_HEAL_VERSION);
        HealVersion<Integer> healingVersion = HealVersionBuilder.build(healingVersionInt);
        healingPipelineDao.getHealersForVertex(graphEdgeLabels.name(), healingVersion).forEach(heal -> healGraphVertex(childVertex, heal));
        childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty(), healingPipelineDao.getCurrentHealVersion().getVersion());
        return childVertex;
    }

    private JanusGraphVertex healGraphVertex(JanusGraphVertex childVertex, Heal<JanusGraphVertex> heal) {
        if (ToggleableFeature.HEALING.isActive()) {
            heal.healData(childVertex);
            final HealVersion<Integer> healVersion = heal.fromVersion();
            HealVersion<Integer> newerVersion = HealVersionBuilder.build(healVersion.getVersion() + 1);
            childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty(), newerVersion);
            heal.healData(childVertex);
        }
        return childVertex;
    }
}
