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

package org.onap.sdc.frontend.ci.tests.US;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasElement;
import org.onap.sdc.frontend.ci.tests.datatypes.CanvasManager;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.InformationalArtifactPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.onap.sdc.frontend.ci.tests.pages.UploadArtifactPopup;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ServiceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class RemoveRestrictionOfDeploymentArtifacts extends SetupCDTest {

    private String folder = "";

    // US833330 - Story [BE] - remove restriction of deployment artifacts
    // Create service without resource instance and without deployment artifacts and verify it can submit for testing
    @Test
    public void createServiceWithoutRIAndArtifacts() throws Exception {
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
    }


    // US833330 - Story [BE] - remove restriction of deployment artifacts
    // Create service with VL resource instance and without deployment artifacts and verify it can submit for testing
    @Test
    public void createServiceWithVlAndWithoutArtfiacts() throws Exception {
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        canvasManager.createElementOnCanvas(LeftPanelCanvasItems.NETWORK);
        canvasManager.createElementOnCanvas(LeftPanelCanvasItems.NETWORK);
        canvasManager.createElementOnCanvas(LeftPanelCanvasItems.NETWORK);

        ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
    }

    // US833330 - Story [BE] - remove restriction of deployment artifacts
    // Create service with VF with informational artifacts and verify it can submit for testing
    @Test
    public void createServiceWithInformationalArtifacts() throws Exception {
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.createVF(resourceMetaData, getUser());

        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();

        String filePath = FileHandling.getFilePath(folder);
        List<ArtifactInfo> informationalArtifactList = new ArrayList<ArtifactInfo>();
        informationalArtifactList.add(new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "OTHER"));
        informationalArtifactList.add(new ArtifactInfo(filePath, "sample-xml-alldata-1-1.xml", "cuku", "artifact2", "GUIDE"));
        for (ArtifactInfo informationalArtifact : informationalArtifactList) {
            InformationalArtifactPage.clickAddNewArtifact();
            ArtifactUIUtils.fillAndAddNewArtifactParameters(informationalArtifact, new UploadArtifactPopup(true));
        }
        //TODO Andrey should click on certify button
        ResourceGeneralPage.clickCertifyButton(resourceMetaData.getName());
		
		/*reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
		TesterOperationPage.certifyComponent(resourceMetaData.getName());
		reloginWithNewRole(UserRoleEnum.DESIGNER);*/

        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);
        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();
        CanvasManager canvasManager = CanvasManager.getCanvasManager();
        CanvasElement resourceInstance = canvasManager.createElementOnCanvas(resourceMetaData.getName());

        ResourceGeneralPage.clickSubmitForTestingButton(serviceMetadata.getName());
    }

}
