package org.openecomp.sdc.be.components.validation.component;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ComponentNameValidator implements ComponentFieldValidator {

    private static final Logger log = Logger.getLogger(ComponentNameValidator.class.getName());
    private ComponentsUtils componentsUtils;
    private ToscaOperationFacade toscaOperationFacade;

    public ComponentNameValidator(ComponentsUtils componentsUtils, ToscaOperationFacade toscaOperationFacade) {
        this.componentsUtils = componentsUtils;
        this.toscaOperationFacade = toscaOperationFacade;
    }

    @Override
    public void validateAndCorrectField(User user, Component component, AuditingActionEnum actionEnum) {
        String componentName = component.getName();
        if (StringUtils.isEmpty(componentName)) {
            log.debug("component name is empty");
            auditErrorAndThrow(user,component, actionEnum, ActionStatus.MISSING_COMPONENT_NAME);
        }

        if (!ValidationUtils.validateComponentNameLength(componentName)) {
            log.debug("Component name exceeds max length {} ", ValidationUtils.COMPONENT_NAME_MAX_LENGTH);
            auditErrorAndThrow(user,component, actionEnum, ActionStatus.COMPONENT_NAME_EXCEEDS_LIMIT);
        }

        if (!ValidationUtils.validateComponentNamePattern(componentName)) {
            log.debug("Component name {} has invalid format", componentName);
            auditErrorAndThrow(user,component, actionEnum, ActionStatus.INVALID_COMPONENT_NAME);
        }
        if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
            validateComponentNameUnique(user,component,actionEnum);
        }
        //TODO remove assignment here
        component.setNormalizedName(ValidationUtils.normaliseComponentName(componentName));
        component.setSystemName(ValidationUtils.convertToSystemName(componentName));
    }

    public void validateComponentNameUnique(User user, Component component, AuditingActionEnum actionEnum) {
        log.debug("validate component name uniqueness for: {}", component.getName());
        ComponentTypeEnum type = component.getComponentType();
        ResourceTypeEnum resourceType = null;
        if(component instanceof Resource){
            resourceType = ((Resource)component).getResourceType();
        }
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateComponentNameExists(component.getName(), resourceType, type);

        if (dataModelResponse.isLeft()) {
            if (dataModelResponse.left().value()) {
                log.info("Component with name {} already exists", component.getName());
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, type.getValue(), component.getName());
                componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, type);
                throw new ByResponseFormatComponentException(errorResponse);
            }
            return;
        }
        BeEcompErrorManager.getInstance().logBeSystemError("validateComponentNameUnique");
        log.debug("Error while validateComponentNameUnique for component: {}", component.getName());
        ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, type);
        throw new ByResponseFormatComponentException(errorResponse);
    }

    private void auditErrorAndThrow(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum, ActionStatus actionStatus) {
        ResponseFormat errorResponse = componentsUtils.getResponseFormat(actionStatus, component.getComponentType().getValue());
        componentsUtils.auditComponentAdmin(errorResponse, user, component, actionEnum, component.getComponentType());
        throw new ByActionStatusComponentException(actionStatus, component.getComponentType().getValue());
    }
}
