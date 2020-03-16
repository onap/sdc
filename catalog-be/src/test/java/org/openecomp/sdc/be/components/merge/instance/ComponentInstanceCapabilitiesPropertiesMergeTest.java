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

package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;

import java.util.Collections;
import java.util.List;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceCapabilitiesPropertiesMergeTest {

    @InjectMocks
    private ComponentInstanceCapabilitiesPropertiesMerge testInstance;

    @Mock
    private ComponentCapabilitiesPropertiesMergeBL capabilitiesPropertiesMergeBL;

    @Mock
    private ComponentsUtils componentsUtils;

    private DataForMergeHolder mergeHolder;

    private Resource origInstanceNode;
    private List<CapabilityDefinition> origInstanceCapabilities;

    @Before
    public void setUp() throws Exception {
        origInstanceNode = new Resource();
        origInstanceCapabilities = Collections.emptyList();
        mergeHolder = new DataForMergeHolder();
        mergeHolder.setOrigInstanceNode(origInstanceNode);
        mergeHolder.setOrigInstanceCapabilities(origInstanceCapabilities);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    public void mergeDataAfterCreate() {
        Service currentComponent = new Service();
        when(capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(currentComponent, origInstanceNode, "instId", origInstanceCapabilities))
            .thenReturn(ActionStatus.OK);
        Component mergeResult = testInstance.mergeDataAfterCreate(new User(), mergeHolder, currentComponent, "instId");
        assertTrue(mergeResult != null);
    }

    @Test(expected = ComponentException.class)
    public void mergeDataAfterCreate_error() {
        Service currentComponent = new Service();
        when(capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(currentComponent, origInstanceNode, "instId", origInstanceCapabilities))
                .thenReturn(ActionStatus.GENERAL_ERROR);
        testInstance.mergeDataAfterCreate(new User(), mergeHolder, currentComponent, "instId");
    }

    @Test
    public void testSaveDataBeforeMerge() {
        DataForMergeHolder dataHolder = new DataForMergeHolder();
		Component containerComponent = new Resource();
		ComponentInstance currentResourceInstance = new ComponentInstance();
		Component originComponent = new Resource();
		testInstance.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, originComponent);
    }
}
