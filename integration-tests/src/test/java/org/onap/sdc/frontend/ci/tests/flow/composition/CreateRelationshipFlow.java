/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.flow.composition;

import com.aventstack.extentreports.Status;
import java.util.Optional;
import org.onap.sdc.frontend.ci.tests.datatypes.composition.RelationshipInformation;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.flow.AbstractUiTestFlow;
import org.onap.sdc.frontend.ci.tests.pages.PageObject;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.RelationshipWizardComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.RelationshipWizardRequirementCapabilityComponent;
import org.openqa.selenium.WebDriver;

/**
 * Creates a relationship between two nodes/component instances
 */
public class CreateRelationshipFlow extends AbstractUiTestFlow {

    private final RelationshipInformation relationshipInformation;
    private CompositionPage compositionPage;

    public CreateRelationshipFlow(final WebDriver webDriver, final RelationshipInformation relationshipInformation) {
        super(webDriver);
        this.relationshipInformation = relationshipInformation;
    }

    @Override
    public Optional<? extends PageObject> run(final PageObject... pageObjects) {
        compositionPage = findParameter(pageObjects, CompositionPage.class);
        compositionPage.isLoaded();
        final RelationshipWizardComponent relationshipWizardComponent = compositionPage
            .createLink(relationshipInformation.getFromNode(), relationshipInformation.getToNode());
        final RelationshipWizardRequirementCapabilityComponent requirementCapabilityComponent =
            new RelationshipWizardRequirementCapabilityComponent(webDriver);
        requirementCapabilityComponent.isLoaded();
        requirementCapabilityComponent.selectRequirementOrCapability(relationshipInformation.getFromCapability());
        ExtentTestActions.takeScreenshot(Status.INFO, "capability-selected",
            String.format("Selected capability '%s'", relationshipInformation.getFromCapability()));
        relationshipWizardComponent.clickOnNext();
        requirementCapabilityComponent.isLoaded();
        requirementCapabilityComponent.selectRequirementOrCapability(relationshipInformation.getToRequirement());
        ExtentTestActions.takeScreenshot(Status.INFO, "requirement-selected",
            String.format("Selected requirement '%s'", relationshipInformation.getToRequirement()));
        relationshipWizardComponent.clickOnNext();
        relationshipWizardComponent.clickOnNext();
        relationshipWizardComponent.clickOnFinish();
        compositionPage.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "relationship-created",
            String.format("Relationship from '%s' to '%s' created", relationshipInformation.getFromNode(), relationshipInformation.getToNode()));
        return Optional.of(compositionPage);
    }

    @Override
    public Optional<CompositionPage> getLandedPage() {
        return Optional.of(compositionPage);
    }
}
