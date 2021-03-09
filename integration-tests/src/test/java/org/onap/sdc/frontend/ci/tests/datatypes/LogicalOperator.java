package org.onap.sdc.frontend.ci.tests.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LogicalOperator {
    EQUALS("equal","="),
    GREATER_THAN("greater_than",">"),
    LESS_THAN("less_than","<");

    private final String name;
    private final String operator;
}
