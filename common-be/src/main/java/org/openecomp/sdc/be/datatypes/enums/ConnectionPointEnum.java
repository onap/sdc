package org.openecomp.sdc.be.datatypes.enums;

import java.util.Arrays;
import java.util.List;

public enum ConnectionPointEnum {

    CAPABILITY("capability"), REQUIREMENT("requirement");

    private String data;
    private static List<ConnectionPointEnum> connectionPointEnums = Arrays.asList(values());

    ConnectionPointEnum(String inData) {
        this.data = inData;
    }

    @Override
    public String toString() {
        return data;
    }

    public static ConnectionPointEnum getConnectionPointEnum(String data) {
        return connectionPointEnums.stream().filter(cp -> cp.toString().equals(data)).findAny().orElse(null);
    }
}
