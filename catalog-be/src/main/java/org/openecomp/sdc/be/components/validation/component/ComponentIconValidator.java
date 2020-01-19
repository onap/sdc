package org.openecomp.sdc.be.components.validation.component;

import org.apache.commons.lang3.StringUtils;
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

import java.util.Arrays;

@org.springframework.stereotype.Component
public class ComponentIconValidator implements ComponentFieldValidator {

    private static final Logger log = Logger.getLogger(ComponentIconValidator.class.getName());
    private static final String DEFAULT_ICON = "defaulticon";
    private ComponentsUtils componentsUtils;

    public ComponentIconValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        log.debug("validate Icon");
        ComponentTypeEnum type = component.getComponentType();
        String icon = component.getIcon();
        if (StringUtils.isEmpty(icon)) {
            log.info("icon is missing.");
            component.setIcon(DEFAULT_ICON);
        }
        try {
            if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
                validateAndSetDefaultIcon(icon, component);
            } else {
                validateIcon(icon,component.getComponentType());
            }

        } catch(ComponentException e){
            ResponseFormat responseFormat = e.getResponseFormat() != null ? e.getResponseFormat()
                    : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditComponentAdmin(responseFormat, user, component, actionEnum, type);
            throw e;
        }
    }

    private void validateAndSetDefaultIcon(String icon, org.openecomp.sdc.be.model.Component componnet) {
        try {
            if (componnet.getCategories().get(0).getIcons() == null) {
                componnet.getCategories().get(0).setIcons(Arrays.asList(DEFAULT_ICON));
            }
            if (icon != null) {
                if (componnet.getCategories().get(0).getIcons().contains(icon)) {
                    return;
                }
            }
        } catch (NullPointerException exp) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY,
                    ComponentTypeEnum.SERVICE.getValue());
        }
        componnet.setIcon(DEFAULT_ICON);
    }

    private void validateIcon(String icon, ComponentTypeEnum type) {
        if (icon != null) {
            if (!ValidationUtils.validateIconLength(icon)) {
                log.debug("icon exceeds max length");
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, type.getValue(), "" + ValidationUtils.ICON_MAX_LENGTH);
            }

            if (!ValidationUtils.validateIcon(icon)) {
                log.info("icon is invalid.");
                throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INVALID_ICON, type.getValue());
            }
        }
    }
}
