/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import static org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage.XpathSelector.DATA_TABLE_BODY;
import static org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage.XpathSelector.DOWNLOAD_LINK;
import static org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage.XpathSelector.MAIN_DIV;
import static org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage.XpathSelector.TITLE_DIV;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ToscaArtifactsPage extends AbstractPageObject {

    private WebElement wrappingElement;
    private WebElement artifactsTableBody;
    private final List<String> downloadedArtifactList = new ArrayList<>();

    public ToscaArtifactsPage(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrappingElement = waitForElementVisibility(By.xpath(MAIN_DIV.getXpath()), 5);
        waitForElementVisibility(By.xpath(TITLE_DIV.getXpath()), 5);
        artifactsTableBody = waitForElementVisibility(By.xpath(DATA_TABLE_BODY.getXpath()), 5);
    }

    public void clickOnDownload(final String artifactName) {
        artifactsTableBody.findElement(By.xpath(DOWNLOAD_LINK.getXpath(artifactName))).click();
    }

    public void addToDownloadedArtifactList(final String downloadedArtifactName) {
        if (downloadedArtifactName == null) {
            return;
        }
        downloadedArtifactList.add(downloadedArtifactName);
    }

    public List<String> getDownloadedArtifactList() {
        return new ArrayList<>(downloadedArtifactList);
    }

    /**
     * Enum that contains identifiers and xpath expressions to elements related to the enclosing page object.
     */
    @AllArgsConstructor
    public enum XpathSelector {
        MAIN_DIV("w-sdc-main-right-container", "//div[@class='%s']"),
        TITLE_DIV("tab-title", "//div[contains(@class,'%s') and contains(text(), 'TOSCA Artifacts')]"),
        DATA_TABLE_BODY("//datatable-body"),
        DOWNLOAD_LINK("//div[contains(@data-tests-id,'download_%s')]");

        @Getter
        private String id;

        private final String xpathFormat;

        XpathSelector(final String xpathFormat) {
            this.xpathFormat = xpathFormat;
        }

        public String getXpath() {
            return String.format(xpathFormat, id);
        }

        public String getXpath(final String... params) {
            return String.format(xpathFormat, params);
        }

    }
}
