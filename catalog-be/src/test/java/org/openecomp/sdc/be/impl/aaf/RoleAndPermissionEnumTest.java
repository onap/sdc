/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
