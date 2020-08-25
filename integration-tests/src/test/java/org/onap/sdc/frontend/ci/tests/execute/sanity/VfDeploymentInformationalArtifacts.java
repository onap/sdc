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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import org.onap.sdc.frontend.ci.tests.datatypes.ArtifactInfo;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.frontend.ci.tests.pages.CompositionPage;
import org.onap.sdc.frontend.ci.tests.utilities.ArtifactUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class VfDeploymentInformationalArtifacts extends SetupCDTest {


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // START US824719
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // US824719 - Import VSP - VF informational artifacts
    // TC1434241 - Import VF Artifacts - Deployment Artifacts - One Artifact, One Type
    @Test
    public void importVfArtifactsDeploymentArtifactsOneArtifactOneType() throws Exception {
        String fileName = "TC1434241.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname1.yaml", null, "heatartifactname1", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1434245 - Import VF Artifacts - Deployment Artifacts - Multiple Artifacts, Multiple Types
    @Test
    public void importVfArtifactsDeploymentArtifactsMultipleArtifactsMultipleTypes() throws Exception {
        String fileName = "TC1434245.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname1.yaml", null, "heatartifactname1", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname2.yaml", null, "heatartifactname2", ArtifactTypeEnum.HEAT.getType(), "1"));

        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName1.yaml", null, "HeatVolArtifactName1", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName2.yaml", null, "HeatVolArtifactName2", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName3.yaml", null, "HeatVolArtifactName3", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));

        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1434247 - Import VF Artifacts - Informational Artifacts - One Artifact, One Type
    @Test
    public void importVfArtifactsInformationalArtifactsOneArtifactOneType() throws Exception {
        String fileName = "TC1434247.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1434248 - 	Import VF Artifacts - Informational Artifacts - Multiple Artifacts, Multiple Types
    @Test
    public void importVfArtifactsInformationalArtifactsMultipleArtifactsMultipleTypes() throws Exception {
        String fileName = "TC1434248.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1"));
        informationalArtifacts.add(new ArtifactInfo(null, "GuideInfoArtifact1.yml", null, "GuideInfoArtifact1", ArtifactTypeEnum.GUIDE.getType(), "1"));
        informationalArtifacts.add(new ArtifactInfo(null, "GuideInfoArtifact2.yml", null, "GuideInfoArtifact2", ArtifactTypeEnum.GUIDE.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1434249 - 	Import VF Artifacts - Deployment and Informational Artifacts - Multiple Artifacts, Multiple Types
    @Test
    public void importVfArtifactsDeploymentAndInformationalArtifactsMultipleArtifactsMultipleTypes() throws Exception {
        String fileName = "TC1434249.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname1.yaml", null, "heatartifactname1", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname2.yaml", null, "heatartifactname2", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName1.yaml", null, "HeatVolArtifactName1", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName2.yaml", null, "HeatVolArtifactName2", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "HeatVolArtifactName3.yaml", null, "HeatVolArtifactName3", ArtifactTypeEnum.HEAT_VOL.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1"));
        informationalArtifacts.add(new ArtifactInfo(null, "GuideInfoArtifact1.yml", null, "GuideInfoArtifact1", ArtifactTypeEnum.GUIDE.getType(), "1"));
        informationalArtifacts.add(new ArtifactInfo(null, "GuideInfoArtifact2.yml", null, "GuideInfoArtifact2", ArtifactTypeEnum.GUIDE.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);
    }

    // TODO: there is defect in flow: "Updated button enabled for artifact in invalid type folder"
    // TODO: re-check it after defect fix
    // US824719 - Import VSP - VF informational artifacts
    // TC1438310 - Import VF Artifacts - Deployment Artifacts - Artifact Type Invalid
    @Test
    public void importVFArtifactsDeploymentArtifactsArtifactTypeInvalid() throws Exception {
        String fileName = "DeploymentArtifactWithInvalidType.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "ArtifactName.yaml", null, "ArtifactName", ArtifactTypeEnum.OTHER.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1438311 - Import VF Artifacts - Informational Artifacts - Artifact Type Invalid
    @Test
    public void importVfArtifactsInformationalArtifactsArtifactTypeInvalid() throws Exception {
        String fileName = "InformationArtifactWithInvalidType.csar";
        String folder = "US825779";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "ArtifactName.yaml", null, "ArtifactName", ArtifactTypeEnum.OTHER.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1438231 - 	Import VF Artifacts - Deployment Artifacts - Artifact Name To Long
    @Test
    public void importVfArtifactsDeploymentArtifactsArtifactNameToLong() throws Exception {
        String folder = "US825779";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String fileName = "DeploymentArtifactWithLongName.csar";

        importVfFromCsar(resourceMetaData, folder, fileName, getUser());

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.EXCEEDS_LIMIT.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    // US824719 - Import VSP - VF informational artifacts
    // TC1438232 - Import VF Artifacts - Informational Artifacts - Artifact Name To Long
    // TODO: make informational artifact name longer then 255
    // TODO: windows/linux not allowed it
    @Test(enabled = true)
    public void importVfArtifactsInformationalArtifactsArtifactNameToLong() throws Exception {
        String folder = "US825779";
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String fileName = "InformationArtifactWithLongName.csar";

        importVfFromCsar(resourceMetaData, folder, fileName, getUser());

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.EXCEEDS_LIMIT.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // END US824719
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // START US825779
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1443954 - Update With Multiple Changes In Deployment And Informational Artifacts
    @Test
    public void updateWithMultipleChangesInDeploymentAndInformationalArtifacts() throws Exception {
        String folder = "US825779";
        String fileName = "ImportTC1443954.csar";

        ArtifactInfo deploymentHeat1 = new ArtifactInfo(null, "heatartifactname1.yaml", null, "heatartifactname1", ArtifactTypeEnum.HEAT.getType(), "1");
        ArtifactInfo deploymentHeat2 = new ArtifactInfo(null, "heatartifactname2.yaml", null, "heatartifactname2", ArtifactTypeEnum.HEAT.getType(), "1");
        ArtifactInfo deploymentHeat3 = new ArtifactInfo(null, "heatartifactname3.yaml", null, "heatartifactname3", ArtifactTypeEnum.HEAT.getType(), "1");

        ArtifactInfo deploymentHeatVol1 = new ArtifactInfo(null, "HeatVolArtifactName1.yaml", null, "HeatVolArtifactName1", ArtifactTypeEnum.HEAT_VOL.getType(), "1");
        ArtifactInfo deploymentHeatVol2 = new ArtifactInfo(null, "HeatVolArtifactName2.yaml", null, "HeatVolArtifactName2", ArtifactTypeEnum.HEAT_VOL.getType(), "1");

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(deploymentHeat1);
        deploymentArtifacts.add(deploymentHeat2);
        deploymentArtifacts.add(deploymentHeat3);
        deploymentArtifacts.add(deploymentHeatVol1);
        deploymentArtifacts.add(deploymentHeatVol2);

        ArtifactInfo infoGuide1 = new ArtifactInfo(null, "GuideInfoArtifact1.yml", null, "GuideInfoArtifact1", ArtifactTypeEnum.GUIDE.getType(), "1");
        ArtifactInfo infoGuide2 = new ArtifactInfo(null, "GuideInfoArtifact2.yml", null, "GuideInfoArtifact2", ArtifactTypeEnum.GUIDE.getType(), "1");

        ArtifactInfo infoOther1 = new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1");
        ArtifactInfo infoOther2 = new ArtifactInfo(null, "artifactname2.txt", null, "artifactname2", ArtifactTypeEnum.OTHER.getType(), "1");
        ArtifactInfo infoOther3 = new ArtifactInfo(null, "artifactname3.txt", null, "artifactname3", ArtifactTypeEnum.OTHER.getType(), "1");

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(infoGuide1);
        informationalArtifacts.add(infoGuide2);
        informationalArtifacts.add(infoOther1);
        informationalArtifacts.add(infoOther2);
        informationalArtifacts.add(infoOther3);

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1443954.csar";

        List<ArtifactInfo> informationalArtifactsNotExist = new ArrayList<ArtifactInfo>();
        List<ArtifactInfo> deploymentArtifactsNotExist = new ArrayList<ArtifactInfo>();

        // Changes in deployment artifacts
        deploymentArtifactsNotExist.add(deploymentHeat1);
        deploymentArtifactsNotExist.add(deploymentHeat2);
        deploymentArtifacts.remove(deploymentHeat1);
        deploymentArtifacts.remove(deploymentHeat2);
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname4.yaml", null, "heatartifactname4", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "heatartifactname5.yaml", null, "heatartifactname5", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentHeatVol1.setArtifactVersion("2");
        deploymentHeatVol2.setArtifactVersion("2");

        // Changes in informational artifacts
        infoGuide1.setArtifactVersion("2");
        infoOther1.setArtifactVersion("2");
        informationalArtifactsNotExist.add(infoGuide2);
        informationalArtifactsNotExist.add(infoOther2);
        informationalArtifacts.remove(infoGuide2);
        informationalArtifacts.remove(infoOther2);
        informationalArtifacts.add(new ArtifactInfo(null, "GuideInfoArtifact3.yml", null, "GuideInfoArtifact3", ArtifactTypeEnum.GUIDE.getType(), "1"));
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname4.txt", null, "artifactname4", ArtifactTypeEnum.OTHER.getType(), "1"));

        updateVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(folder, fileName, deploymentArtifacts, informationalArtifacts, deploymentArtifactsNotExist, informationalArtifactsNotExist);
    }


    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444206 - Update With Existed Deployment Artifact By Artifact With Different Type
    @Test
    public void updateWithExistedDeploymentArtifactByArtifactWithDifferentType() throws Exception {
        String folder = "US825779";
        String fileName = "ImportTC1444206.csar";
        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));
        deploymentArtifacts.add(new ArtifactInfo(null, "artifactname1.yaml", null, "artifactname1", ArtifactTypeEnum.HEAT.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444206.csar";
        String filePath = FileHandling.getFilePath(folder);

        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.ARTIFACT_EXIST.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444207 - Update With Existed Informational Artifact By Artifact With Different Type
    @Test
    public void updateWithExistedInformationalArtifactByArtifactWithDifferentType() throws Exception {
        String folder = "US825779";
        String fileName = "ImportTC1444207.csar";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, informationalArtifacts);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444207.csar";
        String filePath = FileHandling.getFilePath(folder);

        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.ARTIFACT_EXIST.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }


    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444208 - Update With Existed Informational Artifact By Deployment Artifact With Different Type
    @Test
    public void updateWithExistedInformationalArtifactByDeploymentArtifactWithDifferentType() throws Exception {
        String folder = "US825779";
        String fileName = "ImportTC1444208.csar";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));
        deploymentArtifacts.add(new ArtifactInfo(null, "artifactname1.yaml", null, "artifactname1", ArtifactTypeEnum.HEAT.getType(), "1"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444208.csar";
        String filePath = FileHandling.getFilePath(folder);

        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.ARTIFACT_EXIST.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444520 - Update Deployment Artifact With Name To Long
    @Test
    public void updateDeploymentArtifactWithNameToLong() throws Exception {
        String folder = "US825779";

        String fileName = "ImportTC1444520.csar";

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, null, null);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444520.csar";
        String filePath = FileHandling.getFilePath(folder);

        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.EXCEEDS_LIMIT.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444521 - Update Informational Artifact With Name To Long
    @Test
    public void updateInformationalArtifactWithNameToLong() throws Exception {

        String folder = "US825779";
        String fileName = "ImportTC1444521.csar";

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, null, null);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444521.csar";
        String filePath = FileHandling.getFilePath(folder);

        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        String errorMessage = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
        String checkUIResponseOnError = ErrorValidationUtils.checkUIResponseOnError(ActionStatus.EXCEEDS_LIMIT.name());
        assertThat(errorMessage).contains(checkUIResponseOnError);
    }

    // US825779 - Story: [BE] Import VSP - VF informational artifacts  - Update
    // TC1444531 - Update Informational Artifact With Invalid Type
    @Test
    public void updateInformationalArtifactWithInvalidType() throws Exception {
        String folder = "US825779";
        String fileName = "ImportTC1444531.csar";

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, null, null);

        GeneralUIUtils.clickOnElementByTestId("breadcrumbs-button-1");

        fileName = "UpdateTC1444531.csar";

        List<ArtifactInfo> informationalArtifacts = new ArrayList<ArtifactInfo>();
        informationalArtifacts.add(new ArtifactInfo(null, "artifactname1.xml", null, "artifactname1", ArtifactTypeEnum.OTHER.getType(), "1"));

        updateVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(folder, fileName, null, informationalArtifacts, null, null);
    }


    @Test
    public void importValidInformationalArtifactInInvalidFolerTest_TC1438313() throws Exception {
        String fileName = "ValidArtifactNameInInvalidFolder.csar";
        String folder = "US824719";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "1"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));

        importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(fileName, folder, deploymentArtifacts, null);
    }

    @Test
    public void updateValidInformationalArtifactInInvalidFolerTest_TC1444533() throws Exception {
        String fileName = "ImportTC1444533.csar";
        String folder = "US824719";
        String filePath = FileHandling.getFilePath(folder);

        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());

        String updatedCsarFileName = "UpdateTC1444533.csar";

        List<ArtifactInfo> deploymentArtifacts = new ArrayList<ArtifactInfo>();
        deploymentArtifacts.add(new ArtifactInfo(null, "base_ldsa.yaml", null, "base_ldsa", ArtifactTypeEnum.HEAT.getType(), "2"));
        deploymentArtifacts.add(new ArtifactInfo(null, "module_1_ldsa.yaml", null, "module_1_ldsa", ArtifactTypeEnum.HEAT.getType(), "4"));

        updateVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(folder, updatedCsarFileName, deploymentArtifacts, null, null, null);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // END US825779
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void updateVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(String folder, String fileName,
                                                                                             List<ArtifactInfo> deploymentArtifacts, List<ArtifactInfo> informationalArtifacts,
                                                                                             List<ArtifactInfo> deploymentArtifactsNotExist, List<ArtifactInfo> informationalArtifactsNotExist) throws Exception {
        String filePath = FileHandling.getFilePath(folder);
        ResourceUIUtils.updateVfWithCsar(filePath, fileName);

        validateDeploymentArtifactPage(deploymentArtifacts, null);
        validateInformationalArtifactPage(informationalArtifacts, null);

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        validateDeploymentArtifactInComposition(deploymentArtifacts, null);
        validateInformationalArtifactInComposition(informationalArtifacts, null);

    }


    public void importVfFromCsar(ResourceReqDetails resourceMetaData, String folder, String fileName, User user) {
        String filePath = FileHandling.getFilePath(folder);
        GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.Dashboard.IMPORT_AREA.getValue());
        // Insert file to the browse dialog
        WebElement browseWebElement = GeneralUIUtils.getInputElement(DataTestIdEnum.Dashboard.IMPORT_VF_FILE.getValue());
        browseWebElement.sendKeys(filePath + fileName);

        // Fill the general page fields.
        GeneralUIUtils.waitForLoader();
        ResourceUIUtils.fillResourceGeneralInformationPage(resourceMetaData, getUser(), true);
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
    }


    public void importVfAndValidateInformationalDeploymentArtifactPagesOnPagesAndComposition(String fileName, String folder, List<ArtifactInfo> deploymentArtifacts, List<ArtifactInfo> informationalArtifacts) throws Exception {
        String filePath = FileHandling.getFilePath(folder);
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, fileName, getUser());

        validateDeploymentArtifactPage(deploymentArtifacts, null);
        validateInformationalArtifactPage(informationalArtifacts, null);

        ResourceGeneralPage.getLeftMenu().moveToCompositionScreen();

        validateDeploymentArtifactInComposition(deploymentArtifacts, null);
        validateInformationalArtifactInComposition(informationalArtifacts, null);
    }

    public void validateInformationalArtifactInComposition(List<ArtifactInfo> informationalArtifacts, List<ArtifactInfo> informationalArtifactsNotExist) throws Exception {
        CompositionPage.showInformationArtifactTab();
        // Composition informational
        if (informationalArtifacts != null && informationalArtifacts.size() > 0) {
            validateEachArtifactOnCompositionRightMenuInformationPage(informationalArtifacts);
        }
        if (informationalArtifactsNotExist != null && informationalArtifactsNotExist.size() > 0) {
            validateEachArtifactNotExistOnCompositionRightMenuInformationPage(informationalArtifactsNotExist);
        }
    }

    public void validateDeploymentArtifactInComposition(List<ArtifactInfo> deploymentArtifacts, List<ArtifactInfo> deploymentArtifactsNotExist) throws Exception {
        CompositionPage.showDeploymentArtifactTab();
        // Composition deployment
        if (deploymentArtifacts != null && deploymentArtifacts.size() > 0) {
            validateEachArtifactOnCompositionRightMenuDeploymentPage(deploymentArtifacts);
        }
        if (deploymentArtifactsNotExist != null && deploymentArtifactsNotExist.size() > 0) {
            validateEachArtifactNotExistOnCompositionRightMenuDeploymentPage(deploymentArtifactsNotExist);
        }
    }

    public void validateInformationalArtifactPage(List<ArtifactInfo> informationalArtifacts, List<ArtifactInfo> informationalArtifactsNotExist) {
        ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
        // Informational page
        if (informationalArtifacts != null && informationalArtifacts.size() > 0) {
            validateEachArtifactInformationPage(informationalArtifacts);
        }
        if (informationalArtifactsNotExist != null && informationalArtifactsNotExist.size() > 0) {
            validateEachArtifactNotExistInformationPage(informationalArtifactsNotExist);
        }
    }

    public void validateDeploymentArtifactPage(List<ArtifactInfo> deploymentArtifacts, List<ArtifactInfo> deploymentArtifactsNotExist) {
        ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
        // Deployment page
        if (deploymentArtifacts != null && deploymentArtifacts.size() > 0) {
            validateEachArtifactOnDeploymentPage(deploymentArtifacts);
        }
        if (deploymentArtifactsNotExist != null && deploymentArtifactsNotExist.size() > 0) {
            validateEachArtifactNotExistOnDeploymentPage(deploymentArtifactsNotExist);
        }
    }

    // TODO: add validation that if not editable / deleteable then button should not appear
    public void validateEachArtifactOnDeploymentPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            String type = artifact.getArtifactType();
            String label = artifact.getArtifactLabel();
            String version = artifact.getArtifactVersion();

            if (type.equals(ArtifactTypeEnum.HEAT.getType()) || type.equals(ArtifactTypeEnum.HEAT_VOL.getType()) || type.equals(ArtifactTypeEnum.HEAT_NET.getType())) {
                ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(label, null, version, type, true, false, false, true);
            } else {
                ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(label, null, version, type, true, true, true, false);
            }
        }
    }

    public void validateEachArtifactNotExistOnDeploymentPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateNotExistArtifactOnDeploymentInformationPage(artifact.getArtifactLabel());
        }
    }

    public void validateEachArtifactInformationPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(artifact.getArtifactLabel(), null, artifact.getArtifactVersion(), artifact.getArtifactType(), true, true, true, false);
        }
    }

    public void validateEachArtifactNotExistInformationPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateNotExistArtifactOnDeploymentInformationPage(artifact.getArtifactLabel());
        }
    }

    public void validateEachArtifactOnCompositionRightMenuDeploymentPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {

            String type = artifact.getArtifactType();
            String label = artifact.getArtifactLabel();
            String filename = artifact.getFilename();

            if (type.equals(ArtifactTypeEnum.HEAT.getType()) || type.equals(ArtifactTypeEnum.HEAT_VOL.getType()) || type.equals(ArtifactTypeEnum.HEAT_NET.getType())) {
                ArtifactUIUtils.validateExistArtifactOnCompositionRightMenuDeploymentInformationPage(filename, label, false, true, true, false);
            } else {
                ArtifactUIUtils.validateExistArtifactOnCompositionRightMenuDeploymentInformationPage(filename, label, true, false, true, true);
            }
        }
    }

    public void validateEachArtifactNotExistOnCompositionRightMenuDeploymentPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateNotExistArtifactOnCompositionRightMenuDeploymentInformationPage(artifact.getArtifactLabel());
        }
    }

    // TODO: there is defect in this flow
    // TODO: change isEditable to false when defect fix
    public void validateEachArtifactOnCompositionRightMenuInformationPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateExistArtifactOnCompositionRightMenuDeploymentInformationPage(artifact.getFilename(), artifact.getArtifactLabel(), true, false, true, true);
        }
    }

    public void validateEachArtifactNotExistOnCompositionRightMenuInformationPage(List<ArtifactInfo> artifactInfoList) {
        for (ArtifactInfo artifact : artifactInfoList) {
            ArtifactUIUtils.validateNotExistArtifactOnCompositionRightMenuDeploymentInformationPage(artifact.getArtifactLabel());
        }
    }


    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
