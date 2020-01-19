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

package org.openecomp.sdc.be.auditing.impl;

import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public abstract class AuditBaseEventFactory implements AuditEventFactory {

    private AuditingActionEnum action;

    public AuditBaseEventFactory(AuditingActionEnum action) {
        this.action = Objects.requireNonNull(action);
    }

    public AuditBaseEventFactory() {}

    public AuditingActionEnum getAction() {
        return action;
    }

    public static String buildUserNameExtended(User user) {
        if (user == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        appendIfNotEmpty(user.getUserId(), builder);

        String firstName = replaceNullNameWithEmpty(user.getFirstName());
        String lastName = replaceNullNameWithEmpty(user.getLastName());

        if (appendIfNotEmpty(firstName, builder)) {
            appendIfNotEmpty(lastName, builder, " ");
        }
        else {
            appendIfNotEmpty(lastName, builder);
        }
        appendIfNotEmpty(user.getEmail(), builder);
        appendIfNotEmpty(user.getRole(), builder);

        return builder.toString();
    }

    private static boolean appendIfNotEmpty(String value, StringBuilder builder) {
        return appendIfNotEmpty(value, builder, ", ");
    }

    protected static boolean appendIfNotEmpty(String value, StringBuilder builder, String delimiter) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        addDelimiterIfNeeded(builder, delimiter);
        builder.append(value);
        return true;
    }

    private static void addDelimiterIfNeeded(StringBuilder builder, String delimiter) {
        if (builder.length() > 0) {
            builder.append(delimiter);
        }
    }

    protected static String buildUserName(User user) {
        if (user == null || StringUtils.isEmpty(user.getUserId())) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = replaceNullNameWithEmpty(user.getFirstName());
        if (!firstName.isEmpty()) {
            sb.append(firstName);
            sb.append(" ");
        }
        String lastName = replaceNullNameWithEmpty(user.getLastName());
        if (!lastName.isEmpty()) {
            sb.append(lastName);
        }
        sb.append("(").append(user.getUserId()).append(")");
        return sb.toString();
    }

    private static String buildValue(String value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value;
    }

    protected static String replaceNullNameWithEmpty(String name) {
        if (name != null && !name.trim().contains(Constants.NULL_STRING)) {
            return name;
        }
        return StringUtils.EMPTY;
    }

    @Override
    //TODO implement in derived classes for ci testing
    public List<Pair<String, String>> getQueryParams() {
        return Collections.emptyList();
    }

    @Override
    public final String getLogMessage() {
       return String.format(getLogPattern(), getLogArgs());
    }

    private Object[] getLogArgs() {
        return Arrays.stream(getLogMessageParams())
                .map(AuditBaseEventFactory::buildValue)
                .toArray(String[]::new);
    }

    public abstract String getLogPattern();

    public abstract String[] getLogMessageParams();




}
