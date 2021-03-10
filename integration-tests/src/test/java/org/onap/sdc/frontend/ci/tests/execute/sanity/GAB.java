/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020  Nokia Property. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import java.util.Collections;
import java.util.List;

import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.frontend.ci.tests.pages.DeploymentArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.GeneralPageElements;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GAB extends SetupCDTest {

    private static final int THREAD_SLEEP_TIME = 1000;

    private String pnfFilePath;
    private String vnfFilePath;

    @BeforeClass
    public void beforeClass() {
        pnfFilePath = FileHandling.getFilePath("PNFs");
        vnfFilePath = FileHandling.getFilePath("VNFs");
    }

    @Test
    public void addPmDictionaryDeploymentArtifactToPnfAndCheckMagnifierTest() throws Exception {
        final int expectedHeaderSize = 10;
        final int expectedRowSize = 3;
        ResourceReqDetails pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
        ResourceUIUtils.createPNF(pnfMetaData, getUser());
        GeneralPageElements.getLeftMenu().moveToDeploymentArtifactScreen();
        ArtifactInfo art1 = new ArtifactInfo(pnfFilePath, "pmDictionary.yml", "desc", "artifactpm", "PM_DICTIONARY");
        addArtifactAndOpenGAB(art1);
        assertHeaderAndRowSize(expectedHeaderSize, expectedRowSize);
    }

    @Test
    public void addVesEventsDeploymentArtifactToVfAndCheckMagnifierTest() throws Exception {
        final int expectedHeaderSize = 4;
        final int expectedRowSize = 3;
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        GeneralPageElements.getLeftMenu().moveToDeploymentArtifactScreen();
        ArtifactInfo art1 = new ArtifactInfo(vnfFilePath, "vesEvent.yml", "desc", "artifactfault", "VES_EVENTS");
        addArtifactAndOpenGAB(art1);
        assertHeaderAndRowSize(expectedHeaderSize, expectedRowSize);
    }

    private void addArtifactAndOpenGAB(ArtifactInfo art1) throws Exception {
        addNewArtifact(Collections.singletonList(art1));
        openGABPopup(art1);
    }

    private void assertHeaderAndRowSize(final int expectedHeaderSize, final int expectedRowSize) {
        final List<WebElement> headers = getListOfHeaders();
        AssertJUnit.assertEquals(expectedHeaderSize, headers.size());
        final List<WebElement> rows = getListOfRows();
        AssertJUnit.assertEquals(expectedRowSize, rows.size());
    }

    private List<WebElement> getListOfRows() {
        return GeneralUIUtils.getWebElementsListBy(By.xpath("//sdc-modal//datatable-body//datatable-body-row"));
    }

    private void openGABPopup(ArtifactInfo art1) throws InterruptedException {
        SetupCDTest.getExtendTest()
                .log(Status.INFO, String.format("Clicking on magnifier button %s", art1.getArtifactLabel()));
        WebElement magnifierButtonElement = GeneralUIUtils.getWebElementByTestID(
                DataTestIdEnum.ArtifactPageEnum.BROWSE_ARTIFACT.getValue() + art1.getArtifactLabel());
        SetupCDTest.getExtendTest()
                .log(Status.INFO, String.format("Found magnifier button: %s", magnifierButtonElement.getText()));
        magnifierButtonElement.click();
        Thread.sleep(THREAD_SLEEP_TIME); // have to wait until table will be rendered
    }

    private List<WebElement> getListOfHeaders() {
        return GeneralUIUtils.getWebElementsListBy(By.xpath("//sdc-modal//datatable-header//datatable-header-cell"));
    }

    private void addNewArtifact(List<ArtifactInfo> deploymentArtifactList) throws Exception {
        for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
            DeploymentArtifactPage.clickAddNewArtifact();
            ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
        }
        AssertJUnit.assertTrue(DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
    }

}
