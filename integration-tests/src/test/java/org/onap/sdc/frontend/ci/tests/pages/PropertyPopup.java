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

package org.onap.sdc.frontend.ci.tests.pages;

import com.aventstack.extentreports.Status;
import java.util.List;
import javax.annotation.Nullable;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.ConstraintEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.PropertiesPopupEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.ToscaFunction;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyPopup {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyPopup.class);

    private static final int WAIT_FOR_ELEMENT_TIME_OUT = 60;

    private boolean getPopupForm() {
        return GeneralUIUtils.waitForElementInVisibilityByTestId(DataTestIdEnum.PropertiesPageEnum.POPUP_FORM.getValue(), WAIT_FOR_ELEMENT_TIME_OUT);
    }

    public void insertPropertyName(String name) {
        WebElement propertyNameField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_NAME.getValue());
        propertyNameField.clear();
        propertyNameField.sendKeys(name);
    }

    public void setConstraintValue(String value) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Setting constraint value: %s ", value));
        WebElement constraintValueField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ConstraintEnum.VALUE.getValue());
        constraintValueField.clear();
        constraintValueField.sendKeys(value);
    }

    public void setMinConstraintValue(String value) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Setting min value: %s ", value));
        WebElement constraintValueField = GeneralUIUtils.getWebElementByTestID(ConstraintEnum.MIN_VALUE.getValue());
        constraintValueField.clear();
        constraintValueField.sendKeys(value);
    }

    public void setMaxConstraintValue(String value) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Setting max value: %s ", value));
        WebElement constraintValueField = GeneralUIUtils.getWebElementByTestID(ConstraintEnum.MAX_VALUE.getValue());
        constraintValueField.clear();
        constraintValueField.sendKeys(value);
    }

    public void setValidConstraintValue(String value, int inputField) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Setting valid value: %s ", value));
        List<WebElement> constraintValueFields = GeneralUIUtils.getInputElements(ConstraintEnum.VALID_VALUE.getValue());
        WebElement constraintValueField = constraintValueFields.get(inputField);
        constraintValueField.clear();
        constraintValueField.sendKeys(value);
    }

    /**
     * Set the default value to be a TOSCA Function
     * @param toscaFunction 
     * @param propertySource Can be null if TOSCA function is GET_INPUT
     * @param attribute An attribute in the case of GET_ATTRIBUTE and GET_PROPERTY. Input source in the case of GET_INPUT
     * @throws InterruptedException Thrown if unable to save
     * @throws NotImplementedException Thrown if implementation for a specific TOSCA function is missing
     */
    public void setToscaDefaultValue(ToscaFunction toscaFunction, @Nullable String propertySource, String attribute)
        throws InterruptedException, NotImplementedException {
        SetupCDTest.getExtendTest().log(Status.INFO, "Setting default value to TOSCA Function");
        final WebElement toscaRadio = GeneralUIUtils.getWebElementByTestID(PropertiesPopupEnum.RADIO_BUTTON_TOSCA.getValue());
        toscaRadio.click();
        GeneralUIUtils.getSelectList(toscaFunction.getValue(), PropertiesPopupEnum.SELECT_TOSCA.getValue());

        switch (toscaFunction) {
            case GET_ATTRIBUTE:
            case GET_PROPERTY:
                GeneralUIUtils.getSelectList(propertySource, PropertiesPopupEnum.SELECT_SOURCE.getValue());
                GeneralUIUtils.getSelectList(attribute, PropertiesPopupEnum.SELECT_ATTRIBUTE.getValue());
                break;
            case GET_INPUT:
                GeneralUIUtils.getSelectList(attribute, PropertiesPopupEnum.SELECT_ATTRIBUTE.getValue());
                break;
            default:
                throw new NotImplementedException(String.format("%s support not implemented", toscaFunction.getValue()));
        }

        clickSave();
    }

    public void insertPropertyDefaultValue(String value) {
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Inserting to property default value: %s ", value));
        WebElement selectedType = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_TYPE.getValue())).getFirstSelectedOption();
        if (selectedType.getText().equals("boolean")) {
            GeneralUIUtils.getSelectList(value, DataTestIdEnum.PropertiesPopupEnum.PROPERTY_BOOLEAN_VALUE.getValue());
        } else {
            WebElement propertyValue = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_VALUE.getValue());
            propertyValue.clear();
            propertyValue.sendKeys(value);
        }

        GeneralUIUtils.ultimateWait();
    }

    public void insertPropertyDescription(String description) {
        WebElement propertyDescription = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_DESCRIPTION.getValue());
        propertyDescription.clear();
        propertyDescription.sendKeys(description);
    }

    public void selectPropertyType(String propertyType) {
        boolean isEntrySchemaDisplayed;
        try {
            GeneralUIUtils.getSelectList(propertyType, DataTestIdEnum.PropertiesPopupEnum.PROPERTY_TYPE.getValue());
            isEntrySchemaDisplayed = GeneralUIUtils.getDriver().findElement(By.xpath(DataTestIdEnum.PropertiesPopupEnum.ENTRY_SCHEMA.getValue())).isDisplayed();
            if (isEntrySchemaDisplayed) {
                PropertiesPage.getPropertyPopup().selectEntrySchema(propertyType);
            }
        } catch (NoSuchElementException e) {
            LOGGER.error("Couldn't select property", e);
        }
    }

    public void selectEntrySchema(String propertyType) {
        GeneralUIUtils.getSelectList(propertyType, DataTestIdEnum.PropertiesPopupEnum.ENTRY_SCHEMA.getValue());
    }

    public void clickAdd() {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.ADD.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public void clickAddConstraint() {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ConstraintEnum.ADD.getValue());
    }

    public void clickAddValidValue() {
        GeneralUIUtils.clickOnElementByTestId(ConstraintEnum.ADD_VALID_VALUE.getValue());
    }

    public void clickDeleteValidValue() {
        GeneralUIUtils.clickOnElementByTestId(ConstraintEnum.DELETE_VALID_VALUE.getValue());
    }

    public void setConstraintType(ConstraintType constraintType) {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ConstraintEnum.TYPE.getValue());

        WebElement constraint = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ConstraintEnum.TYPE.getValue());
        constraint.sendKeys(constraintType.getType());
    }

    public void clickSave() throws InterruptedException {
        if (isSaveEnabled()) {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.SAVE.getValue());

            if (!ErrorModal.isModalOpen()) {
                getPopupForm();
            } else {
                throw new InterruptedException("Failed to save");
            }
        } else {
            throw new InterruptedException("Save button is disabled");
        }
    }

    public boolean isSaveEnabled() {
        return !GeneralUIUtils.isElementDisabled(DataTestIdEnum.PropertiesPopupEnum.SAVE.getValue());
    }

    public void clickCancel() {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.CANCEL.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public void clickDone() {
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.PropertiesPopupEnum.DONE.getValue());
        //GeneralUIUtils.ultimateWait();
    }

    public void selectPropertyRadioButton(String propertyName) {
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPopupEnum.PROPERTY_RADIO_BUTTON_CONTAINER.getValue() + propertyName).findElement(By.className(DataTestIdEnum.PropertiesPopupEnum.RADIO_BUTTON_CLASS.getValue())).click();
    }

}
