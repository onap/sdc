/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.PortMirroringEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PortMirroringUtils;
import org.openqa.selenium.By;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class PortMirroringVerificator {

    private PortMirroringVerificator() {
    }

    public static void checkProxyServiceName(String serviceName, String instanceId) {
        String serviceActualName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanel.COMPONENT_TITLE.getValue()).getText();
        String serviceExpectedName = PortMirroringUtils.createproxyinstanceservicename(serviceName, instanceId);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the instance name is %s", serviceExpectedName));
        assertTrue(serviceActualName.equalsIgnoreCase(serviceExpectedName));
    }

    public static void checkProxyServiceType() {
        String serviceActualName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanelGeneralInfo.TYPE.getValue()).getText();
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the instance type is Service Proxy");
        assertTrue(serviceActualName.equalsIgnoreCase(PortMirroringEnum.SERVICE_PROXY_TYPE.getValue()));
    }

    public static void validatingProxyServiceNameAndType(String serviceName, String instanceId) {
        checkProxyServiceName(serviceName, instanceId);
        checkProxyServiceType();
    }

    public static void validateGeneralInfo() {
        String type = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanelGeneralInfo.TYPE.getValue()).getText();
        String resourceType = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanelGeneralInfo.RESOURCE_TYPE.getValue()).getText();
        String category = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanelGeneralInfo.CATEGORY.getValue()).getText();
        String subCategory = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanelGeneralInfo.SUB_CATEGORY.getValue()).getText();

        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the type equals %s", PortMirroringEnum.TYPE.getValue()));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the resource type equals %s", PortMirroringEnum.RESOURCE_TYPE.getValue()));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the category equals %s", PortMirroringEnum.CATEGORY.getValue()));
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the sub category equals %s", PortMirroringEnum.SUB_CATEGORY.getValue()));

        assertTrue(type.equalsIgnoreCase(PortMirroringEnum.TYPE.getValue()));
        assertTrue(resourceType.equalsIgnoreCase(PortMirroringEnum.RESOURCE_TYPE.getValue()));
        assertTrue(category.equalsIgnoreCase(PortMirroringEnum.CATEGORY.getValue()));
        assertTrue(subCategory.equalsIgnoreCase(PortMirroringEnum.SUB_CATEGORY.getValue()));
    }

    public static void validateReqsAndCapsTabExist() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying tab reqs and caps exist for PMC element");
        GeneralUIUtils.getWebElementBy(By.xpath(DataTestIdEnum.CompositionRightPanel.REQS_AND_CAPS_TAB_XPATH.getValue())).click();
    }

    public static void validateElementName(String expectedName) {
        String serviceActualName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionRightPanel.COMPONENT_TITLE.getValue()).getText();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying the instance name is %s", expectedName));
        assertTrue(serviceActualName.equalsIgnoreCase(expectedName));
    }

    public static void validateLinkProperties() throws IOException, UnsupportedFlavorException {
        SetupCDTest.getExtendTest().log(Status.INFO, "Validate Link properties values");
        String actualNetworkRole = GeneralUIUtils.getTextValueFromWebElementByXpath(PortMirroringEnum.NETWORK_ROLE_XPATH.getValue());
        String actualNfcType = GeneralUIUtils.getTextValueFromWebElementByXpath(PortMirroringEnum.NFC_TYPE_XPATH.getValue());
        String actualPpsCapacity = GeneralUIUtils.getTextValueFromWebElementByXpath(PortMirroringEnum.PPS_CAPACITY_XPATH.getValue());
        String actualNfType = GeneralUIUtils.getTextValueFromWebElementByXpath(PortMirroringEnum.NF_TYPE_XPATH.getValue());

        assertTrue(actualNetworkRole.equalsIgnoreCase(PortMirroringEnum.NETWORK_ROLE_VALUE.getValue()));
        assertTrue(actualNfcType.equalsIgnoreCase(PortMirroringEnum.NFC_TYPE_VALUE.getValue()));
        assertTrue(actualPpsCapacity.equalsIgnoreCase(PortMirroringEnum.PPS_CAPACITY_VALUE.getValue()));
        assertTrue(actualNfType.equalsIgnoreCase(PortMirroringEnum.NF_TYPE_VALUE.getValue()));
    }


}
