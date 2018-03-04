package org.openecomp.sdc.asdctool.migration.resolver;


import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import java.util.List;

import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;

public interface MigrationResolver {

    /**
     *
     * @return a list of {@code T}
     */
    List<IMigrationStage> resolveMigrations();

}
