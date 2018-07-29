package org.openecomp.sdc.be.components.impl.exceptions;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.exception.ResponseFormat;

public class ComponentException extends RuntimeException {

    /**
     * This class will be initialized either by action status and params or by ResponseFormat
     */

    private final transient ResponseFormat responseFormat;
    private final ActionStatus actionStatus;
    private final String[] params;

    public ComponentException(ResponseFormat responseFormat) {
        this(responseFormat, ActionStatus.OK);
    }

    public ComponentException(ActionStatus actionStatus, String... params) {
        this(null, actionStatus, params);
    }

    private ComponentException(ResponseFormat responseFormat, ActionStatus actionStatus, String... params) {
        this.actionStatus = actionStatus;
        this.params = params.clone();
        this.responseFormat = responseFormat;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public ActionStatus getActionStatus() {
        return actionStatus;
    }

    public String[] getParams() {
        return params.clone();
    }


}
