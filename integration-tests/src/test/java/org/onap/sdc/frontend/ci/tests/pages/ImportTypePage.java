package org.onap.sdc.frontend.ci.tests.pages;

import org.openqa.selenium.WebDriver;

public class ImportTypePage extends AbstractPageObject {

    private final TopNavComponent topNavComponent;
    private final ResourceWorkspaceTopBarComponent workspaceTopBarComponent;

    public ImportTypePage(final WebDriver webDriver) {
        super(webDriver);
        topNavComponent = new TopNavComponent(webDriver);
        workspaceTopBarComponent = new ResourceWorkspaceTopBarComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        topNavComponent.isLoaded();
//        workspaceTopBarComponent.isLoaded();
    }

    public void clickOnCreate() {
        workspaceTopBarComponent.clickOnCreate();
    }
}
