/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.common.http.config;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.security.SecurityUtil;

public class BasicAuthorization {

    private String userName;
    private String password;

    public BasicAuthorization() {
    }

    public BasicAuthorization(BasicAuthorization basicAuthorization) {
        setUserName(basicAuthorization.userName);
        setPassword(basicAuthorization.password, false);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        validate(userName);
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        setPassword(password, true);
    }

    private void setPassword(String password, boolean isEncoded) {
        validate(password);
        if (isEncoded) {
            Either<String, String> passkey = SecurityUtil.decrypt(password);
            if (passkey.isLeft()) {
                this.password = passkey.left().value();
            } else {
                throw new IllegalArgumentException(passkey.right().value());
            }
        } else {
            this.password = password;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BasicAuthorization other = (BasicAuthorization) obj;
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BasicAuthentication [userName=");
        builder.append(userName);
        builder.append("]");
        return builder.toString();
    }

    private void validate(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("BasicAuthorization username and/or password cannot be empty");
        }
    }
}
