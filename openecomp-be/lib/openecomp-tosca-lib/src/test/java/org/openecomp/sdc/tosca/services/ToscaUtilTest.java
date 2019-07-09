/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.tosca.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;

public class ToscaUtilTest {

    @Test
    public void testGetServiceTemplateFileNameServiceTemplateNull() {
        ServiceTemplate serviceTemplate = null;
        Assert.assertNull(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    }

    @Test
    public void testGetServiceTemplateFileNameMetadataNull() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        Assert.assertTrue(ToscaUtil.getServiceTemplateFileName(serviceTemplate)
                .endsWith(ToscaConstants.SERVICE_TEMPLATE_FILE_POSTFIX));
    }

    @Test
    public void testGetServiceTemplateFileName() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setMetadata(
                Collections.singletonMap(ToscaConstants.ST_METADATA_FILE_NAME, ToscaConstants.ST_METADATA_FILE_NAME));
        Assert.assertEquals(ToscaConstants.ST_METADATA_FILE_NAME,
                ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    }

    @Test
    public void testGetServiceTemplateFileNameMap() {
        Assert.assertTrue(ToscaUtil.getServiceTemplateFileName(new HashMap<>()).endsWith("ServiceTemplate.yaml"));
    }

    @Test
    public void testGetSubstitutableGroupMemberIdNodeTemplateNotPresent() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        Assert.assertFalse(ToscaUtil.getSubstitutableGroupMemberId("main.yaml", serviceTemplate).isPresent());
    }

    @Test
    public void testGetSubstitutableGroupMemberIdSubstitutable() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, Object> nodeTemplatePropertyMap = Collections.singletonMap(ToscaConstants
                .SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, Collections.singletonMap(ToscaConstants
                .SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, "mainSubServiceTemplateName"));

        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setProperties(nodeTemplatePropertyMap);
        Map<String, NodeTemplate> nodeTemplateMap = Collections.singletonMap(ToscaConstants
                .SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, nodeTemplate);
        serviceTemplate.getTopology_template().setNode_templates(nodeTemplateMap);

        Assert.assertTrue(ToscaUtil.getSubstitutableGroupMemberId("main.yaml", serviceTemplate).isPresent());
    }

    @Test
    public void testGetSubstitutableGroupMemberIdNotSubstitutable() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setTopology_template(new TopologyTemplate());

        Map<String, Object> nodeTemplatePropertyMap = Collections.singletonMap(ToscaConstants
                .SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME, "subServiceTemplateName");

        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setProperties(nodeTemplatePropertyMap);
        Map<String, NodeTemplate> nodeTemplateMap = Collections.singletonMap(ToscaConstants
                .SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, nodeTemplate);
        serviceTemplate.getTopology_template().setNode_templates(nodeTemplateMap);

        Assert.assertFalse(ToscaUtil.getSubstitutableGroupMemberId("main.yaml", serviceTemplate).isPresent());
    }

    @Test
    public void testAddServiceTemplateToMapWithKeyFileName() {
        Map<String, ServiceTemplate> serviceTemplateMap = new HashMap<>();
        ServiceTemplate serviceTemplate = new ServiceTemplate();

        ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplateMap, serviceTemplate);

        Assert.assertEquals(1, serviceTemplateMap.size());
    }

    @Test
    public void testGetServiceTemplateFileNameWithTemplateName() {
        Assert.assertEquals("nestedServiceTemplate.yaml", ToscaUtil.getServiceTemplateFileName("nested"));
    }
}
