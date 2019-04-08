/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.core.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToscaSolConverterPnfMultipleNodeTemplatesTest {

    private static final String PNF_EXT_CP_1 = "pnfExtCp_1";
    private static final String PNF_EXT_CP_2 = "pnfExtCp_2";

    @Test
    public void testGivenDescriptorWithPnfAndTwoPnfExts_WhenConvertTopologyTemplate_ThenTwoExtCpsInOutput() throws IOException {
        // Added this as separate test as data-driven tests compare strings and as order of nodeTemplates
        // can be different in hashMap and hence test may fail
        final byte[] descriptor = getFileResource("pnfDescriptor/other/pnfDescriptor_PnfAnd2ExtCps.yaml");
        ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        ToscaSolConverterPnf toscaSolConverter = new ToscaSolConverterPnf();
        toscaSolConverter.convertTopologyTemplate(serviceTemplate, serviceTemplateReaderService);
        Map<String, NodeTemplate> nodeTemplates = serviceTemplate.getTopology_template().getNode_templates();
        assertEquals(2, nodeTemplates.size());

        nodeTemplates.entrySet().stream()
                .map(e -> e.getKey())
                .forEach((key -> assertTrue(getErrorString(), hasCorrectName(key))));
    }

    private byte[] getFileResource(String filePath) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        return IOUtils.toByteArray(inputStream);
    }

    private boolean hasCorrectName(String name) {
        return PNF_EXT_CP_1.equals(name) || PNF_EXT_CP_2.equals(name);
    }

    private String getErrorString() {
        return "node template name should be either " + PNF_EXT_CP_1  + " or " + PNF_EXT_CP_2;
    }
}
