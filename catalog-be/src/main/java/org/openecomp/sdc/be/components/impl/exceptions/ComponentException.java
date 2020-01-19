/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
