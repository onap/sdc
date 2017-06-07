package org.openecomp.sdc.be.dao;

import org.openecomp.sdc.be.config.ConfigurationManager;

public class DAOTitanStrategy implements TitanClientStrategy {

    @Override
    public String getConfigFile() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getTitanCfgFile();
    }

}
