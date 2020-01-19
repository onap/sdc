package org.openecomp.sdc.be.components.validation.component;

import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComponentValidator {

    private static final Logger log = Logger.getLogger(ComponentValidator.class.getName());
    protected ComponentsUtils componentsUtils;
    private List<ComponentFieldValidator> componentFieldValidators;

    public ComponentValidator(ComponentsUtils componentsUtils, List<ComponentFieldValidator> componentFieldValidators) {
        this.componentsUtils = componentsUtils;
        this.componentFieldValidators = componentFieldValidators;
    }

    public void validate(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum) {
        componentFieldValidators.stream().forEach(validator ->
                validator.validateAndCorrectField(user,component,actionEnum));
    }

}
