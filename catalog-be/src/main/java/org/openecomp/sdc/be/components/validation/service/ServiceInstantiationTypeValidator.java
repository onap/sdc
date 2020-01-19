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

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.InstantiationTypes;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ServiceInstantiationTypeValidator implements ServiceFieldValidator {
    private static final Logger log = Logger.getLogger(ServiceInstantiationTypeValidator.class.getName());
    private ComponentsUtils componentsUtils;

    public ServiceInstantiationTypeValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        log.debug("validate instantiation type");
        String instantiationType = service.getInstantiationType();
        if (StringUtils.isEmpty(instantiationType)) {
            service.setInstantiationType(InstantiationTypes.A_LA_CARTE.getValue());
        }
        if (!InstantiationTypes.containsName(service.getInstantiationType())){
            log.error("Recieved Instantiation type {} is not valid.", instantiationType);
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_INSTANTIATION_TYPE, instantiationType);
            componentsUtils.auditComponentAdmin(errorResponse, user, service, actionEnum, ComponentTypeEnum.SERVICE);
            throw new ByResponseFormatComponentException(errorResponse);
        }
    }
}
