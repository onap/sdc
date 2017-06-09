package org.openecomp.sdc.asdctool.impl.migration;

public interface Migration {

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
