package org.openecomp.sdc.be.exception;

import org.openecomp.sdc.be.dao.api.ActionStatus;

public class SdcActionException extends RuntimeException {

    private ActionStatus actionStatus;

    public SdcActionException(ActionStatus actionStatus) {
        this.actionStatus = actionStatus;
    }

    public ActionStatus getActionStatus() {
        return actionStatus;
    }
}
