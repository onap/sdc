package org.openecomp.sdc.be.components.impl.aaf;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;

public enum AafPermission {

    READ(PermNames.READ_VALUE),
    WRITE(PermNames.WRITE_VALUE),
    DELETE(PermNames.DELETE_VALUE),
    INTERNAL_ALL(PermNames.INTERNAL_ALL_VALUE);

    private String permission;
    private String permissionSuffix;

    AafPermission(String permissionSuffix) {
        this.permissionSuffix = permissionSuffix;
        this.permission = String.format("%s.%s",
                ConfigurationManager.getConfigurationManager().getConfiguration().getAafNamespace(),
                permissionSuffix);
    }

    public String getFullPermission() {
        return permission;
    }

    public static AafPermission getEnumByString(String perm) {
        for (AafPermission e : AafPermission.values()) {
            if (perm.equals(e.getPermissionSuffix()))
                return e;
        }
        throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY, perm);
    }

    public String getPermissionSuffix() {
        return this.permissionSuffix;
    }

    public static class PermNames {
        public static final String READ_VALUE = "endpoint.api.access|*|read";
        public static final String WRITE_VALUE = "endpoint.api.access|*|write";
        public static final String DELETE_VALUE = "endpoint.api.access|*|delete";
        public static final String INTERNAL_ALL_VALUE = "endpoint.api.internal.access|*|all";
    }
}
