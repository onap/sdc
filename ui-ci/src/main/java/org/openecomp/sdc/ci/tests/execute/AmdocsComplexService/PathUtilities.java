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
import com.clearspring.analytics.util.Pair;
import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;


public class PathUtilities {

    private static final int WAITING_FOR_LOADRE_TIME_OUT = 60 * 10;
    private static final int NUMBER_OF_LINKS = 3;

    static void openPathList() throws Exception {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_LIST_BUTTON.getValue());
    }

    public static ServiceReqDetails createService(User user) throws Exception {
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);
        ServiceGeneralPage.getLeftMenu().moveToCompositionScreen();
        GeneralUIUtils.ultimateWait();
        return serviceMetadata;
    }

    public static List<CanvasElement> linkVFs(String vspName, int linksNum) throws Exception {
        CompositionPage.searchForElement(vspName);
        GeneralUIUtils.ultimateWait();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        GeneralUIUtils.ultimateWait();
        List<CanvasElement> VFs = new ArrayList<CanvasElement>();
        VFs.add(canvasManager.createElementOnCanvas(vspName));
        for (int i = 1; i < linksNum; i++) {
            VFs.add(canvasManager.createElementOnCanvas(vspName));
            GeneralUIUtils.ultimateWait();
//            for(int a=0; a<3; a++)
//                try {
//                    canvasManager.linkElements(VFs.get(i), CircleSize.VF, VFs.get(i - 1), CircleSize.VF);
//                    break;
//                } catch (Exception ignore) {}
            SetupCDTest.getExtendTest().log(Status.INFO, "link VFs");
            GeneralUIUtils.ultimateWait();
        }
        return VFs;
    }

    public static List<CanvasElement> linkServices(String service1, String service2, int linksNum) throws Exception {
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        List<CanvasElement> VFs = new ArrayList<CanvasElement>();

        // get first service
        CompositionPage.searchForElement(service1);
        VFs.add(canvasManager.createElementOnCanvas(service1));

        String service = service2;
        for (int i = 1; i < linksNum; i++) {
            CompositionPage.searchForElement(service);
            VFs.add(canvasManager.createElementOnCanvas(service));
            GeneralUIUtils.ultimateWait();
//            for(int a=0; a<3; a++)
//                try {
//                    canvasManager.linkElements(VFs.get(i), CircleSize.SERVICE, VFs.get(i-1), CircleSize.SERVICE);
//                    break;
//                } catch (Exception ignore) {}
            SetupCDTest.getExtendTest().log(Status.INFO, "link services");
            GeneralUIUtils.ultimateWait();

            // change service to link
            if (service.equals(service2)) {
                service = service1;
            } else {
                service = service2;
            }
        }
        return VFs;
    }

    public static void openCreatePath() throws Exception {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.PATH_MENU_BUTTON.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_PATH_MENU_BUTTON.getValue());
    }

    public static void sendValue(String DataTestId, String value) throws Exception {
        GeneralUIUtils.getWebElementByTestID(DataTestId).sendKeys(value);
        GeneralUIUtils.ultimateWait();
    }

    public static void insertValues(String pathName, String pathProtocol, String pathPortNumbers) throws Exception {
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue(), pathName);
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_PROTOCOL.getValue(), pathProtocol);
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_PORT_NUMBER.getValue(), pathPortNumbers);
        GeneralUIUtils.ultimateWait();
    }

    public static void selectFirstLineParam() throws Exception {
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.LINK_SOURCE.getValue() + "']//option").get(0).click();
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.LINK_SOURCE_CP.getValue() + "']//option").get(0).click();
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET.getValue() + "']//option").get(0).click();
        GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET_CP.getValue() + "']//option").get(0).click();
    }

    public static void editPathName(String pathName, String newName) throws Exception {
        GeneralUIUtils.findElementsByXpath("//*[text()='" + pathName + "']/parent::*//span").get(0).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue()).clear();
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_NAME.getValue(), newName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.SAVE.getValue());
    }

    public static void editPathProtocol(String pathName, String newProtocol) throws Exception {
        GeneralUIUtils.findElementsByXpath("//*[text()='" + pathName + "']/parent::*//span").get(0).click();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.PATH_PROTOCOL.getValue()).clear();
        sendValue(DataTestIdEnum.ComplexServiceAmdocs.PATH_PROTOCOL.getValue(), newProtocol);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.SAVE.getValue());
    }

    public static int deleteLines(int numOfLinesToDelete, int numOfLines) throws Exception {
        for (int i = 0; i < numOfLinesToDelete; i++) {
            GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.REMOVE_LINK.getValue() + "']//span").get(0).click();
            numOfLines--;
        }
        if (GeneralUIUtils.findElementsByXpath("//*[@data-tests-id='" + DataTestIdEnum.ComplexServiceAmdocs.LINK_TARGET_CP + "']//option").size() > (numOfLines + 1)) {
            throw new Exception("Path element was not deleted");
        }
        GeneralUIUtils.ultimateWait();
        return numOfLines;
    }

    public static String createPath(String pathName, String vspName) throws Exception {
        linkVFs(vspName, NUMBER_OF_LINKS);
        openCreatePath();
        insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, "path" + pathName + " has been created");
        return pathName;
    }

    public static void deleteComponents(List<CanvasElement> elements) throws Exception {
        for (CanvasElement element : elements) {
            CanvasManager.getCanvasManager().clickOnCanvaElement(element);
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.DELETE_COMPONENT.getValue());
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
        }
    }

    public static void updateVF(String vspName, VendorSoftwareProductObject vendorSoftwareProduct) throws Exception {
        boolean vspFound = HomePage.searchForVSP(vspName);
        if (vspFound) {
            final List<WebElement> elementsFromTable = GeneralPageElements.getElementsFromTable();
            elementsFromTable.get(1).click();
            GeneralUIUtils.waitForLoader();
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.UPDATE_VSP.getValue());

            OnboardingUiUtils.doCheckOut();

            //Metadata verification
            VfVerificator.verifyOnboardedVnfMetadata(vspName, vendorSoftwareProduct);

            ExtentTestActions.log(Status.INFO, "Clicking create/update VNF");
            String duration = GeneralUIUtils.getActionDuration(() -> waitUntilVnfCreated());
            ExtentTestActions.log(Status.INFO, "Succeeded in importing/updating " + vspName, duration);
        } else {
            Assert.fail("Did not find VSP named " + vspName);
        }
    }

    public static void waitUntilVnfCreated() {
        GeneralUIUtils.clickOnElementByTestIdWithoutWait(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
        GeneralUIUtils.waitForLoader(WAITING_FOR_LOADRE_TIME_OUT);
        GeneralUIUtils.waitForAngular();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
    }

    public static void deleteComponent(CanvasElement element) throws Exception {
        CanvasManager.getCanvasManager().clickOnCanvaElement(element);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.DELETE_COMPONENT.getValue());
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
    }

    public static void submitForTesting() {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.SUBMIT_FOR_TESTING.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ComplexServiceAmdocs.CHANGE_LIFE_CYCLE_MESSAGE.getValue()).sendKeys("new service to certify");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.OK.getValue());
    }

    public static String createPathWithoutLink(String pathName, String vspName) throws Exception {
        openCreatePath();
        insertValues(pathName, "pathProtocol1", "pathPortNumbers1");
        selectFirstLineParam();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ComplexServiceAmdocs.CREATE_BUTTON.getValue());
        SetupCDTest.getExtendTest().log(Status.INFO, "path" + pathName + " has been created");
        return pathName;
    }

    // rest apis
    private static String getServiceUUIDfromCompositionURL() throws Exception {
        String url = SetupCDTest.getDriver().getCurrentUrl();
        String[] result = url.split("/");
        for (int i = 0; i < result.length; i++) {
            if (result[i].equals("workspace")) {
                return result[i + 1];
            }
        }
        throw new Exception("service uuid not found in the url");
    }

    private static Map<String, String> prepareHeadersMap(String userId) {
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
        headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
        headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
        return headersMap;
    }

    public static Pair<RestResponse, ServiceReqDetails> getServiceIDByNameAndVersion(String serviceName, String version) throws Exception {
        User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

        RestResponse restResponse = ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails, serviceName, version);
        ServiceReqDetails service = new ServiceReqDetails();
        service.setName(serviceName);
        service.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
        service.setVersion(version);

        return new Pair<>(restResponse, service);
    }

    public static RestResponse createServiceAPI(User sdncUserDetails, ServiceReqDetails serviceDetails) throws Exception {
        String serviceBaseVersion = "0.1";
        RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
        assertEquals("Check API response code for CreateServiceAPI call", HttpStatus.SC_CREATED, restResponse.getErrorCode().intValue());
        return restResponse;
    }

    public static RestResponse getServiceAPI(User sdncUserDetails, ServiceReqDetails serviceDetails) throws Exception {
        String serviceBaseVersion = "0.1";
        // choose user
        // User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

        // create serviceDetails
        // ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

        // get service
        RestResponse restResponse = ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails, serviceDetails.getName(),
                serviceBaseVersion);
        assertEquals("Check API response code for GetServiceAPI call", HttpStatus.SC_OK, restResponse.getErrorCode().intValue());

        Service service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());
        String uniqueId = service.getUniqueId();
        serviceDetails.setUniqueId(uniqueId);
        // ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, service, sdncUserDetails, (LifecycleStateEnum) null);
        return restResponse;
    }

    public static RestResponse getServiceForwardingPathsAPI(String serviceName) throws Exception {
        User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

        // get service
        Pair<RestResponse, ServiceReqDetails> servicePaths = getServiceIDByNameAndVersion(serviceName, "0.1");

        // set url
        Config config = Utils.getConfig();
        String url = String.format(
                PathUrls.SERVICE_FORWARDING_PATHS,
                config.getCatalogBeHost(),
                config.getCatalogBePort(),
                servicePaths.right.getUniqueId()
        );
        String userId = sdncUserDetails.getUserId();

        Map<String, String> headersMap = prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse restResponse = http.httpSendGet(url, headersMap);
        assertEquals("Check API response code for GetServiceForwardingPathsAPI call", HttpStatus.SC_OK, restResponse.getErrorCode().intValue());
        return restResponse;
    }

    public static RestResponse getServicePathLinkMapAPI(String serviceName) throws Exception {
        User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

        // get service
        Pair<RestResponse, ServiceReqDetails> servicePaths = getServiceIDByNameAndVersion(serviceName, "0.1");

        // set url
        Config config = Utils.getConfig();
        String url = String.format(
                PathUrls.SERVICE_PATH_LINK_MAP,
                config.getCatalogBeHost(),
                config.getCatalogBePort(),
                servicePaths.right.getUniqueId()
        );

        String userId = sdncUserDetails.getUserId();

        Map<String, String> headersMap = prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse restResponse = http.httpSendGet(url, headersMap);
        assertEquals("Check API response code for GetServiceForwardingPathsAPI call", HttpStatus.SC_OK, restResponse.getErrorCode().intValue());
        return restResponse;
    }

    public static Pair<RestResponse, ServiceReqDetails> getServicePathsAPI(String serviceName) throws Exception {
        User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        // get service
        Pair<RestResponse, ServiceReqDetails> servicePaths = getServiceIDByNameAndVersion(serviceName, "0.1");

        // set url
        Config config = Utils.getConfig();
        String url = String.format(
                PathUrls.GET_SERVICE_PATHS,
                config.getCatalogFeHost(),
                config.getCatalogFePort(),
                servicePaths.right.getUniqueId());

        String userId = sdncUserDetails.getUserId();

        Map<String, String> headersMap = prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse restResponse = http.httpSendGet(url, headersMap);
        assertEquals("Check API response code for GetServiceForwardingPathsAPI call", HttpStatus.SC_OK, restResponse.getErrorCode().intValue());
        return new Pair<>(restResponse, servicePaths.right);
    }

    public static Pair<RestResponse, OnboardItemObject> createNewItemVersion(String itemId, String itemVersion, String description, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(
                PathUrls.CREATE_NEW_ITEM_VERSION,
                config.getCatalogBeHost(),
                config.getCatalogBePort(),
                itemId, itemVersion);
        String userId = user.getUserId();
        OnboardItemObject onboardItemObject = new OnboardItemObject();

        onboardItemObject.setCreationMethod("major");
        onboardItemObject.setDescription(description);

        Map<String, String> headersMap = prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        Gson gson = new Gson();
        String body = gson.toJson(onboardItemObject);

        RestResponse response = http.httpSendPost(url, body, headersMap);

        onboardItemObject.setItemId(ResponseParser.getValueFromJsonResponse(response.getResponse(), "id"));
        onboardItemObject.setName(ResponseParser.getValueFromJsonResponse(response.getResponse(), "name"));
        onboardItemObject.setBaseId(ResponseParser.getValueFromJsonResponse(response.getResponse(), "baseId"));
        onboardItemObject.setStatus(ResponseParser.getValueFromJsonResponse(response.getResponse(), "status"));

        return new Pair<>(response, onboardItemObject);
    }

    public static RestResponse updateVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(
                PathUrls.UPDATE_VENDOR_SOFTWARE_PRODUCT,
                config.getCatalogBeHost(),
                config.getCatalogBePort(),
                vendorSoftwareProductObject.getVspId(),
                vendorSoftwareProductObject.getComponentId());
        String userId = user.getUserId();
        VendorSoftwareProductObject updateVendorSoftwareProductObject = new VendorSoftwareProductObject();

        updateVendorSoftwareProductObject.setName(vendorSoftwareProductObject.getName());
        updateVendorSoftwareProductObject.setDescription(vendorSoftwareProductObject.getDescription());
        updateVendorSoftwareProductObject.setCategory(vendorSoftwareProductObject.getCategory());
        updateVendorSoftwareProductObject.setSubCategory(vendorSoftwareProductObject.getSubCategory());
        updateVendorSoftwareProductObject.setVendorName(vendorSoftwareProductObject.getVendorName());
        updateVendorSoftwareProductObject.setVendorId(vendorSoftwareProductObject.getVendorId());
        updateVendorSoftwareProductObject.setIcon(vendorSoftwareProductObject.getIcon());
        updateVendorSoftwareProductObject.setLicensingData(vendorSoftwareProductObject.getLicensingData());
        updateVendorSoftwareProductObject.setLicensingVersion(vendorSoftwareProductObject.getLicensingVersion());

        Map<String, String> headersMap = prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        Gson gson = new Gson();
        String body = gson.toJson(updateVendorSoftwareProductObject);

        return http.httpSendPut(url, body, headersMap);
    }

}
