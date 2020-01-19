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

package org.openecomp.sdc.be.facade.operations;

import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ResultStatusEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class FacadeOperationUtils {
    private static final Logger log = Logger.getLogger(CatalogOperation.class); 
    
    private FacadeOperationUtils() {
    }

    public static ActionStatus convertStatusToActionStatus(IStatus status) {
        ActionStatus result = ActionStatus.OK;
        if (status.getResultStatus() != ResultStatusEnum.SUCCESS){
            log.debug("updateCatalog - failed to  send notification {}", status);
        }
        return result;
    }
}
