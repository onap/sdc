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

package org.openecomp.sdc.asdctool.impl.validator.executers;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chaya on 7/3/2017.
 */
public class TopologyTemplateValidatorExecuter {

    private static Logger log = Logger.getLogger(VfValidatorExecuter.class.getName());

    protected JanusGraphDao janusGraphDao;

    protected String name;

    @Autowired
    public TopologyTemplateValidatorExecuter(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected List<GraphVertex> getVerticesToValidate(ComponentTypeEnum type) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.COMPONENT_TYPE, type.name());
        if(type.equals(ComponentTypeEnum.RESOURCE)) {
            props.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
        }

        Either<List<GraphVertex>, JanusGraphOperationStatus> results = janusGraphDao
            .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, props);
        if (results.isRight()) {
            log.error("getVerticesToValidate failed "+ results.right().value());
            return new ArrayList<>();
        }
        log.info("getVerticesToValidate: "+results.left().value().size()+" vertices to scan");
        return results.left().value();
    }

    protected boolean validate(List<? extends TopologyTemplateValidationTask> tasks, List<GraphVertex> vertices) {
        ReportManager.reportStartValidatorRun(getName(), vertices.size());
        Set<String> failedTasks = new HashSet<>();
        Set<String> successTasks = new HashSet<>();
        boolean successAllVertices = true;
        int vertexNum = 0;
        int verticesSize = vertices.size();

        for (GraphVertex vertex: vertices) {
            vertexNum++;
            boolean successAllTasks = true;
            for (TopologyTemplateValidationTask task: tasks) {
                ReportManager.reportStartTaskRun(vertex, task.getTaskName());
                VertexResult result = task.validate(vertex);
                if (!result.getStatus()) {
                    failedTasks.add(task.getTaskName());
                    successAllVertices = false;
                    successAllTasks = false;
                } else if (successAllTasks && vertexNum == verticesSize) {
                    successTasks.add(task.getTaskName());
                }
                ReportManager.printValidationTaskStatus(vertex, task.getTaskName(), result.getStatus());
                ReportManager.reportTaskEnd(vertex.getUniqueId(), task.getTaskName(), result);
            }
            String componentScanStatus = successAllTasks? "success" : "failed";
            log.info("Topology Template "+vertex.getUniqueId()+" Validation finished with "+componentScanStatus);
        }
        ReportManager.reportValidatorTypeSummary(getName(), failedTasks, successTasks);
        return successAllVertices;
    }
}
