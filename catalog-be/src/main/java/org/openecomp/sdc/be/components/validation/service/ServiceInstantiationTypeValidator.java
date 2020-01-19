package org.openecomp.sdc.be.components.validation.service;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.InstantiationTypes;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

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
