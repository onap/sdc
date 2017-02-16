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

package org.openecomp.sdc.ci.tests.execute.sanity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.BreadCrumbsButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InformationalArtifacts;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.pages.ToscaArtifactsPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

public class Import extends SetupCDTest {

	@Test
	public void importResource() throws Exception {

		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CPWithAttributes.yml";

		// import Resource
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VFC, getUser());
		ResourceUIUtils.importVfc(resourceMetaData, filePath, fileName, getUser());

	}

	@Test
	public void certifyVFC() throws Exception {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CPWithAttributes.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());
		ResourceGeneralPage.clickCheckinButton(atomicResourceMetaData.getName());
		ResourceGeneralPage.clickSubmitForTestingButton(atomicResourceMetaData.getName());
		quitAndReLogin(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(atomicResourceMetaData.getName());
		TesterOperationPage.certifyComponent(atomicResourceMetaData.getName());

		quitAndReLogin(UserRoleEnum.DESIGNER);
		// GeneralUIUtils.clickBreadCrumbs(BreadCrumbsButtonsEnum.CATALOG);
		String cpVersion = GeneralUIUtils.getComponentVersion(atomicResourceMetaData.getName());
		assertTrue("V 1.0".equals(cpVersion));
	}

	@Test
	public void uploadAllInformationalArtifactPlaceholdersInVFC() throws Exception {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CPWithAttributes.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());

		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();

		for (InformationalArtifacts infoArtifact : InformationalArtifacts.values()) {
			ArtifactUIUtils.fillPlaceHolderInformationalArtifact(infoArtifact, filePath, "Heat-File 1.yaml",
					infoArtifact.name());
		}

		assertTrue(InformationalArtifactPage.checkElementsCountInTable(InformationalArtifacts.values().length,
				() -> InformationalArtifactPage.getElemenetsFromTable()));

		InformationalArtifactPage.clickAddNewArtifact();
		Select artifactLabelList = InformationalArtifactPage.artifactPopup().defineArtifactLabel("");
		assertEquals(1, artifactLabelList.getAllSelectedOptions().size());

	}

	@Test(expectedExceptions = ElementNotVisibleException.class)
	public void uploadInformationaArtifactMetdataTest() throws Exception {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CPWithAttributes.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());

		ResourceGeneralPage.getLeftMenu().moveToInformationalArtifactScreen();
		assertTrue(InformationalArtifactPage.checkElementsCountInTable(0,
				() -> InformationalArtifactPage.getElemenetsFromTable()));

		ArtifactInfo artifactInfo = new ArtifactInfo(filePath, "Heat-File 1.yaml", "new artifact", "artifact1",
				"OTHER");
		InformationalArtifactPage.clickAddNewArtifact();
		ArtifactUIUtils.fillAndAddNewArtifactParameters(artifactInfo);

		assertTrue(InformationalArtifactPage.checkElementsCountInTable(1,
				() -> InformationalArtifactPage.getElemenetsFromTable()));

		String actulaArtifactDescription = InformationalArtifactPage
				.getArtifactDescription(artifactInfo.getArtifactLabel());
		assertTrue(artifactInfo.getDescription().equals(actulaArtifactDescription));

		InformationalArtifactPage.clickEditArtifact(artifactInfo.getArtifactLabel());
		InformationalArtifactPage.artifactPopup().defineArtifactLabel("artifact2");
	}

	// @Test()
	// public void updateInformationalArtifact(){
	// ArtifactInfo artifactInfo = new ArtifactInfo("", "", "new artifact",
	// "artifact1", "");
	// InformationalArtifactPage.clickEditArtifact("artifact1");
	// String newDesc = "newDesc";
	// InformationalArtifactPage.artifactPopup().insertDescription(newDesc);
	// InformationalArtifactPage.artifactPopup().clickUpdateButton();
	// String actulaArtifactDescription =
	// InformationalArtifactPage.getArtifactDescription(artifactInfo.getArtifactLabel());
	// assertTrue(newDesc.equals(actulaArtifactDescription));
	// InformationalArtifactPage.clickEditArtifact(artifactInfo.getArtifactLabel());
	// InformationalArtifactPage.artifactPopup().defineArtifactLabel("artifact2");
	// InformationalArtifactPage.artifactPopup().selectArtifactType(artifactInfo.getArtifactType());
	// }

	@Test
	public void verifyTwoToscaArtifacts() throws Exception {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CPWithAttributes.yml";
		ResourceReqDetails atomicResourceMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(
				ResourceTypeEnum.VFC, NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfc(atomicResourceMetaData, filePath, fileName, getUser());

		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();

		// List<WebElement> elemenetsFromTable =
		// GeneralPageElements.getElemenetsFromTable();
		// Supplier<List<WebElement>> supplier = () -> elemenetsFromTable;
		// assertTrue(ToscaArtifactsPage.checkElementsCountInTable(2,
		// supplier));
		assertTrue(ToscaArtifactsPage.checkElementsCountInTable(2));
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
