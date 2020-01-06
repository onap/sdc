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

package org.openecomp.sdc.ci.tests.validation;


import static org.testng.Assert.assertEquals;

import com.aventstack.extentreports.Status;
import java.util.List;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

public final class VfValidation {

    private VfValidation() {
    }

    public static void verifyOnboardedVnfMetadata(String vspName, VendorSoftwareProductObject vspMetadata) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying metadata");

        assertEquals(ResourceGeneralPage.getNameText(), vspName, "VSP name is not valid.");
        List<WebElement> tagsList = ResourceGeneralPage.getElementsFromTagsTable();
        assertEquals(tagsList.size(), 1, "VSP tags size is not equal to 1.");
        assertEquals(tagsList.get(0).getText(), vspName, "VSP tag is not its name.");

        verifyMetadataIndifferentToFlow(vspMetadata);
    }

    public static void verifyOnboardedVnfMetadataAfterUpdateVNF(String vspName, VendorSoftwareProductObject vspMetadata) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying metadata after update VNF");

        // VF name should be updated only only if VF not certified
        if (Double.parseDouble(ResourceGeneralPage.getVersionUI()) < 1.0) {
            assertEquals(ResourceGeneralPage.getNameText(), vspName, "VSP name is not valid.");
            List<WebElement> tagsList = ResourceGeneralPage.getElementsFromTagsTable();
            assertEquals(tagsList.size(), 1, "VSP tags size is not equal to 1.");
            assertEquals(tagsList.get(0).getText(), vspName, "VSP tag is not its name.");
        }

        verifyMetadataIndifferentToFlow(vspMetadata);
    }

    public static void verifyMetadataIndifferentToFlow(VendorSoftwareProductObject vspMetadata) {
        assertEquals(ResourceGeneralPage.getDescriptionText(), vspMetadata.getDescription(),
            "VSP description is not valid.");

        String[] splitedSubCategorey = vspMetadata.getSubCategory().split("\\.");
        String expectedSubCategory = splitedSubCategorey[splitedSubCategorey.length - 1];
        String actualSubCategory = GeneralUIUtils.getSelectedElementFromDropDown(ResourceGeneralPage.getCategoryDataTestsIdAttribute()).getText().trim().toLowerCase();

        assertEquals(actualSubCategory, expectedSubCategory, "VSP category is not valid.");
        assertEquals(ResourceGeneralPage.getVendorNameText(), vspMetadata.getVendorName(),
            "VSP vendor name is not valid.");
        assertEquals(ResourceGeneralPage.getVendorReleaseText(), "1.0", "VSP version is not valid.");
        assertEquals(ResourceGeneralPage.getContactIdText(), vspMetadata.getAttContact(),
            "VSP attContact is not valid.");
    }

}
