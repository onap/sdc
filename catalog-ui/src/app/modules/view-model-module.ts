/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia. All rights reserved.
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

import {AddCategoryModalViewModel} from "../view-models/admin-dashboard/add-category-modal/add-category-modal-view-model";
import {DashboardViewModel} from "../view-models/dashboard/dashboard-view-model";
import {WorkspaceViewModel} from "../view-models/workspace/workspace-view-model";
import {CompositionViewModel} from "../view-models/workspace/tabs/composition/composition-view-model";
import {DetailsViewModel} from "../view-models/workspace/tabs/composition/tabs/details/details-view-model";
import {ResourceArtifactsViewModel} from "../view-models/workspace/tabs/composition/tabs/artifacts/artifacts-view-model";
import {ServiceConsumptionViewModel} from "../view-models/workspace/tabs/composition/tabs/service-consumption/service-consumption-view-model";
import {ServiceDependenciesViewModel} from "../view-models/workspace/tabs/composition/tabs/service-dependencies/service-dependencies-view-model";
import {PropertyFormBaseView} from "../view-models/forms/property-forms/base-property-form/property-form-base-model";
import {PropertyFormViewModel} from "../view-models/forms/property-forms/component-property-form/property-form-view-model";
import {ModulePropertyView} from "../view-models/forms/property-forms/module-property-modal/module-property-model";
import {ArtifactResourceFormViewModel} from "../view-models/forms/artifact-form/artifact-form-view-model";
import {SelectDataTypeViewModel} from "../view-models/forms/property-forms/select-datatype-modal/select-datatype-modal-view-model";
import {AttributeFormViewModel} from "../view-models/forms/attribute-form/attribute-from-view-model";
import {ResourcePropertiesViewModel} from "../view-models/workspace/tabs/composition/tabs/properties-and-attributes/properties-view-model";
import {CatalogViewModel} from "../view-models/catalog/catalog-view-model";
import {OnboardVendorViewModel} from "../view-models/onboard-vendor/onboard-vendor-view-model";
import {DistributionViewModel} from "../view-models/workspace/tabs/distribution/distribution-view-model";
import {SupportViewModel} from "../view-models/support/support-view-model";
import {ConfirmationModalViewModel} from "../view-models/modals/confirmation-modal/confirmation-modal-view-model";
import {EmailModalViewModel} from "../view-models/modals/email-modal/email-modal-view-model";
import {MessageModalViewModel} from "../view-models/modals/message-modal/message-base-modal-model";
import {ServerMessageModalViewModel} from "../view-models/modals/message-modal/message-server-modal/server-message-modal-view-model";
import {ClientMessageModalViewModel} from "../view-models/modals/message-modal/message-client-modal/client-message-modal-view-model";
import {ErrorViewModel} from "../view-models/modals/error-modal/error-view-model";
import {RelationsViewModel} from "../view-models/workspace/tabs/composition/tabs/relations/relations-view-model";
import {ResourceInstanceNameViewModel} from "../view-models/forms/resource-instance-name-form/resource-instance-name-model";
import {WelcomeViewModel} from "../view-models/welcome/welcome-view";
import {PreLoadingViewModel} from "../view-models/preloading/preloading-view";
import {TutorialEndViewModel} from "../view-models/tutorial-end/tutorial-end";
import {AdminDashboardViewModel} from "../view-models/admin-dashboard/admin-dashboard-view-model";
import {EnvParametersFormViewModel} from "../view-models/forms/env-parameters-form/env-parameters-form";
import {StructureViewModel} from "../view-models/workspace/tabs/composition/tabs/structure/structure-view";
import {UserManagementViewModel} from "../view-models/admin-dashboard/user-management/user-management-view-model";
import {CategoryManagementViewModel} from "../view-models/admin-dashboard/category-management/category-management-view-model";

