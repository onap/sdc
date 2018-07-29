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

package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class PathName extends SetupCDTest {

    protected static String filePath = FileHandling.getFilePath("ComplexService");
    private static String fullCompositionFile = "fullComposition.zip";
    private static String fullCompositionFile2 = "fullCompositionNew.zip";
    private static String HSSFile = "HSS.zip";
    private static String VMMEFile = "VMME.zip";
    private static String makeDistributionValue;

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        makeDistributionValue = makeDistributionReadValue;
    }

    //------------------------------------------Tests-----------------------------------------------------

    // Test#8 Jira issue 6168
    @Test
    public void ValidateSameNameTest() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        PathUtilities.createService(getUser());
        String firstPathName = PathUtilities.createPath("Oren",vspName);
        PathUtilities.createPathWithoutLink(firstPathName,vspName);
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        } catch (Exception e) {
            throw new Exception("when creating another path with duplicate name, expected error did not appear");
        }
    }

    // Test#9 Jira issue 6183
    @Test
    public void ValidateEditName() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        PathUtilities.createService(getUser());
        String firstPathName = PathUtilities.createPath("Oren",vspName);
        String secondPathName = PathUtilities.createPathWithoutLink("blabla",vspName);
        PathValidations.validateEditToExistingName(firstPathName,secondPathName);
    }

    // Test#10 Jira issue 6411
    @Test
    public void SpacesName() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        PathUtilities.createService(getUser());
        PathUtilities.createPath("              ", vspName);
        try {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        } catch (Exception e) {
            throw new Exception("service path name cannot be empty or spaces ");
        }
    }

    // Test#11 Jira issue 6186
    @Test
    public void ValidateNameWithSpaces() throws Exception {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        String vspName = onboardAndCertify(resourceReqDetails, filePath, fullCompositionFile);
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        PathUtilities.createService(getUser());
        PathValidations.validateNameWithSpaces("New", vspName);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                               flow methods                                         //
    ////////////////////////////////////////////////////////////////////////////////////////

    // workflow leading to path
    public String onboardAndCertify(ResourceReqDetails resourceReqDetails, String filePath, String vnfFile) throws Exception {
        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUiUtils.onboardAndValidate(resourceReqDetails, filePath, vnfFile, getUser());
        String vspName = vendorSoftwareProductObject.getName();

        DeploymentArtifactPage.getLeftPanel().moveToCompositionScreen();
        ExtentTestActions.addScreenshot(Status.INFO, "TopologyTemplate_" + vnfFile, "The topology template for " + vnfFile + " is as follows : ");

        DeploymentArtifactPage.clickSubmitForTestingButton(vspName);
        SetupCDTest.getExtendTest().log(Status.INFO, "relogin as TESTER");
        reloginWithNewRole(UserRoleEnum.TESTER);
        GeneralUIUtils.findComponentAndClick(vspName);
        TesterOperationPage.certifyComponent(vspName);
        return vspName;
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}