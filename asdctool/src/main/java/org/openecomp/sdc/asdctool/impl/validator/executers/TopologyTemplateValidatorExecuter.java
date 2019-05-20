package org.openecomp.sdc.asdctool.impl.validator.executers;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by chaya on 7/3/2017.
 */
public class TopologyTemplateValidatorExecuter {

    private static Logger log = Logger.getLogger(VfValidatorExecuter.class.getName());

    @Autowired
    protected JanusGraphDao janusGraphDao;

    protected String name;

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
