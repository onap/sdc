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

package org.openecomp.sdc.ci.tests.execute.sanity;

import static org.junit.Assert.assertEquals;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Vf extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Vf.class);

    private String filePath;

    @BeforeClass
    public void beforeClass() {
        filePath = FileHandling.getFilePath("");
    }

    @BeforeMethod
    public void beforeTest() {
        System.out.println("File repository is : " + filePath);
        getExtendTest().log(Status.INFO, "File repository is : " + filePath);
    }

    @Test
    public void changeInstanceNameInVfTest() {
        final ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        GeneralPageElements.getLeftMenu().moveToCompositionScreen();
        final CanvasManager vfCanvasManager = CanvasManager.getCanvasManager();
        final CanvasElement computeElement = vfCanvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);
        final String updatedInstanceName = "updatedName";
        vfCanvasManager.updateElementNameInCanvas(computeElement, updatedInstanceName);
        assertEquals(CompositionPage.getSelectedInstanceName(), updatedInstanceName);
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
