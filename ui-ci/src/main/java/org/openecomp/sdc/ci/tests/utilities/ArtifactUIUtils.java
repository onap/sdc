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

package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.HeatWithParametersDefinition;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.UploadArtifactPopup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public final class ArtifactUIUtils {

    private static final String PARAMETERS = "parameters";
    private static final String DATA_TESTS_ID = "//*[@data-tests-id='";

    private ArtifactUIUtils() {
    }

    public static void fillAndAddNewArtifactParameters(ArtifactInfo artifactInfo) throws Exception {
        UploadArtifactPopup artifactPopup = new UploadArtifactPopup();
        fillAndAddNewArtifactParameters(artifactInfo, artifactPopup);
    }

    public static void fillAndAddNewArtifactParameters(ArtifactInfo artifactInfo, UploadArtifactPopup artifactPopup) throws Exception {
        artifactPopup.defineArtifactLabel(artifactInfo.getArtifactLabel());
        artifactPopup.selectArtifactType(artifactInfo.getArtifactType());
        artifactPopup.insertDescription(artifactInfo.getDescription());
        artifactPopup.loadFile(artifactInfo.getFilepath(), artifactInfo.getFilename());
        artifactPopup.clickDoneButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("A new artifact of type %s was added", artifactInfo.getArtifactType()));
    }

    public static void fillAndAddNewEnvArtifactParameters(ArtifactInfo artifactInfo, UploadArtifactPopup artifactPopup) throws Exception {
        artifactPopup.insertDescription(artifactInfo.getDescription());
        artifactPopup.loadFile(artifactInfo.getFilepath(), artifactInfo.getFilename());
        artifactPopup.clickDoneButton();
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("ENV parameters %s artifact updated ", artifactInfo.getArtifactType()));
    }

    public static void fillPlaceHolderInformationalArtifact(DataTestIdEnum.InformationalArtifactsPlaceholders artifactLabel, String filepath, String filename, String description) throws Exception {
        GeneralUIUtils.clickOnElementByTestId(artifactLabel.getValue());
        InformationalArtifactPage.artifactPopup().loadFile(filepath, filename);
        InformationalArtifactPage.artifactPopup().insertDescription(description);
        InformationalArtifactPage.artifactPopup().clickDoneButton();
    }

    public static void fillPlaceHolderInformationalArtifact(DataTestIdEnum.InformationalArtifactsService artifactLabel,
                                                            String filepath, String filename, String description) throws Exception {
        GeneralUIUtils.clickOnElementByTestId(artifactLabel.getValue());
        UploadArtifactPopup artifactPopup = new UploadArtifactPopup(true);
        artifactPopup.loadFile(filepath, filename);
        artifactPopup.insertDescription(description);
        artifactPopup.clickDoneButton();
    }

    public static void fillPlaceHolderAPIArtifact(DataTestIdEnum.APIArtifactsService artifactLabel,
                                                  String filepath, String filename, String description, String url) throws Exception {
        GeneralUIUtils.clickOnElementByTestId(artifactLabel.getValue());
        UploadArtifactPopup artifactPopup = new UploadArtifactPopup(true);
        artifactPopup.loadFile(filepath, filename);
        artifactPopup.insertURL(url);
        artifactPopup.insertDescription(description);
        artifactPopup.clickDoneButton();
    }

    public static void validateArtifactNameVersionType(String artifactLabel, String artifactVersion, String artifactType) {
        if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactLabel)) {
            SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact label not equal - this warning represent defect.");
        }
        if (artifactVersion != null) {
            if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.VERSION.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactVersion)) {
                SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact version not equal - this warning represent defect.");
            }
        }
        if (artifactType != null) {
            if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.TYPE.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactType)) {
                SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact type not equal - this warning represent defect.");
            }
        }
    }

    public static void validateExistArtifactOnDeploymentInformationPage(String expectedArtifactLabel, String artifactUUID, String artifactVersion, String artifactType, boolean isDownloadable, boolean isEditable, boolean isDeletable, boolean isArtifactParametersEditable) {

        String dataTestId = DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue() + expectedArtifactLabel;

        List<WebElement> artifactElements = GeneralUIUtils.getWebElementsListByContainTestID(dataTestId);
        Assert.assertEquals(artifactElements.size(), 1, "There are more then one artifact named " + expectedArtifactLabel);

        WebElement artifact = artifactElements.get(0);
        String actualArtifactLabel = GeneralUIUtils.getTextContentAttributeValue(artifact).trim();
        Assert.assertEquals(actualArtifactLabel, expectedArtifactLabel);

        if (artifactUUID != null) {
            WebElement uuid = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + expectedArtifactLabel);
            Assert.assertEquals(GeneralUIUtils.getTextContentAttributeValue(uuid).trim(), artifactUUID, "Artifact uuid not equal.");
        }
        if (artifactVersion != null) {
            WebElement version = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.VERSION.getValue() + expectedArtifactLabel);
            Assert.assertEquals(GeneralUIUtils.getTextContentAttributeValue(version).trim(), artifactVersion, "Artifact version not equal.");
        }
        if (artifactType != null) {
            WebElement type = GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.TYPE.getValue() + expectedArtifactLabel);
            Assert.assertEquals(GeneralUIUtils.getTextContentAttributeValue(type).trim(), artifactType, "Artifact type not equal.");
        }
        if (isArtifactParametersEditable) {
            Assert.assertNotNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.EDIT_PARAMETERS_OF_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that parameters edit button enabled.");
        } else {
            Assert.assertNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.EDIT_PARAMETERS_OF_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that parameters edit button disabled.");
        }
        if (isDownloadable) {
            Assert.assertNotNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that download button enabled.");
        } else {
            Assert.assertNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that download button disabled.");
        }
        if (isEditable) {
            Assert.assertNotNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that edit button enabled.");
        } else {
            Assert.assertNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that edit button disabled.");
        }
        if (isDeletable) {
            Assert.assertNotNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.DELETE_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that delete button enabled.");
        } else {
            Assert.assertNull(GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.DELETE_ARTIFACT.getValue() + expectedArtifactLabel), "Expect that delete button disabled.");
        }
    }

    public static void validateNotExistArtifactOnDeploymentInformationPage(String artifactLabel) {
        Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue() + artifactLabel), false);
    }

    public static void validateExistArtifactOnCompositionRightMenuDeploymentInformationPage(String fileName, String artifactDisplayedName,
                                                                                            boolean isUpdateable, boolean isParametersEditable, boolean isDownloadable, boolean isDeleteable) {
        Assert.assertEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_NAME.getValue() + artifactDisplayedName).getText(), fileName);
        Assert.assertEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + artifactDisplayedName).getText(), artifactDisplayedName);

        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + artifactDisplayedName);

        if (isParametersEditable) {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.EDIT_PARAMETERS_OF_ARTIFACT.getValue() + artifactDisplayedName), true, "Expect that parameters edit button enabled.");
        } else {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.EDIT_PARAMETERS_OF_ARTIFACT.getValue() + artifactDisplayedName), false, "Expect that parameters edit button disabled.");
        }
        if (isDownloadable) {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DOWNLOAD.getValue() + artifactDisplayedName), true, "Expect that download button enabled.");
        } else {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DOWNLOAD.getValue() + artifactDisplayedName), false, "Expect that download button disabled.");
        }
        if (isDeleteable) {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + artifactDisplayedName), true, "Expect that delete button enabled.");
        } else {
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.DELETE.getValue() + artifactDisplayedName), false, "Expect that delete button disabled.");
        }
        if (isUpdateable) {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + artifactDisplayedName);
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPopup.MODAL_WINDOW.getValue()), true, "Expect that edit button enabled.");
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPopup.DONE_BUTTON.getValue());
            GeneralUIUtils.waitForElementInVisibilityByTestId(DataTestIdEnum.ArtifactPopup.DONE_BUTTON.getValue());
        } else {
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue() + artifactDisplayedName);
            Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ArtifactPopup.MODAL_WINDOW.getValue()), false, "Expect that edit button disabled.");
        }

    }

    public static void validateNotExistArtifactOnCompositionRightMenuDeploymentInformationPage(String artifactDisplayedName) {
        Assert.assertEquals(GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_NAME.getValue() + artifactDisplayedName), false);
    }

    public static File verifyUpdatedEnvParameters(HeatWithParametersDefinition pairToUpdate, File updateEnvFile, String dataTestId) throws Exception {
        GeneralUIUtils.hoverOnAreaByTestId(dataTestId);
        return verifyUpdatedEnvParameters(pairToUpdate, updateEnvFile);
    }

    public static File verifyUpdatedEnvParameters(HeatWithParametersDefinition pairToUpdate, File updateEnvFile) throws Exception {

        String heatDisplayName = pairToUpdate.getHeatArtifactDisplayName();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue() + heatDisplayName);
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();

        String pattern = PARAMETERS;
        Map<String, Object> mapUpdetedEnvFile = FileHandling.parseYamlFileToMapByPattern(updateEnvFile, pattern);
        Map<String, Object> mapDownloadedEnvFile = FileHandling.parseYamlFileToMapByPattern(latestFilefromDir, pattern);

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to check, that ENV file was updated ...");
        assertTrue("File" + latestFilefromDir.getName() + " contains different parameters number from expected file", mapDownloadedEnvFile.size() == mapUpdetedEnvFile.size());
        assertTrue("Updated file contains not updated parameters value", mapDownloadedEnvFile.entrySet().containsAll(mapUpdetedEnvFile.entrySet()));
        return latestFilefromDir;
    }

    /**
     * compare heat env files by pattern ("parameters")
     *
     * @param expectedFile
     * @param actualFile
     * @param pattern
     * @throws Exception
     */
    public static void compareYamlFilesByPattern(File expectedFile, File actualFile, String pattern) throws Exception {

        Map<String, Object> mapExpectedFile = FileHandling.parseYamlFileToMapByPattern(expectedFile, pattern);
        Map<String, Object> mapActualFile = FileHandling.parseYamlFileToMapByPattern(actualFile, pattern);
        SetupCDTest.getExtendTest().log(Status.INFO, "Going to compare file " + expectedFile.getName());
        assertTrue("File " + actualFile.getName() + " contains different parameters number from expected file", mapActualFile.size() == mapExpectedFile.size());
        assertTrue("File " + actualFile.getName() + " does not contains all expected parameters", mapActualFile.entrySet().containsAll(mapExpectedFile.entrySet()));
    }

    public static void compareYamlParametersByPattern(Map<String, Object> mapExpectedProperties, File actualFileProperties, String pattern) throws Exception {

        Map<String, Object> mapActualFileProerties = FileHandling.parseYamlFileToMapByPattern(actualFileProperties, pattern);
        SetupCDTest.getExtendTest().log(Status.INFO, "Going to compare files ...");
        assertTrue("Actual file contains different parameters number from expected file", mapActualFileProerties.size() == mapExpectedProperties.size());
        Map<String, Object> newMap = new HashMap<>(mapActualFileProerties);
        assertTrue("Actual file does not contains all expected parameters", newMap.entrySet().containsAll(mapExpectedProperties.entrySet()));
    }


    public static File uploadCreatedUpdateParametersEnvFile(HeatWithParametersDefinition heatEnvDetails, String directoryPath) throws Exception {
//		created env file to upload
        File pathToEnvParametersFile = prepareEnvParametersFile(heatEnvDetails, directoryPath);
        ArtifactInfo heatEnvArtifactInfo = new ArtifactInfo(directoryPath, heatEnvDetails.getHeatEnvLabel() + ".env", "heatEnvDesc", heatEnvDetails.getHeatEnvLabel(), heatEnvDetails.getHeatEnvArtifactType());
        ArtifactUIUtils.fillAndAddNewEnvArtifactParameters(heatEnvArtifactInfo, CompositionPage.artifactPopup());
        return pathToEnvParametersFile;
    }

    public static File prepareEnvParametersFile(HeatWithParametersDefinition heatEnvDetails, String directoryPath) throws IOException {
        File pathToEnvParametersFile = FileHandling.createEmptyFile(directoryPath + heatEnvDetails.getHeatEnvLabel() + ".env");
//		fill file
        FileHandling.writeToFile(pathToEnvParametersFile, "parameters:", 0);
        for (HeatParameterDataDefinition paramDefinition : heatEnvDetails.getHeatParameterDefinition()) {
            Object data = getDataToWrite(paramDefinition);
            FileHandling.writeToFile(pathToEnvParametersFile, data, 2);
        }

        return pathToEnvParametersFile;
    }

    public static Object getDataToWrite(HeatParameterDataDefinition paramDefinition) {
        Object data = "";
        switch (paramDefinition.getType()) {
            case "string":
                String text = "\"string\"";
                data = getFormatedData(paramDefinition.getName(), text);
                break;
            case "number":
                data = getFormatedData(paramDefinition.getName(), 666);
                break;
            case "json":
                String jsonText = "{\"param1\":\"param1\", \"param2\":2}";
                data = getFormatedData(paramDefinition.getName(), jsonText);
                break;
            case "boolean":
                if (paramDefinition.getCurrentValue().equals("true")) {
                    data = getFormatedData(paramDefinition.getName(), false);
                } else {
                    data = getFormatedData(paramDefinition.getName(), true);
                }
                break;
            case "comma_delimited_list":
                String commaDelimitedListText = "127.0.0.10, 127.0.0.15, 127.0.0.20";
                data = getFormatedData(paramDefinition.getName(), commaDelimitedListText);
                break;
            default:
                break;
        }
        return data;
    }


    public static Map<String, Object> getDataToWriteInUI(List<HeatParameterDataDefinition> paramDefinitionFromGetResourceResponse) {
        Map<String, Object> newValuesToUpdateInUI = new HashMap<>();
        for (HeatParameterDataDefinition param : paramDefinitionFromGetResourceResponse) {
            switch (param.getType()) {

                case "string":
                    String text = "string";
                    newValuesToUpdateInUI.put(param.getName(), text);
                    break;
                case "number":
                    newValuesToUpdateInUI.put(param.getName(), 666);
                    break;
                case "json":
                    String jsonText = "{\"param1\":\"param1\", \"param2\":2}";
                    newValuesToUpdateInUI.put(param.getName(), jsonText);
                    break;
                case "boolean":
                    if (param.getCurrentValue().equals(true)) {
                        newValuesToUpdateInUI.put(param.getName(), false);
                    } else {
                        newValuesToUpdateInUI.put(param.getName(), true);
                    }
                    break;
                case "comma_delimited_list":
                    String commaDelimitedListText = "127.0.0.10, 127.0.0.15, 127.0.0.20";
                    newValuesToUpdateInUI.put(param.getName(), commaDelimitedListText);
                    break;
                default:
                    break;

            }

        }
        return newValuesToUpdateInUI;
    }

    public static Object getValue(HeatParameterDataDefinition param) {
        String type = param.getType();
        Object result = null;
        switch (type) {
            case "string":
                result = param.getCurrentValue();
                break;
            case "number":
                result = new Integer(param.getCurrentValue());
                break;
            case "json":
                result = param.getCurrentValue();
                break;
            case "boolean":
                result = new Boolean(param.getCurrentValue());
                break;
            case "comma_delimited_list":
                result = param.getCurrentValue();
                break;
            default:
                break;
        }
        return result;
    }

    public static Object getFormatedData(String name, Object text) {
        return name + ": " + text;
    }


}
