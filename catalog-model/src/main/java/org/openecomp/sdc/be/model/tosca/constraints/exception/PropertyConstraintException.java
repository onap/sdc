package org.openecomp.sdc.be.model.tosca.constraints.exception;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil;

public class PropertyConstraintException extends ConstraintFunctionalException {

    private final ActionStatus actionStatus;
    private final String[] params;

    public PropertyConstraintException(String message, Throwable cause, ConstraintUtil.ConstraintInformation constraintInformation, ActionStatus actionStatus, String... params) {
        super(message, cause, constraintInformation);
        this.actionStatus = actionStatus;
        this.params = params;
    }

    public ActionStatus getActionStatus() {
        return actionStatus;
    }

    public String[] getParams() {
        return params.clone();
    }
}
