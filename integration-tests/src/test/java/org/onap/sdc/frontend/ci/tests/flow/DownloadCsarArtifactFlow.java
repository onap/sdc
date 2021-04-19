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

package org.onap.sdc.frontend.ci.tests.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.aventstack.extentreports.Status;
import java.io.File;
import java.time.Duration;
import java.util.Optional;
import lombok.Setter;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;

/**
 * UI Flow for downloading Tosca CSAR from a component
 */
public class DownloadCsarArtifactFlow extends AbstractUiTestFlow {

    private ToscaArtifactsPage toscaArtifactsPage;
    @Setter
    private long waitBeforeGetTheFile = 0L;

    public DownloadCsarArtifactFlow(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public Optional<PageObject> run(final PageObject... pageObjects) {
        extendTest.log(Status.INFO, "Downloading Tosca CSAR generated");
        final ComponentPage componentPage = findParameter(pageObjects, ComponentPage.class);
        componentPage.isLoaded();
        toscaArtifactsPage = componentPage.goToToscaArtifacts();
        toscaArtifactsPage.isLoaded();
        toscaArtifactsPage.clickOnDownload("Tosca Model");

        final File downloadedCsar = waitAndGetDownloadedCsar();
        assertThat("The downloaded CSAR should exist", downloadedCsar, is(notNullValue()));
        assertThat("The downloaded CSAR should exist", downloadedCsar.exists(), is(true));
        toscaArtifactsPage.addToDownloadedArtifactList(downloadedCsar.getName());
        extendTest.log(Status.INFO, "Tosca CSAR was successfully downloaded");
        ExtentTestActions.takeScreenshot(Status.INFO, "tosca-artifact-csar-downloaded", "TOSCA Artifact Downloaded");
        return Optional.of(toscaArtifactsPage);
    }

    @Override
    public Optional<ToscaArtifactsPage> getLandedPage() {
        return Optional.ofNullable(toscaArtifactsPage);
    }

    private File waitAndGetDownloadedCsar() {
        new Actions(webDriver).pause(Duration.ofSeconds(waitBeforeGetTheFile)).perform();
        final FluentWait<String> fluentWait = new FluentWait<>("").withTimeout(Duration.ofSeconds(5)).pollingEvery(Duration.ofSeconds(1));
        fluentWait.until(s -> FileHandling.getLastModifiedFileNameFromDir() != null);
        return FileHandling.getLastModifiedFileNameFromDir();
    }
}
