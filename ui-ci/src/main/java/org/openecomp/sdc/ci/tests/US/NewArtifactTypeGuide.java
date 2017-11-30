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

package org.openecomp.sdc.ci.tests.US;

import static org.testng.AssertJUnit.assertTrue;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.UploadArtifactPopup;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


public class NewArtifactTypeGuide extends SetupCDTest {
	
	private String folder ="";

	// US820276
	// Upload information artifact of type GUIDE to VF
	@Test
	public void crudGuideInformationArtifactForVf() throws Exception {
		String filePath = FileHandling.getFilePath(folder);

		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createVF(resourceMetaData, getUser());
			
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		ArtifactInfo informationArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "GUIDE");

		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationArtifact);
		
		assertTrue("Only created artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
		
		String artifactUUID = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + informationArtifact.getArtifactLabel()).getText();
		ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(informationArtifact.getArtifactLabel(), null, "1", informationArtifact.getArtifactType(), true, true, true, false);
		
		InformationalArtifactPage.clickEditArtifact(informationArtifact.getArtifactLabel());
		UploadArtifactPopup artifactPopup = new UploadArtifactPopup();
		artifactPopup.loadFile(filePath, "CP.yml");
		artifactPopup.clickDoneButton();
		
		assertTrue("Only updated artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
		Assert.assertNotEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + informationArtifact.getArtifactLabel()).getText(), artifactUUID);
		ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(informationArtifact.getArtifactLabel(), null, "2", informationArtifact.getArtifactType(), true, true, true, false);
		
		InformationalArtifactPage.clickDeleteArtifact(informationArtifact.getArtifactLabel());
		InformationalArtifactPage.clickOK();
	}
	
	// US820276
	// Upload information artifact of type GUIDE to VFC
	@Test
	public void crudGuideInformationArtifactForVfc() throws Exception {
		String filePath = FileHandling.getFilePath(folder);

		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VFC, getUser());
		ResourceUIUtils.importVfc(resourceMetaData, filePath, "guidevFW_VFC.yml", getUser());
			
		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		
		ArtifactInfo informationArtifact = new ArtifactInfo(filePath, "asc_heat 0 2.yaml", "kuku", "artifact1", "GUIDE");

		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(informationArtifact);
		
		assertTrue("Only created artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
		String artifactUUID = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + informationArtifact.getArtifactLabel()).getText();
		ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(informationArtifact.getArtifactLabel(), null, "1", informationArtifact.getArtifactType(), true, true, true, false);
		
		InformationalArtifactPage.clickEditArtifact(informationArtifact.getArtifactLabel());
		UploadArtifactPopup artifactPopup = new UploadArtifactPopup();
		artifactPopup.loadFile(filePath, "CP.yml");
		artifactPopup.clickDoneButton();
		
		assertTrue("Only updated artifact need to be exist", DeploymentArtifactPage.checkElementsCountInTable(1));
		Assert.assertNotEquals(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.UUID.getValue() + informationArtifact.getArtifactLabel()).getText(), artifactUUID);
		ArtifactUIUtils.validateExistArtifactOnDeploymentInformationPage(informationArtifact.getArtifactLabel(), null, "2", informationArtifact.getArtifactType(), true, true, true, false);
		
		InformationalArtifactPage.clickDeleteArtifact(informationArtifact.getArtifactLabel());
		InformationalArtifactPage.clickOK();
	}

	

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
