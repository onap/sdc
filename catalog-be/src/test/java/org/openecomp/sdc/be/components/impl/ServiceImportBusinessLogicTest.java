/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.components.impl;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.impl.ComponentsUtils;

public class ServiceImportBusinessLogicTest {
    private final static String USER_ID = "jh0003";
    private final static String COMPONENT_ID = "componentId";
    private final static String VENDOR_RELEASE="vendorRelease";
    private final static String INVARIANT_UUID="InvariantUUID";

    @InjectMocks
    static ServiceImportBusinessLogic serviceImportBusinessLogic;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetComponentsUtils() {
        ComponentsUtils result;
        result = serviceImportBusinessLogic.getComponentsUtils();
    }

    @Test
    public void testSetComponentsUtils() {
        ComponentsUtils componentsUtils = null;

        serviceImportBusinessLogic.setComponentsUtils(componentsUtils);
    }


}