package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.TitanClientStrategy;

public class MigrationTitanStrategy implements TitanClientStrategy {

    @Override
    public String getConfigFile() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getTitanMigrationKeySpaceCfgFile();
    }

}
