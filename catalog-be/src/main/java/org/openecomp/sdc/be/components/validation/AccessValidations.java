package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class AccessValidations {

    private final UserValidations userValidations;
    private final ComponentValidations componentValidations;


    public AccessValidations(UserValidations userValidations, ComponentValidations componentValidations) {
        this.userValidations = userValidations;
        this.componentValidations = componentValidations;
    }

    public Component validateUserCanWorkOnComponentAndLockIt(ComponentTypeEnum componentTypeEnum, String componentId, String userId, String actionContext) {
        userValidations.validateUserExists(userId, actionContext, false)
                .left()
                .on(this::onUserError);

        return componentValidations.validateComponentIsCheckedOutByUserAndLockIt(componentTypeEnum, componentId, userId);
    }

    private User onUserError(ResponseFormat responseFormat) {
        throw new ComponentException(responseFormat);
    }


}
