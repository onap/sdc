package org.openecomp.sdc.be.impl.aaf;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.AafRoles;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RoleAndPermissionEnumTest {
    private static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    private static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
    private final String prefix = ".app.";

    @Test
    public void getRoleReadOnly() {
        Assert.assertEquals(configurationManager.getConfiguration().getAafNamespace() + prefix + "readonly", AafRoles.READ_ONLY.getRole());
    }

    @Test
    public void getRoleAll() {
        Assert.assertEquals(configurationManager.getConfiguration().getAafNamespace() + prefix + "all", AafRoles.ALL.getRole());
    }

    @Test
    public void testGetEnumByStringWithExistingValue() {
        Assert.assertEquals(AafPermission.getEnumByString(AafPermission.PermNames.READ_VALUE),
                AafPermission.READ);
        Assert.assertEquals(AafPermission.getEnumByString(AafPermission.PermNames.WRITE_VALUE),
                AafPermission.WRITE);
        Assert.assertEquals(AafPermission.getEnumByString(AafPermission.PermNames.DELETE_VALUE),
                AafPermission.DELETE);
    }

    @Test
    public void testGetEnumByStringNonExistingValue() {
        ComponentException thrown = (ComponentException) catchThrowable(()-> AafPermission.getEnumByString("stam"));
        assertThat(thrown.getActionStatus()).isEqualTo(ActionStatus.INVALID_PROPERTY);
        assertThat(thrown.getParams()[0]).isEqualTo("stam");
    }

}
