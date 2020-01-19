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

package org.openecomp.sdc.be.components.validation.service;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;

@org.springframework.stereotype.Component
public class ServiceTypeValidator implements ServiceFieldValidator {

    private static final Logger log = Logger.getLogger(ServiceTypeValidator.class.getName());
    private static final String SERVICE_TYPE = JsonPresentationFields.SERVICE_TYPE.getPresentation();
    private ComponentsUtils componentsUtils;

    public ServiceTypeValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        log.debug("validate service type");
        String serviceType = service.getServiceType();
        if (serviceType == null) {
            log.info("service type is not valid.");
            throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY, "" + SERVICE_TYPE);
        }
        validateServiceType(serviceType);
    }

    private void validateServiceType(String serviceType) {
        if (serviceType.isEmpty()) {
            return;
        }
        if (!ValidationUtils.validateServiceTypeLength(serviceType)) {
            log.info("service type exceeds limit.");
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_EXCEEDS_LIMIT, "" + SERVICE_TYPE);
        }
        if (!ValidationUtils.validateServiceMetadata(serviceType)) {
            log.info("service type is not valid.");
            throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERY,"" + SERVICE_TYPE);
        }
    }
}
