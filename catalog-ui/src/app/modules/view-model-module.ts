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

import {PropertyFormBaseView} from "../view-models/forms/property-forms/base-property-form/property-form-base-model";
import {PropertyFormViewModel} from "../view-models/forms/property-forms/component-property-form/property-form-view-model";
import {ModulePropertyView} from "../view-models/forms/property-forms/module-property-modal/module-property-model";
import {SelectDataTypeViewModel} from "../view-models/forms/property-forms/select-datatype-modal/select-datatype-modal-view-model";
import {OnboardVendorViewModel} from "../view-models/onboard-vendor/onboard-vendor-view-model";
import {ErrorViewModel} from "../view-models/modals/error-modal/error-view-model";
import {DcaeAppViewModel} from "../view-models/dcae-app/dcae-app-view-model";
import {IconsModalViewModel} from "../view-models/modals/icons-modal/icons-modal-view";
import {TypeWorkspaceComponent} from "../ng2/pages/type-workspace/type-workspace.component";
import {downgradeComponent} from "@angular/upgrade/static";
import {WorkspaceMenuComponent} from "../ng2/pages/type-workspace/workspace-menu/workspace-menu.component";
import {TypeWorkspaceGeneralComponent} from "../ng2/pages/type-workspace/type-workspace-general/type-workspace-general.component";

let moduleName:string = 'Sdc.ViewModels';
let viewModelModule:ng.IModule = angular.module(moduleName, []);

viewModelModule
  .controller(moduleName + '.PropertyFormBaseView', PropertyFormBaseView)
  .controller(moduleName + '.PropertyFormViewModel', PropertyFormViewModel)
  .controller(moduleName + '.ModulePropertyView', ModulePropertyView)
  .controller(moduleName + '.SelectDataTypeViewModel', SelectDataTypeViewModel)
  .controller(moduleName + '.OnboardVendorViewModel', OnboardVendorViewModel)
  .controller(moduleName + '.ErrorViewModel', ErrorViewModel)
  .controller(moduleName + '.IconsModalViewModel', IconsModalViewModel)
  .controller(moduleName + '.DcaeAppViewModel', DcaeAppViewModel)
  .controller(moduleName + '.TypeWorkspaceComponent', downgradeComponent({ component: TypeWorkspaceComponent }))
  .controller(moduleName + '.WorkspaceMenuComponent', downgradeComponent({ component: WorkspaceMenuComponent }))
  .controller(moduleName + '.TypeWorkspaceGeneralComponent', downgradeComponent({ component: TypeWorkspaceGeneralComponent }));
