/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;

@ExtendWith(MockitoExtension.class)
class ServiceCsarInfoTest {

    private ServiceCsarInfo csarInfo;

    @Mock
    private User user;

    private static final String CSAR_UUID = "csarUUID";
    private static final String PAYLOAD_NAME = "csars/serviceWithUnknownDataTypes.csar";
    private static final String SERVICE_NAME = "serviceWithDataType";
    private static final String MAIN_TEMPLATE_NAME = "Definitions/service-Servicewithdatatype-template.yml";

    @BeforeEach
    void setup() throws ZipException, URISyntaxException {
        csarInfo = createCsarInfo(PAYLOAD_NAME, MAIN_TEMPLATE_NAME);

        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    private ServiceCsarInfo createCsarInfo(final String csarFileName, final String mainTemplateName) throws URISyntaxException, ZipException {
        final File csarFile = new File(ServiceCsarInfoTest.class.getClassLoader().getResource(csarFileName).toURI());
        final Map<String, byte[]> payload = ZipUtils.readZip(csarFile, false);
        String mainTemplateContent = new String(payload.get(mainTemplateName));

return new ServiceCsarInfo(user, CSAR_UUID, payload, SERVICE_NAME, mainTemplateName, mainTemplateContent, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetDataTypes() {
        final Map<String, Object> dataTypes = csarInfo.getDataTypes();
        assertEquals(184, dataTypes.size());
        final Map<String, Object> dataTypeDefinition = (Map<String, Object>) dataTypes.get("tosca.datatypes.test_g");
        assertNotNull(dataTypeDefinition);
        assertEquals("tosca.datatypes.Root", dataTypeDefinition.get("derived_from"));
        assertEquals("tosca.datatypes.test_h",
                ((Map<String, Object>) ((Map<String, Object>) dataTypeDefinition.get("properties")).get("prop2")).get("type"));
    }

}
