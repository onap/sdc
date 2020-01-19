package org.openecomp.sdc.be.components.validation.component;

import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class ComponentProjectCodeValidator implements  ComponentFieldValidator{

    private static final Logger log = Logger.getLogger(ComponentProjectCodeValidator.class.getName());
    private ComponentsUtils componentsUtils;

    public ComponentProjectCodeValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        if (ComponentTypeEnum.RESOURCE == component.getComponentType()) {
            return;
        }
        log.debug("validate ProjectCode name ");
        String projectCode = component.getProjectCode();

        if (!ValidationUtils.validateStringNotEmpty(projectCode)) {
            log.info("projectCode is empty is allowed CR.");
            return;
        }

        try {
            validateProjectCode(projectCode);
        } catch (ComponentException exp) {
            ResponseFormat responseFormat = exp.getResponseFormat();
            componentsUtils.auditComponentAdmin(responseFormat, user, component, actionEnum, component.getComponentType(),
                    ResourceVersionInfo.newBuilder()
                            .build());
            throw exp;
        }

    }

    private void validateProjectCode(String projectCode) {
        if (projectCode != null) {
            if (!ValidationUtils.validateProjectCode(projectCode)) {
                log.info("projectCode  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROJECT_CODE);
                throw new ByResponseFormatComponentException(errorResponse);
            }
        }
    }
}
