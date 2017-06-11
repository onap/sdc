package org.openecomp.sdc.asdctool.impl.migration;

/**
 * for 1707 migration only!!!
 * please don't implement this interface unless you are sure you want to run with 1707 migration classes
 */
public interface Migration1707Task {

    /**
     * performs a migration operation
     * @return true if migration completed successfully or false otherwise
     */
    boolean migrate();

    /**
     *
     * @return a description of what this migration does
     */
    String description();

}
