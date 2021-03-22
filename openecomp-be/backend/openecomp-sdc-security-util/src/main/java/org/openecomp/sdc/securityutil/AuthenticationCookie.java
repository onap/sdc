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
package org.openecomp.sdc.securityutil;

import java.util.Set;

public class AuthenticationCookie {

    private String userID;
    private Set<String> roles;
    private long maxSessionTime;
    private long currentSessionTime;

    public AuthenticationCookie() {
    }

    public AuthenticationCookie(AuthenticationCookie authenticationCookie) {
        this.userID = authenticationCookie.userID;
        this.roles = authenticationCookie.roles;
        this.maxSessionTime = authenticationCookie.maxSessionTime;
        this.currentSessionTime = authenticationCookie.currentSessionTime;
    }

    /**
     * Create new cookie with max_session_time and current_session_time started with same value
     *
     * @param userId
     */
    public AuthenticationCookie(String userId) {
        this.userID = userId;
        long currentTimeMilliSec = System.currentTimeMillis();
        this.maxSessionTime = currentTimeMilliSec;
        this.currentSessionTime = currentTimeMilliSec;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Set getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public long getMaxSessionTime() {
        return maxSessionTime;
    }

    public void setMaxSessionTime(long maxSessionTime) {
        this.maxSessionTime = maxSessionTime;
    }

    public long getCurrentSessionTime() {
        return currentSessionTime;
    }

    public void setCurrentSessionTime(long currentSessionTime) {
        this.currentSessionTime = currentSessionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthenticationCookie)) {
            return false;
        }
        AuthenticationCookie that = (AuthenticationCookie) o;
        if (getMaxSessionTime() != that.getMaxSessionTime()) {
            return false;
        }
        if (getCurrentSessionTime() != that.getCurrentSessionTime()) {
            return false;
        }
        if (getUserID() != null ? !getUserID().equals(that.getUserID()) : that.getUserID() != null) {
            return false;
        }
        return getRoles() != null ? getRoles().containsAll(that.getRoles()) : that.getRoles() == null;
    }

    @Override
    public int hashCode() {
        int result = getUserID() != null ? getUserID().hashCode() : 0;
        result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
        result = 31 * result + (int) (getMaxSessionTime() ^ (getMaxSessionTime() >>> 32));
        result = 31 * result + (int) (getCurrentSessionTime() ^ (getCurrentSessionTime() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "AuthenticationCookie{" + "userID='" + userID + '\'' + ", roles=" + roles + ", maxSessionTime=" + maxSessionTime
            + ", currentSessionTime=" + currentSessionTime + '}';
    }
}
