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

package org.onap.sdc.frontend.ci.tests.pages.component.workspace;

import org.onap.sdc.frontend.ci.tests.pages.AbstractPageObject;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceWorkspaceTopBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;

/**
 * Represents the Component (Service, VF, VFC, etc.) Composition Page
 */
public class CompositionPage extends AbstractPageObject {

    private final ResourceWorkspaceTopBarComponent resourceWorkspaceTopBarComponent;
    private final TopNavComponent topNavComponent;
    private final CompositionElementsComponent compositionElementsComponent;
    private final CompositionCanvasComponent compositionCanvasComponent;
    private final CompositionDetailSideBarComponent compositionDetailSideBarComponent;

    public CompositionPage(final WebDriver webDriver) {
        super(webDriver);
        topNavComponent = new TopNavComponent(webDriver);
        resourceWorkspaceTopBarComponent = new ResourceWorkspaceTopBarComponent(webDriver);
        compositionElementsComponent = new CompositionElementsComponent(webDriver);
        compositionCanvasComponent = new CompositionCanvasComponent(webDriver);
        compositionDetailSideBarComponent = new CompositionDetailSideBarComponent(webDriver);
    }

    @Override
    public void isLoaded() {
        compositionElementsComponent.isLoaded();
        resourceWorkspaceTopBarComponent.isLoaded();
        topNavComponent.isLoaded();
        compositionCanvasComponent.isLoaded();
        compositionDetailSideBarComponent.isLoaded();
    }

    public ComponentInstance addNodeToServiceCompositionUsingApi(final String serviceName, final String serviceVersion, final String resourceName,
                                                          final String resourceVersion) {
        return compositionCanvasComponent.createNodeOnServiceCanvas(serviceName, serviceVersion, resourceName, resourceVersion);
    }

    public ComponentInstance addNodeToResourceCompositionUsingApi(final String serviceName, final String serviceVersion, final String resourceName,
                                                          final String resourceVersion) {
        return compositionCanvasComponent.createNodeOnResourceCanvas(serviceName, serviceVersion, resourceName, resourceVersion);
    }

    public RelationshipWizardComponent createLink(final String fromNodeName, final String toNodeName) {
        return compositionCanvasComponent.createLink(fromNodeName, toNodeName);
    }

    /**
     * Select a node in the canvas
     *
     * @param nodeName the node name to select
     */
    public void selectNode(final String nodeName) {
        compositionCanvasComponent.selectNode(nodeName);
        compositionDetailSideBarComponent.checkComponentIsSelected(nodeName);
    }

    public ComponentPage goToGeneral() {
        topNavComponent.clickOnBreadCrumb(1);
        return new ComponentPage(webDriver);
    }

    public ServiceComponentPage goToServiceGeneral() {
        topNavComponent.clickOnBreadCrumb(1);
        return new ServiceComponentPage(webDriver);
    }

    /**
     * Get the composition page detail sidebar component
     *
     * @return the composition detail sideBar component
     */
    public CompositionDetailSideBarComponent getDetailSideBar() {
        return compositionDetailSideBarComponent;
    }

}
