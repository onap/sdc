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
package org.openecomp.sdc.be.dao.titan;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealConstants;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("titan-generic-dao")
public class HealingTitanGenericDao extends TitanGenericDao {

    @Autowired
    private HealingPipelineDao healingPipelineDao;

    public HealingTitanGenericDao(TitanGraphClient titanClient) {
        super(titanClient);
    }

    @Override
    public ImmutablePair<TitanVertex, Edge> getChildVertex(TitanVertex childVertex, GraphEdgeLabels edgeType) {
        ImmutablePair<TitanVertex, Edge> childVertexEdgeImmutablePair = super.getChildVertex(childVertex, edgeType);
        final TitanVertex graphVertex = childVertexEdgeImmutablePair.left;
        healingPipelineDao.performGraphReadHealing(graphVertex, edgeType);
        healingPipelineDao.setHealingVersion(graphVertex);
        return childVertexEdgeImmutablePair;
    }

    @Override
    public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> getChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz, boolean withEdges) {
        Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> either = super.getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz, withEdges);
        if (either.isRight()) {
            return either;
        }
        List<ImmutablePair<T, GraphEdge>> list = either.left().value();
        list.forEach(this::transformPair);
        return either;
    }

    @Override
    public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, TitanOperationStatus> getChild(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {
        Either<ImmutablePair<T, GraphEdge>, TitanOperationStatus> eitherChild = super.getChild(key, uniqueId, edgeType, nodeTypeEnum, clazz);
        if (eitherChild.isRight()) {
            return eitherChild;
        }
        ImmutablePair<T, GraphEdge> pair = eitherChild.left().value();
        GraphNode graphNode = pair.left;
        GraphEdge graphEdge = pair.right;
        healingPipelineDao.performGraphReadHealing(graphNode, graphEdge);
        healingPipelineDao.setHealingVersion(graphNode);
        return eitherChild;
    }

    private <T extends GraphNode> void transformPair(ImmutablePair<T, GraphEdge> either) {
        GraphEdge edgeType = either.right;
        GraphNode childVertex = either.left;
        Integer healingVersioInt = childVertex.getHealingVersion();
        HealVersionBuilder.build(healingVersioInt);
        healingPipelineDao.performGraphReadHealing(childVertex, edgeType);
        healingPipelineDao.setHealingVersion(childVertex);
    }

    @Override
    public Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> getChildrenVertecies(String key, String uniqueId, GraphEdgeLabels edgeType) {
        Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> either = super.getChildrenVertecies(key, uniqueId, edgeType);
        if (either.isRight()) {
            return either;
        }
        List<ImmutablePair<TitanVertex, Edge>> list = either.left().value();
        list.forEach(this::transformVertexPair);
        return either;
    }

    private void transformVertexPair(ImmutablePair<TitanVertex, Edge> either) {
        String edgeType = either.right.label();
        TitanVertex childVertex = either.left;
        VertexProperty<Integer> healingVersionProperty = childVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty());
        Integer healingVersioInt = healingVersionProperty.orElse(HealConstants.DEFAULT_HEAL_VERSION);
        HealVersionBuilder.build(healingVersioInt);
        healingPipelineDao.performGraphReadHealing(childVertex, edgeType);
        healingPipelineDao.setHealingVersion(childVertex);
    }

    @Override
    public <T extends GraphNode> Either<T, TitanOperationStatus> updateNode(GraphNode node, Class<T> clazz) {
        healingPipelineDao.setHealingVersion(node);
        return super.updateNode(node, clazz);
    }

    @Override
    public TitanOperationStatus updateVertex(GraphNode node, Vertex vertex) {
        healingPipelineDao.setHealingVersion(node);
        return super.updateVertex(node, vertex);
    }


    public void setHealingPipelineDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }
}
