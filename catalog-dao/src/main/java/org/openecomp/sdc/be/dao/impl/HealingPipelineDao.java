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

package org.openecomp.sdc.be.dao.impl;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.thinkaurelius.titan.core.TitanVertex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.impl.heal.HealGraphDao;
import org.openecomp.sdc.be.dao.impl.heal.HealNodeGraphDao;
import org.openecomp.sdc.be.dao.impl.heal.HealTitanGraphDao;
import org.openecomp.sdc.be.dao.impl.heal.HealVertexGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.heal.Heal;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersion;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealVersionBuilder;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("healingPipelineDao")
public class HealingPipelineDao {

    private static Logger logger = Logger.getLogger(HealingPipelineDao.class.getName());

    private HealVersion<Integer> currentHealVersion;

    @Value("${current.heal.version}")
    private Integer healVersion;

    private ImmutableListMultimap<String, Heal> healingPipeline;

    private HealGraphDao healNodeGraphDao;
    private HealGraphDao healVertexGraphDao;
    private HealGraphDao healTitanVertexGraphDao;

    public HealingPipelineDao() {
        healingPipeline = ImmutableListMultimap.of();
        checkValidation(healingPipeline);
    }

    @PostConstruct
    public void initHealVersion() {
        currentHealVersion = HealVersionBuilder.build(healVersion);
    }

    @PostConstruct
    public void initGraphHealers() {
        healNodeGraphDao = new HealNodeGraphDao(this);
        healVertexGraphDao = new HealVertexGraphDao(this);
        healTitanVertexGraphDao = new HealTitanGraphDao(this);
    }


    private HealGraphDao supplyHealer(Object graphNode) {
        if (graphNode instanceof GraphVertex) {
            return healVertexGraphDao;
        }
        if (graphNode instanceof GraphElement) {
            return healNodeGraphDao;
        }
        if (graphNode instanceof TitanVertex) {
            return healTitanVertexGraphDao;
        }

        return null;
    }


    public ImmutableListMultimap<String, Heal> getHealingPipeline() {
        return healingPipeline;
    }

    public boolean shouldHeal(HealVersion<Integer> healerVersion, HealVersion<Integer> vertexVersion) {
        Objects.requireNonNull(healerVersion);
        Objects.requireNonNull(vertexVersion);
        if (healerVersion.compareTo(currentHealVersion) >= 0) {
            return false;
        }
        return healerVersion.compareTo(vertexVersion) >= 0;
    }

    public void setHealVersion(Integer healVersion) {
        this.healVersion = healVersion;
    }


    public ImmutableList<Heal> getHealersForVertex(String edgeLabelEnum, HealVersion<Integer> vertexVersion) {
        final ImmutableList<Heal> vertexHeals = getHealingPipeline().get(edgeLabelEnum);
        List<Heal> list = new ArrayList<>();
        for (Heal heal : vertexHeals) {
            if (shouldHeal(heal.fromVersion(), vertexVersion)) {
                list.add(heal);
            }
        }
        return ImmutableList.copyOf(list);
    }


    public void setHealingPipeline(ImmutableListMultimap<String, Heal> healingPipeline) {
        checkValidation(healingPipeline);
        this.healingPipeline = healingPipeline;
    }


    public void setHealingVersion(final GraphVertex graphVertex) {
        graphVertex.addMetadataProperty(GraphPropertyEnum.HEALING_VERSION, currentHealVersion.getVersion());
    }

    public void setHealingVersion(TitanVertex graphVertex) {
        graphVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty(), currentHealVersion.getVersion());
    }

    public void setHealingVersion(GraphNode graphNode) {
        graphNode.setHealingVersion(currentHealVersion.getVersion());
    }

    public HealVersion<Integer> getCurrentHealVersion() {
        return currentHealVersion;
    }

    public Optional performGraphReadHealing(Object graphNode, Object edgeLabel) {
        HealGraphDao healGraphDao = supplyHealer(graphNode);
        if (healGraphDao == null) {
            logger.error("Unexpected graph node : {}", graphNode.getClass().getCanonicalName());
            return Optional.empty();
        }
        return Optional.of(healGraphDao.performGraphReadHealing(graphNode, edgeLabel));
    }

    /**
     * prevent duplicated healing version for same edge label.
     */
    private void checkValidation(ImmutableListMultimap<String, Heal> listMultimap)  {
        listMultimap.keySet().forEach(key -> this.validNoDuplicates(key, listMultimap.get(key)));
    }

    private void validNoDuplicates(String key, List<Heal> heals) {
        Set<Integer> duplicatedVersionSet = new HashSet<>();
        Set<Integer> duplicatedNumbersSet = heals.stream().map(heal -> ((HealVersion<Integer>) heal.fromVersion()).getVersion()).filter(n -> !duplicatedVersionSet.add(n)).collect(Collectors.toSet());
        if (!duplicatedNumbersSet.isEmpty()) {
            throw new IllegalStateException(String.format("Edge label %s , contains multiple healing with same version %s", key, duplicatedNumbersSet.stream().map(Object::toString).collect(joining(" , ", "[ ", " ]"))));
        }
    }

}
