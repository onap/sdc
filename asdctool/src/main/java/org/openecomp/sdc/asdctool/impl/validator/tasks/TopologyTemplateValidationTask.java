package org.openecomp.sdc.asdctool.impl.validator.tasks;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

/**
 * Created by chaya on 7/5/2017.
 */
public interface TopologyTemplateValidationTask {
    boolean validate(GraphVertex vertex);
    String getTaskName();
    String getTaskResultStatus();
    void setTaskResultStatus(String status);
}
