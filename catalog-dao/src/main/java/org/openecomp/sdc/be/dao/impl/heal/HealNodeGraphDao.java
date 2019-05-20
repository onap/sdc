package org.openecomp.sdc.be.dao.impl.heal;

import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;

public class HealNodeGraphDao implements HealGraphDao<GraphNode, GraphEdge> {

    private HealingPipelineDao healingPipelineDao;


    public HealNodeGraphDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }

    @Override
    public GraphNode performGraphReadHealing(GraphNode childVertex, GraphEdge graphEdge) {
        Integer healingVersionInt =  childVertex.getHealingVersion();
        HealVersion<Integer> healingVersion = HealVersionBuilder.build(healingVersionInt);
        healingPipelineDao.getHealersForVertex(graphEdge.getEdgeType().getProperty(), healingVersion).forEach(heal -> healJanusGraphVertex(childVertex, heal));
        childVertex.setHealingVersion(healingPipelineDao.getCurrentHealVersion().getVersion());
        return childVertex;
    }

    private GraphNode healJanusGraphVertex(GraphNode childVertex, Heal<GraphNode> heal) {
        heal.healData(childVertex);
        final HealVersion<Integer> healVersion = heal.fromVersion();
        HealVersion<Integer> newerVersion = HealVersionBuilder.build(healVersion.getVersion() + 1);
        childVertex.setHealingVersion(newerVersion.getVersion());
        return childVertex;
    }
}
