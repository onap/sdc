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
package org.openecomp.sdc.asdctool.impl.validator.executor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.openecomp.sdc.asdctool.impl.validator.report.Report;
import org.openecomp.sdc.asdctool.impl.validator.report.ReportFile.TXTFile;
import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class TopologyTemplateValidatorExecutor implements ValidatorExecutor {

    private static final Logger log = Logger.getLogger(TopologyTemplateValidatorExecutor.class);
    private final JanusGraphDao janusGraphDao;
    private final ComponentTypeEnum componentType;
    private final List<? extends TopologyTemplateValidationTask> tasks;
    @Getter
    private final String name;

    private TopologyTemplateValidatorExecutor(JanusGraphDao janusGraphDao, String name, ComponentTypeEnum componentType,
                                              List<? extends TopologyTemplateValidationTask> tasks) {
        this.janusGraphDao = janusGraphDao;
        this.name = name;
        this.componentType = componentType;
        this.tasks = tasks;
    }

    @Autowired(required = false)
    public static ValidatorExecutor serviceValidatorExecutor(JanusGraphDao janusGraphDao) {
        return new TopologyTemplateValidatorExecutor(janusGraphDao, "SERVICE_VALIDATOR", ComponentTypeEnum.SERVICE, new ArrayList<>());
    }

    @Autowired(required = false)
    public static ValidatorExecutor vfValidatorExecutor(List<VfValidationTask> tasks, JanusGraphDao janusGraphDao) {
        return new TopologyTemplateValidatorExecutor(janusGraphDao, "BASIC_VF_VALIDATOR", ComponentTypeEnum.RESOURCE, tasks);
    }

    @Override
    public boolean executeValidations(Report report, TXTFile reportFile) {
        List<GraphVertex> vertices = getVerticesToValidate();
        reportFile.reportStartValidatorRun(name, vertices.size());
        Set<String> failedTasks = new HashSet<>();
        Set<String> successTasks = new HashSet<>();
        boolean successAllVertices = true;
        int vertexNum = 0;
        int verticesSize = vertices.size();
        for (GraphVertex vertex : vertices) {
            vertexNum++;
            boolean successAllTasks = true;
            for (TopologyTemplateValidationTask task : tasks) {
                reportFile.reportStartTaskRun(vertex, task.getTaskName());
                VertexResult result = task.validate(report, vertex, reportFile);
                if (!result.getStatus()) {
                    failedTasks.add(task.getTaskName());
                    successAllVertices = false;
                    successAllTasks = false;
                } else if (successAllTasks && vertexNum == verticesSize) {
                    successTasks.add(task.getTaskName());
                }
                reportFile.printValidationTaskStatus(vertex, task.getTaskName(), result.getStatus());
                report.addSuccess(vertex.getUniqueId(), task.getTaskName(), result);
            }
            String componentScanStatus = successAllTasks ? "success" : "failed";
            log.info("Topology Template {} Validation finished with {}", vertex.getUniqueId(), componentScanStatus);
        }
        reportFile.reportValidatorTypeSummary(name, failedTasks, successTasks);
        return successAllVertices;
    }

    private List<GraphVertex> getVerticesToValidate() {
        return janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, buildProps()).either(vs -> {
            log.info("getVerticesToValidate: {} vertices to scan", vs.size());
            return vs;
        }, sos -> {
            log.error("getVerticesToValidate failed {}", sos);
            return new ArrayList<>();
        });
    }

    private Map<GraphPropertyEnum, Object> buildProps() {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
            props.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
        }
        return props;
    }
}
