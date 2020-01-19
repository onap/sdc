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
