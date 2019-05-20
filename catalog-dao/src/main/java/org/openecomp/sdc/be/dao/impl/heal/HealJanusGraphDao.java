package org.openecomp.sdc.be.dao.impl.heal;

import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealConstants;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

public class HealJanusGraphDao implements HealGraphDao<JanusGraphVertex, GraphEdgeLabels> {

    private HealingPipelineDao healingPipelineDao;

    public HealJanusGraphDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }

    @Override
    public JanusGraphVertex performGraphReadHealing(JanusGraphVertex childVertex, GraphEdgeLabels graphEdgeLabels) {
        final Integer healingVersionInt = (Integer) childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty()).orElse(HealConstants.DEFAULT_HEAL_VERSION);
        HealVersion<Integer> healingVersion = HealVersionBuilder.build(healingVersionInt);
        healingPipelineDao.getHealersForVertex(graphEdgeLabels.name(), healingVersion).forEach(heal -> healGraphVertex(childVertex, heal));
        childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty(), healingPipelineDao.getCurrentHealVersion().getVersion());
        return childVertex;
    }


    private JanusGraphVertex healGraphVertex(JanusGraphVertex childVertex, Heal<JanusGraphVertex> heal) {
        heal.healData(childVertex);
        final HealVersion<Integer> healVersion = heal.fromVersion();
        HealVersion newerVersion = HealVersionBuilder.build(healVersion.getVersion() + 1);
        childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty(), newerVersion);
        heal.healData(childVertex);
        return childVertex;
    }
}
