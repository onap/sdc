package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class AccessValidations {

    private final UserValidations userValidations;
    private final ComponentValidations componentValidations;

    public AccessValidations(UserValidations userValidations, ComponentValidations componentValidations) {
        this.userValidations = userValidations;
        this.componentValidations = componentValidations;
    }

    public Component validateUserCanRetrieveComponentData(String componentId, String componentType, String userId, String actionContext)  {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        retrieveUser(userId, actionContext);
	    return componentValidations.getComponent(componentId, componentTypeEnum);
    }

    public Component validateUserCanWorkOnComponent(String componentId, ComponentTypeEnum componentType, String userId, String actionContext) {
        User user = retrieveUser(userId, actionContext);
        validateUserIsAdminOrDesigner(user);
        return componentValidations.validateComponentIsCheckedOutByUser(componentId, componentType, userId);
    }


    public void validateUserCanWorkOnComponent(Component component, String userId, String actionContext) {
        User user = retrieveUser(userId, actionContext);
        validateUserIsAdminOrDesigner(user);
        componentValidations.validateComponentIsCheckedOutByUser(component, userId);
    }

    public void validateUserExists(String userId, String context) {
        retrieveUser(userId, context);
    }

    public void validateUserExist(String userId, String actionContext) {
        userValidations.validateUserExists(userId, actionContext, false);
    }

    public User userIsAdminOrDesigner(String userId, String actionContext){
        User user = retrieveUser(userId, actionContext);
        validateUserIsAdminOrDesigner(user);
        return user;
    }

    private User retrieveUser(String userId, String actionContext) {
        return userValidations.validateUserExists(userId, actionContext, true);
    }

    private void validateUserIsAdminOrDesigner(User user) {
        List<Role> roles = new ArrayList<>(2);
        roles.add(Role.ADMIN);
        roles.add(Role.DESIGNER);
        userValidations.validateUserRole(user, roles);
    }

}
