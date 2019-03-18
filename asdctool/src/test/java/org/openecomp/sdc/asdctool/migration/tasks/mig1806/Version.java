package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

public enum Version {

    MINOR(0), MAJOR(1806);

    private final int value;

    private Version(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
