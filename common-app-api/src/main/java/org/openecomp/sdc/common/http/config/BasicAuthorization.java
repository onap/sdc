package org.openecomp.sdc.common.http.config;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.security.SecurityUtil;

import fj.data.Either;

public class BasicAuthorization {
    private String userName;
    private String password;

    public BasicAuthorization() {
    }

    public BasicAuthorization(BasicAuthorization basicAuthorization) {
        setUserName(basicAuthorization.userName);
        setPassword(basicAuthorization.password, false);
    }

    public void setUserName(String userName) {
        validate(userName);
        this.userName = userName;
    }

    public void setPassword(String password) {
        setPassword(password, true);
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    
    private void setPassword(String password, boolean isEncoded) {
        validate(password);
        if(isEncoded) {
            Either<String, String> passkey = SecurityUtil.INSTANCE.decrypt(password);
            if(passkey.isLeft()) {
                this.password = passkey.left().value();
            }
            else {
                throw new IllegalArgumentException(passkey.right().value());
            }
        }
        else {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasicAuthorization other = (BasicAuthorization) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        }
        else if (!password.equals(other.password))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        }
        else if (!userName.equals(other.userName))
            return false;
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
        if(StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("BasicAuthorization username and/or password cannot be empty");
        }
    }
}
