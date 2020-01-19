package org.openecomp.sdc.be.components.validation.service;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ServiceRoleValidator implements ServiceFieldValidator {

    private static final Logger log = Logger.getLogger(ServiceRoleValidator.class.getName());
    private static final String SERVICE_ROLE = JsonPresentationFields.SERVICE_ROLE.getPresentation();
    private ComponentsUtils componentsUtils;

    public ServiceRoleValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        log.debug("validate service role");
        String serviceRole = service.getServiceRole();
        if (serviceRole != null){
        validateServiceRole(serviceRole);
            }

    }

    private void validateServiceRole(String serviceRole) {
        if (StringUtils.isEmpty(serviceRole)){
            return;
        } else {

            if (!ValidationUtils.validateServiceRoleLength(serviceRole)) {
                log.info("service role exceeds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_EXCEEDS_LIMIT, "" + SERVICE_ROLE);
                throw new ByResponseFormatComponentException(errorResponse);
            }

            if (!ValidationUtils.validateServiceMetadata(serviceRole)) {
                log.info("service role is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERY, ""+ SERVICE_ROLE);
                throw new ByResponseFormatComponentException(errorResponse);
            }
        }
    }
}
