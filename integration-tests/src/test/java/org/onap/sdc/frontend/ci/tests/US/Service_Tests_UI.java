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

package org.onap.sdc.frontend.ci.tests.US;

import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.OnboardingUiUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class Service_Tests_UI extends SetupCDTest {

    public Service_Tests_UI() {
    }

    // US839610 - E2E Declare VL / CP properties as inputs in service level
    @Test
    public void declareVL_CP_InputsInServiceLevel() throws Exception {
        String vnfFile = "FDNT.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, FileHandling.getVnfRepositoryPath(), vnfFile, getUser());
        ServiceReqDetails servicemetadata = ElementFactory.getDefaultService(getUser());
        ServiceUIUtils.createService(servicemetadata);
        GeneralUIUtils.moveToStep(StepsEnum.COMPOSITION);
        CanvasManager service_CanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(vendorSoftwareProductObject.getName());
        GeneralUIUtils.waitForLoader();
        CanvasElement vfi_Element = service_CanvasManager.createElementOnCanvas(vendorSoftwareProductObject.getName());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue())
                .click();
        GeneralUIUtils.findComponentAndClick(servicemetadata.getName());
        GeneralUIUtils.moveToStep(StepsEnum.INPUTS);
        GeneralUIUtils.getWebElementByTestID("inputs-vf-instance-1").click();
//		GeneralUIUtils.onNameClicked(input);
    }

    @Test
    public void CreateServiceWithCpInstance() throws Exception {
        String vnfFile = "FDNT.zip";
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, FileHandling.getVnfRepositoryPath(), vnfFile, getUser());
        ServiceReqDetails servicemetadata = ElementFactory.getDefaultService(getUser());
        ServiceUIUtils.createService(servicemetadata);
        GeneralUIUtils.moveToStep(StepsEnum.COMPOSITION);
        CanvasManager service_CanvasManager = CanvasManager.getCanvasManager();
        CompositionPage.searchForElement(vendorSoftwareProductObject.getName());
        GeneralUIUtils.waitForLoader();
        CanvasElement vfi_Element = service_CanvasManager.createElementOnCanvas(vendorSoftwareProductObject.getName());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue())
                .click();
        GeneralUIUtils.findComponentAndClick(servicemetadata.getName());
        GeneralUIUtils.moveToStep(StepsEnum.DEPLOYMENT_VIEW);
        String version = GeneralUIUtils.getWebElementByTestID("versionHeader").getText();
        RestResponse service = ServiceRestUtils.getServiceByNameAndVersion(getUser(), servicemetadata.getName(),
                version.substring(1));
        List<String> serviceResponseArray = new ArrayList<>();
        serviceResponseArray = LocalGeneralUtilities.getValuesFromJsonArray(service);
        servicemetadata.setUniqueId(serviceResponseArray.get(0));
        RestResponse serviceResponse = ServiceRestUtils.getService(servicemetadata, getUser());
        if (serviceResponseArray.get(0).contains("VL")) {
            System.out.println("OK");
        }

    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }


}
