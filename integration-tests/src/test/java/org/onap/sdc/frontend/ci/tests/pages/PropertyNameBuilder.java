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

package org.onap.sdc.frontend.ci.tests.pages;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;

public class PropertyNameBuilder {
    private static final String PREFIX_VAL = DataTestIdEnum.PropertiesAssignmentScreen.PROPERTY_VALUE_FIELD_PREFIX.getValue();
    private static final String PREFIX_KEY = DataTestIdEnum.PropertiesAssignmentScreen.PROPERTY_KEY_FIELD_PREFIX.getValue();
    private static final String PREFIX_EXPAND = DataTestIdEnum.PropertiesAssignmentScreen.EXPAND_BUTTON.getValue();
    private static final String PREFIX_INPUT_VAL = DataTestIdEnum.PropertiesAssignmentScreen.INPUT_VALUE_FIELD_PREFIX.getValue();
    private static final String POPUP_VAL = DataTestIdEnum.PropertiesAssignmentScreen.POPUP_VALUE_FIELD_PREFIX.getValue();

    private PropertyNameBuilder() {

    }

    //VF/Service simple property value field
    public static String buildSimpleField(String propertyName) {
        return new StringBuilder().append(PREFIX_VAL).append(propertyName).toString();
    }

    public static String buildPopupField(String propertyName) {
        return new StringBuilder().append(POPUP_VAL).append(propertyName).toString();
    }

    public static String buildIndexedField(String propertyName, int index) {
        return new StringBuilder().append(PREFIX_VAL).append(propertyName).append(".").append(index).toString();
    }

    public static String buildIndexedKeyField(String propertyName, int index) {
        return new StringBuilder().append(PREFIX_KEY).append(propertyName).append(".").append(index).toString();
    }

    public static String buildIComplexField(String propertyName, String nestedProperty) {
        return new StringBuilder().append(PREFIX_VAL).append(propertyName).append(".").append(nestedProperty).toString();
    }

    public static String buildIComplexListField(String propertyName, String nestedProperty, int index) {
        return new StringBuilder().append(PREFIX_VAL).append(propertyName).append(".").append(index).append(".").append(nestedProperty).toString();
    }

    public static String buildIExpandButton(String propertyName, int index) {
        return new StringBuilder().append(PREFIX_EXPAND).append(propertyName).append(".").append(index).toString();
    }


    //VF input value field
    public static String buildDeclaredInputField(String componentName, String propertyName) {
        return new StringBuilder().append(PREFIX_INPUT_VAL).append(componentName).append("_").append(propertyName).toString();
    }

    public static String buildInputField(String propertyName) {
        return new StringBuilder().append(PREFIX_INPUT_VAL).append(propertyName).toString();
    }

    //Service Property value field - declared from VF
    public static String buildServicePropertyValue(String componentName, String propertyName) {
        return new StringBuilder().append(PREFIX_VAL).append(componentName).append("_").append(propertyName).toString();
    }

    //Service Input Name
    public static String buildServiceInputNameServiceLevel(ComponentInstance componentInstance, String propertyName) {
        return new StringBuilder().append(componentInstance.getNormalizedName())
                .append("_").append(propertyName).toString();
    }

    public static String buildServiceInputNameVfLevel(ComponentInstance componentInstance, String componentName, String propertyName) {
        return new StringBuilder().append(componentInstance.getNormalizedName())
                .append("_").append(componentName).append("_").append(propertyName).toString();
    }


    //Service Input Value
    public static String buildVfDeclaredPropValue(String componentName, String propertyName) {
        String inputName = componentName + "_" + propertyName;
        return new StringBuilder().append("{\"get_input\":\"").append(inputName).append("\"}").toString();
    }

    public static String buildServiceDeclaredPropertyValue(ComponentInstance componentInstance, String componentName, String propertyName) {
        String inputName = buildServiceInputNameVfLevel(componentInstance, componentName, propertyName);
        return new StringBuilder().append("{\"get_input\":\"").append(inputName).append("\"}").toString();
    }

    public static String buildServiceDeclaredPropValueServiceLevel(ComponentInstance componentInstance, String propertyName) {
        String inputName = buildServiceInputNameServiceLevel(componentInstance, propertyName);
        return new StringBuilder().append("{\"get_input\":\"").append(inputName).append("\"}").toString();
    }

    //Service Input value field
    public static String buildServiceDeclaredFieldServiceLevel(ComponentInstance componentInstance, String propertyName) {
        String inputName = buildServiceInputNameServiceLevel(componentInstance, propertyName);
        return new StringBuilder().append(PREFIX_INPUT_VAL).append(inputName).toString();
    }

    public static String buildServiceDeclaredFieldVfLevel(ComponentInstance componentInstance, String componentName, String propertyName) {
        String inputName = buildServiceInputNameVfLevel(componentInstance, componentName, propertyName);
        return new StringBuilder().append(PREFIX_INPUT_VAL).append(inputName).toString();
    }


}
