package org.openecomp.sdc.be.components.impl.exceptions;

import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.annotation.Nullable;

public class ComponentException extends RuntimeException {

    /**
     * This class will be initialized either by action status and params or by ResponseFormat
     */

    private final transient ResponseFormat responseFormat;
    private final ActionStatus actionStatus;
    private final String[] params;

    public Resource getResource() {
        return resource;
    }

    private final Resource resource;

    public ComponentException(ResponseFormat responseFormat) {
        this(responseFormat, ActionStatus.OK, null);
    }

    public ComponentException(ActionStatus actionStatus, String... params) {
        this(ResponseFormatManager.getInstance().getResponseFormat(actionStatus, params), actionStatus, null, params);
    }

    public ComponentException(ActionStatus actionStatus, Resource resource, String... params) {
        this(ResponseFormatManager.getInstance().getResponseFormat(actionStatus, params), actionStatus, resource, params);
    }

    private ComponentException(ResponseFormat responseFormat, ActionStatus actionStatus, Resource resource, String... params) {
        this.actionStatus = actionStatus;
        this.params = params.clone();
        this.responseFormat = responseFormat;
        this.resource = resource;
    }

    @Nullable
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
