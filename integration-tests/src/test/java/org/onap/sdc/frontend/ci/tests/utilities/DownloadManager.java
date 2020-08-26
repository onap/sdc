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
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.pages.HomePage;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DownloadManager {

    private DownloadManager() {
        
    }

    /**
     * this method download csar file from VSP repository to default browser download directory
     *
     * @param vspName
     * @throws Exception
     */
    public static void downloadCsarByNameFromVSPRepository(String vspName, Boolean isDelete) throws Exception {

        if (isDelete) {
            FileHandling.cleanCurrentDownloadDir();
        }
        HomePage.showVspRepository();
        boolean vspFound = HomePage.searchForVSP(vspName);
        if (vspFound) {
            ExtentTestActions.log(Status.INFO, String.format("Going to downloading VSP %s", vspName));
            final List<WebElement> elementsFromTable = GeneralPageElements.getElementsFromTable();
            elementsFromTable.get(0).click();
            GeneralUIUtils.waitForLoader();
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.DOWNLOAD_CSAR.getValue());
            ExtentTestActions.log(Status.INFO, "Succeeded to downloaded CSAR file named " + vspName + " into folder " + SetupCDTest.getWindowTest().getDownloadDirectory());
            GeneralUIUtils.getElementsByCSS("div[class^='w-sdc-modal-close']").forEach(e -> e.click());
            GeneralUIUtils.ultimateWait();
        }
    }


    public static void downloadCsarByNameFromVSPRepository(String vspName, String vspId) throws Exception {
        downloadCsarByNameFromVSPRepository(vspName, true);
    }

}
