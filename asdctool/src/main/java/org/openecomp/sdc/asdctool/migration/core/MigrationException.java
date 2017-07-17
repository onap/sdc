package org.openecomp.sdc.asdctool.migration.core;

public class MigrationException extends RuntimeException {

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, RuntimeException e) {
        super(message, e);
    }

}
