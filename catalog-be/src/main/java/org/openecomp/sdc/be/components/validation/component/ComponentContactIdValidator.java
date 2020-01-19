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
import org.springframework.beans.factory.annotation.Autowired;

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
