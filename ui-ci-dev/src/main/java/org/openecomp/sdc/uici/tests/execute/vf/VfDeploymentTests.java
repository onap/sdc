package org.openecomp.sdc.uici.tests.execute.vf;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.regex.Pattern;

import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.FileHandling;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.utilities.RestCDUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;

public class VfDeploymentTests extends SetupCDTest {

	// *****************************EditNamePopoverTests*****************************//
	@Test
	public void ClickingOnEditNamePopoverIconShouldOpenTheEditNamePopoverForm() {
		EditNamePopoverTestsSetUp();

		assertTrue(GeneralUIUtils.isElementPresent(DataTestIdEnum.UpdateNamePopover.POPOVER_FORM.getValue()));
	}

	@Test
	public void ModuleDataShouldBeDisplayedInTheEditNameForm() {
		EditNamePopoverTestsSetUp();

		WebElement instanceName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_INSTANCE_NAME.getValue());
		WebElement heatName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_HEAT_NAME.getValue());
		WebElement moduleName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_MODULE_NAME.getValue());

		String moduleNameToDivide = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).getText();

		String[] dividedModuleName = moduleNameToDivide.split(Pattern.quote(".."));

		assertEquals(dividedModuleName[0], instanceName.getText());
		assertEquals(dividedModuleName[1], heatName.getAttribute("value"));
		assertEquals(dividedModuleName[2], moduleName.getText());

	}

	@Test
	public void CloseButtonShouldCloseThePopover() {
		EditNamePopoverTestsSetUp();

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_CLOSE_BUTTON.getValue())
				.click();

		assertFalse(GeneralUIUtils.isElementPresent(DataTestIdEnum.UpdateNamePopover.POPOVER_FORM.getValue()));
	}

	@Test
	public void XButtonShouldCloseThePopover() {
		EditNamePopoverTestsSetUp();

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_X_BUTTON.getValue())
				.click();

		assertFalse(GeneralUIUtils.isElementPresent(DataTestIdEnum.UpdateNamePopover.POPOVER_FORM.getValue()));
	}

	@Test
	public void SaveButtonShouldBeDisabledWhileTheNameHasNotBeenChanged() {
		EditNamePopoverTestsSetUp();

		WebElement popoverSaveButton = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_SAVE_BUTTON.getValue());

		assertTrue(popoverSaveButton.getAttribute("class").contains("disabled"));
	}

	@Test
	public void ClickingOnTheSaveButtonShouldUpdateTheModuleName() {
		EditNamePopoverTestsSetUp();

		String newName = "testName";

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_HEAT_NAME.getValue())
				.clear();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_HEAT_NAME.getValue())
				.sendKeys(newName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_SAVE_BUTTON.getValue())
				.click();

		GeneralUIUtils.waitForLoader();

		String moduleName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).getText();

		String[] dividedModuleName = moduleName.split(Pattern.quote(".."));

		assertEquals(dividedModuleName[1], newName);
	}

	@Test
	public void testUpdateModuleNameSanity() {
		EditNamePopoverTestsSetUp();

		String newName = "testName";

		WebElement instanceName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_INSTANCE_NAME.getValue());
		WebElement heatName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_HEAT_NAME.getValue());
		WebElement moduleName = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_MODULE_NAME.getValue());

		String moduleNameToDivide = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).getText();

		String[] dividedModuleName = moduleNameToDivide.split(Pattern.quote(".."));

		assertEquals(dividedModuleName[0], instanceName.getText());
		assertEquals(dividedModuleName[1], heatName.getAttribute("value"));
		assertEquals(dividedModuleName[2], moduleName.getText());

		WebElement popoverSaveButton = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.POPOVER_SAVE_BUTTON.getValue());

		assertTrue(popoverSaveButton.getAttribute("class").contains("disabled"));

		heatName.clear();
		heatName.sendKeys(newName);

		popoverSaveButton.click();

		GeneralUIUtils.waitForLoader();

		moduleNameToDivide = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).getText();
		dividedModuleName = moduleNameToDivide.split(Pattern.quote(".."));

		assertEquals(dividedModuleName[1], newName);
	}

	// *****************************DeploymentTabsTests*****************************//

	@Test
	public void testTabIsBeingDisplayedAtDeploymentView() {
		DeploymentTestsSetUp();

		assertTrue(GeneralUIUtils.isElementPresent(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()));
	}

	@Test
	public void testClickingOnTabSetsItAsSelected() {
		DeploymentTestsSetUp();

		WebElement hierarchyTab = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue());
		hierarchyTab.click();

		assertTrue(hierarchyTab.getAttribute("class").contains("selected"));
	}

	@Test
	public void testTabNameIsBeingDisplayedInTheSelectedTabHeader() {
		DeploymentTestsSetUp();

		// select the hierarchy tab and check the header
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		WebElement tabHeader = GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.TAB_HEADER.getValue());

		assertEquals(tabHeader.getText(), "HIERARCHY");
	}

	@Test
	public void testSelectingModuleNameInTheHierarchyTabShouldSelectIt() {
		DeploymentTestsSetUp();

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		WebElement hierarchyModule = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE_TITLE.getValue());

		hierarchyModule.click();

		assertTrue(hierarchyModule.getAttribute("class").contains("selected"));
	}

	@Test
	public void testSelectingModuleNameInTheHierarchyTabShouldExpandIt() {
		DeploymentTestsSetUp();

		// select hierarchy tab
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		WebElement hierarchyModule = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue());

		hierarchyModule.click();

		assertTrue(hierarchyModule.getAttribute("class").contains("expanded"));
	}

	@Test
	public void testSelectingModuleNameInTheHierarchyTabShouldDisplayItsData() {
		DeploymentTestsSetUp();

		// select hierarchy tab
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).click();

		assertTrue(GeneralUIUtils.isElementPresent(DataTestIdEnum.TabsBar.HIERARCHY_SELECTED_MODULE_DATA.getValue()));
	}

	@Test(enabled = false)
	public void testResourceNameIsBeingDisplayedInTheSelectedTabSubHeader() {
		DeploymentTestsSetUp();

		// select the hierarchy tab and check the header
		// WebElement tabSubHeader =
		// getWebElement(DataTestIdEnum.TabsBar.TAB_SUB_HEADER.getValue());

		// assertEquals(tabSubHeader.getText(), vmmcCsar.getName());
	}

	@Test(enabled = false)
	public void testSelectingModuleNameInTheHierarchyTabShouldDisplayItsInformation() throws IOException {
		DeploymentTestsSetUp();

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		WebElement hierarchyModule = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue());

		// get the module
		// String component = RestCDUtils.getResource(vmmcCsar).getResponse();
		// TODO idana fix test
		/*
		 * GroupDefinitionInfo module = getModuleById(component,
		 * hierarchyModule.getText());
		 * 
		 * hierarchyModule.click();
		 * 
		 * assertModuleDetails(module, hierarchyModule);
		 */

	}

	@Test(enabled = false)
	public void testSelectingModuleNameInTheHierarchyTabShouldDisplayItsArtifacts() throws IOException {
		DeploymentTestsSetUp();

		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		WebElement hierarchyModule = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue());

		// TODO idana fix test
		// Get the artifact from the module
		/*
		 * String component = RestCDUtils.getResource(vmmcCsar,
		 * getUser()).getResponse(); GroupDefinitionInfo module =
		 * getModuleById(component, hierarchyModule.getText());
		 * ArtifactDefinitionInfo artifact = module.getArtifacts().get(0);
		 * 
		 * hierarchyModule.click();
		 * 
		 * assertModuleArtifactDetails(artifact);
		 */
	}

	@Test
	public void testTabsViewSanity() throws IOException {
		DeploymentTestsSetUp();

		WebElement hierarchyTab = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue());

		assertTrue(hierarchyTab != null);

		hierarchyTab.click();

		assertTrue(hierarchyTab.getAttribute("class").contains("selected"));

		WebElement tabHeader = GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.TAB_HEADER.getValue());
		WebElement tabSubHeader = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.TAB_SUB_HEADER.getValue());

		assertEquals(tabHeader.getText(), "HIERARCHY");
		// assertEquals(tabSubHeader.getText(), vmmcCsar.getName());

		WebElement hierarchyModule = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue());
		WebElement hierarchyModuleTitle = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE_TITLE.getValue());
		hierarchyModule.click();
		WebElement selectedModuleData = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_SELECTED_MODULE_DATA.getValue());

		assertTrue(hierarchyModuleTitle.getAttribute("class").contains("selected"));
		assertTrue(hierarchyModule.getAttribute("class").contains("expanded"));
		assertTrue(selectedModuleData.getAttribute("ng-if") != null);
		// TODO idana fix test
		/*
		 * String component = RestCDUtils.getResource(vmmcCsar,
		 * getUser()).getResponse(); GroupDefinitionInfo module =
		 * getModuleById(component, hierarchyModule.getText());
		 * ArtifactDefinitionInfo artifact = module.getArtifacts().get(0);
		 * 
		 * assertModuleDetails(module, hierarchyModule);
		 * 
		 * assertModuleArtifactDetails(artifact);
		 */

	}

	// ************************DeploymentTestsSetUpFunction************************//

	private void EditNamePopoverTestsSetUp() {
		DeploymentTestsSetUp();

		// clicking on a module and opening the edit name popover
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_TAB.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.TabsBar.HIERARCHY_MODULE.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.UpdateNamePopover.OPEN_POPOVER_ICON.getValue())
				.click();
	}

	private void DeploymentTestsSetUp() {
		// import csar
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vf_with_groups.csar";
		ResourceUIUtils.importVfInUI(getUser(), filePath, fileName);

		GeneralUIUtils.waitForLoader(20);

		// moving to deployment view
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.DEPLOYMENT);
	}
}
