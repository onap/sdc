package org.openecomp.sdc.asdctool.impl.validator.tasks;

/**
 * Created by chaya on 7/5/2017.
 */
public abstract class ServiceValidationTask implements TopologyTemplateValidationTask {
    protected String name = "";
    protected String taskStatus = "NOT_STARTED";

    @Override
    public String getTaskName() {
        return this.name;
    }

    @Override
    public String getTaskResultStatus() {
        return this.taskStatus;
    }

    @Override
    public void setTaskResultStatus(String status) {
        this.taskStatus = status;
    }

}
