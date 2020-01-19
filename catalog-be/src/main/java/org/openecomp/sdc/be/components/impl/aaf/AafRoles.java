package org.openecomp.sdc.be.components.impl.aaf;

import org.openecomp.sdc.be.config.ConfigurationManager;

public enum AafRoles {

        READ_ONLY("app.readonly"),
        ALL("app.all");

        private String role;

        AafRoles(String roleSuffix) {
            this.role = ConfigurationManager.getConfigurationManager().getConfiguration().getAafNamespace() + "." + roleSuffix;
        }

        public String getRole() {
            return role;
        }

}
