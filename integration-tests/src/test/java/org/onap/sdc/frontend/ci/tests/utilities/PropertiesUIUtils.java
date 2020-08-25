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
import org.onap.sdc.backend.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.onap.sdc.frontend.ci.tests.pages.PropertiesPage;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

public class PropertiesUIUtils {

    private static final int SLEEP_TIME = 2000;

    public static Map<String, String> addProperties(String name, String itemType, String defaultValue,
                                                    String description, String schemaType) throws Exception {
        Map<String, String> propertyvalues = new HashMap<String, String>();
        GeneralUIUtils.getSelectList(itemType, "propertyType");
        ResourceUIUtils.definePropertyName(name);
        if (itemType == "boolean") {
            ResourceUIUtils.defineBoolenDefaultValue(defaultValue);
            GeneralUIUtils.setWebElementByTestId("description", "description");
            Thread.sleep(SLEEP_TIME);
            GeneralUIUtils.getWebElementByTestID("Add").click();
        } else if (itemType == "list" || itemType == "map") {
            GeneralUIUtils.getSelectList(schemaType, "schemaType");
        }
        if (!(itemType == "boolean")) {
            ResourceUIUtils.defineDefaultValueByType(defaultValue);
            GeneralUIUtils.setWebElementByTestId("description", "des");
            GeneralUIUtils.getWebElementByTestID("Add").click();
            Thread.sleep(SLEEP_TIME);
        }
        propertyvalues.put("type", itemType);
        propertyvalues.put("defaultValue", defaultValue);
        propertyvalues.put("description", description);
        propertyvalues.put("name", name);

        return propertyvalues;
    }

    public static void vlidateProperties(Map<String, String> propertyValues) throws InterruptedException {
        WebElement name = GeneralUIUtils.getWebElementByTestID(propertyValues.get("name"));
        name.getText().equalsIgnoreCase(propertyValues.get("name"));
        WebElement defaultValue = GeneralUIUtils.getWebElementByTestID(propertyValues.get("name"));
        defaultValue.getText().equalsIgnoreCase(propertyValues.get("defaultValue"));
        WebElement type = GeneralUIUtils.getWebElementByTestID(propertyValues.get("type"));
        type.getText().equalsIgnoreCase(propertyValues.get("type"));
    }

    public static void addNewProperty(PropertyTypeEnum property) {
        GeneralUIUtils.ultimateWait();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Adding new %s property", property.name()));
        PropertiesPage.clickAddPropertyArtifact();
        PropertiesPage.getPropertyPopup().insertPropertyName(property.getName());
        PropertiesPage.getPropertyPopup().selectPropertyType(property.getType());
        PropertiesPage.getPropertyPopup().insertPropertyDescription(property.getDescription());
        PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(property.getValue());
        PropertiesPage.getPropertyPopup().clickSave();
    }

    public static void updateProperty(PropertyTypeEnum property) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Updating property: %s", property.name()));
        PropertiesPage.clickOnProperty(property.getName());
        PropertiesPage.getPropertyPopup().insertPropertyDescription(property.getUpdateDescription());
        PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(property.getUpdateValue());
        PropertiesPage.getPropertyPopup().clickSave();
    }

    public static void changePropertyDefaultValueInComposition(String propertyName, String defaultValue) {
        GeneralUIUtils.clickOnElementByTestId(propertyName);
        PropertiesPage.getPropertyPopup().insertPropertyDefaultValue(defaultValue);
        PropertiesPage.getPropertyPopup().clickSave();
    }

}
