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

package org.onap.sdc.frontend.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import org.onap.sdc.backend.ci.tests.datatypes.ProductReqDetails;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.ProductGeneralPage;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author al714h
 */

public class ProductUIUtils {

    protected static WebDriver driver;

    private ProductUIUtils() {
        super();
    }

    private static void fillProductGeneralPage(ProductReqDetails product, User user) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Fill in metadata values in general page.. "));
        ProductGeneralPage.defineName(product.getName());
        ProductGeneralPage.defineFullName(product.getFullName());
        ProductGeneralPage.defineDescription(product.getDescription());
        ProductGeneralPage.defineProjectCode(product.getProjectCode());
        defineTagsList2(product.getTags());
        ProductGeneralPage.defineContactId(product.getContactId());
        GeneralUIUtils.clickSomewhereOnPage();
    }

    public static void createProduct(ProductReqDetails product, User user) throws Exception {
        clikAddProduct();
        fillProductGeneralPage(product, user);
        GeneralPageElements.clickCreateButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Product %s created", product.getName()));
    }

    private static void defineTagsList2(List<String> productTags) {
        WebElement productTagsTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ProductMetadataEnum.TAGS.getValue());
        for (String tag : productTags) {
            productTagsTextbox.clear();
            productTagsTextbox.sendKeys(tag);
            GeneralUIUtils.waitForAngular();
            productTagsTextbox.sendKeys(Keys.ENTER);
        }
    }

    private static void clikAddProduct() {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking Add Product button"));
        try {
            GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.ADD_AREA.getValue());
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_PRODUCT.getValue()).click();
            GeneralUIUtils.ultimateWait();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Exception on catched on Add Product button, retrying ..."));
            GeneralUIUtils.hoverOnAreaByClassName("w-sdc-dashboard-card-new");
            GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.Dashboard.BUTTON_ADD_PRODUCT.getValue()).click();
            GeneralUIUtils.ultimateWait();
        }
    }

}
