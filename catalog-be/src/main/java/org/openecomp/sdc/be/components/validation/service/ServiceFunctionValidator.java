package org.openecomp.sdc.be.components.validation.service;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ServiceFunctionValidator implements ServiceFieldValidator {

    private static final Logger log = Logger.getLogger(ServiceFunctionValidator.class.getName());
    private static final String SERVICE_FUNCTION = JsonPresentationFields.SERVICE_FUNCTION.getPresentation();
    private ComponentsUtils componentsUtils;

    public ServiceFunctionValidator(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        log.debug("validate service function");
        String serviceFunction = service.getServiceFunction();
        if(serviceFunction == null) {
            log.info("service function is null, assigned to empty value.");
            service.setServiceFunction("");
            return;
        }
        validateServiceFunction(serviceFunction);
    }

    private void validateServiceFunction(String serviceFunction) {
        if (StringUtils.isEmpty(serviceFunction)){
            return;
        } else {
            if (!ValidationUtils.validateServiceFunctionLength(serviceFunction)) {
                log.info("service function exceeds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_EXCEEDS_LIMIT, "" + SERVICE_FUNCTION);
                throw new ByResponseFormatComponentException(errorResponse);
            }

            if (!ValidationUtils.validateServiceMetadata(serviceFunction)) {
                log.info("service function is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERY, "" + SERVICE_FUNCTION);
                throw new ByResponseFormatComponentException(errorResponse);
            }
        }
    }
}
