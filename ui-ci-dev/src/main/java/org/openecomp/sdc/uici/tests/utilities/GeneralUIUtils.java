package org.openecomp.sdc.uici.tests.utilities;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnException;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnResult;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.swallowException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.Dashboard;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public final class GeneralUIUtils {

	private static final int DEFAULT_WAIT_TIME_IN_SECONDS = 10;
	/**************** DRIVERS ****************/
	private static WebDriver driver;

	private GeneralUIUtils() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Finding a component in the home screen by name and clicks on it
	 * Uses the search 
	 * 
	 * @param componentName
	 * @throws Exception
	 */
	public static void findComponentAndClick(String componentName) throws Exception {
		getWebElementWaitForVisible("main-menu-input-search").sendKeys(componentName);
		try {
			getWebElementWaitForClickable(componentName).click();
			GeneralUIUtils.waitForLoader();
			getWebElementWaitForVisible("formlifecyclestate");
		} catch (Exception e) {
			String msg = String.format("DID NOT FIND A COMPONENT NAMED %s", componentName);
			System.out.println(msg);
			Assert.fail(msg);
		}
	}

	public static WebElement getWebElementWaitForVisible(String dataTestId) {
		return getWebElementWaitForVisible(dataTestId, DEFAULT_WAIT_TIME_IN_SECONDS);
	}

	public static WebElement getWebElementWaitForVisible(String dataTestId, int time) {
		WebDriverWait wait = new WebDriverWait(getDriver(), time);
		ExpectedCondition<WebElement> visibilityOfElementLocated = ExpectedConditions
				.visibilityOfElementLocated(builDataTestIdLocator(dataTestId));
		WebElement webElement = wait.until(visibilityOfElementLocated);
		return webElement;
	}

	public static WebElement getWebElementWaitForClickable(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), DEFAULT_WAIT_TIME_IN_SECONDS);
		ExpectedCondition<WebElement> condition = ExpectedConditions
				.elementToBeClickable(builDataTestIdLocator(dataTestId));
		WebElement webElement = wait.until(condition);
		return webElement;
	}

	private static By builDataTestIdLocator(String dataTestId) {
		return By.xpath("//*[@data-tests-id='" + dataTestId + "']");

	}

	/**
	 * Returns A list of Web Elements When they are all visible
	 * 
	 * @param dataTestId
	 * @return
	 */
	public static List<WebElement> getWebElementsListWaitForVisible(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), DEFAULT_WAIT_TIME_IN_SECONDS);
		ExpectedCondition<List<WebElement>> visibilityOfAllElementsLocatedBy = ExpectedConditions
				.visibilityOfAllElementsLocatedBy(builDataTestIdLocator(dataTestId));
		return wait.until(visibilityOfAllElementsLocatedBy);
	}

	/**
	 * @deprecated Do not use. use {@link #getWebElementWaitForVisible(String)}
	 * @param dataTestId
	 * @return
	 */
	public static WebElement getWebElementByDataTestId(String dataTestId) {
		return driver.findElement(builDataTestIdLocator(dataTestId));
	}

	/**
	 * Checks if element is present with given dataTestsId
	 * 
	 * @param dataTestId
	 * @return
	 */
	public static boolean isElementPresent(String dataTestId) {
		final boolean isPresent = !driver.findElements(builDataTestIdLocator(dataTestId)).isEmpty();
		return isPresent;
	}

	public static void clickOnCreateEntityFromDashboard(String buttonId) {
		Supplier<WebElement> addVfButtonSipplier = () -> {
			// TODO ui-ci replace with data-test-id
			GeneralUIUtils.moveToHTMLElementByClassName("w-sdc-dashboard-card-new");
			return GeneralUIUtils.getWebElementByDataTestId(buttonId);
		};
		WebElement addVfButton = FunctionalInterfaces.retryMethodOnException(addVfButtonSipplier);
		addVfButton.click();
	}

	// this function located select list by the data-test-id value and the item
	// to be selected..
	public static Select getSelectList(String item, String dataTestId) {
		Select selectlist = new Select(driver.findElement(builDataTestIdLocator(dataTestId)));
		if (item != null) {
			selectlist.selectByVisibleText(item);
		}
		return selectlist;
	}

	// Define description area .
	public static String defineDescription(String descriptionText) {

		WebElement resourceDescriptionTextbox = GeneralUIUtils.getWebElementWaitForVisible("description");
		resourceDescriptionTextbox.clear();
		resourceDescriptionTextbox.sendKeys(descriptionText);

		return descriptionText;
	}

	/**
	 * Clicks on the create button waits for the create to finish and the check
	 * in button to appear
	 */
	public static void clickCreateButton() {
		GeneralUIUtils.waitForLoader();
		getWebElementWaitForClickable(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue()).click();
		GeneralUIUtils.waitForLoader();
		WebElement successNotification = driver.findElement(By.className("ui-notification"));
		if (successNotification != null) {
			successNotification.click();
		}
		getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.CHECK_IN.getValue());
	}

	public static void clickSaveButton() {
		WebElement createButton = getWebElementWaitForClickable(DataTestIdEnum.LifeCyleChangeButtons.CREATE.getValue());
		createButton.click();
	}

	public static void closeNotificatin() {
		WebElement notification = driver.findElement(By.className("ui-notification"));
		if (notification != null) {
			notification.click();
		}
	}
	public static void checkIn() {
		waitForLoader();
		getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.CHECK_IN.getValue()).click();
		getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue()).sendKeys("Check in !");
		getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		waitForLoader();
	}

	public static void moveToStep(CreateAndUpdateStepsEnum Stepname) {
		waitForLoader();
		getWebElementWaitForClickable(Stepname.getValue()).click();
		waitForLoader();
	}

	public static void sleep(int duration) {
		swallowException(() -> Thread.sleep(duration));
	}

	public static WebDriver getDriver() {
		return driver;
	}

	public static void initDriver() {
		try {
			System.out.println("opening browser");
			WebDriver webDriver;
			boolean remoteTesting = SetupCDTest.config.isRemoteTesting();
			if (!remoteTesting) {
				webDriver = new FirefoxDriver();
			} else {
				String remoteEnvIP = SetupCDTest.config.getRemoteTestingMachineIP();
				String remoteEnvPort = SetupCDTest.config.getRemoteTestingMachinePort();
				DesiredCapabilities cap = new DesiredCapabilities();
				cap = DesiredCapabilities.firefox();
				cap.setPlatform(Platform.WINDOWS);
				cap.setBrowserName("firefox");

				String remoteNodeUrl = String.format(SetupCDTest.SELENIUM_NODE_URL, remoteEnvIP, remoteEnvPort);
				webDriver = new RemoteWebDriver(new URL(remoteNodeUrl), cap);

			}
			driver = webDriver;

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * waits until either loader finishes or 10 seconds has passed.<br>
	 * If 10 seconds has passed and loader didn't finish throws
	 * LoaderStuckException.<br>
	 */
	public static void waitForLoader() {
		waitForLoader(10);
	}

	/**
	 * waits until either loader finishes or maxWaitTimeInSeconds has
	 * passed.<br>
	 * If maxWaitTimeInSeconds has passed and loader didn't finish throws
	 * LoaderStuckException.<br>
	 * 
	 * @param maxWaitTimeInSeconds
	 */
	public static void waitForLoader(int maxWaitTimeInSeconds) {
		long maxWaitTimeMS = maxWaitTimeInSeconds * 1000L;
		Boolean loaderIsRunning = retryMethodOnResult(
				() -> isElementPresent(DataTestIdEnum.GeneralSection.LOADER.getValue()),
				isLoaderPresent -> !isLoaderPresent, maxWaitTimeMS, 50);
		if (loaderIsRunning) {
			throw new LoaderStuckException(
					"UI Loader is stuck, max wait time of " + maxWaitTimeInSeconds + " seconds has passed.");
		}

	}

	private static class LoaderStuckException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private LoaderStuckException(String message) {
			super(message);
		}
	}

	/**
	 * Move to HTML element by class name. When moving to the HTML element, it
	 * will raise hover event.
	 * 
	 * @param className
	 */
	public static void moveToHTMLElementByClassName(String className) {
		Actions actions = new Actions(getDriver());
		final WebElement createButtonsArea = getDriver().findElement(By.className(className));
		actions.moveToElement(createButtonsArea).perform();
	}

	/**
	 * Move to HTML element by element id. When moving to the HTML element, it
	 * will raise hover event.
	 * 
	 * @param className
	 */
	public static void moveToHTMLElementByDataTestId(String dataTestId) {
		Actions actions = new Actions(getDriver());
		final WebElement createButtonsArea = getWebElementByDataTestId(dataTestId);
		actions.moveToElement(createButtonsArea).perform();
	}

	public static void defineVendorName(String resourceVendorName) {
		// TODO ui-ci replace with Enum
		WebElement resourceVendorNameTextbox = getWebElementWaitForVisible("vendorName");
		resourceVendorNameTextbox.clear();
		resourceVendorNameTextbox.sendKeys(resourceVendorName);
	}

	public static String defineUserId(String userId) {
		// TODO ui-ci replace with Enum
		WebElement resourceTagsTextbox = getWebElementWaitForVisible("userId");
		resourceTagsTextbox.clear();
		resourceTagsTextbox.sendKeys(userId);
		return userId;
	}

	public static void clickAddComponent(Dashboard componentType) {
		Runnable clickAddTask = () -> {
			// TODO ui-ci replace with data-test-id
			moveToHTMLElementByClassName("w-sdc-dashboard-card-new");
			WebElement addVfButton = getWebElementByDataTestId(componentType.getValue());
			addVfButton.click();
		};
		retryMethodOnException(clickAddTask);
	}

	/**
	 * This method perform submit for testing process for existing service or
	 * resource.<br>
	 * It assumes it is activated when in the resource screen and the Submit For
	 * Testing button is available.
	 * 
	 * @param componentNameForMessage
	 *            TODO
	 */
	public static void submitForTestingElement(String componentNameForMessage) {
		waitForLoader();
		getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.SUBMIT_FOR_TESTING.getValue()).click();
		waitForLoader();
		getWebElementWaitForVisible(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue())
				.sendKeys("Submit for testing for " + componentNameForMessage);
		waitForLoader();
		getWebElementWaitForClickable(DataTestIdEnum.ModalItems.OK.getValue()).click();
		waitForLoader();
		waitForElementToDisappear(DataTestIdEnum.ModalItems.OK.getValue());

	}

	/**
	 * Waits Until elements disappears or until 10 seconds pass
	 * 
	 * @param dataTestId
	 */
	public static void waitForElementToDisappear(String dataTestId) {
		Supplier<Boolean> elementPresenseChecker = () -> GeneralUIUtils.isElementPresent(dataTestId);
		Function<Boolean, Boolean> verifier = isElementPresent -> !isElementPresent;
		FunctionalInterfaces.retryMethodOnResult(elementPresenseChecker, verifier);

	}

}