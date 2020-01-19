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
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
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
public class ComponentDescriptionValidator implements ComponentFieldValidator {

    private static final Logger log = Logger.getLogger(ComponentTagsValidator.class.getName());
    private ComponentsUtils componentsUtils;

    public ComponentDescriptionValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        ComponentTypeEnum type = component.getComponentType();
        String description = component.getDescription();
        if (!ValidationUtils.validateStringNotEmpty(description)) {
            auditErrorAndThrow(user,component, actionEnum, ActionStatus.COMPONENT_MISSING_DESCRIPTION);
        }

        description = ValidationUtils.cleanUpText(description);
        try{
            validateComponentDescription(description, type);
        } catch(ComponentException e){
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(e.getActionStatus(), component.getComponentType().getValue());
            componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, component.getComponentType());
            throw e;
        }
        component.setDescription(description);
    }

    private void auditErrorAndThrow(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum, ActionStatus actionStatus) {
        ResponseFormat errorResponse = componentsUtils.getResponseFormat(actionStatus, component.getComponentType().getValue());
        componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, component.getComponentType());
        throw new ByActionStatusComponentException(actionStatus, component.getComponentType().getValue());
    }

    private void validateComponentDescription(String description, ComponentTypeEnum type) {
        if (description != null) {
            if (!ValidationUtils.validateDescriptionLength(description)) {
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
            }

            if (!ValidationUtils.validateCommentPattern(description)) {
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_DESCRIPTION, type.getValue());
            }
        }
    }
}