import {OnboardingModalViewModel} from "../view-models/modals/onboarding-modal/onboarding-modal-view-model";
import {DistributionStatusModalViewModel} from "../view-models/workspace/tabs/distribution/disribution-status-modal/disribution-status-modal-view-model";
import {DcaeAppViewModel} from "../view-models/dcae-app/dcae-app-view-model";
import {GeneralViewModel} from "../view-models/workspace/tabs/general/general-view-model";
import {IconsModalViewModel} from "../view-models/modals/icons-modal/icons-modal-view";
import {DeploymentArtifactsViewModel} from "../view-models/workspace/tabs/deployment-artifacts/deployment-artifacts-view-model";
import {InformationArtifactsViewModel} from "../view-models/workspace/tabs/information-artifacts/information-artifacts-view-model";
import {ToscaArtifactsViewModel} from "../view-models/workspace/tabs/tosca-artifacts/tosca-artifacts-view-model";
import {PropertiesViewModel} from "../view-models/workspace/tabs/properties/properties-view-model";
import {AttributesViewModel} from "../view-models/workspace/tabs/attributes/attributes-view-model";
import {ActivityLogViewModel} from "../view-models/workspace/tabs/activity-log/activity-log";
import {ManagementWorkflowViewModel} from "../view-models/workspace/tabs/management-workflow/management-workflow-view-model";
import {InterfaceOperationViewModel} from "../view-models/workspace/tabs/interface-operation/interface-operation-view-model";
import {NetworkCallFlowViewModel} from "../view-models/workspace/tabs/network-call-flow/network-call-flow-view-model";
import {DeploymentViewModel} from "../view-models/workspace/tabs/deployment/deployment-view-model";
import {ReqAndCapabilitiesViewModel} from "../view-models/workspace/tabs/req-and-capabilities/req-and-capabilities-view-model";
import {InputFormViewModel} from "../view-models/forms/input-form/input-form-view-modal";
import {HierarchyViewModel} from "../view-models/tabs/hierarchy/hierarchy-view-model";
import {downgradeComponent} from "@angular/upgrade/static";
import {ConformanceLevelModalViewModel} from "../view-models/modals/conformance-level-modal/conformance-level-modal-view-model";
import {PluginsTabViewModel} from "../view-models/plugins/plugins-tab-view-model";
import {PluginsContextViewModel} from "../view-models/workspace/tabs/plugins/plugins-context-view-model";
let moduleName:string = 'Sdc.ViewModels';
let viewModelModule:ng.IModule = angular.module(moduleName, []);

