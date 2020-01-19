package org.openecomp.sdc.be.user;

import org.openecomp.sdc.be.catalog.api.ITypeMessage;

public class UserMessage implements ITypeMessage{
    private UserOperationEnum operation;
    private String userId;
    private String role;

    public UserMessage(UserOperationEnum operation, String userId, String role) {
        this.setOperation(operation);
        this.setUserId(userId);
        this.setRole(role);
    }

    public UserOperationEnum getOperation() {
        return operation;
    }

    public void setOperation(UserOperationEnum operation) {
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserMessage [operation=" + operation + ", userId=" + userId + ", role=" + role + "]";
    }

    @Override
    public String getMessageType() {
        return getClass().getSimpleName();
    }

}
