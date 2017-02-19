package org.openecomp.sdc.uici.tests.utilities;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public final class ResourceUIUtils {
	public static final String RESOURCE_NAME_PREFIX = "ResourceCDTest-";
	protected static final boolean IS_BEFORE_TEST = true;
	public static final String INITIAL_VERSION = "0.1";
	public static final String ICON_RESOURCE_NAME = "call_controll";
	protected static final String UPDATED_RESOURCE_ICON_NAME = "objectStorage";

	private ResourceUIUtils() {
	}

	public static void defineResourceName(String resourceName) {

		WebElement resourceNameTextbox = GeneralUIUtils.getDriver().findElement(By.name("componentName"));
		resourceNameTextbox.clear();
		resourceNameTextbox.sendKeys(resourceName);
	}

	public static void defineResourceCategory(String category, String datatestsid) {

		GeneralUIUtils.getSelectList(category, datatestsid);
	}

	public static void importFileWithSendKeyBrowse(String FilePath, String FileName) throws Exception {
		WebElement browsebutton = GeneralUIUtils.getWebElementWaitForVisible("browseButton");
		browsebutton.sendKeys(FilePath + FileName);
	}

	public static void defineTagsList(ResourceReqDetails resource, String[] resourceTags) {
		List<String> taglist = new ArrayList<String>();
		;
		WebElement resourceTagsTextbox = GeneralUIUtils.getWebElementWaitForVisible("i-sdc-tag-input");
		for (String tag : resourceTags) {
			resourceTagsTextbox.clear();
			resourceTagsTextbox.sendKeys(tag);
			resourceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
		}
		resource.setTags(taglist);
	}

	public static void defineVendorRelease(String resourceVendorRelease) {

		WebElement resourceVendorReleaseTextbox = GeneralUIUtils.getWebElementWaitForVisible("vendorRelease");
		resourceVendorReleaseTextbox.clear();
		resourceVendorReleaseTextbox.sendKeys(resourceVendorRelease);
	}

	public static void defineProjectCode(String projectCode) {

		WebElement resourceNameTextbox = GeneralUIUtils.getDriver().findElement(By.name("projectCode"));
		resourceNameTextbox.clear();
		resourceNameTextbox.sendKeys(projectCode);
	}

	public static void clickButton(String selectButton) {

		WebElement clickButton = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//*[@data-tests-id='" + selectButton + "']"));
		clickButton.click();
	}

	public static WebElement Waitfunctionforbuttons(String element, int timeout) {
		WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), timeout);
		return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(element)));
	}

	// coded by teddy
	public static void fillGeneralInformationPage(ResourceReqDetails resource, User user) {
		try {
			resource.setContactId(user.getUserId());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFullName());
			defineResourceName(resource.getName());
			defineResourceCategory(resource.getCategories().get(0).getSubcategories().get(0).getName(),
					"selectGeneralCategory");
			GeneralUIUtils.defineDescription(resource.getDescription());
			GeneralUIUtils.defineVendorName(resource.getVendorName());
			defineVendorRelease(resource.getVendorRelease());
			defineTagsList(resource, new String[] { resource.getName() });
			GeneralUIUtils.defineUserId(resource.getCreatorUserId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ResourceReqDetails createResourceInUI(User user) {
		ResourceReqDetails defineResourceDetails = defineResourceDetails();
		GeneralUIUtils.clickAddComponent(DataTestIdEnum.Dashboard.BUTTON_ADD_VF);

		GeneralUIUtils.waitForLoader();
		fillGeneralInformationPage(defineResourceDetails, user);
		GeneralUIUtils.clickCreateButton();
		return defineResourceDetails;

	}

	@SuppressWarnings("deprecation")
	private static void openImportWithFile(String filePath, String fileName, Dashboard elementType) {
		Runnable openImportTask = () -> {
			GeneralUIUtils.moveToHTMLElementByDataTestId(Dashboard.IMPORT_AREA.getValue());
			WebElement imoprtVFButton = GeneralUIUtils.getWebElementByDataTestId(elementType.getValue());
			imoprtVFButton.sendKeys(filePath + fileName);
		};
		retryMethodOnException(openImportTask);

	}

	public static ResourceReqDetails importVfcInUI(User user, String filePath, String fileName) {
		ResourceReqDetails defineResourceDetails = defineResourceDetails();
		openImportWithFile(filePath, fileName, DataTestIdEnum.Dashboard.IMPORT_VFC_FILE);
		// Fill the general page fields.
		GeneralUIUtils.waitForLoader();
		fillGeneralInformationPage(defineResourceDetails, user);
		GeneralUIUtils.clickCreateButton();
		return defineResourceDetails;
	}

	/**
	 * Import VF
	 * 
	 * @param user
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public static ResourceReqDetails importVfInUI(User user, String filePath, String fileName) {
		ResourceReqDetails defineResourceDetails = defineResourceDetails();
		openImportWithFile(filePath, fileName, DataTestIdEnum.Dashboard.IMPORT_VF_FILE);
		// Fill the general page fields.
		GeneralUIUtils.waitForLoader();
		fillGeneralInformationPage(defineResourceDetails, user);

		GeneralUIUtils.clickSaveButton();

		return defineResourceDetails;
	}

	public static ResourceReqDetails importVfInUIWithoutCheckin(User user, String filePath, String fileName) {
		ResourceReqDetails defineResourceDetails = defineResourceDetails();
		openImportWithFile(filePath, fileName, DataTestIdEnum.Dashboard.IMPORT_VF_FILE);
		// Fill the general page fields.
		GeneralUIUtils.waitForLoader();
		fillGeneralInformationPage(defineResourceDetails, user);
		GeneralUIUtils.clickSaveButton();
		GeneralUIUtils.waitForLoader();
		// String okButtonId=DataTestIdEnum.ModalItems.OK.getValue();
		// ResourceUIUtils.clickButton(okButtonId);
		// ResourceUIUtils.Waitfunctionforbuttons("//*[@data-tests-id='"+okButtonId+"']",10);
		// ResourceUIUtils.clickButton(okButtonId);
		return defineResourceDetails;
	}

	public static ResourceReqDetails importVfFromOnBoardingModalWithoutCheckin(User user, String fileName) {
		ResourceReqDetails defineResourceDetails = defineResourceDetails();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.OPEN_MODAL_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(fileName).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.IMPORT_ICON.getValue()).click();

		// Fill the general page fields.
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.clickSaveButton();
		GeneralUIUtils.waitForLoader();
		return defineResourceDetails;
	}

	@SuppressWarnings("deprecation")
	public static void updateVfCsar(String filePath, String fileName) {
		retryMethodOnException(
				() -> GeneralUIUtils.getWebElementByDataTestId(DataTestIdEnum.GeneralSection.BROWSE_BUTTON.getValue())
						.sendKeys(filePath + fileName));
		GeneralUIUtils.clickSaveButton();
		GeneralUIUtils.waitForLoader();
	}

	public static void updateVfCsarFromOnBoarding() {
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.GeneralSection.BROWSE_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementsListWaitForVisible(DataTestIdEnum.OnBoardingTable.CSAR_ROW.getValue()).get(0)
				.click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.UPDATE_ICON.getValue()).click();
		GeneralUIUtils.clickSaveButton();
		GeneralUIUtils.waitForLoader();
	}

	public static ResourceReqDetails defineResourceDetails() {
		ResourceReqDetails resource = new ResourceReqDetails();
		resource = ElementFactory.getDefaultResource(NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS);
		resource.setVersion(INITIAL_VERSION);
		resource.setIcon(ICON_RESOURCE_NAME);
		resource.setResourceType(ResourceTypeEnum.VF.toString());
		resource.setName(getRandomComponentName(RESOURCE_NAME_PREFIX));

		return resource;
	}

	protected static String getRandomComponentName(String prefix) {
		return prefix + new Random().nextInt(10000);
	}

	public static ImmutablePair<String, String> getRIPosition(ResourceReqDetails createResourceInUI, User user) {
		GeneralUIUtils.sleep(1000);
		String responseAfterDrag = RestCDUtils.getResource(createResourceInUI).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		String xPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
				.get("posX");
		String yPosPostDrag = (String) ((JSONObject) ((JSONArray) jsonResource.get("componentInstances")).get(0))
				.get("posY");
		return new ImmutablePair<String, String>(xPosPostDrag, yPosPostDrag);

	}

	public static void fillinDeploymentArtifactFormAndClickDone(
			org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails details, String filePath) {
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ArtifactModal.LABEL.getValue())
				.sendKeys(details.getArtifactLabel());
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DESCRIPTION.getValue())
				.sendKeys(details.getDescription());
		GeneralUIUtils.getSelectList(details.getArtifactType(), DataTestIdEnum.ArtifactModal.TYPE.getValue());
		retryMethodOnException(() -> GeneralUIUtils
				.getWebElementByDataTestId(DataTestIdEnum.GeneralSection.BROWSE_BUTTON.getValue()).sendKeys(filePath));
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DONE.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

	/**
	 * Tests and Accept resource or service
	 * 
	 * @param createResourceInUI
	 */
	public static void testAndAcceptElement(ComponentReqDetails createResourceInUI) {
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(createResourceInUI.getName()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.START_TESTING.getValue())
				.click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.ACCEPT.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue())
				.sendKeys("resource " + createResourceInUI.getName() + " tested successfuly");
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.waitForElementToDisappear(DataTestIdEnum.ModalItems.OK.getValue());
	}

	/**
	 * Waits Until resource changed to requested lifeCycle State
	 * 
	 * @param createResourceInUI
	 * @param requestedLifeCycleState
	 * @return
	 */
	public static Resource waitForState(ResourceReqDetails createResourceInUI,
			LifecycleStateEnum requestedLifeCycleState) {
		Supplier<Resource> resourceGetter = () -> {
			String resourceString = RestCDUtils.getResource(createResourceInUI).getResponse();
			return ResponseParser.convertResourceResponseToJavaObject(resourceString);
		};
		Function<Resource, Boolean> verifier = res -> res.getLifecycleState() == requestedLifeCycleState;
		return FunctionalInterfaces.retryMethodOnResult(resourceGetter, verifier);

	}

}
