package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.PropertyPopup;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CompositionPropertiesAttributesTab extends AbstractPageObject {

    private WebElement wrapperElement;

    public CompositionPropertiesAttributesTab(WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        wrapperElement = waitForElementVisibility(By.xpath(XpathSelector.PROPERTIES_TAB.xPath));
    }

    public void clickOnProperty(String propertyName) {
        final WebElement propElement = wrapperElement.findElement(By.xpath(XpathSelector.PROPERTY.formatXPath(propertyName)));
        propElement.click();
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        PROPERTIES_TAB("//properties-tab"),
        PROPERTY("//span[@data-tests-id='%s']");

        private final String xPath;

        public String formatXPath(Object value) {
            return String.format(xPath, value);
        }
    }
}
