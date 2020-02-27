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

package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import java.util.Collections;
import java.util.List;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GAB extends SetupCDTest {

    private static final int THREAD_SLEEP_TIME = 1000;
    private static final int MAIN_PAGE_ROWS_SIZE = 2;
    private static final int MAIN_PAGE_COLUMN_SIZE = 5;

    private String pnfFilePath;
    private String vnfFilePath;

    @BeforeClass
    public void beforeClass() {
        pnfFilePath = FileHandling.getFilePath("PNFs");
        vnfFilePath = FileHandling.getFilePath("VNFs");
    }

    @Test
    public void addPmDictionaryDeploymentArtifactToPnfAndCheckMagnifierTest() throws Exception {
        final int expectedHeaderSize = MAIN_PAGE_COLUMN_SIZE + 10;
        final int expectedRowSize = MAIN_PAGE_ROWS_SIZE + 3;
        ResourceReqDetails pnfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.PNF, getUser());
        ResourceUIUtils.createPNF(pnfMetaData, getUser());
        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        ArtifactInfo art1 = new ArtifactInfo(pnfFilePath, "pmDictionary.yml", "desc", "artifactpm", "PM_DICTIONARY");
        addArtifactAndOpenGAB(art1);
        assertHeaderAndRowSize(expectedHeaderSize, expectedRowSize);
    }

    @Test
    public void addVesEventsDeploymentArtifactToVfAndCheckMagnifierTest() throws Exception {
        final int expectedHeaderSize = MAIN_PAGE_COLUMN_SIZE + 4;
        final int expectedRowSize = MAIN_PAGE_ROWS_SIZE + 3;
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(vfMetaData, getUser());
        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        ArtifactInfo art1 = new ArtifactInfo(vnfFilePath, "vesEvent.yml", "desc", "artifactfault", "VES_EVENTS");
        addArtifactAndOpenGAB(art1);
        assertHeaderAndRowSize(expectedHeaderSize, expectedRowSize);
    }

    private void addArtifactAndOpenGAB(ArtifactInfo art1) throws Exception {
        addNewArtifact(Collections.singletonList(art1));
        openGABPopup(art1);
    }

    private void assertHeaderAndRowSize(int expectedHeaderSize, int expectedRowSize) {
        List<WebElement> headers = getListOfHeaders();
        AssertJUnit.assertEquals(expectedHeaderSize, headers.size());
        List<WebElement> rows = GeneralUIUtils.getWebElementsListByContainsClassName("datatable-body-row");
        AssertJUnit.assertEquals(expectedRowSize, rows.size());
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
        return GeneralUIUtils.getWebElementsListByClassName("datatable-header-cell");
    }

    private void addNewArtifact(List<ArtifactInfo> deploymentArtifactList) throws Exception {
        for (ArtifactInfo deploymentArtifact : deploymentArtifactList) {
            DeploymentArtifactPage.clickAddNewArtifact();
            ArtifactUIUtils.fillAndAddNewArtifactParameters(deploymentArtifact);
        }
        AssertJUnit.assertTrue(DeploymentArtifactPage.checkElementsCountInTable(deploymentArtifactList.size()));
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
