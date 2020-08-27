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

import com.aventstack.extentreports.Status;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.verificator.CatalogVerificator;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.CheckBoxStatusEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.LifeCycleStateEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.frontend.ci.tests.datatypes.TopMenuButtonsEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.TypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.utilities.CatalogUIUtilitis;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertTrue;

@Test(singleThreaded = true)
public class CatalogLeftPanelTest extends SetupCDTest {

    private String filePath;

    @BeforeMethod
    public void beforeTest() {
        filePath = FileHandling.getFilePath("");
    }

    private static String[] resourceTypes = Arrays.stream(ResourceTypeEnum.class.getEnumConstants()).
            map(ResourceTypeEnum::name).toArray(String[]::new);

    private static String[] catalogTypes = {"RESOURCE", "SERVICE"};

    private static Object[][] provideData(String[] arObj) {
        Object[][] arObject = new Object[arObj.length][];

        int index = 0;
        for (Object obj : arObj) {
            arObject[index++] = new Object[]{obj};
        }
        return arObject;
    }

    @DataProvider(name = "Resource_Type_List")
    private static Object[][] resourceTypeList() {
        // Extract names of constants from enum as array of Strings
        ResourceTypeEnum[] resourceEnums = {ResourceTypeEnum.CP, ResourceTypeEnum.VF, ResourceTypeEnum.VFC, ResourceTypeEnum.VL};
        String[] resourcesForTest = Arrays.stream(resourceEnums).map(ResourceTypeEnum::name).toArray(String[]::new);
        return provideData(resourcesForTest);
    }

    @DataProvider(name = "Type_List")
    private static Object[][] typeList() {
        // Extract names of constants from enum as array of Strings
        Object[][] arObject = new Object[catalogTypes.length][];
        int index = 0;
        for (String catalogType : catalogTypes) {
            if (catalogType.equals("RESOURCE")) {
                arObject[index++] = new Object[]{catalogType, resourceTypes};
            } else {
                arObject[index++] = new Object[]{catalogType, new String[]{catalogType}};
            }
        }
        return arObject;
    }

