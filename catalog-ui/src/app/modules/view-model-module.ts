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

import {AddCategoryModalViewModel} from "../view-models/admin-dashboard/add-category-modal/add-category-modal-view-model";
import {WorkspaceViewModel} from "../view-models/workspace/workspace-view-model";
import {PropertyFormBaseView} from "../view-models/forms/property-forms/base-property-form/property-form-base-model";
import {PropertyFormViewModel} from "../view-models/forms/property-forms/component-property-form/property-form-view-model";
import {ModulePropertyView} from "../view-models/forms/property-forms/module-property-modal/module-property-model";
import {SelectDataTypeViewModel} from "../view-models/forms/property-forms/select-datatype-modal/select-datatype-modal-view-model";
import {OnboardVendorViewModel} from "../view-models/onboard-vendor/onboard-vendor-view-model";
import {ErrorViewModel} from "../view-models/modals/error-modal/error-view-model";
import {AdminDashboardViewModel} from "../view-models/admin-dashboard/admin-dashboard-view-model";
import {UserManagementViewModel} from "../view-models/admin-dashboard/user-management/user-management-view-model";
import {CategoryManagementViewModel} from "../view-models/admin-dashboard/category-management/category-management-view-model";
import {DcaeAppViewModel} from "../view-models/dcae-app/dcae-app-view-model";
import {GeneralViewModel} from "../view-models/workspace/tabs/general/general-view-model";
import {IconsModalViewModel} from "../view-models/modals/icons-modal/icons-modal-view";
import {PropertiesViewModel} from "../view-models/workspace/tabs/properties/properties-view-model";
import {ManagementWorkflowViewModel} from "../view-models/workspace/tabs/management-workflow/management-workflow-view-model";
import {InterfaceOperationViewModel} from "../view-models/workspace/tabs/interface-operation/interface-operation-view-model";
import {NetworkCallFlowViewModel} from "../view-models/workspace/tabs/network-call-flow/network-call-flow-view-model";
import {InterfaceDefinitionViewModel} from "../view-models/workspace/tabs/interface-definition/interface-definition-view-model";
import {DataTypeWorkspaceComponent} from "../ng2/pages/data-type-workspace/data-type-workspace.component";
import {downgradeComponent} from "@angular/upgrade/static";
import {WorkspaceMenuComponent} from "../ng2/pages/data-type-workspace/workspace-menu/workspace-menu.component";
import {TypeWorkspaceGeneralComponent} from "../ng2/pages/data-type-workspace/type-workspace-general/type-workspace-general.component";

let moduleName:string = 'Sdc.ViewModels';
let viewModelModule:ng.IModule = angular.module(moduleName, []);

viewModelModule
  .controller(moduleName + '.PropertyFormBaseView', PropertyFormBaseView)
  .controller(moduleName + '.PropertyFormViewModel', PropertyFormViewModel)
  .controller(moduleName + '.ModulePropertyView', ModulePropertyView)
  .controller(moduleName + '.SelectDataTypeViewModel', SelectDataTypeViewModel)
  .controller(moduleName + '.OnboardVendorViewModel', OnboardVendorViewModel)
  .controller(moduleName + '.ErrorViewModel', ErrorViewModel)
  .controller(moduleName + '.AdminDashboardViewModel', AdminDashboardViewModel)
  .controller(moduleName + '.AddCategoryModalViewModel', AddCategoryModalViewModel)
  .controller(moduleName + '.UserManagementViewModel', UserManagementViewModel)
  .controller(moduleName + '.CategoryManagementViewModel', CategoryManagementViewModel)
  .controller(moduleName + '.IconsModalViewModel', IconsModalViewModel)
  .controller(moduleName + '.DcaeAppViewModel', DcaeAppViewModel)
  //
  // //NEW
  .controller(moduleName + '.WorkspaceViewModel', WorkspaceViewModel)
  .controller(moduleName + '.GeneralViewModel', GeneralViewModel)
  .controller(moduleName + '.PropertiesViewModel', PropertiesViewModel)
  .controller(moduleName + '.ManagementWorkflowViewModel', ManagementWorkflowViewModel)
  .controller(moduleName + '.InterfaceOperationViewModel', InterfaceOperationViewModel)
  .controller(moduleName + '.InterfaceDefinitionViewModel', InterfaceDefinitionViewModel)
  .controller(moduleName + '.NetworkCallFlowViewModel', NetworkCallFlowViewModel)
  .controller(moduleName + '.DataTypeWorkspaceComponent', downgradeComponent({ component: DataTypeWorkspaceComponent }))
  .controller(moduleName + '.WorkspaceMenuComponent', downgradeComponent({ component: WorkspaceMenuComponent }))
  .controller(moduleName + '.TypeWorkspaceGeneralComponent', downgradeComponent({ component: TypeWorkspaceGeneralComponent }));
