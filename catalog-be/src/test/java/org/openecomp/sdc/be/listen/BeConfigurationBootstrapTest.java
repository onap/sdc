/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.listen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class BeConfigurationBootstrapTest {

    @Test
    void startLoadsTheConfigurationManager() {
        ConfigurationManager configurationManager = BeConfigurationBootstrap.start(
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));

        assertSame(configurationManager, ConfigurationManager.getConfigurationManager());
        assertNotNull(configurationManager.getConfiguration().getBeFqdn());
    }

    @Test
    void tlsFilesInOneDirectoryShareAWatch(@TempDir File certs, @TempDir File ca) throws IOException {
        for (File file : List.of(new File(certs, "cert.pem"), new File(certs, "key.pem"), new File(certs, "ca.pem"), new File(ca, "ca.pem"))) {
            assertTrue(file.createNewFile());
        }
        Configuration configuration = new Configuration();
        configuration.setTlsCert(new File(certs, "cert.pem").getPath());
        configuration.setTlsKey(new File(certs, "key.pem").getPath());
        configuration.setCaCert(new File(ca, "ca.pem").getPath());

        Map<String, IOFileFilter> filters = BeConfigurationBootstrap.createTlsFileFilters(configuration);

        assertEquals(2, filters.size());
        assertTrue(filters.get(certs.getPath()).accept(certs, "cert.pem"));
        assertTrue(filters.get(certs.getPath()).accept(certs, "key.pem"));
        assertFalse(filters.get(certs.getPath()).accept(certs, "ca.pem"));
        assertTrue(filters.get(ca.getPath()).accept(ca, "ca.pem"));
    }

    @Test
    void noTlsFilesNoWatch() {
        assertTrue(BeConfigurationBootstrap.createTlsFileFilters(new Configuration()).isEmpty());
    }
}
