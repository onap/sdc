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

package org.openecomp.sdc.ci.tests.pages;

import com.aventstack.extentreports.Status;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.WordUtils;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;
import org.testng.collections.Lists;

public final class DeploymentArtifactPage {

    private DeploymentArtifactPage() {
    }

    public static ResourceLeftMenu getLeftPanel() {
        return new ResourceLeftMenu();
    }

    public static String[] verifyArtifactsExistInTable(String filepath, String vnfFile) throws Exception {
        String[] artifactNamesFromZipFile = FileHandling.getArtifactsFromZip(filepath, vnfFile);
        return verifyArtifactsExistInTable(artifactNamesFromZipFile);
    }

    public static String[] verifyArtifactsExistInTable(String[] artifactNamesFromZipFile) throws Exception {
        if (artifactNamesFromZipFile != null) {
            checkArtifactsDisplayed(artifactNamesFromZipFile);
            checkEnvArtifactsDisplayed();
        }

        return artifactNamesFromZipFile;
    }

    public static void checkArtifactsDisplayed(String[] artifactsFromZipFile) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the artifacts in the table");
        List<String> artifactList = Lists.newArrayList(artifactsFromZipFile).stream().filter(p -> !p.contains(".env")).map(p -> getVisualArtifactName(p)).collect(Collectors.toList());
        try {
            List<WebElement> rows = GeneralUIUtils.getElementsByCSS("div div[data-tests-id^='artifact-item'] span[data-tests-id^='artifactDisplayName']");
            for (WebElement r : rows) {
                String artifactDisplayed = r.getAttribute("textContent").trim();
                if (artifactList.contains(artifactDisplayed)) {
                    artifactList.remove(artifactDisplayed);
                } else if (artifactDisplayed.toLowerCase().contains("license")) {
                    artifactList.add(artifactDisplayed);
                }
            }
            checkLicenseArtifactsDisplayed(artifactList);
        } catch (Exception e) {
            throw new Exception("Table problem");
        }


        if (!artifactList.isEmpty()) {
            throw new Exception(String.format("missing the following artifact(s) : %s", artifactList.toString()));
        }
    }

    public static void checkEnvArtifactsDisplayed() throws Exception {
        List<WebElement> envRows;
        List<WebElement> heatRows;
        List<WebElement> heatNetRows;
        List<WebElement> heatVolRows;
        int envArtifactsSize = 0;

        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the HEAT_ENV artifacts in the table");

        try {
            envRows = GeneralUIUtils.getElementsByCSS("div div[data-tests-id='HEAT_ENV']");

            heatRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT']");
            heatNetRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT_NET']");
            heatVolRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT_VOL']");

            envArtifactsSize = heatRows.size() + heatNetRows.size() + heatVolRows.size();
        } catch (Exception e) {
            throw new Exception("Table problem");
        }

        if (envArtifactsSize != envRows.size()) {
            throw new Exception(String.format("some env artifacts are missing... there is %s instead of %s", envRows.size(), envArtifactsSize));
        }

    }

    public static void checkLicenseArtifactsDisplayed(List<String> rowsFromTable) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the license artifacts in the table");
        String vfLicense = getPreparedLicense(ArtifactTypeEnum.VF_LICENSE.getType());
        String[] split = vfLicense.split(" ");
        vfLicense = vfLicense.replaceAll(split[0], split[0].toUpperCase());
        rowsFromTable.remove(vfLicense);

        String vendorLicense = getPreparedLicense(ArtifactTypeEnum.VENDOR_LICENSE.getType());
        rowsFromTable.remove(vendorLicense);

    }

    public static String getPreparedLicense(String license) {
        return WordUtils.capitalizeFully(license.replaceAll("_", " "));
    }


    private static String getVisualArtifactName(String artifactName) {
        if (artifactName.contains(".")) {
            return artifactName.substring(0, artifactName.lastIndexOf("."));
        }
        return artifactName;
    }

}
