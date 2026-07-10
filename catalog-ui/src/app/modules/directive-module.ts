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

import {NodesFactory} from "../models/graph/nodes/nodes-factory";
import {LinksFactory} from "../models/graph/graph-links/links-factory";
// *** NG2 Components (downgraded) *** //
import {downgradeComponent} from "@angular/upgrade/static";
import {MenuListNg2Component} from "../ng2/components/downgrade-wrappers/menu-list-ng2/menu-list-ng2.component";
import {TopNavComponent} from "../ng2/components/layout/top-nav/top-nav.component";
import {ZoneContainerComponent} from "../ng2/pages/composition/graph/canvas-zone/zone-container.component";
import {ZoneInstanceComponent} from "../ng2/pages/composition/graph/canvas-zone/zone-instance/zone-instance.component";
import {CompositionPanelComponent} from 'app/ng2/pages/composition/panel/composition-panel.component';
import {PropertiesAssignmentComponent} from "../ng2/pages/properties-assignment/properties-assignment.page.component";
import {SearchWithAutoCompleteComponent} from "../ng2/components/ui/search-with-autocomplete/search-with-autocomplete.component";
import {ServicePathSelectorComponent} from '../ng2/pages/composition/graph/service-path-selector/service-path-selector.component';
import {MultilineEllipsisComponent} from "../ng2/shared/multiline-ellipsis/multiline-ellipsis.component";
import {InterfaceOperationComponent} from '../ng2/pages/interface-operation/interface-operation.page.component';
import {PluginFrameComponent} from "../ng2/components/ui/plugin/plugin-frame.component";
import {TileComponent} from "../ng2/components/ui/tile/tile.component";
import {CompositionPageComponent} from "../ng2/pages/composition/composition-page.component";
import {CatalogComponent} from "../ng2/pages/catalog/catalog.component";
import {HomeComponent} from "../ng2/pages/home/home.component";
import {PluginContextViewPageComponent} from "../ng2/pages/plugins/plugin-context-view/plugin-context-view.page.component";
import {PluginTabViewPageComponent} from "../ng2/pages/plugins/plugin-tab-view/plugin-tab-view.page.component";
import {CompositionGraphComponent} from "../ng2/pages/composition/graph/composition-graph.component";
import {DeploymentPageComponent} from "../ng2/pages/workspace/deployment/deployment-page.component";
import {ActivityLogComponent} from "../ng2/pages/workspace/activity-log/activity-log.component";
import {ToscaArtifactPageComponent} from "../ng2/pages/workspace/tosca-artifacts/tosca-artifact-page.component";
import {InformationArtifactPageComponent} from "../ng2/pages/workspace/information-artifact/information-artifact-page.component";
import {AttributesComponent} from "../view-models/workspace/tabs/attributes/attributes.component";
import {DeploymentArtifactsPageComponent} from "../ng2/pages/workspace/deployment-artifacts/deployment-artifacts-page.component";
import {ReqAndCapabilitiesComponent} from "../ng2/pages/workspace/req-and-capabilities/req-and-capabilities.component";
import {DistributionComponent} from '../ng2/pages/workspace/disribution/distribution.component';
import {AttributesOutputsComponent} from "../ng2/pages/attributes-outputs/attributes-outputs.page.component";
import {InterfaceDefinitionComponent} from "../ng2/pages/interface-definition/interface-definition.page.component";
import {ToscaFunctionComponent} from '../ng2/pages/properties-assignment/tosca-function/tosca-function.component';
import {ConstraintsComponent} from '../ng2/pages/properties-assignment/constraints/constraints.component';
import {PropertyMetadataComponent} from '../ng2/pages/properties-assignment/property-metadata/property-metadata.component';
import {TypeWorkspaceComponent} from "../ng2/pages/type-workspace/type-workspace.component";
import {TypeWorkspaceGeneralComponent} from "../ng2/pages/type-workspace/type-workspace-general/type-workspace-general.component";
import {DeclareInputComponent} from "../ng2/pages/properties-assignment/declare-input/declare-input.component";
import {WorkspaceContainerComponent} from "../ng2/pages/workspace/workspace-container/workspace-container.component";
import {GeneralTabComponent} from "../ng2/pages/workspace/general-tab/general-tab.component";
import {ManagementWorkflowTabComponent} from "../ng2/pages/workspace/flow-editor/management-workflow-tab.component";
import {NetworkCallFlowTabComponent} from "../ng2/pages/workspace/flow-editor/network-call-flow-tab.component";
import {WorkspacePropertiesTabComponent} from "../ng2/pages/workspace/properties-tab/properties-tab.component";
import {AdminDashboardComponent} from "../ng2/pages/admin-dashboard/admin-dashboard.component";
import {Error403PageComponent} from "../ng2/pages/error-403/error-403.component";
import {OnboardVendorPageComponent} from "../ng2/pages/onboard-vendor/onboard-vendor.component";

let moduleName: string = 'Sdc.Directives';
let directiveModule: ng.IModule = angular.module(moduleName, []);

// ------------------------------------------- Composition & Deployment Graphs------------------------------------------//
// Util services for Graph
directiveModule.service('NodesFactory', NodesFactory);
directiveModule.service('LinksFactory', LinksFactory);


directiveModule.directive('menuListNg2', downgradeComponent({
  component: MenuListNg2Component,
  inputs: ['props']
}) as angular.IDirectiveFactory);

directiveModule.directive('topNav', downgradeComponent({
  component: TopNavComponent,
  inputs: ['version', 'menuModel', 'topLvlSelectedIndex', 'hideSearch', 'searchTerm', 'notificationIconCallback', 'unsavedChanges', 'unsavedChangesCallback'],
  outputs: ['searchTermChange']
}) as ng.IDirectiveFactory);

