/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.common.util.NoHtml;

@JsonInclude
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {

    public static final String FORCE_DELETE_HEADER_FLAG = "FORCE_DELETE";
    @NoHtml
    private String firstName;
    @NoHtml
    private String lastName;
    @NoHtml
    private String userId;
    @NoHtml
    private String email;
    @NoHtml
    private String role;
    private Long lastLoginTime;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserStatusEnum status = UserStatusEnum.ACTIVE;

    public User(String userId) {
        this.userId = userId;
    }

    public User(String firstName, String lastName, String userId, String emailAddress, String role, Long lastLoginTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.email = emailAddress;
        this.role = role;
        this.lastLoginTime = lastLoginTime;
    }

    public User(User aUser) {
        this(aUser.getFirstName(), aUser.getLastName(), aUser.getUserId(), aUser.getEmail(), aUser.getRole(), aUser.getLastLoginTime());
    }

    public void copyData(User other) {
        if (other == null) {
            return;
        }
        this.firstName = other.getFirstName();
        this.lastName = other.getLastName();
        this.userId = other.getUserId();
        this.email = other.getEmail();
        this.role = other.getRole();
        this.lastLoginTime = other.getLastLoginTime();
    }

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }

    public void setLastLoginTime() {
        DateTime now = new DateTime(DateTimeZone.UTC);
        this.lastLoginTime = now.getMillis();
    }

}
