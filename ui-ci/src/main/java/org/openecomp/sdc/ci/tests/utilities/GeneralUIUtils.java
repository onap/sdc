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

package org.openecomp.sdc.ci.tests.utilities;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.ci.tests.datatypes.CatalogFilterTitlesEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.DashboardCardEnum;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.Status;


public final class GeneralUIUtils {

	public static final String FILE_NAME = "Valid_tosca_Mycompute.yml";
	
	private static int timeOut=(int) (60*1.5);
	
//	public static void setTimeOut(int time) {
//		if (time>0) {
//			timeOut=time;	
//		}
//		else {
//			timeOut=timeOut;
//		}
//	}

	/**************** DRIVER ****************/
	
	public static WebDriver getDriver() {
		try{
			return DriverFactory.getDriver();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/****************************************/
	
	public static List<WebElement> getElemenetsFromTable(By by) {
		return getDriver().findElements(by);
	}

	public static File takeScreenshot(String screenshotFilename, String dir, String testName) throws IOException {
		if (screenshotFilename == null) {
			if (testName != null){
				screenshotFilename = testName;
			}
			else
			{
				screenshotFilename = UUID.randomUUID().toString();
			}
		}
		try {
			File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
			File filePath = new File(String.format("%s/%s.png", dir, screenshotFilename));
			new File(dir).mkdirs();
			FileUtils.copyFile(scrFile, filePath);
			return filePath;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static File takeScreenshot(String screenshotFilename, String dir) throws IOException{
		return takeScreenshot(screenshotFilename, dir, null);
	}


	public static void scrollDown() {
		try{
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
			GeneralUIUtils.waitForLoader();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void minimizeCatalogFilterByTitle(CatalogFilterTitlesEnum titlesEnum) {

		switch (titlesEnum) {
		case CATEGORIES:
			GeneralUIUtils.getWebElementByTestID(titlesEnum.getValue()).click();
			break;
		case STATUS:
			GeneralUIUtils.getWebElementByTestID(titlesEnum.getValue()).click();
			break;
		case TYPE:
			GeneralUIUtils.getWebElementByTestID(titlesEnum.getValue()).click();
			break;
		default:
			break;
		}
	
	}

	public static WebElement getWebElementByTestID(String dataTestId) {
		return getWebElementByTestID(dataTestId, timeOut);
	}
	
	public static WebElement getWebElementByTestID(String dataTestId, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}
	
	public static boolean isWebElementExistByTestId(String dataTestId) {
		if(getDriver().findElements(By.xpath("//*[@data-tests-id='" + dataTestId + "']")).size() == 0) {
			return false;
		}
		return true;
	}

	public static WebElement getInputElement(String dataTestId) {
		try{
			ultimateWait();
			return getDriver().findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
		}
		catch(Exception e){
			return null;
		}
	}

	public static List<WebElement> getInputElements(String dataTestId) {
		ultimateWait();
		return getDriver().findElements(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));

	}
	
	public static WebElement getWebElementBy(By by) {
		return getWebElementBy(by, timeOut);
	}
	
	public static WebElement getWebElementBy(By by, int timeOut) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	public static List<String> getWebElementListText(List<WebElement>elements) {
		List<String>Text=new ArrayList<>();
		for (WebElement webElement : elements) {
			Text.add(webElement.getText());
		}
		return Text;
	}
	
	
	public static List<WebElement> getWebElementsListBy(By by) {
		return getWebElementsListBy(by, timeOut);
	}
	
	public static List<WebElement> getWebElementsListBy(By by, int timeOut) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
	}
	
	public static List<WebElement> getWebElementsListByContainTestID(String dataTestId) {
		try{
			WebDriverWait wait = new WebDriverWait(getDriver(), 10);
			return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[contains(@data-tests-id, '"+dataTestId+"')]")));
		}
		catch(Exception e){
			return new ArrayList<WebElement>();
		}
	}
	
	public static List<WebElement> getWebElementsListByContainsClassName(String containedText) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[contains(@class, '"+containedText+"')]")));
	}
	
	public static WebElement getWebElementByContainsClassName(String containedText) {
		return getWebElementBy(By.xpath("//*[contains(@class, '"+containedText+"')]"));
	}
	
	public static WebElement getWebElementByClassName(String className) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
	}
	
	public static WebElement getWebElementByLinkText(String linkText) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@text='" + linkText + "']")));
	}
	
	
	public static List<WebElement> getWebElementsListByTestID(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}
	
