/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components;

import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import javax.servlet.ServletContext;

import static org.mockito.Mockito.when;
/**
 * tests GroupBusinessLogic class
 * @author ns019t
 *
 */
public class GroupBusinessLogicTest {

    ComponentsUtils componentsUtils;
    final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private static IGraphLockOperation graphLockOperation = Mockito.mock(IGraphLockOperation.class);
    private static User user = Mockito.mock(User.class);
    private static String componentId = "vfUniqueId-xxxx";
    @InjectMocks
    static GroupBusinessLogic bl = new GroupBusinessLogic();


    @Before
    public void setupBeforeMethod() {
        MockitoAnnotations.initMocks(this);
        ExternalConfiguration.setAppName("catalog-be");
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);

        bl.setComponentsUtils(componentsUtils);
    }
    @BeforeClass
    public static void setupBeforeClass() {
        when(graphLockOperation.lockComponent(componentId, ComponentTypeEnum.RESOURCE.getNodeType())).thenReturn(StorageOperationStatus.OK);
    }
}