viewModelModule
  .controller(moduleName + '.DashboardViewModel', DashboardViewModel)

  .controller(moduleName + '.DetailsViewModel', DetailsViewModel)
  .controller(moduleName + '.ResourceArtifactsViewModel', ResourceArtifactsViewModel)
  .controller(moduleName + '.ServiceConsumptionViewModel', ServiceConsumptionViewModel)
  .controller(moduleName + '.ServiceDependenciesViewModel', ServiceDependenciesViewModel)
  .controller(moduleName + '.PropertyFormBaseView', PropertyFormBaseView)
  .controller(moduleName + '.PropertyFormViewModel', PropertyFormViewModel)
  .controller(moduleName + '.ModulePropertyView', ModulePropertyView)
  .controller(moduleName + '.SelectDataTypeViewModel', SelectDataTypeViewModel)
  .controller(moduleName + '.ArtifactResourceFormViewModel', ArtifactResourceFormViewModel)
  .controller(moduleName + '.AttributeFormViewModel', AttributeFormViewModel)
  .controller(moduleName + '.ResourcePropertiesViewModel', ResourcePropertiesViewModel)
  .controller(moduleName + '.CatalogViewModel', CatalogViewModel)
  .controller(moduleName + '.OnboardVendorViewModel', OnboardVendorViewModel)
  .controller(moduleName + '.DistributionViewModel', DistributionViewModel)
  .controller(moduleName + '.SupportViewModel', SupportViewModel)
  .controller(moduleName + '.ConfirmationModalViewModel', ConfirmationModalViewModel)
  .controller(moduleName + '.EmailModalViewModel', EmailModalViewModel)
  .controller(moduleName + '.MessageModalViewModel', MessageModalViewModel)
  .controller(moduleName + '.ServerMessageModalViewModel', ServerMessageModalViewModel)
  .controller(moduleName + '.ClientMessageModalViewModel', ClientMessageModalViewModel)
  .controller(moduleName + '.ErrorViewModel', ErrorViewModel)
  .controller(moduleName + '.RelationsViewModel', RelationsViewModel)
  .controller(moduleName + '.ResourceInstanceNameViewModel', ResourceInstanceNameViewModel)
  .controller(moduleName + '.WelcomeViewModel', WelcomeViewModel)
  .controller(moduleName + '.PreLoadingViewModel', PreLoadingViewModel)
  .controller(moduleName + '.TutorialEndViewModel', TutorialEndViewModel)
  .controller(moduleName + '.AdminDashboardViewModel', AdminDashboardViewModel)
  .controller(moduleName + '.EnvParametersFormViewModel', EnvParametersFormViewModel)
  .controller(moduleName + '.StructureViewModel', StructureViewModel)
  .controller(moduleName + '.AddCategoryModalViewModel', AddCategoryModalViewModel)
  .controller(moduleName + '.UserManagementViewModel', UserManagementViewModel)
  .controller(moduleName + '.CategoryManagementViewModel', CategoryManagementViewModel)
  .controller(moduleName + '.OnboardingModalViewModel', OnboardingModalViewModel)
  .controller(moduleName + '.IconsModalViewModel', IconsModalViewModel)
  .controller(moduleName + '.DistributionStatusModalViewModel', DistributionStatusModalViewModel)
  .controller(moduleName + '.DcaeAppViewModel', DcaeAppViewModel)
  //
  // //NEW
  .controller(moduleName + '.WorkspaceViewModel', WorkspaceViewModel)
  .controller(moduleName + '.ConformanceLevelModalViewModel', ConformanceLevelModalViewModel)
  .controller(moduleName + '.CompositionViewModel', CompositionViewModel)
  .controller(moduleName + '.GeneralViewModel', GeneralViewModel)
  .controller(moduleName + '.DeploymentArtifactsViewModel', DeploymentArtifactsViewModel)
  .controller(moduleName + '.InformationArtifactsViewModel', InformationArtifactsViewModel)
  .controller(moduleName + '.ToscaArtifactsViewModel', ToscaArtifactsViewModel)
  .controller(moduleName + '.PropertiesViewModel', PropertiesViewModel)
  .controller(moduleName + '.AttributesViewModel', AttributesViewModel)
  .controller(moduleName + '.ActivityLogViewModel', ActivityLogViewModel)
  .controller(moduleName + '.ManagementWorkflowViewModel', ManagementWorkflowViewModel)
  .controller(moduleName + '.InterfaceOperationViewModel', InterfaceOperationViewModel)
  .controller(moduleName + '.NetworkCallFlowViewModel', NetworkCallFlowViewModel)
  .controller(moduleName + '.DeploymentViewModel', DeploymentViewModel)
  .controller(moduleName + '.ReqAndCapabilitiesViewModel', ReqAndCapabilitiesViewModel)
  .controller(moduleName + '.InputFormViewModel', InputFormViewModel)
  .controller(moduleName + '.PluginsTabViewModel', PluginsTabViewModel)
  .controller(moduleName + '.PluginsContextViewModel', PluginsContextViewModel)
  //
  // //TABS
  .controller(moduleName + '.HierarchyViewModel', HierarchyViewModel);

// NG2
//.controller(moduleName +  '.NG2Example',  downgradeComponent({component: NG2Example2Component}) );