	public static List<WebElement> getWebElementsListByClassName(String className) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className(className)));
	}
	
	


	public static Boolean isElementInvisibleByTestId(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(
				ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}
	
	public static Boolean isElementVisibleByTestId(String dataTestId) {
		try{
			WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
			if(wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath("//*[@data-tests-id='" + dataTestId + "']")))).isDisplayed()){
				return true;
			}else {
				return false;
			}
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static void clickOnElementByTestId(String dataTestId) {
		clickOnElementByTestIdWithoutWait(dataTestId);
		ultimateWait();
	}
	
	public static void clickOnElementByTestIdWithoutWait(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']"))).click();
	}
	
	public static void clickOnElementByTestId(String dataTestId, int customTimeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(), customTimeout);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-tests-id='" + dataTestId + "']"))).click();
	}

	public static WebElement waitForElementVisibilityByTestId(String dataTestId) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "']")));
	}
	
	public static Boolean waitForElementInVisibilityByTestId(String dataTestId) {
		return waitForElementInVisibilityByTestId(dataTestId, timeOut);
	}
	
	public static Boolean waitForElementInVisibilityByTestId(String dataTestId, int timeOut) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		boolean displayed = getDriver().findElements(By.xpath("//*[@data-tests-id='" + dataTestId + "']")).isEmpty();
		if (!displayed){
			Boolean until = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@data-tests-id='" + dataTestId + "'])")));
			ultimateWait();
			return until;
		}
		return false;
	}
	
	public static Boolean waitForElementInVisibilityByTestId(By by) {
		return waitForElementInVisibilityBy(by, timeOut);
	}
	
	
	public static Boolean waitForElementInVisibilityBy(By by, int timeOut) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		boolean displayed = getDriver().findElements(by).isEmpty();
		if (!displayed){
			Boolean until = wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
			sleep(1000);
			return until;
		}
		return false;
	}
	
	
	public static void setWebElementByTestId(String elemetID, String value) {
		WebElement resourceDescriptionTextbox = GeneralUIUtils.getWebElementByTestID(elemetID);
		resourceDescriptionTextbox.clear();
		resourceDescriptionTextbox.sendKeys(value);
		
	}
	
	public static WebElement hoverOnAreaByTestId(String areaId) {
		Actions actions = new Actions(getDriver());
		WebElement area = getWebElementByTestID(areaId);
		actions.moveToElement(area).perform();
		ultimateWait();
		return area;
	}
	
	public static WebElement hoverOnAreaByClassName(String className) {
		Actions actions = new Actions(getDriver());
		WebElement area = getWebElementByClassName(className);
		actions.moveToElement(area).perform();
		GeneralUIUtils.ultimateWait();
		return area;
	}
	
	public static void clickElementUsingActions(WebElement element){
		Actions actions = new Actions(getDriver());
		
		actions.moveToElement(element);
		actions.perform();
		
		actions.click();
		actions.perform();
		
		ultimateWait();
	}
	
	public static void waitForLoader() {
		waitForLoader(timeOut);
	}
	
	public static void waitForLoader(int timeOut) {
		sleep(500);
		waitForElementInVisibilityBy(By.className("tlv-loader"), timeOut);
	}
	
	public static void findComponentAndClick(String resourceName) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " in homepage");
		WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
		try{
			searchTextbox.clear();
			searchTextbox.sendKeys(resourceName);
			ultimateWait();
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't interact with search bar");
			e.printStackTrace();
		}
		
		
		try{
			SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the %s component from home screen", resourceName));
			clickOnElementByTestId(resourceName);
			GeneralUIUtils.ultimateWait();
			getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on component named " + resourceName);
			e.printStackTrace();
		}
	}
	
	
	public static String getComponentVersion(String componentName) {
		return GeneralUIUtils.getWebElementByTestID(componentName + "Version").getText();
	}
	
	public static void windowZoomOut() {
			final int zoomOutFactor = 3;
			for (int i = 0; i < zoomOutFactor; i++) {
				if(getDriver() instanceof FirefoxDriver) {
					getDriver().findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
				}
		}
	}
	
	public static void resetZoom(){
		getDriver().findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, "0"));
	}
	
	public static void windowZoomOutUltimate(){
		resetZoom();
		windowZoomOut();
//		JavascriptExecutor js = (JavascriptExecutor) driver;
//		js.executeScript("document.body.style.zoom='90%'");
	}
	
	public static void clickASDCLogo() {
		WebDriverWait wait = new WebDriverWait(getDriver(), 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("ASDC")));
		WebElement ClickASDCLogo = getDriver().findElement(By.linkText("ASDC"));
		ClickASDCLogo.click();
		GeneralUIUtils.waitForLoader();
	}
	
	public static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void moveToStep(DataTestIdEnum.StepsEnum stepName) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Going to %s page ", stepName.toString()));
		moveToStep(stepName.getValue());
	}

	public static void moveToStep(String dataTestId) {
		clickOnElementByTestId(dataTestId);
		ultimateWait();
	}
	
	
	public static Select getSelectList(String item, String datatestsid) {
		Select selectlist = new Select(getWebElementByTestID(datatestsid));
		if (item != null) {
			selectlist.selectByVisibleText(item);
		}
		return selectlist;
	}	
	
	public static List<WebElement> waitForElementsListVisibilityTestMethod(DashboardCardEnum dataTestId) {
		GeneralUIUtils.waitForLoader();
		return getDriver().findElements(By.xpath("//*[@data-tests-id='" + dataTestId.getValue() + "']"));
	}
	
    public static List<WebElement> getElementsByCSS(String cssString) throws InterruptedException {
		GeneralUIUtils.waitForLoader();
		List<WebElement> assets = getDriver().findElements(By.cssSelector(cssString));
		return assets;
	}
   
    public static WebElement getElementfromElementByCSS(WebElement parentElement, String cssString){
    	WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
    	GeneralUIUtils.waitForLoader();
    	return parentElement.findElement(By.cssSelector(cssString));
    }
    
    public static WebElement getElementfromElementByXPATH(WebElement parentElement, DashboardCardEnum dataTestId){
    	WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
    	GeneralUIUtils.waitForLoader();
    	return HighlightMyElement( parentElement.findElement(By.xpath("//*[@data-tests-id='" + dataTestId.getValue() + "']")));
    }
    
    public static WebElement HighlightMyElement(WebElement element) { 
	   JavascriptExecutor javascript = (JavascriptExecutor) getDriver();
	   javascript.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 4px solid yellow;");
	   return element;
    } 
    
    public static WebElement getSelectedElementFromDropDown(String dataTestId){
    	GeneralUIUtils.ultimateWait();;
    	WebElement selectedElement = new Select (getDriver().findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"))).getFirstSelectedOption();
    	return selectedElement;
    }
    
    
    public static void waitForPageLoadByReadyState() {
        new WebDriverWait(getDriver(), 30).until((ExpectedCondition<Boolean>) wd ->	
        	((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
    }
    

	public static boolean checkElementsCountInTable(int expectedElementsCount, Supplier<List<WebElement>> func) {
		int maxWaitingPeriodMS = 10 * 1000;
		int napPeriodMS = 100;
		int sumOfWaiting = 0;
		List<WebElement> elements = null;
		boolean isKeepWaiting = false;
		while (!isKeepWaiting) {
			elements = func.get();
			isKeepWaiting = (expectedElementsCount == elements.size());
			sleep(napPeriodMS);
			sumOfWaiting += napPeriodMS;
			if (sumOfWaiting > maxWaitingPeriodMS)
				return false;
		}
		return true;
	}
	
	public static String getActionDuration(Runnable func) throws Exception{
		long startTime = System.nanoTime();
		func.run();
		long estimateTime = System.nanoTime();
	    long duration = TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
	    String durationString = String.format("%02d:%02d", duration / 60, duration % 60);
	    return durationString;
	}
	
    public static WebElement clickOnAreaJS(String areaId) {
    	return clickOnAreaJS(areaId, timeOut);
    }
    
    
    public static WebElement clickOnAreaJS(String areaId, int timeout) {
    	try{
    		ultimateWait();
	        WebElement area = getWebElementByTestID(areaId);
	        JavascriptExecutor javascript = (JavascriptExecutor) getDriver();
	        //HighlightMyElement(area);
	        Object executeScript = javascript.executeScript("arguments[0].click();", area, "color: yellow; border: 4px solid yellow;");
	        waitForLoader(timeout);      
	        return area;
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
		return null;
    }

    
    
    public static WebElement clickOnAreaJS(WebElement areaId) throws InterruptedException {
        JavascriptExecutor javascript = (JavascriptExecutor) getDriver();
        //HighlightMyElement(area);
        javascript.executeScript("arguments[0].click();", areaId, "color: yellow; border: 4px solid yellow;");
        return areaId;
    }
    
    
    
    public static void clickSomewhereOnPage() {
    	getDriver().findElement(By.cssSelector(".asdc-app-title")).click();
	}
    
    public static void findComponentAndClickInCatalog(String resourceName) throws Exception {
    	// This method will find element by element name, don't use data-tests-id argument
		WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
		searchTextbox.clear();
		searchTextbox.sendKeys(resourceName);
		ultimateWait();
		clickOnElementByText(resourceName);
		ultimateWait();
	}
    
	public static void clickOnElementByText(String textInElement) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		HighlightMyElement(wait.until(
				ExpectedConditions.elementToBeClickable(findByText(textInElement)))).click();
	}
	
	public static void clickOnElementByText(String textInElement, int customTimeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(), customTimeout);
		HighlightMyElement(wait.until(
				ExpectedConditions.elementToBeClickable(findByText(textInElement)))).click();
	}
    
	public static void clickJSOnElementByText(String textInElement) throws Exception {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		clickOnAreaJS(wait.until(
				ExpectedConditions.elementToBeClickable(findByText(textInElement))));
	}
	
    public static void fluentWaitTestID(String dataTestId, String text) {
    	FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(getDriver())
				.withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(50, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class);

		fluentWait.until(ExpectedConditions.refreshed(
				ExpectedConditions.textToBePresentInElementValue(By.xpath("//*[@data-tests-id='" + dataTestId + "']"), text)));
    }    
    
    public static void regularWait(WebElement element, String text){
    	WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);

		wait.until(ExpectedConditions.textToBePresentInElementValue(element, text));
    }
    
    public static void waitForAngular(){
    	WebDriverWait wait = new WebDriverWait(getDriver(), 90, 100);
    	wait.until(AdditionalConditions.pageLoadWait());
    	wait.until(AdditionalConditions.angularHasFinishedProcessing());
    }
    
	public static Object getAllElementAttributes(WebElement element) {
		return ((JavascriptExecutor)getDriver()).executeScript("var s = []; var attrs = arguments[0].attributes; for (var l = 0; l < attrs.length; ++l) { var a = attrs[l]; s.push(a.name + ':' + a.value); } ; return s;", element);
	}
    
    public static boolean isElementReadOnly(WebElement element){
    	try {
    		HighlightMyElement(element).clear();
			return false;
		} catch (Exception e) {
			return true;
		} 	
    }
    
    public static boolean isElementReadOnly(String dataTestId){
    	return isElementReadOnly(
    			waitForElementVisibilityByTestId(dataTestId));
    }
    
    public static boolean isElementDisabled(WebElement element){
    	return HighlightMyElement(element).getAttribute("class").contains("view-mode") || 
    			element.getAttribute("class").contains("disabled");
    }
    
    public static boolean isElementDisabled(String dataTestId){
    	return isElementDisabled(
    			waitForElementVisibilityByTestId(dataTestId));
    }
    
    public static void ultimateWait(){
    	long startTime = System.nanoTime();                    

    	GeneralUIUtils.waitForLoader();
    	GeneralUIUtils.waitForBackLoader();
		GeneralUIUtils.waitForAngular();
		
		long estimateTime = System.nanoTime();
		long duration = TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
//		System.out.println("UltimateWait took: "+ duration);
		if(duration > timeOut){
			SetupCDTest.getExtendTest().log(Status.WARNING, String.format("Delays on page, %d seconds", duration));
		}
//		waitForUINotification();
    }
    
    public static WebElement makeElementVisibleWithJS(WebElement element){
    	String js = "arguments[0].style.height='auto'; arguments[0].style.visibility='visible';";
    	((JavascriptExecutor) getDriver()).executeScript(js, element);
    	return element;   	
    }
    
    public static WebElement unhideElement(WebElement element, String attributeValue){
    	String js = "arguments[0].setAttribute('class','" + attributeValue + "');";
    	((JavascriptExecutor) getDriver()).executeScript(js, element);
    	return element;   	
    }   
         
    public static WebElement findByText(String textInElement){
    	return getDriver().findElement(searchByTextContaining(textInElement));
    }
    public static By searchByTextContaining(String textInElement) {
		return By.xpath("//*[contains(text(),'" + textInElement + "')]");
	}
    
    
    public static boolean findAndWaitByText(String textInElement, int timeout){
    	try{
    		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
    		wait.until(ExpectedConditions.presenceOfElementLocated(searchByTextContaining(textInElement)));
    		return true;
    	}
    	catch(Exception e){
    		return false;
    	}
    }
    
    public static WebElement getClickableButtonBy(By by, int timout){
    	try{
    		WebDriverWait wait = new WebDriverWait(getDriver(), timout);
    		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
    		return element;
    	}
    	catch(Exception e){
    		return null;
    	}
    }
    
    
    
    public static WebElement getButtonWithText(String textInButton){
    	try{
    		return getDriver().findElement(By.xpath("//button[contains(text(),'" + textInButton + "')]"));
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }
    
    
    public static List<WebElement> getElementsByDataTestsIdStartWith(String startWithString){
    	ultimateWait();
    	return getDriver().findElements(By.xpath("//*[starts-with(@data-tests-id,'" + startWithString + "')]"));
    }
    
	public static void closeErrorMessage() {
		WebElement okWebElement = getButtonWithText("OK");
		if (okWebElement != null){
			okWebElement.click();
			ultimateWait();
		}
	}
    
    public static WebElement getElementByCSS(String cssString) throws InterruptedException {
		ultimateWait();
		return getDriver().findElement(By.cssSelector(cssString));
	}
    
    public static String getDataTestIdAttributeValue(WebElement element) {
		return element.getAttribute("data-tests-id");
	}
    
    public static String getTextContentAttributeValue(WebElement element) {
		return element.getAttribute("textContent");
	}
    
    public static WebElement getElementInsideElementByDataTestsId(WebElement element, String dataTestId) {
    	try{
    		return element.findElement(By.xpath("//*[@data-tests-id='" + dataTestId + "']"));
    	}
    	catch(Exception e){
    		return null;
    	}
	}
    
    public static void clickOnElementByCSS(String cssString) throws Exception {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeOut);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssString))).click();
		ultimateWait();
	}
	public static String getRandomComponentName(String prefix) {
		return prefix + GeneralUIUtils.randomNumber();
	}
	public static int randomNumber() {
		Random r = new Random();
		return r.nextInt(10000);
	}
	
	public static void waitForUINotification() {
		List<WebElement> notificationElements = getDriver().findElements(By.className("ui-notification"));
		if (!notificationElements.isEmpty()){
			notificationElements.forEach(WebElement::click);
		}
	}
	
	public static boolean checkForDisabledAttribute(String  dataTestId){
		Object elementAttributes = getAllElementAttributes(waitForElementVisibilityByTestId(dataTestId));
		return elementAttributes.toString().contains("disabled");
	}
	
	public static void dragAndDropElementByY(WebElement area, int yOffset) {
		Actions actions = new Actions(getDriver());
		actions.dragAndDropBy(area, 10, yOffset).perform();
		ultimateWait();
	}
	
	public static void waitForBackLoader() {
		waitForBackLoader(timeOut);
	}
	
	public static void waitForBackLoader(int timeOut) {
		sleep(100);
		waitForElementInVisibilityBy(By.className("tlv-loader-back"), timeOut);
	}
	
	public static void addStringtoClipboard(String text){
		StringSelection selection = new StringSelection(text);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}
	
	public static boolean checkForDisabledAttributeInHiddenElement(String  cssString){
		Object elementAttributes = getAllElementAttributes(getDriver().findElement(By.cssSelector(cssString)));
		return elementAttributes.toString().contains("disabled");
	}
    
}
