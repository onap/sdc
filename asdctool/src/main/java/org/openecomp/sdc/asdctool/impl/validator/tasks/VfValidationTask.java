package org.openecomp.sdc.asdctool.impl.validator.tasks;

/**
 * Created by chaya on 7/5/2017.
 */
public abstract class VfValidationTask implements TopologyTemplateValidationTask {
    protected String taskStatus = "NOT_STARTED";
    protected String name = "";

    @Override
    public String getTaskName() {
        return this.name;
    }

    @Override
    public String getTaskResultStatus() {
        return taskStatus;
    }

    @Override
    public void setTaskResultStatus(String status) {
        this.taskStatus = status;
    }

}
