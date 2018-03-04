package org.openecomp.sdc.be.auditing.impl;

import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;

import java.util.Collections;
import java.util.List;

public abstract class AuditBaseEventFactory implements AuditEventFactory {

    //TODO imanzon: Check if requestId and serviceInstanceId fields are required for all tables.
    //Currently they are included even if they ahs null value. If they should not appear then
    //createTable code should be updated so that they need to be removed from the tables
    private AuditingActionEnum action;

    public AuditBaseEventFactory(AuditingActionEnum action) {
        this.action = action;
    }

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
        if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
            return Constants.EMPTY_STRING;
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

    public static String buildValue(String value) {
        if (value == null) {
            return Constants.EMPTY_STRING;
        }
        return value;
    }

    protected static String replaceNullNameWithEmpty(String name) {
        if (name != null && !name.trim().contains(Constants.NULL_STRING)) {
            return name;
        }
        return Constants.EMPTY_STRING;
    }

    @Override
    //TODO implement in derived classes for ci testing
    public List<Pair<String, String>> getQueryParams() {
        return Collections.emptyList();
    }

    @Override
    public String getAuditingEsType() {
        return this.action.getAuditingEsType();
    }


}
