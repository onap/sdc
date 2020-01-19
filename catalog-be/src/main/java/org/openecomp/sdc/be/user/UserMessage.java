/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
