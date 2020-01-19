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

package org.openecomp.sdc.be.components.validation.component;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ComponentContactIdValidator implements ComponentFieldValidator {

    private static final Logger log = Logger.getLogger(ComponentContactIdValidator.class.getName());
    private ComponentsUtils componentsUtils;

    public ComponentContactIdValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        log.debug("validate component contactId");
        ComponentTypeEnum type = component.getComponentType();
        String contactId = component.getContactId();

        if (!ValidationUtils.validateStringNotEmpty(contactId)) {
            log.info("contact is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT, type.getValue());
            componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, type);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_CONTACT, type.getValue());
        }
        validateContactId(contactId, user, component, actionEnum, type);
    }

    private void validateContactId(String contactId, User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum, ComponentTypeEnum type) {
        if (contactId != null && !ValidationUtils.validateContactId(contactId)) {
            log.info("contact is invalid.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CONTACT, type.getValue());
            componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, type);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_CONTACT, type.getValue());
        }
    }
}
