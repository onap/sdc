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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.backend.ci.tests.datatypes.ProductReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.ProductGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.TesterOperationPage;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ProductUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.testng.annotations.Test;

/**
 * @author al714h
 */

public class Product extends SetupCDTest {


    @Test
    public void createProductAndAddCertifiedServiceInstance() throws Exception {
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();

        ServiceUIUtils.createService(serviceMetadata);
        GeneralPageElements.clickSubmitForTestingButton(serviceMetadata.getName());
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
        TesterOperationPage.certifyComponent(serviceMetadata.getName());
        reloginWithNewRole(UserRoleEnum.PRODUCT_MANAGER1);
        ProductUIUtils.createProduct(productReqDetails, getUser());
        ProductGeneralPage.getProductLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement canvasElement = CompositionPage.addElementToCanvasScreen(serviceMetadata.getName(), canvasManager);
        canvasManager.clickOnCanvaElement(canvasElement);
    }

    @Test
    public void loginAsProductStrateger() throws Exception {
        reloginWithNewRole(UserRoleEnum.PRODUCT_STRATEGIST1);
    }

}
