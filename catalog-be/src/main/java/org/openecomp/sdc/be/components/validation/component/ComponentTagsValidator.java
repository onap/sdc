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

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class ComponentTagsValidator implements ComponentFieldValidator {

    private static final Logger log = Logger.getLogger(ComponentTagsValidator.class.getName());
    private static final String TAG_FIELD_LABEL = "tag";
    private ComponentsUtils componentsUtils;

    public ComponentTagsValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        List<String> tagsList = component.getTags();
        try {
            validateComponentTags(tagsList, component.getName(), component.getComponentType(), user, component, actionEnum);
        } catch(ComponentException e){
            ResponseFormat responseFormat = e.getResponseFormat() != null ? e.getResponseFormat()
                    : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditComponentAdmin(responseFormat, user, component, actionEnum, component.getComponentType());
            throw e;
        }
        ValidationUtils.removeDuplicateFromList(component.getTags());
    }

    protected void validateComponentTags(List<String> tags, String name, ComponentTypeEnum componentType, User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum action) {
        log.debug("validate component tags");
        boolean includesComponentName = false;
        int tagListSize = 0;
        ResponseFormat responseFormat;
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                validateTagLength(componentType, user, component, action, tag);
                if (validateTagPattern(tag)) {
                    includesComponentName = isIncludesComponentName(name, includesComponentName, tag);
                } else {
                    log.debug("invalid tag {}", tag);
                    responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_FIELD_FORMAT, componentType.getValue(), TAG_FIELD_LABEL);
                    componentsUtils.auditComponentAdmin(responseFormat, user, component, action, componentType);
                    throw new ByActionStatusComponentException(ActionStatus.INVALID_FIELD_FORMAT, componentType.getValue(), TAG_FIELD_LABEL);
                }
                tagListSize += tag.length() + 1;
            }
            if (tagListSize > 0) {
                tagListSize--;
            }

            if (!includesComponentName) {
                log.debug("tags must include component name");
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);
                componentsUtils.auditComponentAdmin(responseFormat, user, component, action, componentType);
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);
            }
            if (!ValidationUtils.validateTagListLength(tagListSize)) {
                log.debug("overall tags length exceeds limit {}", ValidationUtils.TAG_LIST_MAX_LENGTH);
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH);
                componentsUtils.auditComponentAdmin(responseFormat, user, component, action, componentType);
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_TAGS_EXCEED_LIMIT, "" + ValidationUtils.TAG_LIST_MAX_LENGTH);
            }
        } else {
            tags = new ArrayList<>();
            tags.add(name);
            component.setTags(tags);
        }
    }

    private boolean isIncludesComponentName(String name, boolean includesComponentName, String tag) {
        if (!includesComponentName) {
            includesComponentName = name.equals(tag);
        }
        return includesComponentName;
    }

    private void validateTagLength(ComponentTypeEnum componentType, User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum action, String tag) {
        ResponseFormat responseFormat;
        if (!ValidationUtils.validateTagLength(tag)) {
            log.debug("tag length exceeds limit {}", ValidationUtils.TAG_MAX_LENGTH);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH);
            componentsUtils.auditComponentAdmin(responseFormat, user, component, action, componentType);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT, "" + ValidationUtils.TAG_MAX_LENGTH);
        }
    }

    protected boolean validateTagPattern(String tag) {
        return ValidationUtils.validateTagPattern(tag);
    }
}
