package org.onap.sdc.frontend.ci.tests.flow;

import com.aventstack.extentreports.Status;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;

public class AddNodeToCompositionFlow extends AbstractUiTestFlow {

    private final ComponentData parentComponent;
    private final ComponentData componentToAdd;
    private CompositionPage compositionPage;
    private ComponentInstance createdComponentInstance;

    public AddNodeToCompositionFlow(final WebDriver webDriver, final ComponentData parentComponent, final ComponentData componentToAdd) {
        super(webDriver);
        this.parentComponent = parentComponent;
        this.componentToAdd = componentToAdd;
    }

    @Override
    public Optional<? extends PageObject> run(final PageObject... pageObjects) {
        Objects.requireNonNull(parentComponent);
        Objects.requireNonNull(parentComponent.getComponentType());
        Objects.requireNonNull(componentToAdd);
        extendTest.log(Status.INFO, String.format("Adding Resource '%s' to VF/Service '%s'", componentToAdd.getName(), parentComponent.getName()));

        compositionPage = findParameter(pageObjects, CompositionPage.class);
        compositionPage.isLoaded();
        addNodeToComposition();
        final ComponentPage componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        compositionPage = componentPage.goToComposition();
        compositionPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "component-instance-created",
            String.format("Component instance '%s' of type '%s' created", createdComponentInstance.getName(), componentToAdd.getName()));
        return Optional.of(this.compositionPage);
    }

    private void addNodeToComposition() {
        switch (parentComponent.getComponentType()) {
            case SERVICE:
                createdComponentInstance = compositionPage
                    .addNodeToServiceCompositionUsingApi(parentComponent.getName(), parentComponent.getVersion(), componentToAdd.getName(),
                        componentToAdd.getVersion());
                break;
            case RESOURCE:
                createdComponentInstance = compositionPage
                    .addNodeToResourceCompositionUsingApi(parentComponent.getName(), parentComponent.getVersion(), componentToAdd.getName(),
                        componentToAdd.getVersion());
                break;
            default:
                throw new UnsupportedOperationException(
                    String.format("Add node in a %s not yet supported", parentComponent.getComponentType().getValue()));
        }
    }

    @Override
    public Optional<CompositionPage> getLandedPage() {
        return Optional.ofNullable(compositionPage);
    }

    public Optional<ComponentInstance> getCreatedComponentInstance() {
        return Optional.ofNullable(createdComponentInstance);
    }
}