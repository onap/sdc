/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

import java.util.Arrays;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.exception.ResponseFormat;

public class ByActionStatusComponentException extends ComponentException {

    private final ActionStatus actionStatus;
    private final String[] params;

    public ByActionStatusComponentException(ActionStatus actionStatus, String... params) {
        this.actionStatus = actionStatus;
        this.params = params.clone();
    }

    public ActionStatus getActionStatus() {
        return actionStatus;
    }

    public String[] getParams() {
        return params.clone();
    }

    @Override
    public String toString() {
        return "ComponentException{" +
            "actionStatus=" + actionStatus +
            ", params=" + Arrays.toString(params) +
            '}';
    }

    @Override
    public ResponseFormat responseFormat(ResponseFormatManager responseFormatManager) {
        return responseFormatManager.getResponseFormat(getActionStatus(), getParams());
    }
}
