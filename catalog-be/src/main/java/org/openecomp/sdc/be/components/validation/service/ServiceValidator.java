package org.openecomp.sdc.be.components.validation.service;

import org.openecomp.sdc.be.components.validation.component.ComponentFieldValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceValidator extends ComponentValidator {

    private List<ServiceFieldValidator> serviceFieldValidators;

    public ServiceValidator(ComponentsUtils componentsUtils, List<ComponentFieldValidator> componentFieldValidators, List<ServiceFieldValidator> serviceFieldValidators) {
        super(componentsUtils, componentFieldValidators);
        this.serviceFieldValidators = serviceFieldValidators;
    }

    @Override
    public void validate(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum) {
        super.validate(user, component, actionEnum);
        serviceFieldValidators.forEach(validator ->
                validator.validateAndCorrectField(user,(Service)component,actionEnum));
    }
}
