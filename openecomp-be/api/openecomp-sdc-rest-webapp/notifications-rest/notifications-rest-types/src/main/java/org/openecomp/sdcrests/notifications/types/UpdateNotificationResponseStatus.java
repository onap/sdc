/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdcrests.notifications.types;

import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdcrests.notifications.types.NotificationResponseStatus.Failure;
import static org.openecomp.sdcrests.notifications.types.NotificationResponseStatus.Success;

/**
 * Created by TALIO on 4/27/2016.
 */
public class UpdateNotificationResponseStatus {
    private Map<String, List<ErrorMessage>> errors = new HashMap<>();
    private NotificationResponseStatus status = Success;

    public Map<String, List<ErrorMessage>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<ErrorMessage>> errors) {
        this.errors = errors;
    }

    public NotificationResponseStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationResponseStatus status) {
        this.status = status;
    }

    public void addStructureError(String notificationId, ErrorMessage errorMessage) {
        List<ErrorMessage> errorList =
            errors.computeIfAbsent(notificationId, k -> new ArrayList<>());
        errorList.add(errorMessage);
        if (ErrorLevel.ERROR.equals(errorMessage.getLevel())) {
            status = Failure;
        }
    }
}