directiveModule.directive('ng2ZoneContainer', downgradeComponent({
  component: ZoneContainerComponent,
  inputs: ['title', 'count', 'type', 'visible', 'minimized'],
  outputs: ['minimize', 'backgroundClick']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ZoneInstance', downgradeComponent({
  component: ZoneInstanceComponent,
  inputs: ['zoneInstance', 'isActive', 'activeInstanceMode', 'defaultIconText', 'isViewOnly', 'hidden', 'forceSave'],
  outputs: ['modeChange', 'tagHandleClick', 'assignmentSaveStart', 'assignmentSaveComplete']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2CompositionPanel', downgradeComponent({
  component: CompositionPanelComponent,
  inputs: ['isViewOnly', 'isLoading', 'isCertified', 'selectedZoneInstanceId', 'selectedZoneInstanceType', 'selectedZoneInstanceName', 'topologyTemplate'],
}) as angular.IDirectiveFactory);

directiveModule.directive('propertiesAssignment', downgradeComponent({
  component: PropertiesAssignmentComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('compositionPage', downgradeComponent({
  component: CompositionPageComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('activityLog', downgradeComponent({
  component: ActivityLogComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('distribution', downgradeComponent({
  component: DistributionComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('attributes', downgradeComponent({
  component: AttributesComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('attributesOutputs', downgradeComponent({
  component: AttributesOutputsComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('reqAndCapabilities', downgradeComponent({
  component: ReqAndCapabilitiesComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2SearchWithAutocomplete', downgradeComponent({
  component: SearchWithAutoCompleteComponent,
  inputs: ['searchPlaceholder', 'searchBarClass', 'autoCompleteValues'],
  outputs: ['searchChanged', 'searchButtonClicked']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ServicePathSelector', downgradeComponent({
  component: ServicePathSelectorComponent,
  inputs: ['drawPath', 'deletePaths', 'service', 'selectedPathId'],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('interfaceOperation', downgradeComponent({
  component: InterfaceOperationComponent,
  inputs: ['component', 'readonly'],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('interfaceDefinition', downgradeComponent({
  component: InterfaceDefinitionComponent,
  inputs: ['component', 'readonly'],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2MultilineEllipsis', downgradeComponent({
  component: MultilineEllipsisComponent,
  inputs: ['lines', 'lineHeight', 'className'],
  outputs: ['hasEllipsisChanged']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2UiTile', downgradeComponent({
  component: TileComponent,
  inputs: ['component'],
  outputs: ['onTileClick']
}) as angular.IDirectiveFactory);

directiveModule.directive('pluginFrame', downgradeComponent({
  component: PluginFrameComponent,
  inputs: ['plugin', 'queryParams'],
  outputs: ['onLoadingDone']
}) as angular.IDirectiveFactory);

directiveModule.directive('catalogPage', downgradeComponent({
  component: CatalogComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('homePage', downgradeComponent({
  component: HomeComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('pluginContextView', downgradeComponent({
  component: PluginContextViewPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('pluginTabView', downgradeComponent({
  component: PluginTabViewPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('compositionGraph', downgradeComponent({
  component: CompositionGraphComponent,
  inputs: ['topologyTemplate', 'isViewOnly'],
  outputs: []
}) as angular.IDirectiveFactory);
directiveModule.directive('toscaArtifactPage', downgradeComponent({
  component: ToscaArtifactPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('deploymentPage', downgradeComponent({
  component: DeploymentPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('informationArtifactPage', downgradeComponent({
  component: InformationArtifactPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('deploymentArtifactPage', downgradeComponent({
  component: DeploymentArtifactsPageComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('toscaFunction', downgradeComponent({
  component: ToscaFunctionComponent,
  inputs: ['componentInstanceMap', 'property', 'customToscaFunctions'],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('appConstraints', downgradeComponent({
  component: ConstraintsComponent,
  inputs: ['propertyConstraints', 'isViewOnly', 'propertyType'],
  outputs: ['onConstraintChange']
}) as angular.IDirectiveFactory);

directiveModule.directive('appPropertyMetadata', downgradeComponent({
  component: PropertyMetadataComponent,
  inputs: ['propertyMetadata', 'isViewOnly'],
  outputs: ['onPropertyMetadataChange']
}) as angular.IDirectiveFactory);

directiveModule.directive('appTypeWorkspace', downgradeComponent({
  component: TypeWorkspaceComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('appTypeWorkspaceGeneral', downgradeComponent({
  component: TypeWorkspaceGeneralComponent,
  inputs: [],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('declareInput', downgradeComponent({
  component: DeclareInputComponent,
  inputs: ['property'],
  outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('workspaceContainer', downgradeComponent({
  component: WorkspaceContainerComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('generalTab', downgradeComponent({
  component: GeneralTabComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('managementWorkflowTab', downgradeComponent({
  component: ManagementWorkflowTabComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('networkCallFlowTab', downgradeComponent({
  component: NetworkCallFlowTabComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('workspacePropertiesTab', downgradeComponent({
  component: WorkspacePropertiesTabComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('adminDashboard', downgradeComponent({
  component: AdminDashboardComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('error403Page', downgradeComponent({
  component: Error403PageComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);

directiveModule.directive('onboardVendorPage', downgradeComponent({
  component: OnboardVendorPageComponent,
  propagateDigest: false
}) as angular.IDirectiveFactory);
