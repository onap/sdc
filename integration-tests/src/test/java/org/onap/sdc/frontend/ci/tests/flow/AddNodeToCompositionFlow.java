package org.onap.sdc.frontend.ci.tests.flow;

import com.aventstack.extentreports.Status;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.openqa.selenium.WebDriver;

public class AddNodeToCompositionFlow extends AbstractUiTestFlow {

    private final ResourceCreateData destination;
    private final ResourceCreateData resource;
    private CompositionPage compositionPage;
    private ResourceCreatePage resourceCreatePage;

    public AddNodeToCompositionFlow(final WebDriver webDriver, final ResourceCreateData destination, final ResourceCreateData resource) {
        super(webDriver);
        this.destination = destination;
        this.resource = resource;
    }

    @Override
    public Optional<? extends PageObject> run(final PageObject... pageObjects) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(resource);
        extendTest.log(Status.INFO, String.format("Adding Resource '%s' to VF/Service '%s'", resource.getName(), destination.getName()));

        resourceCreatePage = findParameter(pageObjects, ResourceCreatePage.class);
        resourceCreatePage.isLoaded();

        compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        compositionPage.addNodeToResourceCompositionUsingApi(destination.getName(), "0.1", resource.getName(), "1.0");
        compositionPage.goToGeneral();
        return Optional.of(compositionPage);
    }

    @Override
    public Optional<? extends PageObject> getLandedPage() {
        return Optional.ofNullable(resourceCreatePage);
    }
}