    @DataProvider(name = "Status_List")
    private static Object[][] statusList() {
        CheckBoxStatusEnum[] checkboxes = {CheckBoxStatusEnum.CERTIFIED,
                CheckBoxStatusEnum.IN_DESIGN,
                CheckBoxStatusEnum.DISTRIBUTED,
                CheckBoxStatusEnum.IN_TESTING,
                CheckBoxStatusEnum.READY_FOR_TESTING};
        Object[][] arObject = new Object[checkboxes.length][];
        int index = 0;
        for (CheckBoxStatusEnum checkbox : checkboxes) {
            if (checkbox.equals(CheckBoxStatusEnum.CERTIFIED)) {
                arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.CERTIFIED, LifeCycleStateEnum.DISTRIBUTED, LifeCycleStateEnum.WAITING_FOR_DISTRIBUTION)};
            } else if (checkbox.equals(CheckBoxStatusEnum.IN_DESIGN)) {
                arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.CHECKIN, LifeCycleStateEnum.CHECKOUT)};
            } else if (checkbox.equals(CheckBoxStatusEnum.DISTRIBUTED)) {
                arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.DISTRIBUTED)};
            } else if (checkbox.equals(CheckBoxStatusEnum.IN_TESTING)) {
                arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.IN_TESTING)};
            } else if (checkbox.equals(CheckBoxStatusEnum.READY_FOR_TESTING)) {
                arObject[index++] = new Object[]{checkbox, Arrays.asList(LifeCycleStateEnum.READY_FOR_TESTING)};
            }
        }
        return arObject;
    }

    // filter by Type Resource in catalog
    @Test(dataProvider = "Type_List")
    public void filterByType(String catalogType, String[] classValues) throws Exception {
        setLog(catalogType);
//		getExtendTest().setDescription(catalogType);

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.catalogFilterTypeChecBox(TypesEnum.valueOf(catalogType));
        CatalogVerificator.validateType(TypesEnum.valueOf(catalogType));
    }

    @Test(dataProvider = "Resource_Type_List")
    public void filterByResourceType(String resourceType) throws Exception {
        setLog(resourceType);
//		getExtendTest().setDescription(resourceType);			

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.catalogFilterTypeChecBox(TypesEnum.valueOf(resourceType));

        CatalogVerificator.validateType(TypesEnum.valueOf(resourceType));
    }

    @Test(dataProvider = "Status_List")
    public void filterByStatus(CheckBoxStatusEnum statusCheckbox, List<LifeCycleStateEnum> lifecycleStates) throws Exception {
        setLog(statusCheckbox.name());
//		getExtendTest().setDescription(statusCheckbox.name());

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.CATEGORIES);
        CatalogUIUtilitis.catalogFilterStatusChecBox(statusCheckbox);
        //TODO check the test after removing lifecycle steps for resource
        CatalogVerificator.validateStatus(lifecycleStates, statusCheckbox.name());
    }

    @Test
    public void filterByUpperCategory() throws Exception {
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.TYPE);

        WebElement categoryCheckbox = CatalogUIUtilitis.clickOnUpperCategoryCheckbox();

        CatalogVerificator.validateCategory(categoryCheckbox.getAttribute("textContent").trim());
    }

    @Test
    public void filterByGenericDatabaseSubCategory() throws Exception {
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle.TYPE);

        WebElement checkboxElement = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.CatalogPageLeftPanelSubCategoryCheckbox.DATABASE.getValue()).get(0);
        String checkboxElementName = checkboxElement.getAttribute("textContent").trim();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s subcategory ...", checkboxElementName));
        GeneralUIUtils.clickOnAreaJS(checkboxElement);
        CatalogVerificator.validateSubCategory("Generic", checkboxElementName);
    }

    @Test(priority = 1)
    public void lastUpdatedService() throws Exception {
        // create service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        ResourceGeneralPage.clickCheckinButton(serviceMetadata.getName());
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
        String firstElementName = cardElements.get(0).getAttribute("textContent").trim();
        assertTrue(String.format("Wrong element name, Expected : %s , Actual: %s", serviceMetadata.getName(), firstElementName), serviceMetadata.getName().equals(firstElementName));
    }

    @Test(priority = 17)
    public void lastUpdatedResource() throws Exception {
        // create resource
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());

        ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
        String firstElementName = cardElements.get(0).getAttribute("textContent").trim();
        assertTrue(String.format("Wrong element name, Expected : %s , Actual: %s", vfMetaData.getName(), firstElementName), vfMetaData.getName().equals(firstElementName));
    }

    @Test(priority = 5)
    public void fromCatalogCheckout() throws Exception {
        // create resource
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());

        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        GeneralUIUtils.findComponentAndClick(vfMetaData.getName());
        ResourceGeneralPage.clickCheckoutButton();
        ResourceGeneralPage.clickCheckinButton(vfMetaData.getName());
    }

    @Test
    public void keepSearchResultsInCatalogAfterBrowserBack() throws Exception {
        ResourceReqDetails resourceMetadata = ElementFactory.getDefaultResource();
        ResourceUIUtils.createVF(resourceMetadata, getUser());
        ResourceGeneralPage.clickCheckinButton(resourceMetadata.getName());
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        CatalogUIUtilitis.catalogSearchBox(resourceMetadata.getName());
        GeneralUIUtils.findComponentAndClick(resourceMetadata.getName());

        GeneralUIUtils.clickOnBrowserBackButton();

        int numOfElementsInFilteredCatalog = CatalogVerificator.getNumberOfElementsFromCatalogHeader();
        assertTrue(String.format("Wrong number fo elements, Expected : %s , Actual: %s", 1, numOfElementsInFilteredCatalog), numOfElementsInFilteredCatalog == 1);

        List<WebElement> cardElements = GeneralUIUtils.getElementsByCSS(DataTestIdEnum.DashboardCardEnum.INFO_NAME.getValue());
        String firstElementName = cardElements.get(0).getAttribute("textContent").trim();
        assertTrue(String.format("Wrong element name, Expected : %s , Actual: %s", resourceMetadata.getName(), firstElementName), resourceMetadata.getName().equals(firstElementName));
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
